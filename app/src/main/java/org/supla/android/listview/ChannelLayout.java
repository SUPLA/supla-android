package org.supla.android.listview;

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
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.supla.android.images.ImageCache;
import org.supla.android.R;
import org.supla.android.SuplaChannelStatus;
import org.supla.android.ViewHelper;
import org.supla.android.db.ChannelBase;
import org.supla.android.db.ChannelGroup;
import org.supla.android.images.ImageId;
import org.supla.android.lib.SuplaConst;

public class ChannelLayout extends LinearLayout {


    private int mID;
    private int mFunc;
    private boolean mGroup;

    private ChannelListView mParentListView;
    private RelativeLayout content;
    private FrameLayout right_btn;
    private FrameLayout left_btn;

    private ChannelImageLayout imgl;

    private TextView left_btn_text;
    private TextView right_btn_text;
    private CaptionView caption_text;

    private SuplaChannelStatus right_onlineStatus;
    private SuplaChannelStatus right_ActiveStatus;
    private SuplaChannelStatus left_onlineStatus;

    private LineView bottom_line;

    private boolean Anim;

    private boolean RightButtonEnabled;
    private boolean LeftButtonEnabled;
    private boolean DetailSliderEnabled;


    private class AnimParams {
        public int content_left;
        public int content_right;
        public int left_btn_rotation;
        public int left_btn_left;
        public int left_btn_right;
    }


    private class CaptionView extends android.support.v7.widget.AppCompatTextView {


