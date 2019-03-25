package org.supla.android.images;

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