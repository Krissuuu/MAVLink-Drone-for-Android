package me.lake.librestreaming;

import android.graphics.Bitmap;
import android.media.MediaCodec;

import java.nio.ByteBuffer;

import me.lake.librestreaming.rtmp.RESFlvData;

public class ARFrameUtil {

    private static Bitmap bitmap;

    public static void setBitmap(Bitmap b) {
        bitmap = b;
    }

    public static Bitmap getBitmap(){
        return bitmap;
    }

}