        public CaptionView(Context context, int imgl_id) {
            super(context);


            Typeface type = Typeface.createFromAsset(context.getAssets(), "fonts/OpenSans-Bold.ttf");
            setTypeface(type);
            setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.channel_caption_text));
            setTextColor(getResources().getColor(R.color.channel_caption_text));
            setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);

            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

            if (imgl_id != -1)
                lp.addRule(RelativeLayout.BELOW, imgl_id);

            lp.topMargin = getResources().getDimensionPixelSize(R.dimen.channel_caption_top_margin);
            setLayoutParams(lp);
        }

    }

    private TextView newTextView(Context context) {

        TextView tv = new TextView(context);
        tv.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        Typeface type = Typeface.createFromAsset(context.getAssets(), "fonts/Quicksand-Regular.ttf");

        tv.setTypeface(type);

        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.channel_btn_text));
        tv.setTextColor(getResources().getColor(R.color.channel_btn_text));
        tv.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);

        return tv;
    }


    private class ChannelImageLayout extends RelativeLayout {

        private ImageView Img1;
        private ImageView Img2;
        private ImageId Img1Id;
        private ImageId Img2Id;
        private TextView Text1;
        private TextView Text2;

        public ChannelImageLayout(Context context) {
            super(context);

            setId(ViewHelper.generateViewId());
            mFunc = 0;

            Img1 = newImageView(context);
            Img2 = newImageView(context);

            Text1 = newTextView(context);
            Text2 = newTextView(context);

            SetDimensions();
        }

        private ImageView newImageView(Context context) {

            ImageView Img = new ImageView(context);
            Img.setId(ViewHelper.generateViewId());
            addView(Img);

            return Img;
        }

        private TextView newTextView(Context context) {

            TextView Text = new TextView(context);
            Text.setId(ViewHelper.generateViewId());

            Typeface type = Typeface.createFromAsset(context.getAssets(), "fonts/OpenSans-Regular.ttf");
            Text.setTypeface(type);
            Text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.channel_temp_text));
            Text.setTextColor(getResources().getColor(R.color.channel_temp_text));
            Text.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);

            addView(Text);

            return Text;
        }

        private void SetTextDimensions(TextView Text, ImageView Img, Boolean visible, int width, int height) {

            Text.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);

            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(width, height);

            lp.addRule(RelativeLayout.RIGHT_OF, Img.getId());

            Text.setLayoutParams(lp);
            Text.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);

        }

        private void SetTextDimensions(TextView Text, ImageView Img, Boolean visible) {

            SetTextDimensions(Text, Img, visible, getResources().getDimensionPixelSize(R.dimen.channel_imgtext_width), getResources().getDimensionPixelSize(R.dimen.channel_imgtext_height));

        }

        private void SetImgDimensions(ImageView Img, Boolean img2, int leftMargin, int width, int height) {

            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                    width, height);


            if (!img2) {
                lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            } else {
                lp.addRule(RelativeLayout.RIGHT_OF, Text1.getId());
            }

            lp.leftMargin = leftMargin;
            lp.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);

            Img.setLayoutParams(lp);

        }

        private void SetImgDimensions(ImageView Img, Boolean img2, int leftMargin) {
            SetImgDimensions(Img, img2, leftMargin, getResources().getDimensionPixelSize(R.dimen.channel_img_width), getResources().getDimensionPixelSize(R.dimen.channel_img_height));
        }

        private void SetDimensions() {

            int width = getResources().getDimensionPixelSize(R.dimen.channel_img_width);

            if (mFunc == SuplaConst.SUPLA_CHANNELFNC_THERMOMETER
                    || mFunc == SuplaConst.SUPLA_CHANNELFNC_WINDSENSOR
                    || mFunc == SuplaConst.SUPLA_CHANNELFNC_PRESSURESENSOR
                    || mFunc == SuplaConst.SUPLA_CHANNELFNC_RAINSENSOR
                    || mFunc == SuplaConst.SUPLA_CHANNELFNC_WEIGHTSENSOR) {

                width *= 2.5;

            } else if (mFunc == SuplaConst.SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE) {

                width *= 4.3;
            } else if (mFunc == SuplaConst.SUPLA_CHANNELFNC_DEPTHSENSOR
                    || mFunc == SuplaConst.SUPLA_CHANNELFNC_DISTANCESENSOR
                    || mFunc == SuplaConst.SUPLA_CHANNELFNC_ELECTRICITY_METER
                    || mFunc == SuplaConst.SUPLA_CHANNELFNC_GAS_METER
                    || mFunc == SuplaConst.SUPLA_CHANNELFNC_WATER_METER) {

                width *= 2.8;

            } else if (mFunc == SuplaConst.SUPLA_CHANNELFNC_THERMOSTAT
                    || mFunc == SuplaConst.SUPLA_CHANNELFNC_THERMOSTAT_HEATPOL_HOMEPLUS) {
                width *= 3;
            }

            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                    width, getResources().getDimensionPixelSize(R.dimen.channel_img_height));

            lp.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
            lp.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
            lp.setMargins(0, getResources().getDimensionPixelSize(R.dimen.channel_img_top_margin), 0, 0);

            setLayoutParams(lp);


            if (mFunc == SuplaConst.SUPLA_CHANNELFNC_DISTANCESENSOR) {

                RelativeLayout.LayoutParams _lp = new RelativeLayout.LayoutParams(
                        getResources().getDimensionPixelSize(R.dimen.channel_distanceimg_width), getResources().getDimensionPixelSize(R.dimen.channel_distanceimg_height));

                _lp.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
                _lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

                Img1.setLayoutParams(_lp);
                Img1.setVisibility(View.VISIBLE);


                _lp = new RelativeLayout.LayoutParams(getResources().getDimensionPixelSize(R.dimen.channel_distanceimgtext_width), getResources().getDimensionPixelSize(R.dimen.channel_distanceimgtext_height));

                _lp.addRule(RelativeLayout.ABOVE, Img1.getId());
                _lp.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);

                Text1.setLayoutParams(_lp);
                Text1.setVisibility(View.VISIBLE);
                Text1.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);


            } else if (mFunc == SuplaConst.SUPLA_CHANNELFNC_DEPTHSENSOR) {

                SetImgDimensions(Img1, false, 0);
                SetTextDimensions(Text1, Img1, true, getResources().getDimensionPixelSize(R.dimen.channel_depthimgtext_width), getResources().getDimensionPixelSize(R.dimen.channel_depthimgtext_height));

            } else if (mFunc == SuplaConst.SUPLA_CHANNELFNC_ELECTRICITY_METER
                    || mFunc == SuplaConst.SUPLA_CHANNELFNC_GAS_METER
                    || mFunc == SuplaConst.SUPLA_CHANNELFNC_WATER_METER) {

                SetImgDimensions(Img1, false, 0);
                SetTextDimensions(Text1, Img1, true, getResources().getDimensionPixelSize(R.dimen.channel_emimgtext_width), getResources().getDimensionPixelSize(R.dimen.channel_emimgtext_height));

            } else if (mFunc == SuplaConst.SUPLA_CHANNELFNC_WINDSENSOR) {

                SetImgDimensions(Img1, false, 0);
                SetTextDimensions(Text1, Img1, true, getResources().getDimensionPixelSize(R.dimen.channel_weathersensors_imgtext_width), getResources().getDimensionPixelSize(R.dimen.channel_weathersensors_imgtext_height));
                Text1.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);

            } else if (mFunc == SuplaConst.SUPLA_CHANNELFNC_PRESSURESENSOR) {

                SetImgDimensions(Img1, false, 0);
                SetTextDimensions(Text1, Img1, true, getResources().getDimensionPixelSize(R.dimen.channel_weathersensors_imgtext_width), getResources().getDimensionPixelSize(R.dimen.channel_weathersensors_imgtext_height));
                Text1.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);

            } else if (mFunc == SuplaConst.SUPLA_CHANNELFNC_RAINSENSOR) {

                SetImgDimensions(Img1, false, 0);
                SetTextDimensions(Text1, Img1, true, getResources().getDimensionPixelSize(R.dimen.channel_weathersensors_imgtext_width), getResources().getDimensionPixelSize(R.dimen.channel_weathersensors_imgtext_height));
                Text1.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);

            } else if (mFunc == SuplaConst.SUPLA_CHANNELFNC_WEIGHTSENSOR) {

                SetImgDimensions(Img1, false, 0);
                SetTextDimensions(Text1, Img1, true, getResources().getDimensionPixelSize(R.dimen.channel_weight_imgtext_width), getResources().getDimensionPixelSize(R.dimen.channel_weight_imgtext_height));
                Text1.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);

            } else if (mFunc == SuplaConst.SUPLA_CHANNELFNC_THERMOSTAT
                       || mFunc == SuplaConst.SUPLA_CHANNELFNC_THERMOSTAT_HEATPOL_HOMEPLUS) {

                SetImgDimensions(Img1, false, 0);
                SetTextDimensions(Text1, Img1, true, getResources().getDimensionPixelSize(R.dimen.channel_thimgtext_width), getResources().getDimensionPixelSize(R.dimen.channel_thimgtext_height));
                Text1.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
            } else {

                SetImgDimensions(Img1, false, mFunc == SuplaConst.SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE ? getResources().getDimensionPixelSize(R.dimen.channel_img_left_margin) : 0);
                SetTextDimensions(Text1, Img1, mFunc == SuplaConst.SUPLA_CHANNELFNC_THERMOMETER
                        || mFunc == SuplaConst.SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE);

                SetImgDimensions(Img2, true, 0);
                SetTextDimensions(Text2, Img2, mFunc == SuplaConst.SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE);

            }

        }

        public void setImage(ImageId img1Id, ImageId img2Id) {

            if (ImageId.equals(img1Id, Img1Id)
                    && ImageId.equals(img2Id, Img2Id)) {
                return;
            }

            Img1Id = img1Id;
            Img2Id = img2Id;

            if (Img1Id == null) {
                Img1.setVisibility(View.INVISIBLE);
            } else {
                Img1.setImageBitmap(ImageCache.getBitmap(getContext(), img1Id));
                Img1.setVisibility(View.VISIBLE);
            }

            if (Img2Id == null) {
                Img2.setVisibility(View.INVISIBLE);
            } else {
                Img2.setImageBitmap(ImageCache.getBitmap(getContext(), img2Id));
                Img2.setVisibility(View.VISIBLE);
            }

        }

        private void setText(CharSequence text, TextView tv) {
            if (text == null) {
                text = "";
            }
            if (!text.equals(tv.getText())) {
                tv.setText(text);
            }
        }

        public void setText1(CharSequence text) {
            setText(text, Text1);
        }

        public void setText2(CharSequence text) {
            setText(text, Text2);
        }

    }

    protected RelativeLayout.LayoutParams getOnlineStatusLayoutParams(boolean right) {

        int dot_size = getResources().getDimensionPixelSize(R.dimen.channel_dot_size);

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                mGroup ? dot_size / 2 : dot_size, mGroup ? dot_size * 2 : dot_size);

        int margin = getResources().getDimensionPixelSize(R.dimen.channel_dot_margin);

        if (right) {
            lp.rightMargin = margin;
            lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        } else {
            lp.leftMargin = margin;
            lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        }

        lp.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
        return lp;
    }

    protected SuplaChannelStatus newOnlineStatus(Context context, boolean right) {

        SuplaChannelStatus result = new SuplaChannelStatus(context);

        result.setLayoutParams(getOnlineStatusLayoutParams(right));
        result.setOfflineColor(getResources().getColor(R.color.channel_dot_off));
        result.setOnlineColor(getResources().getColor(R.color.channel_dot_on));

        return result;
    }

    public ChannelLayout(Context context, ChannelListView parentListView) {
        super(context);

        setOrientation(LinearLayout.HORIZONTAL);

        mParentListView = parentListView;
        setBackgroundColor(getResources().getColor(R.color.channel_cell));

        right_btn = new FrameLayout(context);
        left_btn = new FrameLayout(context);

        right_btn.setLayoutParams(new LayoutParams(
                getResources().getDimensionPixelSize(R.dimen.channel_layout_button_width), getResources().getDimensionPixelSize(R.dimen.channel_layout_height)));

        right_btn.setBackgroundColor(getResources().getColor(R.color.channel_btn));

        left_btn.setLayoutParams(new LayoutParams(
                getResources().getDimensionPixelSize(R.dimen.channel_layout_button_width), getResources().getDimensionPixelSize(R.dimen.channel_layout_height)));

        left_btn.setBackgroundColor(getResources().getColor(R.color.channel_btn));

        content = new RelativeLayout(context);
        content.setLayoutParams(new LayoutParams(
                LayoutParams.MATCH_PARENT, getResources().getDimensionPixelSize(R.dimen.channel_layout_height)));

        content.setBackgroundColor(getResources().getColor(R.color.channel_cell));

        addView(content);
        addView(left_btn);
        addView(right_btn);

        left_btn_text = newTextView(context);
        left_btn.addView(left_btn_text);

        right_btn_text = newTextView(context);
        right_btn.addView(right_btn_text);

        right_onlineStatus = newOnlineStatus(context, true);
        right_onlineStatus.setId(ViewHelper.generateViewId());
        content.addView(right_onlineStatus);
        left_onlineStatus = newOnlineStatus(context, false);
        content.addView(left_onlineStatus);

        right_ActiveStatus = new SuplaChannelStatus(context);
        right_ActiveStatus.setSingleColor(true);
        right_ActiveStatus.setOnlineColor(getResources().getColor(R.color.channel_dot_on));

        {
            int dot_size = getResources().getDimensionPixelSize(R.dimen.channel_dot_size);
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                    dot_size / 2 , dot_size * 2);

            lp.addRule(RelativeLayout.LEFT_OF, right_onlineStatus.getId());
            lp.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);

            right_ActiveStatus.setLayoutParams(lp);
        }

        right_ActiveStatus.setVisibility(View.GONE);
        content.addView(right_ActiveStatus);

        bottom_line = new LineView(context);
        content.addView(bottom_line);

        {
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                    getResources().getDimensionPixelSize(R.dimen.channel_img_width), getResources().getDimensionPixelSize(R.dimen.channel_img_height));

            lp.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
            lp.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
            lp.setMargins(0, getResources().getDimensionPixelSize(R.dimen.channel_img_top_margin), 0, 0);

            imgl = new ChannelImageLayout(context);
            content.addView(imgl);
        }

        caption_text = new CaptionView(context, imgl.getId());
        content.addView(caption_text);

        OnTouchListener tl = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {

                int action = event.getAction();

                if (action == MotionEvent.ACTION_DOWN)
                    onTouchDown(v);
                else if (action == MotionEvent.ACTION_UP)
                    onTouchUp(v);

                return true;
            }
        };

        left_btn.setOnTouchListener(tl);
        right_btn.setOnTouchListener(tl);

    }


    public ChannelLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChannelLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setLeftBtnText(String Text) {
        left_btn_text.setText(Text);
    }

    public void setRightBtnText(String Text) {
        right_btn_text.setText(Text);
    }


    public void Slide(int delta) {

        if (Anim)
            return;

        if (!LeftButtonEnabled
                && delta > 0 && content.getLeft() + delta > 0)
            delta = content.getLeft() * -1;

        if (!RightButtonEnabled
                && delta < 0 && content.getLeft() + delta < 0)
            delta = content.getLeft() * -1;

        content.layout(content.getLeft() + delta, content.getTop(), content.getWidth() + content.getLeft() + delta, content.getHeight());

        int bcolor = getResources().getColor(R.color.channel_btn);

        left_btn.setBackgroundColor(bcolor);
        right_btn.setBackgroundColor(bcolor);

        UpdateLeftBtn();
        UpdateRightBtn();

    }

    public void hideButtonImmediately() {
        if (Slided() > 0) {
            Slide(content.getLeft() * -1);
        }
    }

    public boolean Sliding() {

        if (Anim)
            return true;

        if (content.getLeft() > 0
                && content.getLeft() != left_btn.getWidth())
            return true;

        return content.getLeft() < 0
                && Math.abs(content.getLeft()) != right_btn.getWidth();

    }

    public int Slided() {

        if (Anim)
            return 10;

        if (content.getLeft() > 0)
            return 1;

        if (content.getLeft() < 0)
            return 2;

        return 0;
    }

    private void UpdateLeftBtn() {

        float pr = content.getLeft() * 100 / left_btn.getWidth();

        if (pr <= 0) pr = 0;
        else if (pr > 100) pr = 100;

        left_btn.setRotationY(90 - 90 * pr / 100);

        int left = content.getLeft() / 2 - left_btn.getWidth() / 2;
        int right = left_btn.getWidth() + (content.getLeft() / 2 - left_btn.getWidth() / 2);

        if (left > 0) left = 0;
        if (right > left_btn.getWidth()) right = left_btn.getWidth();

        left_btn.layout(left, 0, right, left_btn.getHeight());

    }

    private void onTouchUpDown(boolean up, View v) {

        if (mParentListView != null
                && mParentListView.getOnChannelButtonTouchListener() != null) {
            mParentListView.getOnChannelButtonTouchListener().onChannelButtonTouch(mParentListView, v == left_btn, up, mID, mFunc);
        }

    }

    private void onTouchDown(View v) {

        if (v == left_btn || v == right_btn) {
            v.setBackgroundColor(getResources().getColor(R.color.channel_btn_pressed));
        }


        onTouchUpDown(false, v);
    }

    private void onTouchUp(View v) {

        if (v == left_btn || v == right_btn) {

            final View _v = v;

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    _v.setBackgroundColor(getResources().getColor(R.color.channel_btn));
                }
            }, 200);

        }


        onTouchUpDown(true, v);
    }

    private void UpdateRightBtn() {

        float pr = (content.getLeft() * -1) * 100 / right_btn.getWidth();

        if (pr <= 0) pr = 0;
        else if (pr > 100) pr = 100;

        right_btn.setRotationY(-90 + 90 * pr / 100);

        int left = getWidth() + (content.getLeft() / 2 - right_btn.getWidth() / 2);

        if (content.getLeft() * -1 > right_btn.getWidth())
            left = getWidth() - right_btn.getWidth();

        right_btn.layout(left, 0, left + right_btn.getWidth(), right_btn.getHeight());

    }

    public void AnimateToRestingPosition(boolean start_pos) {

        if (!start_pos
                && Anim) return;


        ObjectAnimator btn_animr = null;
        ObjectAnimator btn_animx = null;
        ObjectAnimator content_animx = null;

        final AnimParams params = new AnimParams();

        params.left_btn_rotation = 90;
        params.left_btn_left = left_btn.getWidth() * -1;
        params.left_btn_right = 0;

        if (content.getLeft() > 0) {

            if (!start_pos
                    && content.getLeft() >= left_btn.getWidth() / 2) {

                params.content_left = left_btn.getWidth();
                params.content_right = getWidth() + left_btn.getWidth();

                btn_animr = ObjectAnimator.ofFloat(left_btn, "RotationY", left_btn.getRotationY(), 0f);
                btn_animx = ObjectAnimator.ofFloat(left_btn, "x", left_btn.getLeft(), 0);
                content_animx = ObjectAnimator.ofFloat(content, "x", content.getLeft(), params.content_left);

            } else {

                params.content_left = 0;
                params.content_right = content.getWidth();

                btn_animr = ObjectAnimator.ofFloat(left_btn, "RotationY", left_btn.getRotationY(), 90f);
                btn_animx = ObjectAnimator.ofFloat(left_btn, "x", left_btn.getLeft(), left_btn.getWidth() / 2 * -1);
                content_animx = ObjectAnimator.ofFloat(content, "x", content.getLeft(), 0f);

            }

        } else if (content.getLeft() < 0) {

            if (!start_pos
                    && content.getLeft() * -1 >= right_btn.getWidth() / 2) {

                params.content_left = right_btn.getWidth() * -1;
                params.content_right = getWidth() - right_btn.getWidth();

                btn_animr = ObjectAnimator.ofFloat(right_btn, "RotationY", right_btn.getRotationY(), 0f);
                btn_animx = ObjectAnimator.ofFloat(right_btn, "x", right_btn.getLeft(), params.content_right);
                content_animx = ObjectAnimator.ofFloat(content, "x", content.getLeft(), params.content_left);

            } else {

                params.content_left = 0;
                params.content_right = content.getWidth();

                btn_animr = ObjectAnimator.ofFloat(right_btn, "RotationY", right_btn.getRotationY(), -90f);
                btn_animx = ObjectAnimator.ofFloat(right_btn, "x", right_btn.getLeft(), getWidth() + right_btn.getWidth() / 2);
                content_animx = ObjectAnimator.ofFloat(content, "x", content.getLeft(), 0f);

            }

        }


        if (content_animx != null) {

            AnimatorSet as = new AnimatorSet();
            as.playTogether(btn_animr, btn_animx, content_animx);
            as.setDuration(200);

            as.addListener(new Animator.AnimatorListener() {


                @Override
                public void onAnimationStart(Animator animation) {
                    Anim = true;
                }

                @Override
                public void onAnimationEnd(Animator animation) {

                    content.setTranslationX(0);
                    content.layout(params.content_left, content.getTop(), params.content_right, getWidth());

                    left_btn.setTranslationX(0);
                    right_btn.setTranslationX(0);
                    UpdateLeftBtn();
                    UpdateRightBtn();
                    Anim = false;

                    if (mParentListView != null)
                        mParentListView.onSlideAnimationEnd();
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    onAnimationEnd(animation);
                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });

            as.start();
        }

    }


    private void setRightButtonEnabled(boolean rightButtonEnabled) {

        if (RightButtonEnabled != rightButtonEnabled) {
            AnimateToRestingPosition(true);
            RightButtonEnabled = rightButtonEnabled;
        }

    }


    public boolean getRightButtonEnabled() {
        return RightButtonEnabled;
    }


    private void setLeftButtonEnabled(boolean leftButtonEnabled) {

        if (LeftButtonEnabled != leftButtonEnabled) {
            AnimateToRestingPosition(true);
            LeftButtonEnabled = leftButtonEnabled;
        }

    }

    private void setDetailSliderEnabled(boolean detailSliderEnabled) {

        if (detailSliderEnabled) {
            setRightButtonEnabled(false);
        }

        DetailSliderEnabled = detailSliderEnabled;

    }

    public boolean getDetailSliderEnabled() {

        return DetailSliderEnabled;
    }

    public boolean getButtonsEnabled() {
        return LeftButtonEnabled || RightButtonEnabled;
    }


    public String getCaption() {
        return caption_text.getText().toString();
    }

    public void setBackgroundColor(int color) {

        super.setBackgroundColor(color);

        if (content != null)
            content.setBackgroundColor(color);
    }


    public void setChannelData(ChannelBase cbase) {

        int OldFunc = mFunc;
        mFunc = cbase.getFunc();
        mID = cbase.getRemoteId();
        boolean OldGroup = mGroup;
        mGroup = cbase instanceof ChannelGroup;

        imgl.setImage(cbase.getImageIdx(ChannelBase.WhichOne.First),
                cbase.getImageIdx(ChannelBase.WhichOne.Second));

        if (OldFunc != mFunc) {
            imgl.SetDimensions();
        }

        {

            SuplaChannelStatus.ShapeType shapeType = mGroup ?
                    SuplaChannelStatus.ShapeType.LinearVertical : SuplaChannelStatus.ShapeType.Dot;

            if (mGroup != OldGroup) {
                left_onlineStatus.setLayoutParams(getOnlineStatusLayoutParams(false));
                right_onlineStatus.setLayoutParams(getOnlineStatusLayoutParams(true));
            }

            left_onlineStatus.setPercent(cbase.getOnLinePercent());
            left_onlineStatus.setShapeType(shapeType);
            right_onlineStatus.setPercent(cbase.getOnLinePercent());
            right_onlineStatus.setShapeType(shapeType);
        }

        if (mGroup) {
            right_ActiveStatus.setVisibility(View.VISIBLE);
            right_ActiveStatus.setPercent(((ChannelGroup)cbase).getActivePercent());
        } else {
            right_ActiveStatus.setVisibility(View.GONE);
        }

        {
            int lidx = -1;
            int ridx = -1;

            switch (mFunc) {
                case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEDOORLOCK:
                case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGATEWAYLOCK:

                    ridx = R.string.channel_btn_open;
                    break;

                case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGATE:
                case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGARAGEDOOR:

                    ridx = R.string.channel_btn_openclose;
                    break;

                /*
                case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER:

                    ridx = R.string.channel_btn_reveal;
                    lidx = R.string.channel_btn_shut;
                    break;
                */

                case SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH:
                case SuplaConst.SUPLA_CHANNELFNC_LIGHTSWITCH:
                case SuplaConst.SUPLA_CHANNELFNC_STAIRCASETIMER:

                    ridx = R.string.channel_btn_on;
                    lidx = R.string.channel_btn_off;
                    break;

            }

            setRightBtnText(ridx == -1 ? "" : getResources().getString(ridx));
            setLeftBtnText(lidx == -1 ? "" : getResources().getString(lidx));
        }

        {
            boolean lenabled = false;
            boolean renabled = false;
            boolean dslider = false;

            switch (mFunc) {
                case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGATE:
                case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGARAGEDOOR:
                case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEDOORLOCK:
                case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGATEWAYLOCK:

                    left_onlineStatus.setVisibility(View.INVISIBLE);
                    right_onlineStatus.setVisibility(View.VISIBLE);

                    renabled = true;

                    break;
                case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER:

                    left_onlineStatus.setVisibility(View.INVISIBLE);
                    right_onlineStatus.setVisibility(View.VISIBLE);
                    dslider = true;

                    break;
                case SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH:
                case SuplaConst.SUPLA_CHANNELFNC_LIGHTSWITCH:
                case SuplaConst.SUPLA_CHANNELFNC_STAIRCASETIMER:

                    left_onlineStatus.setVisibility(View.VISIBLE);
                    right_onlineStatus.setVisibility(View.VISIBLE);

                    lenabled = true;
                    renabled = true;
                    break;

                case SuplaConst.SUPLA_CHANNELFNC_NOLIQUIDSENSOR:
                case SuplaConst.SUPLA_CHANNELFNC_OPENSENSOR_DOOR:
                case SuplaConst.SUPLA_CHANNELFNC_OPENSENSOR_GARAGEDOOR:
                case SuplaConst.SUPLA_CHANNELFNC_OPENSENSOR_GATE:
                case SuplaConst.SUPLA_CHANNELFNC_OPENSENSOR_GATEWAY:
                case SuplaConst.SUPLA_CHANNELFNC_OPENSENSOR_ROLLERSHUTTER:
                case SuplaConst.SUPLA_CHANNELFNC_OPENINGSENSOR_WINDOW:
                case SuplaConst.SUPLA_CHANNELFNC_MAILSENSOR:

                    left_onlineStatus.setVisibility(View.VISIBLE);
                    left_onlineStatus.setShapeType(SuplaChannelStatus.ShapeType.Ring);
                    right_onlineStatus.setVisibility(View.VISIBLE);
                    right_onlineStatus.setShapeType(SuplaChannelStatus.ShapeType.Ring);

                    break;

                case SuplaConst.SUPLA_CHANNELFNC_RGBLIGHTING:
                case SuplaConst.SUPLA_CHANNELFNC_DIMMER:
                case SuplaConst.SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING:
                case SuplaConst.SUPLA_CHANNELFNC_ELECTRICITY_METER:
                case SuplaConst.SUPLA_CHANNELFNC_GAS_METER:
                case SuplaConst.SUPLA_CHANNELFNC_WATER_METER:
                case SuplaConst.SUPLA_CHANNELFNC_THERMOMETER:
                case SuplaConst.SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE:
                case SuplaConst.SUPLA_CHANNELFNC_THERMOSTAT:
                case SuplaConst.SUPLA_CHANNELFNC_THERMOSTAT_HEATPOL_HOMEPLUS:

                    left_onlineStatus.setVisibility(View.INVISIBLE);
                    right_onlineStatus.setVisibility(View.VISIBLE);
                    dslider = true;

                    break;

                default:

                    left_onlineStatus.setVisibility(View.INVISIBLE);
                    right_onlineStatus.setVisibility(View.INVISIBLE);


                    break;
            }

            setLeftButtonEnabled(lenabled && cbase.getOnLine());
            setRightButtonEnabled(renabled && cbase.getOnLine());
            setDetailSliderEnabled(dslider && cbase.getOnLine());


        }
        caption_text.setText(cbase.getNotEmptyCaption(getContext()));

        imgl.setText1(cbase.getHumanReadableValue());
        imgl.setText2(cbase.getHumanReadableValue(ChannelBase.WhichOne.Second));

    }
}
