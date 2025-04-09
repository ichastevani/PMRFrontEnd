package com.example.myapplication.ui.screens

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Card
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import android.graphics.pdf.PdfRenderer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.network.request.RequestHealthRecordAdd
import com.example.myapplication.network.response.UserData
import com.example.myapplication.ui.PMRScreenEnum
import com.example.myapplication.ui.components.BottomNavigationBarUI
import androidx.compose.ui.graphics.asImageBitmap
import android.graphics.Bitmap
import com.example.myapplication.ui.components.LoadingUI
import com.example.myapplication.ui.components.TopAppBarUI
import com.example.myapplication.ui.components.TopAppBarWBackUI
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.viewModels.MetaMaskViewModel
import com.example.myapplication.ui.viewModels.PMRLoginUiState
import com.example.myapplication.ui.viewModels.PMRTransactionUiState
import com.example.myapplication.ui.viewModels.PMRUserPermissionUiState
import com.example.myapplication.ui.viewModels.PMRViewModel
import com.example.myapplication.utils.ToolsUtil.getFileFromUri
import com.example.myapplication.R
import androidx.compose.material.icons.filled.ArrowDropDown
import java.io.FileInputStream
import java.io.File

@Composable
fun AddHealthRecordScreen(
  navController: NavHostController,
  metaMaskViewModel: MetaMaskViewModel,
  pmrViewModel: PMRViewModel,
  snackbarHost: SnackbarHostState,
  patientAddress: String,
) {
  val context = LocalContext.current
  var isProcessAddHealthRecord by remember { mutableStateOf(false) }
  var authToken by remember { mutableStateOf("") }
  var role by remember { mutableStateOf("Patient") }

  var targetUser by remember { mutableStateOf<UserData?>(null) }

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
        pmrViewModel.getUserByAddress(authToken, patientAddress)
      }
      else -> {
        navController.navigate(PMRScreenEnum.Welcome.name)
      }
    }
  }

  LaunchedEffect(key1 = uiStatePMR.transaction) {
    if(!isProcessAddHealthRecord) return@LaunchedEffect

    when (uiStatePMR.transaction) {
      is PMRTransactionUiState.Error -> {
        val errMsg = (uiStatePMR.transaction as PMRTransactionUiState.Error).message
        snackbarHost.showSnackbar(
          message = errMsg,
          duration = SnackbarDuration.Long
        )
        isProcessAddHealthRecord = false
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
        isProcessAddHealthRecord = false
      }
      else -> {}
    }
  }

  LaunchedEffect(key1 = uiStateMetaMask.isTransactionSuccess) {
    if(uiStateMetaMask.isTransactionSuccess){
      if(role == "Doctor"){
        navController.navigate(PMRScreenEnum.DoctorHealthRecords.name + "/${patientAddress}")
      }else{
        navController.navigate(PMRScreenEnum.PatientHealthRecords.name)
      }

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

  val onSubmit: (mimeType: String, request: RequestHealthRecordAdd) -> Unit = { mimeType, request ->
    pmrViewModel.addHealthRecords(
      authToken,
      mimeType,
      request
    )
    isProcessAddHealthRecord = true
  }

  if (isProcessAddHealthRecord || targetUser == null) {
    LoadingUI()
  } else {
    AddHealthRecordUI(
      navController,
      context,
      targetUser!!,
      onSubmit,
      role
    )
  }

}

@Composable
fun AddHealthRecordUI(
  navController: NavHostController,
  context: Context,
  targetUser: UserData,
  onSubmit: (mimeType: String, request: RequestHealthRecordAdd) -> Unit,
  role: String
) {
  var description by remember { mutableStateOf("") }

  // Untuk select record type
  val recordTypeOptions = listOf(
    "Laboratory Test", "Vaccination", "Radiology",
    "Physical Examination", "Therapies",
    "Medical Procedures", "Specialist Consultations", "Medications ", "Mental Health Records", "Medical Certificates"
  )
  var expanded by remember { mutableStateOf(false) }
  var selectedOption by remember { mutableStateOf(recordTypeOptions[0]) }

  // Untuk file input
  val selectedFileUri = remember { mutableStateOf<Uri?>(null) }
  val fileData = remember { mutableStateOf<File?>(null) }
  val fileMimeType = remember { mutableStateOf<String>("application/octet-stream") }
  val filterMimeTypes = arrayOf("application/pdf", "image/*")

  val filePickerLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
      Log.d("AddHealthRecordUI", "uri: $uri")
      selectedFileUri.value = uri
    }

  LaunchedEffect(key1 = selectedFileUri.value) {
    val uri = selectedFileUri.value
    Log.d("AddHealthRecordUI LaunchedEffect", "uri: $uri")
    if (uri != null) {
      fileData.value = getFileFromUri(context, uri)
      fileMimeType.value = context.contentResolver.getType(uri) ?: "application/octet-stream"
    }
  }

  // State untuk menyimpan bitmap preview PDF
  var pdfPreviewBitmaps by remember { mutableStateOf<List<Bitmap>>(emptyList()) }

  // Fungsi untuk menghasilkan bitmap preview dari PDF
  fun generatePdfPreview(context: Context, uri: Uri) {
    try {
      context.contentResolver.openFileDescriptor(uri, "r")?.use { parcelFileDescriptor ->
        PdfRenderer(parcelFileDescriptor).use { renderer ->
          if (renderer.pageCount > 0) {
              val bitmaps = mutableListOf<Bitmap>()
              for (i in 0 until renderer.pageCount) {
                  val page = renderer.openPage(i)
                  val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                  page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                  bitmaps.add(bitmap)
                  page.close()
              }
            pdfPreviewBitmaps = bitmaps
          } else {
              // Handle case where there are no pages
              Log.e("PDFPreview", "No pages found in PDF")
              pdfPreviewBitmaps = emptyList()// or set a default error bitmap
           }
        }
      }
    } catch (e: Exception) {
      Log.e("PDFPreview", "Error generating PDF preview", e)
      pdfPreviewBitmaps = emptyList()// or set a default error bitmap
    }
  }
  Column {
    // Top Navigation
    if(role == "Doctor"){
      TopAppBarWBackUI("Tambah Health Record", navController)
    }else{
      TopAppBarUI(stringResource(R.string.app_name), navController)
    }

    Box(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .padding(16.dp),
    ) {
      Column {
        if(role == "Doctor"){
          Text(

            text = "Tambah Health Record",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
          )
        }else{
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 8.dp), // Menambahkan padding vertical agar tidak terlalu rapat
            verticalAlignment = Alignment.CenterVertically // Untuk memastikan keduanya ter-align dengan baik secara vertikal
          ) {
            Text(
              text = "Nama Pasien: ",
              style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            )
            Spacer(modifier = Modifier.width(5.dp)) // Menambahkan jarak antara label dan nama pasien
            Text(
              text = targetUser.name,
              style = MaterialTheme.typography.bodyMedium,
            )
          }


          Divider(
            modifier = Modifier
              .fillMaxWidth()
              .padding(bottom = 12.dp)
          )
          Text(
            text = "(Lengkapi data berikut untuk menambahkan rekam medis baru: pilih jenis rekam medis, beri deskripsi, dan unggah gambar atau file PDF terkait)",
            style = MaterialTheme.typography.bodySmall.copy(
              fontStyle = FontStyle.Italic,
              color = Color.Red
            )


          )
        }

        Spacer(modifier = Modifier.padding(bottom = 8.dp))


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
            Icon(
              imageVector = Icons.Filled.ArrowDropDown,
              contentDescription = "Drop-down Arrow",
            )
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
        Card(
          modifier = Modifier
            .width(120.dp)
            .clip(MaterialTheme.shapes.medium)
            .wrapContentSize()
            .clickable {
              // Aksi untuk memilih file
              filePickerLauncher.launch(filterMimeTypes)
            },
        ) {
          Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center,
              modifier = Modifier.fillMaxSize().padding(16.dp)
            ) {
              Icon(
                painter = painterResource(id = R.drawable.add),
                contentDescription = "Pilih File",
                modifier = Modifier.size(40.dp)
              )
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = "Pilih File",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
              )
            }
          }
        }


        selectedFileUri.value?.let { uri ->
          Spacer(modifier = Modifier.padding(bottom = 8.dp))
          val isPdf = fileMimeType.value.equals("application/pdf", ignoreCase = true)
          if (isPdf) {
            LaunchedEffect(key1 = uri) {
              generatePdfPreview(context, uri)
            }
            Text(text = "Preview PDF:", fontWeight = FontWeight.Bold)
            LazyRow(modifier = Modifier.fillMaxWidth()) {
              items(pdfPreviewBitmaps.size) { index ->
                  Image(
                      bitmap = pdfPreviewBitmaps[index].asImageBitmap(),
                      contentDescription = "PDF Preview Page ${index + 1}",
                      modifier = Modifier
                        .width(200.dp)
                        .aspectRatio(1f)
                        .padding(8.dp)
                  )
              }

            }
          }else {
            Text(text = "Preview Gambar:", fontWeight = FontWeight.Bold)
            Image(

              painter = rememberAsyncImagePainter(uri),
              contentDescription = "Gambar Terpilih",
              modifier = Modifier.size(200.dp)
            )
          }
        }

        fileData.value?.let { file ->
          if (description.isBlank()) return
          // Submit data
          Spacer(modifier = Modifier.padding(bottom = 16.dp))
          Button(
            onClick = {
              onSubmit(
                fileMimeType.value,
                RequestHealthRecordAdd(
                  file,
                  targetUser.address,
                  description = description,
                  recordType = selectedOption,
                )
              )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = shapes.large,
            colors = ButtonDefaults.buttonColors(
              containerColor = colorScheme.primary
            )
          ) {
            Text(
              text = "Submit",
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
fun PreviewDoctorHealthRecordUIScreen() {
  MyApplicationTheme {
    AddHealthRecordUI(
      navController = rememberNavController(),
      LocalContext.current,
      UserData("0x123", "John Doe", "1234567890", "Patient", "Test"),
      {} as (String, RequestHealthRecordAdd) -> Unit,
      "Doctor"
    )
  }
}
