package jp.oreore.hk.json.obj;

import jp.oreore.hk.json.Entry;
import jp.oreore.hk.json.Util;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Colophon {
	private static final String TAG = "Colophon";

	private final JSONObject self;

	private static Entry<String> id = new Entry<String>("id");
	private static Entry<String> title = new Entry<String>("title");
	private static Entry<String> author = new Entry<String>("author");
	private static Entry<String> publisher = new Entry<String>("publisher");
	private static Entry<String> pubdate = new Entry<String>("pubdate");

	public Colophon(JSONObject o) {
		if(o == null) {
			self = new JSONObject();
			return;
		}
		self = o;
	}

	public String getId() { String s = id.get(self); return (s == null ? "" : s); }
	public String getTitle() { String s = title.get(self); return (s == null ? "" : s); }
	public String getAuther() { String s = author.get(self); return (s == null ? "" : s); }
	public String getPublisher() { String s = publisher.get(self); return (s == null ? "" : s); }
	public String getPubdate() { String s = pubdate.get(self); return (s == null ? "" : s); }

	public JSONObject toJSONObject() { return self; }

	@Override
	public String toString() {
		String ret = "";
		try {
			ret = self.toString(Util.indentSpaces);
		} catch (JSONException e) {
			Log.e(TAG, "toString() illeagal.", e);
		}
		return ret;
	}
}
