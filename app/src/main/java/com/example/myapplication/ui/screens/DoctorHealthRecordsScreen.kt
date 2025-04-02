package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.network.response.DataAccess
import com.example.myapplication.network.response.DataHealthRecord
import com.example.myapplication.network.response.UserData
import com.example.myapplication.ui.PMRScreenEnum
import com.example.myapplication.ui.components.BottomNavigationBarUI
import com.example.myapplication.ui.components.LoadingUI
import com.example.myapplication.ui.components.TopAppBarWBackUI
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.viewModels.MetaMaskViewModel
import com.example.myapplication.ui.viewModels.PMRDoctorPermission
import com.example.myapplication.ui.viewModels.PMRHealthRecordsUiState
import com.example.myapplication.ui.viewModels.PMRLoginUiState
import com.example.myapplication.ui.viewModels.PMRUserPermissionUiState
import com.example.myapplication.ui.viewModels.PMRViewModel

@Composable
fun DoctorHealthRecordsScreen(
  navController: NavHostController,
  metaMaskViewModel: MetaMaskViewModel,
  pmrViewModel: PMRViewModel,
  snackbarHost: SnackbarHostState,
  patientAddress: String,
) {
  var authToken by remember { mutableStateOf("") }
  var role by remember { mutableStateOf("Patient") }

  var targetUser by remember { mutableStateOf<UserData?>(null) }
  var doctorPermission by remember { mutableStateOf<DataAccess?>(null) }
  var dataHealthRecords by remember { mutableStateOf(ArrayList<DataHealthRecord>()) }

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
        pmrViewModel.getHealthRecords(authToken, patientAddress)
      }

      else -> {
        navController.navigate(PMRScreenEnum.Welcome.name)
      }
    }
  }

  LaunchedEffect(key1 = uiStatePMR.healthRecords) {
    when (uiStatePMR.healthRecords) {
      is PMRHealthRecordsUiState.Success -> {
        dataHealthRecords =
          (uiStatePMR.healthRecords as PMRHealthRecordsUiState.Success).healthRecords
      }

      else -> {}
    }
  }

  LaunchedEffect(key1 = uiStatePMR.userPermission) {
    when (uiStatePMR.userPermission) {
      is PMRUserPermissionUiState.Success -> {
        targetUser = (uiStatePMR.userPermission as PMRUserPermissionUiState.Success).user
        doctorPermission =
          (uiStatePMR.userPermission as PMRUserPermissionUiState.Success).permission
      }

      is PMRUserPermissionUiState.Error -> {
        navController.navigate(PMRScreenEnum.Home.name)
      }

      else -> {}
    }
  }

  if (targetUser == null || doctorPermission == null) {
    LoadingUI()
  } else {
    DoctorHealthRecordsUI(
      navController,
      healthRecords = dataHealthRecords,
      targetUser = targetUser!!,
      doctorPermission = doctorPermission!!,
      role
    )
  }
}

@Composable
fun DoctorHealthRecordsUI(
  navController: NavHostController,
  healthRecords: ArrayList<DataHealthRecord>,
  targetUser: UserData,
  doctorPermission: DataAccess,
  role: String
) {
  Column(
    modifier = Modifier.fillMaxSize()
  ) {
    TopAppBarWBackUI(
      title = "Daftar Health Records",
      navController = navController,
    )

    Box(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      Column {
        Text(
          text = targetUser.name,
          style = MaterialTheme.typography.titleLarge,
          textAlign = TextAlign.Center,
          modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Divider(
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
        )

        LazyColumn {
          items(healthRecords) { item ->
            DoctorItemHealthRecordUI(navController, targetUser, item)
          }
        }
      }

      if (doctorPermission.canCreate) {
        FloatingActionButton(
          onClick = {
            navController.navigate(PMRScreenEnum.AddHealthRecord.name + "/${targetUser.address}")
          },
          containerColor = colorScheme.primary,
          modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(16.dp)
        ) {
          Icon(imageVector = Icons.Default.Add, contentDescription = "Add Health Records")
        }
      }
    }

    BottomNavigationBarUI(navController, role)
  }
}


@Composable
fun DoctorItemHealthRecordUI(
  navController: NavHostController,
  targetUser: UserData,
  item: DataHealthRecord
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .padding(bottom = 6.dp)
      .clickable {
        navController.navigate(PMRScreenEnum.DetailHealthRecord.name + "/${item.id}/${targetUser.address}")
      }
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
          text = item.description,
          fontSize = 16.sp
        )
        Text(
          text = "Creator: ${item.creator.name}",
          fontSize = 14.sp
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
        text = item.recordType,
        fontSize = 12.sp,
        color = Color.White,
        modifier = Modifier
          .background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(4.dp))
          .padding(horizontal = 8.dp, vertical = 4.dp)
      )
    }
  }
}

@Preview(showBackground = true)
@Composable
fun PreviewDoctorHealthRecordsUI() {
  val dummyUser = UserData("0x123", "John Doe", "1234567890", "Patient", "Test")

  val dummyRecords = arrayListOf(
    DataHealthRecord(
      "1",
      dummyUser,
      "Medical Checkup",
      "CID_001",
      "Health",
      "22",
      "true",
      "1.0",
      "0"
    ),
    DataHealthRecord(
      "2",
      dummyUser,
      "Blood Test Report",
      "CID_002",
      "Lab",
      "22",
      "true",
      "1.1",
      "1"
    ),
    DataHealthRecord(
      "3",
      dummyUser,
      "X-Ray Scan",
      "CID_003",
      "Radiology",
      "22",
      "false",
      "1.0",
      "2"
    )
  )

  MyApplicationTheme {
    DoctorHealthRecordsUI(
      navController = rememberNavController(),
      healthRecords = dummyRecords,
      targetUser = dummyUser,
      doctorPermission = DataAccess(true, true, true, true),
      "Patient"
    )
  }
}
