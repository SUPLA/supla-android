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
syays GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.res.ResourcesCompat;
import dagger.hilt.android.AndroidEntryPoint;
import java.util.ArrayList;
import javax.inject.Inject;
import org.supla.android.core.branding.Configuration.Menu;
import org.supla.android.profile.ProfileManager;

@AndroidEntryPoint
public class MenuItemsLayout extends LinearLayout implements View.OnClickListener {

  public static final int BTN_SETTINGS = 0x1;
  public static final int BTN_ADD_DEVICE = 0x2;
  public static final int BTN_ABOUT = 0x4;
  public static final int BTN_HELP = 0x10;
  public static final int BTN_HOMEPAGE = 0x20;
  public static final int BTN_FREE_SPACE = 0x40;
  public static final int BTN_Z_WAVE = 0x80;
  public static final int BTN_CLOUD = 0x90;
  public static final int BTN_NOTIFICATIONS = 0x100;
  public static final int BTN_DEVICE_CATALOG = 0x200;
  public static final int BTN_PROFILE = 0x1000;
  public static final int BTN_ALL = 0xFFFF;
  ArrayList<Button> buttons = new ArrayList<>();
  private LinearLayout mMainButtonsAreaLayout = null;
  private boolean mInitialized = false;
  private int availableButtons = 0;
  private OnClickListener mOnClickListener;

  @Inject ProfileManager profileManager;

  public MenuItemsLayout(Context context) {
    super(context);
    initialize();
  }

