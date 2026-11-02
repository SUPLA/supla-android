package org.supla.core.shared.data.model.rest

import kotlin.io.encoding.Base64

data class UserIconDto(
  val id: Int,
  val functionId: Int,
  val images: List<String>,
  val imagesDark: List<String>?
) {
  fun image(index: Int): ByteArray? = images.getOrNull(index)?.let { Base64.decode(it) }
  fun imageDark(index: Int): ByteArray? = imagesDark?.getOrNull(index)?.let { Base64.decode(it) }
}
