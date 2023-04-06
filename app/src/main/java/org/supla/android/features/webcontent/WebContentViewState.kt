package org.supla.android.features.webcontent

import org.supla.android.core.ui.ViewState


data class WebContentViewState(override val loading: Boolean = false) : ViewState(loading) {
}