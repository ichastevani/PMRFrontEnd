package com.example.myapplication.ui.viewModels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.PMRRepository
import com.example.myapplication.network.request.RequestAccessApproving
import com.example.myapplication.network.request.RequestAccessDoctor
import com.example.myapplication.network.request.RequestAuthLogin
import com.example.myapplication.network.request.RequestAuthRegister
import com.example.myapplication.network.request.RequestHealthRecordAdd
import com.example.myapplication.network.request.RequestHealthRecordUpdate
import com.example.myapplication.network.request.RequestHealthRecordUpdateNoFile
import com.example.myapplication.network.request.RequestPermissionGrantDoctor
import com.example.myapplication.network.request.RequestPermissionRevokeDoctor
import com.example.myapplication.network.response.DataAccess
import com.example.myapplication.network.response.DataHealthRecord
import com.example.myapplication.network.response.ResponseAccess
import com.example.myapplication.network.response.UserData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

sealed interface PMRIsRegisteredUiState {
  data class Success(val isRegistered: Boolean) : PMRIsRegisteredUiState
  data class Error(val message: String) : PMRIsRegisteredUiState
  object Loading : PMRIsRegisteredUiState
}

sealed interface PMRTransactionUiState {
  data class Success(val transaction: String?) : PMRTransactionUiState
  data class Error(val message: String) : PMRTransactionUiState
  object Loading : PMRTransactionUiState
}

sealed interface PMRLoginUiState {
  data class Success(val token: String, val user: UserData) : PMRLoginUiState
  data class Error(val message: String) : PMRLoginUiState
  object Loading : PMRLoginUiState
}

sealed interface PMRHealthRecordsUiState {
  data class Success(val healthRecords: ArrayList<DataHealthRecord>) : PMRHealthRecordsUiState
  data class Error(val message: String) : PMRHealthRecordsUiState
  object Loading : PMRHealthRecordsUiState
}

sealed interface PMRHealthRecordUiState {
  data class Success(val healthRecord: DataHealthRecord) : PMRHealthRecordUiState
  data class Error(val message: String) : PMRHealthRecordUiState
  object Loading : PMRHealthRecordUiState
}

sealed interface PMRUserAccessUiState {
  data class Success(val userAccess: ResponseAccess) : PMRUserAccessUiState
  data class Error(val message: String) : PMRUserAccessUiState
  object Loading : PMRUserAccessUiState
}

sealed interface PMRRequestAccessPatientApprovingUiState {
  data class Success(val message: String) : PMRRequestAccessPatientApprovingUiState
  data class Error(val message: String) : PMRRequestAccessPatientApprovingUiState
  object Loading : PMRRequestAccessPatientApprovingUiState
}

sealed interface PMRFileUiState {
  data class Success(val file: File) : PMRFileUiState
  data class Error(val message: String) : PMRFileUiState
  object Loading : PMRFileUiState
}

sealed interface PMRUserPermissionUiState {
  data class Success(val user: UserData, val permission: DataAccess) : PMRUserPermissionUiState
  data class Error(val message: String) : PMRUserPermissionUiState
  object Loading : PMRUserPermissionUiState
}

sealed interface PMRDoctorPermission {
  data class Success(val permission: DataAccess) : PMRDoctorPermission
  data class Error(val message: String) : PMRDoctorPermission
  object Loading : PMRDoctorPermission
}

data class UiStatePMR(
  val isRegistered: PMRIsRegisteredUiState = PMRIsRegisteredUiState.Loading,
  val transaction: PMRTransactionUiState = PMRTransactionUiState.Loading,
  val login: PMRLoginUiState = PMRLoginUiState.Loading,
  val healthRecords: PMRHealthRecordsUiState = PMRHealthRecordsUiState.Loading,
  val healthRecord: PMRHealthRecordUiState = PMRHealthRecordUiState.Loading,
  val file: PMRFileUiState = PMRFileUiState.Loading,
  val userAccess: PMRUserAccessUiState = PMRUserAccessUiState.Loading,
  val requestAccessPatientApproving: PMRRequestAccessPatientApprovingUiState = PMRRequestAccessPatientApprovingUiState.Loading,
  val userPermission: PMRUserPermissionUiState = PMRUserPermissionUiState.Loading,
)

