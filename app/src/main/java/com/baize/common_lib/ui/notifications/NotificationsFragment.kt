package com.baize.common_lib.ui.notifications

import android.os.Bundle
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.baize.common_lib.Constants
import com.baize.common_lib.adapter.MqttMessageAdapter
import com.baize.common_lib.adapter.TopicAdapter
import com.baize.common_lib.databinding.FragmentNotificationsBinding
import com.baize.mqtt_lib.MQTTManager
import com.baize.mqtt_lib.bean.MqttEntity
import com.ocamara.common_libs.utils.LogUtil

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val topicAdapter: TopicAdapter = TopicAdapter();
    private val msgAdapter: MqttMessageAdapter = MqttMessageAdapter();

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        initView()

        return root
    }

    private fun initView() {
        _binding?.let { binding ->
            binding.etClientId.setText("05_2_NSR1560220240802_2333")
            binding.etBrokerUrl.setText("tcp://10.10.5.35:1883")
            binding.etUsername.setText("admin")
            binding.etPassword.setText("123456")
            binding.rvTopic.adapter = topicAdapter
            binding.rvTopic.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            binding.rvMsg.adapter = msgAdapter
            binding.rvMsg.layoutManager = LinearLayoutManager(context)
            topicAdapter.setNewList(
                listOf(Constants.TOPIC_CALL,
                    Constants.TOPIC_CALL_INFO,
                    Constants.TOPIC_CALL_VISIT,
                    Constants.TOPIC_BASE_CONFIG,
                    Constants.TOPIC_NURSING_STATION_PATIENT_CARD,
                    Constants.TOPIC_SCREEN_RESTART,
                    Constants.TOPIC_SCREEN_AUDIO,
                    Constants.TOPIC_SCREEN_LOGO,
                    Constants.TOPIC_SCREEN_SOUND_BRIGHT_CONFIG,
                    Constants.TOPIC_SURGICAL_NURSE_RECEIVE_NOTICE,
                    Constants.TOPIC_ROOM_SERVICE,
                    Constants.TOPIC_SMART_MONITOR_MESSAGE,
                    Constants.TOPIC_DOCTOR_LOCATION,
                    Constants.TOPIC_AUXILIARY_TIMING,
                    Constants.TOPIC_OUTING_MESSAGE, )
            )

            binding.btnConnect.setOnClickListener { connect() }
            binding.btnClear.setOnClickListener { msgAdapter.clear() }
            binding.btnDisConnect.setOnClickListener { MQTTManager.getInstance().cleanMqttClient() }
        }
    }

    private fun connect() {
        if (_binding == null) return
        val mqttEntity = MqttEntity()
        mqttEntity.clientId = binding.etClientId.text.toString()
        mqttEntity.brokerUrl = binding.etBrokerUrl.text.toString()
        mqttEntity.userName = binding.etUsername.text.toString()
        mqttEntity.password = binding.etPassword.text.toString()
        mqttEntity.topics = topicAdapter.getSelectList()
        LogUtil.d("MQTT", "-初始化MQTT:" + mqttEntity)

        MQTTManager.getInstance().init(mqttEntity, object : MQTTManager.MQTTListener {
            override fun onMessageArrived(topic: String?, message: String?) {
                LogUtil.d("MQTT", "--onMessageArrived topic:$topic message:$message")
                msgAdapter.add(Pair(topic, message))
            }

            override fun onMqttStateChange(online: Boolean, message: String?) {
                LogUtil.d("MQTT", "--onMqttStateChange online:$online message:$message")
                _binding?.tvTitle?.setText("MQTT状态：" + if (online) "在线" else "离线")
                Toast.makeText(context, "$message", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        MQTTManager.getInstance().cleanMqttClient()
    }
}