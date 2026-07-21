package androidx.compose.material3
/* Copied from androidx.cmopose.material3.NavigationBarItem and adopted for smaller height */

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastFirst
import kotlinx.coroutines.flow.map
import org.supla.android.R
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun RowScope.ResizeableNavigationBarItem(
  selected: Boolean,
  onClick: () -> Unit,
  icon: @Composable () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  label: @Composable (() -> Unit)? = null,
  colors: NavigationBarItemColors = NavigationBarItemDefaults.colors(),
  interactionSource: MutableInteractionSource? = null,
  minHeight: Dp = dimensionResource(R.dimen.bottom_bar_height)
) {
  @Suppress("NAME_SHADOWING")
  val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
  // TODO Load the motionScheme tokens from the component tokens file
  val colorAnimationSpec = spring<Color>(dampingRatio = 1f, stiffness = 1600f)
  val styledIcon =
    @Composable {
      val iconColor by
        animateColorAsState(
          targetValue = colors.iconColor(selected = selected, enabled = enabled),
          animationSpec = colorAnimationSpec,
        )
      // If there's a label, don't have a11y services repeat the icon description.
      val clearSemantics = label != null && (selected)
      Box(modifier = if (clearSemantics) Modifier.clearAndSetSemantics {} else Modifier) {
        CompositionLocalProvider(LocalContentColor provides iconColor, content = icon)
      }
    }

  val styledLabel: @Composable (() -> Unit)? =
    label?.let {
      @Composable {
        val style = MaterialTheme.typography.labelMedium
        val textColor by
          animateColorAsState(
            targetValue = colors.textColor(selected = selected, enabled = enabled),
            animationSpec = colorAnimationSpec,
          )
        ProvideContentColorTextStyle(
          contentColor = textColor,
          textStyle = style,
          content = label,
        )
      }
    }

  var itemWidth by remember { mutableIntStateOf(0) }

  Box(
    modifier
      .selectable(
        selected = selected,
        onClick = onClick,
        enabled = enabled,
        role = Role.Tab,
        interactionSource = interactionSource,
        indication = null,
      )
      .defaultMinSize(minHeight = minHeight)
      .weight(1f)
      .onSizeChanged { itemWidth = it.width },
    contentAlignment = Alignment.Center,
    propagateMinConstraints = true,
  ) {
    // The entire item is selectable, but only the indicator pill shows the ripple. To achieve
    // this, we re-map the coordinates of the item's InteractionSource into the coordinates of
    // the indicator.
    val deltaOffset: Offset
    with(LocalDensity.current) {
      val indicatorWidth = NavigationBarVerticalItemTokens.ActiveIndicatorWidth.roundToPx()
      deltaOffset =
        Offset((itemWidth - indicatorWidth).toFloat() / 2, IndicatorVerticalOffset.toPx())
    }
    val offsetInteractionSource =
      remember(interactionSource, deltaOffset) {
        MappedInteractionSource(interactionSource, deltaOffset)
      }

    // The indicator has a width-expansion animation which interferes with the timing of the
    // ripple, which is why they are separate composables
    val indicatorRipple =
      @Composable {
        Box(
          Modifier
            .layoutId(INDICATOR_RIPPLE_LAYOUT_ID_TAG)
            .clip(CircleShape)
            .indication(offsetInteractionSource, ripple())
        )
      }
    val indicator =
      @Composable {
        Box(
          Modifier
            .layoutId(INDICATOR_LAYOUT_ID_TAG)
            .background(
              color = colors.selectedIndicatorColor,
              shape = CircleShape,
            )
        )
      }

    NavigationBarItemLayout(
      indicatorRipple = indicatorRipple,
      indicator = indicator,
      icon = styledIcon,
      label = styledLabel,
      minHeight = minHeight
    )
  }
}

@Stable
private fun NavigationBarItemColors.iconColor(selected: Boolean, enabled: Boolean): Color =
  when {
    !enabled -> disabledIconColor
    selected -> selectedIconColor
    else -> unselectedIconColor
  }

@Stable
private fun NavigationBarItemColors.textColor(selected: Boolean, enabled: Boolean): Color =
  when {
    !enabled -> disabledTextColor
    selected -> selectedTextColor
    else -> unselectedTextColor
  }

@Composable
private fun ProvideContentColorTextStyle(
  contentColor: Color,
  textStyle: TextStyle,
  content: @Composable () -> Unit,
) {
  val mergedStyle = LocalTextStyle.current.merge(textStyle)
  CompositionLocalProvider(
    LocalContentColor provides contentColor,
    LocalTextStyle provides mergedStyle,
    content = content,
  )
}

private class MappedInteractionSource(
  underlyingInteractionSource: InteractionSource,
  private val delta: Offset,
) : InteractionSource {
  private val mappedPresses = mutableMapOf<PressInteraction.Press, PressInteraction.Press>()

  override val interactions =
    underlyingInteractionSource.interactions.map { interaction ->
      when (interaction) {
        is PressInteraction.Press -> {
          val mappedPress = mapPress(interaction)
          mappedPresses[interaction] = mappedPress
          mappedPress
        }
        is PressInteraction.Cancel -> {
          val mappedPress = mappedPresses.remove(interaction.press)
          if (mappedPress == null) {
            interaction
          } else {
            PressInteraction.Cancel(mappedPress)
          }
        }
        is PressInteraction.Release -> {
          val mappedPress = mappedPresses.remove(interaction.press)
          if (mappedPress == null) {
            interaction
          } else {
            PressInteraction.Release(mappedPress)
          }
        }
        else -> interaction
      }
    }

  private fun mapPress(press: PressInteraction.Press): PressInteraction.Press =
    PressInteraction.Press(press.pressPosition - delta)
}

