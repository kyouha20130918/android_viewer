package jp.oreore.hk.viewer.book;

import jp.oreore.hk.file.dir.DirSimple;
import jp.oreore.hk.file.json.JsonBook;
import jp.oreore.hk.file.json.JsonLibrary;
import jp.oreore.hk.file.json.JsonNote;
import jp.oreore.hk.iface.IPageTurner;
import jp.oreore.hk.json.obj.Book;
import jp.oreore.hk.json.obj.Library;
import jp.oreore.hk.json.obj.Note;
import jp.oreore.hk.screen.RawScreenSize;
import jp.oreore.hk.types.PageType;
import jp.oreore.hk.viewer.R;
import jp.oreore.hk.viewer.ViewerUtil;
import jp.oreore.hk.viewer.detail.DetailActivity;
import jp.oreore.hk.viewer.listener.PageGesture;
import jp.oreore.hk.viewer.shelf.ShelfActivity;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;

public class BookActivity extends Activity implements IPageTurner {
	private static final String TAG = "BookActivity";

	// for intent bundle
	public static final String IKEY_BUNDLE = "bundle";
	// for intent bundle key
	public static final String IKEY_LIBRARY_PATH = "libpath";
	public static final String IKEY_JSON_LIBRARY = "library";
	// for bundle of local
	private static final String KEY_LIBRARY_PATH = "libpath";
	private static final String KEY_JSON_LIBRARY = "library";

