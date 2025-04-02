package com.example.myapplication.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.myapplication.R
import com.example.myapplication.network.request.RequestAuthLogin
import com.example.myapplication.ui.PMRScreenEnum
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.viewModels.MetaMaskViewModel
import com.example.myapplication.ui.viewModels.PMRLoginUiState
import com.example.myapplication.ui.viewModels.PMRViewModel

@Composable
fun LoginScreen(
  navController: NavHostController,
  metaMaskViewModel: MetaMaskViewModel,
  pmrViewModel: PMRViewModel,
  snackbarHost: SnackbarHostState,
) {
  var isProcessLogin by remember { mutableStateOf(false) }

  val uiStatePMR by pmrViewModel.uiState.collectAsState()
  val usStateMetaMask by metaMaskViewModel.uiState.collectAsState()

  var password by remember { mutableStateOf("") }
  var passwordVisible by remember { mutableStateOf(false) }

  val ethAddress = usStateMetaMask.ethAddress
  if(ethAddress == null){
    navController.navigate(PMRScreenEnum.Welcome.name)
    return
  }

  val onLogin: () -> Unit = {
    pmrViewModel.login(
      RequestAuthLogin(
        ethAddress, password
      )
    )
    isProcessLogin = true
  }

  LaunchedEffect(key1 = uiStatePMR.login) {
    if(!isProcessLogin) return@LaunchedEffect

    when(uiStatePMR.login){
      is PMRLoginUiState.Error -> {
        val errMsg = (uiStatePMR.login as PMRLoginUiState.Error).message
        snackbarHost.showSnackbar(
          message = errMsg,
          duration = SnackbarDuration.Long
        )
        isProcessLogin = false
      }
      is PMRLoginUiState.Success -> {
        navController.navigate(PMRScreenEnum.Home.name)
        isProcessLogin = false
      }
      else -> {}
    }
  }

  if(isProcessLogin){
    LoadingLoginScreen()
  }else{
    LoginUI(
      password,
      onPasswordChange = { password = it },
      passwordVisible,
      onPasswordVisibleChange = {passwordVisible = it},
      onLogin
    )
  }

}

@Composable
fun LoginUI(
  password: String,
  onPasswordChange: (String) -> Unit,
  passwordVisible: Boolean,
  onPasswordVisibleChange: (Boolean) -> Unit,
  onLogin: () -> Unit
){
  Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
    Column(modifier = Modifier.padding(16.dp)) {
      Text(
        text = "Login",
        style = MaterialTheme.typography.titleLarge,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
      )
      Spacer(modifier = Modifier.padding(bottom = 8.dp))
      Divider(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp))

      OutlinedTextField(
        value = password,
        singleLine = true,
        shape = shapes.large,
        modifier = Modifier.fillMaxWidth(),
        colors = TextFieldDefaults.colors(
          focusedContainerColor = colorScheme.surface,
          unfocusedContainerColor = colorScheme.surface,
          disabledContainerColor = colorScheme.surface,
        ),
        onValueChange = onPasswordChange,
        label = { Text("Password") },
        keyboardOptions = KeyboardOptions.Default.copy(
          imeAction = ImeAction.Done
        ),
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
          val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
          val description = if (passwordVisible) "Hide password" else "Show password"

          IconButton(onClick = { onPasswordVisibleChange(!passwordVisible) }) {
            Icon(imageVector = image, contentDescription = description)
          }
        }
      )

      // Tombol
      Spacer(modifier = Modifier.padding(bottom = 16.dp))
      Button(
        onClick = onLogin,
        modifier = Modifier.fillMaxWidth(),
        shape = shapes.large,
        colors = ButtonDefaults.buttonColors(
          containerColor = colorScheme.primary
        )
      ) {
        Text(
          text = "Login",
          style = MaterialTheme.typography.titleMedium,
          color = Color.White,
          modifier = Modifier.padding(4.dp)
        )
      }
    }
  }
}

@Composable
fun LoadingLoginScreen(modifier: Modifier = Modifier) {
  Image(
    modifier = modifier.size(200.dp),
    painter = painterResource(R.drawable.loading_img),
    contentDescription = stringResource(R.string.loading)
  )
}

@Preview(showBackground = true)
@Composable
fun PreviewLoadingLoginScreen() {
  MyApplicationTheme {
    LoadingLoginScreen()
  }
}
