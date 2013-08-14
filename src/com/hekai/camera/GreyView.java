package com.hekai.camera;

import android.content.Context;
import android.graphics.Canvas;
import android.util.Log;

public class GreyView extends OverlayView{
	
	private static final String TAG="GreyView";
	
	private int[] cacheColors,drawColors;
	
	public GreyView(Context context) {
		super(context);
		
	}
	
	@Override
	public void init() {
		cacheColors = new int[mWidth * mHeight];
		drawColors = new int[mWidth * mHeight];
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		int angle = 90 * mRotation;
		canvas.rotate(angle,mCenterX,mCenterY);
		
		canvas.drawBitmap(drawColors, 0, mWidth, mCenterX - mWidth / 2,
				mCenterY - mHeight / 2, mWidth, mHeight, false, null);
		
		
	}

	@Override
	public void updateData(byte[] data, int width, int height, int format) {
		for(int i=0;i<cacheColors.length;i++){
			cacheColors[i]=0;
		}
		
		int skipX=width/mWidth;
		int skipY=height/mHeight;
		int index=0;
		
		for (int j = 0; j < height; j+=skipY) {
			for (int i = 0; i < width; i+=skipX) {
				int y=(0xff & (int)(data[j*width+i]));
				int u=0;
				int v=0;
				
				int r = y;
	            int g = y;
	            int b = y;
	            
	            int color=0xff000000 | r<<16 | g<<8 | b;
	            cacheColors[index++]=color;
	            
			}
		}
		
		Log.d(TAG,"index="+index+",("+mWidth+","+mHeight+"),"+mWidth*mHeight);
		
		System.arraycopy(cacheColors, 0, drawColors, 0, cacheColors.length);
	}



}
