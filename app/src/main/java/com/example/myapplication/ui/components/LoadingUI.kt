package com.example.myapplication.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.myapplication.R

@Composable
fun LoadingUI(modifier: Modifier = Modifier) {
  Image(
    modifier = modifier.size(200.dp),
    painter = painterResource(R.drawable.loading_img),
    contentDescription = stringResource(R.string.loading)
  )
}