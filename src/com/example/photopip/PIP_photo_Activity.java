package com.example.photopip;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import pip.dimens.Dimensions;
import pip.dimens.MaskModel;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import bitmapUtils.BitmapCompression;
import bitmapUtils.PIPUtils;
import customImageView.ZoomableImageView;

public class PIP_photo_Activity extends Activity implements OnClickListener {

	String tag = "PIP_photo_Activity";
	public static final int BACK_FROM_ACTIVITY = 98;
	public static final int RESULT_FROM_PIP_CAMERA = 3;
	public static final int RESULT_FROM_PIP_GALLERY = 4;
	public static final String TEMP_PHOTO_FILE_NAME = "temp_photo.jpg";
	private File mFileTemp;
	private static String FOLDER_NAME = "";
	private File mGalleryFolder;
	boolean isImageEffectable = false;

	Bitmap croppedImage;
	ImageView ivPhotoImages;
	FrameLayout flEditor;
	Button Gallery;
	Button ivSave;
	Button ivShare;
	Button ivMore, ivPIP;
	ProgressDialog pd;
	Uri screenshotUri;
	Uri mImageUri;
	ArrayList<ImageView> ivPipPhotoFrame = new ArrayList<ImageView>();

	LayoutParams params;
	ZoomableImageView iv2;
	DisplayMetrics om;
	int h, w;
	Bitmap pipbit, b, mask;
	Display display;
	Uri selectedImageUri;
	PopupWindow pwindo;
	Point p;
	Paint p_mask;
	static String PIP_IMG = "bottle";
	ArrayList<String> piplist;
	static String PIP_IMG_mask = "bottle";
	Bitmap bottle;
	int selected = 0;
	MaskModel model;
	Bitmap blured_bg;
	PIPUtils u;
    Button back;
    String applicationName;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_editor);

		om = new DisplayMetrics();

		getWindowManager().getDefaultDisplay().getMetrics(om);
		h = om.heightPixels;
		w = om.widthPixels;

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		 applicationName = getResources().getString(R.string.app_name);
		FOLDER_NAME = "" + applicationName;
		mGalleryFolder = createFolders();

		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {

			mFileTemp = new File(Environment.getExternalStorageDirectory(),
					TEMP_PHOTO_FILE_NAME);
		} else {
			mFileTemp = new File(getFilesDir(), TEMP_PHOTO_FILE_NAME);
		}
		findviewByID();

		// ImagePreview();

		// pipphoto
		/*croppedImage = BitmapFactory.decodeFile(mFileTemp.getAbsolutePath());
		croppedImage = scaleCenterCrop(croppedImage, h - 100, w);
		b = croppedImage.copy(Config.ARGB_8888, true);
*/
		
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;

		croppedImage = BitmapFactory.decodeFile(mFileTemp.getAbsolutePath(),
				opts);
		opts.inSampleSize = BitmapCompression.calculateInSampleSize(opts, w,
				h - 100);
		opts.inJustDecodeBounds = false;
		croppedImage = BitmapFactory.decodeFile(mFileTemp.getAbsolutePath(),
				opts);
		croppedImage = adjustImageOrientation(croppedImage);
		croppedImage = scaleCenterCrop(croppedImage, h - 100, w);
		b = croppedImage.copy(Config.ARGB_8888, true);

		// PipPhoto();
		maskingImage();
	}

	public Bitmap blurBG() {
		if (blured_bg == null) {
			u = new PIPUtils(this);
			blured_bg = u.fastblur(croppedImage, 10);
		}
		return blured_bg;
	}

	private void maskingImage() {
		new AsyncTask<Void, Void, Void>() {
			ProgressDialog pd;
			Point p = new Point();

			protected void onPreExecute() {
				pd = new ProgressDialog(PIP_photo_Activity.this);
				pd.show();
			};

			@Override
			protected Void doInBackground(Void... params) {
				try {
					Log.d("main", "pos: " + selected);
					bottle = Bitmap.createScaledBitmap(
							BitmapFactory.decodeStream(getAssets().open(
									"frame" + selected + "/moby.png")), w,
							h, false);

					Dimensions dms = new Dimensions();
					model = Dimensions.models.get(selected);

					p = getFirstPoint(bottle);

					Log.d("main", "point X: " + p.x + "  Y:  " + p.y);

					mask = Bitmap.createScaledBitmap(
							BitmapFactory.decodeStream(getAssets().open(
									"frame" + selected + "/moby_mask.png")),
							w * model.width / 720, h * model.height / 1280,
							false);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Canvas canvas = new Canvas(b);
                blured_bg = null;
				Paint p_mask = new Paint();
				p_mask.setXfermode(new PorterDuffXfermode(Mode.DST_OUT));
				canvas.drawBitmap(blurBG(), 0, 0, null);
				canvas.drawBitmap(mask, p.x, p.y, p_mask);
				canvas.drawBitmap(bottle, 0, 0, null);
				p_mask.setXfermode(null);

				return null;
			}

			protected void onPostExecute(Void result) {
				ivPhotoImages.setImageBitmap(b);

				params = new LayoutParams(mask.getWidth(), mask.getHeight());

				params.leftMargin = p.x;
				params.topMargin = p.y;
				iv2.setLayoutParams(params);
				iv2.setScaleModel(model.zoom);
				iv2.setImageBitmap(croppedImage);

				pd.dismiss();
			};
		}.execute();

	}

	private void findviewByID() {

		flEditor = (FrameLayout) findViewById(R.id.flEditor);
		ivPhotoImages = (ImageView) findViewById(R.id.ivPhotoImage);
		iv2 = (ZoomableImageView) findViewById(R.id.imageView2);

		Gallery = (Button) findViewById(R.id.btnImport);
		ivSave = (Button) findViewById(R.id.btnSave);
		ivShare = (Button) findViewById(R.id.btnShare);
		ivPIP = (Button) findViewById(R.id.btnLibrary);
		ivMore = (Button) findViewById(R.id.btnMore);
		back = (Button) findViewById(R.id.btnBack);

		Gallery.setOnClickListener(onclickGallery);
		ivSave.setOnClickListener(onclickSave);
		ivShare.setOnClickListener(onclickShare);
		ivPIP.setOnClickListener(onclickPIP);
		ivMore.setOnClickListener(onclickMore);
		back.setOnClickListener(onclickback);

	}

	private void selectPipPhoto() {

		final CharSequence[] options = { "Take Photo", "Choose from Gallery",
				"Cancel" };

		AlertDialog.Builder builder = new AlertDialog.Builder(
				PIP_photo_Activity.this);
		builder.setTitle("Select Image From...!");
		builder.setIcon(R.drawable.ic_launcher);

		builder.setItems(options, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int item) {

				if (options[item].equals("Take Photo")) {

					Intent it_cam = new Intent(
							"android.media.action.IMAGE_CAPTURE");
					it_cam.putExtra(MediaStore.EXTRA_OUTPUT,
							Uri.fromFile(mFileTemp));

					startActivityForResult(it_cam, RESULT_FROM_PIP_CAMERA);

				} else if (options[item].equals("Choose from Gallery")) {

					Intent it_gallary = new Intent(Intent.ACTION_PICK,
							MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
					startActivityForResult(it_gallary, RESULT_FROM_PIP_GALLERY);

				} else if (options[item].equals("Cancel")) {
					dialog.dismiss();
				}

			}

		});

		builder.show();
	}

	public Point getFirstPoint(Bitmap bmp) {
		Point p = new Point();
		int[] pixels = new int[bmp.getWidth() * bmp.getHeight()];
		bmp.getPixels(pixels, 0, bmp.getWidth(), 0, 0, bmp.getWidth(),
				bmp.getHeight());
		for (int j = 0; j < bmp.getHeight(); j++) {
			for (int i = 0; i < bmp.getWidth(); i++) {

				int point = bmp.getPixel(i, j);
				if (point == model.ColorCode) {
					p.set(i, j);
					return p;
				}
			}

		}
		return p;
	}

	public Bitmap scaleCenterCrop(Bitmap source, int newHeight, int newWidth) {
		int sourceWidth = source.getWidth();
		int sourceHeight = source.getHeight();

		// Compute the scaling factors to fit the new height and width,
		// respectively.
		// To cover the final image, the final scaling will be the bigger
		// of these two.
		float xScale = (float) newWidth / sourceWidth;
		float yScale = (float) newHeight / sourceHeight;
		float scale = Math.max(xScale, yScale);

		// Now get the size of the source bitmap when scaled
		float scaledWidth = scale * sourceWidth;
		float scaledHeight = scale * sourceHeight;

		// Let's find out the upper left coordinates if the scaled bitmap
		// should be centered in the new size give by the parameters
		float left = (newWidth - scaledWidth) / 2;
		float top = (newHeight - scaledHeight) / 2;

		// The target rectangle for the new, scaled version of the source bitmap
		// will now
		// be
		RectF targetRect = new RectF(left, top, left + scaledWidth, top
				+ scaledHeight);

		// Finally, we create a new bitmap of the specified size and draw our
		// new,
		// scaled bitmap onto it.
		Bitmap dest = Bitmap.createBitmap(newWidth, newHeight,
				source.getConfig());
		Canvas canvas = new Canvas(dest);
		canvas.drawBitmap(source, null, targetRect, null);

		return dest;
	}

	protected void PIP_img_popup() {
		
		LayoutInflater inflate = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View viewPip = inflate.inflate(R.layout.pip_img_grid, null);

		pwindo = new PopupWindow(viewPip,
				WindowManager.LayoutParams.MATCH_PARENT,
				WindowManager.LayoutParams.MATCH_PARENT, true);

		pwindo.setContentView(viewPip);

		pwindo.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		pwindo.setOutsideTouchable(true);
		pwindo.showAtLocation(flEditor, Gravity.CENTER, 0, 0);
		GridView pipgridview = (GridView) viewPip.findViewById(R.id.PipGrid);

		pipgridview.setAdapter(new ImageAdapter(PIP_photo_Activity.this));

		pipgridview
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int pos, long arg3) {
						selected = pos;
						maskingImage();
						pwindo.dismiss();

					}
				});

		TextView closepopup = (TextView) viewPip.findViewById(R.id.close);
		closepopup.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				pwindo.dismiss();
			}
		});

	}

	private Bitmap decodeFile(File f, int reqHeight, int reqWidth) {
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

	private Bitmap adjustImageOrientation(Bitmap image) {
		ExifInterface exif;
		try {
			exif = new ExifInterface(mFileTemp.getAbsolutePath());
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
				int w = image.getWidth();
				int h = image.getHeight();
				Matrix mtx = new Matrix();
				mtx.preRotate(rotate);
				image = Bitmap.createBitmap(image, 0, 0, w, h, mtx, false);
			}
		} catch (IOException e) {
			return null;
		}
		return image.copy(Bitmap.Config.ARGB_8888, true);
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
	View.OnClickListener onclickback = new OnClickListener() {
		@Override
		public void onClick(View v) {
			onBackPressed();
		}
	};
	View.OnClickListener onclickSave = new OnClickListener() {
		@Override
		public void onClick(View v) {
			saveImage();
			Toast.makeText(getApplicationContext(), "Image Saved in sdcard's folder "+FOLDER_NAME,
					Toast.LENGTH_LONG).show();
		}
	};

	View.OnClickListener onclickShare = new OnClickListener() {
		@Override
		public void onClick(View v) {
			new shareTask().execute();
		}
	};
	View.OnClickListener onclickGallery = new OnClickListener() {
		@Override
		public void onClick(View v) {
			selectPipPhoto();

		}
	};
	View.OnClickListener onclickPIP = new OnClickListener() {
		@Override
		public void onClick(View v) {

			PIP_img_popup();
		}
	};
	View.OnClickListener onclickMore = new OnClickListener() {
		@Override
		public void onClick(View v) {
		}
	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case RESULT_FROM_PIP_CAMERA:
                
				// pipphoto
				/*croppedImage = BitmapFactory.decodeFile(mFileTemp
						.getAbsolutePath());
				croppedImage = scaleCenterCrop(croppedImage, h - 100, w);
				b = croppedImage.copy(Config.ARGB_8888, true);*/
				

				BitmapFactory.Options opts = new BitmapFactory.Options();
				opts.inJustDecodeBounds = true;

				croppedImage = BitmapFactory.decodeFile(mFileTemp.getAbsolutePath(),
						opts);
				opts.inSampleSize = BitmapCompression.calculateInSampleSize(opts, w,
						h - 100);
				opts.inJustDecodeBounds = false;
				croppedImage = BitmapFactory.decodeFile(mFileTemp.getAbsolutePath(),
						opts);
				croppedImage = adjustImageOrientation(croppedImage);
				
				croppedImage = scaleCenterCrop(croppedImage, h - 100, w);
				b = croppedImage.copy(Config.ARGB_8888, true);
				
				maskingImage();

				break;
			case RESULT_FROM_PIP_GALLERY:
	
				selectedImageUri = data.getData();
				try {
					InputStream imageStream = getContentResolver()
							.openInputStream(selectedImageUri);
					croppedImage = BitmapFactory.decodeStream(imageStream);
					Matrix m = BitmapCompression.adjustImageOrientationUri(
							this, selectedImageUri);
					int wt = croppedImage.getWidth();
					int ht = croppedImage.getHeight();
					croppedImage = Bitmap.createBitmap(croppedImage, 0, 0, wt,
							ht, m, false);
					FileOutputStream fos = null;

					fos = new FileOutputStream(mFileTemp.getAbsolutePath());
					croppedImage.compress(Bitmap.CompressFormat.JPEG, 70, fos);

					fos.flush();
					fos.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
				/*croppedImage = scaleCenterCrop(croppedImage, h - 100, w);
				b = croppedImage.copy(Config.ARGB_8888, true);*/

				BitmapFactory.Options opts2 = new BitmapFactory.Options();
				opts2.inJustDecodeBounds = true;

				croppedImage = BitmapFactory.decodeFile(mFileTemp.getAbsolutePath(),
						opts2);
				opts2.inSampleSize = BitmapCompression.calculateInSampleSize(opts2, w,
						h - 100);
				opts2.inJustDecodeBounds = false;
				croppedImage = BitmapFactory.decodeFile(mFileTemp.getAbsolutePath(),
						opts2);
				croppedImage = scaleCenterCrop(croppedImage, h - 100, w);
				b = croppedImage.copy(Config.ARGB_8888, true);
				maskingImage();

				break;
			case BACK_FROM_ACTIVITY:
				isImageEffectable = true;
				if (!data.getStringExtra("muri").equals("null")) {
					mImageUri = Uri.parse(data.getStringExtra("muri"));
					ivPhotoImages.setImageBitmap(null);
					ivPhotoImages.setImageURI(mImageUri);
					if (mFileTemp.exists())
						mFileTemp.delete();
				} else
					mImageUri = Uri.parse(mFileTemp.getAbsolutePath());
				break;
			}
		} else if (resultCode == RESULT_CANCELED) {
			switch (requestCode) {
			case BACK_FROM_ACTIVITY:
				break;
			}
		}
	}

	@Override
	public void onClick(View v) {
	}

	private void saveImage() {
		if (!isImageEffectable) {

			File file = null;
			if (mGalleryFolder != null) {
				if (mGalleryFolder.exists()) {
					file = new File(mGalleryFolder, "cameff_"
							+ System.currentTimeMillis() + ".jpg");
				}
			}
			try {
				Bitmap selectedImage = getFrameBitmap();
				FileOutputStream fos = null;
				mImageUri = Uri.parse("file://" + file.getPath());
				isImageEffectable = true;
				fos = new FileOutputStream(file);
				selectedImage.compress(Bitmap.CompressFormat.JPEG, 70, fos);
				fos.flush();
				fos.close();
				MediaScannerConnection.scanFile(this,
						new String[] { file.getAbsolutePath() },
						new String[] { "image/jpeg" }, null);
				if (mFileTemp.exists())
					mFileTemp.delete();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public Bitmap getFrameBitmap() {
		Bitmap bm = null;
		flEditor.postInvalidate();
		flEditor.setDrawingCacheEnabled(true);
		flEditor.buildDrawingCache();
		bm = Bitmap.createBitmap(flEditor.getDrawingCache());
		flEditor.destroyDrawingCache();
		return bm;
	}

	public void applyEffect() {
		try {
			Bitmap selectedImage = getFrameBitmap();
			FileOutputStream fos = null;
			fos = new FileOutputStream(mFileTemp.getAbsolutePath());
			selectedImage.compress(Bitmap.CompressFormat.JPEG, 70, fos);
			fos.flush();
			fos.close();
			MediaScannerConnection.scanFile(this,
					new String[] { mFileTemp.getAbsolutePath() },
					new String[] { "image/jpeg" }, null);

		} catch (Exception e) {
		}
	}

	private class shareTask extends AsyncTask<Void, Void, Bitmap> {
		@Override
		protected void onPreExecute() {
		}

		@Override
		protected Bitmap doInBackground(Void... arg0) {
			saveImage();
			return null;
		}

		protected void onPostExecute(Bitmap result) {
			Intent sharingIntent = new Intent(
					android.content.Intent.ACTION_SEND);
			screenshotUri = Uri.fromFile(new File(mImageUri.getPath()));
			sharingIntent.setType("image/jpg");
			sharingIntent.putExtra(android.content.Intent.EXTRA_STREAM,
					screenshotUri);
			startActivity(Intent.createChooser(sharingIntent, "Share Image")); //
		}

	}

	private class applyEffectTask extends AsyncTask<Void, Void, Bitmap> {
		@Override
		protected void onPreExecute() {
		}

		@Override
		protected Bitmap doInBackground(Void... arg0) {
			applyEffect();
			return null;
		}

		protected void onPostExecute(Bitmap result) {

			/*
			 * Intent in = new Intent(PIP_photo_Activity.this,
			 * ImageEditorActivity.class); in.putExtra("iseffect",
			 * isImageEffectable); in.putExtra("muri", mImageUri);
			 * startActivityForResult(in, BACK_FROM_ACTIVITY);
			 */
		}
	}

	private File createFolders() {
		File baseDir;
		if (android.os.Build.VERSION.SDK_INT < 8) {
			baseDir = Environment.getExternalStorageDirectory();
		} else {
			baseDir = Environment.getExternalStorageDirectory();
		}
		if (baseDir == null)
			return Environment.getExternalStorageDirectory();
		File aviaryFolder = new File(baseDir, FOLDER_NAME);
		if (aviaryFolder.exists())
			return aviaryFolder;
		if (aviaryFolder.mkdirs())
			return aviaryFolder;
		return Environment.getExternalStorageDirectory();
	}

	@Override
	public void onBackPressed() {
		Intent newIntent = new Intent(PIP_photo_Activity.this,
				StartPip.class);
		newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(newIntent);
	}

	@Override
	protected void onStop() {
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		if (!PIP_photo_Activity.this.isFinishing() && pd != null
				&& pd.isShowing()) {
			pd.dismiss();
		}
		System.gc();
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		super.onDestroy();
	}
}
