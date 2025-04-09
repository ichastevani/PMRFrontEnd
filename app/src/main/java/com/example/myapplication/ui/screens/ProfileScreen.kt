//package com.example.myapplication.ui.screens
//
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material3.SnackbarHostState
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.collectAsState
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.res.stringResource
//import androidx.compose.ui.unit.dp
//import androidx.navigation.NavHostController
//import com.example.myapplication.R
//import com.example.myapplication.network.response.UserData
//import com.example.myapplication.ui.PMRScreenEnum
//import com.example.myapplication.ui.components.BottomNavigationBarUI
//import com.example.myapplication.ui.components.LoadingUI
//import com.example.myapplication.ui.components.TopAppBarUI
//import com.example.myapplication.ui.components.TopAppBarWBackUI
//import com.example.myapplication.ui.viewModels.MetaMaskViewModel
//import com.example.myapplication.ui.viewModels.PMRLoginUiState
//import com.example.myapplication.ui.viewModels.PMRViewModel
//
//@Composable
//fun ProfileScreen(
//  navController: NavHostController,
//  metaMaskViewModel: MetaMaskViewModel,
//  pmrViewModel: PMRViewModel,
//  snackbarHost: SnackbarHostState,
//) {
//  var authToken by remember { mutableStateOf("") }
//  var userData: UserData? by remember { mutableStateOf(null) }
//
//  val uiStatePMR by pmrViewModel.uiState.collectAsState()
//  val uiStateMetaMask by metaMaskViewModel.uiState.collectAsState()
//
//  val ethAddress = uiStateMetaMask.ethAddress
//  if (ethAddress == null) {
//    navController.navigate(PMRScreenEnum.Welcome.name)
//    return
//  }
//
//  // Panggil sekali untuk cek apakah pengguna telah login
//  LaunchedEffect(Unit) {
//    when (uiStatePMR.login) {
//      is PMRLoginUiState.Success -> {
//        authToken = (uiStatePMR.login as PMRLoginUiState.Success).token
//        userData = (uiStatePMR.login as PMRLoginUiState.Success).user
//      }
//      else -> {
//        navController.navigate(PMRScreenEnum.Welcome.name)
//      }
//    }
//  }
//
//  if(userData != null){
//    ProfileUI(
//      navController,
//      userData!!
//    )
//  }else{
//    LoadingUI()
//  }
//}
//
//@Composable
//fun ProfileUI(
//  navController: NavHostController,
//  userData: UserData
//) {
//  Column {
//    TopAppBarWBackUI("Profile", navController)
//    Box(
//      modifier = Modifier
//        .weight(1f)
//        .fillMaxWidth()
//        .verticalScroll(rememberScrollState())
//        .padding(16.dp),
//    ) {
//      Column {
//        Text(text = "Name: ${userData.name}")
//        Text(text = "Home Address: ${userData.homeAddress}")
//        Text(text = "Role: ${userData.role}")
//        Text(text = "Public Address: ${userData.address}")
//      }
//    }
//  }
//}

package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.myapplication.R
import com.example.myapplication.network.response.UserData
import com.example.myapplication.ui.PMRScreenEnum
import com.example.myapplication.ui.components.BottomNavigationBarUI
import com.example.myapplication.ui.components.LoadingUI
import com.example.myapplication.ui.components.TopAppBarUI
import com.example.myapplication.ui.components.TopAppBarWBackUI
import com.example.myapplication.ui.viewModels.MetaMaskViewModel
import com.example.myapplication.ui.viewModels.PMRLoginUiState
import com.example.myapplication.ui.viewModels.PMRViewModel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource


@Composable
fun ProfileScreen(
  navController: NavHostController,
  metaMaskViewModel: MetaMaskViewModel,
  pmrViewModel: PMRViewModel,
  snackbarHost: SnackbarHostState,
) {
  var authToken by remember { mutableStateOf("") }
  var userData: UserData? by remember { mutableStateOf(null) }

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
        userData = (uiStatePMR.login as PMRLoginUiState.Success).user
      }
      else -> {
        navController.navigate(PMRScreenEnum.Welcome.name)
      }
    }
  }

  if(userData != null){
    ProfileUI(
      navController,
      userData!!
    )
  }else{
    LoadingUI()
  }
}

