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

import android.app.Application;
import android.app.UiModeManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.hilt.work.HiltWorkerFactory;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.work.Configuration.Builder;
import androidx.work.ExistingWorkPolicy;
import androidx.work.WorkManager;
import com.github.mikephil.charting.utils.Utils;
import dagger.hilt.android.HiltAndroidApp;
import javax.inject.Inject;
import org.supla.android.core.SuplaAppApi;
import org.supla.android.core.infrastructure.storage.DebugFileLoggingTree;
import org.supla.android.core.infrastructure.storage.ReleaseLoggingTree;
import org.supla.android.core.networking.suplaclient.SuplaClientBuilder;
import org.supla.android.core.networking.suplaclient.SuplaClientNetworkCallback;
import org.supla.android.core.networking.suplaclient.workers.InitializationWorker;
import org.supla.android.core.notifications.NotificationsHelper;
import org.supla.android.core.observers.AppLifecycleObserver;
import org.supla.android.core.storage.ApplicationPreferences;
import org.supla.android.core.storage.EncryptedPreferences;
import org.supla.android.data.ValuesFormatter;
import org.supla.android.db.room.app.AppDatabase;
import org.supla.android.lib.AndroidSuplaClientMessageHandler;
import org.supla.android.lib.SuplaClient;
import org.supla.android.widget.extended.ExtendedValueWidgetWorker;
import static org.supla.android.model.general.NightModeSettingAndroid.appCompatDelegateValue;
import static org.supla.android.model.general.NightModeSettingAndroid.modeManagerValue;
import org.supla.core.shared.data.model.export.NightModeSetting;
import org.supla.core.shared.infrastructure.messaging.SuplaClientMessage;
import org.supla.core.shared.infrastructure.messaging.SuplaClientMessageHandler;
import timber.log.Timber;
import timber.log.Timber.DebugTree;

@HiltAndroidApp
public class SuplaApp extends Application
    implements SuplaClientMessageHandler.Listener, ValuesFormatterProvider, SuplaAppApi {

  private static final Object _lck1 = new Object();
  private static SuplaClient _SuplaClient = null;
  private static SuplaApp _SuplaApp = null;

  @Inject ValuesFormatter valuesFormatter;
  @Inject NotificationsHelper notificationsHelper;
  @Inject AppLifecycleObserver appLifecycleObserver;
  @Inject SuplaClientBuilder suplaClientBuilder;
  @Inject HiltWorkerFactory workerFactory;
  @Inject AppDatabase appDatabase;
  @Inject UiModeManager modeManager;
  @Inject SuplaClientNetworkCallback suplaClientNetworkCallback;
  @Inject ApplicationPreferences applicationPreferences;
  @Inject EncryptedPreferences encryptedPreferences;
  @Inject DebugFileLoggingTree debugFileLoggingTree;

  public SuplaApp() {
    AndroidSuplaClientMessageHandler.Companion.getGlobalInstance().register(this);
  }

  public static SuplaApp getApp() {
    return _SuplaApp;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    setupTimber();
    setupNightMode();
    setupNetworkCallback();
    SuplaApp._SuplaApp = this;

    notificationsHelper.registerForToken();
    ProcessLifecycleOwner.get().getLifecycle().addObserver(appLifecycleObserver);

    SuplaFormatter.sharedFormatter();

    AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    WorkManager.initialize(this, new Builder().setWorkerFactory(workerFactory).build());
    Utils.init(this);

    // Needed to trigger database migration through Room.
    migrateDatabase();

    enqueueInitialization();
    enqueueExtendedValueWidgetUpdates();
  }

  public SuplaClient SuplaClientInitIfNeed(
      @NonNull Context context, @Nullable String oneTimePassword) {

    SuplaClient result;

    synchronized (_lck1) {
      if (_SuplaClient == null || _SuplaClient.canceled()) {
        _SuplaClient = suplaClientBuilder.build(context, oneTimePassword);
        _SuplaClient.start();
      }

      result = _SuplaClient;
    }

    return result;
  }

  public SuplaClient SuplaClientInitIfNeed(@NonNull Context context) {
    return SuplaClientInitIfNeed(context, null);
  }

  public void OnSuplaClientFinished(SuplaClient sender) {

    synchronized (_lck1) {
      if (_SuplaClient == sender) {
        _SuplaClient = null;
      }
    }
  }

  public SuplaClient getSuplaClient() {

    SuplaClient result;

    synchronized (_lck1) {
      result = _SuplaClient;
    }

    return result;
  }

  @Override
  public void onReceived(@NonNull SuplaClientMessage message) {}

  @NonNull
  public ValuesFormatter getValuesFormatter() {
    return valuesFormatter;
  }

  private void setupTimber() {
    if (BuildConfig.DEBUG) {
      Timber.plant(new DebugTree());
    } else {
      Timber.plant(new ReleaseLoggingTree());
    }
    if (encryptedPreferences.getDevLogActive()) {
      Timber.plant(debugFileLoggingTree);
    }
  }

  private void setupNightMode() {
    NightModeSetting nightModeSetting = applicationPreferences.getNightMode();
    if (VERSION.SDK_INT < VERSION_CODES.S) {
      AppCompatDelegate.setDefaultNightMode(appCompatDelegateValue(nightModeSetting));
    }
    if (nightModeSetting == NightModeSetting.UNSET) {
      applicationPreferences.setNightMode(NightModeSetting.NEVER);
      if (VERSION.SDK_INT >= VERSION_CODES.S) {
        // If unset, expected is that the app will start without night mode.
        modeManager.setApplicationNightMode(modeManagerValue(nightModeSetting));
      }
    }
  }

  private void setupNetworkCallback() {
    NetworkRequest.Builder builder =
        new NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);

    if (VERSION.SDK_INT >= VERSION_CODES.O) {
      builder = builder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI_AWARE);
    }

    NetworkRequest request = builder.build();

    ConnectivityManager manager =
        (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    manager.registerNetworkCallback(request, suplaClientNetworkCallback);
  }

  private void migrateDatabase() {
    try {
      appDatabase.getOpenHelper().getReadableDatabase();
    } catch (Exception exception) {
      if (BuildConfig.DEBUG) {
        throw exception;
      }

      Timber.e(exception, "Could not migrate database, trying to delete it");
      boolean result = deleteDatabase(AppDatabase.NAME);
      Timber.e("Database deletion finished with %s", (result ? "success" : "failure"));
    }
  }

  private void enqueueInitialization() {
    WorkManager.getInstance(this)
        .enqueueUniqueWork(
            InitializationWorker.NAME,
            ExistingWorkPolicy.KEEP,
            InitializationWorker.Companion.build());
  }

  private void enqueueExtendedValueWidgetUpdates() {
    ExtendedValueWidgetWorker.Companion.enqueuePeriodic(WorkManager.getInstance(this));
  }
}
