package com.sting.commandrunner

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity : Activity() {

    // 命令列表
    private val commands = arrayOf(
        "cmd appops set com.sting.virtualloc android:mock_location allow",
        "settings put global mock_location_enforced 0",
        "settings put secure mock_location_app com.sting.virtualloc",
        "settings put global development_settings_enabled 0"
    )

    // 当前步骤 (0-3)
    private var currentStep = 0

    private lateinit var tvTitle: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvCommand: TextView
    private lateinit var tvResult: TextView
    private lateinit var btnNext: Button
    private lateinit var btnReboot: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        updateUI()
    }

    private fun initViews() {
        tvTitle = findViewById(R.id.tvTitle)
        tvStatus = findViewById(R.id.tvStatus)
        tvCommand = findViewById(R.id.tvCommand)
        tvResult = findViewById(R.id.tvResult)
        btnNext = findViewById(R.id.btnNext)
        btnReboot = findViewById(R.id.btnReboot)

        btnNext.setOnClickListener { executeCurrentCommand() }
        btnReboot.setOnClickListener { rebootDevice() }
    }

    private fun updateUI() {
        if (currentStep < commands.size) {
            tvStatus.text = "第 ${currentStep + 1} / ${commands.size} 步"
            tvCommand.text = commands[currentStep]
            btnNext.text = "执行第 ${currentStep + 1} 步"
            btnNext.isEnabled = true
        } else {
            tvStatus.text = "全部命令已执行完成"
            tvCommand.text = ""
            btnNext.isEnabled = false
            btnNext.visibility = View.GONE
        }

        btnReboot.isEnabled = currentStep >= commands.size
    }

    private fun executeCurrentCommand() {
        if (currentStep >= commands.size) return

        btnNext.isEnabled = false
        tvResult.text = "正在执行..."

        Thread {
            val result = runCommand(commands[currentStep])

            runOnUiThread {
                if (result.first) {
                    tvResult.text = "✅ 执行成功"
                } else {
                    tvResult.text = "❌ " + result.second
                }

                currentStep++
                updateUI()
            }
        }.start()
    }

    private fun runCommand(cmd: String): Pair<Boolean, String> {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", cmd))
            val output = process.inputStream.bufferedReader().readText()
            val error = process.errorStream.bufferedReader().readText()
            val exitCode = process.waitFor()

            if (exitCode == 0) {
                Pair(true, output)
            } else {
                // 如果su不可用，尝试直接执行
                val process2 = Runtime.getRuntime().exec(arrayOf("sh", "-c", cmd))
                val output2 = process2.inputStream.bufferedReader().readText()
                val error2 = process2.errorStream.bufferedReader().readText()
                val exitCode2 = process2.waitFor()

                if (exitCode2 == 0) {
                    Pair(true, output2)
                } else {
                    Pair(false, error2.ifEmpty { "执行失败 (exit: $exitCode2)" })
                }
            }
        } catch (e: Exception) {
            // 尝试不需要su的方式
            try {
                val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", cmd))
                val output = process.inputStream.bufferedReader().readText()
                val error = process.errorStream.bufferedReader().readText()
                val exitCode = process.waitFor()

                if (exitCode == 0) {
                    Pair(true, output)
                } else {
                    Pair(false, error.ifEmpty { "exit: $exitCode" })
                }
            } catch (e2: Exception) {
                Pair(false, e2.message ?: "未知错误")
            }
        }
    }

    private fun rebootDevice() {
        btnReboot.isEnabled = false
        btnReboot.text = "正在重启..."

        Thread {
            try {
                Runtime.getRuntime().exec(arrayOf("su", "-c", "reboot"))
            } catch (e: Exception) {
                try {
                    Runtime.getRuntime().exec(arrayOf("sh", "-c", "reboot"))
                } catch (e2: Exception) {
                    runOnUiThread {
                        Toast.makeText(this, "重启失败，请手动重启", Toast.LENGTH_LONG).show()
                        btnReboot.isEnabled = true
                        btnReboot.text = "重启手机（强制）"
                    }
                    return@Thread
                }
            }

            runOnUiThread {
                Toast.makeText(this, "正在重启...", Toast.LENGTH_SHORT).show()
            }
        }.start()
    }
}