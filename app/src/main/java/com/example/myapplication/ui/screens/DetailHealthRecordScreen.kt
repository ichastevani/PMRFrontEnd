package com.example.myapplication.ui.screens

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.myapplication.network.response.DataAccess
import com.example.myapplication.network.response.DataHealthRecord
import com.example.myapplication.network.response.UserData
import com.example.myapplication.ui.PMRScreenEnum
import com.example.myapplication.ui.components.BottomNavigationBarUI
import com.example.myapplication.ui.components.LoadingUI
import com.example.myapplication.ui.components.PdfViewerUI
import com.example.myapplication.ui.components.TopAppBarWBackUI
import com.example.myapplication.ui.viewModels.MetaMaskViewModel
import com.example.myapplication.ui.viewModels.PMRFileUiState
import com.example.myapplication.ui.viewModels.PMRHealthRecordUiState
import com.example.myapplication.ui.viewModels.PMRLoginUiState
import com.example.myapplication.ui.viewModels.PMRTransactionUiState
import com.example.myapplication.ui.viewModels.PMRUserPermissionUiState
import com.example.myapplication.ui.viewModels.PMRViewModel
import java.io.File

@Composable
fun DetailHealthRecordScreen(
  navController: NavHostController,
  metaMaskViewModel: MetaMaskViewModel,
  pmrViewModel: PMRViewModel,
  snackbarHost: SnackbarHostState,
  healthRecordId: String,
  patientAddress: String
) {
  val context = LocalContext.current
  var authToken by rememberSaveable { mutableStateOf("") }
  var targetUser by remember { mutableStateOf<UserData?>(null) }
  var doctorPermission by remember { mutableStateOf<DataAccess?>(null) }
  var role by remember { mutableStateOf("Patient") }

  var isProcessDeleteHealthRecord by remember { mutableStateOf(false) }

  var isLoading by rememberSaveable { mutableStateOf(true) }
  var dataHealthRecord: DataHealthRecord? by remember { mutableStateOf(null) }
  var file: File? by remember { mutableStateOf(null) }

  val uiStatePMR by pmrViewModel.uiState.collectAsState()
  val uiStateMetaMask by metaMaskViewModel.uiState.collectAsState()

  LaunchedEffect(healthRecordId) {
    Log.d("FILE_UPDATE", "ID: ${healthRecordId}")
    file = null
  }

  LaunchedEffect(Unit) {
    if (uiStatePMR.login is PMRLoginUiState.Success) {
      authToken = (uiStatePMR.login as PMRLoginUiState.Success).token
      role = (uiStatePMR.login as PMRLoginUiState.Success).user.role
      pmrViewModel.getHealthRecord(authToken, patientAddress, healthRecordId)
      pmrViewModel.getUserByAddress(authToken, patientAddress)
    } else {
      navController.navigate(PMRScreenEnum.Welcome.name)
    }
  }

  LaunchedEffect(uiStatePMR.healthRecord) {
    if (uiStatePMR.healthRecord is PMRHealthRecordUiState.Success && file == null) {
      dataHealthRecord = (uiStatePMR.healthRecord as PMRHealthRecordUiState.Success).healthRecord
      dataHealthRecord?.let {
        Log.d("FILE_UPDATE", "MENGAMBIL FILE dengan CID: ${it.cid}")
        pmrViewModel.getFileByCID(context, authToken, patientAddress, it.cid)
      } ?: navController.navigate(PMRScreenEnum.DoctorHealthRecords.name + "/$patientAddress")

      isLoading = false
    } else if (uiStatePMR.healthRecord is PMRHealthRecordUiState.Error) {
      navController.navigate(PMRScreenEnum.DoctorHealthRecords.name + "/$patientAddress")
    }
  }

  LaunchedEffect(uiStatePMR.file) {
    if (uiStatePMR.file is PMRFileUiState.Success && file == null) {
      val newFile = (uiStatePMR.file as PMRFileUiState.Success).file

      if (dataHealthRecord != null) {
        if (newFile.absolutePath.toString()
            .contains(dataHealthRecord!!.cid) && dataHealthRecord!!.id == healthRecordId
        ) {
          file = newFile
          Log.d("FILE_UPDATE", "File baru diterima: ${file!!.absolutePath}")
        }
      }

    }
  }

  LaunchedEffect(key1 = uiStatePMR.userPermission) {
    when (uiStatePMR.userPermission) {
      is PMRUserPermissionUiState.Success -> {
        targetUser = (uiStatePMR.userPermission as PMRUserPermissionUiState.Success).user
        doctorPermission = (uiStatePMR.userPermission as PMRUserPermissionUiState.Success).permission
      }
      is PMRUserPermissionUiState.Error -> {
        navController.navigate(PMRScreenEnum.Home.name)
      }
      else -> {}
    }
  }

  LaunchedEffect(key1 = uiStatePMR.transaction) {
    if(!isProcessDeleteHealthRecord) return@LaunchedEffect

    when (uiStatePMR.transaction) {
      is PMRTransactionUiState.Error -> {
        val errMsg = (uiStatePMR.transaction as PMRTransactionUiState.Error).message
        snackbarHost.showSnackbar(
          message = errMsg,
          duration = SnackbarDuration.Long
        )
        isProcessDeleteHealthRecord = false
      }
      is PMRTransactionUiState.Success -> {
        val transactionData =
          (uiStatePMR.transaction as PMRTransactionUiState.Success).transaction
        if(transactionData == null){
          snackbarHost.showSnackbar(
            message = "Gagal melakukan tindakan",
            duration = SnackbarDuration.Long
          )
        }else{
          metaMaskViewModel.sendRequest(transactionData.toString())
        }
        isProcessDeleteHealthRecord = false
      }
      else -> {}
    }
  }

  LaunchedEffect(key1 = uiStateMetaMask.isTransactionSuccess) {
    if(uiStateMetaMask.isTransactionSuccess){
      navController.navigate(PMRScreenEnum.DoctorHealthRecords.name + "/${patientAddress}")
      metaMaskViewModel.resetIsTransaction()
    }
  }

  val onDeleteHealthRecord: () -> Unit = {
    pmrViewModel.deleteHealthRecords(
      authToken,
      patientAddress,
      healthRecordId,
    )
    isProcessDeleteHealthRecord = true
  }

  if (isProcessDeleteHealthRecord || isLoading || doctorPermission == null || targetUser == null) {
    LoadingUI()
  } else if (dataHealthRecord != null) {
    DetailHealthRecordUI(
      context,
      navController,
      dataHealthRecord!!,
      file,
      doctorPermission!!,
      onDeleteHealthRecord,
      patientAddress,
      role
    )
  }

}

