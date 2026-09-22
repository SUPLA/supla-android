package org.supla.android.usecases.list
/*
 Copyright (C) AC SOFTWARE SP. Z O.O.

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.
 */

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.supla.android.ui.lists.ListItem

class ListItemSectionTest {

  @Test
  fun `should return items from merged location section`() {
    val header = mockk<ListItem.LocationItem>()
    val first = item(11, 1)
    val second = item(22, 2)
    val nextHeader = mockk<ListItem.LocationItem>()
    val outside = item(33, 3)
    val list = listOf(header, first, second, nextHeader, outside)

    val sectionItems = list.findReorderableSectionItems(22) { it as? ListItem.DefaultItem }

    assertThat(sectionItems).containsExactly(first, second)
  }

  @Test
  fun `should distinguish sections with the same caption`() {
    val firstHeader = location("Kitchen")
    val first = item(11, 1)
    val secondHeader = location("Kitchen")
    val second = item(22, 2)
    val list = listOf(firstHeader, first, secondHeader, second)

    val sectionItems = list.findReorderableSectionItems(11) { it as? ListItem.DefaultItem }

    assertThat(sectionItems).containsExactly(first)
  }

  @Test
  fun `should allow moving only items under the same header`() {
    val list = listOf(
      location("Kitchen"),
      item(11, 1),
      item(22, 2),
      location("Kitchen"),
      item(33, 3)
    )

    assertThat(list.canMoveItemWithinSection(1, 2) { it is ListItem.DefaultItem }).isTrue()
    assertThat(list.canMoveItemWithinSection(1, 4) { it is ListItem.DefaultItem }).isFalse()
    assertThat(list.canMoveItemWithinSection(1, 3) { it is ListItem.DefaultItem }).isFalse()
  }

  private fun item(remoteId: Int, locationId: Int): ListItem.DefaultItem = mockk {
    every { this@mockk.remoteId } returns remoteId
    every { this@mockk.locationId } returns locationId
  }

  private fun location(caption: String): ListItem.LocationItem = mockk {
    every { userCaption } returns caption
  }
}
