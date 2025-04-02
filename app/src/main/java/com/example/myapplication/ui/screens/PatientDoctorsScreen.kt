package com.example.myapplication.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.myapplication.R
import com.example.myapplication.network.request.RequestAccessApproving
import com.example.myapplication.network.request.RequestPermissionGrantDoctor
import com.example.myapplication.network.request.RequestPermissionRevokeDoctor
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
fun PatientDoctorsScreen(
  navController: NavHostController,
  metaMaskViewModel: MetaMaskViewModel,
  pmrViewModel: PMRViewModel,
  snackbarHost: SnackbarHostState,
) {
  var authToken by remember { mutableStateOf("") }
  var doctorsRequestAccess by remember { mutableStateOf(ArrayList<DataPermission>()) }
  var doctorsGrantedAccess by remember { mutableStateOf(ArrayList<DataPermission>()) }

  var isProcessApproving by remember { mutableStateOf(false) }

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
        pmrViewModel.getPatientDoctors(authToken)
      }

      else -> {
        navController.navigate(PMRScreenEnum.Welcome.name)
      }
    }
  }

  LaunchedEffect(key1 = uiStatePMR.userAccess) {
    when (uiStatePMR.userAccess) {
      is PMRUserAccessUiState.Success -> {
        val dataPatientDoctors =
          (uiStatePMR.userAccess as PMRUserAccessUiState.Success).userAccess

        doctorsGrantedAccess = dataPatientDoctors.permissions
        doctorsRequestAccess = dataPatientDoctors.requestAccess
      }

      is PMRUserAccessUiState.Error -> {
        doctorsGrantedAccess = ArrayList<DataPermission>()
        doctorsRequestAccess = ArrayList<DataPermission>()
      }

      else -> {}
    }
  }

  val onRequestAccessPatientApproving: (addressDoctor: String, statusApproved: Boolean) -> Unit =
    { addressDoctor, statusApproved ->
      statusApproving = statusApproved
      pmrViewModel.putRequestAccessPatientApproving(
        authToken,
        RequestAccessApproving(addressDoctor, statusApproved)
      )
      isProcessApproving = true
    }

  val onPatientRevokeDoctorPermission: (addressDoctor: String) -> Unit =
    { addressDoctor ->
      statusApproving = true
      pmrViewModel.putPatientRevokeDoctorPermission(
        authToken,
        RequestPermissionRevokeDoctor(addressDoctor)
      )
      isProcessApproving = true
    }

  val onPatientUpdateDoctorPermission: (request: RequestPermissionGrantDoctor) -> Unit =
    { request ->
      statusApproving = true
      pmrViewModel.putPatientPermissionDoctor(
        authToken,
        request
      )
      isProcessApproving = true
    }

  LaunchedEffect(key1 = uiStatePMR.transaction) {
    if (!isProcessApproving) return@LaunchedEffect

    when (uiStatePMR.transaction) {
      is PMRTransactionUiState.Error -> {
        val errMsg = (uiStatePMR.transaction as PMRTransactionUiState.Error).message
        snackbarHost.showSnackbar(
          message = errMsg,
          duration = SnackbarDuration.Long
        )
        isProcessApproving = false
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
        isProcessApproving = false
      }

      else -> {}
    }
  }

  LaunchedEffect(key1 = uiStateMetaMask.isTransactionSuccess) {
    if (uiStateMetaMask.isTransactionSuccess) {
      pmrViewModel.getPatientDoctors(authToken)
      if (statusApproving) {
        selectedTabIndex = 0
      }
      metaMaskViewModel.resetIsTransaction()
    }
  }

  if (isProcessApproving) {
    LoadingPatientDoctorsScreen()
  } else {
    PatientDoctorsUI(
      navController,
      selectedTabIndex,
      { selectedTabIndex = it },
      onRequestAccessPatientApproving,
      onPatientRevokeDoctorPermission,
      onPatientUpdateDoctorPermission,
      doctorsRequestAccess = doctorsRequestAccess,
      doctorsGrantedAccess = doctorsGrantedAccess
    )
  }

}