	private String libPath;
	private Library currentPosition;
	private Book currentBook;
	private Note currentNote;
	private boolean needWriteLibrary = false;
	private boolean doneWriteNote = false;
	private RawScreenSize rawSize;
	private LogicPage logic;
	private GestureDetector detector;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate Start.");
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_book);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().hide();

		if(handleIntent(getIntent())) {
			backToShelf();
			finish();
			return;
		}
		detector = new GestureDetector(this, new PageGesture(this, currentBook.isR2L(), rawSize));
	}
	
	@Override
	protected void onResume() {
		Log.d(TAG, "onResume Start.");
	    super.onResume();
		
		Log.d(TAG, "book=[" + currentBook.getPath() + "]");
	    
	    if(logic != null) {
			logic.setExitTasksEarly(false);
			logic.startShowPage();
	    }
	}

    @Override
    public void onPause() {
		Log.d(TAG, "onPause Start.");
        super.onPause();
    }
	
	@Override
	protected void onDestroy() {
		Log.d(TAG, "onDestroy Start.");
	    super.onDestroy();

	    if(logic != null) {
			logic.setExitTasksEarly(true);
	    }
	    
	    if(!doneWriteNote) {
		    writeCurrentNote();
	    }
	    if(needWriteLibrary) {
			writeCurrentPosition();
	    }
	}
	
	@Override 
    public boolean onTouchEvent(MotionEvent event) { 
        this.detector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

	//
	// for menu
	//
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.d(TAG, "onCreateOptionsMenu Start.");
		
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.book, menu);
		
		return true;
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
        	backToShelf();
        	finish();
            return true;
    	case R.id.action_finish:
    		needWriteLibrary = true;
    		finish();
    		return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    //
    // call activity
    //

    private void backToShelf() {
		Log.d(TAG, "Back to Shelf.");
		
        Bundle appData = new Bundle();
        appData.putString(ShelfActivity.IKEY_LIBRARY_PATH, libPath);
        currentPosition.setPage(PageType.Shelf);
        appData.putString(ShelfActivity.IKEY_JSON_LIBRARY, currentPosition.toString());
    	Intent intent = new Intent(this, ShelfActivity.class);
    	intent.setAction(Intent.ACTION_VIEW);
    	intent.putExtra(ShelfActivity.IKEY_BUNDLE, appData);
		startActivity(intent);
    }
    
    private void callDetail() {
		Log.d(TAG, "Call detail.");
		
	    writeCurrentNote();
	    doneWriteNote = true;
	    
        Bundle appData = new Bundle();
        appData.putString(DetailActivity.IKEY_LIBRARY_PATH, libPath);
        appData.putString(DetailActivity.IKEY_JSON_LIBRARY, currentPosition.toString());
        appData.putString(DetailActivity.IKEY_JSON_BOOK, currentBook.toString());
        int idx = logic.getCurrentIdx();
        String pnm = logic.getPageInfo(idx);
        appData.putString(DetailActivity.IKEY_JSON_PAGENAME, pnm);
    	Intent intent = new Intent(this, DetailActivity.class);
    	intent.setAction(Intent.ACTION_VIEW);
    	intent.putExtra(DetailActivity.IKEY_BUNDLE, appData);
		startActivity(intent);
    }

    //
    // handle intent
    //
    
    // return true if need finish
    private boolean handleIntent(Intent intent) {
    	String act = intent.getAction();
    	Bundle initData = new Bundle();
    	
    	if(Intent.ACTION_VIEW.equals(act)) {
    		Log.d(TAG, "Handle Intent.ACTION_VIEW.");
    		Bundle appData = intent.getBundleExtra(IKEY_BUNDLE);
            initData.putString(KEY_LIBRARY_PATH, appData.getString(IKEY_LIBRARY_PATH));
            initData.putString(KEY_JSON_LIBRARY, appData.getString(IKEY_JSON_LIBRARY));
    	} else {
    		Log.e(TAG, "Recieve NoMean Intent[" + act + "].");
    		backToShelf();
    		return true;
    	}
    	return init(initData);
    }
    
    //
    // internal methods
    //
    
    // return true if need backToShelf and finish
    private boolean init(Bundle initData) {
    	setCurrentPosition(initData);
    	String fnm = currentPosition.getBookPath() + getString(R.string.fname_book_json);
    	JsonBook.init(getString(R.string.json_default_attributes), getString(R.string.fname_bookattr_json), libPath);
    	JsonBook jBook = new JsonBook(fnm);
    	if(!jBook.exists()) {
    		return true;
    	}
    	currentBook = jBook.read();
    	String fnote = currentPosition.getBookPath() + getString(R.string.fname_booknote_json);
    	JsonNote jNote = new JsonNote(fnote);
    	currentNote = jNote.read();
    	if(TextUtils.isEmpty(currentNote.getId())) {
    		currentNote.setId(currentBook.getColophon().getId());
    	}
		rawSize = new RawScreenSize(this);
    	logic = new LogicPage(this, currentBook, currentNote, rawSize);
    	logic.startReadPages();
    	return false;
    }

    // make library info from bundle
    private void setCurrentPosition(Bundle initData) {
		libPath = initData.getString(KEY_LIBRARY_PATH);
		String libJsonStr = initData.getString(KEY_JSON_LIBRARY);
		currentPosition = new Library(libJsonStr);
    }
    
    // write library.json
    private void writeCurrentPosition() {
		DirSimple libdir = new DirSimple(libPath);
		if(!libdir.isValid()) {
			return;
		}
		String libfnm = getString(R.string.fname_library_json);
		JsonLibrary f = new JsonLibrary(libPath + libfnm, null);
    	if(!f.write(currentPosition)) {
    		String msg = "write failed.[" + libfnm + "]";
    		Log.e(TAG, msg);
			ViewerUtil.printToast(this, msg);
    	}
    }

    // write booknote.json
    private void writeCurrentNote() {
    	String fnm = currentPosition.getBookPath() + getString(R.string.fname_booknote_json);
    	JsonNote f = new JsonNote(fnm);
    	if(!f.write(currentNote)) {
    		String msg = "write failed.[" + fnm + "]";
    		Log.e(TAG, msg);
    	}
    }
    
    //
    // for IPageTurner
    //
    
	public void showActionBar() {
		if(!getActionBar().isShowing()) {
			getActionBar().show();
		}
	}
	public void hideActionBar() {
		if(getActionBar().isShowing()) {
			getActionBar().hide();
		}
	}
	public void turnPageToForward() {
		logic.turnToForward();
	}
	public void turnPageToBackward() {
		logic.turnToBackward();
	}
	public void turnPageDirect(int idx) {
		logic.turnToDirect(idx);
	}
	public void showPageDialog() {
		PageJumpDialog dialog = PageJumpDialog.newInstance(this, currentNote);
		dialog.show(getFragmentManager(), "pageJumpDialog");
	}
	public void moveToDetail() {
		callDetail();
		finish();
	}
	public void moveToBack() {
		backToShelf();
    	finish();
	}
	public int getCurrentIdx() {
		return logic.getCurrentIdx();
	}
	public int getPageCount() {
		return logic.getPageCount();
	}
	public String getPageInfo(int idx) {
		return logic.getPageInfo(idx);
	}
	public int getPageIdx(String pnm) {
		String ppath = currentBook.getPath() + pnm;
		return logic.getPageIdx(ppath);
	}
	public boolean isR2L() {
		return currentBook.isR2L();
	}
}
