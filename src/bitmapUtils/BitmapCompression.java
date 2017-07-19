package bitmapUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;

public class BitmapCompression {
	static int w;
	static int h;
	public BitmapCompression() {
	}

	public static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;
		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;
			while ((halfHeight / inSampleSize) > reqHeight
					&& (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}
		return inSampleSize;
	}
	public static Bitmap decodeFile(File f, int reqHeight, int reqWidth) {
		try {
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(new FileInputStream(f), null, o);
			int scale = 1;
			while (o.outWidth / scale / 2 >= reqWidth
					&& o.outHeight / scale / 2 >= reqHeight)
				scale *= 2;
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
		} catch (FileNotFoundException e) {
		}
		return null;
	}
	public static Bitmap adjustImageOrientation(File f, Bitmap image) {
		ExifInterface exif;
		try {
			exif = new ExifInterface(f.getAbsolutePath());
			int exifOrientation = exif.getAttributeInt(
					ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);

			int rotate = 0;
			switch (exifOrientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				rotate = 90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				rotate = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				rotate = 270;
				break;
			}
			if (rotate != 0) {
				w = image.getWidth();
				h = image.getHeight();
				Matrix mtx = new Matrix();
				mtx.preRotate(rotate);
				image = Bitmap.createBitmap(image, 0, 0, w, h, mtx, false);
			}
		} catch (IOException e) {
			return null;
		}
		return image.copy(Bitmap.Config.ARGB_8888, true);
	}
	public static Bitmap decodeSampledBitmapFromResource(Resources res,
			int resId, int reqWidth, int reqHeight) {
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(res, resId, options);
		options.inSampleSize = calculateInSampleSize(options, reqWidth,
				reqHeight);
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeResource(res, resId, options);
	}
	public static Matrix adjustImageOrientationUri(Context context, Uri f) {

		Matrix matrix = new Matrix();
		Cursor cursor = context.getContentResolver().query(f,
				new String[] { MediaStore.Images.ImageColumns.ORIENTATION },
				null, null, null);
		if (cursor.getCount() == 1) {
			cursor.moveToFirst();
			int orientation = cursor.getInt(0);
			matrix.preRotate(orientation);
		}
		return matrix;
		// w = image.getWidth();
		// h = image.getHeight();
		// image = Bitmap.createBitmap(image, 0, 0, w, h, matrix, false);
		// return image.copy(Bitmap.Config.ARGB_8888, true);
	}
	
}
