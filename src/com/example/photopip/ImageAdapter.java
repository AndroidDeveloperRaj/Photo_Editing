package com.example.photopip;


import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

public class ImageAdapter extends BaseAdapter {
	private Context mContext;
	private LayoutInflater mInflater;
	private String[] pipdata;
	public ImageAdapter(Context c) {
		
		mInflater = LayoutInflater.from(c);
		mContext = c;
		try {
			pipdata = mContext.getAssets().list("pip_thumb");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public int getCount() {
		return pipdata.length;
		// return pipdata.size();
	}

	public Object getItem(int position) {
		return pipdata[position];
	}

	public long getItemId(int position) {
		return position;
	}

	// create a new ImageView for each item referenced by the
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) { // if it's not recycled,
			convertView = mInflater.inflate(R.layout.pip_img_raw, null);
			convertView.setLayoutParams(new
					GridView.LayoutParams(200,200));
			holder = new ViewHolder();
			holder.icon = (ImageView) convertView.findViewById(R.id.ivpip_tiny);

			
			convertView.setTag(holder);
		}

		else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.icon.setAdjustViewBounds(true);
		holder.icon.setScaleType(ImageView.ScaleType.CENTER_CROP);
		holder.icon.setPadding(8, 8, 8, 8);
		Bitmap bmp = null;
		try {
			bmp = BitmapFactory.decodeStream(mContext.getAssets().open(
					"pip_thumb/" + pipdata[position]));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		holder.icon.setImageBitmap(bmp);
		// holder.icon.setImageResource(Constant.mThumbIds[position]);
		holder.icon.setScaleType(ScaleType.FIT_XY);
		return convertView;
	}

	class ViewHolder {
		TextView title;
		ImageView icon;
	}

}