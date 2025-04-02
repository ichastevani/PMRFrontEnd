package com.example.myapplication.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.myapplication.R
import com.example.myapplication.network.request.RequestAccessDoctor
import com.example.myapplication.network.response.DataPermission
import com.example.myapplication.ui.PMRScreenEnum
import com.example.myapplication.ui.components.BottomNavigationBarUI
import com.example.myapplication.ui.components.TopAppBarUI
import com.example.myapplication.ui.viewModels.MetaMaskViewModel
import com.example.myapplication.ui.viewModels.PMRLoginUiState
import com.example.myapplication.ui.viewModels.PMRTransactionUiState
import com.example.myapplication.ui.viewModels.PMRUserAccessUiState
import com.example.myapplication.ui.viewModels.PMRViewModel

@Composable
fun DoctorPatientsScreen(
  navController: NavHostController,
  metaMaskViewModel: MetaMaskViewModel,
  pmrViewModel: PMRViewModel,
  snackbarHost: SnackbarHostState,
) {
  var authToken by remember { mutableStateOf("") }
  var patientsPending by remember { mutableStateOf(ArrayList<DataPermission>()) }
  var patientsApproved by remember { mutableStateOf(ArrayList<DataPermission>()) }

  var isProcessRequestAccess by remember { mutableStateOf(false) }

  var statusApproving by remember { mutableStateOf(false) }
  var selectedTabIndex by remember { mutableStateOf(0) }

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
        pmrViewModel.getDoctorPatients(authToken)
      }

      else -> {
        navController.navigate(PMRScreenEnum.Welcome.name)
      }
    }
  }

  LaunchedEffect(key1 = uiStatePMR.userAccess) {
    when (uiStatePMR.userAccess) {
      is PMRUserAccessUiState.Success -> {
        val dataAccess =
          (uiStatePMR.userAccess as PMRUserAccessUiState.Success).userAccess

        patientsApproved = dataAccess.permissions
        patientsPending = dataAccess.requestAccess
      }

      is PMRUserAccessUiState.Error -> {
        patientsApproved = ArrayList<DataPermission>()
        patientsPending = ArrayList<DataPermission>()
      }

      else -> {}
    }
  }

  val onRequestAccessDoctor: (request: RequestAccessDoctor) -> Unit =
    { request ->
      pmrViewModel.putRequestAccessDoctor(
        authToken,
        request
      )
      isProcessRequestAccess = true
    }

  LaunchedEffect(key1 = uiStatePMR.transaction) {
    if (!isProcessRequestAccess) return@LaunchedEffect

    when (uiStatePMR.transaction) {
      is PMRTransactionUiState.Error -> {
        val errMsg = (uiStatePMR.transaction as PMRTransactionUiState.Error).message
        snackbarHost.showSnackbar(
          message = errMsg,
          duration = SnackbarDuration.Long
        )
        isProcessRequestAccess = false
      }

      is PMRTransactionUiState.Success -> {
        val transactionData = (uiStatePMR.transaction as PMRTransactionUiState.Success).transaction
        if (transactionData != null) {
          metaMaskViewModel.sendRequest(transactionData.toString())
        } else {
          snackbarHost.showSnackbar(
            message = "Gagal melakukan tindakan",
            duration = SnackbarDuration.Long
          )
        }
        isProcessRequestAccess = false
      }

      else -> {}
    }
  }

  LaunchedEffect(key1 = uiStateMetaMask.isTransactionSuccess) {
    if (uiStateMetaMask.isTransactionSuccess) {
      pmrViewModel.getDoctorPatients(authToken)
      if (statusApproving) {
        selectedTabIndex = 0
      }
      metaMaskViewModel.resetIsTransaction()
    }
  }

  if (isProcessRequestAccess) {
    LoadingDoctorPatientsScreen()
  } else {
    DoctorPatientsUI(
      navController,
      selectedTabIndex,
      { selectedTabIndex = it },
      onRequestAccessDoctor,
      patientsPending = patientsPending,
      patientsApproved = patientsApproved
    )
  }

}

@Composable
fun DoctorPatientsUI(
  navController: NavHostController,
  selectedTabIndex: Int,
  onChangeSelectedTabIndex: (Int) -> Unit,
  onRequestAccessDoctor: (request: RequestAccessDoctor) -> Unit,
  patientsPending: ArrayList<DataPermission> = ArrayList(),
  patientsApproved: ArrayList<DataPermission> = ArrayList(),
) {
  Column {
    TopAppBarUI(stringResource(R.string.app_name), navController)
    DoctorPatientsNavTabs(
      selectedTabIndex,
      onChangeSelectedTabIndex,
      onRequestAccessDoctor,
      patientsPending = patientsPending,
      patientsApproved = patientsApproved,
      navController = navController,
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
    )
    // Bottom Navigation
    BottomNavigationBarUI(navController, "Doctor")
  }
}

