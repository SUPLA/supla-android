package org.supla.android

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.children
import androidx.customview.widget.Openable
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.core.notifications.NotificationsHelper
import org.supla.android.core.ui.BackHandleOwner
import org.supla.android.db.MeasurementsDbHelper
import org.supla.android.features.notificationinfo.NotificationInfoDialog
import org.supla.android.images.ImageCache
import org.supla.android.images.ImageId
import org.supla.android.lib.SuplaConst
import org.supla.android.lib.SuplaEvent
import org.supla.android.navigator.MainNavigator
import org.supla.android.restapi.DownloadUserIcons
import org.supla.android.ui.LoadableContent
import org.supla.android.ui.ToolbarItemsClickHandler
import org.supla.android.ui.ToolbarItemsController
import org.supla.android.ui.ToolbarTitleController
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/*
 Copyright (C) AC SOFTWARE SP. Z O.O.

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
syays GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

@AndroidEntryPoint
class MainActivity : NavigationActivity(), ToolbarTitleController, LoadableContent, ToolbarItemsController, BackHandleOwner {

  private var downloadUserIcons: DownloadUserIcons? = null
  private var NotificationView: RelativeLayout? = null
  private var notif_handler: Handler? = null
  private var notif_nrunnable: Runnable? = null
  private var notif_img: ImageView? = null
  private var notif_text: TextView? = null
  private var animatingMenu = false

  private val toolbar: Toolbar by lazy { findViewById(R.id.supla_toolbar) }
  private val menuLayout: MenuItemsLayout by lazy { findViewById(R.id.main_menu) }
  private val toolbarItemsClickHandlers = mutableListOf<ToolbarItemsClickHandler>()
  private val newGestureInfo: ConstraintLayout by lazy { findViewById(R.id.new_gesture_info) }
  private val newGestureInfoClose: AppCompatImageView by lazy { findViewById(R.id.new_gesture_info_close) }

  private var lastDestinationId: Int? = null

  private val menuListener: Openable = object : Openable {

    override fun isOpen(): Boolean = menuIsVisible()

    override fun close() = setMenuVisible(false)

    override fun open() {
      newGestureInfo.visibility = View.GONE

      if (isOpen) {
        setMenuVisible(false)
      } else {
        setMenuVisible(true)
      }
    }
  }

  @Inject
  lateinit var navigator: MainNavigator

  @Inject
  lateinit var preferences: Preferences

  @Inject
  lateinit var notificationsHelper: NotificationsHelper

  @RequiresApi(Build.VERSION_CODES.O)
  val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
    if (isGranted) {
      notificationsHelper.setupNotificationChannel(this)

      // Because for disabled we're sending an empty token, after right is granted we need to update token on server
      notificationsHelper.updateToken()
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    legacySetup()
    navigationSetup()
    toolbarSetup()
    notificationsHelper.setup(this) {
      NotificationInfoDialog.create().show(supportFragmentManager, null)
    }

    if (preferences.shouldShowNewGestureInfo() && preferences.isNewGestureInfoPresented.not()) {
      newGestureInfo.bringToFront()
      newGestureInfo.visibility = View.VISIBLE
      newGestureInfoClose.setOnClickListener { newGestureInfo.visibility = View.GONE }
      preferences.isNewGestureInfoPresented = true
    }

    menuLayout.setOnClickListener(this::handleMenuClicks)
  }

  fun registerMenuItemClickHandler(handler: ToolbarItemsClickHandler) {
    toolbarItemsClickHandlers.add(handler)
  }

  fun unregisterMenuItemClickHandler(handler: ToolbarItemsClickHandler) {
    toolbarItemsClickHandlers.remove(handler)
  }

  private fun legacySetup() {
    notif_handler = null
    notif_nrunnable = null
    setContentView(R.layout.activity_main)
    NotificationView = Inflate(R.layout.notification, null) as RelativeLayout
    NotificationView!!.visibility = View.GONE
    val NotifBgLayout = NotificationView!!.findViewById<RelativeLayout>(R.id.notif_bg_layout)
    NotifBgLayout.setOnClickListener(this)
    NotifBgLayout.setBackgroundColor(resources.getColor(R.color.notification_bg))
    rootLayout.addView(NotificationView)
    notif_img = NotificationView!!.findViewById(R.id.notif_img)
    notif_text = NotificationView!!.findViewById<TextView?>(R.id.notif_txt).also {
      it.typeface = SuplaApp.getApp().typefaceOpenSansRegular
    }

    MeasurementsDbHelper.getInstance(this) // For upgrade purposes
    RegisterMessageHandler()
  }

  private fun navigationSetup() {
    val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
    val navController = navHostFragment.navController
    navController.setGraph(R.navigation.main_nav_graph)
    navController.addOnDestinationChangedListener { _, destination, _ -> configureToolbarOnDestinationChange(destination) }

    NavigationUI.setupWithNavController(
      toolbar,
      navController,
      AppBarConfiguration(
        navGraph = navController.graph,
        menuListener
      )
    )
  }

  private fun toolbarSetup() {
    toolbar.inflateMenu(R.menu.toolbar)
    toolbar.setOnMenuItemClickListener { item ->
      newGestureInfo.visibility = View.GONE

      if (item.itemId == R.id.toolbar_accounts) {
        showProfileSelector()
        return@setOnMenuItemClickListener true
      }
      for (handler in toolbarItemsClickHandlers) {
        if (handler.onMenuItemClick(item)) {
          return@setOnMenuItemClickListener true
        }
      }

      false
    }

    for (child in toolbar.children) {
      if (child is AppCompatTextView && child.id != R.id.supla_toolbar_title) {
        child.visibility = View.GONE
      }
    }
  }

  private fun configureToolbarOnDestinationChange(destination: NavDestination) {
    lastDestinationId = destination.id
    setAccountItemVisible(profileManager.getAllProfiles().blockingFirst().size > 1 && lastDestinationId == R.id.main_fragment)

    if (destination.id != R.id.main_fragment) {
      findViewById<FrameLayout>(R.id.main_content).setPadding(0, 0, 0, 0)
    }
  }

  override fun onResume() {
    super.onResume()
    setAccountItemVisible(profileManager.getAllProfiles().blockingFirst().size > 1 && lastDestinationId == R.id.main_fragment)

    if (SuperuserAuthorizationDialog.lastOneIsStillShowing()) {
      return
    }
    runDownloadTask()
    val ra = RateApp(this)
    ra.showDialog(1000)
  }

  private fun setAccountItemVisible(visible: Boolean) {
    toolbar.menu.findItem(R.id.toolbar_accounts)?.isVisible = visible
  }

  private fun runDownloadTask() {
    Trace.d("RubDownloadTask", "RunDownloadTask")
    if (downloadUserIcons != null && !downloadUserIcons!!.isAlive(90)) {
      downloadUserIcons!!.cancel(true)
      downloadUserIcons = null
    }
    if (downloadUserIcons == null) {
      downloadUserIcons = DownloadUserIcons(this)
      downloadUserIcons!!.execute()
    }
  }

  override fun onRegisteredMsg() {
    runDownloadTask()
  }

  override fun onEventMsg(event: SuplaEvent) {
    super.onEventMsg(event)
    if ((event == null || event.Owner) && event.Event != SuplaConst.SUPLA_EVENT_SET_BRIDGE_VALUE_FAILED || event.ChannelID == 0) return
    val channel = dbHelper.getChannel(event.ChannelID) ?: return
    var imgResId = 0
    var imgId: ImageId? = null
    var msg: String
    if (event.Event == SuplaConst.SUPLA_EVENT_SET_BRIDGE_VALUE_FAILED) {
      if (channel.flags and SuplaConst.SUPLA_CHANNEL_FLAG_ZWAVE_BRIDGE > 0) {
        msg = resources.getString(R.string.zwave_device_communication_error)
        imgResId = R.drawable.zwave_device_error
      } else {
        return
      }
    } else {
      val msgId: Int = when (event.Event) {
        SuplaConst.SUPLA_EVENT_CONTROLLINGTHEGATEWAYLOCK -> R.string.event_openedthegateway
        SuplaConst.SUPLA_EVENT_CONTROLLINGTHEGATE -> R.string.event_openedclosedthegate
        SuplaConst.SUPLA_EVENT_CONTROLLINGTHEGARAGEDOOR -> R.string.event_openedclosedthegatedoors
        SuplaConst.SUPLA_EVENT_CONTROLLINGTHEDOORLOCK -> R.string.event_openedthedoor
        SuplaConst.SUPLA_EVENT_CONTROLLINGTHEROLLERSHUTTER -> R.string.event_openedcloserollershutter
        SuplaConst.SUPLA_EVENT_CONTROLLINGTHEROOFWINDOW -> R.string.event_openedclosedtheroofwindow
        SuplaConst.SUPLA_EVENT_POWERONOFF -> R.string.event_poweronoff
        SuplaConst.SUPLA_EVENT_LIGHTONOFF -> R.string.event_turnedthelightonoff
        SuplaConst.SUPLA_EVENT_VALVEOPENCLOSE -> R.string.event_openedclosedthevalve
        else -> return
      }
      imgId = channel.imageIdx
      msg = resources.getString(msgId)
      @SuppressLint("SimpleDateFormat")
      val sdf = SimpleDateFormat("HH:mm:ss")
      msg = sdf.format(Date()) + " " + event.SenderName + " " + msg
    }
    if (channel.caption != "") {
      msg = msg + " (" + channel.caption + ")"
    }
    showNotificationMessage(msg, imgId, imgResId)
  }

  private fun showHideNotificationView(show: Boolean) {
    if (!show && NotificationView!!.visibility == View.GONE) return
    val height = resources.getDimension(R.dimen.channel_layout_height)
    NotificationView!!.visibility = View.VISIBLE
    NotificationView!!.bringToFront()
    NotificationView!!.setTranslationY(if (show) height else 0f)
    NotificationView!!.animate()
      .translationY(if (show) 0f else height)
      .setDuration(100)
      .setListener(
        object : AnimatorListenerAdapter() {
          override fun onAnimationEnd(animation: Animator) {
            super.onAnimationEnd(animation)
            if (!show) {
              NotificationView!!.visibility = View.GONE
            }
          }
        }
      )
  }

  private fun showNotificationMessage(msg: String?, imgId: ImageId?, imgResId: Int) {
    notif_img!!.setImageBitmap(null)
    notif_img!!.background = null
    if (imgId != null) {
      notif_img!!.setImageBitmap(ImageCache.getBitmap(this, imgId))
    } else if (imgResId > 0) {
      notif_img!!.background = ResourcesCompat.getDrawable(resources, imgResId, null)
    }
    notif_text!!.text = msg
    showHideNotificationView(true)
    if (notif_handler != null && notif_nrunnable != null) {
      notif_handler!!.removeCallbacks(notif_nrunnable!!)
    }
    notif_handler = Handler()
    notif_nrunnable = Runnable {
      hideNotificationMessage()
      notif_handler = null
      notif_nrunnable = null
    }
    notif_handler!!.postDelayed(notif_nrunnable!!, 5000)
  }

  private fun hideNotificationMessage() {
    showHideNotificationView(false)
  }

  private fun menuIsVisible(): Boolean {
    return menuLayout.visibility == View.VISIBLE
  }

  private fun setMenuVisible(visible: Boolean) {
    if (visible && menuIsVisible()) return
    if (!visible && !menuIsVisible()) return

    if (visible) {
      if (animatingMenu) return
      val btns =
        if (dbHelper.isZWaveBridgeChannelAvailable) MenuItemsLayout.BTN_ALL else MenuItemsLayout.BTN_ALL xor MenuItemsLayout.BTN_Z_WAVE
      menuLayout.setButtonsAvailable(btns)
      menuLayout.y = (-menuLayout.btnAreaHeight).toFloat()
      menuLayout.visibility = View.VISIBLE
      animatingMenu = true
      menuLayout.animate()
        .translationY(0f)
        .setDuration(200)
        .setListener(
          object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
              super.onAnimationEnd(animation)
              animatingMenu = false
            }
          }
        )
    } else {
      if (animatingMenu) return
      animatingMenu = true
      menuLayout
        .animate()
        .translationY(-menuLayout.btnAreaHeight.toFloat())
        .setDuration(200)
        .setListener(
          object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
              super.onAnimationEnd(animation)
              menuLayout.visibility = View.GONE
              animatingMenu = false
            }
          }
        )
    }
  }

  override fun onClick(v: View) {
    super.onClick(v)
    if (v.parent === NotificationView) {
      hideNotificationMessage()
    }
  }

  override fun onBackPressed() {
    if (menuIsVisible()) {
      setMenuVisible(false)
    } else if (isBackHandledInChildFragment(supportFragmentManager)) {
      return // Do nothing, is consumed by child fragment
    } else if (!navigator.back()) {
      finishAffinity()
    }
  }

  override fun onProfileChanged() {
    super.onProfileChanged()
    runDownloadTask()
    setMenuVisible(false)
  }

  override fun setToolbarTitle(title: String) {
    toolbar.title = title
  }

  override fun getLoadingIndicator(): View = findViewById(R.id.loadingIndicator)

  private fun handleMenuClicks(v: View) {
    setMenuVisible(false)

    when (MenuItemsLayout.getButtonId(v)) {
      MenuItemsLayout.BTN_SETTINGS -> navigator.navigateTo(R.id.application_settings_fragment)
      MenuItemsLayout.BTN_ABOUT -> showAbout()
      MenuItemsLayout.BTN_ADD_DEVICE -> showAddWizard()
      MenuItemsLayout.BTN_Z_WAVE -> SuperUserAuthorize(MenuItemsLayout.BTN_Z_WAVE)
      MenuItemsLayout.BTN_DONATE -> donate()
      MenuItemsLayout.BTN_HELP -> openForumpage()
      MenuItemsLayout.BTN_CLOUD -> openCloud()
      MenuItemsLayout.BTN_HOMEPAGE -> openHomepage()
      MenuItemsLayout.BTN_PROFILE -> showProfile(this)
    }
  }

  override fun setToolbarItemVisible(itemId: Int, visible: Boolean) {
    toolbar.menu.findItem(itemId).isVisible = visible
  }
}
