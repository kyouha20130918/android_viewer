package jp.oreore.hk.viewer.book;

import jp.oreore.hk.file.dir.DirSimple;
import jp.oreore.hk.file.json.JsonBook;
import jp.oreore.hk.file.json.JsonLibrary;
import jp.oreore.hk.json.obj.Book;
import jp.oreore.hk.json.obj.Library;
import jp.oreore.hk.types.PageType;
import jp.oreore.hk.viewer.R;
import jp.oreore.hk.viewer.Util;
import jp.oreore.hk.viewer.shelf.ShelfActivity;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class BookActivity extends Activity {
	private static final String TAG = "BookActivity";

	// for intent bundle
	public static final String IKEY_BUNDLE = "bundle";
	// for intent bundle key
	public static final String IKEY_LIBRARY_PATH = "libpath";
	public static final String IKEY_JSON_LIBRARY = "library";
	// for bundle of savedInstanceState
	private static final String KEY_LIBRARY_PATH = "libpath";
	private static final String KEY_JSON_LIBRARY = "library";

	private String libPath;
	private Library currentPosition;
	private Book currentBook;
	private boolean needWriteLibrary = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate Start.");
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_book);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		//getActionBar().hide();

		//if(savedInstanceState != null) {
		//	restoreSavedInstanceState(savedInstanceState);
		//}
		
		if(handleIntent(getIntent())) {
			backToShelf();
			finish();
			return;
		}
	}
	
	@Override
	protected void onResume() {
		Log.d(TAG, "onResume Start.");
	    super.onResume();
		
		Log.d(TAG, "book=[" + currentBook.getPath() + "]");
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
	    
	    if(needWriteLibrary) {
			writeCurrentPosition();
	    }
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
		Log.d(TAG, "onSaveInstanceState Start.");
		
		outState.putString(KEY_LIBRARY_PATH, libPath);
		outState.putString(KEY_JSON_LIBRARY, currentPosition.toString());
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {  
	    super.onRestoreInstanceState(savedInstanceState);
		Log.d(TAG, "onRestoreInstanceState Start.");
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
    
    private void restoreSavedInstanceState(Bundle savedInstanceState) {
		Log.d(TAG, "restore savedInstanceState Start.");
		
		libPath = savedInstanceState.getString(KEY_LIBRARY_PATH);
		String libJsonStr = savedInstanceState.getString(KEY_JSON_LIBRARY);
		currentPosition = new Library(libJsonStr);
    }
    
    // return true if need backToShelf and finish
    private boolean init(Bundle savedInstanceState) {
    	boolean ret = false;
    	setCurrentPosition(savedInstanceState);
    	String fnm = currentPosition.getBookPath() + getString(R.string.fname_book_json);
    	JsonBook.init(getString(R.string.json_default_attributes), getString(R.string.fname_bookattr_json), libPath);
    	JsonBook json = new JsonBook(fnm);
    	if(!json.exists()) {
    		return true;
    	}
    	currentBook = json.read();
    	return ret;
    }

    // make library, book info from bundle, or default
    private void setCurrentPosition(Bundle savedInstanceState) {
		libPath = savedInstanceState.getString(KEY_LIBRARY_PATH);
		String libJsonStr = savedInstanceState.getString(KEY_JSON_LIBRARY);
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
			Util.printToast(this, "write failed.[" + libfnm + "]");
    	}
    }
    
}
