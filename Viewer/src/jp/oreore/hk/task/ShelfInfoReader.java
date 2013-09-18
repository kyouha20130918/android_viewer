package jp.oreore.hk.task;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import android.util.Log;
import jp.oreore.hk.file.json.JsonShelf;
import jp.oreore.hk.iface.IShelvesMaker;
import jp.oreore.hk.json.obj.Shelf;

public class ShelfInfoReader {
    private static final String TAG = "ShelfInfoReader";

    private boolean mExitTasksEarly = false;
    protected boolean mPauseWork = false;
    private final Object mPauseWorkLock = new Object();

    private WeakReference<IShelvesMaker> makerReference;
    private String libraryPath;
    private String jsonFname;
	private Queue<Shelf> targetList;	// because single task
    private WeakReference<List<Shelf>> shelfQeueuReference;
    
    public ShelfInfoReader(IShelvesMaker m, String p, String j, List<Shelf> l, List<Shelf> q) {
    	makerReference = new WeakReference<IShelvesMaker>(m);
    	libraryPath = p;
    	jsonFname = j;
		targetList = new LinkedList<Shelf>();
    	targetList.addAll(l);
    	shelfQeueuReference = new WeakReference<List<Shelf>>(q);
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
    }

    //
    // task
    //
    
    private class Reader extends AsyncTask<Void, Void, Void> {
    	protected void onPostExecute(Void result) {
    		IShelvesMaker m = makerReference.get();
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
    	
		@Override
		protected Void doInBackground(Void... params) {
			while(true) {
				Shelf s = targetList.poll();
				if(s == null) {
					break;		// finish
				}
				waitIfPause();
				if(isCancelled() || mExitTasksEarly) {
					break;		// escaped
				}
				String fnm = libraryPath + s.getPath() + jsonFname;
				File f = new File(fnm);
				if(!f.exists() || !f.canRead()) {
					continue;	// something changed
				}
				JsonShelf sj = new JsonShelf(fnm);
				Shelf ss = sj.read();
				List<Shelf> q = shelfQeueuReference.get();
				if(q == null) {
					break;		// destroyed
				}
				q.add(ss);
			}
			List<Shelf> q = shelfQeueuReference.get();
			if(q != null) {
				q.add(Shelf.EndMark);
			}
			return null;
		}
    }
    
}
