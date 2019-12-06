package com.example.bachelor

import android.app.Activity
import android.os.AsyncTask
import android.text.TextUtils
import android.widget.Button
import android.widget.TextView
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import java.io.PrintWriter
import java.io.StringWriter
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit

private class GrpcTask constructor(activity: Activity) : AsyncTask<String, Void, String>() {
    private val activityReference: WeakReference<Activity> = WeakReference(activity)
    private var channel: ManagedChannel? = null

    override fun doInBackground(vararg params: String): String {
        val host = params[0]
        val message = params[1]
        val portStr = params[2]
        val port = if (TextUtils.isEmpty(portStr)) 0 else Integer.valueOf(portStr)
        return try {
            channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build()
            val stub = GreeterGrpc.newBlockingStub(channel)
            val request = HelloRequest.newBuilder().setName(message).build()
            val reply = stub.sayHello(request)
            reply.message
        } catch (e: Exception) {
            val sw = StringWriter()
            val pw = PrintWriter(sw)
            e.printStackTrace(pw)
            pw.flush()

            "Failed... : %s".format(sw)
        }
    }

    override fun onPostExecute(result: String) {
        try {
            channel?.shutdown()?.awaitTermination(1, TimeUnit.SECONDS)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }

        val activity = activityReference.get() ?: return
//        val resultText: TextView = activity.findViewById(R.id.grpc_response_text)
//        val sendButton: Button = activity.findViewById(R.id.send_button)

//        resultText.text = result
//        sendButton.isEnabled = true
    }
}