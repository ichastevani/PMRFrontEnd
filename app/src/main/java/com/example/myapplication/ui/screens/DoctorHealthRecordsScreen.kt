package com.example.myapplication.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.R
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorHealthRecordsUI(
  navController: NavHostController,
  healthRecords: ArrayList<DataHealthRecord>,
  targetUser: UserData,
  doctorPermission: DataAccess,
  role: String
) {
  var searchQuery by remember { mutableStateOf("") }

  /// Filter health records berdasarkan search query
  val filteredRecords = if (searchQuery.isEmpty()) {
    healthRecords
  } else {
    healthRecords.filter { it.description.contains(searchQuery, ignoreCase = true)  || it.recordType.contains(searchQuery, ignoreCase = true) || it.creator.name.contains(searchQuery, ignoreCase = true)}
  }

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

        // Input pencarian dengan garis luar biru dan tanpa latar belakang
        TextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          label = { Text("Cari Rekaman Kesehatan") },
          modifier = Modifier
            .fillMaxWidth()
            .padding(2.dp)
            .border(1.dp, Color.Blue, RoundedCornerShape(30.dp))
           // Optional padding to make the TextField look better
            .clip(RoundedCornerShape(30.dp)), // Sudut lebih bulat
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFFefeff7), // Warna border saat fokus
            unfocusedBorderColor = Color(0xFFefeff7), // Warna border saat tidak fokus
            // Tidak ada background
            focusedLabelColor = Color.Blue, // Warna label saat fokus
            unfocusedLabelColor = Color.Gray, // Warna label saat tidak fokus
          ),
          singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
          items(filteredRecords) { item ->
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
        // Row for description, creator name, and image
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Conditional image based on recordType
          when (item.recordType) {
            "Laboratory Test" -> {
              Image(
                painter = painterResource(id = R.drawable.laboratory), // Replace with actual lab image resource
                contentDescription = "Laboratory Test Image",
                modifier = Modifier
                  .size(40.dp)
                  .padding(end = 8.dp) // Space between image and text
              )
            }
            "Vaccination" -> {
              Image(
                painter = painterResource(id = R.drawable.vaccin), // Replace with actual vaccination image resource
                contentDescription = "Vaccination Image",
                modifier = Modifier
                  .size(40.dp)
                  .padding(end = 8.dp) // Space between image and text
              )
            }
            "Radiology" -> {
              Image(
                painter = painterResource(id = R.drawable.radiology), // Replace with actual vaccination image resource
                contentDescription = "Radiology Image",
                modifier = Modifier
                  .size(40.dp)
                  .padding(end = 8.dp) // Space between image and text
              )
            }
            "Physical Examination" -> {
              Image(
                painter = painterResource(id = R.drawable.physical_examination), // Replace with actual vaccination image resource
                contentDescription = "Physical Examination Image",
                modifier = Modifier
                  .size(40.dp)
                  .padding(end = 8.dp) // Space between image and text
              )
            }
            "Therapies" -> {
              Image(
                painter = painterResource(id = R.drawable.therapies), // Replace with actual vaccination image resource
                contentDescription = "Therapies Image",
                modifier = Modifier
                  .size(40.dp)
                  .padding(end = 8.dp) // Space between image and text
              )
            }
            "Medical Procedures" -> {
              Image(
                painter = painterResource(id = R.drawable.procedures), // Replace with actual vaccination image resource
                contentDescription = "Medical Procedures Image",
                modifier = Modifier
                  .size(40.dp)
                  .padding(end = 8.dp) // Space between image and text
              )
            }
            "Specialist Consultations" -> {
              Image(
                painter = painterResource(id = R.drawable.physical_examination), // Replace with actual vaccination image resource
                contentDescription = "Specialist Consultations Image",
                modifier = Modifier
                  .size(40.dp)
                  .padding(end = 8.dp) // Space between image and text
              )
            }
            "Mental Health Records" -> {
              Image(
                painter = painterResource(id = R.drawable.mental ), // Replace with actual vaccination image resource
                contentDescription = "Mental Health Records Image",
                modifier = Modifier
                  .size(40.dp)
                  .padding(end = 8.dp) // Space between image and text
              )
            }
            "Medical Certificates" -> {
              Image(
                painter = painterResource(id = R.drawable.certificates ), // Replace with actual vaccination image resource
                contentDescription = "Medical Certificates Image",
                modifier = Modifier
                  .size(40.dp)
                  .padding(end = 8.dp) // Space between image and text
              )
            }
            "Others" -> {
              Image(
                painter = painterResource(id = R.drawable.others ), // Replace with actual vaccination image resource
                contentDescription = "Others Image",
                modifier = Modifier
                  .size(40.dp)
                  .padding(end = 8.dp) // Space between image and text
              )
            }
          }

          // Column for description and creator name
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = if (item.description.length > 45) "${item.description.take(45)}..." else item.description,
              fontSize = 16.sp
            )
            Text(
              text = buildAnnotatedString {
                append("Creator: ")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                  append(item.creator.name)
                }
              },
              fontSize = 14.sp
            )
          }
        }
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
