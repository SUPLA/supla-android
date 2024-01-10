package org.supla.android.core.networking.esp
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

import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import org.supla.android.Trace
import org.supla.android.extensions.TAG
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

private const val STATE_PATTERN = "^LAST\\ STATE:\\ (.*)\\<br\\>Firmware:\\ (.*)\\<br\\>GUID:\\ (.*)\\<br\\>MAC:\\ (.*)$"

private const val INPUT_NAME_NO_CHANNELS = "no_visible_channels"
private const val INPUT_VALUE_NO_CHANNELS = "1"

@Singleton
class EspHtmlParser @Inject constructor() {

  fun findInputs(doc: Document): Map<String, String> {
    val map = mutableMapOf<String, String>()

    val inputs: Elements = doc.getElementsByTag("input")

    for (element in inputs) {
      var appendToList = true
      if (element.attr("type").equals("checkbox", ignoreCase = true)) {
        // skip not checked checkboxes
        appendToList = element.hasAttr("checked")
      }

      val name = element.attr("name")
      if (name.trim().isEmpty()) {
        Trace.w(TAG, "Skipping input with empty name: `${element.html()}`")
      } else if (appendToList) {
        map[name] = element.`val`()
      }
    }

    val sel: Elements = doc.getElementsByTag("select")
    for (element in sel) {
      val option = element.select("option[selected]")
      val name = element.attr("name")

      if (name.trim().isEmpty()) {
        Trace.w(TAG, "Skipping select with empty name: `${element.html()}`")
      } else if (option != null && option.hasAttr("selected")) {
        map[name] = option.`val`()
      }
    }

    return map
  }

  fun prepareResult(doc: Document, fieldMap: Map<String, String>): EspConfigResult {
    val result = EspConfigResult()

    result.needsCloudConfig = fieldMap[INPUT_NAME_NO_CHANNELS]?.let { it == INPUT_VALUE_NO_CHANNELS } ?: false

    doc.getElementsByTag("h1")?.also {
      it.next()?.also { elements ->
        for (element in elements) {
          if (element.html().contains("LAST STATE").not()) {
            continue
          }

          val pattern = Pattern.compile(STATE_PATTERN)
          val matcher = pattern.matcher(element.html())
          if (matcher.find() && matcher.groupCount() == 4) {
            result.deviceName = it.html()
            result.deviceLastState = matcher.group(1)
            result.deviceFirmwareVersion = matcher.group(2)
            result.deviceGUID = matcher.group(3)
            result.deviceMAC = matcher.group(4)
          }
          break
        }
      }
    }

    return result
  }
}
