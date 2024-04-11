package org.supla.android.core.infrastructure

import android.net.Uri
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UriProxy @Inject constructor() {

  fun toUri(url: String): Uri {
    return Uri.parse(url)
  }
}