@HiltViewModel
class PMRViewModel @Inject constructor(private val pmrRepository: PMRRepository) : ViewModel() {
  private val _uiState = MutableStateFlow(UiStatePMR())
  val uiState = _uiState.asStateFlow()

  fun getIsUserRegistered(address: String) {
    _uiState.update {
      it.copy(
        isRegistered = PMRIsRegisteredUiState.Loading,
      )
    }

    viewModelScope.launch {
      _uiState.update {
        val isRegisteredState = runCatching {
          pmrRepository.getIsUserRegistered(address)
        }.fold(
          onSuccess = { PMRIsRegisteredUiState.Success(it) },
          onFailure = { PMRIsRegisteredUiState.Error("Pengguna belum terdaftar") }
        )

        it.copy(
          isRegistered = isRegisteredState
        )
      }

      _uiState.value = _uiState.value.apply {

      }
    }
  }

  fun register(requestRegister: RequestAuthRegister) {
    viewModelScope.launch {
      _uiState.update {
        it.copy(
          transaction = PMRTransactionUiState.Loading
        )
      }

      _uiState.update {
        val transactionState = runCatching {
          pmrRepository.register(requestRegister)
        }.fold(
          onSuccess = { PMRTransactionUiState.Success(it.data?.transactionData) },
          onFailure = { PMRTransactionUiState.Error(it.message ?: "Unknown error") }
        )

        it.copy(
          transaction = transactionState
        )
      }
    }
  }

  fun login(requestLogin: RequestAuthLogin) {
    viewModelScope.launch {
      _uiState.update {
        it.copy(
          login = PMRLoginUiState.Loading
        )
      }

      _uiState.update {
        val loginState = runCatching {
          pmrRepository.login(requestLogin)
        }.fold(
          onSuccess = {
            if (it.data?.token == null) {
              PMRLoginUiState.Error("Data token dan user tidak tersedia")
            } else {
              PMRLoginUiState.Success(it.data.token, it.data.user)
            }
          },
          onFailure = { PMRLoginUiState.Error(it.message ?: "Unknown error") }
        )

        it.copy(
          login = loginState
        )
      }
    }
  }

  // Users
  fun getUserByAddress(authToken: String, address: String) {
    viewModelScope.launch {
      _uiState.update {
        it.copy(
          userPermission = PMRUserPermissionUiState.Loading
        )
      }

      _uiState.update {
        val state = runCatching {
          pmrRepository.getUserByAddress(authToken, address)
        }.fold(
          onSuccess = {
            if(it.data != null){
              PMRUserPermissionUiState.Success(it.data.user, it.data.permission)
            }else{
              PMRUserPermissionUiState.Error("Data pengguna tidak tersedia")
            }
          },
          onFailure = { PMRUserPermissionUiState.Error(it.message ?: "Unknown error") }
        )

        it.copy(
          userPermission = state
        )
      }
    }
  }

  // Health Records
  fun addHealthRecords(authToken: String, mimeType: String, request: RequestHealthRecordAdd) {
    viewModelScope.launch {
      _uiState.update {
        it.copy(
          transaction = PMRTransactionUiState.Loading
        )
      }

      _uiState.update {
        val transactionState = runCatching {
          pmrRepository.postHealthRecords(authToken, mimeType, request)
        }.fold(
          onSuccess = {
            PMRTransactionUiState.Success(it.data?.transactionData)
          },
          onFailure = { PMRTransactionUiState.Error(it.message ?: "Unknown error") }
        )

        it.copy(
          transaction = transactionState
        )
      }
    }
  }

