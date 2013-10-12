package jp.oreore.hk.viewer;

import android.content.Context;
import android.content.res.Configuration;
import android.widget.Toast;

public class ViewerUtil {
	private ViewerUtil() {}

	// for navigation mode in library
	public enum NavMode {
		Standard
		,Tabs
	}
	
	// for display elements
	public enum Display {
		ON
		,OFF
	}
	
	// for orientation
	public enum OrientationMode {
		Portrait
		,Landscape
	}
	
	// for view mode in shelf
	public enum ShelfViewMode {
		BackFace
		,Cover
		;
		public static ShelfViewMode of(String s) {
			ShelfViewMode ret = BackFace;
			for(ShelfViewMode m : ShelfViewMode.values()) {
				if(m.name().equals(s)) {
					ret = m;
					break;
				}
			}
			return ret;
		}
	}

	public static void printToast(Context context, String msg) {
		Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
	}

	public static OrientationMode getOrientationMode(Context context) {
		OrientationMode ret = OrientationMode.Portrait;
		if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			ret = OrientationMode.Landscape;
		}
		return ret;
	}
}
