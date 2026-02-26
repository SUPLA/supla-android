package org.supla.android.ui.views.texts
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
fun LabelLarge(@StringRes stringRes: Int, color: Color = MaterialTheme.colorScheme.onSurface, textAlign: TextAlign = TextAlign.Start) =
  LabelLarge(text = stringResource(id = stringRes), color = color, textAlign = textAlign)

@Composable
fun LabelLarge(text: String, color: Color = MaterialTheme.colorScheme.onSurface, textAlign: TextAlign = TextAlign.Start) =
  Text(
    text = text,
    style = MaterialTheme.typography.labelLarge,
    color = color,
    textAlign = textAlign
  )

@Composable
fun TitleLarge(
  @StringRes stringRes: Int,
  modifier: Modifier = Modifier,
  color: Color = MaterialTheme.colorScheme.onSurface,
  textAlign: TextAlign = TextAlign.Start,
  overflow: TextOverflow = TextOverflow.Clip,
  maxLines: Int = Int.MAX_VALUE
) =
  TitleLarge(
    text = stringResource(id = stringRes),
    modifier = modifier,
    color = color,
    textAlign = textAlign,
    overflow = overflow,
    maxLines = maxLines,
  )

@Composable
fun TitleLarge(
  text: String,
  modifier: Modifier = Modifier,
  color: Color = MaterialTheme.colorScheme.onSurface,
  textAlign: TextAlign = TextAlign.Start,
  overflow: TextOverflow = TextOverflow.Clip,
  maxLines: Int = Int.MAX_VALUE
) =
  Text(
    text = text,
    modifier = modifier,
    style = MaterialTheme.typography.titleLarge,
    color = color,
    textAlign = textAlign,
    overflow = overflow,
    maxLines = maxLines
  )

@Composable
fun TitleMedium(@StringRes stringRes: Int, color: Color = MaterialTheme.colorScheme.onSurface, textAlign: TextAlign = TextAlign.Start) =
  TitleMedium(text = stringResource(id = stringRes), color = color, textAlign = textAlign)

@Composable
fun TitleMedium(text: String, color: Color = MaterialTheme.colorScheme.onSurface, textAlign: TextAlign = TextAlign.Start) =
  Text(
    text = text,
    style = MaterialTheme.typography.titleMedium,
    color = color,
    textAlign = textAlign
  )

@Composable
fun TitleSmall(@StringRes stringRes: Int, color: Color = MaterialTheme.colorScheme.onSurface, textAlign: TextAlign = TextAlign.Start) =
  TitleSmall(text = stringResource(id = stringRes), color = color, textAlign = textAlign)

@Composable
fun TitleSmall(text: String, color: Color = MaterialTheme.colorScheme.onSurface, textAlign: TextAlign = TextAlign.Start) =
  Text(
    text = text,
    style = MaterialTheme.typography.titleSmall,
    color = color,
    textAlign = textAlign
  )

@Composable
fun BodyLarge(
  stringRes: Int,
  color: Color = MaterialTheme.colorScheme.onSurface,
  textAlign: TextAlign = TextAlign.Center,
  overflow: TextOverflow = TextOverflow.Clip,
  maxLines: Int = Int.MAX_VALUE
) =
  BodyLarge(
    text = stringResource(id = stringRes),
    color = color,
    textAlign = textAlign,
    overflow = overflow,
    maxLines = maxLines
  )

@Composable
fun BodyLarge(
  text: String,
  modifier: Modifier = Modifier,
  color: Color = MaterialTheme.colorScheme.onSurface,
  textAlign: TextAlign = TextAlign.Center,
  overflow: TextOverflow = TextOverflow.Clip,
  maxLines: Int = Int.MAX_VALUE
) =
  Text(
    text = text,
    modifier = modifier,
    style = MaterialTheme.typography.bodyLarge,
    color = color,
    textAlign = textAlign,
    overflow = overflow,
    maxLines = maxLines
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
  modifier: Modifier = Modifier,
  color: Color = MaterialTheme.colorScheme.onSurface,
  textAlign: TextAlign = TextAlign.Center
) =
  Text(
    text = text,
    style = MaterialTheme.typography.bodyMedium,
    color = color,
    textAlign = textAlign,
    modifier = modifier
  )

@Composable
fun BodySmall(stringRes: Int, color: Color = MaterialTheme.colorScheme.onSurface, textAlign: TextAlign = TextAlign.Center) =
  Text(
    text = stringResource(id = stringRes),
    style = MaterialTheme.typography.bodySmall,
    color = color,
    textAlign = textAlign
  )
