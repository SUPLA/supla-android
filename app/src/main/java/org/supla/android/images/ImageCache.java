package org.supla.android.images;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.supla.android.Trace;

import java.io.ByteArrayInputStream;
import java.util.LinkedHashMap;

public class ImageCache {
    private static final LinkedHashMap<ImageId, Bitmap> map = new LinkedHashMap<>();

    public static synchronized Bitmap getBitmap(Context context, ImageId imgId) {
        if (imgId == null) {
            return null;
        }

        Bitmap result = map.get(imgId);
        if (result == null && !imgId.isUserImage()) {
            result = BitmapFactory.decodeResource(context.getResources(), imgId.getId());
            if (result != null) {
                map.put(imgId, result);
            }
        }

        return result;
    }

    public static synchronized boolean bitmapExists(ImageId imgId) {
        return imgId != null && map.containsKey(imgId);
    }

    public static synchronized boolean addBitmap(ImageId imgId, Bitmap bmp) {
        if (!bitmapExists(imgId)) {
            return map.put(imgId, bmp) != null;
        }

        return false;
    }

    public static synchronized boolean addImage(ImageId imgId, byte[] image) {
        if (imgId == null || image == null) {
            return false;
        }

        ByteArrayInputStream imageStream = new ByteArrayInputStream(image);
        return addBitmap(imgId, BitmapFactory.decodeStream(imageStream));
    }

    public static synchronized void clear() {
        map.clear();
    }
}