@Composable
fun LoadingDoctorPatientsScreen(modifier: Modifier = Modifier) {
  Image(
    modifier = modifier.size(200.dp),
    painter = painterResource(R.drawable.loading_img),
    contentDescription = stringResource(R.string.loading)
  )
}

@Composable
fun DoctorPatientsNavTabs(
  selectedTabIndex: Int,
  onChangeSelectedTabIndex: (Int) -> Unit,
  onRequestAccessDoctor: (request: RequestAccessDoctor) -> Unit,
  patientsPending: ArrayList<DataPermission> = ArrayList(),
  patientsApproved: ArrayList<DataPermission> = ArrayList(),
  navController: NavHostController,
  modifier: Modifier
) {
  val tabs = listOf("Approved", "Pending")

  Column(modifier = modifier) {
    Spacer(modifier = Modifier.padding(bottom = 8.dp))
    Text(
      text = "List Patients",
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

    // Tabs Row
    TabRow(selectedTabIndex = selectedTabIndex) {
      tabs.forEachIndexed { index, title ->
        Tab(
          selected = selectedTabIndex == index,
          onClick = { onChangeSelectedTabIndex(index) },
          text = { Text(title) }
        )
      }
    }
    Spacer(modifier = Modifier.height(8.dp))
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
    ) {
      // Content based on selected tab
      when (selectedTabIndex) {
        0 -> DoctorPatientsAccessUI(
          onRequestAccessDoctor,
          patientsApproved,
          navController,
          false
        )

        1 -> DoctorPatientsAccessUI(
          onRequestAccessDoctor,
          patientsPending,
          navController,
          true
        )
      }
    }

    if (selectedTabIndex == 1) {
      var showDialogCreate by remember { mutableStateOf(false) }
      // Floating Action Button
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(16.dp),
        contentAlignment = Alignment.BottomEnd
      ) {
        FloatingActionButton(
          onClick = { showDialogCreate = true },
          containerColor = colorScheme.primary
        ) {
          Icon(imageVector = Icons.Default.Add, contentDescription = "Add Patient")
        }
      }

      PermissionDialogCreateRequestAccess(
        showDialog = showDialogCreate,
        onDismiss = { showDialogCreate = false },
        onRequestAccessDoctor = onRequestAccessDoctor
      )
    }
  }
}

@Composable
fun DoctorPatientsAccessUI(
  onRequestAccessDoctor: (request: RequestAccessDoctor) -> Unit,
  dataDoctors: ArrayList<DataPermission> = ArrayList(),
  navController: NavHostController,
  isPending: Boolean
) {
  LazyColumn {
    items(dataDoctors) { item ->
      var access = "Access: "

      val accessPermission = listOf(
        "Create".takeIf { item.access.canCreate },
        "Read".takeIf { item.access.canRead },
        "Update".takeIf { item.access.canUpdate },
        "Delete".takeIf { item.access.canDelete }
      )

      // Filter `null` values dan gabungkan string dengan koma
      access += accessPermission.filterNotNull().joinToString(", ")

      ItemDoctorPatientUI(
        onRequestAccessDoctor,
        item,
        access,
        navController,
        isPending
      )
    }
  }
}

