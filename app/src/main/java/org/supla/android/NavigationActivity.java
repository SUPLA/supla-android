package org.supla.android;

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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import dagger.hilt.android.AndroidEntryPoint;
import javax.inject.Inject;
import org.supla.android.cfg.CfgActivity;
import org.supla.android.lib.SuplaClient;
import org.supla.android.profile.ProfileChooser;
import org.supla.android.profile.ProfileManager;

@SuppressLint("registered")
@AndroidEntryPoint
public class NavigationActivity extends BaseActivity
    implements View.OnClickListener,
        SuperuserAuthorizationDialog.OnAuthorizarionResultListener,
        ProfileChooser.Listener {

  public static final String INTENTSENDER = "sender";
  public static final String INTENTSENDER_MAIN = "main";

  @Inject ProfileManager profileManager;
  private RelativeLayout RootLayout;
  private RelativeLayout ContentLayout;
  private RelativeLayout MenuBarLayout;
  private ViewGroup Content;
  private Button MenuButton;
  private Button ProfileButton;
  private SuperuserAuthorizationDialog mAuthDialog;
  private TextView title;
  private TextView detailTitle;
  private ProfileChooser profileChooser;

  private static void showActivity(Activity sender, Class<?> cls, int flags, String action) {

    Intent i = new Intent(sender.getBaseContext(), cls);
    i.setFlags(flags == 0 ? Intent.FLAG_ACTIVITY_REORDER_TO_FRONT : flags);
    i.putExtra(INTENTSENDER, sender instanceof MainActivity ? INTENTSENDER_MAIN : "");
    if (action != null) {
      i.setAction(action);
    }

    sender.startActivity(i);

    sender.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
  }

  private static void showActivity(Activity sender, Class<?> cls, int flags) {
    showActivity(sender, cls, flags, null);
  }

  public static void showMain(Activity sender) {

    SuplaClient client = SuplaApp.getApp().getSuplaClient();

    if (client != null && client.registered()) {

      showActivity(sender, MainActivity.class, 0);

    } else {
      showStatus(sender);
    }
  }

  public static void showStatus(Activity sender) {
    showActivity(sender, StatusActivity.class, 0);
  }

  public static void showAuth(Activity sender) {
    showActivity(
        sender,
        org.supla.android.cfg.CfgActivity.class,
        0,
        org.supla.android.cfg.CfgActivity.ACTION_AUTH);
  }

  public static void showProfile(Activity sender) {
    showActivity(sender, org.supla.android.cfg.CfgActivity.class, 0, null);
  }

  public static void showCfg(Activity sender) {
    showActivity(
        sender,
        org.supla.android.cfg.CfgActivity.class,
        0,
        org.supla.android.cfg.CfgActivity.ACTION_CONFIG);
  }

  @Override
  protected void onResume() {

    super.onResume();
    CurrentActivity = this;

    getMenuBarLayout();
  }

  @Override
  protected void onPause() {
    super.onPause();

    if (CurrentActivity == this) {
      CurrentActivity = null;
    }
  }

  protected RelativeLayout getRootLayout() {

    if (RootLayout == null) {

      RootLayout = new RelativeLayout(this);
      RootLayout.setId(ViewHelper.generateViewId());
      RootLayout.setLayoutParams(
          new RelativeLayout.LayoutParams(
              ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
      super.setContentView(RootLayout);
    }

    return RootLayout;
  }

  protected View Inflate(int resID, ViewGroup root) {
    LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
    return inflater == null ? null : inflater.inflate(resID, root);
  }

  private RelativeLayout getMenuBarLayout() {

    if (MenuBarLayout == null) {

      MenuBarLayout = (RelativeLayout) Inflate(R.layout.menubar, null);
      MenuBarLayout.setVisibility(View.GONE);

      title = MenuBarLayout.findViewById(R.id.menubar_title);
      title.setTypeface(SuplaApp.getApp().getTypefaceQuicksandRegular());

      detailTitle = MenuBarLayout.findViewById(R.id.menubar_detail_title);
      detailTitle.setTypeface(SuplaApp.getApp().getTypefaceQuicksandRegular());

      getRootLayout().addView(MenuBarLayout);

      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
        MenuBarLayout.setLayoutParams(
            new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, MenuBarLayout.getLayoutParams().height));
      }

      MenuButton = findViewById(R.id.menubutton);
      MenuButton.setVisibility(View.GONE);
      MenuButton.setOnClickListener(this);

      ProfileButton = findViewById(R.id.profilebutton);
      ProfileButton.setVisibility(View.GONE);
      ProfileButton.setOnClickListener(this);
    }

    return MenuBarLayout;
  }

  protected RelativeLayout getContentLayout() {

    if (ContentLayout == null) {

      ContentLayout = new RelativeLayout(this);
      ContentLayout.setId(ViewHelper.generateViewId());
      ContentLayout.setBackgroundColor(getResources().getColor(R.color.activity_bg));

      RelativeLayout.LayoutParams lp =
          new RelativeLayout.LayoutParams(
              RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
      lp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);

      ContentLayout.setLayoutParams(lp);
      getRootLayout().addView(ContentLayout);
    }

    return ContentLayout;
  }

  @Override
  public void setContentView(int layoutResID) {

    if (Content != null) {
      getContentLayout().removeView(Content);
      Content = null;
    }

    Content = (ViewGroup) Inflate(layoutResID, getContentLayout());
  }

  public void showBackButton() {
    getMenuBarLayout();
    setBtnBackground(MenuButton, R.drawable.back);
    MenuButton.setVisibility(View.VISIBLE);
    MenuButton.setTag(Integer.valueOf(1));
    ProfileButton.setVisibility(View.GONE);
  }

  public void showMenuBar() {

    RelativeLayout.LayoutParams lp =
        new RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
    lp.addRule(RelativeLayout.BELOW, getMenuBarLayout().getId());
    getContentLayout().setLayoutParams(lp);

    if (MenuBarLayout != null) MenuBarLayout.setVisibility(View.VISIBLE);
  }

  public void openHomepage() {
    Intent browserIntent =
        new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.homepage_url)));
    startActivity(browserIntent);
  }

  public void openForumpage() {
    Intent browserIntent =
        new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.forumpage_url)));
    startActivity(browserIntent);
  }

  public void openCloud() {
    Intent browserIntent =
        new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.cloud_url)));
    startActivity(browserIntent);
  }

  public void donate() {
    Intent browserIntent =
        new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.donate_url)));
    startActivity(browserIntent);
  }

  public void showAbout() {
    showActivity(this, AboutActivity.class, 0);
  }

  public void showAddWizard() {
    showActivity(this, AddDeviceWizardActivity.class, 0);
  }

  public void showZWaveConfigurationWizard() {
    showActivity(this, ZWaveConfigurationWizardActivity.class, 0);
  }

  public void gotoMain() {
    Intent intent = new Intent(Intent.ACTION_MAIN);
    intent.addCategory(Intent.CATEGORY_HOME);
    startActivity(intent);
  }

  protected void showProfileSelector() {
    profileChooser = new ProfileChooser(this, profileManager);
    profileChooser.setListener(this);
    profileChooser.show();
  }

  public void dismissProfileSelector() {
    if (profileChooser != null) {
      profileChooser.dismiss();
      profileChooser = null;
    }
  }

  private void setBtnBackground(Button btn, int imgResId) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
      btn.setBackground(getResources().getDrawable(imgResId));
    } else {
      btn.setBackgroundDrawable(getResources().getDrawable(imgResId));
    }
  }

  @Override
  public void onClick(View v) {
    if (v == MenuButton && MenuButton.getTag().equals(Integer.valueOf(1))) {
      onBackPressed();
      return;
    }

    if (v == ProfileButton) {
      showProfileSelector();
    }
  }

  @Override
  protected void beforeStatusMsg() {
    super.beforeStatusMsg();

    if (CurrentActivity != null
        && !(CurrentActivity instanceof StatusActivity)
        && !(CurrentActivity instanceof CfgActivity)
        && !(CurrentActivity instanceof AddDeviceWizardActivity)) {
      showStatus(this);
    }
  }

  public static NavigationActivity getCurrentNavigationActivity() {
    if (CurrentActivity != null && CurrentActivity instanceof NavigationActivity) {
      return (NavigationActivity) CurrentActivity;
    }
    return null;
  }

  public void SuperUserAuthorize(int sourceBtnId) {
    if (mAuthDialog != null) {
      mAuthDialog.close();
      mAuthDialog = null;
    }

    mAuthDialog = new SuperuserAuthorizationDialog(this);
    mAuthDialog.setObject(sourceBtnId);
    mAuthDialog.setOnAuthorizarionResultListener(this);
    mAuthDialog.showIfNeeded();
  }

  @Override
  public void onSuperuserOnAuthorizarionResult(
      SuperuserAuthorizationDialog dialog, boolean Success, int Code) {
    if (Success
        && dialog != null
        && dialog == mAuthDialog
        && dialog.getObject().equals(Integer.valueOf(MenuItemsLayout.BTN_Z_WAVE))) {
      mAuthDialog.close();
      mAuthDialog = null;
      showZWaveConfigurationWizard();
    }
  }

  @Override
  public void authorizationCanceled() {}

  @Override
  public void onProfileChanged() {
    invalidateDbHelper();
  }
}
