package jp.oreore.hk.json;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;

public class JsonUtil {
	private JsonUtil() {}
	
	public static final int indentSpaces = 2;
	
	@SuppressLint("SimpleDateFormat")
	public static String getNowStr() {
		Date now = new Date();
		String ret = (new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")).format(now);
		return ret;
	}

}
