package org.supla.android.features.details.impulsecounter.counterphoto
/*
Copyright (C) AC SOFTWARE SP. Z O.O.

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.ui.views.buttons.OutlinedButton

data class CounterPhotoViewState(
  val imageFile: Any? = null,
  val croppedImageFile: Any? = null
)

@Composable
fun CounterPhotoView(
  state: CounterPhotoViewState,
  onCloudClick: () -> Unit = {}
) {
  Column(
    modifier = Modifier
      .verticalScroll(rememberScrollState())
      .padding(horizontal = Distance.default),
    horizontalAlignment = Alignment.Start,
    verticalArrangement = Arrangement.spacedBy(Distance.default)
  ) {
    state.croppedImageFile?.let {
      ImageWithCaption(R.string.counter_photo_counter_area, it, topPadding = Distance.default)
    }
    state.imageFile?.let {
      ImageWithCaption(R.string.counter_photo_original_photo, it)
    }

    Row {
      Spacer(modifier = Modifier.weight(1f))
      OutlinedButton(
        text = stringResource(R.string.counter_photo_settings),
        onClick = onCloudClick
      )
      Spacer(modifier = Modifier.weight(1f))
    }
  }
}

@Composable
private fun ImageWithCaption(@StringRes title: Int, data: Any, topPadding: Dp = 0.dp) {
  Text(
    text = stringResource(title),
    style = MaterialTheme.typography.titleMedium,
    modifier = Modifier.padding(top = topPadding)
  )
  Image(
    rememberAsyncImagePainter(
      ImageRequest.Builder(LocalPlatformContext.current)
        .data(data)
        .build()
    ),
    contentDescription = null,
    contentScale = ContentScale.FillWidth,
    modifier = Modifier.fillMaxWidth()
  )
}

@Composable
@Preview(showBackground = true)
fun Preview() {
  SuplaTheme {
    CounterPhotoView(
      CounterPhotoViewState(
        R.drawable.on_off_widget_preview_image,
        R.drawable.on_off_widget_preview_image
      )
    )
  }
}
