package com.example.myapplication.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.myapplication.R
import com.example.myapplication.helper.OnEvent
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.PMRScreenEnum
import com.example.myapplication.ui.viewModels.EventSinkMetaMask
import com.example.myapplication.ui.viewModels.PMRIsRegisteredUiState
import com.example.myapplication.ui.viewModels.MetaMaskViewModel
import com.example.myapplication.ui.viewModels.PMRViewModel
import com.example.myapplication.ui.viewModels.UiEventMetaMask

@Composable
fun MetaMaskConnectScreen(
  navController: NavHostController,
  metaMaskViewModel: MetaMaskViewModel,
  pmrViewModel: PMRViewModel,
  snackbarHost: SnackbarHostState
) {
  val context = LocalContext.current
  val uiStateMetaMask by metaMaskViewModel.uiState.collectAsState()
  val uiStatePMR by pmrViewModel.uiState.collectAsState()

  LaunchedEffect(key1 = uiStateMetaMask.isConnecting) {
    if (uiStateMetaMask.isConnecting) {
      val ethAddress = uiStateMetaMask.ethAddress
      if (ethAddress != null) {
        pmrViewModel.getIsUserRegistered(ethAddress)
      } else {
        snackbarHost.showSnackbar(
          message = "Gagal terkoneksi dengan MetaMask",
          duration = SnackbarDuration.Long
        )
      }
    }
  }

  LaunchedEffect(key1 = uiStatePMR.isRegistered) {
    val ethAddress = uiStateMetaMask.ethAddress
    if (ethAddress != null) {
      when (uiStatePMR.isRegistered) {
        is PMRIsRegisteredUiState.Loading -> {}
        is PMRIsRegisteredUiState.Error -> {
          snackbarHost.showSnackbar(
            message = "Gagal terhubung dengan server",
            duration = SnackbarDuration.Long
          )
        }

        is PMRIsRegisteredUiState.Success -> {
          val isRegistered =
            (uiStatePMR.isRegistered as PMRIsRegisteredUiState.Success).isRegistered
          Log.d("MetaMaskConnectScreen", "isRegistered: ${isRegistered}")
          if (isRegistered) {
            navController.navigate(PMRScreenEnum.Login.name)
          } else {
            navController.navigate(PMRScreenEnum.Register.name)
          }
        }
      }
    }
  }

  OnEvent(events = metaMaskViewModel.uiEvent) { event ->
    when (event) {
      is UiEventMetaMask.Message -> {
        Toast.makeText(
          context,
          event.error,
          Toast.LENGTH_SHORT
        ).show()
      }
    }
  }

  Surface(
    modifier = Modifier.fillMaxSize(),
    color = MaterialTheme.colorScheme.background
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      // Background image
      Image(
        painter = painterResource(id = R.drawable.background), // Ganti dengan nama file gambar
        contentDescription = "Background Image",
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop // Sesuaikan gambar agar mengisi layar
      )

      // Box putih dengan konten (judul, teks, dan tombol connect)
      Card(
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .fillMaxWidth()
          .height(250.dp)
          .clip(RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White)
      )
      {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          // Judul pertama
          Text(
            text = "Your Health, Your Control",
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center, // Tambahan ini
            modifier = Modifier
              .padding(bottom = 5.dp)
              .fillMaxWidth() // Supaya align center berfungsi
          )


          Spacer(modifier = Modifier.height(12.dp)) // Menambahkan jarak antara kedua teks

          // Judul kedua
          Text(
            text = "Manage your medical history securely with the power of blockchain technology",
            fontSize = 16.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 16.dp)
          )

          // Tombol connect
//          Button(
//            onClick = { metaMaskViewModel.eventSink(EventSinkMetaMask.Connect) },
//            modifier = Modifier
//              .fillMaxWidth() // Memperpanjang panjang button
//              .border(
//                width = 2.dp, // Lebar border
//                color = Color(0xFF2196F3), // Warna biru pada border
//                shape = RoundedCornerShape(12.dp) // Membulatkan sudut
//              )
//              .padding(2.dp), // Memberikan padding di dalam tombol
//            contentPadding = PaddingValues(0.dp), // Menghilangkan padding internal default
//            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent) // Pastikan tidak ada warna latar belakang
//          ) {
//            Text(
//              text = "Connect",
//              color = Color(0xFF2196F3), // Warna teks biru
//              fontSize = 16.sp,
//            )
//          }
          Button(
            onClick = { metaMaskViewModel.eventSink(EventSinkMetaMask.Connect) },
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5A9EFF)) // Warna biru solid
          ) {
            Text(
              text = "Connect",
              color = Color.White,
              fontSize = 16.sp,
              fontWeight = FontWeight.Medium
            )
          }


        }
      }
      // Menambahkan gambar di atas box putih
      Image(
        painter = painterResource(id = R.drawable.image), // Ganti dengan nama file gambar
        contentDescription = "Image above box",
        modifier = Modifier
          .width(350.dp) // Atur lebar gambar sesuai kebutuhan
          .height(350.dp) // Atur tinggi gambar sesuai kebutuhan
          .align(Alignment.TopCenter) // Menempatkan gambar di tengah atas
          .padding(top = 55.dp), // Menambahkan jarak di bagian atas gambar
        contentScale = ContentScale.Crop
      )
    }
  }


}

  @Composable
@Preview(showBackground = true)
private fun LoggedInPreview() {
  MyApplicationTheme {
    MetaMaskConnectScreen(
      navController = NavHostController(LocalContext.current),
      metaMaskViewModel = MetaMaskViewModel(
        context = TODO(),
        ethereum = TODO()
      ),
      pmrViewModel = PMRViewModel(
        pmrRepository = TODO()
      ),
      snackbarHost = SnackbarHostState()
    )
  }
}