package com.example.myapplication.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.myapplication.R
import com.example.myapplication.network.response.DataHealthRecord
import com.example.myapplication.ui.PMRScreenEnum
import com.example.myapplication.ui.components.BottomNavigationBarUI
import com.example.myapplication.ui.components.TopAppBarUI
import com.example.myapplication.ui.viewModels.MetaMaskViewModel
import com.example.myapplication.ui.viewModels.PMRHealthRecordsUiState
import com.example.myapplication.ui.viewModels.PMRLoginUiState
import com.example.myapplication.ui.viewModels.PMRViewModel

@Composable
fun PatientHealthRecordsScreen(
  navController: NavHostController,
  metaMaskViewModel: MetaMaskViewModel,
  pmrViewModel: PMRViewModel,
  snackbarHost: SnackbarHostState,
) {
  var authToken by remember { mutableStateOf("") }
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
        pmrViewModel.getHealthRecords(authToken, ethAddress)
      }
      else -> {
        navController.navigate(PMRScreenEnum.Welcome.name)
      }
    }
  }

  LaunchedEffect(key1 = uiStatePMR.healthRecords) {
    when (uiStatePMR.healthRecords) {
      is PMRHealthRecordsUiState.Success -> {
        dataHealthRecords = (uiStatePMR.healthRecords as PMRHealthRecordsUiState.Success).healthRecords
      }
      else -> {}
    }
  }

  HealthRecordsUI(
    navController,
    healthRecords = dataHealthRecords
  )
}

@Composable
fun HealthRecordsUI(
  navController: NavHostController,
  healthRecords: ArrayList<DataHealthRecord>
) {
  var searchQuery by remember { mutableStateOf("") }

  /// Filter health records berdasarkan search query
  val filteredRecords = if (searchQuery.isEmpty()) {
    healthRecords
  } else {
    healthRecords.filter { it.description.contains(searchQuery, ignoreCase = true)  || it.recordType.contains(searchQuery, ignoreCase = true) || it.creator.name.contains(searchQuery, ignoreCase = true)}
  }

  Column {
    TopAppBarUI(stringResource(R.string.app_name), navController)
    Box(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .padding(16.dp),
    ) {
      Column {
        Text(
          text = "Daftar Health Records",
          style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
          textAlign = TextAlign.Center,
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.padding(bottom = 8.dp))
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
            ItemPatientHealthRecordUI(navController, item)
          }
        }
      }
    }
    // Bottom Navigation
    BottomNavigationBarUI(navController)
  }
}



@Composable
fun ItemPatientHealthRecordUI(navController: NavHostController, item: DataHealthRecord) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .padding(bottom = 6.dp)
      .clickable {
        navController.navigate(PMRScreenEnum.DetailHealthRecord.name + "/${item.id}/${item.creator.address}")
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
              text = if (item.description.length > 45) {
                item.description.substring(0, 45) + "..."
              } else {
                item.description
              },
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
fun PreviewPatientHealthRecordsUI() {

}
