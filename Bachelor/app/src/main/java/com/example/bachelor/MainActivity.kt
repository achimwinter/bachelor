package com.example.bachelor

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import io.grpc.ManagedChannelBuilder
import io.grpc.stub.StreamObserver

class MainActivity : AppCompatActivity() {
    private var btn: Button? = null
    private var comBtn: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvresult = findViewById(R.id.tvresult)
        greetResult = findViewById(R.id.greet_result)

        btn = findViewById(R.id.btn) as Button


        btn!!.setOnClickListener {
            val intent = Intent(this@MainActivity, ScanActivity::class.java)
            startActivity(intent)
        }

    }

    companion object {
        var greetResult: TextView? = null
        var tvresult: TextView? = null
    }


    fun startGreeting(view: View) {

        val host = "192.168.2.94"
        val port = 50051
        val message = "Hello from Client"



        val mChannel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build()
        val stub = GreeterGrpc.newBlockingStub(mChannel)
        val request = HelloRequest.newBuilder().setName(message).build()
        val reply = stub.sayHello(request)
        reply.message
    }


}