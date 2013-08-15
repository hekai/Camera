package com.hekai.camera;

import android.content.Context;
import android.util.Log;
import android.view.SurfaceView;

public abstract class OverlayView extends SurfaceView{
	
	private static final String TAG="OverlayView";
	
	int mCenterX=0,mCenterY=0;
	int mWidth=0,mHeight=0;
	int mRotation;
	boolean mIsRotateWithSensor=true;
	Object extra;
	
	public OverlayView(Context context) {
		super(context);
		
		setWillNotDraw(false);
	}
	
	public void updatePosition(int centerX,int centerY){
		Log.d(TAG,"updatePosition centerX="+centerX+",centerY="+centerY);
		this.mCenterX=centerX;
		this.mCenterY=centerY;
	}
	
	public void updateRotation(int rotation){
//		Log.d(TAG,"updateRotation rotation="+rotation);
		this.mRotation=rotation;
	}
	
	public void setSize(int width,int height){
		mWidth=width;
		mHeight=height;
	}

	public boolean isRotateWithSensor() {
		return mIsRotateWithSensor;
	}

	public void setIsRotateWithSensor(boolean mIsRotateWithSensor) {
		this.mIsRotateWithSensor = mIsRotateWithSensor;
	}
	
	public Object getExtra() {
		return extra;
	}

	public void setExtra(Object extra) {
		this.extra = extra;
	}

	public abstract void init();
	public abstract void updateData(byte[] data, int width, int height, int format);
}
