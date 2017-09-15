package org.supla.android.listview;

/*
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

 Author: Przemyslaw Zygmunt przemek@supla.org
 */


import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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

import org.supla.android.R;
import org.supla.android.ViewHelper;
import org.supla.android.db.Channel;
import org.supla.android.lib.SuplaConst;


public class ChannelLayout extends LinearLayout {

    private int ChannelID;
    private int Func;

    private ChannelListView ParentListView;
    private RelativeLayout content;
    private FrameLayout right_btn;
    private FrameLayout left_btn;

    private ChannelImageLayout  imgl;

    private TextView left_btn_text;
    private TextView right_btn_text;
    private CaptionView caption_text;

    private DotView right_dot;
    private DotView left_dot;

    private LineView bottom_line;

    private boolean Anim;

    private boolean RightButtonEnabled;
    private boolean LeftButtonEnabled;
    private boolean DetailSliderEnabled;


    private class AnimParams  {
        public int content_left;
        public int content_right;
        public int left_btn_rotation;
        public int left_btn_left;
        public int left_btn_right;
    }

    private class DotView extends View {

        private Paint paint;
        private boolean On;

        public DotView(Context context, boolean right) {
            super(context);

            int size = getResources().getDimensionPixelSize(R.dimen.channel_dot_size);
            paint = new Paint();
            paint.setFlags(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(getResources().getColor(R.color.channel_dot_off));
            paint.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    (float) 1, getResources().getDisplayMetrics()));
            paint.setStyle(Paint.Style.FILL);

            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                    size, size);

            int margin = getResources().getDimensionPixelSize(R.dimen.channel_dot_margin);

            if ( right ) {
                lp.rightMargin = margin;
                lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
            } else {
                lp.leftMargin = margin;
                lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
            }

