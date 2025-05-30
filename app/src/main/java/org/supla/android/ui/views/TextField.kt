@file:OptIn(ExperimentalMaterial3Api::class)

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
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.core.ui.theme.gray

@Composable
fun TextField(
  value: String,
  modifier: Modifier = Modifier,
  onValueChange: (String) -> Unit = { },
  onClicked: (() -> Unit)? = null,
  keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
  keyboardActions: KeyboardActions = KeyboardActions.Default,
  visualTransformation: VisualTransformation = VisualTransformation.None,
  enabled: Boolean = true,
  readOnly: Boolean = false,
  isError: Boolean = false,
  textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
  singleLine: Boolean = false,
  label: @Composable (() -> Unit)? = null,
  placeholder: @Composable (() -> Unit)? = null,
  leadingIcon: @Composable (() -> Unit)? = null,
  trailingIcon: @Composable (() -> Unit)? = null,
  prefix: @Composable (() -> Unit)? = null,
  suffix: @Composable (() -> Unit)? = null,
  focusedColor: Color? = null,
  contentPadding: PaddingValues =
    if (label == null) {
      TextFieldDefaults.contentPaddingWithoutLabel()
    } else {
      TextFieldDefaults.contentPaddingWithLabel()
    }
) {
  val colors = TextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    disabledContainerColor = MaterialTheme.colorScheme.surface,
    focusedTextColor = MaterialTheme.colorScheme.onBackground,
    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
    disabledTextColor = MaterialTheme.colorScheme.outline,
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    disabledIndicatorColor = Color.Transparent,
    errorCursorColor = MaterialTheme.colorScheme.error,
    errorIndicatorColor = Color.Transparent,
    unfocusedTrailingIconColor = MaterialTheme.colorScheme.gray,
    focusedTrailingIconColor = MaterialTheme.colorScheme.gray
  )
  val cursorColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground

  val shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_default))
  val interactionSource = remember { MutableInteractionSource() }
  val isFocused by interactionSource.collectIsFocusedAsState()

  if (onClicked != null) {
    LaunchedEffect(interactionSource) {
      interactionSource.interactions.collect {
        if (it is PressInteraction.Press) {
          onClicked()
        }
      }
    }
  }

  val borderColor = when {
    isError -> MaterialTheme.colorScheme.error
    isFocused && focusedColor != null -> focusedColor
    else -> colorResource(id = R.color.gray_lighter)
  }
  val textColor = when {
    enabled -> MaterialTheme.colorScheme.onBackground
    else -> MaterialTheme.colorScheme.outline
  }

  BasicTextField(
    value = value,
    modifier = modifier
      .background(MaterialTheme.colorScheme.surface, shape)
      .border(
        width = 1.dp,
        color = borderColor,
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_default))
      ),
    onValueChange = onValueChange,
    enabled = enabled,
    readOnly = readOnly,
    textStyle = textStyle.merge(TextStyle(color = textColor)),
    cursorBrush = SolidColor(cursorColor),
    visualTransformation = visualTransformation,
    keyboardOptions = keyboardOptions,
    keyboardActions = keyboardActions,
    interactionSource = interactionSource,
    singleLine = singleLine,
    maxLines = if (singleLine) 1 else Int.MAX_VALUE,
    minLines = 1,
    decorationBox = @Composable { innerTextField ->
      // places leading icon, text field with label and placeholder, trailing icon
      TextFieldDefaults.DecorationBox(
        value = value,
        visualTransformation = visualTransformation,
        innerTextField = innerTextField,
        placeholder = placeholder,
        label = label,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        singleLine = singleLine,
        prefix = prefix,
        suffix = suffix,
        enabled = enabled,
        isError = isError,
        interactionSource = interactionSource,
        shape = shape,
        colors = colors,
        contentPadding = contentPadding
      )
    }
  )
}

@Preview
@Composable
private fun Preview() {
  SuplaTheme {
    Column(verticalArrangement = Arrangement.spacedBy(Distance.default)) {
      TextField("Without label")
      TextField("With label", label = { Text("Label") })
      TextField("Disabled", enabled = false)
    }
  }
}
