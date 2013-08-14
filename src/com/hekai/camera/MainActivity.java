package com.hekai.camera;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.hardware.Camera.Area;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class MainActivity extends Activity implements OnClickListener,PreviewCallback,SensorEventListener{
	
	private static final String TAG="MainActivity";

	private Camera mCamera;
	private Parameters mParameters;
	private List<Area> mFocusArea;
    private CameraPreview mPreview;
    
    private FrameLayout preview;
    
    private LayoutInflater mLayoutInflater;
    private HistogramView histogramView;
    
    private boolean mIsOverlayShow=false;
    
    private ImageView mCaptureButton,mSwitchButton;
    
    private int mCameraIndex;
	
    private byte[] buffer;
    
    private Display mDisplay;
    private int mRotation;
    private SensorManager mSensorManager;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mCameraIndex=0;//use back camera default. 
		mFocusArea=new ArrayList<Area>();
		
        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this);
        preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
        
        mCaptureButton=(ImageView)findViewById(R.id.button_capture);
        mCaptureButton.setOnClickListener(this);
        mSwitchButton=(ImageView)findViewById(R.id.switch_camera);
        mSwitchButton.setOnClickListener(this);
		
        mLayoutInflater=LayoutInflater.from(this);
		
		mDisplay = ((WindowManager) getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay();
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		
		showOverlay();
	}
	
	@Override
	protected void onResume() {
		Log.d(TAG, "onResume");
		super.onResume();
		
		mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
		startCamera();
	}
	
	@Override
	protected void onPause() {
		Log.d(TAG, "onPause");
		super.onPause();
		
		mSensorManager.unregisterListener(this);
		stopCamera();
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	/** Check if this device has a camera */
	private boolean checkCameraHardware(Context context) {
	    if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
	        // this device has a camera
	        return true;
	    } else {
	        // no camera on this device
	        return false;
	    }
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action=event.getAction();
		switch(action){
		case MotionEvent.ACTION_DOWN:
			//TODO implement touch focus.
			mCamera.stopFaceDetection();
			float x = event.getX();
	        float y = event.getY();
	        float touchMajor = event.getTouchMajor();
	        float touchMinor = event.getTouchMinor();

			Rect touchRect = new Rect((int) (x - touchMajor / 2),
					(int) (y - touchMinor / 2), (int) (x + touchMajor / 2),
					(int) (y + touchMinor / 2));
			
			Log.d(TAG,"(x,y)=("+x+","+y+"),rect="+touchRect+",(w,h)=("+mPreview.getWidth()+","+mPreview.getHeight()+")");

			if(mFocusArea.isEmpty())
				mFocusArea.add(new Area(touchRect, 1));
			else
				mFocusArea.set(0, new Area(touchRect, 1));
			mParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
			mParameters.setFocusAreas(mFocusArea);
			mParameters.setMeteringAreas(mFocusArea);
			mCamera.setParameters(mParameters);
			autoFocus();
			return true;
		}
		return super.onTouchEvent(event);
	}
	
	private AutoFocusCallback mAutoFocusCallback=new AutoFocusCallback() {
		
		@Override
		public void onAutoFocus(boolean success, Camera camera) {
			Log.d(TAG,"autofucos success="+success);
		}
	};
	
	/** A safe way to get an instance of the Camera object. */
	public static Camera getCameraInstance(int index){
	    Camera c = null;
	    try {
	        c = Camera.open(index); // attempt to get a Camera instance
	    }
	    catch (Exception e){
	        // Camera is not available (in use or does not exist)
	    }
	    return c; // returns null if camera is unavailable
	}

	@Override
	public void onClick(View v) {
		if(v.equals(mCaptureButton)){
			autoFocus();
		}else if(v.equals(mSwitchButton)){
			switchCamera();
		}
	}

	private void startCamera(){
		preview.removeView(mPreview);
		mCamera = getCameraInstance(mCameraIndex);
		
		mParameters=mCamera.getParameters();
		mParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
		mCamera.setParameters(mParameters);
		
		mSwitchButton.setImageResource(mCameraIndex==0?R.drawable.ic_switch_front:R.drawable.ic_switch_back);
		mPreview.onResume(mCamera);
		preview.postDelayed(new Runnable() {
			@Override
			public void run() {
				if(!mPreview.isRelease()){
					buffer=new byte[640*480*2];
					mCamera.addCallbackBuffer(buffer);
					mCamera.setPreviewCallbackWithBuffer(MainActivity.this);
				}
			}
		},1000);
		preview.addView(mPreview);
		if(mIsOverlayShow){
			if(histogramView!=null){
				preview.removeView(histogramView);
				preview.addView(histogramView);
			}
		}
			
	}
	
	private void stopCamera(){
		mCamera.setPreviewCallback(null);
		mPreview.onPause();
	}
	
	private void autoFocus(){
		mCamera.autoFocus(mAutoFocusCallback);
	}
	
	private void showOverlay(){
		if(!mIsOverlayShow){
			histogramView=new HistogramView(this);
			histogramView.updatePosition(mDisplay.getWidth()/2, mDisplay.getHeight()/2);
			histogramView.updateRotation(mRotation);
			preview.addView(histogramView);
		}else{
			preview.removeView(histogramView);
			histogramView=null;
		}
		mIsOverlayShow=!mIsOverlayShow;
	}
	
	private void switchCamera(){
		int cameraNum=Camera.getNumberOfCameras();
		if(cameraNum<=1)
			return;
		
		stopCamera();
		
		mCameraIndex=(mCameraIndex+1)%2;
		
		startCamera();
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		Size size=mCamera.getParameters().getPreviewSize();
		int imgFormat = mCamera.getParameters().getPreviewFormat();
//		Log.d(TAG,"data.length = "+data.length+" , width = "+size.width+" , height = "
//				+size.height+" , format = "+imgFormat);
		if(histogramView!=null){
			histogramView.updateData(data, size.width, size.height, imgFormat);
			histogramView.invalidate();
		}
		
		mCamera.addCallbackBuffer(buffer);
//		YuvImage image = new YuvImage(data, imgFormat, size.width, size.height, null);
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (Sensor.TYPE_ACCELEROMETER != event.sensor.getType()) {
            return;
        }

        float[] values = event.values;
        float ax = values[0];
        float ay = values[1];
        float az = values[2];
        
//		Log.d(TAG, "ax="+ax+",ay="+ay+",az="+az);
        
        float absAx=Math.abs(ax);
        float absAy=Math.abs(ay);
        
		if (absAx < 5 && absAy < 5)
			return;
		
		if(absAx>absAy){
			mRotation=ax<0?3:1;//270:90
		}else{
			mRotation=ay<0?2:0;//180:0
		}
        
		if(histogramView!=null)
			histogramView.updateRotation(mRotation);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}
	
	private void calculateTapArea(int x, int y, float areaMultiple, Rect rect) {
        int areaSize = (int) (Math.min(mPreview.getWidth(), mPreview.getHeight()) * areaMultiple / 20);
        int left = clamp(x - areaSize, 0, mPreview.getWidth() - 2 * areaSize);
        int top = clamp(y - areaSize, 0, mPreview.getHeight() - 2 * areaSize);

        RectF rectF = new RectF(left, top, left + 2 * areaSize, top + 2 * areaSize);
//        mMatrix.mapRect(rectF);
        rectFToRect(rectF, rect);
    }
	
	public static int clamp(int x, int min, int max) {
        if (x > max) return max;
        if (x < min) return min;
        return x;
    }
	
	public static void rectFToRect(RectF rectF, Rect rect) {
        rect.left = Math.round(rectF.left);
        rect.top = Math.round(rectF.top);
        rect.right = Math.round(rectF.right);
        rect.bottom = Math.round(rectF.bottom);
    }
}
