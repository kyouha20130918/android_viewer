package jp.oreore.hk.json;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Entry<U> {
	private static final String TAG = "Entry";
	
	public String name;
	@SuppressWarnings("unused")
	private Entry() {};
	public Entry(String name) {
		this.name = name;
	}
	public void set(JSONObject dst, U u) {
		try {
			dst.put(name, u);
		} catch (JSONException e) {
			Log.e(TAG, "put failed:[" + name + "]", e);
		}
	}
	@SuppressWarnings("unchecked")
	public U get(JSONObject src) {
		return (U)src.opt(name);
	}
	public void remove(JSONObject src) {
		src.remove(name);
	}
	@SuppressWarnings("unchecked")
	public boolean hasValue(JSONObject src) {
		U u = (U)src.opt(name);
		return (u != null);
	}
}
