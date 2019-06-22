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

    private int Id;
    private int SubId;
    private boolean userImage;

    public int getId() {
        return Id;
    }

    public int getSubId() {
        return SubId;
    }

    public boolean isUserImage() {
        return userImage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ImageId)) return false;

        ImageId imageId = (ImageId) o;

        if (Id != imageId.Id) return false;
        if (SubId != imageId.SubId) return false;
        return userImage == imageId.userImage;
    }

    public static boolean equals(ImageId id1, ImageId id2) {
        if (id1 == null || id2 == null) {
            return false;
        }

        return id1.equals(id2);
    }

    @Override
    public int hashCode() {
        int result = Id;
        result = 31 * result + SubId;
        result = 31 * result + (userImage ? 1 : 0);
        return result;
    }

    public ImageId(int resId) {
        Id = resId;
        userImage = false;
        SubId = 0;
    }

    public ImageId(int userImageId, int subId) {
        Id = userImageId;
        SubId = subId;
        userImage = true;
    }
}