package com.example.myapplication.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
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
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
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
import com.example.myapplication.network.request.RequestAuthRegister
import com.example.myapplication.ui.PMRScreenEnum
import com.example.myapplication.ui.components.DatePickerModal
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.viewModels.MetaMaskViewModel
import com.example.myapplication.ui.viewModels.PMRTransactionUiState
import com.example.myapplication.ui.viewModels.PMRViewModel
import com.example.myapplication.utils.ToolsUtil
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RegisterScreen(
  navController: NavHostController,
  metaMaskViewModel: MetaMaskViewModel,
  pmrViewModel: PMRViewModel,
  snackbarHost: SnackbarHostState,
) {
  var isProcessRegistered by remember { mutableStateOf(false) }
  val uiStatePMR by pmrViewModel.uiState.collectAsState()
  val uiStateMetaMask by metaMaskViewModel.uiState.collectAsState()

  val ethAddress = uiStateMetaMask.ethAddress
  if(ethAddress == null){
    navController.navigate(PMRScreenEnum.Welcome.name)
    return
  }

  var name by remember { mutableStateOf("") }
  var birthDate by remember { mutableStateOf("") }
  var homeAddress by remember { mutableStateOf("") }
  var role by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var passwordVisible by remember { mutableStateOf(false) }

  val onRegistered: () -> Unit = {
    pmrViewModel.register(
      RequestAuthRegister(
        ethAddress, name, birthDate, homeAddress, role, password
      )
    )
    isProcessRegistered = true
  }

  LaunchedEffect(key1 = uiStatePMR.transaction) {
    if(!isProcessRegistered) return@LaunchedEffect

    when (uiStatePMR.transaction) {
      is PMRTransactionUiState.Error -> {
        val errMsg = (uiStatePMR.transaction as PMRTransactionUiState.Error).message
        snackbarHost.showSnackbar(
          message = errMsg,
          duration = SnackbarDuration.Long
        )
        isProcessRegistered = false
      }

      is PMRTransactionUiState.Success -> {
        val transactionData =
          (uiStatePMR.transaction as PMRTransactionUiState.Success).transaction
        if (transactionData == null) {
          snackbarHost.showSnackbar(
            message = "Gagal melakukan pendaftaran",
            duration = SnackbarDuration.Long
          )
        } else {
          metaMaskViewModel.sendRequest(transactionData.toString())
        }
        isProcessRegistered = false
      }

      else -> {}
    }
  }

  LaunchedEffect(key1 = uiStateMetaMask.isTransactionSuccess) {
    if(uiStateMetaMask.isTransactionSuccess){
      navController.navigate(PMRScreenEnum.Login.name)
      metaMaskViewModel.resetIsTransaction()
    }
  }

  if (isProcessRegistered) {
    LoadingRegisterScreen()
  } else {
    RegisterUI(
      name,
      onNameChange = { name = it },
      onBirthDateChange = { birthDate = it },
      homeAddress,
      onHomeAddressChange = { homeAddress = it },
      onRoleChange = { role = it },
      password,
      onPasswordChange = { password = it },
      passwordVisible,
      onPasswordVisibleChange = { passwordVisible = it },
      onRegistered
    )
  }
}

