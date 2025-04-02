package com.example.myapplication.network.response

import kotlinx.serialization.Serializable

@Serializable
data class ResponseTransaction(
  val transactionData: String
)