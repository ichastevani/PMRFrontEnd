package com.example.myapplication

import android.app.Application
import com.example.myapplication.data.AppContainer
import com.example.myapplication.network.DefaultAppContainer
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application()