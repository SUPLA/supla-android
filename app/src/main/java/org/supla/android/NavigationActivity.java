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


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.supla.android.lib.SuplaClient;

@SuppressLint("Registered")
public class NavigationActivity extends BaseActivity implements View.OnClickListener {

    public static final String INTENTSENDER = "sender";
    public static final String INTENTSENDER_MAIN = "main";

    private RelativeLayout RootLayout;
    private RelativeLayout ContentLayout;
    private RelativeLayout MenuBarLayout;
    private MenuItemsLayout mMenuItemsLayout;
    private ViewGroup Content;

    private Button MenuButton;
    private Button GroupButton;

    private boolean Anim = false;

    @Override
    protected void onResume() {

        super.onResume();
        CurrentActivity = this;
    }

    @Override
    protected void onPause() {
        super.onPause();

        if ( CurrentActivity == this ) {
            CurrentActivity = null;
        }
    }


    protected RelativeLayout getRootLayout() {

        if ( RootLayout == null ) {

            RootLayout = new RelativeLayout(this);
            RootLayout.setId(ViewHelper.generateViewId());
            RootLayout.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            super.setContentView(RootLayout);
        }

        return RootLayout;
    }

    protected View Inflate(int resID, ViewGroup root) {
        LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        return inflater == null ? null : inflater.inflate(resID, root);
    }

    private RelativeLayout getMenuBarLayout() {

        if ( MenuBarLayout == null ) {

            MenuBarLayout = (RelativeLayout)Inflate(R.layout.menubar, null);
            MenuBarLayout.setVisibility(View.GONE);

            TextView title = MenuBarLayout.findViewById(R.id.menubar_title);
            Typeface type = Typeface.createFromAsset(getAssets(),"fonts/Quicksand-Regular.ttf");
            title.setTypeface(type);

            getRootLayout().addView(MenuBarLayout);

            MenuButton = findViewById(R.id.menubutton);
            MenuButton.setVisibility(View.GONE);
            MenuButton.setOnClickListener(this);

            GroupButton = findViewById(R.id.groupbutton);
            GroupButton.setVisibility(View.GONE);
            GroupButton.setOnClickListener(this);
            GroupButton.setTag(0);

        }

        return MenuBarLayout;
    }

    private MenuItemsLayout getMenuItemsLayout() {

        if ( mMenuItemsLayout == null ) {
            mMenuItemsLayout = new MenuItemsLayout(this);
            mMenuItemsLayout.setOnClickListener(this);
            getRootLayout().addView(mMenuItemsLayout);
        }

        return mMenuItemsLayout;
    }

    protected RelativeLayout getContentLayout() {

        if ( ContentLayout == null ) {

            ContentLayout = new RelativeLayout(this);
            ContentLayout.setId(ViewHelper.generateViewId());
            ContentLayout.setBackgroundColor(getResources().getColor(R.color.activity_bg));

            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            lp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);

            ContentLayout.setLayoutParams(lp);
            getRootLayout().addView(ContentLayout);
        }

