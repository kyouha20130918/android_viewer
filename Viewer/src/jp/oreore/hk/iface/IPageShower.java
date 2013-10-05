package jp.oreore.hk.iface;

import java.util.Iterator;

import android.util.Pair;
import android.widget.ImageView;

public interface IPageShower {

	void setFirst(int idx);
	int getCurrentIdx();
	String getCurrentPpath();
	Iterator<Pair<String, ImageView>> iterator();
	void turnToForward();
	void turnToBackward();
	void clearView();
}
