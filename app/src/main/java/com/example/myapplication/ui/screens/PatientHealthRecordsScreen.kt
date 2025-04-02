package com.example.myapplication.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
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

        LazyColumn {
          items(healthRecords) { item ->
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
      .clickable{
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
fun PreviewPatientHealthRecordsUI() {

}
