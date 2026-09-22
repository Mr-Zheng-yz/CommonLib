package com.example.mq_lib;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.ocamara.common_libs.utils.LogUtil;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * RabbitMQ 全局管理器（单例）
 * 设计要点：
 * 1. 全局唯一：单例 + 单线程调度器，建连/重连/发布全部串行执行，天然避免并发建连
 * 2. 自动重连：断线后指数退避重试，次数与间隔由 Config 控制；
 *    重连成功后自动重建通道、重新声明队列、重新绑定、重新消费
 * 3. 声明式订阅：外部只传入 exchange → routingKeys 绑定表（{@link #subscribe}），
 *    queueBind / basicConsume 由内部完成；绑定前自动探测 exchange，不存在仅告警并跳过
 * 4. 资源清理：每次建连前强制 abort 旧 Connection/Channel，杜绝连接泄漏
 * 5. 参数外置：连接参数通过 {@link Config} 注入
 * 6. 状态机：{@link State} 可查询，变化时主线程回调；消息通过 {@link MessageListener} 主线程回调
 * 注意：刻意未开启 amqp-client 内置的 automaticRecovery，重连统一由本管理器接管，
 *       避免内置恢复与手动重连两条路径并发建连（设备端连接数爆炸的常见根因）。
 */
public final class RabbitMqManager {

    private static final String TAG = "RabbitMqManager";

    // 队列声明参数（与原 MqService 行为保持一致）
    private static final boolean QUEUE_DURABLE = true;  //是否持久化（Broker 重启后队列不丢失）
    private static final boolean QUEUE_EXCLUSIVE = true;  //排他队列，仅声明它的连接可用 注意：排他队列随连接断开而删除，断线期间的消息会丢失
    private static final boolean QUEUE_AUTO_DELETE = false; //最后一个消费者断开后自动删除

    // ==================== 状态定义 ====================

    public enum State {
        IDLE,           // 初始/未连接
        CONNECTING,     // 初始化连接中
        CONNECTED,      // 正常
        RECONNECTING,   // 断线重试中
        DISCONNECTED,   // 已断开（重试次数耗尽）
        CLOSED          // 已手动释放，不再自动重连
    }

    /** 状态监听，回调在主线程 */
    public interface StateListener {
        void onStateChanged(State oldState, State newState, String detail);
    }

    /** 消息监听，回调在主线程 */
    public interface MessageListener {
        void onMessage(String exchange, String routingKey, String message);
    }

    // ==================== 配置（外部传入） ====================

    public static final class Config {
        public final String host;
        public final int port;
        public final String username;
        public final String password;
        public final String virtualHost;
        public final int connectionTimeoutMs;   // TCP 连接超时
        public final int handshakeTimeoutMs;    // AMQP 握手超时
        public final int requestedHeartbeatSec; // 心跳，建议 30s，过长会导致僵尸连接堆积
        public final int maxRetryCount;         // 最大重试次数，-1 为不限
        public final long baseRetryDelayMs;     // 退避起始间隔
        public final long maxRetryDelayMs;      // 退避上限

        private Config(Builder b) {
            this.host = b.host;
            this.port = b.port;
            this.username = b.username;
            this.password = b.password;
            this.virtualHost = b.virtualHost;
            this.connectionTimeoutMs = b.connectionTimeoutMs;
            this.handshakeTimeoutMs = b.handshakeTimeoutMs;
            this.requestedHeartbeatSec = b.requestedHeartbeatSec;
            this.maxRetryCount = b.maxRetryCount;
            this.baseRetryDelayMs = b.baseRetryDelayMs;
            this.maxRetryDelayMs = b.maxRetryDelayMs;
        }

        boolean isValid() {
            return host != null && !host.isEmpty() && port > 0 && port <= 65535;
        }

        public static final class Builder {
            private final String host;
            private final String username;
            private final String password;
            private int port = 5672;
            private String virtualHost = "/";
            private int connectionTimeoutMs = 10_000;
            private int handshakeTimeoutMs = 10_000;
            private int requestedHeartbeatSec = 30;
            private int maxRetryCount = -1;
            private long baseRetryDelayMs = 1_000;
            private long maxRetryDelayMs = 60_000;

            public Builder(String host, String username, String password) {
                this.host = host;
                this.username = username;
                this.password = password;
            }

            public Builder port(int port) { this.port = port; return this; }
            public Builder virtualHost(String vh) { this.virtualHost = vh; return this; }
            public Builder connectionTimeoutMs(int ms) { this.connectionTimeoutMs = ms; return this; }
            public Builder handshakeTimeoutMs(int ms) { this.handshakeTimeoutMs = ms; return this; }
            public Builder requestedHeartbeatSec(int sec) { this.requestedHeartbeatSec = sec; return this; }
            public Builder maxRetryCount(int count) { this.maxRetryCount = count; return this; }
            public Builder baseRetryDelayMs(long ms) { this.baseRetryDelayMs = ms; return this; }
            public Builder maxRetryDelayMs(long ms) { this.maxRetryDelayMs = ms; return this; }
            public Config build() { return new Config(this); }
        }

        @Override
        public String toString() {
            return "Config{" +
                    "host='" + host + '\'' +
                    ", port=" + port +
                    ", username='" + username + '\'' +
                    ", password='" + password + '\'' +
                    ", virtualHost='" + virtualHost + '\'' +
                    ", connectionTimeoutMs=" + connectionTimeoutMs +
                    ", handshakeTimeoutMs=" + handshakeTimeoutMs +
                    ", requestedHeartbeatSec=" + requestedHeartbeatSec +
                    ", maxRetryCount=" + maxRetryCount +
                    ", baseRetryDelayMs=" + baseRetryDelayMs +
                    ", maxRetryDelayMs=" + maxRetryDelayMs +
                    '}';
        }
    }

    /** 订阅描述：队列名 + exchange → routingKeys 绑定表 */
    private static final class QueueSubscription {
        final String queueName;
        final Map<String, List<String>> bindings;

        QueueSubscription(String queueName, Map<String, List<String>> bindings) {
            this.queueName = queueName;
            this.bindings = bindings;
        }
    }

    // ==================== 单例 ====================

    private static volatile RabbitMqManager instance;

    public static RabbitMqManager getInstance() {
        if (instance == null) {
            synchronized (RabbitMqManager.class) {
                if (instance == null) {
                    instance = new RabbitMqManager();
                }
            }
        }
        return instance;
    }

    // ==================== 内部成员 ====================

    /** 所有 MQ 操作串行执行的单线程调度器 */
    private final ScheduledExecutorService ioExecutor;
    private final Handler mainHandler;

    private volatile Config config;
    private volatile Connection connection;
    private volatile Channel channel;
    /** 手动释放标记，为 true 时不再自动重连 */
    private volatile boolean userClosed = true;

    private volatile QueueSubscription subscription;
    private volatile MessageListener messageListener;

    private final AtomicReference<State> state = new AtomicReference<>(State.IDLE);
    private final AtomicInteger retryCount = new AtomicInteger(0);
    private final Object channelLock = new Object();
    /** exchange 存在性探测缓存（避免每次发布/绑定都探测） */
    private final Map<String, Boolean> exchangeCache = new ConcurrentHashMap<>();
    private volatile StateListener stateListener;

    private Future<?> retryFuture;

    private RabbitMqManager() {
        ioExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "rabbitmq-io");
            t.setDaemon(true);
            return t;
        });
        mainHandler = new Handler(Looper.getMainLooper());
    }

    // ==================== 对外 API ====================

    /** 注入连接参数。参数变化后直接再调 {@link #connect()} 即可，内部会先清理旧连接 */
    public void init(Config config) {
        this.config = config;
    }

    /**
     * 声明式订阅：传入队列名与 exchange → routingKeys 绑定表。
     * <ul>
     *   <li>queueDeclare / queueBind / basicConsume 由内部完成，重连后自动重建</li>
     *   <li>绑定前自动探测 exchange，不存在仅告警并跳过，不影响其他绑定</li>
     *   <li>routingKeys 传空列表时以 "" 绑定（适用于 fanout 类型）</li>
     *   <li>运行中重复调用可更新订阅，会立即生效</li>
     * </ul>
     *
     * @param queueName 队列名
     * @param bindings  exchange → 该 exchange 下要绑定的 routingKey 列表
     * @param listener  消息回调（主线程）
     */
    public void subscribe(String queueName, Map<String, List<String>> bindings, MessageListener listener) {
        Map<String, List<String>> copy = new LinkedHashMap<>();
        if (bindings != null) {
            for (Map.Entry<String, List<String>> e : bindings.entrySet()) {
                List<String> keys = e.getValue();
                copy.put(e.getKey(), keys == null
                        ? Collections.<String>emptyList()
                        : new ArrayList<>(keys));
            }
        }
        this.subscription = new QueueSubscription(queueName, copy);
        this.messageListener = listener;

        // 已连接则立即应用：重建通道并执行拓扑
        if (isConnected()) {
            ioExecutor.execute(() -> {
                Connection c = connection;
                if (!userClosed && c != null && c.isOpen()) {
                    recreateChannel(c);
                }
            });
        }
    }

    /** 设置状态监听（单字段替换语义：重复调用覆盖旧值，传 null 即移除），回调在主线程 */
    public void setStateListener(StateListener listener) {
        this.stateListener = listener;
    }

    public State getState() {
        return state.get();
    }

    public boolean isConnected() {
        Channel ch = channel;
        Connection conn = connection;
        return state.get() == State.CONNECTED
                && conn != null && conn.isOpen()
                && ch != null && ch.isOpen();
    }

    public int getRetryCount() {
        return retryCount.get();
    }

    /** 建立连接（异步）。重复调用安全：会先取消未执行的重试、清理旧连接 */
    public void connect() {
        userClosed = false;
        retryCount.set(0);
        synchronized (this) {
            cancelPendingRetry();
        }
        ioExecutor.execute(this::doConnect);
    }

    /** 手动断开并停止自动重连（如 Service.onDestroy 时调用） */
    public void disconnect() {
        userClosed = true;
        synchronized (this) {
            cancelPendingRetry();
        }
        ioExecutor.execute(() -> {
            closeQuietly(connection, channel);
            setState(State.CLOSED, "manual disconnect");
        });
    }

    /** 发布消息（异步，fire-and-forget）。MQ 未就绪或 exchange 不存在时丢弃并打印警告 */
    public void publish(String exchange, String routingKey, String message) {
        ioExecutor.execute(() -> doPublish(exchange, routingKey, message));
    }

    /**
     * 探测 exchange 是否存在（结果带缓存）。
     * 注意：含网络操作，禁止主线程调用（内部已在 io 线程使用）。
     */
    public boolean isExchangeAvailable(String exchange) {
        if (exchange == null || exchange.isEmpty()) return true; // 默认交换机
        Boolean cached = exchangeCache.get(exchange);
        if (cached != null) return cached;

        Connection conn = connection;
        if (conn == null || !conn.isOpen()) return false;

        Channel probe = null;
        boolean ok = false;
        try {
            // 被动探测必须在独立通道上进行：探测失败会让通道被服务端关闭，不能连累主通道
            probe = conn.createChannel();
            probe.exchangeDeclarePassive(exchange);
            ok = true;
        } catch (Exception e) {
            LogUtil.wi(TAG, "!!exchange 探测失败(可能不存在): " + exchange + " -> " + e.getMessage());
        } finally {
            if (probe != null) {
                try { probe.abort(); } catch (Exception ignore) { }
            }
        }
        exchangeCache.put(exchange, ok);
        return ok;
    }

    // ==================== 内部实现 ====================

    /** 建连主流程，运行在 io 线程 */
    private void doConnect() {
        if (userClosed) return;

        Config cfg = config;
        if (cfg == null || !cfg.isValid()) {
            LogUtil.e(TAG, "!!!config 无效，取消连接");
            setState(State.IDLE, "invalid config");
            return;
        }
        if (isConnected()) {
            LogUtil.e(TAG, "!已连接，跳过重复建连");
            return;
        }

        setState(State.CONNECTING, "connecting " + cfg.host + ":" + cfg.port);
        closeQuietly(connection, channel); // 取消上一次连接，防止泄漏

        Connection newConn = null;
        Channel newChannel = null;
        try {
            ConnectionFactory factory = buildFactory(cfg);
            newConn = factory.newConnection(); // 阻塞调用，必须在 io 线程
            newChannel = newConn.createChannel();
            registerConnectionShutdownListener(newConn);
            registerChannelShutdownListener(newChannel);

            connection = newConn;
            channel = newChannel;
            retryCount.set(0);
            exchangeCache.clear(); // 服务端拓扑可能变化，重新探测
            setState(State.CONNECTED, "connected");
            runTopology(newChannel);
        } catch (Exception e) {
            LogUtil.e(TAG, "!!!MQ连接失败: " + Log.getStackTraceString(e));
            closeQuietly(newConn, newChannel);
            scheduleRetry("connect failed: " + e.getMessage());
        }
    }

    private ConnectionFactory buildFactory(Config cfg) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(cfg.host);
        factory.setPort(cfg.port);
        factory.setUsername(cfg.username);
        factory.setPassword(cfg.password);
        factory.setVirtualHost(cfg.virtualHost);
        factory.setConnectionTimeout(cfg.connectionTimeoutMs);
        factory.setHandshakeTimeout(cfg.handshakeTimeoutMs);
        factory.setRequestedHeartbeat(cfg.requestedHeartbeatSec);
        // 关键：关闭内置自动恢复，重连统一由本管理器接管，保证只有一条重连路径
        factory.setAutomaticRecoveryEnabled(false);
        factory.setTopologyRecoveryEnabled(false);
        return factory;
    }

    private void registerConnectionShutdownListener(final Connection conn) {
        conn.addShutdownListener(cause -> {
            // isInitiatedByApplication=true 说明是自己 abort/close 的，忽略
            if (userClosed || cause.isInitiatedByApplication()) return;
            if (connection != conn) return; // 旧连接的残留回调
            LogUtil.e(TAG, "!!!连接意外断开: " + cause.getMessage());
            scheduleRetry("connection shutdown: " + cause.getMessage());
        });
    }

    private void registerChannelShutdownListener(final Channel ch) {
        ch.addShutdownListener(cause -> {
            if (userClosed || cause.isInitiatedByApplication()) return;
            if (channel != ch) return; // 旧通道的残留回调
            LogUtil.e(TAG, "!!!通道意外关闭: " + cause.getMessage());
            // 连接还在则只重建通道，否则整体重连
            ioExecutor.execute(() -> {
                Connection c = connection;
                if (!userClosed && c != null && c.isOpen()) {
                    recreateChannel(c);
                } else {
                    scheduleRetry("channel shutdown and connection lost");
                }
            });
        });
    }

    private void recreateChannel(Connection conn) {
        try {
            closeChannelQuietly(); // 丢弃旧通道（连同旧消费者）
            Channel ch = conn.createChannel();
            registerChannelShutdownListener(ch);
            channel = ch;
            runTopology(ch);
        } catch (Exception e) {
            scheduleRetry("recreate channel failed: " + e.getMessage());
        }
    }

    /**
     * 拓扑建立：声明队列 → 逐个绑定 exchange/routingKey（自动探测，缺失跳过）→ 启动消费。
     * 运行在 io 线程，每次（重）连、通道重建、订阅更新后执行。
     */
    private void runTopology(Channel ch) {
        QueueSubscription sub = subscription;
        if (sub == null || sub.queueName == null || sub.queueName.isEmpty()) {
            LogUtil.i(TAG, "!!无订阅配置，仅保持连接");
            return;
        }
        try {
            ch.queueDeclare(sub.queueName, QUEUE_DURABLE, QUEUE_EXCLUSIVE, QUEUE_AUTO_DELETE, null);

            int boundCount = 0;
            for (Map.Entry<String, List<String>> entry : sub.bindings.entrySet()) {
                String exchange = entry.getKey();
                if (!isExchangeAvailable(exchange)) {
                    LogUtil.wi(TAG, "!!exchange 不存在，跳过绑定: " + exchange);
                    continue;
                }
                List<String> keys = entry.getValue();
                if (keys == null || keys.isEmpty()) {
                    ch.queueBind(sub.queueName, exchange, ""); // fanout 场景
                    LogUtil.wi(TAG, "---绑定[" + exchange + "]");
                    boundCount++;
                    continue;
                }
                for (String key : keys) {
                    ch.queueBind(sub.queueName, exchange, key);
                    LogUtil.wi(TAG, "---绑定[" + exchange + " / " + key + "]");
                    boundCount++;
                }
            }

            startConsume(ch, sub.queueName);
            LogUtil.wi(TAG, "-拓扑完成: queue=" + sub.queueName + ", 绑定数=" + boundCount);
        } catch (Exception e) {
            // 拓扑失败（如通道被服务端关闭），告警并整体重连
            LogUtil.e(TAG, "!!!拓扑建立失败: " + e.getMessage());
            scheduleRetry("topology failed: " + e.getMessage());
        }
    }

    /** 启动消费（autoAck，与原 MqService 行为一致），消息回调切到主线程 */
    private void startConsume(Channel ch, String queueName) throws IOException {
        if (messageListener == null) return;
        ch.basicConsume(queueName, true, new DefaultConsumer(ch) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope,
                                       AMQP.BasicProperties properties, byte[] body) {
                final String msg = new String(body, StandardCharsets.UTF_8);
                final String exchange = envelope.getExchange();
                final String routingKey = envelope.getRoutingKey();
                final MessageListener l = messageListener;
                if (l == null) return;
                mainHandler.post(() -> {
                    try {
                        l.onMessage(exchange, routingKey, msg);
                    } catch (Exception e) {
                        LogUtil.e(TAG, "!!!消息回调异常: " + e.getMessage());
                    }
                });
            }
        });
    }

    private void doPublish(String exchange, String routingKey, String message) {
        Channel ch = channel;
        if (!isConnected() || ch == null) {
            LogUtil.w(TAG, "!!!MQ 未就绪(state=" + state.get() + ")，消息丢弃: " + exchange);
            return;
        }
        if (!isExchangeAvailable(exchange)) {
            LogUtil.w(TAG, "!!!exchange 不存在，消息丢弃: " + exchange);
            return;
        }
        try {
            synchronized (channelLock) {
                ch.basicPublish(exchange, routingKey, null,
                        message.getBytes(StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            LogUtil.e(TAG, "!!!MQ消息发布失败: " + e.getMessage());
        }
    }

    /** 指数退避重试：base → ×2 → ×2 ... 封顶 maxRetryDelayMs */
    private synchronized void scheduleRetry(String reason) {
        if (userClosed) return;
        Config cfg = config;
        int max = cfg == null ? -1 : cfg.maxRetryCount;
        int attempt = retryCount.incrementAndGet();
        if (max >= 0 && attempt > max) {
            setState(State.DISCONNECTED, "重试次数耗尽(" + max + "): " + reason);
            return;
        }
        long base = cfg == null ? 1_000 : cfg.baseRetryDelayMs;
        long cap = cfg == null ? 60_000 : cfg.maxRetryDelayMs;
        long delay = base;
        for (int i = 1; i < attempt && delay < cap; i++) {
            delay = Math.min(cap, delay * 2);
        }
        setState(State.RECONNECTING, "第" + attempt + "次重试，" + delay + "ms 后执行，原因: " + reason);
        cancelPendingRetry();
        retryFuture = ioExecutor.schedule(this::doConnect, delay, TimeUnit.MILLISECONDS);
    }

    private void cancelPendingRetry() {
        if (retryFuture != null) {
            retryFuture.cancel(false);
            retryFuture = null;
        }
    }

    private void closeChannelQuietly() {
        Channel ch = channel;
        channel = null;
        if (ch != null) {
            try { ch.abort(); } catch (Exception ignore) { }
        }
    }

    /** 静默清理资源。abort 属于应用主动关闭，ShutdownListener 会识别并忽略 */
    private void closeQuietly(Connection conn, Channel ch) {
        if (ch != null) {
            try { ch.abort(); } catch (Exception ignore) { }
        }
        if (conn != null) {
            try { conn.abort(); } catch (Exception ignore) { }
        }
        if (connection == conn) {
            connection = null;
            channel = null;
        }
    }

    private void setState(State newState, String detail) {
        State old = state.getAndSet(newState);
        if (old == newState) return;
        StateListener l = stateListener;
        if (l == null) return;
        mainHandler.post(() -> {
            try {
                l.onStateChanged(old, newState, detail);
            } catch (Exception ignore) { }
        });
    }
}
