package jp.oreore.hk.iface;

import java.util.Iterator;

import android.widget.ImageView;

public interface IPageShower {

	class ImageInfo {
		public final String path;
		public final ImageView iview;
		public ImageInfo(String p, ImageView v) {
			path = p;
			iview = v;
		}
	};
	void setFirst(int idx);
	int getCurrentIdx();
	String getCurrentPpath();
	Iterator<ImageInfo> iterator();
	String getNextPpath();
	void turnToForward();
	void turnToBackward();
	void clearView();
	void additionalAction(ImageInfo i);
	int getAllowFlingLimit();
}
