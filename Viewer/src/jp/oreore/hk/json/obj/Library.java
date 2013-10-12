package jp.oreore.hk.json.obj;

import jp.oreore.hk.json.Entry;
import jp.oreore.hk.json.JsonUtil;
import jp.oreore.hk.types.ItemType;
import jp.oreore.hk.types.PageType;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Library {
	private static final String TAG = "Library";

	private final JSONObject self;

	private static Entry<String> page = new Entry<String>("page");
	private static Entry<String> tab = new Entry<String>("tab");
	private static Entry<String> shelf = new Entry<String>("shelf");
	private static Entry<String> search = new Entry<String>("search");
	private static Entry<String> book = new Entry<String>("book");
	
	public Library(JSONObject o) {
		if(!page.hasValue(o) || !tab.hasValue(o)) {
			Log.e(TAG, "Library(o) lack field.[" + o.toString() + "]");
			o = new JSONObject();
		}
		self = o;
	}
	public Library(String s) {
		JSONObject o = new JSONObject();
		try {
			o = new JSONObject(s);
			if(!page.hasValue(o) || !tab.hasValue(o)) {
				Log.e(TAG, "Library(s) lack field.[" + s + "]");
				o = new JSONObject();
			}
		} catch (JSONException e) {
			Log.e(TAG, "Library(s) Illeagal string.[" + s + "]", e);
		} finally {
			this.self = o;
		}
	}

	public boolean isValid() {
		return page.hasValue(self);
	}
	
	public PageType getPage()  { String s = page.get(self); return PageType.of(s); }
	public ItemType getTab() { String s = tab.get(self); return ItemType.of(s); }
	public String getShelfPath() { return shelf.get(self); }
	public String getSearchCondition() { return search.get(self); }
	public String getBookPath() { return book.get(self); }

	public void setPage(PageType u) { page.set(self, u.toString()); }
	public void setTab(ItemType u) { tab.set(self, u.toString()); }
	public void setShelfPath(String u) { shelf.set(self, u); }
	public void setSearchCondition(String u) { search.set(self, u); }
	public void setBookPath(String u) { book.set(self, u); }
	
	public void removeShelfPath() { shelf.remove(self); }
	public void removeBookPath() { book.remove(self); }

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
}