@Composable
fun DetailHealthRecordUI(
  context: Context,
  navController: NavHostController,
  healthRecord: DataHealthRecord,
  file: File?,
  doctorPermission: DataAccess,
  onDeleteHealthRecord: () -> Unit,
  patientAddress: String,
  role: String
) {
  var showDialog by remember { mutableStateOf(false) }

  Column {
    TopAppBarWBackUI("Detail Health Record", navController)

    Box(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .padding(16.dp),
    ) {
      Column {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp)
        ) {
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 4.dp),
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(4.dp)
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Text(
                text = healthRecord.description,
                fontSize = 16.sp
              )
            }
          }

          // Label di sudut kanan atas
          Box(
            modifier = Modifier
              .align(Alignment.TopEnd)
              .padding(8.dp)
          ) {
            Text(
              text = healthRecord.recordType,
              fontSize = 12.sp,
              color = Color.White,
              modifier = Modifier
                .background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }
        }

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
        ) {

          file?.let { currentFile ->

            when {
              currentFile.extension in listOf("jpg", "jpeg", "png", "gif", "bmp", "webp") -> {
                val bitmap = BitmapFactory.decodeFile(currentFile.absolutePath)
                if (bitmap != null) {
                  Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    Image(
                      bitmap = bitmap.asImageBitmap(),
                      contentDescription = "PDF Page",
                      modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(bitmap.width.toFloat() / bitmap.height.toFloat())
                        .clipToBounds()
                    )
                  }
                } else {
                  Text("Gagal memuat gambar", color = Color.Red, modifier = Modifier.padding(16.dp))
                }
              }

              currentFile.extension == "pdf" -> {
                PdfViewerUI(file = currentFile)
              }

              else -> {
                Text("File tidak bisa ditampilkan, buka dengan aplikasi eksternal")
              }
            }
          }
        }
      }

      // Floating Button Layout
      Row(
        modifier = Modifier
          .align(Alignment.BottomEnd)
          .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        if (doctorPermission.canDelete && doctorPermission.canUpdate) {
          FloatingActionButton(
            onClick = {
              showDialog = true
            },
            containerColor = colorScheme.error
          ) {
            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Health Records")
          }

          FloatingActionButton(
            onClick = {
              navController.navigate(PMRScreenEnum.UpdateHealthRecord.name + "/${healthRecord.id}/${patientAddress}")
            },
            containerColor = colorScheme.primary
          ) {
            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Health Records")
          }
        } else if (doctorPermission.canUpdate) {
          FloatingActionButton(
            onClick = {
              navController.navigate(PMRScreenEnum.UpdateHealthRecord.name + "/${healthRecord.id}/${patientAddress}")
            },
            containerColor = colorScheme.primary
          ) {
            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Health Records")
          }
        } else if (doctorPermission.canDelete) {
          FloatingActionButton(
            onClick = {
              showDialog = true
            },
            containerColor = colorScheme.error
          ) {
            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Health Records")
          }
        }
      }

      ConfirmDialogDeleteHealthRecord(
        showDialog = showDialog,
        onDismiss = { showDialog = false },
        onYesClick = {
          onDeleteHealthRecord()
          showDialog = false
        },
      )
      // end
    }

    // Bottom Navigation
    BottomNavigationBarUI(navController, role)
  }
}

@Composable
fun ConfirmDialogDeleteHealthRecord(
  showDialog: Boolean,
  onDismiss: () -> Unit,
  onYesClick: () -> Unit,
) {
  if (showDialog) {
    AlertDialog(
      onDismissRequest = onDismiss,
      title = { Text(text = "Konfirmasi") },
      text = { Text(text = "Apakah kamu yakin ingin menghapus health record ini?") },
      confirmButton = {
        Button(onClick = onYesClick) {
          Text("Ya, Lanjutkan")
        }
      },
      dismissButton = {
        TextButton(onClick = onDismiss) {
          Text("Batal")
        }
      }
    )
  }
}

@Preview(showBackground = true)
@Composable
fun PreviewDetailHealthRecordUIScreen() {

}
