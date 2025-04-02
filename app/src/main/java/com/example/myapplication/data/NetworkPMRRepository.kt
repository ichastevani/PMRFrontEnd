package com.example.myapplication.data

import android.content.Context
import android.os.Environment
import android.util.Log
import android.webkit.MimeTypeMap
import com.example.myapplication.network.PMRApiService
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
import com.example.myapplication.network.response.ResponseMessageNoData
import com.example.myapplication.network.response.ResponsePermission
import com.example.myapplication.network.response.ResponseTransaction
import com.example.myapplication.network.response.ResponseUserPermission
import com.example.myapplication.utils.ToolsUtil.createPartFromString
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class NetworkPMRRepository(
  private val pmrApiService: PMRApiService
) : PMRRepository {

  override suspend fun getIsUserRegistered(address: String): Boolean {
    val result = runCatching { pmrApiService.getIsUserRegistered(address) }
      .getOrElse {
        return false
      }
    return result.data?.isRegistered == true
  }

  override suspend fun register(
    requestRegister: RequestAuthRegister
  ): ResponseMessage<ResponseTransaction?> {
    // Log.d("NetworkPMRRepository", "data: ${requestRegister}")
    return try {
      val result = pmrApiService.register(requestRegister)
      // Log.d("NetworkPMRRepository", "register: ${result}")
      result
    } catch (e: Exception) {
      // Log.d("NetworkPMRRepository", e.message ?: "Unknown error")
      ResponseMessage(status = "error", message = e.message ?: "Unknown error")
    } as ResponseMessage<ResponseTransaction?>
  }

  override suspend fun login(requestLogin: RequestAuthLogin): ResponseMessage<ResponseAuthLogin?> {
    return try {
      val result = pmrApiService.login(requestLogin)
      result
    } catch (e: Exception) {
      ResponseMessage(status = "error", message = e.message ?: "Unknown error")
    } as ResponseMessage<ResponseAuthLogin?>
  }

  override suspend fun getUserByAddress(
    authToken: String,
    address: String
  ): ResponseMessage<ResponseUserPermission?> {
    return try {
      val bearerToken = "Bearer $authToken"
      val result = pmrApiService.getUserByAddress(bearerToken, address)
      result
    } catch (e: Exception) {
      ResponseMessage(status = "error", message = e.message ?: "Unknown error")
    } as ResponseMessage<ResponseUserPermission?>
  }

  override suspend fun getDoctorPermission(
    authToken: String,
    addressDoctor: String,
    addressPatient: String
  ): ResponseMessage<ResponsePermission?> {
    return try {
      val bearerToken = "Bearer $authToken"
      val result = pmrApiService.getDoctorPermission(bearerToken, addressDoctor, addressPatient)
      result
    } catch (e: Exception) {
      ResponseMessage(status = "error", message = e.message ?: "Unknown error")
    } as ResponseMessage<ResponsePermission?>
  }

  override suspend fun postHealthRecords(
    authToken: String,
    mimeType: String,
    request: RequestHealthRecordAdd
  ): ResponseMessage<ResponseTransaction?> {
    val requestFile = request.file.asRequestBody(mimeType.toMediaTypeOrNull())
    val filePart = MultipartBody.Part.createFormData("file", request.file.name, requestFile)

    val addressPart = createPartFromString(request.patientAddress)
    val descriptionPart = createPartFromString(request.description)
    val recordTypePart = createPartFromString(request.recordType)

    return try {
      val bearerToken = "Bearer $authToken"
      val result = pmrApiService.postHealthRecords(bearerToken, filePart, addressPart, descriptionPart, recordTypePart)
      result
    } catch (e: Exception) {
      ResponseMessageNoData(status = "error", message = e.message ?: "Unknown error")
    } as ResponseMessage<ResponseTransaction?>
  }

  override suspend fun getHealthRecords(authToken: String, address: String): ResponseMessage<ResponseHealthRecords?> {
    return try {
      val bearerToken = "Bearer $authToken"
      val result = pmrApiService.getHealthRecords(bearerToken, address)
      result
    } catch (e: Exception) {
      ResponseMessage(status = "error", message = e.message ?: "Unknown error")
    } as ResponseMessage<ResponseHealthRecords?>
  }

  override suspend fun putHealthRecords(
    authToken: String,
    address: String,
    recordId: String,
    mimeType: String,
    request: RequestHealthRecordUpdate,
  ): ResponseMessage<ResponseTransaction?> {
    val requestFile = request.file.asRequestBody(mimeType.toMediaTypeOrNull())
    val filePart = MultipartBody.Part.createFormData("file", request.file.name, requestFile)

    val descriptionPart = createPartFromString(request.description)
    val recordTypePart = createPartFromString(request.recordType)

    return try {
      val bearerToken = "Bearer $authToken"
      val result = pmrApiService.putHealthRecords(bearerToken, address, recordId, filePart, descriptionPart, recordTypePart)
      result
    } catch (e: Exception) {
      ResponseMessageNoData(status = "error", message = e.message ?: "Unknown error")
    } as ResponseMessage<ResponseTransaction?>
  }

  override suspend fun putHealthRecordNoFile(
    authToken: String,
    address: String,
    recordId: String,
    request: RequestHealthRecordUpdateNoFile
  ): ResponseMessage<ResponseTransaction?> {
    return try {
      val bearerToken = "Bearer $authToken"
      val result = pmrApiService.putHealthRecordNoFile(bearerToken, address, recordId, request)
      result
    } catch (e: Exception) {
      ResponseMessage(status = "error", message = e.message ?: "Unknown error")
    } as ResponseMessage<ResponseTransaction?>
  }

  override suspend fun deleteHealthRecord(
    authToken: String,
    address: String,
    recordId: String
  ): ResponseMessage<ResponseTransaction?> {
    return try {
      val bearerToken = "Bearer $authToken"
      val result = pmrApiService.deleteHealthRecord(bearerToken, address, recordId)
      result
    } catch (e: Exception) {
      ResponseMessage(status = "error", message = e.message ?: "Unknown error")
    } as ResponseMessage<ResponseTransaction?>
  }

  override suspend fun getHealthRecord(
    authToken: String,
    address: String,
    recordId: String
  ): ResponseMessage<ResponseHealthRecord?> {
    return try {
      val bearerToken = "Bearer $authToken"
      val result = pmrApiService.getHealthRecord(bearerToken, address, recordId)
      result
    } catch (e: Exception) {
      ResponseMessage(status = "error", message = e.message ?: "Unknown error")
    } as ResponseMessage<ResponseHealthRecord?>
  }

  override suspend fun getFileByCID(
    context: Context,
    authToken: String,
    address: String,
    cid: String
  ): File? {
//    findExistingFile(context, cid)?.let { return it }

    // 🌐 Lanjutkan download jika belum ada
    val bearerToken = "Bearer $authToken"
    val response = pmrApiService.getFileByCID(bearerToken, address, cid)
    if (!response.isSuccessful || response.body() == null) return null

    val body = response.body()!!

    // 🔍 Coba dapatkan nama file dari header "Content-Disposition"
//    val contentDisposition = response.headers()["Content-Disposition"]
//    val fileName = contentDisposition?.let { extractFileName(it) }

    // 🏷️ Cek MIME type dari response
    val mimeType = body.contentType()?.toString() ?: "application/octet-stream"
    val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "bin"

    // 🔄 Gunakan fileName dari header atau buat nama baru
    val finalFileName = "file_${cid}.${extension}"

    // 📂 Buat file baru
    val newFile = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), finalFileName)

    return try {
      body.byteStream().use { inputStream ->
        newFile.outputStream().use { outputStream ->
          inputStream.copyTo(outputStream)
        }
      }
      newFile
    } catch (e: Exception) {
      Log.e("NetworkPMRRepository", "Error saving file: ${e.message}")
      null
    }
  }

  override suspend fun getPatientDoctors(authToken: String): ResponseMessage<ResponseAccess?> {
    return try {
      val bearerToken = "Bearer $authToken"
      val result = pmrApiService.getPatientDoctors(bearerToken)
      result
    } catch (e: Exception) {
      ResponseMessage(status = "error", message = e.message ?: "Unknown error")
    } as ResponseMessage<ResponseAccess?>
  }

  override suspend fun getDoctorPatients(authToken: String): ResponseMessage<ResponseAccess?> {
    return try {
      val bearerToken = "Bearer $authToken"
      val result = pmrApiService.getDoctorPatients(bearerToken)
      result
    } catch (e: Exception) {
      ResponseMessage(status = "error", message = e.message ?: "Unknown error")
    } as ResponseMessage<ResponseAccess?>
  }

  override suspend fun putRequestAccessPatientApproving(
    authToken: String,
    request: RequestAccessApproving
  ): ResponseMessage<ResponseTransaction?> {
    return try {
      val bearerToken = "Bearer $authToken"
      val result = pmrApiService.putRequestAccessPatientApproving(bearerToken, request)
      result
    } catch (e: Exception) {
      ResponseMessageNoData(status = "error", message = e.message ?: "Unknown error")
    } as ResponseMessage<ResponseTransaction?>
  }

  override suspend fun putRequestAccessDoctor(
    authToken: String,
    request: RequestAccessDoctor
  ): ResponseMessage<ResponseTransaction?> {
    return try {
      val bearerToken = "Bearer $authToken"
      val result = pmrApiService.putRequestAccessDoctor(bearerToken, request)
      result
    } catch (e: Exception) {
      ResponseMessageNoData(status = "error", message = e.message ?: "Unknown error")
    } as ResponseMessage<ResponseTransaction?>
  }

  override suspend fun putPatientRevokeDoctorPermission(
    authToken: String,
    request: RequestPermissionRevokeDoctor
  ): ResponseMessage<ResponseTransaction?> {
    return try {
      val bearerToken = "Bearer $authToken"
      val result = pmrApiService.putPatientRevokeDoctorPermission(bearerToken, request)
      result
    } catch (e: Exception) {
      ResponseMessageNoData(status = "error", message = e.message ?: "Unknown error")
    } as ResponseMessage<ResponseTransaction?>
  }

  override suspend fun putPatientPermissionDoctor(
    authToken: String,
    request: RequestPermissionGrantDoctor
  ): ResponseMessage<ResponseTransaction?> {
    return try {
      val bearerToken = "Bearer $authToken"
      val result = pmrApiService.putPatientPermissionDoctor(bearerToken, request)
      result
    } catch (e: Exception) {
      ResponseMessageNoData(status = "error", message = e.message ?: "Unknown error")
    } as ResponseMessage<ResponseTransaction?>
  }
}