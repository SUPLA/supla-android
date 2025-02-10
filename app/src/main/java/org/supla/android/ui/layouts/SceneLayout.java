package org.supla.android.ui.layouts;

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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.core.content.res.ResourcesCompat;
import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import java.util.Date;
import javax.annotation.Nullable;
import javax.inject.Inject;
import org.supla.android.Preferences;
import org.supla.android.R;
import org.supla.android.SuplaChannelStatus;
import org.supla.android.SuplaChannelStatus.ShapeType;
import org.supla.android.data.source.local.entity.SceneEntity;
import org.supla.android.events.UpdateEventsManager;
import org.supla.android.images.ImageCache;
import org.supla.android.images.ImageId;
import org.supla.android.ui.lists.SlideableItem;
import org.supla.android.ui.lists.SwapableListItem;
import org.supla.android.usecases.icon.GetSceneIconUseCase;

@AndroidEntryPoint
public class SceneLayout extends LinearLayout implements SlideableItem, SwapableListItem {

  @Inject UpdateEventsManager eventsManager;
  @Inject DurationTimerHelper durationTimerHelper;
  @Inject GetSceneIconUseCase getSceneIconUseCase;

  private RelativeLayout content;
  private FrameLayout right_btn;
  private FrameLayout left_btn;

  private ChannelImageLayout imgl;

  private TextView left_btn_text;
  private TextView right_btn_text;
  private CaptionView caption_text;

  private SuplaChannelStatus right_onlineStatus;
  private SuplaChannelStatus left_onlineStatus;
  private TextView sceneDurationTimer;
  private CountDownTimer sceneCountDown;
  private Handler uiThreadHandler;

  private Listener listener;

  private int sceneId;
  private String locationCaption;
  private Disposable sceneChangesDisposable = null;

  public interface Listener {

    void onLeftButtonClick(int sceneId);

    void onRightButtonClick(int sceneId);

    void onCaptionLongPress(int sceneId);
  }

  public SceneLayout(Context context) {
    super(context);

    init(context);
  }

  public String getLocationCaption() {
    return locationCaption;
  }

  public int getSceneId() {
    return sceneId;
  }

