package com.example.myapplication.network.request

import java.io.File

data class RequestHealthRecordUpdate(
  val file: File,
  val description: String,
  val recordType: String,
)