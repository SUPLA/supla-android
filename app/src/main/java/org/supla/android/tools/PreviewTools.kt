package org.supla.android.tools

import androidx.compose.ui.tooling.preview.Preview

const val BACKGROUND_COLOR = 0x00F5F6F7L

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.FUNCTION)
@Preview(showBackground = true, widthDp = 360, heightDp = 467, backgroundColor = BACKGROUND_COLOR)
annotation class SuplaSmallPreview

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.FUNCTION)
@Preview(showBackground = true, backgroundColor = BACKGROUND_COLOR, showSystemUi = true)
annotation class SuplaPreview

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.FUNCTION)
@Preview(
  name = "Phone - Landscape",
  device = "spec:width=411dp,height=891dp,orientation=landscape,dpi=420",
  showSystemUi = true,
  backgroundColor = BACKGROUND_COLOR
)
annotation class SuplaPreviewLandscape

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.FUNCTION)
@Preview(showBackground = true, backgroundColor = BACKGROUND_COLOR)
annotation class SuplaComponentPreview

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.FUNCTION)
@Preview(
  name = "Square small",
  device = "spec:width=430dp,height=430dp,dpi=240,orientation=portrait",
  showSystemUi = false,
  showBackground = true,
  backgroundColor = BACKGROUND_COLOR
)
@Preview(
  name = "Square medium",
  device = "spec:width=550dp,height=550dp,dpi=240",
  showSystemUi = false,
  showBackground = true,
  backgroundColor = BACKGROUND_COLOR
)
@Preview(
  name = "Square big",
  device = "spec:width=650dp,height=650dp,dpi=240",
  showSystemUi = false,
  showBackground = true,
  backgroundColor = BACKGROUND_COLOR
)
@Preview(
  name = "Portrait small",
  device = "spec:width=300dp,height=430dp,dpi=240",
  showSystemUi = false,
  showBackground = true,
  backgroundColor = BACKGROUND_COLOR
)
@Preview(
  name = "Portrait medium",
  device = "spec:width=410dp,height=550dp,dpi=240",
  showSystemUi = false,
  showBackground = true,
  backgroundColor = BACKGROUND_COLOR
)
@Preview(
  name = "Portrait big",
  device = "spec:width=410dp,height=750dp,dpi=240",
  showSystemUi = false,
  showBackground = true,
  backgroundColor = BACKGROUND_COLOR
)
@Preview(
  name = "Landscape small",
  device = "spec:width=430dp,height=300dp,dpi=240,orientation=landscape",
  showSystemUi = false,
  showBackground = true,
  backgroundColor = BACKGROUND_COLOR
)
@Preview(
  name = "Landscape medium",
  device = "spec:width=550dp,height=410dp,dpi=240,orientation=landscape",
  showSystemUi = false,
  showBackground = true,
  backgroundColor = BACKGROUND_COLOR
)
@Preview(
  name = "Landscape big",
  device = "spec:width=750dp,height=410dp,dpi=240,orientation=landscape",
  showSystemUi = false,
  showBackground = true,
  backgroundColor = BACKGROUND_COLOR
)
annotation class SuplaSizeClassPreview
