package com.hekai.camera;

import android.content.Context;
import android.util.Log;
import android.view.SurfaceView;

public abstract class OverlayView extends SurfaceView{
	
	private static final String TAG="OverlayView";
	
	int centerX=0,centerY=0;
	int rotation;
	
	public OverlayView(Context context) {
		super(context);
		
		setWillNotDraw(false);
	}
	
	public void updatePosition(int centerX,int centerY){
		Log.d(TAG,"updatePosition centerX="+centerX+",centerY="+centerY);
		this.centerX=centerX;
		this.centerY=centerY;
	}
	
	public void updateRotation(int rotation){
//		Log.d(TAG,"updateRotation rotation="+rotation);
		this.rotation=rotation;
	}

	public abstract void updateData(byte[] data, int width, int height, int format);
	
}
