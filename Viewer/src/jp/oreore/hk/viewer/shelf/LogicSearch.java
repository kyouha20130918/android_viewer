package jp.oreore.hk.viewer.shelf;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import jp.oreore.hk.file.dir.DirSimple;
import jp.oreore.hk.iface.IShelfLogic;
import jp.oreore.hk.json.obj.Library;
import jp.oreore.hk.viewer.ViewerUtil;

public class LogicSearch extends LogicBase implements IShelfLogic {
	private static final String TAG = "LogicSearch";

	private String libPath;
	private String query;
	
	public LogicSearch(Activity activity, ViewerUtil.ShelfViewMode mode, int idx, String l, String q) {
		super(activity, mode, idx);
		libPath = l;
		query = q;
		Log.d(TAG, "query:[" + query + "]");
	}
	
	@Override
	public boolean isValidParameter(Library currentPosition) {
		boolean ret = false;
		DirSimple libdir = new DirSimple(libPath);
		if(libdir.isValid() && !TextUtils.isEmpty(query)) {
			ret = true;
			currentPosition.setSearchCondition(query);
		}
		return ret;
	}

	@Override
	public void startReadBooks() {
		super.startReadBooks();
	}

	@Override
	public void setExitTasksEarly(boolean exit) {
		super.setExitTasksEarly(exit);
	}

	@Override
	public void setToBundleForBackToLibrary(Bundle appData) {
		;	// nothing
	}

	@Override
	protected String getLibPath() {
		return libPath;
	}

	@Override
	protected String getShelfPath() {
		return libPath;
	}

	@Override
	protected String getSearchQuery() {
		return query;
	}

}
