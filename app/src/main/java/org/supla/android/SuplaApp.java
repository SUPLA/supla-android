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

import static org.supla.android.widget.shared.WidgetReloadWorker.WORK_ID;

import android.app.UiModeManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.WifiManager;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.hilt.work.HiltWorkerFactory;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.multidex.MultiDexApplication;
import androidx.work.Configuration.Builder;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import com.github.mikephil.charting.utils.Utils;
import dagger.hilt.android.HiltAndroidApp;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import org.supla.android.core.SuplaAppApi;
import org.supla.android.core.infrastructure.storage.DebugFileLoggingTree;
import org.supla.android.core.infrastructure.storage.ReleaseLoggingTree;
import org.supla.android.core.networking.suplaclient.SuplaClientBuilder;
import org.supla.android.core.networking.suplaclient.SuplaClientNetworkCallback;
import org.supla.android.core.networking.suplaclient.workers.InitializationWorker;
import org.supla.android.core.notifications.NotificationsHelper;
import org.supla.android.core.observers.AppLifecycleObserver;
import org.supla.android.core.shared.SuplaClientMessageExtensionsKt;
import org.supla.android.core.storage.ApplicationPreferences;
import org.supla.android.core.storage.EncryptedPreferences;
import org.supla.android.data.ValuesFormatter;
import org.supla.android.data.model.general.NightModeSetting;
import org.supla.android.db.DbHelper;
import org.supla.android.db.room.app.AppDatabase;
import org.supla.android.features.icons.LoadUserIconsIntoCacheWorker;
import org.supla.android.lib.AndroidSuplaClientMessageHandler;
import org.supla.android.lib.SuplaClient;
import org.supla.android.lib.SuplaOAuthToken;
import org.supla.android.profile.ProfileManager;
import org.supla.android.restapi.SuplaRestApiClientTask;
import org.supla.android.widget.extended.ExtendedValueWidgetWorker;
import org.supla.android.widget.shared.WidgetReloadWorker;
import org.supla.core.shared.infrastructure.messaging.SuplaClientMessage;
import org.supla.core.shared.infrastructure.messaging.SuplaClientMessageHandler;
import timber.log.Timber;
import timber.log.Timber.DebugTree;

@HiltAndroidApp
public class SuplaApp extends MultiDexApplication
    implements SuplaClientMessageHandler.Listener, ValuesFormatterProvider, SuplaAppApi {

  private static final Object _lck1 = new Object();
  private static final Object _lck3 = new Object();
  private static SuplaClient _SuplaClient = null;
  private static SuplaApp _SuplaApp = null;
  private SuplaOAuthToken _OAuthToken;
  private final ArrayList<SuplaRestApiClientTask> _RestApiClientTasks = new ArrayList<>();
  private static long lastWifiScanTime;

  @Inject ProfileManager profileManager;
  @Inject ValuesFormatter valuesFormatter;
  @Inject NotificationsHelper notificationsHelper;
  @Inject AppLifecycleObserver appLifecycleObserver;
  @Inject SuplaClientBuilder suplaClientBuilder;
  @Inject HiltWorkerFactory workerFactory;
  @Inject AppDatabase appDatabase;
  @Inject Preferences preferences;
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

    enqueueWidgetRefresh();
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

  public SuplaOAuthToken RegisterRestApiClientTask(SuplaRestApiClientTask task) {
    SuplaOAuthToken result = null;
    synchronized (_lck3) {
      if (_OAuthToken != null && _OAuthToken.isAlive()) {
        result = new SuplaOAuthToken(_OAuthToken);
      }
      _RestApiClientTasks.add(task);
    }

    return result;
  }

  public void UnregisterRestApiClientTask(SuplaRestApiClientTask task) {
    synchronized (_lck3) {
      _RestApiClientTasks.remove(task);
    }
  }

  public void CancelAllRestApiClientTasks(boolean mayInterruptIfRunning) {
    synchronized (_lck3) {
      for (int a = 0; a < _RestApiClientTasks.size(); a++) {
        _RestApiClientTasks.get(a).cancel(mayInterruptIfRunning);
      }
    }
  }

  public static boolean wifiStartScan(WifiManager manager) {
    if (manager.startScan()) {
      lastWifiScanTime = System.currentTimeMillis();
      return true;
    }

    return false;
  }

  public static long getSecondsSinceLastWiFiScan() {
    long result = System.currentTimeMillis() - lastWifiScanTime;
    result /= 1000;
    return result;
  }

  @Override
  public void onReceived(@NonNull SuplaClientMessage message) {
    if (message instanceof SuplaClientMessage.OAuthToken authToken) {
      synchronized (_lck3) {
        _OAuthToken = SuplaClientMessageExtensionsKt.getSuplaToken(authToken);
        for (int a = 0; a < _RestApiClientTasks.size(); a++) {
          _RestApiClientTasks.get(a).setToken(_OAuthToken);
        }
      }
    }
  }

  @NonNull
  public ValuesFormatter getValuesFormatter() {
    return valuesFormatter;
  }

  public void cleanupToken() {
    _OAuthToken = null;
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
      AppCompatDelegate.setDefaultNightMode(nightModeSetting.appCompatDelegateValue());
    }
    if (nightModeSetting == NightModeSetting.UNSET) {
      applicationPreferences.setNightMode(NightModeSetting.NEVER);
      if (VERSION.SDK_INT >= VERSION_CODES.S) {
        // If unset, expected is that the app will start without night mode.
        modeManager.setApplicationNightMode(nightModeSetting.modeManagerValue());
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

  private void enqueueWidgetRefresh() {
    Constraints constraints =
        new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
    PeriodicWorkRequest request =
        new PeriodicWorkRequest.Builder(
                WidgetReloadWorker.class,
                PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS,
                TimeUnit.MINUTES)
            .setConstraints(constraints)
            .addTag(WORK_ID)
            .build();
    WorkManager.getInstance(this)
        .enqueueUniquePeriodicWork(
            WORK_ID, ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE, request);
    LoadUserIconsIntoCacheWorker.Companion.start(this);
  }

  private void migrateDatabase() {
    try {
      appDatabase.getOpenHelper().getReadableDatabase();
    } catch (IllegalStateException exception) {
      if (BuildConfig.DEBUG) {
        throw exception;
      }

      Timber.e(exception, "Could not migrated database, trying to delete it");
      boolean result = deleteDatabase(DbHelper.DATABASE_NAME);
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
