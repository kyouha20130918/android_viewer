package jp.oreore.hk.viewer.listener;

import jp.oreore.hk.iface.IShelfSwitcher;
import jp.oreore.hk.viewer.listener.GestureUtil.FlingDirection;
import android.content.Context;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class ShelfBacksGesture extends GestureDetector.SimpleOnGestureListener
							implements OnTouchListener {
    private static final String TAG = "ShelfGesture";

	GestureDetector detector;
	IShelfSwitcher switcher;
	int minMoveLen;
	
	public ShelfBacksGesture() {
		super();
	}
	
	public ShelfBacksGesture(Context context, IShelfSwitcher s, int m) {
		detector = new GestureDetector(context, this);
		switcher = s;
		minMoveLen = m;
	}

	@Override
	public boolean onTouch(View v, MotionEvent e1) {
		return detector.onTouchEvent(e1);
	}

	@Override
	public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
        //Log.d(TAG, "onFling:" + event1.toString() + ", " + event2.toString() + ", velocity=[" + velocityX + ", " + velocityY + "]"); 
    	double angle = GestureUtil.getAngle(event1, event2, minMoveLen);
    	if(angle < 0.0d) {
            Log.d(TAG, "flinged, but too short. so ignored.");
    		return true;
    	}
    	FlingDirection d = GestureUtil.toWhereFling(angle);
        Log.d(TAG, "flinged " + d.toString());
        
        if(FlingDirection.Down == d) {
        	int posx = Math.round(event1.getX());
        	int posy = Math.round(event1.getY());
        	switcher.viewCover(posx, posy);
        }
        
        return true;
	}
}
