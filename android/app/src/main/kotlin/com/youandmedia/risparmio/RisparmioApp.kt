package com.youandmedia.risparmio

import android.app.Application
import com.youandmedia.risparmio.data.AppDatabase

class RisparmioApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
}
