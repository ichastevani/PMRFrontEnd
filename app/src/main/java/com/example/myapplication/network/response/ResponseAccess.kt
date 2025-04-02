package com.example.myapplication.network.response

import kotlinx.serialization.Serializable

@Serializable
data class DataAccess(
  val canCreate: Boolean,
  val canRead: Boolean,
  val canUpdate: Boolean,
  val canDelete: Boolean,
)

@Serializable
data class DataPermission(
  val user: UserData,
  val access: DataAccess,
)

@Serializable
data class ResponseAccess(
  val permissions: ArrayList<DataPermission>,
  val requestAccess: ArrayList<DataPermission>,
)