        return ContentLayout;
    }

    protected ViewGroup getContentView() {
        return Content;
    }

    @Override
    public void setContentView(int layoutResID) {

        if ( Content != null ) {
            getContentLayout().removeView(Content);
            Content = null;
        }

        Content = (ViewGroup)Inflate(layoutResID, getContentLayout());

    }

    public void showMenuButton() {
        getMenuBarLayout();
        MenuButton.setVisibility(View.VISIBLE);
        GroupButton.setVisibility(View.VISIBLE);
    }

    public void hideMenuButton() {
        getMenuBarLayout();
        MenuButton.setVisibility(View.GONE);
        GroupButton.setVisibility(View.GONE);
    }

    protected void onGroupButtonTouch(boolean On) {}

    public boolean menuIsVisible() {
        return getMenuItemsLayout().getVisibility() == View.VISIBLE;
    }

    private void showHideMenu(boolean Show, boolean Animated) {

        if ( Show && menuIsVisible() ) return;
        if ( !Show && !menuIsVisible() ) return;

        if ( Show ) {

            if ( Anim ) return;

            getMenuItemsLayout().setButtonsAvailable(MenuItemsLayout.BTN_ALL);
            getMenuItemsLayout().setY(getMenuItemsLayout().getBtnAreaHeight() * -1
                    + getMenuBarLayout().getHeight() );
            getMenuItemsLayout().setVisibility(View.VISIBLE);
            getMenuItemsLayout().bringToFront();
            getMenuBarLayout().bringToFront();

            if ( Animated ) {

                Anim = true;

                getMenuItemsLayout()
                        .animate()
                        .translationY(getMenuBarLayout().getHeight())
                        .setDuration(200)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                Anim = false;
                            }
                        });
            } else {
                getMenuItemsLayout().setTop(getMenuBarLayout().getHeight());
            }

        } else {

            if ( Animated ) {

                if ( Anim ) return;
                Anim = true;

                getMenuItemsLayout()
                        .animate()
                        .translationY(getMenuItemsLayout().getBtnAreaHeight() * -1
                                + getMenuBarLayout().getHeight())
                        .setDuration(200)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                getMenuItemsLayout().setVisibility(View.GONE);
                                Anim = false;
                            }
                        });
            } else {
                getMenuItemsLayout().setVisibility(View.GONE);
            }

        }


    }

    public void showMenuBar() {

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        lp.addRule(RelativeLayout.BELOW, getMenuBarLayout().getId());
        getContentLayout().setLayoutParams(lp);

        if ( MenuBarLayout != null )
            MenuBarLayout.setVisibility(View.VISIBLE);
    }

    public void hideMenuBar() {

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        lp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        getContentLayout().setLayoutParams(lp);

        if ( MenuBarLayout != null )
            MenuBarLayout.setVisibility(View.GONE);
    }

    public void showMenu(boolean Animated) {
        showHideMenu(true, Animated);

    }

    public void hideMenu(boolean Animated) {
        showHideMenu(false, Animated);
    }

    public void openHomepage() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.homepage_url)));
        startActivity(browserIntent);
    }

    public void openForumpage() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.forumpage_url)));
        startActivity(browserIntent);
    }

    public void donate() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.donate_url)));
        startActivity(browserIntent);
    }

    public void addDevice() {

        showAddWizard();
    }


    private static void showActivity(Activity sender,  Class<?> cls, int flags) {

        Intent i = new Intent(sender.getBaseContext(), cls);
        i.setFlags(flags == 0 ? Intent.FLAG_ACTIVITY_REORDER_TO_FRONT : flags);
        i.putExtra(INTENTSENDER, sender instanceof MainActivity ? INTENTSENDER_MAIN : "");
        sender.startActivity(i);

        sender.overridePendingTransition( R.anim.fade_in, R.anim.fade_out);
    }

    public static void showMain(Activity sender) {

        SuplaClient client = SuplaApp.getApp().getSuplaClient();

        if ( client != null
                && client.Registered() ) {

            showActivity(sender, MainActivity.class, 0);

        } else {
            showStatus(sender);
        }



    }

    public static void showStatus(Activity sender) {
        showActivity(sender, StatusActivity.class, 0);
    }

    public static void showCfg(Activity sender) {
        showActivity(sender, CfgActivity.class, 0);
    }

    public void showAbout() {
        showActivity(this, AboutActivity.class, 0);
    }

    public void showCreateAccount() {
        showActivity(this, CreateAccountActivity.class, 0);
    }

    public void showAddWizard() {
        showActivity(this, AddWizardActivity.class, 0);
    }

    public void gotoMain() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {

        if (  v != MenuButton
              && menuIsVisible() ) {

            hideMenu(true);
        }

        if ( v == MenuButton )  {

            if ( menuIsVisible() )
                hideMenu(true);
            else
                showMenu(true);

        } else if ( v == GroupButton ) {

            int img;

            if (GroupButton.getTag() == Integer.valueOf(0)) {
                GroupButton.setTag(1);
                img = R.drawable.groupon;
            } else {
                GroupButton.setTag(0);
                img = R.drawable.groupoff;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                GroupButton.setBackground(getResources().getDrawable(img));
            } else {
                GroupButton.setBackgroundDrawable(getResources().getDrawable(img));
            }

            onGroupButtonTouch(img == R.drawable.groupon);
        } else {
            switch (MenuItemsLayout.getButtonId(v)) {
                case MenuItemsLayout.BTN_SETTINGS:
                    showCfg(this);
                    break;
                case MenuItemsLayout.BTN_ABOUT:
                    showAbout();
                    break;
                case MenuItemsLayout.BTN_ADD_DEVICE:
                    addDevice();
                    break;
                case MenuItemsLayout.BTN_DONATE:
                    donate();
                    break;
                case MenuItemsLayout.BTN_HELP:
                    openForumpage();
                    break;
                case MenuItemsLayout.BTN_HOMEPAGE:
                    openHomepage();
                    break;
            }
        }



    }


    @Override
    protected void BeforeStatusMsg() {
        super.BeforeStatusMsg();

        if (  CurrentActivity != null
                && !(CurrentActivity instanceof StatusActivity)
                && !(CurrentActivity instanceof CfgActivity)
                && !(CurrentActivity instanceof AddWizardActivity )
                && !(CurrentActivity instanceof CreateAccountActivity )) {
            showStatus(this);
        }
    }

}
