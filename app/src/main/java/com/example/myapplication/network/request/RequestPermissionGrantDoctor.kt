package com.example.myapplication.network.request

data class RequestPermissionGrantDoctor(
  val addressDoctor: String,
  val canCreate: Boolean,
  val canRead: Boolean,
  val canUpdate: Boolean,
  val canDelete: Boolean
)