@Composable
fun PatientDoctorsUI(
  navController: NavHostController,
  selectedTabIndex: Int,
  onChangeSelectedTabIndex: (Int) -> Unit,
  onRequestAccessPatientApproving: (addressDoctor: String, statusApproved: Boolean) -> Unit,
  onPatientRevokeDoctorPermission: (addressDoctor: String) -> Unit,
  onPatientUpdateDoctorPermission: (request: RequestPermissionGrantDoctor) -> Unit,
  doctorsRequestAccess: ArrayList<DataPermission> = ArrayList(),
  doctorsGrantedAccess: ArrayList<DataPermission> = ArrayList(),
) {
  Column {
    TopAppBarUI(stringResource(R.string.app_name), navController)
    PatientDoctorsNavTabs(
      selectedTabIndex,
      onChangeSelectedTabIndex,
      onRequestAccessPatientApproving,
      onPatientRevokeDoctorPermission,
      onPatientUpdateDoctorPermission,
      doctorsRequestAccess = doctorsRequestAccess,
      doctorsGrantedAccess = doctorsGrantedAccess,
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
    )
    // Bottom Navigation
    BottomNavigationBarUI(navController)
  }
}

@Composable
fun LoadingPatientDoctorsScreen(modifier: Modifier = Modifier) {
  Image(
    modifier = modifier.size(200.dp),
    painter = painterResource(R.drawable.loading_img),
    contentDescription = stringResource(R.string.loading)
  )
}

@Composable
fun PatientDoctorsNavTabs(
  selectedTabIndex: Int,
  onChangeSelectedTabIndex: (Int) -> Unit,
  onRequestAccessPatientApproving: (addressDoctor: String, statusApproved: Boolean) -> Unit,
  onPatientRevokeDoctorPermission: (addressDoctor: String) -> Unit,
  onPatientUpdateDoctorPermission: (request: RequestPermissionGrantDoctor) -> Unit,
  doctorsRequestAccess: ArrayList<DataPermission> = ArrayList(),
  doctorsGrantedAccess: ArrayList<DataPermission> = ArrayList(),
  modifier: Modifier
) {
  val tabs = listOf("Granted Access", "Request Access")

  Column(modifier = modifier) {
    Spacer(modifier = Modifier.padding(bottom = 8.dp))
    Text(
      text = "List Doctors",
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
        0 -> PatientDoctorsAccessUI(
          onRequestAccessPatientApproving,
          onPatientRevokeDoctorPermission,
          onPatientUpdateDoctorPermission,
          doctorsGrantedAccess,
          false
        )

        1 -> PatientDoctorsAccessUI(
          onRequestAccessPatientApproving,
          onPatientRevokeDoctorPermission,
          onPatientUpdateDoctorPermission,
          doctorsRequestAccess,
          true
        )
      }
    }
  }

}

@Composable
fun PatientDoctorsAccessUI(
  onRequestAccessPatientApproving: (addressDoctor: String, statusApproved: Boolean) -> Unit,
  onPatientRevokeDoctorPermission: (addressDoctor: String) -> Unit,
  onPatientUpdateDoctorPermission: (request: RequestPermissionGrantDoctor) -> Unit,
  dataDoctors: ArrayList<DataPermission> = ArrayList(),
  isRequestAccess: Boolean = false
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

      if (isRequestAccess) {
        ItemPatientDoctorRequestAccessUI(onRequestAccessPatientApproving, item, access)
      } else {
        ItemPatientDoctorGrantedAccessUI(
          onPatientRevokeDoctorPermission,
          onPatientUpdateDoctorPermission,
          item,
          access
        )
      }
    }
  }
}

