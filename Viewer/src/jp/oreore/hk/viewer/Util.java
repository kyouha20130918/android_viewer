package jp.oreore.hk.viewer;

import android.content.Context;
import android.widget.Toast;

public class Util {
	private Util() {}

	public static void printToast(Context context, String msg) {
		Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
	}
	
	public enum NavMode {
		Standard
		,Tabs
	}
	public enum Display {
		ON
		,OFF
	}
}
