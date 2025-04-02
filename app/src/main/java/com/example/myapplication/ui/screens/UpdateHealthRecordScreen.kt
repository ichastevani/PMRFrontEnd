package com.example.myapplication.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.network.response.DataHealthRecord
import com.example.myapplication.network.response.UserData
import com.example.myapplication.ui.PMRScreenEnum
import com.example.myapplication.ui.components.BottomNavigationBarUI
import com.example.myapplication.ui.components.LoadingUI
import com.example.myapplication.ui.components.TopAppBarWBackUI
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.viewModels.MetaMaskViewModel
import com.example.myapplication.ui.viewModels.PMRHealthRecordUiState
import com.example.myapplication.ui.viewModels.PMRLoginUiState
import com.example.myapplication.ui.viewModels.PMRTransactionUiState
import com.example.myapplication.ui.viewModels.PMRUserPermissionUiState
import com.example.myapplication.ui.viewModels.PMRViewModel
import com.example.myapplication.utils.ToolsUtil.getFileFromUri
import java.io.File

@Composable
fun UpdateHealthRecordScreen(
  navController: NavHostController,
  metaMaskViewModel: MetaMaskViewModel,
  pmrViewModel: PMRViewModel,
  snackbarHost: SnackbarHostState,
  patientAddress: String,
  healthRecordId: String,
) {
  val context = LocalContext.current
  var isProcessUpdateHealthRecord by remember { mutableStateOf(false) }
  var authToken by remember { mutableStateOf("") }
  var role by remember { mutableStateOf("Patient") }
  var targetUser by remember { mutableStateOf<UserData?>(null) }

  var isLoading by rememberSaveable { mutableStateOf(true) }
  var file: File? by remember { mutableStateOf(null) }
  var dataHealthRecord: DataHealthRecord? by remember { mutableStateOf(null) }

  val uiStatePMR by pmrViewModel.uiState.collectAsState()
  val uiStateMetaMask by metaMaskViewModel.uiState.collectAsState()

  val ethAddress = uiStateMetaMask.ethAddress
  if (ethAddress == null) {
    navController.navigate(PMRScreenEnum.Welcome.name)
    return
  }

  // Panggil sekali untuk cek apakah pengguna telah login
  LaunchedEffect(Unit) {
    when (uiStatePMR.login) {
      is PMRLoginUiState.Success -> {
        authToken = (uiStatePMR.login as PMRLoginUiState.Success).token
        role = (uiStatePMR.login as PMRLoginUiState.Success).user.role
        pmrViewModel.getHealthRecord(authToken, patientAddress, healthRecordId)
        pmrViewModel.getUserByAddress(authToken, patientAddress)
      }

      else -> {
        navController.navigate(PMRScreenEnum.Welcome.name)
      }
    }
  }

  LaunchedEffect(uiStatePMR.healthRecord) {
    if (uiStatePMR.healthRecord is PMRHealthRecordUiState.Success && file == null) {
      dataHealthRecord = (uiStatePMR.healthRecord as PMRHealthRecordUiState.Success).healthRecord
      dataHealthRecord?.let {
        pmrViewModel.getFileByCID(context, authToken, patientAddress, it.cid)
      } ?: {
        navController.navigate(PMRScreenEnum.DoctorHealthRecords.name + "/$patientAddress")
      }

      isLoading = false
    } else if (uiStatePMR.healthRecord is PMRHealthRecordUiState.Error) {
      navController.navigate(PMRScreenEnum.DoctorHealthRecords.name + "/$patientAddress")
    }
  }

  LaunchedEffect(key1 = uiStatePMR.transaction) {
    if (!isProcessUpdateHealthRecord) return@LaunchedEffect

    when (uiStatePMR.transaction) {
      is PMRTransactionUiState.Error -> {
        val errMsg = (uiStatePMR.transaction as PMRTransactionUiState.Error).message
        snackbarHost.showSnackbar(
          message = errMsg,
          duration = SnackbarDuration.Long
        )
        isProcessUpdateHealthRecord = false
      }

      is PMRTransactionUiState.Success -> {
        val transactionData =
          (uiStatePMR.transaction as PMRTransactionUiState.Success).transaction
        if (transactionData == null) {
          snackbarHost.showSnackbar(
            message = "Gagal melakukan tindakan",
            duration = SnackbarDuration.Long
          )
        } else {
          metaMaskViewModel.sendRequest(transactionData.toString())
        }
        isProcessUpdateHealthRecord = false
      }

      else -> {}
    }
  }

  LaunchedEffect(key1 = uiStateMetaMask.isTransactionSuccess) {
    if (uiStateMetaMask.isTransactionSuccess) {
      pmrViewModel.getHealthRecord(authToken, patientAddress, healthRecordId)
      navController.navigateUp()
      metaMaskViewModel.resetIsTransaction()
    }
  }

  LaunchedEffect(key1 = uiStatePMR.userPermission) {
    when (uiStatePMR.userPermission) {
      is PMRUserPermissionUiState.Success -> {
        targetUser = (uiStatePMR.userPermission as PMRUserPermissionUiState.Success).user
      }

      is PMRUserPermissionUiState.Error -> {
        navController.navigate(PMRScreenEnum.Home.name)
      }

      else -> {}
    }
  }

  val onSubmit: (mimeType: String, description: String, recordType: String, file: File?) -> Unit =
    { mimeType, description, recordType, file ->
      pmrViewModel.updateHealthRecords(
        authToken,
        patientAddress,
        recordId = healthRecordId,
        mimeType,
        file,
        description,
        recordType
      )
      isProcessUpdateHealthRecord = true
    }

  if (isLoading || dataHealthRecord == null || isProcessUpdateHealthRecord || targetUser == null) {
    LoadingUI()
  } else {
    UpdateHealthRecordUI(
      navController,
      context,
      targetUser!!,
      dataHealthRecord!!,
      onSubmit,
      role
    )
  }

}

