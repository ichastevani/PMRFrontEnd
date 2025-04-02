package com.example.myapplication.network.response

import kotlinx.serialization.Serializable

@Serializable
data class ResponseMessage<T>(
  val status: String,
  val message: String,
  val data: T? = null
)

@Serializable
data class ResponseMessageNoData(
  val status: String,
  val message: String,
)