//@Composable
//fun ProfileUI(
//  navController: NavHostController,
//  userData: UserData
//) {
//  Column(
//    modifier = Modifier
//      .fillMaxSize()
//      .background(Color(0xFFC7DAF5)) // Warna latar belakang
//      .padding(top = 40.dp), // Jarak dari atas
//    horizontalAlignment = Alignment.CenterHorizontally
//  ) {
//    TopAppBarWBackUI("Profile", navController = navController)
//    Box(
//      modifier = Modifier
//        .fillMaxWidth()
//        .weight(1f)
//        .padding(16.dp)
//        .background(Color.White, shape = RoundedCornerShape(24.dp))
//        .padding(vertical = 32.dp, horizontal = 24.dp),
//      contentAlignment = Alignment.TopCenter
//    ) {
//      Column(
//        horizontalAlignment = Alignment.CenterHorizontally,
//        modifier = Modifier.verticalScroll(rememberScrollState())
//      ) {
//        Image(
//          painter = painterResource(id = R.drawable.undraw_female_avatar_7t6k_1), // Tambahkan gambar ke drawable
//          contentDescription = "Profile Picture",
//          modifier = Modifier
//            .size(100.dp)
//            .clip(CircleShape)
//        )
//        Spacer(modifier = Modifier.height(16.dp))
//        ProfileInfoField(label = "Name", value = userData.name)
//        ProfileInfoField(label = "Home Address", value = userData.homeAddress)
//        ProfileInfoField(label = "Role", value = userData.role)
//        ProfileInfoField(label = "Public Address", value = userData.address)
//      }
//    }



@Composable
fun ProfileUI(
  navController: NavHostController,
  userData: UserData
) {
  val backgroundColor = Color(0xFFC7DAF5)

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(backgroundColor)
//      .padding(top = 40.dp),
      .statusBarsPadding(),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    TopAppBarWBackUI(
      title = "Profile",
      navController = navController,
    )

    Box(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
        .padding(16.dp)
        .background(Color.White, shape = RoundedCornerShape(24.dp))
        .padding(vertical = 32.dp, horizontal = 24.dp),
      contentAlignment = Alignment.TopCenter
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.verticalScroll(rememberScrollState())
      ) {
        Image(
          painter = painterResource(id = R.drawable.profile),
          contentDescription = "Profile Picture",
          modifier = Modifier
            .size(150.dp)
            .clip(CircleShape)
        )
        Spacer(modifier = Modifier.height(16.dp))
        ProfileInfoField(label = "Name", value = userData.name, borderColor = backgroundColor)
        ProfileInfoField(label = "Home Address", value = userData.homeAddress, borderColor = backgroundColor)
        ProfileInfoField(label = "Role", value = userData.role, borderColor = backgroundColor)
        ProfileInfoField(label = "Public Address", value = userData.address, borderColor = backgroundColor)
      }
    }
  }
}



//      Column {
//        Text(text = "Name: ${userData.name}")
//        Text(text = "Home Address: ${userData.homeAddress}")
//        Text(text = "Role: ${userData.role}")
//        Text(text = "Public Address: ${userData.address}")
//      }


//@Composable
//fun ProfileInfoField(label: String, value: String) {
//  Column(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
//    Text(text = label, fontSize = 14.sp, color = Color.Gray)
//    OutlinedTextField(
//      value = value,
//      onValueChange = {},
//      readOnly = true,
//      shape = RoundedCornerShape(12.dp),
//      modifier = Modifier.fillMaxWidth()
//    )
//  }
//}

@Composable
fun ProfileInfoField(label: String, value: String, borderColor: Color) {
  Column(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
    Text(text = label, fontSize = 14.sp, color = Color.Gray)
    OutlinedTextField(
      value = value,
      onValueChange = {},
      readOnly = true,
      shape = RoundedCornerShape(12.dp),
      modifier = Modifier.fillMaxWidth(),
      colors = OutlinedTextFieldDefaults.colors(
        unfocusedBorderColor = borderColor,
        focusedBorderColor = borderColor
      )
    )
  }
}
