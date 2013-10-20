package jp.oreore.hk.viewer.shelf;

import jp.oreore.hk.iface.IShelfSwitcher;
import jp.oreore.hk.task.ImageFetcher;
import jp.oreore.hk.viewer.listener.ShelfCoverGesture;
import android.app.Activity;
import android.widget.LinearLayout;

public class CoverShowerBase {
	protected Activity activity;
	protected ImageFetcher imageFetcher;
	protected IShelfSwitcher switcher;
	protected ShelfCoverGesture gesture;

	public CoverShowerBase(Activity act, ImageFetcher f, IShelfSwitcher s, int minMoveLen) {
		activity = act;
		imageFetcher = f;
		switcher = s;
		gesture = new ShelfCoverGesture(activity, switcher, minMoveLen);
	}
	
	protected void setGestureListener(LinearLayout layout) {
		layout.setOnTouchListener(gesture);
	}
}