@Composable
fun ItemDoctorPatientUI(
  onRequestAccessDoctor: (request: RequestAccessDoctor) -> Unit,
  item: DataPermission,
  strAccess: String,
  navController: NavHostController,
  isPending: Boolean
) {
  var showDialog by remember { mutableStateOf(false) }

  Box(
    modifier = Modifier
      .fillMaxWidth()
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
          text = item.user.name,
          fontSize = 16.sp
        )
        Text(
          text = item.user.address,
          fontSize = 12.sp,
          color = Color.Gray,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        Spacer(
          modifier = Modifier.padding(
            bottom = 2.dp
          )
        )
        Text(
          text = strAccess,
          fontSize = 14.sp,
          color = Color.Blue
        )
        Spacer(
          modifier = Modifier.padding(
            bottom = 6.dp
          )
        )

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(6.dp),
          horizontalArrangement = Arrangement.End
        ) {
          Spacer(
            modifier = Modifier.padding(
              horizontal = 4.dp
            )
          )

          Button(
            onClick = {
              showDialog = true
            },
            colors = ButtonDefaults.buttonColors(
              containerColor = Color.Blue
            ),
          ) {
            Text(
              text = "Update",
              style = MaterialTheme.typography.bodySmall,
              color = colorScheme.onSecondary
            )
          }

          Spacer(
            modifier = Modifier.padding(
              horizontal = 4.dp
            )
          )

          if(!isPending){
            Button(
              onClick = {
                navController.navigate(PMRScreenEnum.DoctorHealthRecords.name + "/${item.user.address}")
              },
              colors = ButtonDefaults.buttonColors(
                containerColor = Color.Blue
              ),
            ) {
              Text(
                text = "Health Records",
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSecondary
              )
            }
          }



        }
      }
    }
  }

  PermissionDialogUpdateRequestAccess(
    patient = item,
    showDialog = showDialog,
    onDismiss = { showDialog = false },
    onSave = { read, create, update, delete ->
      onRequestAccessDoctor(
        RequestAccessDoctor(
          addressPatient =  item.user.address,
          canRead = read,
          canCreate = create,
          canUpdate = update,
          canDelete = delete
        )
      )
    }
  )
}

@Composable
fun PermissionDialogCreateRequestAccess(
  showDialog: Boolean,
  onDismiss: () -> Unit,
  onRequestAccessDoctor: (request: RequestAccessDoctor) -> Unit,
) {
  if (showDialog) {
    var address by remember { mutableStateOf("") }
    var canRead by remember { mutableStateOf(false) }
    var canCreate by remember { mutableStateOf(false) }
    var canUpdate by remember { mutableStateOf(false) }
    var canDelete by remember { mutableStateOf(false) }

    AlertDialog(
      onDismissRequest = onDismiss,
      title = { Text(text = "Request Hak Akses") },
      text = {
        Column {
          OutlinedTextField(
            value = address,
            singleLine = true,
            shape = shapes.large,
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
              focusedContainerColor = colorScheme.surface,
              unfocusedContainerColor = colorScheme.surface,
              disabledContainerColor = colorScheme.surface,
            ),
            onValueChange = { address = it },
            label = { Text("Patient Address") },
            keyboardOptions = KeyboardOptions.Default.copy(
              imeAction = ImeAction.Done
            ),
          )
          CheckboxRow("Can Read", canRead) { canRead = it }
          CheckboxRow("Can Create", canCreate) { canCreate = it }
          CheckboxRow("Can Update", canUpdate) { canUpdate = it }
          CheckboxRow("Can Delete", canDelete) { canDelete = it }
        }
      },
      confirmButton = {
        Button(onClick = {
          onRequestAccessDoctor(
            RequestAccessDoctor(
              addressPatient =  address,
              canRead = canRead,
              canCreate = canCreate,
              canUpdate = canUpdate,
              canDelete = canDelete
            )
          )
          onDismiss()
        }) {
          Text("Send")
        }
      },
      dismissButton = {
        TextButton(onClick = onDismiss) {
          Text("Cancel")
        }
      }
    )
  }
}

@Composable
fun PermissionDialogUpdateRequestAccess(
  patient: DataPermission,
  showDialog: Boolean,
  onDismiss: () -> Unit,
  onSave: (Boolean, Boolean, Boolean, Boolean) -> Unit
) {
  if (showDialog) {
    var canRead by remember { mutableStateOf(patient.access.canRead) }
    var canCreate by remember { mutableStateOf(patient.access.canCreate) }
    var canUpdate by remember { mutableStateOf(patient.access.canUpdate) }
    var canDelete by remember { mutableStateOf(patient.access.canDelete) }

    AlertDialog(
      onDismissRequest = onDismiss,
      title = { Text(text = "Perbarui Hak Akses") },
      text = {
        Column {
          Text("Buat Request Access baru ke pasien ${patient.user.name}. Ini akan menghapus hak akses sebelumnya.")
          CheckboxRow("Can Read", canRead) { canRead = it }
          CheckboxRow("Can Create", canCreate) { canCreate = it }
          CheckboxRow("Can Update", canUpdate) { canUpdate = it }
          CheckboxRow("Can Delete", canDelete) { canDelete = it }
        }
      },
      confirmButton = {
        Button(onClick = {
          onSave(canRead, canCreate, canUpdate, canDelete)
          onDismiss()
        }) {
          Text("Save")
        }
      },
      dismissButton = {
        TextButton(onClick = onDismiss) {
          Text("Cancel")
        }
      }
    )
  }
}

@Preview(showBackground = true)
@Composable
fun PreviewItemDoctorPatientRequestAccessUI() {

}
