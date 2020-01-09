package com.example.bachelor

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.bachelor.signal.SessionGenerator
import com.example.bachelor.smime.SmimeUtils
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.io.InputStream
import java.security.Security


class MainActivity : AppCompatActivity() {

    private var btn: Button? = null
    private var comBtn: Button? = null
    private var prefs: SharedPreferences? = null

    companion object {
        var tvresult: TextView? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvresult = findViewById(R.id.tvresult)

        btn = findViewById(R.id.btn)

        Security.addProvider(BouncyCastleProvider())

        btn!!.setOnClickListener {
            val intent = Intent(this@MainActivity, ScanActivity::class.java)
            startActivity(intent)
        }

        prefs = getSharedPreferences("de.adorsys.bachelor", MODE_PRIVATE)
    }

    override fun onResume() {
        super.onResume()

        if (prefs!!.getBoolean("firstrun", true)) {
            prefs!!.edit().putBoolean("firstrun", false).apply()
        }
    }


    fun startGreeting(view: View) {
        SmimeUtils.keystoreInputStream = resources.openRawResource(
            resources.getIdentifier(
                "smime",
                "raw", packageName
            )
        )

        SessionGenerator().startCommunication()
    }


}