  public MenuItemsLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
    initialize();
  }

  public MenuItemsLayout(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  public MenuItemsLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    initialize();
  }

  public static int getButtonId(View v) {
    if (v != null && v.getTag() instanceof Integer) {
      return (Integer) v.getTag();
    }
    return 0;
  }

  private void initialize() {
    if (!mInitialized) {
      mInitialized = true;
      setVisibility(GONE);
      setBackgroundColor(0);
      setLayoutParams(
          new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
      setOrientation(VERTICAL);

      mMainButtonsAreaLayout = new LinearLayout(getContext());
      LayoutParams params =
          new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
      mMainButtonsAreaLayout.setLayoutParams(params);
      mMainButtonsAreaLayout.setOrientation(VERTICAL);
      mMainButtonsAreaLayout.setBackgroundColor(getResources().getColor(R.color.toolbar));
      addView(mMainButtonsAreaLayout);
    }
  }

  private void addLongSeparator() {
    View view = new View(getContext());
    view.setBackgroundColor(getResources().getColor(R.color.menuseparator));
    view.setAlpha(0.4f);
    mMainButtonsAreaLayout.addView(view);

    LinearLayout.LayoutParams params =
        new LinearLayout.LayoutParams(
            LayoutParams.MATCH_PARENT,
            getResources().getDimensionPixelSize(R.dimen.menuitem_separator_height));
    view.setLayoutParams(params);
  }

  private void addShortSeparator() {

    LinearLayout ll = new LinearLayout(getContext());
    ll.setLayoutParams(
        new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    ll.setOrientation(HORIZONTAL);
    mMainButtonsAreaLayout.addView(ll);

    View view = new View(getContext());
    view.setBackgroundColor(Color.TRANSPARENT);
    ll.addView(view);

    int margin = getResources().getDimensionPixelSize(R.dimen.menuitem_iamge_margin);
    int width = getResources().getDimensionPixelSize(R.dimen.menuitem_height);
    int separator_height = getResources().getDimensionPixelSize(R.dimen.menuitem_separator_height);

    LinearLayout.LayoutParams params =
        new LinearLayout.LayoutParams(margin * 2 + width, separator_height);
    view.setLayoutParams(params);

    view = new View(getContext());
    view.setBackgroundColor(getResources().getColor(R.color.menuseparator));
    view.setAlpha(0.4f);
    ll.addView(view);

    params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, separator_height);
    view.setLayoutParams(params);
  }

  private void addButton(int id, @DrawableRes int iconResId, @StringRes int textResId) {

    if ((availableButtons & id) == 0) {
      return;
    }

    if (buttons.isEmpty()) {
      addLongSeparator();
    } else {
      addShortSeparator();
    }

    LinearLayout ll = new LinearLayout(getContext());
    ll.setLayoutParams(
        new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    ll.setOrientation(HORIZONTAL);
    ll.setClickable(true);
    mMainButtonsAreaLayout.addView(ll);

    ImageView iv = new ImageView(getContext());
    iv.setClickable(true);
    iv.setTag(id);
    iv.setOnClickListener(this);
    ll.addView(iv);

    int size = getResources().getDimensionPixelSize(R.dimen.icon_big_size);
    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
    int verticalMargin = getResources().getDimensionPixelSize(R.dimen.distance_tiny);
    int horizontalMargin = getResources().getDimensionPixelSize(R.dimen.distance_small);
    params.setMargins(horizontalMargin, verticalMargin, horizontalMargin, verticalMargin);

    iv.setLayoutParams(params);

    iv.setImageResource(iconResId);
    iv.setImageTintList(
        ColorStateList.valueOf(ResourcesCompat.getColor(getResources(), R.color.on_primary, null)));

    Button btn = new Button(getContext(), null);
    btn.setOnClickListener(this);
    btn.setTypeface(SuplaApp.getApp().getTypefaceOpenSansRegular());
    btn.setTextSize(
        TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.menuitem_text_size));
    btn.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
    btn.setTextColor(Color.WHITE);

    btn.setPadding(0, 0, 0, getResources().getDimensionPixelSize(R.dimen.menuitem_padding));
    btn.setTransformationMethod(null);
    btn.setText(getResources().getString(textResId).toUpperCase());
    btn.setBackgroundColor(0);
    btn.setTag(id);
    buttons.add(btn);
    ll.addView(btn);

    params =
        new LinearLayout.LayoutParams(
            LayoutParams.MATCH_PARENT,
            getResources().getDimensionPixelSize(R.dimen.menuitem_height));
    btn.setLayoutParams(params);
  }

  private void addFooter() {

    Button btn;
    LayoutParams params;

    if ((availableButtons & BTN_HOMEPAGE) != 0) {
      addLongSeparator();

      btn = new Button(getContext(), null);
      btn.setTag(BTN_HOMEPAGE);
      btn.setText(getResources().getString(R.string.homepage).toUpperCase());
      btn.setTextSize(
          TypedValue.COMPLEX_UNIT_PX,
          getResources().getDimension(R.dimen.menuitem_homepage_text_size));
      btn.setTextColor(Color.WHITE);
      btn.setBackgroundColor(0);
      btn.setOnClickListener(this);
      btn.setTypeface(SuplaApp.getApp().getTypefaceOpenSansBold());
      mMainButtonsAreaLayout.addView(btn);

      params =
          new LayoutParams(
              LayoutParams.MATCH_PARENT,
              getResources().getDimensionPixelSize(R.dimen.menuitem_height));
      btn.setLayoutParams(params);
      btn.setTransformationMethod(null);
    }

    if ((availableButtons & BTN_FREE_SPACE) != 0) {
      btn = new Button(getContext(), null);
      btn.setTag(BTN_FREE_SPACE);
      btn.setOnClickListener(this);
      addView(btn);

      params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
      btn.setLayoutParams(params);
      btn.setTransformationMethod(null);
      btn.setBackgroundColor(0);
    }
  }

  public void setButtonsAvailable(int available) {

    if (availableButtons == available) {
      return;
    }

    boolean hasManyAccounts = profileManager.getAllProfiles().blockingFirst().size() > 1;

    availableButtons = available;
    mMainButtonsAreaLayout.removeAllViews();
    buttons.clear();

    addButton(
        BTN_PROFILE,
        R.drawable.ic_menu_profiles,
        hasManyAccounts ? R.string.profile_plural : R.string.profile);
    addButton(BTN_SETTINGS, R.drawable.ic_menu_settings, R.string.settings);
    addButton(BTN_ADD_DEVICE, R.drawable.ic_menu_add_device, R.string.add_device);
    if (Menu.DEVICES_OPTION_VISIBLE) {
      addButton(
          BTN_DEVICE_CATALOG, R.drawable.ic_menu_device_catalog, R.string.menu_device_catalog);
    }
    if (Menu.Z_WAVE_OPTION_VISIBLE) {
      addButton(BTN_Z_WAVE, R.drawable.ic_menu_z_wave, R.string.z_wave);
    }
    addButton(BTN_NOTIFICATIONS, R.drawable.ic_notification, R.string.menu_notifications);
    if (Menu.ABOUT_OPTION_VISIBLE) {
      addButton(BTN_ABOUT, R.drawable.ic_menu_about, R.string.about);
    }
    // Google Play Policy
    if (Menu.HELP_OPTION_VISIBLE) {
      addButton(BTN_HELP, R.drawable.ic_menu_help, R.string.help);
    }
    addButton(BTN_CLOUD, R.drawable.ic_menu_cloud, R.string.supla_cloud);
    addFooter();
  }

  public int getBtnAreaHeight() {
    return mMainButtonsAreaLayout == null ? getHeight() : mMainButtonsAreaLayout.getHeight();
  }

  @Override
  public void onClick(View v) {
    if (mOnClickListener != null) {
      mOnClickListener.onClick(v);
    }
  }

  @Override
  public void setOnClickListener(@Nullable OnClickListener l) {
    mOnClickListener = l;
  }
}
