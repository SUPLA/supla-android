package org.supla.android.scenes;

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

import android.content.Context;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import java.util.Date;
import java.util.concurrent.Callable;
import javax.annotation.Nullable;
import javax.inject.Inject;
import org.supla.android.Preferences;
import org.supla.android.R;
import org.supla.android.SuplaApp;
import org.supla.android.SuplaChannelStatus;
import org.supla.android.SuplaChannelStatus.ShapeType;
import org.supla.android.ViewHelper;
import org.supla.android.db.Scene;
import org.supla.android.images.ImageCache;
import org.supla.android.images.ImageId;
import org.supla.android.listview.LineView;

@AndroidEntryPoint
public class SceneLayout extends LinearLayout {

  private static final String TIMER_INACTIVE = "--:--:--";


  @Inject
  SceneEventsManager eventsManager;

  private Callable<ViewHolder> viewHolderProvider;

  private RelativeLayout content;

  private ChannelImageLayout imgl;

  private CaptionView caption_text;

  private SuplaChannelStatus right_onlineStatus;
  private SuplaChannelStatus left_onlineStatus;
  private TextView sceneDurationTimer;
  private CountDownTimer sceneCountDown;
  private Handler uiThreadHandler;

  private Listener listener;

  private int sceneId;
  private int locationId;
  private Disposable sceneChangesDisposable = null;

  public interface Listener {

    void onCaptionLongPress(SceneLayout l);

    void onLongPress(ViewHolder viewHolder);
  }


  public SceneLayout(Context context) {
    super(context);

    init(context);
  }

  public int getLocationId() {
    return locationId;
  }

