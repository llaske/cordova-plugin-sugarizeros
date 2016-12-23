package sugarizer.olpc_france.org.sugarizeroslibrary.applications.icons;


import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ApplicationIconFetcher {

    private static File cacheDir = null;
    private Context mContext;
    private PackageManager mPackageName;
    private ApplicationIconCacher applicationIconCacher;

    public ApplicationIconFetcher(Context context, PackageManager packageManager) {
        mContext = context;
        mPackageName = packageManager;
        applicationIconCacher = new ApplicationIconCacher();
    }

    public String getIcon(String packageName) {

        if (cacheDir == null) {
            cacheDir = mContext.getCacheDir();
        }

        if (applicationIconCacher.contains(packageName)) {
            return cacheDir + "/" + packageName + ".png";
        }

        try {
            File file = new File(cacheDir, packageName);
            Drawable iconDrawable = mPackageName.getApplicationIcon(packageName);
            Bitmap bitmap = drawableToBitmap(iconDrawable);
            return bitmapToFile(mContext, packageName, bitmap);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    private String bitmapToFile(Context context, String filename, Bitmap bitmap) {
        if (cacheDir == null) {
            cacheDir = context.getCacheDir();
        }

        File f = new File(cacheDir, filename + ".png");
        try {
            f.createNewFile();
            //Convert bitmap to byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
            byte[] bitmapdata = bos.toByteArray();

            //write the bytes in file
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(f);
                fos.write(bitmapdata);
                fos.flush();
                fos.close();
                applicationIconCacher.add(filename);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return f.getAbsolutePath();
    }


    private String drawableToBase64(Drawable drawable) {
        BitmapDrawable bitDw = ((BitmapDrawable) drawable);
        Bitmap bitmap = bitDw.getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] bitmapByte = stream.toByteArray();
        return String.format("data:image/png;base64,%s", Base64.encodeToString(bitmapByte, Base64.DEFAULT));
    }

    private static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }
        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

}
