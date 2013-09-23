package jp.oreore.hk.json.obj;

import jp.oreore.hk.json.Entry;
import jp.oreore.hk.json.Util;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;
import android.util.Log;

public class Book {
	private static final String TAG = "Book";

	private final JSONObject self;

	private static Entry<JSONObject> colophon = new Entry<JSONObject>("colophon");
	private static Entry<JSONObject> attributes = new Entry<JSONObject>("attributes");
	private static Entry<String> path = new Entry<String>("path");
	
	public static final Book EndMark = new Book();

	private Book() {
		self = new JSONObject();
	}
	public Book(String s) {
		JSONObject o = new JSONObject();
		try {
			o = new JSONObject(s);
		} catch (JSONException e) {
			Log.e(TAG, "Book(s) Illeagal string.[" + s + "]", e);
		} finally {
			this.self = o;
		}
	}
	public static Book getEmptyInstance() {
		return new Book();
	}

	public Colophon getColophon() { return new Colophon(colophon.get(self)); }
	public Attributes getAttributes() { return new Attributes(attributes.get(self)); }
	public String getPath() { return path.get(self); }

	public void setColophon(Colophon c) { colophon.set(self, c.toJSONObject());}
	public void setAttributes(Attributes a) { attributes.set(self, a.toJSONObject());}
	public void setPath(String u) { path.set(self, u); }

	public JSONObject toJSONObject() { return self; }
	
	public void setOverride(Book b) {
		Colophon c = b.getColophon();
		setColophon(getColophon().setOverride(c));
		Attributes a = b.getAttributes();
		setAttributes(getAttributes().setOverride(a));
	}
	
	public String getBackFaceFname() {
		String prefix = getAttributes().getBackImagePrefix();
		return getPath() + prefix + "00.jpg";
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
		Book other = (Book) obj;
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
