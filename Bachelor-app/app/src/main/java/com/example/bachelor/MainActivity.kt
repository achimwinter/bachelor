package com.example.bachelor

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.bachelor.signal.SessionGenerator
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.Security


class MainActivity : AppCompatActivity() {

    private lateinit var btn: Button
    private lateinit var prefs: SharedPreferences

    companion object {
        const val SCAN_RESULT_CODE = 888
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn = findViewById(R.id.btn)

        Security.addProvider(BouncyCastleProvider())

        btn.setOnClickListener {
            val intent = Intent(this@MainActivity, ScanActivity::class.java)
            startActivityForResult(intent, SCAN_RESULT_CODE)
        }

        prefs = getSharedPreferences("de.adorsys.bachelor", MODE_PRIVATE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == SCAN_RESULT_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val scanResult = data?.getStringExtra(ScanActivity.EXTRA_SCAN_RESULT)
                startGreeting(scanResult)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onResume() {
        super.onResume()

        if (prefs.getBoolean("firstrun", true)) {
            prefs.edit()?.putBoolean("firstrun", false)?.apply()
        }
    }

    fun startGreeting(scanResult: String?) {
        SessionGenerator().startCommunication(scanResult!!)
    }

}