package org.supla.android.ui.views
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

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import org.supla.android.core.shared.invoke
import org.supla.core.shared.infrastructure.LocalizedString

@Composable
fun HeadlineSmall(stringRes: Int, color: Color = MaterialTheme.colorScheme.onSurface) =
  Text(
    text = stringResource(id = stringRes),
    style = MaterialTheme.typography.headlineSmall,
    color = color
  )

@Composable
fun BodyLarge(stringRes: Int, color: Color = MaterialTheme.colorScheme.onSurface, textAlign: TextAlign = TextAlign.Center) =
  Text(
    text = stringResource(id = stringRes),
    style = MaterialTheme.typography.bodyLarge,
    color = color,
    textAlign = textAlign
  )

@Composable
fun BodyLarge(string: String, color: Color = MaterialTheme.colorScheme.onSurface, textAlign: TextAlign = TextAlign.Center) =
  Text(
    text = string,
    style = MaterialTheme.typography.bodyLarge,
    color = color,
    textAlign = textAlign
  )

@Composable
fun BodyMedium(stringRes: Int, color: Color = MaterialTheme.colorScheme.onSurface, textAlign: TextAlign = TextAlign.Center) =
  BodyMedium(
    text = stringResource(id = stringRes),
    color = color,
    textAlign = textAlign
  )

@Composable
fun BodyMedium(
  localizedString: LocalizedString,
  color: Color = MaterialTheme.colorScheme.onSurface,
  textAlign: TextAlign = TextAlign.Center
) =
  BodyMedium(
    text = localizedString(LocalContext.current),
    color = color,
    textAlign = textAlign
  )

@Composable
fun BodyMedium(
  text: String,
  color: Color = MaterialTheme.colorScheme.onSurface,
  textAlign: TextAlign = TextAlign.Center
) =
  Text(
    text = text,
    style = MaterialTheme.typography.bodyMedium,
    color = color,
    textAlign = textAlign
  )

@Composable
fun BodySmall(stringRes: Int, color: Color = MaterialTheme.colorScheme.onSurface, textAlign: TextAlign = TextAlign.Center) =
  Text(
    text = stringResource(id = stringRes),
    style = MaterialTheme.typography.bodySmall,
    color = color,
    textAlign = textAlign
  )
