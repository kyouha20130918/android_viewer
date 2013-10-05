package jp.oreore.hk.listener;

import jp.oreore.hk.iface.IPageTurner;
import jp.oreore.hk.screen.RawScreenSize;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

public class PageGesture extends GestureDetector.SimpleOnGestureListener {
    private static final String TAG = "PageGesture";

    private enum TouchArea {
    	//Left,
    	//Right,
    	CenterTop,
    	CenterMiddle,
    	Other,
    }
	private enum FlingDirection {
		Left,
		Right,
		Up,
		Down,
		NoMean
	}

    private IPageTurner turner;
    private boolean isR2L;
	private RawScreenSize rawSize;
    
    public PageGesture(IPageTurner t, boolean r2l, RawScreenSize r) {
    	turner = t;
    	isR2L = r2l;
    	rawSize = r;
    }
    
    @Override
    public boolean onDown(MotionEvent event) { 
        //Log.d(TAG, "onDown:" + event.toString()); 
        return true;
    }

    @Override
    public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
        //Log.d(TAG, "onFling:" + event1.toString() + ", " + event2.toString() + ", velocity=[" + velocityX + ", " + velocityY + "]"); 
    	double angle = getAngle(event1, event2);
    	FlingDirection d = toWhereFling(angle);
        Log.d(TAG, "flinged " + d.toString());
        
    	if(FlingDirection.Up == d) {
    		turner.moveToBack();
    	} else if(FlingDirection.Down == d) {
    		turner.moveToDetail();
    	} else if(isR2L && FlingDirection.Right == d) {
    		turner.turnPageToBackward();
    	} else if(!isR2L && FlingDirection.Left == d) {
    		turner.turnPageToBackward();
    	}
	    return true;
	}
	
	@Override
	public boolean onSingleTapUp(MotionEvent event) {
	    float rawX = event.getRawX();
	    float rawY = event.getRawY();
	    Log.d(TAG, "onSingleTapUp: " + event.toString() + ", raw=[" + rawX + ", " + rawY + "]");
        
        boolean showActionBar = false;
        TouchArea t = whereTouch(rawX, rawY);
        Log.d(TAG, "touched " + t.toString());
        
        if(TouchArea.CenterTop == t) {
        	turner.showActionBar();
        	showActionBar = true;
        } else if(TouchArea.CenterMiddle == t) {
        	turner.showPageDialog();
        } else {
        	turner.turnPageToForward();
        }
        if(!showActionBar) {
        	turner.hideActionBar();
        }
        return true;
	}

	//
	// internal method
	//
	
    private TouchArea whereTouch(float rawX, float rawY) {
    	TouchArea ret = TouchArea.Other;
    	
    	int rawWidth = rawSize.width;
    	int rawHeight = rawSize.height;
    	
    	int posX = (int)rawX;
    	int posY = (int)rawY;
    	
    	float ratioWidth = 0.4f;
    	int leftLimitPosX = (int)(1.0f * rawWidth * ratioWidth);
    	int rightLimitPosX = rawWidth - (int)(1.0f * rawWidth * ratioWidth);
    	float ratioTop = 0.1f;
    	int topLimitPosY = (int)(1.0f * rawHeight * ratioTop);
    	
    	if(posX >= leftLimitPosX && posX <= rightLimitPosX) {
    		ret = TouchArea.CenterMiddle;
    		if(posY <= topLimitPosY) {
        		ret = TouchArea.CenterTop;
        	}
    	}
    	
    	return ret;
    }
    
    private double getAngle(MotionEvent event1, MotionEvent event2) {
        float moveX = event2.getRawX() - event1.getRawX();
        float moveY = event2.getRawY() - event1.getRawY();
        double sita = Math.atan2(moveY, moveX);
        double angle = Math.toDegrees(sita);
        if(angle < 0.0) {
        	angle += 360.0d;
        }
        return angle;
    }

	private FlingDirection toWhereFling(double angle) {
		FlingDirection ret = FlingDirection.NoMean;
		
		if(angle <= 30.0d || angle >= 330.0d) {
			ret = FlingDirection.Right;
		} else if(angle >= 60.0d && angle <= 120.0d) {
			ret = FlingDirection.Down;
		} else if(angle >= 150.0d && angle <= 210.0d) {
			ret = FlingDirection.Left;
		} else if(angle >= 240.0d && angle <= 300.0d) {
			ret = FlingDirection.Up;
		}
		return ret;
	}
}