@Composable
fun RegisterUI(
  name: String,
  onNameChange: (String) -> Unit,
  onBirthDateChange: (String) -> Unit,
  homeAddress: String,
  onHomeAddressChange: (String) -> Unit,
  onRoleChange: (String) -> Unit,
  password: String,
  onPasswordChange: (String) -> Unit,
  passwordVisible: Boolean,
  onPasswordVisibleChange: (Boolean) -> Unit,
  onRegistered: () -> Unit
) {
  val context = LocalContext.current
  // Untuk memilih birth date
  var selectedDate by remember { mutableStateOf<Long?>(null) }
  var showModalDate by remember { mutableStateOf(false) }
  // untuk memilih role
  var selectedRole by remember { mutableStateOf("") }

  Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
    Column(modifier = Modifier.padding(16.dp)) {
      Text(
        text = "Register",
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
      OutlinedTextField(
        value = name,
        singleLine = true,
        shape = shapes.large,
        modifier = Modifier.fillMaxWidth(),
        colors = TextFieldDefaults.colors(
          focusedContainerColor = colorScheme.surface,
          unfocusedContainerColor = colorScheme.surface,
          disabledContainerColor = colorScheme.surface,
        ),
        onValueChange = onNameChange,
        label = { Text("Name") },
        keyboardOptions = KeyboardOptions.Default.copy(
          imeAction = ImeAction.Done
        ),
      )

      // Input Tanggal Lahir (Menggunakan DatePicker)
      Spacer(modifier = Modifier.padding(bottom = 8.dp))
      OutlinedTextField(
        value = selectedDate?.let { ToolsUtil.convertMillisToDate(it) } ?: "",
        onValueChange = {},
        shape = shapes.large,
        label = { Text("Birth Date") },
        placeholder = { Text("MM/DD/YYYY") },
        trailingIcon = {
          Icon(Icons.Default.DateRange, contentDescription = "Select date")
        },
        colors = TextFieldDefaults.colors(
          focusedContainerColor = colorScheme.surface,
          unfocusedContainerColor = colorScheme.surface,
          disabledContainerColor = colorScheme.surface,
        ),
        modifier = Modifier
          .fillMaxWidth()
          .pointerInput(selectedDate) {
            awaitEachGesture {
              awaitFirstDown(pass = PointerEventPass.Initial)
              val upEvent = waitForUpOrCancellation(pass = PointerEventPass.Initial)
              if (upEvent != null) {
                showModalDate = true
              }
            }
          }
      )
      if (showModalDate) {
        DatePickerModal(
          onDateSelected = {
            selectedDate = it
            if(it != null){
              val formattedDate = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
                .format(Date(it)) // Format ke YYYY/MM/DD
              onBirthDateChange(formattedDate)
            }
          },
          onDismiss = { showModalDate = false }
        )
      }

      // Select Role
      // Radio Button untuk memilih role
      Spacer(modifier = Modifier.height(16.dp))
      Text(
        text = "Select Role",
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(bottom = 8.dp)
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        RadioButton(
          selected = selectedRole == "Patient",
          onClick = {
            selectedRole = "Patient"
            onRoleChange("Patient")
          }
        )
        Text(
          text = "Patient",
          modifier = Modifier.padding(start = 4.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        RadioButton(
          selected = selectedRole == "Doctor",
          onClick = {
            selectedRole = "Doctor"
            onRoleChange("Doctor")
          }
        )
        Text(
          text = "Doctor",
          modifier = Modifier.padding(start = 4.dp)
        )
      }

      // Input address
      Spacer(modifier = Modifier.padding(bottom = 8.dp))
      OutlinedTextField(
        value = homeAddress,
        singleLine = false,
        shape = shapes.large,
        modifier = Modifier
          .fillMaxWidth()
          .height(120.dp),
        colors = TextFieldDefaults.colors(
          focusedContainerColor = colorScheme.surface,
          unfocusedContainerColor = colorScheme.surface,
          disabledContainerColor = colorScheme.surface,
        ),
        onValueChange = onHomeAddressChange,
        label = { Text("Home Address") },
        keyboardOptions = KeyboardOptions.Default.copy(
          imeAction = ImeAction.Done
        ),
      )

      // Input password
      Spacer(modifier = Modifier.padding(bottom = 8.dp))
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
          val image =
            if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
          val description = if (passwordVisible) "Hide password" else "Show password"

          IconButton(onClick = { onPasswordVisibleChange(!passwordVisible) }) {
            Icon(imageVector = image, contentDescription = description)
          }
        }
      )

      // Tombol
      Spacer(modifier = Modifier.padding(bottom = 16.dp))
      Button(
        onClick = onRegistered,
        modifier = Modifier.fillMaxWidth(),
        shape = shapes.large,
        colors = ButtonDefaults.buttonColors(
          containerColor = colorScheme.primary
        )
      ) {
        Text(
          text = "Register",
          style = MaterialTheme.typography.titleMedium,
          color = Color.White,
          modifier = Modifier.padding(4.dp)
        )
      }

    }
  }
}

@Composable
fun LoadingRegisterScreen(modifier: Modifier = Modifier) {
  Image(
    modifier = modifier.size(200.dp),
    painter = painterResource(R.drawable.loading_img),
    contentDescription = stringResource(R.string.loading)
  )
}

@Preview(showBackground = true)
@Composable
fun PreviewLoadingRegisterScreen() {
  MyApplicationTheme {
    LoadingRegisterScreen()
  }
}