@Composable
fun ItemPatientDoctorGrantedAccessUI(
  onPatientRevokeDoctorPermission: (addressDoctor: String) -> Unit,
  onPatientUpdateDoctorPermission: (request: RequestPermissionGrantDoctor) -> Unit,
  item: DataPermission,
  strAccess: String
) {
  var showDialog by remember { mutableStateOf(false) }
  var showDialogPermission by remember { mutableStateOf(false) }

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
          Button(
            onClick = {
              showDialog = true
            },
            colors = ButtonDefaults.buttonColors(
              containerColor = colorScheme.error
            )
          ) {
            Text(
              text = "Revoke",
              style = MaterialTheme.typography.bodySmall,
              color = colorScheme.onError
            )
          }

          Spacer(
            modifier = Modifier.padding(
              horizontal = 4.dp
            )
          )

          Button(
            onClick = {
              showDialogPermission = true
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
        }
      }
    }
  }

  val strMessage = "Apakah Anda yakin ingin mencabut akses ${strAccess} dari dokter ${item.user.name}?"

  ConfirmDialogPatientDoctorRequestAccessApproving(
    strMessage = strMessage,
    showDialog = showDialog,
    onDismiss = { showDialog = false },
    onYesClick = {
      showDialog = false
      onPatientRevokeDoctorPermission(item.user.address)
    },
  )

  PermissionDialog(
    doctor = item,
    showDialog = showDialogPermission,
    onDismiss = { showDialogPermission = false },
    onSave = { read, create, update, delete ->

      onPatientUpdateDoctorPermission(
        RequestPermissionGrantDoctor(
          addressDoctor = item.user.address,
          canCreate =  create,
          canRead =  read,
          canUpdate =  update,
          canDelete =  delete
        )
      )
    }
  )
}

@Composable
fun ItemPatientDoctorRequestAccessUI(
  onRequestAccessPatientApproving: (addressDoctor: String, statusApproved: Boolean) -> Unit,
  item: DataPermission,
  strAccess: String
) {
  var showDialog by remember { mutableStateOf(false) }
  var statusApproved by remember { mutableStateOf(false) }

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
          Button(
            onClick = {
              showDialog = true
              statusApproved = false
            },
            colors = ButtonDefaults.buttonColors(
              containerColor = colorScheme.error
            ),
          ) {
            Text(
              text = "Reject",
              style = MaterialTheme.typography.bodySmall,
              color = colorScheme.onError
            )
          }

          Spacer(
            modifier = Modifier.padding(
              horizontal = 4.dp
            )
          )

          Button(
            onClick = {
              showDialog = true
              statusApproved = true
            },
            colors = ButtonDefaults.buttonColors(
              containerColor = Color.Blue
            ),
          ) {
            Text(
              text = "Approve",
              style = MaterialTheme.typography.bodySmall,
              color = colorScheme.onSecondary
            )
          }
        }
      }
    }
  }

  val strApproved = if (statusApproved) "Menerima" else "Menolak"
  val strMessage =
    "Apakah Anda yakin ingin ${strApproved} memberikan akses ${strAccess} ke dokter ${item.user.name}?"

  ConfirmDialogPatientDoctorRequestAccessApproving(
    strMessage = strMessage,
    showDialog = showDialog,
    onDismiss = { showDialog = false },
    onYesClick = {
      showDialog = false
      onRequestAccessPatientApproving(item.user.address, statusApproved)
    },
  )
}

@Composable
fun ConfirmDialogPatientDoctorRequestAccessApproving(
  strMessage: String,
  showDialog: Boolean,
  onDismiss: () -> Unit,
  onYesClick: () -> Unit,
) {
  if (showDialog) {
    AlertDialog(
      onDismissRequest = onDismiss,
      title = { Text(text = "Konfirmasi") },
      text = { Text(text = strMessage) },
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

@Composable
fun PermissionDialog(
  doctor: DataPermission,
  showDialog: Boolean,
  onDismiss: () -> Unit,
  onSave: (Boolean, Boolean, Boolean, Boolean) -> Unit
) {
  if (showDialog) {
    var canRead by remember { mutableStateOf(doctor.access.canRead) }
    var canCreate by remember { mutableStateOf(doctor.access.canCreate) }
    var canUpdate by remember { mutableStateOf(doctor.access.canUpdate) }
    var canDelete by remember { mutableStateOf(doctor.access.canDelete) }

    AlertDialog(
      onDismissRequest = onDismiss,
      title = { Text(text = "Perbarui Hak Akses") },
      text = {
        Column {
          Text("Ubah hak akses untuk dokter ${doctor.user.name}?")
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

@Composable
fun CheckboxRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp)
  ) {
    Checkbox(checked = checked, onCheckedChange = onCheckedChange)
    Text(text = label, modifier = Modifier.padding(start = 8.dp))
  }
}

@Preview(showBackground = true)
@Composable
fun PreviewItemPatientDoctorRequestAccessUI() {

}