@Composable
private fun NavigationBarItemLayout(
  indicatorRipple: @Composable () -> Unit,
  indicator: @Composable () -> Unit,
  icon: @Composable () -> Unit,
  label: @Composable (() -> Unit)?,
  minHeight: Dp
) {
  Layout(
    content = {
      indicatorRipple()
      indicator()

      Box(Modifier.layoutId(ICON_LAYOUT_ID_TAG)) { icon() }

      if (label != null) {
        Box(Modifier.layoutId(LABEL_LAYOUT_ID_TAG)) {
          label()
        }
      }
    },
  ) { measurables, constraints ->
    // Ensure that the progress is >= 0. It may be negative on bouncy springs, for example.
    @Suppress("NAME_SHADOWING")
    val looseConstraints = constraints.copy(minWidth = 0, minHeight = 0)
    val iconPlaceable =
      measurables.fastFirst { it.layoutId == ICON_LAYOUT_ID_TAG }.measure(looseConstraints)
    val labelPlaceable =
      label?.let {
        measurables.fastFirst { it.layoutId == LABEL_LAYOUT_ID_TAG }.measure(looseConstraints)
      }

    val itemMaxWidth = max(iconPlaceable.width, (labelPlaceable?.width ?: 0))
    val totalIndicatorWidth = itemMaxWidth + (IndicatorHorizontalPadding * 2).roundToPx()
    val indicatorHeight = iconPlaceable.height + (labelPlaceable?.height ?: 0) + IndicatorVerticalOffset.roundToPx()
    val indicatorRipplePlaceable =
      measurables
        .fastFirst { it.layoutId == INDICATOR_RIPPLE_LAYOUT_ID_TAG }
        .measure(Constraints.fixed(width = totalIndicatorWidth, height = indicatorHeight))

    if (label == null) {
      placeIcon(iconPlaceable, indicatorRipplePlaceable, constraints, minHeight)
    } else {
      placeLabelAndIcon(
        labelPlaceable!!,
        iconPlaceable,
        indicatorRipplePlaceable,
        constraints,
      )
    }
  }
}

private fun MeasureScope.placeIcon(
  iconPlaceable: Placeable,
  indicatorRipplePlaceable: Placeable,
  constraints: Constraints,
  minHeight: Dp
): MeasureResult {
  val width =
    if (constraints.maxWidth == Constraints.Infinity) {
      iconPlaceable.width + NavigationBarItemToIconMinimumPadding.roundToPx() * 2
    } else {
      constraints.maxWidth
    }
  val height = constraints.constrainHeight(minHeight.roundToPx())

  val iconX = (width - iconPlaceable.width) / 2
  val iconY = (height - iconPlaceable.height) / 2

  val rippleX = (width - indicatorRipplePlaceable.width) / 2
  val rippleY = (height - indicatorRipplePlaceable.height) / 2

  return layout(width, height) {
    iconPlaceable.placeRelative(iconX, iconY)
    indicatorRipplePlaceable.placeRelative(rippleX, rippleY)
  }
}

private fun MeasureScope.placeLabelAndIcon(
  labelPlaceable: Placeable,
  iconPlaceable: Placeable,
  indicatorRipplePlaceable: Placeable,
  constraints: Constraints,
): MeasureResult {
  val contentHeight = iconPlaceable.height + labelPlaceable.height
  val contentVerticalPadding = IndicatorVerticalPadding.toPx()
  val height = contentHeight + contentVerticalPadding * 2

  // Icon (when selected) should be `contentVerticalPadding` from top
  val selectedIconY = contentVerticalPadding

  // Label should be fixed padding below icon
  val labelY = selectedIconY + iconPlaceable.height

  val containerWidth =
    if (constraints.maxWidth == Constraints.Infinity) {
      iconPlaceable.width + NavigationBarItemToIconMinimumPadding.roundToPx() * 2
    } else {
      constraints.maxWidth
    }

  val labelX = (containerWidth - labelPlaceable.width) / 2
  val iconX = (containerWidth - iconPlaceable.width) / 2

  val rippleX = (containerWidth - indicatorRipplePlaceable.width) / 2
  val rippleY = selectedIconY - contentVerticalPadding / 2

  return layout(containerWidth, height.roundToInt()) {
    iconPlaceable.placeRelative(iconX, selectedIconY.roundToInt())
    labelPlaceable.placeRelative(labelX, labelY.roundToInt())
    indicatorRipplePlaceable.placeRelative(rippleX, rippleY.roundToInt())
  }
}

private val IndicatorVerticalOffset: Dp = 12.dp
private val NavigationBarItemToIconMinimumPadding: Dp = 44.dp
private const val INDICATOR_RIPPLE_LAYOUT_ID_TAG: String = "indicatorRipple"
private const val INDICATOR_LAYOUT_ID_TAG: String = "indicator"
private const val ICON_LAYOUT_ID_TAG: String = "icon"
private const val LABEL_LAYOUT_ID_TAG: String = "label"

private object NavigationBarVerticalItemTokens {
  val ActiveIndicatorWidth = 56.0.dp
  val IconSize = 24.0.dp
}

private val IndicatorHorizontalPadding: Dp =
  (
    NavigationBarVerticalItemTokens.ActiveIndicatorWidth -
      NavigationBarVerticalItemTokens.IconSize
    ) / 2

private val IndicatorVerticalPadding: Dp = 8.dp
