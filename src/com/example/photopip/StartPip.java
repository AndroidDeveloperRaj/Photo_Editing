package com.example.photopip;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import bitmapUtils.BitmapCompression;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

public class StartPip extends Activity {
	ImageView StartPIP;
	PopupWindow pwindo;
	LinearLayout llEditor;
	public static final int RESULT_FROM_PIP_CAMERA = 1;
	public static final int RESULT_FROM_PIP_GALLERY = 2;
	public static final String TEMP_PHOTO_FILE_NAME = "temp_photo.jpg";
	public static File mFileTemp;
	ImageView camera, Gallery;
	Uri selectedImageUri;
	Bitmap OrientationImage;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			mFileTemp = new File(Environment.getExternalStorageDirectory(),
					TEMP_PHOTO_FILE_NAME);
		} else {
			mFileTemp = new File(getFilesDir(), TEMP_PHOTO_FILE_NAME);
		}
		
		camera = (ImageView)findViewById(R.id.btnCamera);
		camera.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
				intent.putExtra(MediaStore.EXTRA_OUTPUT,
						Uri.fromFile(mFileTemp));
				startActivityForResult(intent, RESULT_FROM_PIP_CAMERA);
			}
		});
		Gallery = (ImageView)findViewById(R.id.btnGallery);
		Gallery.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
				photoPickerIntent.setType("image/*");
				startActivityForResult(photoPickerIntent,
						RESULT_FROM_PIP_GALLERY);
			}
			
		});

	}

	protected void onActivityResult(int requestCode, int resultCode,
			android.content.Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case RESULT_FROM_PIP_CAMERA:

				Intent newIntent = new Intent(StartPip.this,
						PIP_photo_Activity.class);
				startActivity(newIntent);
				
				break;
			case RESULT_FROM_PIP_GALLERY:

				selectedImageUri = data.getData();
				try {
					InputStream imageStream = getContentResolver()
							.openInputStream(selectedImageUri);
					Bitmap selectedImage = BitmapFactory
							.decodeStream(imageStream);
					Matrix m = BitmapCompression.adjustImageOrientationUri(
							StartPip.this, selectedImageUri);
					int wt = selectedImage.getWidth();
					int ht = selectedImage.getHeight();
					selectedImage = Bitmap.createBitmap(selectedImage, 0, 0,
							wt, ht, m, false);
					FileOutputStream fos = null;

					fos = new FileOutputStream(mFileTemp);
					selectedImage.compress(Bitmap.CompressFormat.JPEG, 70, fos);

					fos.flush();
					fos.close();
				} catch (Exception e) {
					e.printStackTrace();
				}

				Intent newIntent1 = new Intent(StartPip.this,
						PIP_photo_Activity.class);
				startActivity(newIntent1);
				break;

			}
		}
		;

	}

	@Override
	public void onActionModeFinished(ActionMode mode) {
		finish();
	}

	@Override
	public void onBackPressed() {
		finish();
		System.gc();
	}

	@Override
	protected void onStop() {
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		super.onDestroy();

	}
}
