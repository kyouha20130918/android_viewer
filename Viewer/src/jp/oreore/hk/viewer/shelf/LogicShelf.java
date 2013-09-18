package jp.oreore.hk.viewer.shelf;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import jp.oreore.hk.file.dir.DirSimple;
import jp.oreore.hk.iface.IShelfLogic;
import jp.oreore.hk.json.obj.Library;
import jp.oreore.hk.json.obj.Shelf;
import jp.oreore.hk.viewer.library.LibraryActivity;

public class LogicShelf extends LogicBase implements IShelfLogic {
	private static final String TAG = "LogicShelf";

	private Shelf currentShelf;
	
	public LogicShelf(Activity activity, String jsonShelfStr) {
		super(activity);
		currentShelf = new Shelf(jsonShelfStr);
		Log.d(TAG, "shelf:[" + currentShelf.getName() + "]");
	}
	
	@Override
	public boolean isValidParameter(Library currentPosition) {
		boolean ret = false;
		String shelfpath = currentShelf.getPath();
		DirSimple shelfdir = new DirSimple(shelfpath);
		if(shelfdir.isValid()) {
			ret = true;
			currentPosition.setShelfPath(shelfpath);
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
		appData.putString(LibraryActivity.IKEY_JSON_SHELF, currentShelf.toString());
	}

	@Override
	protected String getShelfPath() {
		return currentShelf.getPath();
	}

	@Override
	protected String getSearchQuery() {
		return "";
	}

}