            lp.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);

            setLayoutParams(lp);

        }

        public void setRing(boolean ring) {
            paint.setStyle(ring ? Paint.Style.STROKE : Paint.Style.FILL);
            invalidate();
        }

        public boolean getRing() {
            return paint.getStyle() == Paint.Style.STROKE;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            canvas.drawColor(Color.TRANSPARENT);
            canvas.drawCircle(getWidth() / 2, getHeight() / 2, getHeight() / 2 - (getRing() ? paint.getStrokeWidth()/2 : 0), paint);

        }


        public void setOn(boolean on) {
            paint.setColor(getResources().getColor(on ? R.color.channel_dot_on : R.color.channel_dot_off));
            invalidate();
        }

    }

    private class CaptionView extends TextView {


        public CaptionView(Context context, int imgl_id) {
            super(context);


            Typeface type = Typeface.createFromAsset(context.getAssets(),"fonts/OpenSans-Bold.ttf");
            setTypeface(type);
            setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.channel_caption_text));
            setTextColor(getResources().getColor(R.color.channel_caption_text));
            setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);

            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

            if ( imgl_id != -1 )
               lp.addRule(RelativeLayout.BELOW, imgl_id);

            lp.topMargin = getResources().getDimensionPixelSize(R.dimen.channel_caption_top_margin);
            setLayoutParams(lp);
        }

    }

    private TextView newTextView(Context context) {

        TextView tv = new TextView(context);
        tv.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        Typeface type = Typeface.createFromAsset(context.getAssets(),"fonts/Quicksand-Regular.ttf");

        tv.setTypeface(type);

        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.channel_btn_text));
        tv.setTextColor(getResources().getColor(R.color.channel_btn_text));
        tv.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);

        return tv;
    }

    public static int getImageIdx(int StateUp, int func, int img) {

        int img_idx = -1;

        if ( img != 1
                && func != SuplaConst.SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE )
            return img_idx;

        switch(func) {
            case SuplaConst.SUPLA_CHANNELFNC_OPENSENSOR_GATEWAY:
            case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGATEWAYLOCK:
                img_idx = StateUp == 1 ? R.drawable.gatewayclosed : R.drawable.gatewayopen;
                break;
            case SuplaConst.SUPLA_CHANNELFNC_OPENSENSOR_GATE:
            case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGATE:
                img_idx = StateUp == 1 ? R.drawable.gateclosed : R.drawable.gateopen;
                break;
            case SuplaConst.SUPLA_CHANNELFNC_OPENSENSOR_GARAGEDOOR:
            case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGARAGEDOOR:
                img_idx = StateUp == 1 ? R.drawable.garagedoorclosed : R.drawable.garagedooropen;
                break;
            case SuplaConst.SUPLA_CHANNELFNC_OPENSENSOR_DOOR:
            case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEDOORLOCK:
                img_idx = StateUp == 1 ? R.drawable.doorclosed : R.drawable.dooropen;
                break;
            case SuplaConst.SUPLA_CHANNELFNC_OPENSENSOR_ROLLERSHUTTER:
            case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER:
                img_idx = StateUp == 1 ? R.drawable.rollershutterclosed : R.drawable.rollershutteropen;
                break;
            case SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH:
                img_idx = StateUp == 1 ? R.drawable.poweron : R.drawable.poweroff;
                break;
            case SuplaConst.SUPLA_CHANNELFNC_LIGHTSWITCH:
                img_idx = StateUp == 1 ? R.drawable.lighton : R.drawable.lightoff;
                break;
            case SuplaConst.SUPLA_CHANNELFNC_THERMOMETER:
                img_idx = R.drawable.thermometer;
                break;
            case SuplaConst.SUPLA_CHANNELFNC_HUMIDITY:
                img_idx = R.drawable.humidity;
                break;
            case SuplaConst.SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE:
                img_idx = img == 1 ? R.drawable.thermometer : R.drawable.humidity;
                break;
            case SuplaConst.SUPLA_CHANNELFNC_NOLIQUIDSENSOR:
                img_idx = StateUp == 1 ? R.drawable.liquid : R.drawable.noliquid;
                break;
            case SuplaConst.SUPLA_CHANNELFNC_DIMMER:
                img_idx = StateUp == 1 ? R.drawable.dimmeron : R.drawable.dimmeroff;
                break;
            case SuplaConst.SUPLA_CHANNELFNC_RGBLIGHTING:
                img_idx = StateUp == 1 ? R.drawable.rgbon : R.drawable.rgboff;
                break;
            case SuplaConst.SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING:

                switch(StateUp) {
                    case 0:
                        img_idx = R.drawable.dimmerrgboffoff;
                        break;
                    case 1:
                        img_idx = R.drawable.dimmerrgbonoff;
                        break;
                    case 2:
                        img_idx = R.drawable.dimmerrgboffon;
                        break;
                    case 3:
                        img_idx = R.drawable.dimmerrgbonon;
                        break;
                }

                break;

            case SuplaConst.SUPLA_CHANNELFNC_DEPTHSENSOR:
                img_idx = R.drawable.depthsensor;
                break;

            case SuplaConst.SUPLA_CHANNELFNC_DISTANCESENSOR:
                img_idx = R.drawable.distancesensor;
                break;
        }

        return img_idx;
    }

    private class ChannelImageLayout extends RelativeLayout {

        private ImageView Img1;
        private ImageView Img2;
        private TextView Text1;
        private TextView Text2;
        private int Func;
        private int StateUp;


        public ChannelImageLayout(Context context) {
            super(context);

            setId(ViewHelper.generateViewId());
            Func = 0;

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

            return  Img;
        }

        private TextView newTextView(Context context) {

            TextView Text = new TextView(context);
            Text.setId(ViewHelper.generateViewId());

            Typeface type = Typeface.createFromAsset(context.getAssets(),"fonts/OpenSans-Regular.ttf");
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
                lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT );
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

            if ( Func == SuplaConst.SUPLA_CHANNELFNC_THERMOMETER ) {

                width*=2.5;

            } else if ( Func == SuplaConst.SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE ) {

                width*=4.3;
            } else if ( Func == SuplaConst.SUPLA_CHANNELFNC_DEPTHSENSOR
                        || Func == SuplaConst.SUPLA_CHANNELFNC_DISTANCESENSOR  ) {

                width*=2.8;

            }

            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                    width, getResources().getDimensionPixelSize(R.dimen.channel_img_height));

            lp.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
            lp.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
            lp.setMargins(0, getResources().getDimensionPixelSize(R.dimen.channel_img_top_margin), 0, 0);

            setLayoutParams(lp);




            if ( Func == SuplaConst.SUPLA_CHANNELFNC_DISTANCESENSOR ) {

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


            } else if ( Func == SuplaConst.SUPLA_CHANNELFNC_DEPTHSENSOR ) {

                SetImgDimensions(Img1, false, 0);
                SetTextDimensions(Text1, Img1, true, getResources().getDimensionPixelSize(R.dimen.channel_depthimgtext_width), getResources().getDimensionPixelSize(R.dimen.channel_depthimgtext_height));
            } else {

                SetImgDimensions(Img1, false, Func == SuplaConst.SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE ? getResources().getDimensionPixelSize(R.dimen.channel_img_left_margin) : 0);
                SetTextDimensions(Text1, Img1, Func == SuplaConst.SUPLA_CHANNELFNC_THERMOMETER
                        || Func == SuplaConst.SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE);

            }


            SetImgDimensions(Img2, true, 0);
            SetTextDimensions(Text2, Img2, Func == SuplaConst.SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE);

        }

        public void setFunc(int func, int stateUp) {

            if ( Func == func && StateUp == stateUp )
                return;

            StateUp = stateUp;

            int img1_idx = getImageIdx(StateUp, func, 1);

            if ( img1_idx == -1 ) {
                Img1.setVisibility(View.INVISIBLE);
            } else {
                Img1.setImageResource(img1_idx);
                Img1.setVisibility(View.VISIBLE);
            }

            int img2_idx = getImageIdx(StateUp, func, 2);

            if ( img2_idx == -1 ) {
                Img2.setVisibility(View.INVISIBLE);
            } else {
                Img2.setImageResource(img2_idx);
                Img2.setVisibility(View.VISIBLE);
            }

            if ( Func != func ) {
                Func = func;
                SetDimensions();
            }

        }

        public void setText1(String text) {
            Text1.setText(text);
        }

        public void setText2(String text) {
            Text2.setText(text);
        }

    }


    public ChannelLayout(Context context, ChannelListView parentListView) {
        super(context);

        setOrientation(LinearLayout.HORIZONTAL);

        ParentListView = parentListView;
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

        right_dot = new DotView(context, true);
        content.addView(right_dot);

        left_dot = new DotView(context, false);
        content.addView(left_dot);

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

                if ( action == MotionEvent.ACTION_DOWN )
                   onTouchDown(v);
                else if ( action == MotionEvent.ACTION_UP )
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

        if ( Anim )
            return;

        if (!LeftButtonEnabled
             && delta > 0 && content.getLeft()+delta > 0 )
            delta = content.getLeft()*-1;

        if (!RightButtonEnabled
             && delta < 0 && content.getLeft()+delta < 0 )
            delta = content.getLeft() * -1;

        content.layout(content.getLeft() + delta, content.getTop(), content.getWidth() + content.getLeft() + delta, content.getHeight());
        UpdateLeftBtn();
        UpdateRightBtn();

    }

    public boolean Sliding() {

        if (Anim)
            return true;

        if ( content.getLeft() > 0
                && content.getLeft() != left_btn.getWidth()  )
            return true;

        return content.getLeft() < 0
                && Math.abs(content.getLeft()) != right_btn.getWidth();

    }

    public int Slided() {

        if (Anim)
            return 10;

        if ( content.getLeft() > 0 )
            return 1;

        if ( content.getLeft() < 0 )
            return 2;

        return 0;
    }

    private void UpdateLeftBtn() {

        float pr = content.getLeft()*100/ left_btn.getWidth();

        if ( pr <= 0 ) pr = 0; else if ( pr > 100 ) pr = 100;

        left_btn.setRotationY(90 - 90 * pr / 100);

        int left = content.getLeft() / 2 - left_btn.getWidth() / 2;
        int right = left_btn.getWidth() + (content.getLeft() / 2 - left_btn.getWidth() / 2);

        if ( left > 0 ) left  = 0;
        if ( right > left_btn.getWidth() ) right = left_btn.getWidth();

        left_btn.layout(left, 0, right, left_btn.getHeight());

    }

    private void onTouchUpDown(boolean up, View v) {

        if ( ParentListView != null
                && ParentListView.getOnChannelButtonTouchListener() != null ) {
            ParentListView.getOnChannelButtonTouchListener().onChannelButtonTouch(v == left_btn, up, ChannelID, Func);
        }

    }

    private void onTouchDown(View v) {

        if ( v == left_btn || v == right_btn ) {
            v.setBackgroundColor(getResources().getColor(R.color.channel_btn_pressed));
        }


        onTouchUpDown(false, v);
    }

    private void onTouchUp(View v) {

        if ( v == left_btn || v == right_btn ) {

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

        float pr = (content.getLeft()*-1)*100/ right_btn.getWidth();

        if ( pr <= 0 ) pr = 0; else if ( pr > 100 ) pr = 100;

        right_btn.setRotationY(-90 + 90 * pr / 100);

        int left = getWidth() + ( content.getLeft() / 2 - right_btn.getWidth() / 2);

        if ( content.getLeft()*-1 > right_btn.getWidth() )
            left  = getWidth()-right_btn.getWidth();

        right_btn.layout(left, 0, left + right_btn.getWidth(), right_btn.getHeight());

    }

    public void AnimateToRestingPosition(boolean start_pos) {

        if (!start_pos
                && Anim ) return;


        ObjectAnimator btn_animr = null;
        ObjectAnimator btn_animx = null;
        ObjectAnimator content_animx = null;

        final AnimParams params = new AnimParams();

        params.left_btn_rotation = 90;
        params.left_btn_left = left_btn.getWidth()*-1;
        params.left_btn_right = 0;

        if ( content.getLeft() > 0 ) {

            if (!start_pos
                 && content.getLeft() >= left_btn.getWidth() / 2 ) {

                params.content_left = left_btn.getWidth();
                params.content_right = getWidth()+left_btn.getWidth();

                btn_animr = ObjectAnimator.ofFloat(left_btn, "RotationY", left_btn.getRotationY(), 0f);
                btn_animx = ObjectAnimator.ofFloat(left_btn, "x", left_btn.getLeft(), 0);
                content_animx = ObjectAnimator.ofFloat(content, "x", content.getLeft(), params.content_left);

            } else {

                params.content_left = 0;
                params.content_right = content.getWidth();

                btn_animr = ObjectAnimator.ofFloat(left_btn, "RotationY", left_btn.getRotationY(), 90f);
                btn_animx = ObjectAnimator.ofFloat(left_btn, "x", left_btn.getLeft(), left_btn.getWidth()/2*-1);
                content_animx = ObjectAnimator.ofFloat(content, "x", content.getLeft(), 0f);

            }

        } else if ( content.getLeft() < 0 ) {

            if (!start_pos
                 && content.getLeft()*-1 >= right_btn.getWidth() / 2 ) {

                params.content_left = right_btn.getWidth()*-1;
                params.content_right = getWidth()-right_btn.getWidth();

                btn_animr = ObjectAnimator.ofFloat(right_btn, "RotationY", right_btn.getRotationY(), 0f);
                btn_animx = ObjectAnimator.ofFloat(right_btn, "x", right_btn.getLeft(), params.content_right);
                content_animx = ObjectAnimator.ofFloat(content, "x", content.getLeft(), params.content_left);

            } else {

                params.content_left = 0;
                params.content_right = content.getWidth();

                btn_animr = ObjectAnimator.ofFloat(right_btn, "RotationY", right_btn.getRotationY(), -90f);
                btn_animx = ObjectAnimator.ofFloat(right_btn, "x", right_btn.getLeft(), getWidth()+right_btn.getWidth()/2);
                content_animx = ObjectAnimator.ofFloat(content, "x", content.getLeft(), 0f);

            }

        }


        if ( content_animx != null) {

            AnimatorSet as = new AnimatorSet();
            as.playTogether(btn_animr, btn_animx, content_animx);
            as.setDuration(200);

            as.addListener(new AnimatorListener() {


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

                    if (ParentListView != null)
                        ParentListView.onSlideAnimationEnd();
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

        if ( RightButtonEnabled != rightButtonEnabled ) {
            AnimateToRestingPosition(true);
            RightButtonEnabled = rightButtonEnabled;
        }

    }


    public boolean getRightButtonEnabled() {
        return RightButtonEnabled;
    }


    private void setLeftButtonEnabled(boolean leftButtonEnabled) {

        if ( LeftButtonEnabled != leftButtonEnabled ) {
            AnimateToRestingPosition(true);
            LeftButtonEnabled = leftButtonEnabled;
        }

    }

    private void setDetailSliderEnabled(boolean detailSliderEnabled) {

        if ( detailSliderEnabled ) {
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

        if ( content != null )
            content.setBackgroundColor(color);
    }

    @SuppressLint("DefaultLocale")
    public void setChannelData(Channel channel) {

        Func = channel.getFunc();
        ChannelID = channel.getChannelId();

        imgl.setFunc(Func, channel.StateUp());
        left_dot.setOn(channel.getOnLine());
        left_dot.setRing(false);
        right_dot.setOn(channel.getOnLine());
        right_dot.setRing(false);

        {
            int lidx = -1;
            int ridx = -1;

            switch(Func) {
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

            switch(Func) {
                case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGATE:
                case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGARAGEDOOR:
                case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEDOORLOCK:
                case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGATEWAYLOCK:

                    left_dot.setVisibility(View.INVISIBLE);
                    right_dot.setVisibility(View.VISIBLE);

                    renabled = true;

                    break;
                case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER:

                    left_dot.setVisibility(View.INVISIBLE);
                    right_dot.setVisibility(View.VISIBLE);
                    dslider = true;

                    break;
                case SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH:
                case SuplaConst.SUPLA_CHANNELFNC_LIGHTSWITCH:

                    left_dot.setVisibility(View.VISIBLE);
                    right_dot.setVisibility(View.VISIBLE);

                    lenabled = true;
                    renabled = true;

                    break;
                case SuplaConst.SUPLA_CHANNELFNC_RGBLIGHTING:
                case SuplaConst.SUPLA_CHANNELFNC_DIMMER:
                case SuplaConst.SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING:

                    left_dot.setVisibility(View.INVISIBLE);
                    right_dot.setVisibility(View.VISIBLE);
                    dslider = true;

                    break;

                case SuplaConst.SUPLA_CHANNELFNC_NOLIQUIDSENSOR:
                case SuplaConst.SUPLA_CHANNELFNC_OPENSENSOR_DOOR:
                case SuplaConst.SUPLA_CHANNELFNC_OPENSENSOR_GARAGEDOOR:
                case SuplaConst.SUPLA_CHANNELFNC_OPENSENSOR_GATE:
                case SuplaConst.SUPLA_CHANNELFNC_OPENSENSOR_GATEWAY:
                case SuplaConst.SUPLA_CHANNELFNC_OPENSENSOR_ROLLERSHUTTER:

                    left_dot.setVisibility(View.VISIBLE);
                    left_dot.setRing(true);
                    right_dot.setVisibility(View.VISIBLE);
                    right_dot.setRing(true);

                    break;

                default:

                    left_dot.setVisibility(View.INVISIBLE);
                    right_dot.setVisibility(View.INVISIBLE);


                    break;
            }

            setLeftButtonEnabled(lenabled && channel.getOnLine());
            setRightButtonEnabled(renabled && channel.getOnLine());
            setDetailSliderEnabled(dslider && channel.getOnLine());

        }

        caption_text.setText(channel.getNotEmptyCaption(getContext()));

        if ( channel.getFunc() == SuplaConst.SUPLA_CHANNELFNC_THERMOMETER ) {

            if ( channel.getOnLine()
                    && channel.getTemp() >= -273 )
                imgl.setText1(String.format("%.1f", channel.getTemp())+ (char) 0x00B0);
            else
                imgl.setText1("---");

        } else if ( channel.getFunc() == SuplaConst.SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE ) {

            if ( channel.getOnLine()
                 && channel.getTemp() >= -273 )
                imgl.setText1(String.format("%.1f", channel.getTemp()) + (char) 0x00B0);
            else
                imgl.setText1("---");

            if ( channel.getOnLine()
                    && channel.getHumidity() > -1 )
                imgl.setText2(String.format("%.1f", channel.getHumidity()));
            else
                imgl.setText2("---");

        } else if ( channel.getFunc() == SuplaConst.SUPLA_CHANNELFNC_DISTANCESENSOR
                    || channel.getFunc() == SuplaConst.SUPLA_CHANNELFNC_DEPTHSENSOR ) {

            if ( channel.getOnLine()
                 && channel.getDistance() > -1 ) {

                double distance = channel.getDistance();

                if ( distance >= 1000 ) {

                    imgl.setText1(String.format("%.2f km", distance/1000.00));

                } else if ( distance >= 1 ) {

                    imgl.setText1(String.format("%.2f m", distance));

                } else {
                    distance *= 100;

                    if ( distance >= 1 ) {
                        imgl.setText1(String.format("%.1f cm", distance));
                    } else {
                        distance *= 10;

                        imgl.setText1(String.format("%i mm", (int)distance));
                    }
                }


            } else {
                imgl.setText1("--- m");
            }


        }
    }

}
