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

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuDefaults
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.theme.grey

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TextField(
  value: String,
  modifier: Modifier = Modifier,
  onValueChange: (String) -> Unit = { },
  onClicked: (() -> Unit)? = null,
  keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
  enabled: Boolean = true,
  readOnly: Boolean = false,
  isError: Boolean = false,
  textStyle: TextStyle = MaterialTheme.typography.body1,
  singleLine: Boolean = false,
  label: @Composable (() -> Unit)? = null,
  placeholder: @Composable (() -> Unit)? = null,
  leadingIcon: @Composable (() -> Unit)? = null,
  trailingIcon: @Composable (() -> Unit)? = null,
  contentPadding: PaddingValues =
    if (label == null) {
      TextFieldDefaults.textFieldWithoutLabelPadding()
    } else {
      TextFieldDefaults.textFieldWithLabelPadding()
    }
) {
  val colors = ExposedDropdownMenuDefaults.textFieldColors(
    backgroundColor = MaterialTheme.colors.surface,
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    errorCursorColor = MaterialTheme.colors.error,
    errorIndicatorColor = Color.Transparent,
    trailingIconColor = MaterialTheme.colors.grey,
    focusedTrailingIconColor = MaterialTheme.colors.grey
  )

  val shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_default))
  val interactionSource = remember { MutableInteractionSource() }
  val visualTransformation = VisualTransformation.None
  val textColor = LocalTextStyle.current.color.takeOrElse {
    colors.textColor(enabled).value
  }
  val mergedTextStyle = textStyle.merge(TextStyle(color = textColor))

  if (onClicked != null) {
    LaunchedEffect(interactionSource) {
      interactionSource.interactions.collect {
        if (it is PressInteraction.Press) {
          onClicked()
        }
      }
    }
  }

  @OptIn(ExperimentalMaterialApi::class)
  BasicTextField(
    value = value,
    modifier = modifier
      .background(colors.backgroundColor(enabled).value, shape)
      .border(
        width = 1.dp,
        color = if (isError) MaterialTheme.colors.error else colorResource(id = R.color.gray_light),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_default))
      ),
    onValueChange = onValueChange,
    enabled = enabled,
    readOnly = readOnly,
    textStyle = mergedTextStyle,
    cursorBrush = SolidColor(colors.cursorColor(isError).value),
    visualTransformation = visualTransformation,
    keyboardOptions = keyboardOptions,
    keyboardActions = KeyboardActions(),
    interactionSource = interactionSource,
    singleLine = singleLine,
    maxLines = if (singleLine) 1 else Int.MAX_VALUE,
    minLines = 1,
    decorationBox = @Composable { innerTextField ->
      // places leading icon, text field with label and placeholder, trailing icon
      TextFieldDefaults.TextFieldDecorationBox(
        value = value,
        visualTransformation = visualTransformation,
        innerTextField = innerTextField,
        placeholder = placeholder,
        label = label,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        singleLine = singleLine,
        enabled = enabled,
        isError = isError,
        interactionSource = interactionSource,
        colors = colors,
        contentPadding = contentPadding
      )
    }
  )
}
