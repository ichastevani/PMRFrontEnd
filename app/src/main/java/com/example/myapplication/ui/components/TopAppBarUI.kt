package com.example.myapplication.ui.components

import android.graphics.Color.parseColor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import com.example.myapplication.ui.PMRScreenEnum

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarUI(title: String, navController: NavHostController) {
  var expanded by remember { mutableStateOf(false) }
  TopAppBar(
    title = { Text(title) },
    colors = TopAppBarDefaults.topAppBarColors(
      containerColor = Color(parseColor("#EEF3FC")), // Warna Hex
      titleContentColor = Color(parseColor("#1F323F"))
    ),
    actions = {
      IconButton(onClick = { expanded = true }) {
        Icon(
          imageVector = Icons.Default.MoreVert,
          contentDescription = "Account",
        )
      }
      DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
      ) {
        DropdownMenuItem(
          text = { Text("Profile") },
          onClick = {
            navController.navigate(PMRScreenEnum.Profile.name)
            expanded = false
          }
        )
        DropdownMenuItem(
          text = { Text("Logout") },
          onClick = {
            navController.navigate(PMRScreenEnum.Logout.name)
            expanded = false
          }
        )
      }
    }
  )
}