package com.example.photopip;

import java.util.ArrayList;

import android.content.Context;
import android.view.Gravity;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class HsBoundRuntime {

	Context mContext;

	public HsBoundRuntime(Context context) {
		mContext = context;
	}

	public LinearLayout HSPipPhotoFrameList(int i,
			ArrayList<LinearLayout> tvCrop) {
		LinearLayout layout = new LinearLayout(mContext);

		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT);
		layout.setGravity(Gravity.CENTER);
		layout.setLayoutParams(lp);

		LinearLayout layout1 = new LinearLayout(mContext);

		LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.MATCH_PARENT);
		layout1.setGravity(Gravity.CENTER);
		layout.setPadding(5, 5, 5, 5);
		layout1.setLayoutParams(lp1);

		ImageView imageView = new ImageView(mContext);
		// imageView.setImageResource(Utils.mPipPhotoFrameResId[i]);
		layout.setOnClickListener((OnClickListener) mContext);
		layout.addView(imageView);

		ImageView imageView1 = new ImageView(mContext);
		imageView1.setImageResource(R.drawable.crop_devider);
		layout.setBackgroundResource(R.drawable.crop_deselection);

		tvCrop.add(layout);
		layout1.addView(layout);
		layout1.addView(imageView1);
		return layout1;
	}

}
