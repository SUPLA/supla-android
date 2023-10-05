package org.supla.android.db;

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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import dagger.hilt.android.EntryPointAccessors;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.supla.android.Encryption;
import org.supla.android.Preferences;
import org.supla.android.R;
import org.supla.android.SuplaApp;
import org.supla.android.Trace;
import org.supla.android.core.infrastructure.DateProvider;
import org.supla.android.data.source.ChannelRepository;
import org.supla.android.data.source.ColorListRepository;
import org.supla.android.data.source.DefaultChannelRepository;
import org.supla.android.data.source.DefaultColorListRepository;
import org.supla.android.data.source.DefaultSceneRepository;
import org.supla.android.data.source.DefaultUserIconRepository;
import org.supla.android.data.source.ResultTuple;
import org.supla.android.data.source.SceneRepository;
import org.supla.android.data.source.UserIconRepository;
import org.supla.android.data.source.local.ChannelDao;
import org.supla.android.data.source.local.ColorListDao;
import org.supla.android.data.source.local.LocationDao;
import org.supla.android.data.source.local.SceneDao;
import org.supla.android.data.source.local.UserIconDao;
import org.supla.android.data.source.local.entity.ChannelRelationEntity;
import org.supla.android.db.SuplaContract.ChannelExtendedValueEntry;
import org.supla.android.db.versions.MigratorV27;
import org.supla.android.di.entrypoints.ProfileIdHolderEntryPoint;
import org.supla.android.images.ImageCacheProvider;
import org.supla.android.lib.SuplaChannel;
import org.supla.android.lib.SuplaChannelExtendedValue;
import org.supla.android.lib.SuplaChannelGroup;
import org.supla.android.lib.SuplaChannelGroupRelation;
import org.supla.android.lib.SuplaChannelValue;
import org.supla.android.lib.SuplaChannelValueUpdate;
import org.supla.android.lib.SuplaLocation;
import org.supla.android.profile.ProfileIdHolder;
import org.supla.android.profile.ProfileMigrator;

public class DbHelper extends BaseDbHelper {

  public static final int DATABASE_VERSION = 30;
  public static final String DATABASE_NAME = "supla.db";
  private static final Object mutex = new Object();

  private static DbHelper instance;

  private final ChannelRepository channelRepository;
  private final ColorListRepository colorListRepository;
  private final UserIconRepository userIconRepository;
  private final SceneRepository sceneRepository;
  private final Preferences preferences;

