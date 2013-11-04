package jp.oreore.hk.json.obj;

import jp.oreore.hk.json.Entry;
import jp.oreore.hk.json.JsonUtil;
import jp.oreore.hk.types.ViewType;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;
import android.util.Log;

public class Shelf {
	private static final String TAG = "Shelf";

	private final JSONObject self;

	private static Entry<String> name = new Entry<String>("name");
	private static Entry<String> itemType = new Entry<String>("itemType");
	private static Entry<String> path = new Entry<String>("path");
	
	public static final Shelf EndMark = new Shelf();

	private Shelf() {
		self = new JSONObject();
	}
	public Shelf(String s) {
		JSONObject o = new JSONObject();
		try {
			o = new JSONObject(s);
			if(!name.hasValue(o) || !itemType.hasValue(o)) {
				Log.e(TAG, "Shelf(s) lack field.[" + s + "]");
				o = new JSONObject();
			}
		} catch (JSONException e) {
			Log.e(TAG, "Shelf(s) Illeagal string.[" + s + "]", e);
		} finally {
			this.self = o;
		}
	}
	public static Shelf getEmptyInstance() {
		return new Shelf();
	}

	public String getName() { return name.get(self); }
	public ViewType getItemType() { String s = itemType.get(self); return ViewType.of(s); }
	public String getPath() { return path.get(self); }

	public void setName(String u) { name.set(self, u); }
	public void setItemType(ViewType u) { itemType.set(self, u.toString()); }
	public void setPath(String u) { path.set(self, u); }

	public JSONObject toJSONObject() { return self; }

	@Override
	public String toString() {
		String ret = "";
		try {
			ret = self.toString(JsonUtil.indentSpaces);
		} catch (JSONException e) {
			Log.e(TAG, "toString() illeagal.", e);
		}
		return ret;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		String path = getPath();
		if(TextUtils.isEmpty(path)) {
			path = "";
		}
		result = prime * result + path.hashCode();
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Shelf other = (Shelf) obj;
		String path = getPath();
		if(TextUtils.isEmpty(path)) {
			path = "";
		}
		String pathOther = other.getPath();
		if(TextUtils.isEmpty(pathOther)) {
			pathOther = "";
		}
		return path.equals(pathOther);
	}
}
