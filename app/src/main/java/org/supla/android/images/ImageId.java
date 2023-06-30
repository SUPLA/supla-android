package org.supla.android.images;

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

public class ImageId {

  private int id;
  private int subId;
  private boolean userImage;
  private long profileId;

  public ImageId(int resId) {
    id = resId;
    userImage = false;
    subId = 0;
    profileId = 0;
  }

  public ImageId(int userImageId, int subId, long profileId) {
    id = userImageId;
    this.subId = subId;
    userImage = true;
    this.profileId = profileId;
  }

  public static boolean equals(ImageId id1, ImageId id2) {
    if (id1 == null || id2 == null) {
      return false;
    }

    return id1.equals(id2);
  }

  public int getId() {
    return id;
  }

  public int getSubId() {
    return subId;
  }

  public long getProfileId() {
    return profileId;
  }

  public boolean isUserImage() {
    return userImage;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ImageId)) return false;

    ImageId imageId = (ImageId) o;

    if (id != imageId.id) return false;
    if (subId != imageId.subId) return false;
    if (profileId != imageId.profileId) return false;
    return userImage == imageId.userImage;
  }

  @Override
  public int hashCode() {
    int result = id;
    result = 31 * result + subId;
    result = 31 * result + (userImage ? 1 : 0);
    if (profileId != 0) {
      result = 31 * result + (int) profileId;
    }
    return result;
  }
}