  private DbHelper(Context context, ProfileIdProvider profileIdProvider) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION, profileIdProvider);
    this.channelRepository =
        new DefaultChannelRepository(
            new ChannelDao(this), new LocationDao(this), new DateProvider());
    this.colorListRepository = new DefaultColorListRepository(new ColorListDao(this));
    this.userIconRepository =
        new DefaultUserIconRepository(
            new UserIconDao(this), new ImageCacheProvider(), profileIdProvider);
    this.sceneRepository = new DefaultSceneRepository(new SceneDao(this));
    this.preferences = new Preferences(context);
  }

  /**
   * Gets a single instance of the {@link DbHelper} class. If the instance does not exist, is
   * created like in classic Singleton pattern.
   *
   * @param context The context.
   * @return {@link DbHelper} instance.
   */
  public static DbHelper getInstance(Context context) {
    DbHelper result = instance;
    if (result == null) {
      synchronized (mutex) {
        result = instance;
        if (result == null) {
          ProfileIdHolder profileIdHolder =
              EntryPointAccessors.fromApplication(
                      context.getApplicationContext(), ProfileIdHolderEntryPoint.class)
                  .provideProfileIdHolder();
          instance = result = new DbHelper(context, profileIdHolder::getProfileId);
        }
      }
    }
    return result;
  }

  public SceneRepository getSceneRepository() {
    return sceneRepository;
  }

  @Override
  protected String getDatabaseNameForLog() {
    return DATABASE_NAME;
  }

  private void createLocationTable(SQLiteDatabase db) {

    final String SQL_CREATE_LOCATION_TABLE =
        "CREATE TABLE "
            + SuplaContract.LocationEntry.TABLE_NAME
            + " ("
            + SuplaContract.LocationEntry._ID
            + " INTEGER PRIMARY KEY,"
            + SuplaContract.LocationEntry.COLUMN_NAME_LOCATIONID
            + " INTEGER NOT NULL,"
            + SuplaContract.LocationEntry.COLUMN_NAME_CAPTION
            + " TEXT NOT NULL,"
            + SuplaContract.LocationEntry.COLUMN_NAME_VISIBLE
            + " INTEGER NOT NULL,"
            + SuplaContract.LocationEntry.COLUMN_NAME_COLLAPSED
            + " INTEGER NOT NULL default 0,"
            + SuplaContract.LocationEntry.COLUMN_NAME_SORTING
            + " TEXT NOT NULL default 'DEFAULT',"
            + SuplaContract.LocationEntry.COLUMN_NAME_SORT_ORDER
            + " INTEGER NOT NULL DEFAULT -1)";

    execSQL(db, SQL_CREATE_LOCATION_TABLE);
    createIndex(
        db,
        SuplaContract.LocationEntry.TABLE_NAME,
        SuplaContract.LocationEntry.COLUMN_NAME_LOCATIONID);
  }

  private void createSceneTable(SQLiteDatabase db) {
    final String SQL_CREATE_SCENE_TABLE =
        "CREATE TABLE "
            + SuplaContract.SceneEntry.TABLE_NAME
            + " ("
            + SuplaContract.SceneEntry._ID
            + " INTEGER PRIMARY KEY, "
            + SuplaContract.SceneEntry.COLUMN_NAME_SCENEID
            + " INTEGER NOT NULL, "
            + SuplaContract.SceneEntry.COLUMN_NAME_LOCATIONID
            + " INTEGER NOT NULL, "
            + SuplaContract.SceneEntry.COLUMN_NAME_ALTICON
            + " INTEGER NOT NULL, "
            + SuplaContract.SceneEntry.COLUMN_NAME_USERICON
            + " INTEGER NOT NULL, "
            + SuplaContract.SceneEntry.COLUMN_NAME_CAPTION
            + " TEXT NOT NULL, "
            + SuplaContract.SceneEntry.COLUMN_NAME_STARTED_AT
            + " INTEGER NULL, "
            + SuplaContract.SceneEntry.COLUMN_NAME_EST_END_DATE
            + " INTEGER NULL, "
            + SuplaContract.SceneEntry.COLUMN_NAME_INITIATOR_ID
            + " INTEGER NULL, "
            + SuplaContract.SceneEntry.COLUMN_NAME_INITIATOR_NAME
            + " TEXT NULL, "
            + SuplaContract.SceneEntry.COLUMN_NAME_SORT_ORDER
            + " INTEGER NOT NULL, "
            + SuplaContract.SceneEntry.COLUMN_NAME_VISIBLE
            + " INTEGER NOT NULL,"
            + SuplaContract.SceneEntry.COLUMN_NAME_PROFILEID
            + " TEXT NULL)";

    execSQL(db, SQL_CREATE_SCENE_TABLE);
    createIndex(
        db, SuplaContract.SceneEntry.TABLE_NAME, SuplaContract.SceneEntry.COLUMN_NAME_SCENEID);
    createIndex(
        db, SuplaContract.SceneEntry.TABLE_NAME, SuplaContract.SceneEntry.COLUMN_NAME_LOCATIONID);
    createIndex(
        db, SuplaContract.SceneEntry.TABLE_NAME, SuplaContract.SceneEntry.COLUMN_NAME_PROFILEID);
  }

  private void createSceneView(SQLiteDatabase db) {
    final String SQL =
        "CREATE VIEW "
            + SuplaContract.SceneViewEntry.VIEW_NAME
            + " AS "
            + "SELECT S."
            + SuplaContract.SceneEntry._ID
            + ", "
            + "S."
            + SuplaContract.SceneEntry.COLUMN_NAME_SCENEID
            + ", "
            + "S."
            + SuplaContract.SceneEntry.COLUMN_NAME_LOCATIONID
            + ", "
            + "S."
            + SuplaContract.SceneEntry.COLUMN_NAME_ALTICON
            + ", "
            + "S."
            + SuplaContract.SceneEntry.COLUMN_NAME_USERICON
            + ", "
            + "S."
            + SuplaContract.SceneEntry.COLUMN_NAME_CAPTION
            + ", "
            + "S."
            + SuplaContract.SceneEntry.COLUMN_NAME_STARTED_AT
            + ", "
            + "S."
            + SuplaContract.SceneEntry.COLUMN_NAME_EST_END_DATE
            + ", "
            + "S."
            + SuplaContract.SceneEntry.COLUMN_NAME_INITIATOR_ID
            + ", "
            + "S."
            + SuplaContract.SceneEntry.COLUMN_NAME_INITIATOR_NAME
            + ", "
            + "S."
            + SuplaContract.SceneEntry.COLUMN_NAME_SORT_ORDER
            + ", "
            + "S."
            + SuplaContract.SceneEntry.COLUMN_NAME_PROFILEID
            + ", "
            + "S."
            + SuplaContract.SceneEntry.COLUMN_NAME_VISIBLE
            + ", "
            + "L."
            + SuplaContract.LocationEntry.COLUMN_NAME_CAPTION
            + " AS  "
            + SuplaContract.SceneViewEntry.COLUMN_NAME_LOCATION_NAME
            + ", "
            + "L."
            + SuplaContract.LocationEntry.COLUMN_NAME_SORT_ORDER
            + " AS "
            + SuplaContract.SceneViewEntry.COLUMN_NAME_LOCATION_SORT_ORDER
            + ", "
            + "L."
            + SuplaContract.LocationEntry.COLUMN_NAME_VISIBLE
            + " AS "
            + SuplaContract.SceneViewEntry.COLUMN_NAME_LOCATION_VISIBLE
            + " FROM "
            + SuplaContract.SceneEntry.TABLE_NAME
            + " S"
            + " JOIN "
            + SuplaContract.LocationEntry.TABLE_NAME
            + " L ON ("
            + "S."
            + SuplaContract.SceneEntry.COLUMN_NAME_LOCATIONID
            + " = L."
            + SuplaContract.LocationEntry.COLUMN_NAME_LOCATIONID
            + " AND "
            + "S."
            + SuplaContract.SceneEntry.COLUMN_NAME_PROFILEID
            + " = L."
            + SuplaContract.LocationEntry.COLUMN_NAME_PROFILEID
            + ")";

    execSQL(db, SQL);
  }

  private void createChannelTable(SQLiteDatabase db) {

    final String SQL_CREATE_CHANNEL_TABLE =
        "CREATE TABLE "
            + SuplaContract.ChannelEntry.TABLE_NAME
            + " ("
            + SuplaContract.ChannelEntry._ID
            + " INTEGER PRIMARY KEY,"
            + SuplaContract.ChannelEntry.COLUMN_NAME_CHANNELID
            + " INTEGER NOT NULL,"
            + SuplaContract.ChannelEntry.COLUMN_NAME_DEVICEID
            + " INTEGER NULL,"
            + SuplaContract.ChannelEntry.COLUMN_NAME_CAPTION
            + " TEXT NOT NULL,"
            + SuplaContract.ChannelEntry.COLUMN_NAME_TYPE
            + " INTEGER NOT NULL,"
            + SuplaContract.ChannelEntry.COLUMN_NAME_FUNC
            + " INTEGER NOT NULL,"
            + SuplaContract.ChannelEntry.COLUMN_NAME_VISIBLE
            + " INTEGER NOT NULL,"
            + SuplaContract.ChannelEntry.COLUMN_NAME_LOCATIONID
            + " INTEGER NOT NULL,"
            + SuplaContract.ChannelEntry.COLUMN_NAME_ALTICON
            + " INTEGER NOT NULL,"
            + SuplaContract.ChannelEntry.COLUMN_NAME_USERICON
            + " INTEGER NOT NULL,"
            + SuplaContract.ChannelEntry.COLUMN_NAME_MANUFACTURERID
            + " SMALLINT NOT NULL,"
            + SuplaContract.ChannelEntry.COLUMN_NAME_PRODUCTID
            + " SMALLINT NOT NULL,"
            + SuplaContract.ChannelEntry.COLUMN_NAME_FLAGS
            + " INTEGER NOT NULL,"
            + SuplaContract.ChannelEntry.COLUMN_NAME_PROTOCOLVERSION
            + " INTEGER NOT NULL,"
            + SuplaContract.ChannelEntry.COLUMN_NAME_POSITION
            + " INTEGER NOT NULL default 0)";

    execSQL(db, SQL_CREATE_CHANNEL_TABLE);
    createIndex(
        db,
        SuplaContract.ChannelEntry.TABLE_NAME,
        SuplaContract.ChannelEntry.COLUMN_NAME_CHANNELID);
    createIndex(
        db,
        SuplaContract.ChannelEntry.TABLE_NAME,
        SuplaContract.ChannelEntry.COLUMN_NAME_LOCATIONID);
  }

  private void createChannelValueTable(SQLiteDatabase db) {

    final String SQL_CREATE_CHANNELVALUE_TABLE =
        "CREATE TABLE "
            + SuplaContract.ChannelValueEntry.TABLE_NAME
            + " ("
            + SuplaContract.ChannelValueEntry._ID
            + " INTEGER PRIMARY KEY,"
            + SuplaContract.ChannelValueEntry.COLUMN_NAME_CHANNELID
            + " INTEGER NOT NULL,"
            + SuplaContract.ChannelValueEntry.COLUMN_NAME_ONLINE
            + " INTEGER NOT NULL,"
            + SuplaContract.ChannelValueEntry.COLUMN_NAME_SUBVALUE_TYPE
            + " INTEGER NOT NULL,"
            + SuplaContract.ChannelValueEntry.COLUMN_NAME_SUBVALUE
            + " TEXT,"
            + SuplaContract.ChannelValueEntry.COLUMN_NAME_VALUE
            + " TEXT)";

    execSQL(db, SQL_CREATE_CHANNELVALUE_TABLE);
    createIndex(
        db,
        SuplaContract.ChannelValueEntry.TABLE_NAME,
        SuplaContract.ChannelValueEntry.COLUMN_NAME_CHANNELID);
  }

  private void createChannelExtendedValueTable(SQLiteDatabase db) {

    final String SQL_CREATE_CHANNELEXTENDEDVALUE_TABLE =
        "CREATE TABLE "
            + SuplaContract.ChannelExtendedValueEntry.TABLE_NAME
            + " ("
            + SuplaContract.ChannelExtendedValueEntry._ID
            + " INTEGER PRIMARY KEY,"
            + SuplaContract.ChannelExtendedValueEntry.COLUMN_NAME_CHANNELID
            + " INTEGER NOT NULL,"
            + SuplaContract.ChannelExtendedValueEntry.COLUMN_NAME_VALUE
            + " BLOB,"
            + SuplaContract.ChannelExtendedValueEntry.COLUMN_NAME_TIMER_START_TIME
            + " INTEGER"
            + ")";

    execSQL(db, SQL_CREATE_CHANNELEXTENDEDVALUE_TABLE);
    createIndex(
        db,
        SuplaContract.ChannelExtendedValueEntry.TABLE_NAME,
        SuplaContract.ChannelExtendedValueEntry.COLUMN_NAME_CHANNELID);
  }

  private void createAuthProfileTable(SQLiteDatabase db) {
    final String SQL_CREATE_AUTHPROFILE_TABLE =
        "CREATE TABLE "
            + SuplaContract.AuthProfileEntry.TABLE_NAME
            + " ("
            + SuplaContract.AuthProfileEntry._ID
            + " INTEGER PRIMARY KEY,"
            + SuplaContract.AuthProfileEntry.COLUMN_NAME_PROFILE_NAME
            + " TEXT NOT NULL UNIQUE,"
            + SuplaContract.AuthProfileEntry.COLUMN_NAME_EMAIL_ADDR
            + " TEXT,"
            + SuplaContract.AuthProfileEntry.COLUMN_NAME_SERVER_ADDR_ACCESS_ID
            + " TEXT,"
            + SuplaContract.AuthProfileEntry.COLUMN_NAME_SERVER_ADDR_EMAIL
            + " TEXT,"
            + SuplaContract.AuthProfileEntry.COLUMN_NAME_SERVER_AUTO_DETECT
            + " INTEGER NOT NULL,"
            + SuplaContract.AuthProfileEntry.COLUMN_NAME_EMAIL_AUTH
            + " INTEGER NOT NULL,"
            + SuplaContract.AuthProfileEntry.COLUMN_NAME_ACCESS_ID
            + " INTEGER,"
            + SuplaContract.AuthProfileEntry.COLUMN_NAME_ACCESS_ID_PWD
            + " TEXT,"
            + SuplaContract.AuthProfileEntry.COLUMN_NAME_PREFERRED_PROTOCOL_VERSION
            + " INTEGER,"
            + SuplaContract.AuthProfileEntry.COLUMN_NAME_IS_ACTIVE
            + " INTEGER,"
            + SuplaContract.AuthProfileEntry.COLUMN_NAME_IS_ADVANCED_MODE
            + " INTEGER)";

    execSQL(db, SQL_CREATE_AUTHPROFILE_TABLE);
  }

  private void createChannelView(SQLiteDatabase db) {

    final String SQL_CREATE_CHANNELVALUE_TABLE =
        "CREATE VIEW "
            + SuplaContract.ChannelViewEntry.VIEW_NAME
            + " AS "
            + "SELECT C."
            + SuplaContract.ChannelEntry._ID
            + ", "
            + "C."
            + SuplaContract.ChannelEntry.COLUMN_NAME_DEVICEID
            + ", "
            + "C."
            + SuplaContract.ChannelEntry.COLUMN_NAME_CHANNELID
            + ", "
            + "C."
            + SuplaContract.ChannelEntry.COLUMN_NAME_CAPTION
            + ", "
            + "C."
            + SuplaContract.ChannelEntry.COLUMN_NAME_PROFILEID
            + ", "
            + "CV."
            + SuplaContract.ChannelValueEntry._ID
            + ", "
            + "CEV."
            + SuplaContract.ChannelExtendedValueEntry._ID
            + ", "
            + "CV."
            + SuplaContract.ChannelValueEntry.COLUMN_NAME_ONLINE
            + ", "
            + "CV."
            + SuplaContract.ChannelValueEntry.COLUMN_NAME_SUBVALUE_TYPE
            + ", "
            + "CV."
            + SuplaContract.ChannelValueEntry.COLUMN_NAME_SUBVALUE
            + ", "
            + "CV."
            + SuplaContract.ChannelValueEntry.COLUMN_NAME_VALUE
            + ", "
            + "CEV."
            + SuplaContract.ChannelExtendedValueEntry.COLUMN_NAME_VALUE
            + ", "
            + "CEV."
            + SuplaContract.ChannelExtendedValueEntry.COLUMN_NAME_TIMER_START_TIME
            + ", "
            + "C."
            + SuplaContract.ChannelEntry.COLUMN_NAME_TYPE
            + ", "
            + "C."
            + SuplaContract.ChannelEntry.COLUMN_NAME_FUNC
            + ", "
            + "C."
            + SuplaContract.ChannelEntry.COLUMN_NAME_VISIBLE
            + ", "
            + "C."
            + SuplaContract.ChannelEntry.COLUMN_NAME_LOCATIONID
            + ", "
            + "C."
            + SuplaContract.ChannelEntry.COLUMN_NAME_ALTICON
            + ", "
            + "C."
            + SuplaContract.ChannelEntry.COLUMN_NAME_USERICON
            + ", "
            + "C."
            + SuplaContract.ChannelEntry.COLUMN_NAME_MANUFACTURERID
            + ", "
            + "C."
            + SuplaContract.ChannelEntry.COLUMN_NAME_PRODUCTID
            + ", "
            + "C."
            + SuplaContract.ChannelEntry.COLUMN_NAME_FLAGS
            + ", "
            + "C."
            + SuplaContract.ChannelEntry.COLUMN_NAME_PROTOCOLVERSION
            + ", "
            + "C."
            + SuplaContract.ChannelEntry.COLUMN_NAME_POSITION
            + ", "
            + "I."
            + SuplaContract.UserIconsEntry.COLUMN_NAME_IMAGE1
            + " "
            + SuplaContract.ChannelViewEntry.COLUMN_NAME_USERICON_IMAGE1
            + ", "
            + "I."
            + SuplaContract.UserIconsEntry.COLUMN_NAME_IMAGE2
            + " "
            + SuplaContract.ChannelViewEntry.COLUMN_NAME_USERICON_IMAGE2
            + ", "
            + "I."
            + SuplaContract.UserIconsEntry.COLUMN_NAME_IMAGE3
            + " "
            + SuplaContract.ChannelViewEntry.COLUMN_NAME_USERICON_IMAGE3
            + ", "
            + "I."
            + SuplaContract.UserIconsEntry.COLUMN_NAME_IMAGE4
            + " "
            + SuplaContract.ChannelViewEntry.COLUMN_NAME_USERICON_IMAGE4
            + " "
            + "FROM "
            + SuplaContract.ChannelEntry.TABLE_NAME
            + " C "
            + "JOIN "
            + SuplaContract.ChannelValueEntry.TABLE_NAME
            + " CV ON ("
            + "C."
            + SuplaContract.ChannelEntry.COLUMN_NAME_CHANNELID
            + " = CV."
            + SuplaContract.ChannelValueEntry.COLUMN_NAME_CHANNELID
            + " AND "
            + "C."
            + SuplaContract.ChannelEntry.COLUMN_NAME_PROFILEID
            + " = CV."
            + SuplaContract.ChannelValueEntry.COLUMN_NAME_PROFILEID
            + ") "
            + "LEFT JOIN "
            + SuplaContract.ChannelExtendedValueEntry.TABLE_NAME
            + " CEV ON "
            + "(C."
            + SuplaContract.ChannelEntry.COLUMN_NAME_CHANNELID
            + " = CEV."
            + SuplaContract.ChannelExtendedValueEntry.COLUMN_NAME_CHANNELID
            + " AND "
            + "C."
            + SuplaContract.ChannelEntry.COLUMN_NAME_PROFILEID
            + " = CEV."
            + SuplaContract.ChannelExtendedValueEntry.COLUMN_NAME_PROFILEID
            + ") "
            + "LEFT JOIN "
            + SuplaContract.UserIconsEntry.TABLE_NAME
            + " I ON "
            + "(C."
            + SuplaContract.ChannelEntry.COLUMN_NAME_USERICON
            + " = I."
            + SuplaContract.UserIconsEntry.COLUMN_NAME_REMOTEID
            + " AND "
            + "C."
            + SuplaContract.ChannelEntry.COLUMN_NAME_PROFILEID
            + " = I."
            + SuplaContract.UserIconsEntry.COLUMN_NAME_PROFILEID
            + ")";

    execSQL(db, SQL_CREATE_CHANNELVALUE_TABLE);
  }

  private void createColorTable(SQLiteDatabase db) {

    final String SQL_CREATE_COLOR_TABLE =
        "CREATE TABLE "
            + SuplaContract.ColorListItemEntry.TABLE_NAME
            + " ("
            + SuplaContract.ColorListItemEntry._ID
            + " INTEGER PRIMARY KEY,"
            + SuplaContract.ColorListItemEntry.COLUMN_NAME_REMOTEID
            + " INTEGER NOT NULL,"
            + SuplaContract.ColorListItemEntry.COLUMN_NAME_GROUP
            + " INTEGER NOT NULL,"
            + SuplaContract.ColorListItemEntry.COLUMN_NAME_IDX
            + " INTEGER NOT NULL,"
            + SuplaContract.ColorListItemEntry.COLUMN_NAME_COLOR
            + " INTEGER NOT NULL,"
            + SuplaContract.ColorListItemEntry.COLUMN_NAME_BRIGHTNESS
            + " INTEGER NOT NULL)";

    execSQL(db, SQL_CREATE_COLOR_TABLE);
    createIndex(
        db,
        SuplaContract.ColorListItemEntry.TABLE_NAME,
        SuplaContract.ColorListItemEntry.COLUMN_NAME_REMOTEID);
    createIndex(
        db,
        SuplaContract.ColorListItemEntry.TABLE_NAME,
        SuplaContract.ColorListItemEntry.COLUMN_NAME_GROUP);
  }

  private void createChannelGroupTable(SQLiteDatabase db) {

    final String SQL_CREATE_CHANNELGROUP_TABLE =
        "CREATE TABLE "
            + SuplaContract.ChannelGroupEntry.TABLE_NAME
            + " ("
            + SuplaContract.ChannelGroupEntry._ID
            + " INTEGER PRIMARY KEY,"
            + SuplaContract.ChannelGroupEntry.COLUMN_NAME_GROUPID
            + " INTEGER NOT NULL,"
            + SuplaContract.ChannelGroupEntry.COLUMN_NAME_CAPTION
            + " TEXT NOT NULL,"
            + SuplaContract.ChannelGroupEntry.COLUMN_NAME_ONLINE
            + " INTEGER NOT NULL,"
            + SuplaContract.ChannelGroupEntry.COLUMN_NAME_FUNC
            + " INTEGER NOT NULL,"
            + SuplaContract.ChannelGroupEntry.COLUMN_NAME_VISIBLE
            + " INTEGER NOT NULL,"
            + SuplaContract.ChannelGroupEntry.COLUMN_NAME_LOCATIONID
            + " INTEGER NOT NULL,"
            + SuplaContract.ChannelGroupEntry.COLUMN_NAME_ALTICON
            + " INTEGER NOT NULL,"
            + SuplaContract.ChannelGroupEntry.COLUMN_NAME_USERICON
            + " INTEGER NOT NULL,"
            + SuplaContract.ChannelGroupEntry.COLUMN_NAME_FLAGS
            + " INTEGER NOT NULL,"
            + SuplaContract.ChannelGroupEntry.COLUMN_NAME_TOTALVALUE
            + " TEXT, "
            + SuplaContract.ChannelGroupEntry.COLUMN_NAME_POSITION
            + " INTEGER NOT NULL default 0)";

    execSQL(db, SQL_CREATE_CHANNELGROUP_TABLE);
    createIndex(
        db,
        SuplaContract.ChannelGroupEntry.TABLE_NAME,
        SuplaContract.ChannelGroupEntry.COLUMN_NAME_GROUPID);
    createIndex(
        db,
        SuplaContract.ChannelGroupEntry.TABLE_NAME,
        SuplaContract.ChannelGroupEntry.COLUMN_NAME_LOCATIONID);
  }

  private void createChannelGroupRelationTable(SQLiteDatabase db) {

    final String SQL_CREATE_CHANNELGROUP_REL_TABLE =
        "CREATE TABLE "
            + SuplaContract.ChannelGroupRelationEntry.TABLE_NAME
            + " ("
            + SuplaContract.ChannelGroupRelationEntry._ID
            + " INTEGER PRIMARY KEY,"
            + SuplaContract.ChannelGroupRelationEntry.COLUMN_NAME_GROUPID
            + " INTEGER NOT NULL,"
            + SuplaContract.ChannelGroupRelationEntry.COLUMN_NAME_CHANNELID
            + " INTEGER NOT NULL,"
            + SuplaContract.ChannelGroupRelationEntry.COLUMN_NAME_VISIBLE
            + " INTEGER NOT NULL)";

    execSQL(db, SQL_CREATE_CHANNELGROUP_REL_TABLE);
    createIndex(
        db,
        SuplaContract.ChannelGroupRelationEntry.TABLE_NAME,
        SuplaContract.ChannelGroupRelationEntry.COLUMN_NAME_GROUPID);
    createIndex(
        db,
        SuplaContract.ChannelGroupRelationEntry.TABLE_NAME,
        SuplaContract.ChannelGroupRelationEntry.COLUMN_NAME_CHANNELID);
  }

  private void createChannelGroupValueView(SQLiteDatabase db) {

    final String SQL_CREATE_CHANNELGROUP_VALUE_VIEW =
        "CREATE VIEW "
            + SuplaContract.ChannelGroupValueViewEntry.VIEW_NAME
            + " AS "
            + "SELECT V."
            + SuplaContract.ChannelGroupValueViewEntry._ID
            + " "
            + SuplaContract.ChannelGroupValueViewEntry._ID
            + ", "
            + "V."
            + SuplaContract.ChannelGroupValueViewEntry.COLUMN_NAME_PROFILEID
            + ", "
            + " V."
            + SuplaContract.ChannelGroupValueViewEntry.COLUMN_NAME_PROFILEID
            + ", "
            + " G."
            + SuplaContract.ChannelGroupValueViewEntry.COLUMN_NAME_GROUPID
            + " "
            + SuplaContract.ChannelGroupValueViewEntry.COLUMN_NAME_GROUPID
            + ", "
            + "G."
            + SuplaContract.ChannelGroupValueViewEntry.COLUMN_NAME_FUNC
            + " "
            + SuplaContract.ChannelGroupValueViewEntry.COLUMN_NAME_FUNC
            + ", "
            + "R."
            + SuplaContract.ChannelGroupValueViewEntry.COLUMN_NAME_CHANNELID
            + " "
            + SuplaContract.ChannelGroupValueViewEntry.COLUMN_NAME_CHANNELID
            + ", "
            + "V."
            + SuplaContract.ChannelGroupValueViewEntry.COLUMN_NAME_ONLINE
            + " "
            + SuplaContract.ChannelGroupValueViewEntry.COLUMN_NAME_ONLINE
            + ", "
            + "V."
            + SuplaContract.ChannelGroupValueViewEntry.COLUMN_NAME_SUBVALUE
            + " "
            + SuplaContract.ChannelGroupValueViewEntry.COLUMN_NAME_SUBVALUE
            + ", "
            + "V."
            + SuplaContract.ChannelGroupValueViewEntry.COLUMN_NAME_SUBVALUE_TYPE
            + " "
            + SuplaContract.ChannelGroupValueViewEntry.COLUMN_NAME_SUBVALUE_TYPE
            + ", "
            + "V."
            + SuplaContract.ChannelGroupValueViewEntry.COLUMN_NAME_VALUE
            + " "
            + SuplaContract.ChannelGroupValueViewEntry.COLUMN_NAME_VALUE
            + " "
            + " FROM "
            + SuplaContract.ChannelGroupRelationEntry.TABLE_NAME
            + " R "
            + " JOIN "
            + SuplaContract.ChannelGroupEntry.TABLE_NAME
            + " G ON G."
            + SuplaContract.ChannelGroupEntry.COLUMN_NAME_GROUPID
            + " = R."
            + SuplaContract.ChannelGroupRelationEntry.COLUMN_NAME_GROUPID
            + " JOIN "
            + SuplaContract.ChannelValueEntry.TABLE_NAME
            + " V ON V."
            + SuplaContract.ChannelValueEntry.COLUMN_NAME_CHANNELID
            + " = R."
            + SuplaContract.ChannelGroupRelationEntry.COLUMN_NAME_CHANNELID
            + " WHERE R."
            + SuplaContract.ChannelGroupRelationEntry.COLUMN_NAME_VISIBLE
            + " > 0 AND "
            + "G."
            + SuplaContract.ChannelGroupEntry.COLUMN_NAME_VISIBLE
            + " > 0";

    execSQL(db, SQL_CREATE_CHANNELGROUP_VALUE_VIEW);
  }

  private void createUserIconsTable(SQLiteDatabase db) {

    final String SQL_CREATE_IMAGE_TABLE =
        "CREATE TABLE "
            + SuplaContract.UserIconsEntry.TABLE_NAME
            + " ("
            + SuplaContract.UserIconsEntry._ID
            + " INTEGER PRIMARY KEY,"
            + SuplaContract.UserIconsEntry.COLUMN_NAME_REMOTEID
            + " INTEGER NOT NULL,"
            + SuplaContract.UserIconsEntry.COLUMN_NAME_IMAGE1
            + " BLOB,"
            + SuplaContract.UserIconsEntry.COLUMN_NAME_IMAGE2
            + " BLOB,"
            + SuplaContract.UserIconsEntry.COLUMN_NAME_IMAGE3
            + " BLOB,"
            + SuplaContract.UserIconsEntry.COLUMN_NAME_IMAGE4
            + " BLOB)";

    execSQL(db, SQL_CREATE_IMAGE_TABLE);
    createIndex(
        db,
        SuplaContract.UserIconsEntry.TABLE_NAME,
        SuplaContract.UserIconsEntry.COLUMN_NAME_REMOTEID);

    final String SQL_CREATE_INDEX =
        "CREATE UNIQUE INDEX "
            + SuplaContract.UserIconsEntry.TABLE_NAME
            + "_unique_index ON "
            + SuplaContract.UserIconsEntry.TABLE_NAME
            + "("
            + SuplaContract.UserIconsEntry.COLUMN_NAME_REMOTEID
            + ")";

    execSQL(db, SQL_CREATE_INDEX);
  }

  private void createChannelRelationTable(SQLiteDatabase db) {

    final String SQL_CREATE_CHANNEL_RELATION_TABLE =
        "CREATE TABLE "
            + ChannelRelationEntity.TABLE_NAME
            + " ("
            + ChannelRelationEntity.COLUMN_CHANNEL_ID
            + " INTEGER NOT NULL,"
            + ChannelRelationEntity.COLUMN_PARENT_ID
            + " INTEGER NOT NULL,"
            + ChannelRelationEntity.COLUMN_CHANNEL_RELATION_TYPE
            + " INTEGER NOT NULL,"
            + ChannelRelationEntity.COLUMN_PROFILE_ID
            + " INTEGER NOT NULL,"
            + ChannelRelationEntity.COLUMN_DELETE_FLAG
            + " INTEGER NOT NULL,"
            + " PRIMARY KEY ("
            + ChannelRelationEntity.COLUMN_CHANNEL_ID
            + ","
            + ChannelRelationEntity.COLUMN_PARENT_ID
            + ","
            + ChannelRelationEntity.COLUMN_PROFILE_ID
            + "))";

    execSQL(db, SQL_CREATE_CHANNEL_RELATION_TABLE);
  }

  private void insertDefaultProfile(SQLiteDatabase db) {
    ProfileMigrator migrator = new ProfileMigrator(SuplaApp.getApp());
    AuthProfileItem itm = migrator.makeProfileUsingPreferences();
    if (itm != null) {
      db.insert(SuplaContract.AuthProfileEntry.TABLE_NAME, null, itm.getContentValuesV22());
    }
  }

  private void alterTablesToReferProfile(SQLiteDatabase db) {
    /* NOTE: V19 doesn't have relationships yet. */
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    createLocationTable(db);
    createChannelTable(db);
    createChannelValueTable(db);
    createColorTable(db);
    createChannelGroupTable(db);
    createChannelGroupRelationTable(db);
    createChannelExtendedValueTable(db);
    createUserIconsTable(db);
    upgradeToV19(db);
    upgradeToV22(db);
    upgradeToV23(db);
    upgradeToV24(db);
    // Do not call upgradeToV25(db) here, it relates only for migration (see onUpgrade)
    upgradeToV26(db);
    upgradeToV27(db);

    createChannelRelationTable(db);

    // Create views at the end
    createChannelView(db);
    createChannelGroupValueView(db);
  }

  private void upgradeToV2(SQLiteDatabase db) {
    Trace.d(DbHelper.class.getName(), "upgradeToV2");
    createColorTable(db);
  }

  private void upgradeToV3(SQLiteDatabase db) {
    Trace.d(DbHelper.class.getName(), "upgradeToV3");
    addColumn(
        db,
        "ALTER TABLE "
            + SuplaContract.ChannelEntry.TABLE_NAME
            + " ADD COLUMN "
            + SuplaContract.ChannelEntry.COLUMN_NAME_ALTICON
            + " INTEGER NOT NULL default 0");

    addColumn(
        db,
        "ALTER TABLE "
            + SuplaContract.ChannelEntry.TABLE_NAME
            + " ADD COLUMN "
            + SuplaContract.ChannelEntry.COLUMN_NAME_FLAGS
            + " INTEGER NOT NULL default 0");

    addColumn(
        db,
        "ALTER TABLE "
            + SuplaContract.ChannelEntry.TABLE_NAME
            + " ADD COLUMN "
            + SuplaContract.ChannelEntry.COLUMN_NAME_PROTOCOLVERSION
            + " INTEGER NOT NULL default 0");
  }

  private void upgradeToV4(SQLiteDatabase db) {
    Trace.d(DbHelper.class.getName(), "upgradeToV4");
    execSQL(
        db,
        "ALTER TABLE "
            + SuplaContract.ColorListItemEntry.TABLE_NAME
            + " RENAME TO "
            + SuplaContract.ColorListItemEntry.TABLE_NAME
            + "_old");

    createColorTable(db);

    final String SQL_COPY =
        "INSERT INTO "
            + SuplaContract.ColorListItemEntry.TABLE_NAME
            + " ("
            + SuplaContract.ColorListItemEntry.COLUMN_NAME_REMOTEID
            + ","
            + SuplaContract.ColorListItemEntry.COLUMN_NAME_GROUP
            + ","
            + SuplaContract.ColorListItemEntry.COLUMN_NAME_IDX
            + ","
            + SuplaContract.ColorListItemEntry.COLUMN_NAME_COLOR
            + ","
            + SuplaContract.ColorListItemEntry.COLUMN_NAME_BRIGHTNESS
            + ") "
            + "SELECT c.channelid, 0, ci.idx, ci.color, ci.brightness FROM channel c JOIN color_list_item_old ci ON c._id = ci.channel";

    execSQL(db, SQL_COPY);

    execSQL(db, "DROP TABLE " + SuplaContract.ColorListItemEntry.TABLE_NAME + "_old");

    execSQL(db, "DROP TABLE accessid");
    execSQL(db, "DROP TABLE " + SuplaContract.LocationEntry.TABLE_NAME);
    execSQL(db, "DROP TABLE " + SuplaContract.ChannelEntry.TABLE_NAME);

    createLocationTable(db);
    createChannelTable(db);
    createChannelValueTable(db);
    createChannelGroupTable(db);
    createChannelGroupRelationTable(db);
  }

  private void upgradeToV5(SQLiteDatabase db) {
    Trace.d(DbHelper.class.getName(), "upgradeToV5");
    createChannelExtendedValueTable(db);
  }

  private void upgradeToV6(SQLiteDatabase db) {
    Trace.d(DbHelper.class.getName(), "upgradeToV6");

    addColumn(
        db,
        "ALTER TABLE "
            + SuplaContract.ChannelEntry.TABLE_NAME
            + " ADD COLUMN "
            + SuplaContract.ChannelEntry.COLUMN_NAME_DEVICEID
            + " INTEGER NOT NULL default 0");

    addColumn(
        db,
        "ALTER TABLE "
            + SuplaContract.ChannelEntry.TABLE_NAME
            + " ADD COLUMN "
            + SuplaContract.ChannelEntry.COLUMN_NAME_USERICON
            + " INTEGER NOT NULL default 0");

    addColumn(
        db,
        "ALTER TABLE "
            + SuplaContract.ChannelEntry.TABLE_NAME
            + " ADD COLUMN "
            + SuplaContract.ChannelEntry.COLUMN_NAME_MANUFACTURERID
            + " SMALLINT NOT NULL default 0");

    addColumn(
        db,
        "ALTER TABLE "
            + SuplaContract.ChannelEntry.TABLE_NAME
            + " ADD COLUMN "
            + SuplaContract.ChannelEntry.COLUMN_NAME_TYPE
            + " INTEGER NOT NULL default 0");

    addColumn(
        db,
        "ALTER TABLE "
            + SuplaContract.ChannelEntry.TABLE_NAME
            + " ADD COLUMN "
            + SuplaContract.ChannelEntry.COLUMN_NAME_PRODUCTID
            + " SMALLINT NOT NULL default 0");

    addColumn(
        db,
        "ALTER TABLE "
            + SuplaContract.ChannelGroupEntry.TABLE_NAME
            + " ADD COLUMN "
            + SuplaContract.ChannelEntry.COLUMN_NAME_USERICON
            + " INTEGER NOT NULL default 0");

    addColumn(
        db,
        "ALTER TABLE "
            + SuplaContract.LocationEntry.TABLE_NAME
            + " ADD COLUMN "
            + SuplaContract.LocationEntry.COLUMN_NAME_COLLAPSED
            + " INTEGER NOT NULL default 0");
  }

  private void upgradeToV8(SQLiteDatabase db) {
    Trace.d(DbHelper.class.getName(), "upgradeToV8");
    createUserIconsTable(db);
  }

  private void upgradeToV9(SQLiteDatabase db) {
    Trace.d(DbHelper.class.getName(), "upgradeToV9");
    execSQL(db, "DROP TABLE " + SuplaContract.ChannelValueEntry.TABLE_NAME);
    execSQL(db, "DROP TABLE " + SuplaContract.ChannelExtendedValueEntry.TABLE_NAME);
    createChannelValueTable(db);
    createChannelExtendedValueTable(db);
  }

  private void upgradeToV13(SQLiteDatabase db) {
    // Views are recreated at the end of each database structure update
    // recreateViews(db);
  }

  private void upgradeToV15(SQLiteDatabase db) {
    addColumn(
        db,
        "ALTER TABLE "
            + SuplaContract.ChannelEntry.TABLE_NAME
            + " ADD COLUMN "
            + SuplaContract.ChannelEntry.COLUMN_NAME_POSITION
            + " INTEGER NOT NULL default 0");
    addColumn(
        db,
        "ALTER TABLE "
            + SuplaContract.LocationEntry.TABLE_NAME
            + " ADD COLUMN "
            + SuplaContract.LocationEntry.COLUMN_NAME_SORTING
            + " TEXT NOT NULL default 'DEFAULT'");
  }

  private void upgradeToV16(SQLiteDatabase db) {
    addColumn(
        db,
        "ALTER TABLE "
            + SuplaContract.ChannelGroupEntry.TABLE_NAME
            + " ADD COLUMN "
            + SuplaContract.ChannelGroupEntry.COLUMN_NAME_POSITION
            + " INTEGER NOT NULL default 0");
  }

  private void upgradeToV17(SQLiteDatabase db) {
    addColumn(
        db,
        "ALTER TABLE "
            + SuplaContract.ChannelValueEntry.TABLE_NAME
            + " ADD COLUMN "
            + SuplaContract.ChannelValueEntry.COLUMN_NAME_SUBVALUE_TYPE
            + " INTEGER NOT NULL default 0");
  }

  private void upgradeToV19(SQLiteDatabase db) {
    createAuthProfileTable(db);
    insertDefaultProfile(db);
    alterTablesToReferProfile(db);
  }

  private void upgradeToV20(SQLiteDatabase db) {
    dropViews(db);
    execSQL(db, "DROP TABLE " + SuplaContract.ChannelExtendedValueEntry.TABLE_NAME);
    createChannelExtendedValueTable(db);
  }

  private void upgradeToV21(SQLiteDatabase db) {
    addColumn(
        db,
        "ALTER TABLE "
            + SuplaContract.LocationEntry.TABLE_NAME
            + " ADD COLUMN "
            + SuplaContract.LocationEntry.COLUMN_NAME_SORT_ORDER
            + " INTEGER NOT NULL DEFAULT -1");
  }

  private void upgradeToV22(SQLiteDatabase db) {
    ContentValues cv = new ContentValues();
    cv.put(
        SuplaContract.AuthProfileEntry.COLUMN_NAME_PROFILE_NAME,
        getContext().getText(R.string.profile_default_name).toString());
    db.update(SuplaContract.AuthProfileEntry.TABLE_NAME, cv, null, null);
    String column_name = "profileid";
    String tables[] = {
      SuplaContract.LocationEntry.TABLE_NAME,
      SuplaContract.ChannelEntry.TABLE_NAME,
      SuplaContract.ChannelValueEntry.TABLE_NAME,
      SuplaContract.ChannelExtendedValueEntry.TABLE_NAME,
      SuplaContract.ColorListItemEntry.TABLE_NAME,
      SuplaContract.ChannelGroupEntry.TABLE_NAME,
      SuplaContract.ChannelGroupRelationEntry.TABLE_NAME,
      SuplaContract.UserIconsEntry.TABLE_NAME
    };

    for (String table : tables) {
      addColumn(
          db,
          "ALTER TABLE " + table + " ADD COLUMN " + column_name + " INTEGER NOT NULL DEFAULT 1");
      createIndex(db, table, column_name);
    }
  }

  private void updateEncryptBlob(SQLiteDatabase db, String table, String column, byte[] payload) {
    ContentValues cv = new ContentValues();
    byte[] encrypted;
    String key = Preferences.getDeviceID(getContext());

    if (payload == null) {
      encrypted = null;
    } else {
      encrypted = Encryption.encryptDataWithNullOnException(payload, key);
    }
    if (encrypted == null) {
      cv.putNull(column);
    } else {
      cv.put(column, encrypted);
    }

    db.update(table, cv, null, null);
  }

  private void upgradeToV23(SQLiteDatabase db) {
    addColumn(
        db,
        "ALTER TABLE "
            + SuplaContract.AuthProfileEntry.TABLE_NAME
            + " ADD COLUMN "
            + SuplaContract.AuthProfileEntry.COLUMN_NAME_GUID
            + " BLOB");
    addColumn(
        db,
        "ALTER TABLE "
            + SuplaContract.AuthProfileEntry.TABLE_NAME
            + " ADD COLUMN "
            + SuplaContract.AuthProfileEntry.COLUMN_NAME_AUTHKEY
            + " BLOB");

    Preferences prefs = new Preferences(getContext());

    // Migrate existing guid / authkey if any
    updateEncryptBlob(
        db,
        SuplaContract.AuthProfileEntry.TABLE_NAME,
        SuplaContract.AuthProfileEntry.COLUMN_NAME_GUID,
        prefs.getClientGUID());
    updateEncryptBlob(
        db,
        SuplaContract.AuthProfileEntry.TABLE_NAME,
        SuplaContract.AuthProfileEntry.COLUMN_NAME_AUTHKEY,
        prefs.getAuthKey());
  }

  private void upgradeToV24(SQLiteDatabase db) {
    String indexName = SuplaContract.UserIconsEntry.TABLE_NAME + "_unique_index";

    execSQL(db, "DELETE FROM " + SuplaContract.UserIconsEntry.TABLE_NAME);
    execSQL(db, "DROP INDEX " + indexName);

    execSQL(
        db,
        "CREATE UNIQUE INDEX "
            + indexName
            + " ON "
            + SuplaContract.UserIconsEntry.TABLE_NAME
            + "("
            + SuplaContract.UserIconsEntry.COLUMN_NAME_REMOTEID
            + ", "
            + SuplaContract.UserIconsEntry.COLUMN_NAME_PROFILEID
            + ")");
  }

  private void upgradeToV25(SQLiteDatabase db) {
    // Related to:
    // https://github.com/SUPLA/supla-core/commit/2a2f2cec89e4e20a49d37e4e89306cf178dd1d05
    execSQL(
        db,
        "UPDATE "
            + SuplaContract.AuthProfileEntry.TABLE_NAME
            + " "
            + "SET "
            + SuplaContract.AuthProfileEntry.COLUMN_NAME_AUTHKEY
            + " = "
            + SuplaContract.AuthProfileEntry.COLUMN_NAME_GUID);
  }

  private void upgradeToV26(SQLiteDatabase db) {
    createSceneTable(db);
    createSceneView(db);
  }

  private void upgradeToV27(SQLiteDatabase db) {
    MigratorV27 migrator = new MigratorV27(db, getContext());
    migrator.migrateUserProfiles();
    migrator.migrateScenesDates(
        () -> {
          createSceneTable(db);
          createSceneView(db);
        });
  }

  private void upgradeToV28(SQLiteDatabase db) {
    addColumn(
        db,
        "ALTER TABLE "
            + ChannelExtendedValueEntry.TABLE_NAME
            + " ADD COLUMN "
            + ChannelExtendedValueEntry.COLUMN_NAME_TIMER_START_TIME
            + " INTEGER");
    execSQL(db, "DROP VIEW IF EXISTS " + SuplaContract.ChannelViewEntry.VIEW_NAME);
    createChannelView(db);
  }

  private void upgradeToV29(SQLiteDatabase db) {
    createChannelRelationTable(db);
  }

  private void dropViews(SQLiteDatabase db) {
    execSQL(db, "DROP VIEW IF EXISTS " + SuplaContract.ChannelViewEntry.VIEW_NAME);
    execSQL(db, "DROP VIEW IF EXISTS " + SuplaContract.ChannelGroupValueViewEntry.VIEW_NAME);
  }

  private void recreateViews(SQLiteDatabase db) {
    dropViews(db);
    createChannelView(db);
    createChannelGroupValueView(db);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    if (oldVersion < newVersion) {

      for (int nv = oldVersion; nv < newVersion; nv++) {
        switch (nv) {
          case 1:
            upgradeToV2(db);
            break;
          case 2:
            upgradeToV3(db);
            break;
          case 3:
            upgradeToV4(db);
            break;
          case 4:
            upgradeToV5(db);
            break;
          case 5:
            upgradeToV6(db);
            break;
          case 7:
            upgradeToV8(db);
            break;
          case 8:
            upgradeToV9(db);
            break;
          case 12:
            upgradeToV13(db);
            break;
          case 14:
            upgradeToV15(db);
            break;
          case 15:
            upgradeToV16(db);
            break;
          case 16:
            upgradeToV17(db);
            break;
          case 18:
            upgradeToV19(db);
            break;
          case 19:
            upgradeToV20(db);
            break;
          case 20:
            upgradeToV21(db);
            break;
          case 21:
            upgradeToV22(db);
            break;
          case 22:
            upgradeToV23(db);
            break;
          case 23:
            upgradeToV24(db);
            break;
          case 24:
            upgradeToV25(db);
            break;
          case 25:
            preferences.setShouldShowNewGestureInfo();
            upgradeToV26(db);
            break;
          case 26:
            upgradeToV27(db);
            break;
          case 27:
            upgradeToV28(db);
            break;
          case 28:
            upgradeToV29(db);
            break;
        }
      }

      // Recreate views on the end
      recreateViews(db);
    }
  }

  public Location getLocation(int locationId) {
    return channelRepository.getLocation(locationId);
  }

  public boolean updateLocation(SuplaLocation suplaLocation) {
    return channelRepository.updateLocation(suplaLocation);
  }

  public void updateLocation(Location location) {
    channelRepository.updateLocation(location);
  }

  public Channel getChannel(int channelId) {
    return channelRepository.getChannel(channelId);
  }

  public ChannelGroup getChannelGroup(int groupId) {
    return channelRepository.getChannelGroup(groupId);
  }

  public boolean updateChannel(SuplaChannel suplaChannel) {
    return channelRepository.updateChannel(suplaChannel);
  }

  public void updateChannel(Channel channel) {
    channelRepository.updateChannel(channel);
  }

  public boolean updateChannelValue(
      SuplaChannelValue suplaChannelValue, int channelId, boolean onLine) {
    return channelRepository.updateChannelValue(suplaChannelValue, channelId, onLine);
  }

  public boolean updateChannelValue(SuplaChannelValueUpdate channelValue) {
    return channelRepository.updateChannelValue(
        channelValue.Value, channelValue.Id, channelValue.OnLine);
  }

  public ResultTuple updateChannelExtendedValue(
      SuplaChannelExtendedValue suplaChannelExtendedValue, int channelId) {
    return channelRepository.updateChannelExtendedValue(suplaChannelExtendedValue, channelId);
  }

  public boolean updateChannelGroup(SuplaChannelGroup suplaChannelGroup) {
    return channelRepository.updateChannelGroup(suplaChannelGroup);
  }

  public void updateChannelGroup(ChannelGroup channelGroup) {
    channelRepository.updateChannelGroup(channelGroup);
  }

  public boolean updateChannelGroupRelation(SuplaChannelGroupRelation suplaChannelGroupRelation) {
    return channelRepository.updateChannelGroupRelation(suplaChannelGroupRelation);
  }

  public boolean setChannelsVisible(int visible, int whereVisible) {
    return channelRepository.setChannelsVisible(visible, whereVisible);
  }

  public boolean setChannelGroupsVisible(int visible, int whereVisible) {
    return channelRepository.setChannelGroupsVisible(visible, whereVisible);
  }

  public boolean setChannelGroupRelationsVisible(int visible, int whereVisible) {
    return channelRepository.setChannelGroupRelationsVisible(visible, whereVisible);
  }

  public boolean setChannelsOffline() {
    return channelRepository.setChannelsOffline();
  }

  public int getChannelCount() {
    return channelRepository.getChannelCount();
  }

  public Cursor getChannelListCursorForGroup(int groupId) {
    return channelRepository.getChannelListCursorForGroup(groupId);
  }

  public ColorListItem getColorListItem(int id, boolean group, int idx) {
    return colorListRepository.getColorListItem(id, group, idx);
  }

  public void updateColorListItemValue(ColorListItem item) {
    colorListRepository.updateColorListItemValue(item);
  }

  public List<Integer> updateChannelGroups() {
    return channelRepository.updateAllChannelGroups();
  }

  public List<Integer> iconsToDownload() {
    Set<Integer> result = new LinkedHashSet<>();
    result.addAll(channelRepository.getChannelUserIconIdsToDownload());
    result.addAll(sceneRepository.getSceneUserIconIdsToDownload());
    return new ArrayList<>(result);
  }

  public boolean addUserIcons(int id, byte[] img1, byte[] img2, byte[] img3, byte[] img4) {
    return userIconRepository.addUserIcons(id, img1, img2, img3, img4);
  }

  public void loadUserIconsIntoCache() {
    userIconRepository.loadUserIconsIntoCache();
  }

  public boolean isZWaveBridgeChannelAvailable() {
    return channelRepository.isZWaveBridgeChannelAvailable();
  }

  public List<Channel> getZWaveBridgeChannels() {
    return channelRepository.getZWaveBridgeChannels();
  }

  public ChannelRepository getChannelRepository() {
    return channelRepository;
  }
}
