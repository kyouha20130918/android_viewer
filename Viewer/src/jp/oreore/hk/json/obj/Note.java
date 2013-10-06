package jp.oreore.hk.json.obj;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import jp.oreore.hk.json.Entry;
import jp.oreore.hk.json.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.util.Log;

public class Note {
	private static final String TAG = "Note";

	private final JSONObject self;
	private final Set<Mark> markList;

	private static Entry<String> id = new Entry<String>("id");
	private static Entry<JSONArray> marks = new Entry<JSONArray>("marks");
	private static Entry<String> update = new Entry<String>("update");

	private Note() {
		self = new JSONObject();
		markList = getEmptyMarkList();
		setId("");
		marks.set(self, new JSONArray());
	}
	public Note(String s) {
		JSONObject o = new JSONObject();
		try {
			o = new JSONObject(s);
			if(!id.hasValue(o) || !marks.hasValue(o) || !update.hasValue(o)) {
				Log.e(TAG, "Note(s) lack field.");
				o = new JSONObject();
			}
		} catch (JSONException e) {
			Log.e(TAG, "Note(s) classize failed.", e);
		}
		self = o;
		markList = (isValid() ? getMarkList(marks.get(o)) : getEmptyMarkList());
	}
	public static Note getEmptyInstance() {
		return new Note();
	}
	
	private Set<Mark> getEmptyMarkList() {
		return new TreeSet<Mark>();
	}

	private Set<Mark> getMarkList(JSONArray a) {
		Set<Mark> ret = getEmptyMarkList();
		for(int i = 0; i < a.length(); i ++) {
			try {
				Mark bm = new Mark(a.getJSONObject(i));
				ret.add(bm);
			} catch (JSONException e) {
				Log.e(TAG, "read Mark array failed.[i=" + i + "]", e);
			}
		}
		return ret;
	}
	
	private void setMarkList() {
		JSONArray a = new JSONArray();
		for(Mark m : markList) {
			a.put(m.toJSONObject());
		}
		marks.set(self, a);
	}

	public String getId() { return id.get(self); }
	public Set<Mark> getMarks() { return markList; }
	public String getUpdate() { return update.get(self); }
	
	public void setId(String u) { id.set(self, u); }

	public boolean isValid() {
		return id.hasValue(self);
	}

	public Mark getMarkOfTemporary() {
		for(Mark m : markList) {
			if(m.isTemporaly()) {
				return m;
			}
		}
		return null;
	}
	
	private void removeMarkOfTemporary() {
		for(Mark m : markList) {
			if(m.isTemporaly()) {
				markList.remove(m);
				break;
			}
		}
	}
	
	@SuppressLint("SimpleDateFormat")
	private void setLastUpdate() {
		String upd = (new SimpleDateFormat("yyyy/MM/dd HH:mm:ss Z")).format(new Date());
		update.set(self, upd);
	}
	
	public void addMark(Mark m) {
		if(m.isTemporaly()) {
			removeMarkOfTemporary();
			setLastUpdate();
		} else if(markList.contains(m)) {
			markList.remove(m);
		}
		markList.add(m);
		setMarkList();
	}
	
	public void removeMark(Mark m) {
		if(markList.contains(m)) {
			markList.remove(m);
			setMarkList();
		}
	}
	
	public Mark getMark(String pnm) {
		Mark ret = null;
		for(Mark m : markList) {
			if(m.isTemporaly()) {
				continue;
			}
			String p = m.getPageName();
			if(p.endsWith(pnm)) {
				ret = m;
				break;
			}
		}
		return ret;
	}
	
	public List<Mark> getPermanentMarkList() {
		List<Mark> ret = new ArrayList<Mark>();
		for(Mark m : markList) {
			if(m.isTemporaly()) {
				continue;
			}
			ret.add(m);
		}
		return ret;
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
}
