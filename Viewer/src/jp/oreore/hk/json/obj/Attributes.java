package jp.oreore.hk.json.obj;

import jp.oreore.hk.json.Entry;
import jp.oreore.hk.json.Util;
import jp.oreore.hk.types.BookAttrOnPath;
import jp.oreore.hk.types.BookDirection;
import jp.oreore.hk.types.ItemType;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Attributes {
	private static final String TAG = "Attributes";

	private final JSONObject self;

	private static Entry<String> itemType = new Entry<String>("itemType");
	private static Entry<String> direction = new Entry<String>("direction");
	private static Entry<String> searchPagenoFormat = new Entry<String>("searchPagenoFormat");
	private static Entry<String> backImagePrefix = new Entry<String>("backImagePrefix");
	private static Entry<String> coverImagePrefix = new Entry<String>("coverImagePrefix");
	private static Entry<String> introduction = new Entry<String>("introduction");
	private static Entry<Boolean> twinView = new Entry<Boolean>("twinView");
	private static Entry<String> mimeType = new Entry<String>("mimeType");
	private static Entry<String> firstPage = new Entry<String>("firstPage");

	public Attributes(JSONObject o) {
		if(o == null) {
			self = new JSONObject();
			return;
		}
		self = o;
	}

	public ItemType getItemType() { String s = itemType.get(self); return ItemType.of(s); }
	public BookDirection getDirection() { String s = direction.get(self); return BookDirection.of(s); }
	public String getSearchPagenoFormat() { return searchPagenoFormat.get(self); }
	public String getBackImagePrefix() { return backImagePrefix.get(self); }
	public String getCoverImagePrefix() { return coverImagePrefix.get(self); }
	public String getIntroduction() { return introduction.get(self); }
	public boolean isTwinView() { Boolean s = twinView.get(self); return (s == null ? true : s); }
	public String getMimeType() { return mimeType.get(self); }
	public String getFirstPage() { return firstPage.get(self); }
	
	public boolean hasItemType() { return itemType.hasValue(self); }
	public boolean hasDirection() { return direction.hasValue(self); }
	public boolean hasTwinView() { return twinView.hasValue(self); }

	public void setItemType(ItemType u) { itemType.set(self, u.toString()); }
	public void setDirection(BookDirection u) { direction.set(self, u.toString()); }
	public void setSearchPagenoFormat(String u) { searchPagenoFormat.set(self, u); }
	public void setBackImagePrefix(String u) { backImagePrefix.set(self, u); }
	public void setCoverImagePrefix(String u) { coverImagePrefix.set(self, u); }
	public void setIntroduction(String u) { introduction.set(self, u); }
	public void setTwinView(boolean twin) { twinView.set(self, Boolean.valueOf(twin)); }
	public void setMimeType(String u) { mimeType.set(self, u); }
	public void setFirstPage(String u) { firstPage.set(self, u); }

	public JSONObject toJSONObject() { return self; }
	
	public Attributes setOverride(Attributes a) {
		if(a.hasDirection()) { setDirection(a.getDirection()); }
		if(a.getSearchPagenoFormat() != null) { setSearchPagenoFormat(a.getSearchPagenoFormat()); }
		if(a.getBackImagePrefix() != null) { setBackImagePrefix(a.getBackImagePrefix()); }
		if(a.getCoverImagePrefix() != null) { setCoverImagePrefix(a.getCoverImagePrefix()); }
		if(a.getIntroduction() != null) { setIntroduction(a.getIntroduction()); }
		if(a.hasItemType()) { setItemType(a.getItemType()); }
		if(a.hasTwinView()) { setTwinView(a.isTwinView()); }
		if(a.getMimeType() != null) { setMimeType(a.getMimeType()); }
		if(a.getFirstPage() != null) { setFirstPage(a.getFirstPage()); }
		return this;
	}

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
	
	//
	// set from path
	//
	
	public void setFromPath(BookAttrOnPath a) {
		if(BookAttrOnPath.L2R == a) {
			setDirection(BookDirection.L2R);
		} else if(BookAttrOnPath.html == a) {
			setItemType(ItemType.Html);
		} else if(BookAttrOnPath.pdf == a) {
			setItemType(ItemType.Html);
			setMimeType("application/pdf");
		} else if(BookAttrOnPath.solo == a) {
			setTwinView(false);
		} else if(BookAttrOnPath.all == a) {
			String fmt = "[(^%s)|(^%s)]*.*";
			String s = String.format(fmt, getBackImagePrefix(), getCoverImagePrefix());
			setSearchPagenoFormat(s);
		}
	}
}
