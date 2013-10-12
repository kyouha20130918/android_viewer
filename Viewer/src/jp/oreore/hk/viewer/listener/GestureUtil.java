package jp.oreore.hk.viewer.listener;

import android.view.MotionEvent;

public class GestureUtil {

	private GestureUtil() {}
	
	enum FlingDirection {
		Left,
		Right,
		Up,
		Down,
		NoMean
	}

    public static double getAngle(MotionEvent event1, MotionEvent event2) {
        float moveX = event2.getRawX() - event1.getRawX();
        float moveY = event2.getRawY() - event1.getRawY();
        double sita = Math.atan2(moveY, moveX);
        double angle = Math.toDegrees(sita);
        if(angle < 0.0) {
        	angle += 360.0d;
        }
        return angle;
    }

	public static FlingDirection toWhereFling(double angle) {
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
