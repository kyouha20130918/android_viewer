package jp.oreore.hk.json.obj;

import jp.oreore.hk.json.Entry;
import jp.oreore.hk.json.Util;
import jp.oreore.hk.types.ItemType;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Attributes {
	private static final String TAG = "Attributes";

	private final JSONObject self;

	private static Entry<String> direction = new Entry<String>("direction");
	private static Entry<String> searchPagenoFormat = new Entry<String>("searchPagenoFormat");
	private static Entry<String> backImagePrefix = new Entry<String>("backImagePrefix");
	private static Entry<String> coverImagePrefix = new Entry<String>("coverImagePrefix");
	private static Entry<String> introduction = new Entry<String>("introduction");
	private static Entry<String> itemType = new Entry<String>("itemType");
	private static Entry<String> twinView = new Entry<String>("twinView");

	public Attributes(JSONObject o) {
		if(o == null) {
			self = new JSONObject();
			return;
		}
		self = o;
	}

	public String getDirection() { return direction.get(self); }
	public String getSearchPagenoFormat() { return searchPagenoFormat.get(self); }
	public String getBackImagePrefix() { return backImagePrefix.get(self); }
	public String getCoverImagePrefix() { return coverImagePrefix.get(self); }
	public String getIntroduction() { return introduction.get(self); }
	public ItemType getItemType() { String s = itemType.get(self); return ItemType.of(s); }
	public boolean isTwinView() { return Boolean.valueOf(twinView.get(self)); }

	public void setDirection(String u) { direction.set(self, u); }
	public void setSearchPagenoFormat(String u) { searchPagenoFormat.set(self, u); }
	public void setBackImagePrefix(String u) { backImagePrefix.set(self, u); }
	public void setCoverImagePrefix(String u) { coverImagePrefix.set(self, u); }
	public void setIntroduction(String u) { introduction.set(self, u); }
	public void setItemType(ItemType u) { itemType.set(self, u.toString()); }
	public void setTwinView(boolean twin) { twinView.set(self, Boolean.valueOf(twin).toString()); }

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
