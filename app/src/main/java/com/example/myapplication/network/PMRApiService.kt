package com.example.myapplication.network

import com.example.myapplication.network.request.RequestAccessApproving
import com.example.myapplication.network.request.RequestAccessDoctor
import com.example.myapplication.network.request.RequestAuthLogin
import com.example.myapplication.network.request.RequestAuthRegister
import com.example.myapplication.network.request.RequestHealthRecordUpdateNoFile
import com.example.myapplication.network.request.RequestPermissionGrantDoctor
import com.example.myapplication.network.request.RequestPermissionRevokeDoctor
import com.example.myapplication.network.response.ResponseAccess
import com.example.myapplication.network.response.ResponseAuthLogin
import com.example.myapplication.network.response.ResponseHealthRecord
import com.example.myapplication.network.response.ResponseHealthRecords
import com.example.myapplication.network.response.ResponseIsRegistered
import com.example.myapplication.network.response.ResponseMessage
import com.example.myapplication.network.response.ResponsePermission
import com.example.myapplication.network.response.ResponseTransaction
import com.example.myapplication.network.response.ResponseUserPermission
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Streaming

interface PMRApiService {
  @GET("open/users/{address}/check")
  suspend fun getIsUserRegistered(@Path("address") address: String):
    ResponseMessage<ResponseIsRegistered>

  // Auth
  @POST("auth/register")
  suspend fun register(
    @Body request: RequestAuthRegister,
  ): ResponseMessage<ResponseTransaction>

  @POST("auth/login")
  suspend fun login(
    @Body request: RequestAuthLogin,
  ): ResponseMessage<ResponseAuthLogin>

  // Users
  @GET("users/address/{address}")
  suspend fun getUserByAddress(
    @Header("Authorization") token: String,
    @Path("address") address: String
  ): ResponseMessage<ResponseUserPermission>

  @GET("/users/doctor-permission/{addressDoctor}/{addressPatient}")
  suspend fun getDoctorPermission(
    @Header("Authorization") token: String,
    @Path("addressDoctor") addressDoctor: String,
    @Path("addressPatient") addressPatient: String
  ): ResponseMessage<ResponsePermission>

  // Heath Records
  @Multipart
  @POST("health-records")
  suspend fun postHealthRecords(
    @Header("Authorization") token: String,
    @Part file: MultipartBody.Part,
    @Part("patientAddress") patientAddress: RequestBody,
    @Part("description") description: RequestBody,
    @Part("recordType") recordType: RequestBody
  ): ResponseMessage<ResponseTransaction>

  @GET("health-records/{address}")
  suspend fun getHealthRecords(
    @Header("Authorization") token: String,
    @Path("address") address: String
  ): ResponseMessage<ResponseHealthRecords>

  @Multipart
  @PUT("health-records/{address}/{recordId}")
  suspend fun putHealthRecords(
    @Header("Authorization") token: String,
    @Path("address") address: String,
    @Path("recordId") recordId: String,
    @Part file: MultipartBody.Part,
    @Part("description") description: RequestBody,
    @Part("recordType") recordType: RequestBody
  ): ResponseMessage<ResponseTransaction>

  @PUT("health-records/{address}/{recordId}/no-file")
  suspend fun putHealthRecordNoFile(
    @Header("Authorization") token: String,
    @Path("address") address: String,
    @Path("recordId") recordId: String,
    @Body request: RequestHealthRecordUpdateNoFile,
  ): ResponseMessage<ResponseTransaction>

  @GET("health-records/{address}/{recordId}")
  suspend fun getHealthRecord(
    @Header("Authorization") token: String,
    @Path("address") address: String,
    @Path("recordId") recordId: String,
  ): ResponseMessage<ResponseHealthRecord>

  @DELETE("health-records/{address}/{recordId}")
  suspend fun deleteHealthRecord(
    @Header("Authorization") token: String,
    @Path("address") address: String,
    @Path("recordId") recordId: String,
  ): ResponseMessage<ResponseTransaction>

  @GET("health-records/{address}/ipfs/{cid}")
  @Streaming
  suspend fun getFileByCID(
    @Header("Authorization") token: String,
    @Path("address") address: String,
    @Path("cid") cid: String,
  ): Response<ResponseBody>

  // Users Patient
  @GET("users/patient-doctors")
  suspend fun getPatientDoctors(
    @Header("Authorization") token: String,
  ): ResponseMessage<ResponseAccess>

  @GET("users/doctor-patients")
  suspend fun getDoctorPatients(
    @Header("Authorization") token: String,
  ): ResponseMessage<ResponseAccess>

  // Access
  @PUT("access/request-access/approving")
  suspend fun putRequestAccessPatientApproving(
    @Header("Authorization") token: String,
    @Body request: RequestAccessApproving,
  ): ResponseMessage<ResponseTransaction>

  // Access
  @PUT("access/request-access/doctor")
  suspend fun putRequestAccessDoctor(
    @Header("Authorization") token: String,
    @Body request: RequestAccessDoctor,
  ): ResponseMessage<ResponseTransaction>

  @PUT("access/permissions/patient-revoke-doctor")
  suspend fun putPatientRevokeDoctorPermission(
    @Header("Authorization") token: String,
    @Body request: RequestPermissionRevokeDoctor,
  ): ResponseMessage<ResponseTransaction>

  @PUT("access/permissions/patient-grant-doctor")
  suspend fun putPatientPermissionDoctor(
    @Header("Authorization") token: String,
    @Body request: RequestPermissionGrantDoctor,
  ): ResponseMessage<ResponseTransaction>
}