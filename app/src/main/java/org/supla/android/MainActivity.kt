package org.supla.android
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
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.children
import androidx.customview.widget.Openable
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.google.android.material.appbar.AppBarLayout
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.supla.android.core.networking.suplaclient.SuplaClientState
import org.supla.android.core.networking.suplaclient.SuplaClientStateHolder
import org.supla.android.core.notifications.NotificationsHelper
import org.supla.android.core.ui.BackHandleOwner
import org.supla.android.extensions.TAG
import org.supla.android.extensions.getChannelIconUseCase
import org.supla.android.extensions.setStatusBarColor
import org.supla.android.extensions.visibleIf
import org.supla.android.features.lockscreen.LockScreenFragment
import org.supla.android.features.lockscreen.UnlockAction
import org.supla.android.images.ImageCache
import org.supla.android.images.ImageId
import org.supla.android.lib.SuplaConst
import org.supla.android.lib.SuplaEvent
import org.supla.android.navigator.MainNavigator
import org.supla.android.restapi.DownloadUserIcons
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.ui.LoadableContent
import org.supla.android.ui.ToolbarItemsClickHandler
import org.supla.android.ui.ToolbarItemsController
import org.supla.android.ui.ToolbarTitleController
import org.supla.android.ui.ToolbarVisibilityController
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity :
  NavigationActivity(),
  ToolbarTitleController,
  LoadableContent,
  ToolbarItemsController,
  ToolbarVisibilityController,
  BackHandleOwner {

  private var downloadUserIcons: DownloadUserIcons? = null
  private var notificationView: RelativeLayout? = null
  private var notificationHandler: Handler? = null
  private var notificationnrunnable: Runnable? = null
  private var notificationImage: ImageView? = null
  private var notificationText: TextView? = null
  private var animatingMenu = false
  private val handler = Handler(Looper.getMainLooper())

  private val toolbar: Toolbar by lazy { findViewById(R.id.supla_toolbar) }
  private val menuLayout: MenuItemsLayout by lazy { findViewById(R.id.main_menu) }
  private val toolbarItemsClickHandlers = mutableListOf<ToolbarItemsClickHandler>()
  private val newGestureInfo: ConstraintLayout by lazy { findViewById(R.id.new_gesture_info) }
  private val newGestureInfoClose: AppCompatImageView by lazy { findViewById(R.id.new_gesture_info_close) }
  private val appBarLayout: AppBarLayout by lazy { findViewById(R.id.app_bar_layout) }
  private val appBarLayoutSpacer: View by lazy { findViewById(R.id.main_content_top_spacer) }

  private var lastDestinationId: Int? = null
  private val disposables: CompositeDisposable = CompositeDisposable()
  private var keepSplashScreen = true
  private var splashScreenDisposable: Disposable? = null

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

  @Inject
  lateinit var suplaClientStateHolder: SuplaClientStateHolder

  @Inject
  lateinit var suplaSchedulers: SuplaSchedulers

  @RequiresApi(Build.VERSION_CODES.O)
  val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
    if (isGranted) {
      notificationsHelper.setupNotificationChannel(this)
      notificationsHelper.setupBackgroundNotificationChannel(this)

      // Because for disabling we're sending an empty token, after right is granted we need to update token on server
      notificationsHelper.updateToken()
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    handleSplashScreen()
    legacySetup()
    navigationSetup()
    toolbarSetup()
    backCallbackSetup()

    if (preferences.shouldShowNewGestureInfo() && preferences.isNewGestureInfoPresented.not()) {
      newGestureInfo.bringToFront()
      newGestureInfo.visibility = View.VISIBLE
      newGestureInfoClose.setOnClickListener { newGestureInfo.visibility = View.GONE }
      preferences.isNewGestureInfoPresented = true
    }

    menuLayout.setOnClickListener(this::handleMenuClicks)
  }

  override fun onStart() {
    super.onStart()
    disposables.add(
      suplaClientStateHolder.state()
        .observeOn(suplaSchedulers.ui)
        .subscribeBy(
          onNext = {
            Trace.d(TAG, "Got state $it")
            when (it) {
              SuplaClientState.FirstProfileCreation -> navigator.navigateToNewProfile()
              is SuplaClientState.Connecting,
              SuplaClientState.Initialization,
              is SuplaClientState.Disconnecting,
              SuplaClientState.Locking,
              is SuplaClientState.Finished -> navigator.navigateToStatus()

              SuplaClientState.Locked -> {
                if (menuIsVisible()) {
                  setMenuVisible(false)
                }
                navigator.navigateTo(R.id.lock_screen_fragment, LockScreenFragment.bundle(UnlockAction.AuthorizeApplication))
              }

              else -> {}
            }
          }
        )
    )
  }

  override fun onStop() {
    super.onStop()
    disposables.clear()
  }

  fun registerMenuItemClickHandler(handler: ToolbarItemsClickHandler) {
    toolbarItemsClickHandlers.add(handler)
  }

  fun unregisterMenuItemClickHandler(handler: ToolbarItemsClickHandler) {
    toolbarItemsClickHandlers.remove(handler)
  }

  private fun legacySetup() {
    notificationHandler = null
    notificationnrunnable = null
    setContentView(R.layout.activity_main)
    notificationView = Inflate(R.layout.notification, null) as RelativeLayout
    notificationView!!.visibility = View.GONE
    val notificationBackgroundLayout = notificationView!!.findViewById<RelativeLayout>(R.id.notif_bg_layout)
    notificationBackgroundLayout.setOnClickListener(this)
    notificationBackgroundLayout.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.notification_bg, null))
    rootLayout.addView(notificationView)
    notificationImage = notificationView!!.findViewById(R.id.notif_img)
    notificationText = notificationView!!.findViewById<TextView?>(R.id.notif_txt).also {
      it.typeface = SuplaApp.getApp().typefaceOpenSansRegular
    }

    registerMessageHandler()
  }

  private fun navigationSetup() {
    val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
    val navController = navHostFragment.navController
    navController.setGraph(R.navigation.main_nav_graph)
    navController.addOnDestinationChangedListener { _, destination, _ -> configureToolbarOnDestinationChange(destination) }

    val appBarConfiguration = AppBarConfiguration
      .Builder(setOf(R.id.status_fragment, R.id.main_fragment))
      .setOpenableLayout(menuListener)
      .build()

    NavigationUI.setupWithNavController(
      toolbar,
      navController,
      appBarConfiguration
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

  private fun backCallbackSetup() {
    onBackPressedDispatcher.addCallback(
      this,
      object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
          if (menuIsVisible()) {
            setMenuVisible(false)
          } else if (isBackHandledInChildFragment(supportFragmentManager)) {
            return // Do nothing, is consumed by child fragment
          } else if (!navigator.back()) {
            finishAffinity()
          }
        }
      }
    )
  }

  private fun configureToolbarOnDestinationChange(destination: NavDestination) {
    lastDestinationId = destination.id

    setAccountItemVisible(profileManager.getAllProfiles().blockingFirst().size > 1 && lastDestinationId == R.id.main_fragment)
    setDeleteVisible(lastDestinationId == R.id.notifications_log_fragment)
    setDeleteHistoryVisible(lastDestinationId == R.id.thermometer_detail_fragment || lastDestinationId == R.id.gpm_detail_fragment)
  }

  override fun onResume() {
    super.onResume()
    setAccountItemVisible(profileManager.getAllProfiles().blockingFirst().size > 1 && lastDestinationId == R.id.main_fragment)
    setDeleteVisible(lastDestinationId == R.id.notifications_log_fragment)

    if (SuperuserAuthorizationDialog.lastOneIsStillShowing()) {
      return
    }
    runDownloadTask()

    RateApp(this).showDialog {
      handler.postDelayed({ it.run() }, 1000)
    }
  }

  override fun onPause() {
    super.onPause()
    handler.removeCallbacksAndMessages(null)
  }

  private fun setAccountItemVisible(visible: Boolean) {
    toolbar.menu.findItem(R.id.toolbar_accounts)?.isVisible = visible
  }

  private fun setDeleteVisible(visible: Boolean) {
    toolbar.menu.findItem(R.id.toolbar_delete)?.isVisible = visible
    toolbar.menu.findItem(R.id.toolbar_delete_older_than_month)?.isVisible = visible
  }

  private fun setDeleteHistoryVisible(visible: Boolean) {
    toolbar.menu.findItem(R.id.toolbar_delete_chart_history)?.isVisible = visible
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
    if (event.Owner && event.Event != SuplaConst.SUPLA_EVENT_SET_BRIDGE_VALUE_FAILED || event.ChannelID == 0) return
    val channel = getDbHelper()?.getChannel(event.ChannelID) ?: return
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
      imgId = getChannelIconUseCase.invoke(channel)
      msg = resources.getString(msgId)
      @SuppressLint("SimpleDateFormat")
      val sdf = SimpleDateFormat("HH:mm:ss")
      msg = sdf.format(Date()) + " " + event.SenderName + " " + msg
    }
    if (channel.hasCustomCaption()) {
      msg = msg + " (" + channel.getCaption(this) + ")"
    }
    showNotificationMessage(msg, imgId, imgResId)
  }

  private fun showHideNotificationView(show: Boolean) {
    if (!show && notificationView!!.visibility == View.GONE) return
    val height = resources.getDimension(R.dimen.channel_layout_height)
    notificationView!!.visibility = View.VISIBLE
    notificationView!!.bringToFront()
    notificationView!!.translationY = if (show) height else 0f
    notificationView!!.animate()
      .translationY(if (show) 0f else height)
      .setDuration(100)
      .setListener(
        object : AnimatorListenerAdapter() {
          override fun onAnimationEnd(animation: Animator) {
            super.onAnimationEnd(animation)
            if (!show) {
              notificationView!!.visibility = View.GONE
            }
          }
        }
      )
  }

  private fun showNotificationMessage(msg: String?, imgId: ImageId?, imgResId: Int) {
    notificationImage!!.setImageBitmap(null)
    notificationImage!!.background = null
    if (imgId != null) {
      notificationImage!!.setImageBitmap(ImageCache.getBitmap(this, imgId))
    } else if (imgResId > 0) {
      notificationImage!!.background = ResourcesCompat.getDrawable(resources, imgResId, null)
    }
    notificationText!!.text = msg
    showHideNotificationView(true)
    if (notificationHandler != null && notificationnrunnable != null) {
      notificationHandler!!.removeCallbacks(notificationnrunnable!!)
    }
    notificationHandler = Handler(Looper.getMainLooper())
    notificationnrunnable = Runnable {
      hideNotificationMessage()
      notificationHandler = null
      notificationnrunnable = null
    }
    notificationHandler!!.postDelayed(notificationnrunnable!!, 5000)
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
        if (getDbHelper()?.isZWaveBridgeChannelAvailable == true) {
          MenuItemsLayout.BTN_ALL
        } else {
          MenuItemsLayout.BTN_ALL xor MenuItemsLayout.BTN_Z_WAVE
        }
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
    if (v.parent === notificationView) {
      hideNotificationMessage()
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
      MenuItemsLayout.BTN_ABOUT -> navigator.navigateTo(R.id.about_fragment)
      MenuItemsLayout.BTN_ADD_DEVICE -> navigator.navigateToAddWizard()
      MenuItemsLayout.BTN_Z_WAVE -> SuperUserAuthorize(MenuItemsLayout.BTN_Z_WAVE)
      MenuItemsLayout.BTN_HELP -> navigator.navigateToWeb(Uri.parse(resources.getString(R.string.forumpage_url)))
      MenuItemsLayout.BTN_CLOUD -> navigator.navigateToCloudExternal()
      MenuItemsLayout.BTN_HOMEPAGE -> navigator.navigateToSuplaOrgExternal()
      MenuItemsLayout.BTN_PROFILE -> showProfile(this)
      MenuItemsLayout.BTN_NOTIFICATIONS -> navigator.navigateTo(R.id.notifications_log_fragment)
      MenuItemsLayout.BTN_DEVICE_CATALOG -> navigator.navigateTo(R.id.device_catalog_fragment)
    }
  }

  override fun setToolbarItemVisible(itemId: Int, visible: Boolean) {
    toolbar.menu.findItem(itemId).isVisible = visible
  }

  override fun setToolbarVisible(visible: Boolean) {
    appBarLayout.visibleIf(visible)
    appBarLayoutSpacer.visibleIf(visible)
    setStatusBarColor(if (visible) R.color.primary_container else R.color.background, visible.not())
  }

  private fun handleSplashScreen() {
    val splashScreen = installSplashScreen()
    splashScreen.setKeepOnScreenCondition { keepSplashScreen }

    splashScreenDisposable = suplaClientStateHolder.state()
      .subscribeOn(suplaSchedulers.io)
      .observeOn(suplaSchedulers.ui)
      .subscribeBy(
        onNext = {
          if (it != SuplaClientState.Initialization) {
            keepSplashScreen = false
            splashScreenDisposable?.dispose()
          }
        }
      )
  }
}
