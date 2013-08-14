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
		
		final int frameSize = width * height;
		int r,g,b;
		
		for (int j = 0; j < height; j+=skipY) {
			int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
			for (int i = 0; i < width; i+=skipX) {
				int y=(0xff & (int)(data[j*width+i]));
				if ((i & 1) == 0) {
//		            v = 0xff & (int)data[uvp++];
//		            u = 0xff & (int)data[uvp++];
					u=128;v=128;//grey-scale
		        }
				
				r = y + (int) 1.4075f * (v-128);
				g = y - (int) (0.3455f * (u-128) + 0.7169f * (v-128));
				b = y + (int) 1.779f * (u-128);
				
			    r = r>255? 255 : r<0 ? 0 : r;
			    g = g>255? 255 : g<0 ? 0 : g;
			    b = b>255? 255 : b<0 ? 0 : b;
	            
	            int color=0xff000000 | r<<16 | g<<8 | b;
	            cacheColors[index++]=color;
	            
			}
		}
		
		Log.d(TAG,"index="+index+",("+mWidth+","+mHeight+"),"+mWidth*mHeight);
		
		System.arraycopy(cacheColors, 0, drawColors, 0, cacheColors.length);
	}



}