  fun updateHealthRecords(
    authToken: String,
    address: String,
    recordId: String,
    mimeType: String,
    file: File?,
    description: String,
    recordType: String
  ) {
    viewModelScope.launch {
      _uiState.update {
        it.copy(
          transaction = PMRTransactionUiState.Loading
        )
      }

      _uiState.update {
        val transactionState = runCatching {
          if(file == null){
            pmrRepository.putHealthRecordNoFile(authToken, address, recordId,
              RequestHealthRecordUpdateNoFile(
                description = description,
                recordType = recordType
              )
            )
          }else{
            pmrRepository.putHealthRecords(authToken, address, recordId, mimeType,
              RequestHealthRecordUpdate(
                file = file,
                description = description,
                recordType = recordType
              )
            )
          }

        }.fold(
          onSuccess = {
            PMRTransactionUiState.Success(it.data?.transactionData)
          },
          onFailure = { PMRTransactionUiState.Error(it.message ?: "Unknown error") }
        )

        it.copy(
          transaction = transactionState
        )
      }
    }
  }

  fun deleteHealthRecords(authToken: String, address: String, recordId: String) {
    viewModelScope.launch {
      _uiState.update {
        it.copy(
          transaction = PMRTransactionUiState.Loading
        )
      }

      _uiState.update {
        val transactionState = runCatching {
          pmrRepository.deleteHealthRecord(authToken, address, recordId)
        }.fold(
          onSuccess = {
            PMRTransactionUiState.Success(it.data?.transactionData)
          },
          onFailure = { PMRTransactionUiState.Error(it.message ?: "Unknown error") }
        )

        it.copy(
          transaction = transactionState
        )
      }
    }
  }

  fun getHealthRecords(authToken: String, address: String) {
    viewModelScope.launch {
      _uiState.update {
        it.copy(
          healthRecords = PMRHealthRecordsUiState.Loading
        )
      }

      _uiState.update {
        val state = runCatching {
          pmrRepository.getHealthRecords(authToken, address)
        }.fold(
          onSuccess = {
            PMRHealthRecordsUiState.Success(it.data?.healthRecords ?: ArrayList())
          },
          onFailure = { PMRHealthRecordsUiState.Error(it.message ?: "Unknown error") }
        )

        it.copy(
          healthRecords = state
        )
      }
    }
  }

  fun getHealthRecord(authToken: String, address: String, recordId: String) {
    viewModelScope.launch {
      _uiState.update {
        it.copy(
          healthRecord = PMRHealthRecordUiState.Loading
        )
      }

      _uiState.update {
        val state = runCatching {
          pmrRepository.getHealthRecord(authToken, address, recordId)
        }.fold(
          onSuccess = {
            if (it.data != null) {
              PMRHealthRecordUiState.Success(it.data.healthRecord)
            } else {
              PMRHealthRecordUiState.Error("Data health record tidak tersedia.")
            }
          },
          onFailure = { PMRHealthRecordUiState.Error(it.message ?: "Unknown error") }
        )

        it.copy(
          healthRecord = state
        )
      }
    }
  }

  fun getFileByCID(context: Context, authToken: String, address: String, cid: String) {
    viewModelScope.launch {
      _uiState.update { it.copy(file = PMRFileUiState.Loading) }

      val state = runCatching {
        pmrRepository.getFileByCID(context, authToken, address, cid)
      }.fold(
        onSuccess = { newFile ->
          when {
            newFile == null -> PMRFileUiState.Error("File tidak tersedia.")
            else -> PMRFileUiState.Success(newFile)
          }
        },
        onFailure = { PMRFileUiState.Error("Unknown error") }
      )

      _uiState.update { it.copy(file = state) }
    }
  }