  private void init(Context context) {
    uiThreadHandler = new Handler(Looper.getMainLooper());
    Preferences prefs = new Preferences(context);
    setOrientation(LinearLayout.HORIZONTAL);

    setBackgroundColor(getResources().getColor(R.color.surface));

    right_btn = new FrameLayout(context);
    left_btn = new FrameLayout(context);

    float heightScaleFactor = (prefs.getChannelHeight() + 0f) / 100f;
    int channelHeight =
        (int)
            (((float) getResources().getDimensionPixelSize(R.dimen.channel_layout_height))
                * heightScaleFactor);

    right_btn.setLayoutParams(
        new LayoutParams(
            getResources().getDimensionPixelSize(R.dimen.channel_layout_button_width),
            channelHeight));

    right_btn.setBackgroundColor(getResources().getColor(R.color.primary));

    left_btn.setLayoutParams(
        new LayoutParams(
            getResources().getDimensionPixelSize(R.dimen.channel_layout_button_width),
            channelHeight));

    left_btn.setBackgroundColor(getResources().getColor(R.color.primary));

    content = new RelativeLayout(context);
    content.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, channelHeight));

    content.setBackgroundColor(getResources().getColor(R.color.surface));

    addView(content);
    addView(left_btn);
    addView(right_btn);

    left_btn_text = newTextView(context);
    left_btn.addView(left_btn_text);

    right_btn_text = newTextView(context);
    right_btn.addView(right_btn_text);

    right_onlineStatus = newOnlineStatus(context, true);
    right_onlineStatus.setId(View.generateViewId());
    content.addView(right_onlineStatus);
    left_onlineStatus = newOnlineStatus(context, false);
    left_onlineStatus.setId(View.generateViewId());
    content.addView(left_onlineStatus);

    sceneDurationTimer = durationTimerHelper.createTimerView(context, heightScaleFactor);
    content.addView(sceneDurationTimer);
    sceneDurationTimer.setLayoutParams(
        durationTimerHelper.getTimerViewLayoutParams(context, heightScaleFactor));

    RelativeLayout channelIconContainer = new RelativeLayout(context);
    content.addView(channelIconContainer);
    channelIconContainer.setLayoutParams(getChannelIconContainerLayoutParams());

    SuplaChannelStatus right_ActiveStatus = new SuplaChannelStatus(context);
    right_ActiveStatus.setSingleColor(true);
    right_ActiveStatus.setOnlineColor(getResources().getColor(R.color.primary));

    int dot_size = getResources().getDimensionPixelSize(R.dimen.channel_dot_size);
    RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(dot_size / 2, dot_size * 2);

    lp.addRule(RelativeLayout.LEFT_OF, right_onlineStatus.getId());
    lp.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);

    right_ActiveStatus.setLayoutParams(lp);

    right_ActiveStatus.setVisibility(View.GONE);
    content.addView(right_ActiveStatus);

    LineView bottom_line = new LineView(context);
    content.addView(bottom_line);

    imgl = new ChannelImageLayout(context, heightScaleFactor);
    channelIconContainer.addView(imgl);

    caption_text = new CaptionView(context, imgl.getId(), heightScaleFactor);
    caption_text.setOnLongClickListener(
        v -> {
          listener.onCaptionLongPress(sceneId);
          return true;
        });
    channelIconContainer.addView(caption_text);

    left_btn.setOnClickListener(v -> listener.onLeftButtonClick(sceneId));
    right_btn.setOnClickListener(v -> listener.onRightButtonClick(sceneId));
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

    lp =
        new RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
    lp.addRule(RelativeLayout.CENTER_IN_PARENT);

    return lp;
  }

  private TextView newTextView(Context context) {

    TextView tv = new TextView(context);
    tv.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

    tv.setTypeface(ResourcesCompat.getFont(context, R.font.quicksand_regular));

    tv.setTextSize(
        TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.channel_btn_text_size));
    tv.setTextColor(ResourcesCompat.getColor(context.getResources(), R.color.on_primary, null));
    tv.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);

    return tv;
  }

  protected RelativeLayout.LayoutParams getOnlineStatusLayoutParams(boolean right) {

    int dot_size = getResources().getDimensionPixelSize(R.dimen.channel_dot_size);

    RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(dot_size, dot_size);

    int margin = getResources().getDimensionPixelSize(R.dimen.list_horizontal_spacing);

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
    result.setOfflineColor(getResources().getColor(R.color.red));
    result.setOnlineColor(getResources().getColor(R.color.primary));

    return result;
  }

  public void setLeftBtnText(String Text) {
    left_btn_text.setText(Text);
  }

  public void setRightBtnText(String Text) {
    right_btn_text.setText(Text);
  }

  public void slide(int delta) {
    content.layout(delta, content.getTop(), content.getWidth() + delta, content.getHeight());

    int bcolor = getResources().getColor(R.color.primary);

    left_btn.setBackgroundColor(bcolor);
    right_btn.setBackgroundColor(bcolor);

    UpdateLeftBtn();
    UpdateRightBtn();
  }

  private void UpdateLeftBtn() {

    float pr = (float) (content.getLeft() * 100) / left_btn.getWidth();

    if (pr <= 0) {
      pr = 0;
    } else if (pr > 100) {
      pr = 100;
    }

    left_btn.setRotationY(90 - 90 * pr / 100);

    int left = content.getLeft() / 2 - left_btn.getWidth() / 2;
    int right = left_btn.getWidth() + (content.getLeft() / 2 - left_btn.getWidth() / 2);

    if (left > 0) {
      left = 0;
    }
    if (right > left_btn.getWidth()) {
      right = left_btn.getWidth();
    }

    left_btn.layout(left, 0, right, left_btn.getHeight());
  }

  private void UpdateRightBtn() {

    float pr = (content.getLeft() * -1) * 100 / (float) right_btn.getWidth();

    if (pr <= 0) {
      pr = 0;
    } else if (pr > 100) {
      pr = 100;
    }

    right_btn.setRotationY(-90 + 90 * pr / 100);

    int left = getWidth() + (content.getLeft() / 2 - right_btn.getWidth() / 2);

    if (content.getLeft() * -1 > right_btn.getWidth()) {
      left = getWidth() - right_btn.getWidth();
    }

    right_btn.layout(left, 0, left + right_btn.getWidth(), right_btn.getHeight());
  }

  public String getCaption() {
    return caption_text.getText().toString();
  }

  public void setLocationCaption(String locationCaption) {
    this.locationCaption = locationCaption;
  }

  public void setScene(SceneEntity scene) {
    sceneId = scene.getRemoteId();

    setupLayout(scene);
    observeStateChanges();
  }

  private void setupLayout(SceneEntity scene) {
    imgl.setImage(getSceneIconUseCase.invoke(scene));

    setRightBtnText(getResources().getString(R.string.btn_execute));
    setLeftBtnText(getResources().getString(R.string.btn_abort));

    caption_text.setText(scene.getCaption());
    setupSceneStatus(scene.isExecuting());
  }

  private void observeStateChanges() {
    if (sceneChangesDisposable != null && !sceneChangesDisposable.isDisposed()) {
      sceneChangesDisposable.dispose();
    }

    sceneChangesDisposable =
        eventsManager
            .observerScene(sceneId)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                scene -> {
                  setupLayout(scene);

                  if (sceneCountDown != null) {
                    sceneCountDown.cancel();
                    setTimerInactive();
                  }

                  if (scene.isExecuting()) {
                    startTimer(scene.getEstimatedEndDate());
                  }
                  setupSceneStatus(scene.isExecuting());
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
    sceneCountDown =
        new CountDownTimer(sceneDuration, 100) {
          private final long endTime = end.getTime();
          private boolean firstTick = true;

          public void onTick(long millisUntilFinished) {
            uiThreadHandler.post(
                () -> {
                  long restTime = endTime - System.currentTimeMillis();
                  sceneDurationTimer.setText(durationTimerHelper.formatMillis(restTime));

                  if (firstTick) {
                    sceneDurationTimer.setVisibility(VISIBLE);
                    firstTick = false;
                  }
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
    sceneDurationTimer.setVisibility(GONE);

    SuplaChannelStatus.ShapeType state = SuplaChannelStatus.ShapeType.Ring;
    left_onlineStatus.setShapeType(state);
    right_onlineStatus.setShapeType(state);
  }

  static class CaptionView extends androidx.appcompat.widget.AppCompatTextView {

    public CaptionView(Context context, int imgl_id, float heightScaleFactor) {
      super(context);
      float textSize = getResources().getDimension(R.dimen.channel_caption_text_size);
      if (heightScaleFactor > 1.0) {
        textSize *= heightScaleFactor;
      }
      setTypeface(ResourcesCompat.getFont(context, R.font.open_sans_bold));
      setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
      setTextColor(ResourcesCompat.getColor(context.getResources(), R.color.on_background, null));
      setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);

      RelativeLayout.LayoutParams lp =
          new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

      if (imgl_id != -1) {
        lp.addRule(RelativeLayout.BELOW, imgl_id);
      }

      lp.topMargin =
          (int)
              (getResources().getDimensionPixelSize(R.dimen.channel_caption_top_margin)
                  * heightScaleFactor);
      setLayoutParams(lp);
    }
  }

  private static class ChannelImageLayout extends LinearLayout {

    private final ImageView imageView;
    private final float heightScaleFactor;

    public ChannelImageLayout(Context context, float heightScaleFactor) {
      super(context);
      this.heightScaleFactor = heightScaleFactor;

      setId(View.generateViewId());
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
      imageView.setId(View.generateViewId());
      imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

      return imageView;
    }

    private int scaledDimension(int dim) {
      return (int) (dim * heightScaleFactor);
    }

    private void setImageDimensions(ImageView Img, int width, int height) {
      int sw = scaledDimension(width), sh = scaledDimension(height);

      LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(sw, sh);

      Img.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
      Img.setLayoutParams(lp);
    }

    private void setDimensions() {
      setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);

      int h = getResources().getDimensionPixelSize(R.dimen.channel_img_height);
      int sh = scaledDimension(h);

      RelativeLayout.LayoutParams lp =
          new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, sh);

      lp.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
      lp.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);

      setLayoutParams(lp);

      setImageDimensions(
          imageView,
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
}
