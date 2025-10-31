package org.supla.android.tools

import androidx.compose.ui.tooling.preview.Preview

const val BACKGROUND_COLOR = 0x00F5F6F7L

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.FUNCTION)
@Preview(showBackground = true, backgroundColor = BACKGROUND_COLOR)
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
annotation class ComponentPreview
