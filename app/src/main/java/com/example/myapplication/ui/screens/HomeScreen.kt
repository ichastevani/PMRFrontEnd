package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.R
import com.example.myapplication.network.response.UserData
import com.example.myapplication.ui.PMRScreenEnum
import com.example.myapplication.ui.components.BottomNavigationBarUI
import com.example.myapplication.ui.components.LoadingUI
import com.example.myapplication.ui.components.TopAppBarUI
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.viewModels.MetaMaskViewModel
import com.example.myapplication.ui.viewModels.PMRLoginUiState
import com.example.myapplication.ui.viewModels.PMRViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState

@Composable
fun HomeScreen(
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
    HomeUI(
      navController,
      userData!!
    )
  }else{

    LoadingUI()
  }
}
@OptIn(ExperimentalPagerApi::class)

@Composable
fun HomeUI(
  navController: NavHostController,
  userData: UserData
) {


  Column(
    modifier = Modifier.fillMaxWidth()
  ) {
    TopAppBarUI(stringResource(R.string.app_name), navController)
    Spacer(modifier = Modifier.height(0.dp)) // Add spacer between top app bar and content
    Box (
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth().padding(top = 60.dp)
        .paint( //change
          painterResource(id = R.drawable.foto_dashboard_kedua),
          contentScale = ContentScale.Crop,
        ),
    ) {
      Text(
        text = "Hi, ${userData.name}!",
        fontSize = 26.sp,
        modifier = Modifier
          .padding(16.dp)
      )


    }


    // Bottom Navigation
    BottomNavigationBarUI(navController, userData.role)
  }
}


@Preview(showBackground = true)
@Composable
fun PreviewHomeUIScreen() {
  MyApplicationTheme {
    HomeUI(
      navController = rememberNavController(),
      userData = UserData(
        name = "Doctor 1",
        birthDate = "1990-01-01",
        address = "0x1234567890",
        homeAddress = "",
        role = "Doctor",
      )
    )
  }
}