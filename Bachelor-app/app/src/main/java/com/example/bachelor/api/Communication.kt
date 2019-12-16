package com.example.bachelor.api

import android.content.Context
import com.example.bachelor.GreeterGrpc

import com.example.bachelor.signal.SessionGenerator

class Communication {

    fun start(context: Context) {
        val session = SessionGenerator().generateSession(context)


    }

}