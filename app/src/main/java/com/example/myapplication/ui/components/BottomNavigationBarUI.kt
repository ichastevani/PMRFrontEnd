package com.example.myapplication.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.ui.PMRScreenEnum

sealed class MenuBottomNavigationBar(
  val route: String,
  val title: String,
  val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
  object Home : MenuBottomNavigationBar(PMRScreenEnum.Home.name, "Home", Icons.Default.Home)

  // Users
  object HealthRecord :
    MenuBottomNavigationBar(
      PMRScreenEnum.PatientHealthRecords.name,
      "HealthRecords",
      Icons.Default.Receipt
    )

  object PatientAddHealthRecord : MenuBottomNavigationBar(
    PMRScreenEnum.PatientAddHealthRecord.name,
    "AddHealthRecord",
    Icons.Default.Add
  )

  object PatientDoctors : MenuBottomNavigationBar(
    PMRScreenEnum.PatientDoctors.name,
    "PatientDoctors",
    Icons.Default.Groups
  )

  // Doctors
  object DoctorPatients : MenuBottomNavigationBar(
    PMRScreenEnum.DoctorPatients.name,
    "PatientDoctors",
    Icons.Default.Group
  )
}

@Composable
fun BottomNavigationBarUI(navController: NavController, role: String = "Patient") {
  var items: List<MenuBottomNavigationBar>? = null

  if(role == "Doctor"){
    items = listOf(
      MenuBottomNavigationBar.Home,
      MenuBottomNavigationBar.DoctorPatients,
    )
  }else{
    items = listOf(
      MenuBottomNavigationBar.Home,
      MenuBottomNavigationBar.HealthRecord,
      MenuBottomNavigationBar.PatientAddHealthRecord,
      MenuBottomNavigationBar.PatientDoctors
    )
  }

  val currentRoute = navController.currentDestination?.route

  NavigationBar(
    containerColor = Color(0xFFEEF3FC), // Warna latar belakang
    contentColor = Color(0xFF1F323F)
  ) {
    items.forEachIndexed { index, screen ->
      NavigationBarItem(
        selected = currentRoute == screen.route,
        onClick = {
          navController.navigate(screen.route)
        },
        icon = {
          Icon(
            screen.icon,
            contentDescription = screen.title,
            modifier = Modifier.size(28.dp)
          )
        },
        colors = NavigationBarItemDefaults.colors(
          selectedIconColor = Color.White,
          indicatorColor = Color(0xFF1F323F),
          unselectedIconColor = Color(0xFF1F323F),
        )
      )
    }
  }
}