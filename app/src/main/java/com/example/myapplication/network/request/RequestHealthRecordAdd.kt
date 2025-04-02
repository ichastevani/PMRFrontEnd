package com.example.myapplication.network.request

import java.io.File

data class RequestHealthRecordAdd(
  val file: File,
  val patientAddress: String,
  val description: String,
  val recordType: String
)