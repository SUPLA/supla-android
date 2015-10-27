package org.supla.android;

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

 Author: Przemyslaw Zygmunt p.zygmunt@acsoftware.pl [AC SOFTWARE]
 */

import android.content.Context;

import org.supla.android.lib.SuplaClient;

public class SuplaApp {

 private static final Object _lck = new Object();
 private static SuplaClient _SuplaClient = null;
 private static SuplaApp _SuplaApp = null;

 public void SuplaClientInitIfNeed(Context context) {

  synchronized (_lck) {

   if (_SuplaClient == null) {
    _SuplaClient = new SuplaClient(context);
    _SuplaClient.start();
   }

  }

 }

 public SuplaClient getSuplaClient() {

  SuplaClient result;

  synchronized (_lck) {
   result = _SuplaClient;
  }

  return result;
 }

 public static synchronized SuplaApp getApp() {

  synchronized (_lck) {

   if (_SuplaApp == null) {
    _SuplaApp = new SuplaApp();
   }

  }

  return _SuplaApp;
 }

}
