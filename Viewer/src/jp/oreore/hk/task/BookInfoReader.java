package jp.oreore.hk.task;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import android.util.Log;
import jp.oreore.hk.file.dir.DirShelf;
import jp.oreore.hk.file.json.JsonBook;
import jp.oreore.hk.iface.IBooksMaker;
import jp.oreore.hk.iface.ITaskStatusChecker;
import jp.oreore.hk.json.obj.Book;

public class BookInfoReader {
    private static final String TAG = "BookInfoReader";

    private boolean mExitTasksEarly = false;
    protected boolean mPauseWork = false;
    private final Object mPauseWorkLock = new Object();

    private WeakReference<IBooksMaker> makerReference;
    private String shelfPath;
    private String searchQuery;
    private String jsonFname;
    private int maxSearchCount;
    private WeakReference<List<Book>> bookQeueuReference;
    
    public BookInfoReader(IBooksMaker m, String shelf, String query, String fnm, int maxCnt, List<Book> q) {
    	makerReference = new WeakReference<IBooksMaker>(m);
    	shelfPath = shelf;
    	searchQuery = query;
    	jsonFname = fnm;
    	maxSearchCount = maxCnt;
    	bookQeueuReference = new WeakReference<List<Book>>(q);
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
		Log.d(TAG, "BookInfoRead started.");
    }
    
    //
    // task
    //
    
    private class Reader extends AsyncTask<Void, Void, Void> {
    	protected void onPostExecute(Void result) {
    		IBooksMaker m = makerReference.get();
    		if(m == null) {
    			Log.d(TAG, "onPost miss notify.");
    			return;		// destroyed
    		}
    		m.notifyComplete();
			Log.d(TAG, "onPost notified.");
    	}
    	
    	private void waitIfPause() {
            synchronized (mPauseWorkLock) {
                while (mPauseWork && !isCancelled()) {
                    try {
                        mPauseWorkLock.wait();
                    } catch (InterruptedException e) {}
                }
            }
    	}

        private class BreakChecker implements ITaskStatusChecker {
        	public boolean shouldBeBreak() {
        		return (isCancelled() || mExitTasksEarly);
        	}
        }

		@Override
		protected Void doInBackground(Void... params) {
			Queue<Book> targetList = new LinkedList<Book>();
			BreakChecker bc = new BreakChecker();
			DirShelf ds = new DirShelf(shelfPath, jsonFname, searchQuery, maxSearchCount, bc);
			targetList.addAll(ds.getBooks());
			while(true) {
				Book b = targetList.poll();
				if(b == null) {
					break;		// finish
				}
				waitIfPause();
				if(isCancelled() || mExitTasksEarly) {
					break;		// escaped
				}
				String fnm = b.getPath() + jsonFname;
				File f = new File(fnm);
				if(!f.exists() || !f.canRead()) {
					continue;	// something changed
				}
				JsonBook bj = new JsonBook(fnm);
				Book ss = bj.read();
				List<Book> q = bookQeueuReference.get();
				if(q == null) {
					break;		// destroyed
				}
				q.add(ss);
			}
			return null;
		}
    }
}
