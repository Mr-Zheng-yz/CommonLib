package com.baize.mqtt_lib;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.baize.mqtt_lib.bean.MqttEntity;
import com.ocamara.common_libs.utils.LogUtil;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import androidx.annotation.NonNull;

public class MQTTManagerAndroid {
//    private static final String TAG = "MqttHelper";
//    private static volatile MQTTManagerAndroid sInstance;
//    public static MQTTManagerAndroid getInstance() {
//        if (sInstance == null) {
//            synchronized (MQTTManagerAndroid.class) {
//                if (sInstance == null) {
//                    sInstance = new MQTTManagerAndroid();
//                }
//            }
//        }
//        return sInstance;
//    }
//    /**
//     * qos有3个等级:0、1、2。
//     * 0:最多一次,有可能重复或丢失；
//     * 1:至少一次,有可能重复；
//     * 2:只有一次,确保消息只到达一次。
//     */
//    public static final int QOS = 1;
//    public static final int HANDLER_MQTT_RECONNECT_TASK = 600; //5秒重连间隔
//
//    private MqttAndroidClient mqttAndroidClient;
//    private Handler mHandler = new Handler(Looper.getMainLooper()) {
//        @Override
//        public void handleMessage(@NonNull Message msg) {
//            super.handleMessage(msg);
//            if (msg.what == HANDLER_MQTT_RECONNECT_TASK) {
//                if(mqttAndroidClient != null) {
//                    if (!mqttAndroidClient.isConnected()) {
//                        connect();
//                    } else {
//                        subscribe();
//                    }
//                } else {
//                    init(context, mqttEntity, mMQTTCallback);
//                }
//            }
//        }
//    };
//    private Context context; //MQTT配置
//    private MqttEntity mqttEntity; //MQTT配置
//    private MQTTListener mMQTTCallback;
//
//    public void init(Context context, MqttEntity mqttEntity, MQTTListener mMqttCallback) {
//        if (mqttEntity == null) {
//            LogUtil.e(TAG, "!!!MQTT配置为Null");
//            return;
//        }
//        cleanMqttClient();
//        this.context = context;
//        this.mqttEntity = mqttEntity;
//        this.mMQTTCallback = mMqttCallback;
//        mqttAndroidClient = new MqttAndroidClient(context.getApplicationContext(), mqttEntity.getBrokerUrl(), mqttEntity.getClientId());
//        setCallback();
//        connect();
//    }
//
//    public void connect() {
//        MqttConnectOptions options = new MqttConnectOptions();
//        options.setMaxReconnectDelay(5000);    //设置每次断线重连之间的间隔 毫秒
//        options.setAutomaticReconnect(true); // 开启自动重连
//        options.setConnectionTimeout(20);
//        options.setKeepAliveInterval(15); // 设置心跳间隔（单位：秒）
//        options.setCleanSession(true); //是否清理消息 true：设置为清除对话，服务器不会记录客户端的订阅信息和未接收的消息 false：为保留会话
//        options.setUserName(mqttEntity.getUserName());
//        options.setPassword(mqttEntity.getPassword().toCharArray());
//        LogUtil.wi(TAG, "-连接MQTT服务... 线程:" + Thread.currentThread().getName());
//        try {
//            mqttAndroidClient.connect(options, null, new IMqttActionListener() {
//                @Override
//                public void onSuccess(IMqttToken asyncActionToken) {
//                    LogUtil.wi(TAG, "连接成功");
//                    // 连接成功后可订阅主题
//                    subscribe();
//                }
//
//                @Override
//                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
//                    LogUtil.e(TAG, "连接失败:err" + exception);
//                    // 连接失败，可在此安排重试
//                    if (mMQTTCallback != null) {
//                        mMQTTCallback.onMqttStateChange(false, exception.getMessage());
//                    }
//                }
//            });
//        } catch (MqttException e) {
//            LogUtil.e(TAG, "!!!MQTT连接异常:" + e.getMessage());
//            if (mMQTTCallback != null) {
//                mMQTTCallback.onMqttStateChange(false, e.getMessage());
//            }
//        }
//    }
//
//    private void setCallback() {
//        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
//            @Override
//            public void connectComplete(boolean reconnect, String serverURI) {
//                LogUtil.wi(TAG, "连接完成，是否重连: " + reconnect);
//                if (mMQTTCallback != null) {
//                    mMQTTCallback.onMqttStateChange(true, serverURI);
//                }
//            }
//
//            @Override
//            public void connectionLost(Throwable cause) {
//                LogUtil.wi(TAG, "连接丢失:" + cause);
//                // 即使开启了自动重连，也可在此添加手动重连或日志记录[citation:2]
//                if (mMQTTCallback != null) {
//                    mMQTTCallback.onMqttStateChange(false, cause.getMessage());
//                }
//                reConnect();
//            }
//
//            @Override
//            public void messageArrived(String topic, MqttMessage message) throws Exception {
//                String payload = new String(message.getPayload());
//                LogUtil.wi(TAG, "收到消息: " + payload + " [主题: " + topic + "]" + " t:" + Thread.currentThread().getName());
//                // 处理收到的消息，例如使用EventBus分发[citation:2]
//                if (mMQTTCallback != null) {
//                    mMQTTCallback.onMessageArrived(topic, payload);
//                }
//            }
//
//            @Override
//            public void deliveryComplete(IMqttDeliveryToken token) {
//                LogUtil.d(TAG, "消息投递完成");
//            }
//        });
//    }
//
//    public boolean isConnect(boolean notify) {
//        try {
//            if(mqttAndroidClient != null && mqttAndroidClient.isConnected()) {
//                if (mMQTTCallback != null && notify) {
//                    mMQTTCallback.onMqttStateChange(true, "连接在线");
//                }
//                return true;
//            } else {
//                if (mMQTTCallback != null && notify) {
//                    mMQTTCallback.onMqttStateChange(false, "断开连接");
//                }
//                reConnect();
//                return false;
//            }
//        } catch (Exception e) {
//            if (mMQTTCallback != null && notify) {
//                mMQTTCallback.onMqttStateChange(false, e.getMessage());
//            }
//            LogUtil.e(TAG,"!!!检查mqtt连接状态异常:" + e.getMessage());
//            return false;
//        }
//    }
//
//    public void reConnect() {
//        if (!mHandler.hasMessages(HANDLER_MQTT_RECONNECT_TASK)) {
//            mHandler.sendEmptyMessageDelayed(HANDLER_MQTT_RECONNECT_TASK, 5000);
//        }
//    }
//
//    public void cleanMqttClient() {
//        try {
//            this.context = null;
//            this.mqttEntity = null;
//            this.mMQTTCallback = null;
//            mHandler.removeMessages(HANDLER_MQTT_RECONNECT_TASK);
//            if(mqttAndroidClient != null && mqttAndroidClient.isConnected()) {
//                mqttAndroidClient.disconnect();
//                mqttAndroidClient.close();
//                mqttAndroidClient = null;
//            }
//        } catch (Exception e) {
//            LogUtil.e(TAG,"!!!MQTT退出异常:" + e.getMessage());
//        }
//    }
//
//    public String publish(String topic, String message) {
//        try {
//            if (isConnect(false)) {
//                LogUtil.wi(TAG, "MQTT发送消息 topic：" + topic + " msg：" + message + " t:" + Thread.currentThread().getName());
//                MqttMessage mqttMessage = new MqttMessage(message.getBytes());
//                mqttMessage.setQos(1);
//                long start = System.currentTimeMillis();
//                mqttAndroidClient.publish(topic, mqttMessage);
//                LogUtil.d(TAG, "MQTT发送消息等待时长:" + (System.currentTimeMillis() - start));
//                return Constant.SUCCESS;
//            } else {
//                return "MQTT当前未连接";
//            }
//        } catch (MqttException e) {
//            LogUtil.e(TAG, "!!!消息发送异常:" + e.getMessage());
//            return e.getMessage();
//        }
//    }
//
//    public void subscribe() {
//        for (String topic : mqttEntity.getTopics()) {
//            try {
//                LogUtil.wi(TAG, "订阅主题 topic：" + topic + " t:" + Thread.currentThread().getName());
//                mqttAndroidClient.subscribe(topic, QOS);
//            } catch (MqttException e) {
//                LogUtil.e(TAG, "!!!订阅异常:" + e.getMessage());
//            }
//        }
//    }
//
//    /**
//     * 消息到达接口回调
//     */
//    public interface MQTTListener {
//        void onMessageArrived(String topic, String message);
//
//        void onMqttStateChange(boolean online, String message);
//    }
}