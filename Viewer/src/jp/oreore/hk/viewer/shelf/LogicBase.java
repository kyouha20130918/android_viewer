package jp.oreore.hk.viewer.shelf;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import jp.oreore.hk.iface.IBooksMaker;
import jp.oreore.hk.json.obj.Book;
import jp.oreore.hk.task.BookInfoReader;
import jp.oreore.hk.viewer.R;
import android.app.Activity;
import android.util.Log;
import android.widget.ArrayAdapter;

public abstract class LogicBase implements IBooksMaker {
	private static final String TAG = "LogicBase";

	protected Activity activity;
	protected List<Book> foundBooks;
	protected ArrayAdapter<String> adapter;
	protected BookInfoReader task;
	
	protected LogicBase(Activity act) {
		activity = act;
	}

	public void startReadBooks() {
		String jsonFname = activity.getString(R.string.fname_book_json);
		Integer maxCount = Integer.valueOf(activity.getString(R.string.max_search_count_in_shelf));
		foundBooks = Collections.synchronizedList(new LinkedList<Book>());
		
		if(task != null) {
			task.setExitTasksEarly(true);
		}
		task = new BookInfoReader(this, getShelfPath(), getSearchQuery(), jsonFname, maxCount, foundBooks);
	}

	public void setExitTasksEarly(boolean exit) {
		if(task != null) {
			task.setExitTasksEarly(true);
		}
	}
	
	//
	// abstract
	//
	
	protected abstract String getShelfPath();
	protected abstract String getSearchQuery();
	
	//
	// interface
	//

    // IBooksMaker
    public void notifyComplete() {
    	Log.d(TAG, "notifyComplete.");
    }

}
