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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.core.content.res.ResourcesCompat;
import dagger.hilt.android.AndroidEntryPoint;
import javax.inject.Inject;
import org.supla.android.profile.ProfileChooser;
import org.supla.android.profile.ProfileManager;
import org.supla.android.usecases.profile.ActivateProfileUseCase;
import org.supla.android.usecases.profile.ReadAllProfilesUseCase;

@SuppressLint("registered")
@AndroidEntryPoint
public class NavigationActivity extends BaseActivity
    implements View.OnClickListener,
        SuperuserAuthorizationDialog.OnAuthorizarionResultListener,
        ProfileChooser.Listener {

  public static final String INTENT_SENDER = "sender";
  public static final String INTENT_SENDER_MAIN = "main";

  @Inject ProfileManager profileManager;
  @Inject ActivateProfileUseCase activateProfileUseCase;
  @Inject ReadAllProfilesUseCase readAllProfilesUseCase;
  private RelativeLayout RootLayout;
  private RelativeLayout ContentLayout;
  private RelativeLayout MenuBarLayout;
  private ViewGroup Content;
  private Button MenuButton;
  private Button ProfileButton;
  private SuperuserAuthorizationDialog mAuthDialog;

  private static void showActivity(Activity sender, Class<?> cls) {

    Intent i = new Intent(sender.getBaseContext(), cls);
    i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
    i.putExtra(INTENT_SENDER, sender instanceof MainActivity ? INTENT_SENDER_MAIN : "");

    sender.startActivity(i);

    sender.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
  }

  public static void showMain(Activity sender) {
    showActivity(sender, MainActivity.class);
  }

  public static void showProfile(Activity sender) {
    showActivity(sender, org.supla.android.cfg.CfgActivity.class);
  }

  @Override
  protected void onResume() {
    super.onResume();
    getMenuBarLayout();
  }

  @Override
  protected void onPause() {
    super.onPause();
  }

  protected RelativeLayout getRootLayout() {

    if (RootLayout == null) {

      RootLayout = new RelativeLayout(this);
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

      TextView title = MenuBarLayout.findViewById(R.id.menubar_title);
      title.setTypeface(SuplaApp.getApp().getTypefaceQuicksandRegular());

      TextView detailTitle = MenuBarLayout.findViewById(R.id.menubar_detail_title);
      detailTitle.setTypeface(SuplaApp.getApp().getTypefaceQuicksandRegular());

      getRootLayout().addView(MenuBarLayout);

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
      ContentLayout.setBackgroundColor(getResources().getColor(R.color.background));

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
    MenuButton.setTag(1);
    ProfileButton.setVisibility(View.GONE);
  }

  public void showMenuBar() {

    RelativeLayout.LayoutParams lp =
        new RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
    lp.addRule(RelativeLayout.BELOW, getMenuBarLayout().getId());
    getContentLayout().setLayoutParams(lp);

    if (MenuBarLayout != null) {
      MenuBarLayout.setVisibility(View.VISIBLE);
    }
  }

  public void showZWaveConfigurationWizard() {
    showActivity(this, ZWaveConfigurationWizardActivity.class);
  }

  protected void showProfileSelector() {
    ProfileChooser profileChooser =
        new ProfileChooser(this, activateProfileUseCase, readAllProfilesUseCase);
    profileChooser.setListener(this);
    profileChooser.show();
  }

  private void setBtnBackground(Button btn, int imgResId) {
    btn.setBackground(ResourcesCompat.getDrawable(getResources(), imgResId, null));
  }

  @Override
  public void onClick(View v) {
    if (v == MenuButton && MenuButton.getTag().equals(1)) {
      getOnBackPressedDispatcher().onBackPressed();
      return;
    }

    if (v == ProfileButton) {
      showProfileSelector();
    }
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
        && dialog.getObject().equals(MenuItemsLayout.BTN_Z_WAVE)) {
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