  fun getPatientDoctors(authToken: String) {
    viewModelScope.launch {
      _uiState.update {
        it.copy(
          userAccess = PMRUserAccessUiState.Loading
        )
      }

      _uiState.update {
        val state = runCatching {
          pmrRepository.getPatientDoctors(authToken)
        }.fold(
          onSuccess = {
            if (it.data != null) {
              PMRUserAccessUiState.Success(it.data)
            } else {
              PMRUserAccessUiState.Error("Data patient doctors tidak tersedia.")
            }
          },
          onFailure = { PMRUserAccessUiState.Error(it.message ?: "Unknown error") }
        )

        it.copy(
          userAccess = state
        )
      }
    }
  }

  fun getDoctorPatients(authToken: String) {
    viewModelScope.launch {
      _uiState.update {
        it.copy(
          userAccess = PMRUserAccessUiState.Loading
        )
      }

      _uiState.update {
        val state = runCatching {
          pmrRepository.getDoctorPatients(authToken)
        }.fold(
          onSuccess = {
            if (it.data != null) {
              PMRUserAccessUiState.Success(it.data)
            } else {
              PMRUserAccessUiState.Error("Data doctor patients tidak tersedia.")
            }
          },
          onFailure = { PMRUserAccessUiState.Error(it.message ?: "Unknown error") }
        )

        it.copy(
          userAccess = state
        )
      }
    }
  }

  fun putRequestAccessDoctor(
    authToken: String,
    request: RequestAccessDoctor
  ) {
    viewModelScope.launch {
      viewModelScope.launch {
        _uiState.update {
          it.copy(
            transaction = PMRTransactionUiState.Loading
          )
        }

        _uiState.update {
          val transactionState = runCatching {
            pmrRepository.putRequestAccessDoctor(authToken, request)
          }.fold(
            onSuccess = { PMRTransactionUiState.Success(it.data?.transactionData) },
            onFailure = { PMRTransactionUiState.Error(it.message ?: "Unknown error") }
          )

          it.copy(
            transaction = transactionState
          )
        }
      }
    }
  }

  fun putRequestAccessPatientApproving(
    authToken: String,
    request: RequestAccessApproving
  ) {
    viewModelScope.launch {
      viewModelScope.launch {
        _uiState.update {
          it.copy(
            transaction = PMRTransactionUiState.Loading
          )
        }

        _uiState.update {
          val transactionState = runCatching {
            pmrRepository.putRequestAccessPatientApproving(authToken, request)
          }.fold(
            onSuccess = { PMRTransactionUiState.Success(it.data?.transactionData) },
            onFailure = { PMRTransactionUiState.Error(it.message ?: "Unknown error") }
          )

          it.copy(
            transaction = transactionState
          )
        }
      }
    }
  }

  fun putPatientRevokeDoctorPermission(
    authToken: String,
    request: RequestPermissionRevokeDoctor
  ) {
    viewModelScope.launch {
      viewModelScope.launch {
        _uiState.update {
          it.copy(
            transaction = PMRTransactionUiState.Loading
          )
        }

        _uiState.update {
          val transactionState = runCatching {
            pmrRepository.putPatientRevokeDoctorPermission(authToken, request)
          }.fold(
            onSuccess = { PMRTransactionUiState.Success(it.data?.transactionData) },
            onFailure = { PMRTransactionUiState.Error(it.message ?: "Unknown error") }
          )

          it.copy(
            transaction = transactionState
          )
        }
      }
    }
  }

  fun putPatientPermissionDoctor(
    authToken: String,
    request: RequestPermissionGrantDoctor
  ) {
    viewModelScope.launch {
      viewModelScope.launch {
        _uiState.update {
          it.copy(
            transaction = PMRTransactionUiState.Loading
          )
        }

        _uiState.update {
          val transactionState = runCatching {
            pmrRepository.putPatientPermissionDoctor(authToken, request)
          }.fold(
            onSuccess = { PMRTransactionUiState.Success(it.data?.transactionData) },
            onFailure = { PMRTransactionUiState.Error(it.message ?: "Unknown error") }
          )

          it.copy(
            transaction = transactionState
          )
        }
      }
    }
  }

}