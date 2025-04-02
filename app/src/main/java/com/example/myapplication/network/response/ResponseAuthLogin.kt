package com.example.myapplication.network.response

import kotlinx.serialization.Serializable

@Serializable
data class ResponseAuthLogin(
  val token: String,
  val user: UserData,
)

@Serializable
data class UserData(
  val name: String,
  val birthDate: String,
  val homeAddress: String,
  val role: String,
  val address: String,
)