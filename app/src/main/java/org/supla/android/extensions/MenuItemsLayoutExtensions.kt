package org.supla.android.extensions
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

import android.animation.Animator
import android.view.ViewPropertyAnimator
import org.supla.android.MenuItemsLayout

enum class MenuItemsAnimationType {
  TRANSLATION, FADE_IN_OUT
}

fun MenuItemsLayout.hide(
  position: Float,
  animationType: MenuItemsAnimationType,
  onEndCallback: () -> Unit
): ViewPropertyAnimator =
  when (animationType) {
    MenuItemsAnimationType.TRANSLATION ->
      animate()
        .translationY(position)
        .setDuration(200)
        .setListener(provideListener(onEndCallback))

    MenuItemsAnimationType.FADE_IN_OUT -> {
      animate()
        .alpha(0f)
        .setDuration(200)
        .setListener(object : DefaultAnimatorListener() {
          override fun onAnimationEnd(p0: Animator) {
            translationY = -position
            alpha = 1f
            onEndCallback()
          }

          override fun onAnimationCancel(p0: Animator) {
            translationY = -position
            alpha = 1f
            onEndCallback()
          }
        })
    }
  }

fun MenuItemsLayout.show(
  animationType: MenuItemsAnimationType,
  onEndCallback: () -> Unit
): ViewPropertyAnimator =
  when (animationType) {
    MenuItemsAnimationType.TRANSLATION ->
      animate()
        .translationY(0f)
        .setDuration(200)
        .setListener(provideListener(onEndCallback))

    MenuItemsAnimationType.FADE_IN_OUT -> {
      alpha = 0f
      translationY = 0f
      animate()
        .alpha(1f)
        .setDuration(200)
        .setListener(provideListener(onEndCallback))
    }
  }

private fun provideListener(endCallback: () -> Unit): Animator.AnimatorListener =
  object : DefaultAnimatorListener() {
    override fun onAnimationEnd(p0: Animator) {
      endCallback()
    }
  }

private open class DefaultAnimatorListener : Animator.AnimatorListener {
  override fun onAnimationStart(p0: Animator) {}
  override fun onAnimationEnd(p0: Animator) {}
  override fun onAnimationCancel(p0: Animator) {}
  override fun onAnimationRepeat(p0: Animator) {}
}
