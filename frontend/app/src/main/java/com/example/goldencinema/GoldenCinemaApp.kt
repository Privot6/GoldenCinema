package com.example.goldencinema

import android.app.Application

class GoldenCinemaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        TokenStore.init(this)
    }
}
