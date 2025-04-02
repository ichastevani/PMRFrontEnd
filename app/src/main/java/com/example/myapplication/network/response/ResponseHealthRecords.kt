package com.example.myapplication.network.response

import kotlinx.serialization.Serializable

@Serializable
data class ResponseHealthRecords(
  val healthRecords: ArrayList<DataHealthRecord>
)

@Serializable
data class ResponseHealthRecord(
  val healthRecord: DataHealthRecord
)

@Serializable
data class DataHealthRecord(
  val id: String,
  val creator: UserData,
  val description: String,
  val cid: String,
  val recordType: String,
  val createdAt: String,
  val isActive: String,
  val version: String,
  val previousId: String,
)