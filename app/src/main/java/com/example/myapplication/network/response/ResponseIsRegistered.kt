package com.example.myapplication.network.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResponseIsRegistered(
  val isRegistered: Boolean
)