package jp.oreore.hk.screen;

import java.lang.reflect.Method;

import android.app.ActionBar;
import android.app.Activity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;

public class RawScreenSize {
	private static final String TAG = "RawScreenSize";
	
	public int width;
	public int height;
	public float density;
	public int densityDpi;
	
	private RawScreenSize(RawScreenSize r) {
		width = r.width;
		height = r.height;
		density = r.density;
		densityDpi = r.densityDpi;
	}
	
	public RawScreenSize(Activity act) {
		DisplayMetrics dm = getScreenPixels(act);
    	Display display = act.getWindowManager().getDefaultDisplay();
    	
    	width = -1;
    	height = -1;
    	int barHeight = 0;
		try {
			Method mGetRawH = Display.class.getMethod("getRawHeight");
			Method mGetRawW = Display.class.getMethod("getRawWidth");
			width = (Integer) mGetRawW.invoke(display);
			height = (Integer) mGetRawH.invoke(display);
		} catch (Exception e) {
			Log.w(TAG, "failure of getting raw screen size.[" + e.getMessage() + "]");
			width = dm.widthPixels;
			height = dm.heightPixels;
		}
		Log.d(TAG, "raw pixel width=" + width + ", height=" + height);
		
		ActionBar bar = act.getActionBar();
		if(bar.isShowing()) {
			barHeight = bar.getHeight();
			height -= barHeight;
			Log.d(TAG, "raw pixel(exclude actionBar) height=" + height);
		}
		
		density = dm.density;
		densityDpi = dm.densityDpi;
		Log.d(TAG, "density=" + density + ", dpi=" + densityDpi);
		
		float physicalWidthInchi = 1.0f * width / densityDpi; 
		float physicalHeightInchi = 1.0f * (height + barHeight) / densityDpi;
		Log.d(TAG, "calc. physical inchi width=" + physicalWidthInchi + ", height=" + physicalHeightInchi);
	}
	
	public static RawScreenSize getHalfWidth(RawScreenSize r) {
		RawScreenSize half = new RawScreenSize(r);
		half.width = (int)Math.floor((1.0f * half.width + 0.5f) / 2.0f);
		return half;
	}

	private DisplayMetrics getScreenPixels(Activity act) {
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        act.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		return displayMetrics;
	}
}
