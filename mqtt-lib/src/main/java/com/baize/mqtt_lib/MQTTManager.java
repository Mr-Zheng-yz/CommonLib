package com.baize.mqtt_lib;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.baize.mqtt_lib.bean.MqttEntity;
import com.ocamara.common_libs.utils.LogUtil;

import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import androidx.annotation.NonNull;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * MQTT连接管理
 */
public class MQTTManager {
    private static final String TAG = "MQTTManager";
    private static volatile MQTTManager sInstance;
    public static MQTTManager getInstance() {
        if (sInstance == null) {
            synchronized (MQTTManager.class) {
                if (sInstance == null) {
                    sInstance = new MQTTManager();
                }
            }
        }
        return sInstance;
    }

    public static final String topicTest = "test_topic";    //测试数据，当心跳使
    public static final String testMsg = "testMessage";    //测试数据

    /***
     * qos有3个等级:0、1、2。
     * 0:最多一次,有可能重复或丢失；
     * 1:至少一次,有可能重复；
     * 2:只有一次,确保消息只到达一次。
     */
    public static final int QOS = 1;

    public static final int HANDLER_MQTT_RECONNECT_TASK = 600; //5秒重连间隔
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == HANDLER_MQTT_RECONNECT_TASK) {
                if(mqttClient != null) {
                    if (!mqttClient.isConnected()) {
                        connect();
                    } else {
                        subTopic();
                    }
                } else {
                    init(mqttEntity, mMQTTCallback);
                }
            }
        }
    };

    private MqttClient mqttClient;  //MQTT连接客户端对象
    private MqttConnectOptions connOpts;
    private MqttEntity mqttEntity; //MQTT配置
    private MQTTListener mMQTTCallback;
    private ScheduledExecutorService executor;

    // 定义 MQTT 回调方法
    private MqttCallback mMQTTNativeCallback = new MqttCallbackExtended() {
        @Override
        public void connectComplete(boolean reconnect, String serverURI) {
            LogUtil.d(TAG, "###MQTT 建立连接 connectComplete reconnect：" + reconnect
                    + " serverURI：" + serverURI + " 线程:" + Thread.currentThread().getName());
            if (mMQTTCallback != null) {
                mHandler.post(() -> mMQTTCallback.onMqttStateChange(true, serverURI));
            }
            subTopic();
        }

        @Override
        public void connectionLost(Throwable cause) {
            LogUtil.e(TAG, "###MQTT 连接丢失 Connection lost cause：" + cause + " 线程:" + Thread.currentThread().getName());
            if (mMQTTCallback != null) {
                mHandler.post(() -> mMQTTCallback.onMqttStateChange(false, cause.getMessage()));
            }
            reConnect();
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) {
            if (message == null) return;
            String messageStr = new String(message.getPayload());
            LogUtil.wi(TAG, "###MQTT 收到消息: " + messageStr + " --topic " + topic + " 线程:" + Thread.currentThread().getName());
            if(TextUtils.isEmpty(messageStr)) return;
            if (topicTest.equals(topic) || testMsg.equals(messageStr)) {
                LogUtil.wi(TAG, "测试消息，不处理..");
                return;
            }
            // 处理收到的消息
            if (mMQTTCallback != null) {
                mHandler.post(() -> mMQTTCallback.onMessageArrived(topic, messageStr));
            }
        }

        @Override
        public void deliveryComplete(org.eclipse.paho.client.mqttv3.IMqttDeliveryToken token) {
            LogUtil.d(TAG, "###消息传送发送完成..." + " 线程:" + Thread.currentThread().getName());
        }
    };

    public void init(MqttEntity mqttEntity, MQTTListener mqttListener) {
        LogUtil.d(TAG, "-init:" + mqttEntity);
        if (mqttEntity == null) {
            LogUtil.e(TAG, "!!!MQTT配置为Null");
            return;
        }
        cleanMqttClient();
        this.mqttEntity = mqttEntity;
        this.mMQTTCallback = mqttListener;
        try {
            initParams(mqttEntity);
            MemoryPersistence persistence = new MemoryPersistence();
            mqttClient = new MqttClient(mqttEntity.getBrokerUrl(), mqttEntity.getClientId(), persistence);
            mqttClient.setTimeToWait(30_000); //连接等待时长 单位：毫秒
            mqttClient.setCallback(mMQTTNativeCallback);
            executor = Executors.newScheduledThreadPool(1);
            executor.scheduleWithFixedDelay(() -> isConnect(true), 20, 10, TimeUnit.SECONDS);
            connect();
        } catch (MqttException e) {
            LogUtil.e(TAG, "Failed to create MQTT client:" + e.getMessage());
        }
    }

    private void initParams(MqttEntity mqttEntity) {
        connOpts = new MqttConnectOptions();
        connOpts.setUserName(mqttEntity.getUserName());
        connOpts.setPassword(mqttEntity.getPassword().toCharArray());
        connOpts.setCleanSession(true);//是否清理消息 true：设置为清除对话，服务器不会记录客户端的订阅信息和未接收的消息 false：为保留会话
        connOpts.setAutomaticReconnect(true);   // 开启自动重连功能
        connOpts.setMaxReconnectDelay(5000);    //设置每次断线重连之间的间隔 毫秒
        connOpts.setConnectionTimeout(20);
        connOpts.setKeepAliveInterval(15); // 设置心跳间隔（单位：秒）
    }

    // 连接到 MQTT 服务器
    private void connect() {
        try {
            long start = System.currentTimeMillis();
            mqttClient.connect(connOpts);
            LogUtil.wi(TAG, "-连接MQTT服务... 线程:" + Thread.currentThread().getName() + " 连接耗时：" + (System.currentTimeMillis() - start));
        } catch (MqttException e) {
            LogUtil.e(TAG, "-连接MQTT服务异常 err:" + e + " 线程:" + Thread.currentThread().getName());
            reConnect();
        }
    }

    //订阅主题
    private void subTopic() {
        for (String topic : mqttEntity.getTopics()) {
            try {
                LogUtil.wi(TAG, "订阅主题 topic：" + topic + " t:" + Thread.currentThread().getName());
                mqttClient.subscribe(topic, QOS);
            } catch (MqttException e) {
                LogUtil.e(TAG, "!!!订阅异常 topic:" + topic + " reason:" + e.getMessage());
            }
        }
    }

    private void reConnect() {
        if (!mHandler.hasMessages(HANDLER_MQTT_RECONNECT_TASK)) {
            mHandler.sendEmptyMessageDelayed(HANDLER_MQTT_RECONNECT_TASK, 5000);
        }
    }

    public boolean isConnect(boolean notify) {
        try {
            if(mqttClient != null && mqttClient.isConnected()) {
                if (mMQTTCallback != null && notify) {
                    mHandler.post(() -> mMQTTCallback.onMqttStateChange(true, "连接在线"));
                }
                return true;
            } else {
                if (mMQTTCallback != null && notify) {
                    mHandler.post(() -> mMQTTCallback.onMqttStateChange(false, "断开连接"));
                }
                reConnect();
                return false;
            }
        } catch (Exception e) {
            if (mMQTTCallback != null && notify) {
                mHandler.post(() -> mMQTTCallback.onMqttStateChange(false, e.getMessage()));
            }
            LogUtil.e(TAG,"!!!检查mqtt连接状态异常:" + e.getMessage());
            return false;
        }
    }

    /***
     * 推送MQTT消息
     * @param msg 消息文本
     */
    public String publishMsg(String topic, String msg) {
        LogUtil.wi(TAG, "MQTT发送消息 topic：" + topic + " msg：" + msg + " 线程:" + Thread.currentThread().getName());
        if (isConnect(false)) {
            try {
                MqttMessage message = new MqttMessage(msg.getBytes());
                message.setQos(QOS);      //设置消息质量等级QOS
                long start = System.currentTimeMillis();
                mqttClient.publish(topic, message);  //MQTT客户端推送消息
                LogUtil.d(TAG, "---MQTT发送消息等待时长:" + (System.currentTimeMillis() - start));
                return Constant.SUCCESS;
            } catch (Exception e) {
                LogUtil.e(TAG, "!!!MQTT发送数据异常 err:" + e);
                return e.getMessage();
            }
        } else {
            LogUtil.e(TAG, "!!!MQTT发送数据异常 MQTT当前未连接!!");
            return "MQTT当前未连接";
        }
    }

    /***
     * 关闭MQTT连接
     */
    public void cleanMqttClient(){
        try {
            LogUtil.e(TAG,"-cleanMqttClient:" + mqttEntity);
            if (mMQTTCallback != null) {
                mMQTTCallback.onMqttStateChange(false, "主动退出");
            }
            this.mqttEntity = null;
            this.mMQTTCallback = null;
            mHandler.removeMessages(HANDLER_MQTT_RECONNECT_TASK);
            if(mqttClient != null && mqttClient.isConnected()) {
                mqttClient.disconnect();
                mqttClient.close();
                mqttClient = null;
            }
            if (executor != null) {
                executor.shutdown();
                executor = null;
            }
        } catch (Exception e) {
            LogUtil.e(TAG,"!!!MQTT退出异常:" + e.getMessage());
        }
    }

    public MqttEntity getMqttEntity() {
        return mqttEntity;
    }

    /**
     * 消息到达接口回调
     */
    public interface MQTTListener {
        void onMessageArrived(String topic, String message);

        void onMqttStateChange(boolean online, String message);
    }
}
