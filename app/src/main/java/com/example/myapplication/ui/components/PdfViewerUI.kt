package com.example.myapplication.ui.components

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import java.io.File

@Composable
fun PdfViewerUI(file: File) {
  val bitmaps = remember { mutableStateListOf<Bitmap>() }

  LaunchedEffect(file) {
    val pdfRenderer = PdfRenderer(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY))

    for (i in 0 until pdfRenderer.pageCount) {
      val page = pdfRenderer.openPage(i)
      val bmp = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
      page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
      page.close()

      bitmaps.add(bmp) // Langsung update daftar agar UI bereaksi
    }

    pdfRenderer.close()
  }

  Column(modifier = Modifier.fillMaxSize()) {
    LazyColumn(
      modifier = Modifier
        .weight(1f),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      items(bitmaps) { bitmap ->
        Image(
          bitmap = bitmap.asImageBitmap(),
          contentDescription = "PDF Page",
          modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(bitmap.width.toFloat() / bitmap.height.toFloat())
        )
      }
    }
  }
}