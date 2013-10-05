package jp.oreore.hk.task;

import java.lang.ref.WeakReference;
import java.util.List;

import android.content.Context;
import android.util.Log;
import jp.oreore.hk.file.dir.DirBook;
import jp.oreore.hk.iface.IPagesMaker;
import jp.oreore.hk.iface.ITaskStatusChecker;
import jp.oreore.hk.json.obj.Book;

public class BookPageReader {
    private static final String TAG = "BookPageReader";

    private boolean mExitTasksEarly = false;
    protected boolean mPauseWork = false;
    private final Object mPauseWorkLock = new Object();
    
    private WeakReference<IPagesMaker> makerReference;
    private Context context;
    private Book book;
    private WeakReference<List<String>> pageListReference;

    public BookPageReader(IPagesMaker m, Context con, Book b, List<String> l) {
    	makerReference = new WeakReference<IPagesMaker>(m);
    	context = con;
    	book = b;
    	pageListReference = new WeakReference<List<String>>(l);
    }

    public void setPauseWork(boolean pauseWork) {
        synchronized (mPauseWorkLock) {
            mPauseWork = pauseWork;
            if (!mPauseWork) {
                mPauseWorkLock.notifyAll();
            }
        }
    }

    public void setExitTasksEarly(boolean exitTasksEarly) {
        mExitTasksEarly = exitTasksEarly;
        setPauseWork(false);
    }

    public void exec() {
    	Reader task = new Reader();
		task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
		Log.d(TAG, "BookPageRead started.");
    }

    //
    // task
    //
    
    private class Reader extends AsyncTask<Void, Void, Void> {
    	protected void onPostExecute(Void result) {
    		IPagesMaker m = makerReference.get();
    		if(m == null) {
    			Log.d(TAG, "onPost miss notify.");
    			return;		// destroyed
    		}
    		m.notifyComplete();
			Log.d(TAG, "onPost notified.");
    	}
    	
        private class BreakChecker implements ITaskStatusChecker {
        	public boolean shouldBeBreak() {
        		return (isCancelled() || mExitTasksEarly);
        	}
        }

		@Override
		protected Void doInBackground(Void... params) {
			BreakChecker bc = new BreakChecker();
			DirBook db = new DirBook(book, context);
			List<String> list = db.getPageList(bc);
			List<String> ret = pageListReference.get();
			if(ret != null) {
				ret.addAll(list);
			}
			return null;
		}
    }
}
