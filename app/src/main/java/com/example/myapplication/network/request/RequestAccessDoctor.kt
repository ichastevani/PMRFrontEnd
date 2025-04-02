package com.example.myapplication.network.request

data class RequestAccessDoctor(
  val addressPatient: String,
  val canCreate: Boolean,
  val canRead: Boolean,
  val canUpdate: Boolean,
  val canDelete: Boolean
)