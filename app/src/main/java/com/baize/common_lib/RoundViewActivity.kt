package com.baize.common_lib

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.baize.common_lib.databinding.ActivityRabbitMqActivityBinding
import com.baize.common_lib.databinding.ActivityRoundViewBinding

class RoundViewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRoundViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoundViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById<View>(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.tvText.setOnClickListener {
            Toast.makeText(this, "哈哈", Toast.LENGTH_SHORT).show()
        }
    }
}