package com.example.myapplication.ui.viewModels

import android.app.ActivityManager
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import coil.util.CoilUtils.result
import com.example.myapplication.helper.launch
import com.example.myapplication.utils.ConstUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.metamask.androidsdk.EthereumFlow
import io.metamask.androidsdk.EthereumMethod
import io.metamask.androidsdk.EthereumRequest
import io.metamask.androidsdk.Result
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.math.BigInteger
import javax.inject.Inject

data class UiStateMetaMask(
  val ethAddress: String? = null,
  val isConnecting: Boolean = false,
  val isTransactionSuccess: Boolean = false,
  val balance: String? = null,
)

sealed class UiEventMetaMask {
  data class Message(val error: String) : UiEventMetaMask()
}

sealed class EventSinkMetaMask {
  data object Connect : EventSinkMetaMask()
  data class SendRequest(val transactionData: String) : EventSinkMetaMask()
  data object GetBalance : EventSinkMetaMask()
  data object Disconnect : EventSinkMetaMask()
}

@HiltViewModel
class MetaMaskViewModel @Inject constructor(
  @ApplicationContext private val context: Context,
  private val ethereum: EthereumFlow
) : ViewModel() {

  private val _uiEvent = MutableSharedFlow<UiEventMetaMask>()
  val uiEvent = _uiEvent.asSharedFlow()

  private val _uiState = MutableStateFlow(UiStateMetaMask())
  val uiState = _uiState.asStateFlow()

  private fun showMessage(message: String) {
    launch {
      _uiEvent.emit(UiEventMetaMask.Message(message))
    }
  }

  fun eventSink(eventSink: EventSinkMetaMask) {
    launch {
      when (val event = eventSink) {
        EventSinkMetaMask.Connect -> {
          val result = ethereum.connect()
          when (result) {
            is Result.Error -> {
              _uiState.update {
                it.copy(
                  isConnecting = false,
                )
              }
              showMessage(result.error.message)
              Log.e("", "Connection failed: ${result.error.message}")
            }

            is Result.Success -> {
              _uiState.update {
                it.copy(
                  isConnecting = true,
                  ethAddress = ethereum.selectedAddress
                )
              }
              Log.d("MetaMaskViewModel", "Connected successfully!")
            }
          }

          // Bawa aplikasi ke foreground tanpa restart
          val activityManager =
            context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
          activityManager.appTasks.firstOrNull()?.moveToFront()
        }

        is EventSinkMetaMask.SendRequest -> {
          val (transactionData) = event

          when (val result = ethereum.sendRequest(
            EthereumRequest(
              method = EthereumMethod.ETH_SEND_TRANSACTION.value,
              params = listOf(
                mapOf(
                  "to" to ConstUtil.ethContractAddress,
                  "from" to ethereum.selectedAddress,
                  "data" to transactionData,
                )
              )
            )
          )) {
            is Result.Success.Item -> {
              _uiState.update {
                it.copy(isTransactionSuccess = true)
              }
              Log.d("MetaMaskViewModel", result.toString())
            }

            else -> {
              Log.d("MetaMaskViewModel", result.toString())
              _uiState.update {
                it.copy(isTransactionSuccess = false)
              }
            }
          }
        }

        EventSinkMetaMask.GetBalance -> {
          val balanceResult = ethereum.sendRequest(
            EthereumRequest(
              method = EthereumMethod.ETH_GET_BALANCE.value,
              params = listOf(ethereum.selectedAddress, "latest")
            )
          )
          when (balanceResult) {
            is Result.Error -> showMessage(balanceResult.error.message)
            is Result.Success.Item -> {
              val cleanHexString = if (balanceResult.value.startsWith("0x")) {
                balanceResult.value.substring(2)
              } else {
                balanceResult.value
              }
              _uiState.update {
                it.copy(balance = "${BigInteger(cleanHexString, 16)} ETH")
              }
            }

            else -> {
              _uiState.update {
                it.copy(balance = "NA")
              }
            }
          }
        }

        EventSinkMetaMask.Disconnect -> {
          _uiState.update { it.copy(isConnecting = false) }
          ethereum.disconnect(true)
          showMessage("Disconnected!")
        }
      }
    }
  }

  fun updateBalance() {
    if (ethereum.selectedAddress.isNotEmpty()) {
      eventSink(EventSinkMetaMask.GetBalance)
      showMessage("Fetching the wallet balance")
    } else {
      showMessage("The wallet is not connected!")
    }
  }

  fun sendRequest(transactionData: String) {
    if (ethereum.selectedAddress.isNotEmpty()) {
      showMessage("Melakukan Request Transaksi")
      eventSink(EventSinkMetaMask.SendRequest(transactionData))
    } else {
      showMessage("Kamu belum terhubung dengan Meta Mask")
    }
  }

  fun resetIsTransaction() {
    _uiState.update {
      it.copy(isTransactionSuccess = false)
    }
  }
}
