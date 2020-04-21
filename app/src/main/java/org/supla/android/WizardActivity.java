package org.supla.android;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

@SuppressLint("Registered")
public abstract class WizardActivity extends NavigationActivity {

    private Button mBtnNextRightPart;
    private Button mBtnNextMiddlePart;
    private Button mBtnNextLeftPart;
    private Timer mBtnNextPreloaderTimer;
    private short mBtnNextPreloaderPos;

    private RelativeLayout mContent;
    private ArrayList<View> mPages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_wizard);

        mBtnNextRightPart = findViewById(R.id.wizard_next_right_part);
        mBtnNextRightPart.setOnClickListener(this);

        mBtnNextMiddlePart = findViewById(R.id.wizard_next_middle_part);
        mBtnNextMiddlePart.setOnClickListener(this);
        mBtnNextMiddlePart.setTypeface(SuplaApp.getApp().getTypefaceQuicksandRegular());

        mBtnNextLeftPart = findViewById(R.id.wizard_next_left_part);
        mBtnNextLeftPart.setOnClickListener(this);

        mContent = findViewById(R.id.wizard_content);
    }

    protected View addStepPage(int layoutResId, int pageId) {
        View stepPage = Inflate(layoutResId, null);
        if (stepPage != null) {
            stepPage.setVisibility(View.GONE);
            stepPage.setTag(pageId);
            mContent.addView(stepPage);
            mPages.add(stepPage);

            if (stepPage instanceof RelativeLayout) {
                stepPage.setLayoutParams(
                        new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                                RelativeLayout.LayoutParams.MATCH_PARENT));
            }
        }
        return stepPage;
    }

    protected void showPage(int pageId) {
        for (View page : mPages) {
            Integer i = pageId;
            page.setVisibility(page.getTag() == i ? View.VISIBLE : View.GONE);
        }
    }

    protected int getVisiblePageId() {
        for (View page : mPages) {
            if (page.getVisibility() == View.VISIBLE) {
                return (Integer)page.getTag();
            }
        }

        return 0;
    }

    protected void setBtnNextEnabled(boolean enabled) {
        mBtnNextLeftPart.setEnabled(enabled);
        mBtnNextMiddlePart.setEnabled(enabled);
        mBtnNextRightPart.setEnabled(enabled);
    }

    protected void setBtnNextText(int resId) {
        mBtnNextMiddlePart.setText(resId, TextView.BufferType.NORMAL);
    }

    protected boolean isBtnNextPreloaderVisible() {
        return mBtnNextPreloaderTimer != null;
    }

    protected void setBtnNextPreloaderVisible(boolean visible) {

        if (mBtnNextPreloaderTimer != null) {
            mBtnNextPreloaderPos = -1;
            mBtnNextPreloaderTimer.cancel();
            mBtnNextPreloaderTimer = null;
        }


        if (visible) {

            mBtnNextRightPart.setBackgroundResource(R.drawable.btnnextr2);

            ViewGroup.LayoutParams params = mBtnNextRightPart.getLayoutParams();
            params.width = getResources().getDimensionPixelSize(R.dimen.wizard_btnnextl_width);

            mBtnNextRightPart.setLayoutParams(params);

            mBtnNextPreloaderPos = 0;

            mBtnNextPreloaderTimer = new Timer();
            mBtnNextPreloaderTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {

                    runOnUiThread(new Runnable() {
                        public void run() {

                            if (mBtnNextPreloaderPos == -1) {
                                return;
                            }

                            String txt = "";

                            for (int a = 0; a < 10; a++) {
                                txt += mBtnNextPreloaderPos == a ? "|" : ".";
                            }


                            mBtnNextPreloaderPos++;
                            if (mBtnNextPreloaderPos > 9) {
                                mBtnNextPreloaderPos = 0;
                            }


                            mBtnNextMiddlePart.setText(txt, TextView.BufferType.NORMAL);


                        }
                    });
                }
            }, 0, 100);

        } else {

            mBtnNextRightPart.setBackgroundResource(R.drawable.btnnextr);
            ViewGroup.LayoutParams params = mBtnNextRightPart.getLayoutParams();
            params.width = getResources().getDimensionPixelSize(R.dimen.wizard_btnnextr_width);
            mBtnNextRightPart.setLayoutParams(params);

        }

    }


    @Override
    public void onClick(View v) {
        super.onClick(v);

        if (v == mBtnNextLeftPart
                || v == mBtnNextMiddlePart
                || v == mBtnNextRightPart) {
            onBtnNextClick();
        }
    }

    protected abstract void onBtnNextClick();
}
