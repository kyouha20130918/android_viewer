package jp.oreore.hk.json.obj;

import jp.oreore.hk.json.Entry;
import jp.oreore.hk.json.JsonUtil;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Mark implements Comparable<Mark> {
	private static final String TAG = "Mark";
	
	public enum MarkType {
		Temporaly("T"),
		Permanent("P"),
		;
		public String nm;
		private MarkType(String s) {
			nm = s;
		}
		public static MarkType of(String s) {
			for(MarkType m : MarkType.values()) {
				if(m.nm.equals(s)) {
					return m;
				}
			}
			return null;
		}
	}

	private final JSONObject self;

	private static Entry<String> mark = new Entry<String>("mark");
	private static Entry<String> pageName = new Entry<String>("pageName");
	private static Entry<String> comment = new Entry<String>("comment");

	private Mark() {
		self = new JSONObject();
	}

	public Mark(JSONObject o) {
		if(!mark.hasValue(o) || !pageName.hasValue(o)) {
			Log.e(TAG, "Mark(o) lack field.");
			o = new JSONObject();
		}
		self = o;
	}

	public boolean isTemporaly() {
		return (MarkType.Temporaly == MarkType.of(mark.get(self)));
	}

	public MarkType getMarkType() { String s = mark.get(self); return MarkType.of(s); }
	public String getPageName() { return pageName.get(self); }
	public String getComment() { return comment.get(self); }

	public void setMark(MarkType u) { mark.set(self, u.nm); }
	public void setPageName(String u) { pageName.set(self, u); }
	public void setComment(String u) { comment.set(self, u); }
	
	public JSONObject toJSONObject() { return self; }

	public static final Mark getNewInstanceOfTemporary(String pnm, String cmnt) {
		Mark ret = new Mark();
		ret.setMark(MarkType.Temporaly);
		ret.setPageName(pnm);
		ret.setComment(cmnt);
		return ret;
	}

	public static final Mark getNewInstanceOfPermanent(String pnm, String cmnt) {
		Mark ret = new Mark();
		ret.setMark(MarkType.Permanent);
		ret.setPageName(pnm);
		ret.setComment(cmnt);
		return ret;
	}

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
		String mark = getMarkType().nm;
		result = prime * result + mark.hashCode();
		String pnm = getPageName();
		result = prime * result + pnm.hashCode();
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
		Mark you = (Mark)obj;
		if(compareTo(you) == 0) {
			return true;
		}
		return false;
	}
	
	//
	// interface
	//

	// Comparable
	@Override
	public int compareTo(Mark you) {
		int meLess = -1;
		int youLess = 1;
		MarkType meType = getMarkType();
		MarkType youType = you.getMarkType();
		if(meType != youType) {
			if(MarkType.Temporaly == youType) {
				return youLess;
			}
			return meLess;
		}
		String mePnm = getPageName();
		String youPnm = you.getPageName();
		return mePnm.compareTo(youPnm);
	}
}
