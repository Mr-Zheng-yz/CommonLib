package com.baize.common_lib

import android.os.Bundle
import android.text.TextUtils
import android.util.Pair
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.baize.common_lib.adapter.MqttMessageAdapter
import com.baize.common_lib.databinding.ActivityRabbitMqActivityBinding
import com.example.mq_lib.RabbitMqManager
import com.ocamara.common_libs.dialog.LoadingDialogFragment
import com.ocamara.common_libs.utils.LogUtil

class RabbitMQActivity : AppCompatActivity() {
    private val TAG = "RabbitMQActivity"
    private lateinit var binding: ActivityRabbitMqActivityBinding

    private val mqManager: RabbitMqManager = RabbitMqManager.getInstance()
    private val QUEUE_NAME: String = "baize_queue"

    private lateinit var msgAdapter: MqttMessageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        binding = ActivityRabbitMqActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        msgAdapter = MqttMessageAdapter()
        binding.rvMsg.layoutManager = LinearLayoutManager(this)
        binding.rvMsg.adapter = msgAdapter

        binding.btnLogin.setOnClickListener {
            initMq()
        }
        binding.btnDisconnect.setOnClickListener {
            mqManager.disconnect()
        }
        binding.btnClear.setOnClickListener {
            msgAdapter.clear()
        }
        updatePage(RabbitMqManager.State.IDLE)
    }

    private fun initMq() {
        val mqUser = binding.etUsername.text.toString()
        val mqPass = binding.etPassword.text.toString()
        val mqService = binding.etService.text.toString()
        val mqPort = binding.etPort.text.toString()
        if (TextUtils.isEmpty(mqService) || TextUtils.isEmpty(mqPort)) {
            Toast.makeText(this, "mq参数未配置", Toast.LENGTH_SHORT).show()
            return
        }
        val port: Int
        try {
            port = mqPort.toInt()
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "mq端口配置非法", Toast.LENGTH_SHORT).show()
            return
        }
        val config: RabbitMqManager.Config? = RabbitMqManager.Config.Builder(mqService, mqUser, mqPass)
            .port(port)
            .connectionTimeoutMs(10000)
            .requestedHeartbeatSec(60) //与原 MqService 设置保持一致
            .maxRetryCount(-1) //不限重试次数
            .baseRetryDelayMs(1000)
            .maxRetryDelayMs(30000)
            .build()
        LogUtil.d(TAG, "-mqConfig:$config")
        mqManager.init(config)
        mqManager.setStateListener { oldState, newState, detail ->
            updatePage(newState)
            Toast.makeText(this, "$detail", Toast.LENGTH_SHORT).show()
        }
        mqManager.subscribe(QUEUE_NAME, buildBindings(), ::dispatchMessage)
        mqManager.connect()
    }

    private fun updatePage(state: RabbitMqManager.State) {
        LogUtil.d(TAG, "-updatePage:${state}")
        LoadingDialogFragment.hideLoading(supportFragmentManager)
        when (state) {
            RabbitMqManager.State.IDLE, RabbitMqManager.State.CLOSED -> {
                binding.mslLayout.showContent()
                binding.llInput.visibility = View.VISIBLE
                binding.llConnected.visibility = View.GONE
            }
            RabbitMqManager.State.CONNECTING -> {
//                binding.mslLayout.showLoading("连接中...")
                LoadingDialogFragment.showLoading(supportFragmentManager, "连接中...")
            }
            RabbitMqManager.State.CONNECTED -> {
                binding.mslLayout.showContent()
                binding.llInput.visibility = View.GONE
                binding.llConnected.visibility = View.VISIBLE
            }
            RabbitMqManager.State.RECONNECTING -> {
//                binding.mslLayout.showLoading("重连中...")
                LoadingDialogFragment.showLoading(supportFragmentManager, "重连中...")
            }
            RabbitMqManager.State.DISCONNECTED -> {
                binding.mslLayout.showError("已到达最大重连次数，点击重连")
                binding.mslLayout.setRetryListener { mqManager.connect() }
            }
        }
    }

    private fun buildBindings(): MutableMap<String, MutableList<String>> {
        return mutableMapOf("exchange_ocamar_dwmws_server_time_topic" to arrayListOf("global"))
    }

    private fun dispatchMessage(exchange: String, routingKey: String, message: String) {
        msgAdapter.add(Pair.create("exchange:$exchange / routingKey:$routingKey", message))
    }

    override fun onDestroy() {
        super.onDestroy()
        mqManager.setStateListener(null)
        mqManager.disconnect()
    }

}