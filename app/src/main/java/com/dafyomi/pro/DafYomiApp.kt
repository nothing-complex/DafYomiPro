package com.dafyomi.pro

import android.app.Application

class DafYomiApp : Application() {

    val repository by lazy { com.dafyomi.pro.domain.DafRepository() }

    override fun onCreate() {
        super.onCreate()
    }
}
