package com.example.myapplication.data

import android.content.Context
import com.example.myapplication.network.request.RequestAccessApproving
import com.example.myapplication.network.request.RequestAccessDoctor
import com.example.myapplication.network.request.RequestAuthLogin
import com.example.myapplication.network.request.RequestAuthRegister
import com.example.myapplication.network.request.RequestHealthRecordAdd
import com.example.myapplication.network.request.RequestHealthRecordUpdate
import com.example.myapplication.network.request.RequestHealthRecordUpdateNoFile
import com.example.myapplication.network.request.RequestPermissionGrantDoctor
import com.example.myapplication.network.request.RequestPermissionRevokeDoctor
import com.example.myapplication.network.response.ResponseAccess
import com.example.myapplication.network.response.ResponseAuthLogin
import com.example.myapplication.network.response.ResponseHealthRecord
import com.example.myapplication.network.response.ResponseHealthRecords
import com.example.myapplication.network.response.ResponseMessage
import com.example.myapplication.network.response.ResponsePermission
import com.example.myapplication.network.response.ResponseTransaction
import com.example.myapplication.network.response.ResponseUserPermission
import java.io.File

interface PMRRepository {
  suspend fun getIsUserRegistered(address: String): Boolean

  suspend fun register(
    requestRegister: RequestAuthRegister
  ): ResponseMessage<ResponseTransaction?>

  suspend fun login(
    requestLogin: RequestAuthLogin
  ): ResponseMessage<ResponseAuthLogin?>

  // Users
  suspend fun getUserByAddress(
    authToken: String,
    address: String
  ): ResponseMessage<ResponseUserPermission?>

  suspend fun getDoctorPermission(
    authToken: String,
    addressDoctor: String,
    addressPatient: String
  ): ResponseMessage<ResponsePermission?>

  // Health Records
  suspend fun postHealthRecords(
    authToken: String,
    mimeType: String,
    request: RequestHealthRecordAdd
  ): ResponseMessage<ResponseTransaction?>

  suspend fun getHealthRecords(authToken: String, address: String):
    ResponseMessage<ResponseHealthRecords?>

  suspend fun putHealthRecords(
    authToken: String,
    address: String,
    recordId: String,
    mimeType: String,
    request: RequestHealthRecordUpdate,
  ): ResponseMessage<ResponseTransaction?>

  suspend fun putHealthRecordNoFile(
    authToken: String,
    address: String,
    recordId: String,
    request: RequestHealthRecordUpdateNoFile,
  ): ResponseMessage<ResponseTransaction?>

  suspend fun deleteHealthRecord(
    authToken: String,
    address: String,
    recordId: String,
  ): ResponseMessage<ResponseTransaction?>

  suspend fun getHealthRecord(authToken: String, address: String, recordId: String):
    ResponseMessage<ResponseHealthRecord?>

  suspend fun getFileByCID(
    context: Context,
    authToken: String,
    address: String,
    cid: String,
  ): File?

  suspend fun getPatientDoctors(
    authToken: String,
  ): ResponseMessage<ResponseAccess?>

  suspend fun getDoctorPatients(
    authToken: String,
  ): ResponseMessage<ResponseAccess?>

  suspend fun putRequestAccessPatientApproving(
    authToken: String,
    request: RequestAccessApproving,
  ): ResponseMessage<ResponseTransaction?>

  suspend fun putRequestAccessDoctor(
    token: String,
    request: RequestAccessDoctor,
  ): ResponseMessage<ResponseTransaction?>

  suspend fun putPatientRevokeDoctorPermission(
    authToken: String,
    request: RequestPermissionRevokeDoctor,
  ): ResponseMessage<ResponseTransaction?>

  suspend fun putPatientPermissionDoctor(
    authToken: String,
    request: RequestPermissionGrantDoctor,
  ): ResponseMessage<ResponseTransaction?>
}