@Composable
fun UpdateHealthRecordUI(
  navController: NavHostController,
  context: Context,
  targetUser: UserData,
  healthRecord: DataHealthRecord,
  onSubmit: (
    mimeType: String,
    description: String,
    recordType: String,
    file: File?
  ) -> Unit,
  role: String
) {
  var description by remember { mutableStateOf(healthRecord.description) }

  // Untuk select record type
  val recordTypeOptions = listOf(
    "Laboratory Test", "Vaccination", "Radiology",
    "Health Record", "Health Facility",
    "Treatment History", "Others"
  )
  var expanded by remember { mutableStateOf(false) }
  var selectedOption by remember { mutableStateOf(healthRecord.recordType) }

  // Untuk file input
  val selectedFileUri = remember { mutableStateOf<Uri?>(null) }
  val fileData = remember { mutableStateOf<File?>(null) }
  val fileMimeType = remember { mutableStateOf<String>("application/octet-stream") }
  val filterMimeTypes = arrayOf("application/pdf", "image/*")

  val filePickerLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
      selectedFileUri.value = uri
    }

  LaunchedEffect(key1 = selectedFileUri.value) {
    val uri = selectedFileUri.value
    if (uri != null) {
      fileData.value = getFileFromUri(context, uri)
      fileMimeType.value = context.contentResolver.getType(uri) ?: "application/octet-stream"
    }
  }

  Column {
    // Top Navigation
    TopAppBarWBackUI("Ubah Health Record", navController)
    Box(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .padding(16.dp),
    ) {
      Column {
        Text(
          text = targetUser.name,
          style = MaterialTheme.typography.titleLarge,
          textAlign = TextAlign.Center,
          modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.padding(bottom = 8.dp))
        Divider(
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
        )

        // Select record type
        Column {
          Spacer(modifier = Modifier.padding(bottom = 8.dp))

          Text(
            text = "Record Type",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.fillMaxWidth()
          )

          OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(selectedOption)
          }

          DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.offset(y = (0).dp) // Sesuaikan posisi dropdown lebih dekat
          ) {
            recordTypeOptions.forEach { option ->
              DropdownMenuItem(
                text = { Text(option) },
                onClick = {
                  selectedOption = option
                  expanded = false
                }
              )
            }
          }
        }

        // Input address
        Spacer(modifier = Modifier.padding(bottom = 8.dp))
        OutlinedTextField(
          value = description,
          singleLine = false,
          shape = shapes.large,
          modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
          colors = TextFieldDefaults.colors(
            focusedContainerColor = colorScheme.surface,
            unfocusedContainerColor = colorScheme.surface,
            disabledContainerColor = colorScheme.surface,
          ),
          onValueChange = { description = it },
          label = { Text("Description") },
          keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Done
          ),
        )

        // Ambil file
        Spacer(modifier = Modifier.padding(bottom = 16.dp))
        OutlinedButton(
          modifier = Modifier.fillMaxWidth(),
          onClick = {
            filePickerLauncher.launch(filterMimeTypes)
          }
        ) {
          Text(text = "Ubah File (PDF/Gambar)")
        }

        selectedFileUri.value?.let { uri ->
          Spacer(modifier = Modifier.padding(bottom = 8.dp))
          if (uri.toString().contains("document%")) {
            Text(text = "File dipilih: PDF", fontWeight = FontWeight.Bold)
          } else {
            Image(
              painter = rememberAsyncImagePainter(uri),
              contentDescription = "Gambar Terpilih",
              modifier = Modifier.size(200.dp)
            )
          }
        }

        if (!description.isBlank()){
          // Submit data
          Spacer(modifier = Modifier.padding(bottom = 16.dp))
          Button(
            onClick = {
              onSubmit(
                fileMimeType.value,
                description,
                selectedOption,
                fileData.value,
              )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = shapes.large,
            colors = ButtonDefaults.buttonColors(
              containerColor = colorScheme.primary
            )
          ) {
            Text(
              text = "Save",
              style = MaterialTheme.typography.titleMedium,
              color = Color.White,
              modifier = Modifier.padding(4.dp)
            )
          }
        }
      }
    }
    // Bottom Navigation
    BottomNavigationBarUI(navController, role)
  }
}


@Preview(showBackground = true)
@Composable
fun PreviewUpdateHealthRecordUIScreen() {
  MyApplicationTheme {

  }
}
