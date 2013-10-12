package jp.oreore.hk.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

public class LinearLayoutOfIntercepetTouchEvent extends LinearLayout {
	//private static final String TAG = "LinearLayoutOfIntercepetTouchEvent";

	protected OnTouchListener touchListener; 

	public LinearLayoutOfIntercepetTouchEvent(Context context) {
		super(context);
	}
	
	public LinearLayoutOfIntercepetTouchEvent(Context context, AttributeSet att) {
		super(context, att);
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		//Log.d(TAG, "onInterceptTouchEvent : " + ev.toString());
		return true;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		//Log.d(TAG, "onTouchEvent : " + ev.toString());
		touchListener.onTouch(this, ev);
		return true;
	}
	
	@Override
	public void setOnTouchListener(OnTouchListener listener) {
		touchListener = listener;
	}

}