  private void init(Context context) {
    uiThreadHandler = new Handler(Looper.getMainLooper());
    Preferences prefs = new Preferences(context);
    setOrientation(LinearLayout.HORIZONTAL);

    setBackgroundColor(getResources().getColor(R.color.channel_cell));

    float heightScaleFactor = (prefs.getChannelHeight() + 0f) / 100f;
    int channelHeight = (int) (
        ((float) getResources().getDimensionPixelSize(R.dimen.channel_layout_height))
            * heightScaleFactor);

    content = new RelativeLayout(context);
    content.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, channelHeight));

    content.setBackgroundColor(getResources().getColor(R.color.channel_cell));

    addView(content);

    right_onlineStatus = newOnlineStatus(context, true);
    right_onlineStatus.setId(ViewHelper.generateViewId());
    content.addView(right_onlineStatus);
    left_onlineStatus = newOnlineStatus(context, false);
    left_onlineStatus.setId(ViewHelper.generateViewId());
    content.addView(left_onlineStatus);

    sceneDurationTimer = newTimerView(context);
    content.addView(sceneDurationTimer);
    RelativeLayout.LayoutParams sdlp = new RelativeLayout
        .LayoutParams((int) getResources().getDimension(R.dimen.channel_imgtext_width),
        (int) (getResources().getDimension(R.dimen.default_text_size)
            * 1.5));
    sdlp.addRule(RelativeLayout.ABOVE, right_onlineStatus.getId());
    sdlp.addRule(RelativeLayout.ALIGN_RIGHT, right_onlineStatus.getId());
    sdlp.setMargins(0, 0, 0,
        (int) getResources().getDimension(R.dimen.form_element_spacing));
    sceneDurationTimer.setLayoutParams(sdlp);
    sceneDurationTimer.setText(TIMER_INACTIVE);

    RelativeLayout channelIconContainer = new RelativeLayout(context);
    content.addView(channelIconContainer);
    channelIconContainer
        .setLayoutParams(getChannelIconContainerLayoutParams());

    SuplaChannelStatus right_ActiveStatus = new SuplaChannelStatus(context);
    right_ActiveStatus.setSingleColor(true);
    right_ActiveStatus.setOnlineColor(getResources().getColor(R.color.channel_dot_on));

    {
      int dot_size = getResources().getDimensionPixelSize(R.dimen.channel_dot_size);
      RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
          dot_size / 2, dot_size * 2);

      lp.addRule(RelativeLayout.LEFT_OF, right_onlineStatus.getId());
      lp.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);

      right_ActiveStatus.setLayoutParams(lp);
    }

    right_ActiveStatus.setVisibility(View.GONE);
    content.addView(right_ActiveStatus);

    LineView bottom_line = new LineView(context);
    content.addView(bottom_line);

    imgl = new ChannelImageLayout(context, heightScaleFactor);
    channelIconContainer.addView(imgl);

    caption_text = new CaptionView(context, imgl.getId(), heightScaleFactor);
    caption_text.setOnLongClickListener(v -> {
      listener.onCaptionLongPress(this);
      return true;
    });
    channelIconContainer.addView(caption_text);
    setOnLongClickListener(v -> {
      listener.onLongPress(provideViewHolderForLongPressCallback());
      return true;
    });
  }


  public SceneLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  public SceneLayout(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context);
  }

  public void setSceneListener(Listener l) {
    listener = l;
  }

  public void setViewHolderProvider(Callable<ViewHolder> provider) {
    viewHolderProvider = provider;
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();

    if (sceneChangesDisposable != null && !sceneChangesDisposable.isDisposed()) {
      sceneChangesDisposable.dispose();
    }
    if (sceneCountDown != null) {
      sceneCountDown.cancel();
    }
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();

    if (sceneChangesDisposable != null && sceneChangesDisposable.isDisposed()) {
      observeStateChanges();
    }
  }

  private RelativeLayout.LayoutParams getChannelIconContainerLayoutParams() {
    RelativeLayout.LayoutParams lp;

    lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
        RelativeLayout.LayoutParams.WRAP_CONTENT);
    lp.addRule(RelativeLayout.CENTER_IN_PARENT);

    return lp;
  }

  private TextView newTimerView(Context context) {

    TextView tv = new TextView(context);
    tv.setTypeface(SuplaApp.getApp().getTypefaceQuicksandRegular());

    tv.setTextSize(TypedValue.COMPLEX_UNIT_PX,
        getResources().getDimension(R.dimen.default_text_size));
    tv.setTextColor(getResources().getColor(R.color.label_grey));
    tv.setGravity(Gravity.BOTTOM | Gravity.END);

    return tv;
  }

  protected RelativeLayout.LayoutParams getOnlineStatusLayoutParams(boolean right) {

    int dot_size = getResources().getDimensionPixelSize(R.dimen.channel_dot_size);

    RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(dot_size, dot_size);

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

  public String getCaption() {
    return caption_text.getText().toString();
  }

  public void setBackgroundColor(int color) {

    super.setBackgroundColor(color);

    if (content != null) {
      content.setBackgroundColor(color);
    }
  }


  public void setScene(Scene scene) {
    sceneId = scene.getSceneId();
    locationId = scene.getLocationId();

    int[] standardIcons = {
        R.drawable.scene0, R.drawable.scene1,
        R.drawable.scene2, R.drawable.scene3,
        R.drawable.scene4, R.drawable.scene5,
        R.drawable.scene6, R.drawable.scene7,
        R.drawable.scene8, R.drawable.scene9,
        R.drawable.scene10, R.drawable.scene11,
        R.drawable.scene12, R.drawable.scene13,
        R.drawable.scene14, R.drawable.scene15,
        R.drawable.scene16, R.drawable.scene17,
        R.drawable.scene18, R.drawable.scene19};

    int iconId = scene.getAltIcon();
    if (iconId == 0) {
      iconId = scene.getUserIcon();
    }

    ImageId imgId;
    if (iconId < standardIcons.length) {
      imgId = new ImageId(standardIcons[iconId]);
    } else {
      imgId = new ImageId(iconId, 0);
    }

    imgl.setImage(imgId);

    caption_text.setText(scene.getCaption());
    setupSceneStatus(scene.isExecuting());

    observeStateChanges();
  }

  private void observeStateChanges() {
    sceneChangesDisposable = eventsManager
        .observerScene(sceneId)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(sceneState -> {
          if (sceneState.getExecuting()) {
            startTimer(sceneState.getEndTime());
          } else {
            if (sceneCountDown != null) {
              sceneCountDown.cancel();
              setTimerInactive();
            }
          }
          setupSceneStatus(sceneState.getExecuting());
        });
  }

  private ShapeType getStatusShapeType(boolean sceneActive) {
    if (sceneActive) {
      return SuplaChannelStatus.ShapeType.Dot;
    } else {
      return SuplaChannelStatus.ShapeType.Ring;
    }
  }

  private void setupSceneStatus(boolean sceneActive) {
    ShapeType shapeType = getStatusShapeType(sceneActive);
    left_onlineStatus.setShapeType(shapeType);
    right_onlineStatus.setShapeType(shapeType);
  }

  private void startTimer(@Nullable Date end) {
    if (end == null) {
      return;
    }
    long sceneDuration = end.getTime() - System.currentTimeMillis();
    sceneCountDown = new CountDownTimer(sceneDuration, 1000) {
      private long duration = sceneDuration;

      public void onTick(long millisUntilFinished) {
        uiThreadHandler.post(() -> {
          duration -= 1000;
          sceneDurationTimer.setText(formatMillis(duration));
        });
      }

      public void onFinish() {
        sceneCountDown = null;
        uiThreadHandler.post(() -> setTimerInactive());
      }
    };
    sceneCountDown.start();
  }

  private void setTimerInactive() {
    sceneDurationTimer.setText(TIMER_INACTIVE);
    SuplaChannelStatus.ShapeType state = SuplaChannelStatus.ShapeType.Ring;
    left_onlineStatus.setShapeType(state);
    right_onlineStatus.setShapeType(state);
  }

  private String formatMillis(long v) {
    long r = v, k = 0;
    StringBuilder sb = new StringBuilder();

    k = r / 3600000;
    sb.append(String.format("%02d:", k));
    r -= k * 3600000;
    k = r / 60000;
    sb.append(String.format("%02d:", k));
    r -= k * 60000;
    sb.append(String.format("%02d", r / 1000));

    return sb.toString();
  }

  static class CaptionView extends androidx.appcompat.widget.AppCompatTextView {


    public CaptionView(Context context, int imgl_id, float heightScaleFactor) {
      super(context);
      float textSize = getResources().getDimension(R.dimen.channel_caption_text_size);
      if (heightScaleFactor > 1.0) {
        textSize *= heightScaleFactor;
      }
      setTypeface(SuplaApp.getApp().getTypefaceOpenSansBold());
      setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
      setTextColor(getResources().getColor(R.color.channel_caption_text));
      setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);

      RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
          LayoutParams.WRAP_CONTENT);

      if (imgl_id != -1) {
        lp.addRule(RelativeLayout.BELOW, imgl_id);
      }

      lp.topMargin = (int) (getResources().getDimensionPixelSize(R.dimen.channel_caption_top_margin)
          * heightScaleFactor);
      setLayoutParams(lp);
    }

  }

  private class ChannelImageLayout extends LinearLayout {

    private final ImageView imageView;
    private final float heightScaleFactor;

    public ChannelImageLayout(Context context, float heightScaleFactor) {
      super(context);
      this.heightScaleFactor = heightScaleFactor;

      setId(ViewHelper.generateViewId());
      imageView = newImageView(context);

      configureSubviews();
      setDimensions();
    }

    private void configureSubviews() {
      removeAllViews();
      setOrientation(LinearLayout.HORIZONTAL);
      addView(imageView);
    }

    private ImageView newImageView(Context context) {
      ImageView imageView = new ImageView(context);
      imageView.setId(ViewHelper.generateViewId());
      imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

      return imageView;
    }

    private int scaledDimension(int dim) {
      return (int) (dim * heightScaleFactor);
    }

    private void setImageDimensions(ImageView Img, int width, int height) {
      int sw = scaledDimension(width),
          sh = scaledDimension(height);

      LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(sw, sh);

      Img.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
      Img.setLayoutParams(lp);

    }

    private void setDimensions() {
      setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);

      int h = getResources().getDimensionPixelSize(R.dimen.channel_img_height);
      int sh = scaledDimension(h);

      RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
          LayoutParams.WRAP_CONTENT, sh);

      lp.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
      lp.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);

      setLayoutParams(lp);

      setImageDimensions(imageView,
          getResources().getDimensionPixelSize(R.dimen.channel_img_width),
          getResources().getDimensionPixelSize(R.dimen.channel_img_height));
    }

    public void setImage(ImageId img1Id) {
      if (img1Id == null) {
        imageView.setVisibility(View.GONE);
      } else {
        imageView.setImageBitmap(ImageCache.getBitmap(getContext(), img1Id));
        imageView.setVisibility(View.VISIBLE);
      }
    }
  }

  private ViewHolder provideViewHolderForLongPressCallback() {
    try {
      return viewHolderProvider.call();
    } catch (Exception e) {
      // Should never happen
      return null;
    }
  }
}
