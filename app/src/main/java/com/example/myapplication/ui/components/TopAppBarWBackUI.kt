package com.example.myapplication.ui.components

import android.graphics.Color.parseColor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarWBackUI(
  title: String,
  navController: NavHostController,
) {
  TopAppBar(
    title = { Text(text = title) },
    navigationIcon = {
      IconButton(onClick = { navController.navigateUp() }) {
        Icon(
          imageVector = Icons.Default.ArrowBack,
          contentDescription = "Back"
        )
      }
    },
    colors = TopAppBarDefaults.topAppBarColors(
      containerColor = Color(parseColor("#EEF3FC")), // Warna Hex
      titleContentColor = Color(parseColor("#1F323F"))
    ),
  )
}