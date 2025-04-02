package com.example.myapplication.network.response

import kotlinx.serialization.Serializable

@Serializable
data class ResponseUserPermission(
  val user: UserData,
  val permission: DataAccess
)

@Serializable
data class ResponsePermission(
  val permission: DataAccess,
)