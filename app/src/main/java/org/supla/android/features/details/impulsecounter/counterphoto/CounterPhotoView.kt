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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NoPhotography
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import org.supla.android.R
import org.supla.android.core.shared.invoke
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.ui.views.buttons.BlueTextButton
import org.supla.android.ui.views.buttons.OutlinedButton
import org.supla.android.usecases.ocr.OcrPhoto
import org.supla.core.shared.infrastructure.LocalizedString

data class CounterPhotoViewState(
  val refreshing: Boolean = false,
  val latestPhoto: OcrPhoto? = null,
  val photos: List<OcrPhoto> = emptyList(),

  val loadingError: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CounterPhotoView(
  state: CounterPhotoViewState,
  onCloudClick: () -> Unit = {},
  onRefresh: () -> Unit = {}
) {
  val pullToRefreshState = rememberPullToRefreshState()

  Box(
    modifier = Modifier.pullToRefresh(
      isRefreshing = state.refreshing,
      onRefresh = onRefresh,
      state = pullToRefreshState,
      threshold = 58.dp
    )
  ) {
    if (state.loadingError) {
      ErrorContent(
        onCloudClick = onCloudClick,
        onTryAgainClick = onRefresh
      )
    } else if (!state.refreshing) {
      Content(
        latestPhoto = state.latestPhoto,
        photos = state.photos,
        onCloudClick = onCloudClick
      )
    }

    PullToRefreshDefaults.Indicator(
      state = pullToRefreshState,
      isRefreshing = state.refreshing,
      modifier = Modifier.align(Alignment.TopCenter),
      color = MaterialTheme.colorScheme.primary,
      maxDistance = 58.dp
    )
  }
}

@Composable
private fun ErrorContent(onCloudClick: () -> Unit, onTryAgainClick: () -> Unit) {
  Box(
    modifier = Modifier.fillMaxSize(),
  ) {
    Column(
      verticalArrangement = Arrangement.spacedBy(Distance.default),
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.align(Alignment.Center)
    ) {
      Image(
        painter = painterResource(id = R.drawable.ic_status_error),
        contentDescription = null
      )
      Text(stringResource(R.string.counter_photo_loading_error), style = MaterialTheme.typography.bodyLarge)
      BlueTextButton(
        text = stringResource(id = R.string.status_try_again),
        onClick = onTryAgainClick
      )
      OutlinedButton(
        text = stringResource(R.string.counter_photo_settings),
        onClick = onCloudClick
      )
    }
  }
}

@Composable
private fun Content(latestPhoto: OcrPhoto?, photos: List<OcrPhoto>, onCloudClick: () -> Unit) {
  Column(
    modifier = Modifier
      .verticalScroll(rememberScrollState()),
    horizontalAlignment = Alignment.Start,
    verticalArrangement = Arrangement.spacedBy(Distance.default)
  ) {
    if (latestPhoto?.cropped != null) {
      ImageWithCaption(R.string.counter_photo_counter_area, latestPhoto.cropped, topPadding = Distance.default)
    } else {
      NoImageWithCaption(R.string.counter_photo_counter_area, topPadding = Distance.default)
    }

    if (latestPhoto?.original != null) {
      ImageWithCaption(R.string.counter_photo_original_photo, latestPhoto.original)
    } else {
      NoImageWithCaption(R.string.counter_photo_original_photo)
    }

    latestPhoto?.date?.let {
      Row {
        Spacer(modifier = Modifier.weight(1f))
        Text(text = it, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.weight(1f))
      }
    }

    if (photos.isNotEmpty()) {
      Text(
        text = stringResource(R.string.counter_photo_photos),
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(top = Distance.default, start = Distance.default, end = Distance.default)
      )
      Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
        photos.forEach { PhotoRow(it) }
      }
    }

    Row(modifier = Modifier.padding(bottom = Distance.default)) {
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
    modifier = Modifier.padding(top = topPadding, start = Distance.default, end = Distance.default)
  )
  Image(
    rememberAsyncImagePainter(
      ImageRequest.Builder(LocalPlatformContext.current)
        .data(data)
        .build()
    ),
    contentDescription = null,
    contentScale = ContentScale.FillWidth,
    modifier = Modifier
      .fillMaxWidth()
      .padding(start = Distance.default, end = Distance.default)
  )
}

@Composable
private fun NoImageWithCaption(@StringRes title: Int, topPadding: Dp = 0.dp) {
  Text(
    text = stringResource(title),
    style = MaterialTheme.typography.titleMedium,
    modifier = Modifier.padding(top = topPadding, start = Distance.default, end = Distance.default)
  )
  Row {
    Spacer(modifier = Modifier.weight(1f))
    Image(
      Icons.Outlined.NoPhotography,
      contentDescription = null,
      modifier = Modifier.size(64.dp),
      colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.outline)
    )
    Spacer(modifier = Modifier.weight(1f))
  }
}

@Composable
private fun PhotoRow(photo: OcrPhoto) =
  Row(
    horizontalArrangement = Arrangement.spacedBy(Distance.small),
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .background(MaterialTheme.colorScheme.surface)
      .padding(horizontal = Distance.default, vertical = Distance.tiny)
  ) {
    Text(photo.date, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(0.4f))
    Image(
      rememberAsyncImagePainter(
        ImageRequest.Builder(LocalPlatformContext.current)
          .data(photo.cropped)
          .build()
      ),
      contentDescription = null,
      modifier = Modifier.weight(0.3f)
    )
    Box(modifier = Modifier.weight(0.3f)) {
      CounterParsedValue(photo.value)
    }
  }

@Composable
context(BoxScope)
private fun CounterParsedValue(value: OcrPhoto.Value) =
  Text(
    value.value(LocalContext.current),
    style = MaterialTheme.typography.bodyMedium,
    modifier = Modifier
      .background(colorResource(value.backgroundColor), RoundedCornerShape(dimensionResource(R.dimen.radius_small)))
      .padding(horizontal = 6.dp, vertical = 4.dp)
      .align(Alignment.Center),
    textAlign = TextAlign.Center,
    color = colorResource(value.textColor)
  )

@Composable
@Preview(showBackground = true)
fun Preview() {
  SuplaTheme {
    CounterPhotoView(
      CounterPhotoViewState(
        refreshing = false,
        latestPhoto = OcrPhoto(
          date = "12.12.2024 12:12",
          original = null,
          cropped = R.drawable.on_off_widget_preview_image,
          OcrPhoto.Value.Warning(LocalizedString.Constant("12345"))
        ),
        photos = listOf(
          OcrPhoto(
            date = "12.12.2024 12:12",
            original = null,
            cropped = R.drawable.on_off_widget_preview_image,
            OcrPhoto.Value.Success(LocalizedString.Constant("12345"))
          ),
          OcrPhoto(date = "12.12.2024 12:12", original = null, cropped = R.drawable.on_off_widget_preview_image, OcrPhoto.Value.Error)
        )
      )
    )
  }
}

@Composable
@Preview(showBackground = true)
fun PreviewError() {
  SuplaTheme {
    CounterPhotoView(
      CounterPhotoViewState(
        loadingError = true
      )
    )
  }
}
