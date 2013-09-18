package jp.oreore.hk.viewer.shelf;

import jp.oreore.hk.file.json.JsonLibrary;
import jp.oreore.hk.iface.IShelfLogic;
import jp.oreore.hk.json.obj.Library;
import jp.oreore.hk.types.PageType;
import jp.oreore.hk.viewer.R;
import jp.oreore.hk.viewer.Util;
import jp.oreore.hk.viewer.library.LibraryActivity;
import android.os.Bundle;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;

public class ShelfActivity extends Activity
				implements SearchView.OnQueryTextListener {
	private static final String TAG = "ShelfActivity";
	
	// for intent bundle
	public static final String IKEY_BUNDLE = "bundle";
	// for intent bundle key
	public static final String IKEY_LIBRARY_PATH = "libpath";
	public static final String IKEY_JSON_LIBRARY = "library";
	public static final String IKEY_JSON_SHELF = "shelf";
	// for bundle of savedInstanceState
	private static final String KEY_LIBRARY_PATH = "libpath";
	private static final String KEY_JSON_LIBRARY = "library";
	private static final String KEY_JSON_SHELF = "shelf";
	private static final String KEY_SEARCH_QUERY = "query";

	private String libPath;
	private Library currentPosition;
	private IShelfLogic logic;
	private boolean needWriteLibrary = false;
	
	private SearchView searchView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_shelf);

		getActionBar().setDisplayHomeAsUpEnabled(true);

		if(savedInstanceState != null) {
			restoreSavedInstanceState(savedInstanceState);
		}
		
		if(handleIntent(getIntent())) {
			finish();
			return;
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		Log.d(TAG, "onNewIntent Start.");
		
	    setIntent(intent);
	    if(handleIntent(intent)) {
			finish();
			return;
	    }
	}
	
	@Override
	protected void onResume() {
		Log.d(TAG, "onResume Start.");
	    super.onResume();
	    
	    if(logic != null) {
			logic.setExitTasksEarly(false);
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
		getMenuInflater().inflate(R.menu.shelf, menu);
		
	    // Associate searchable configuration with the SearchView
	    SearchManager searchManager = (SearchManager)getSystemService(Context.SEARCH_SERVICE);
	    MenuItem item = menu.findItem(R.id.search);
	    searchView = (SearchView)item.getActionView();
	    searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
	    if(currentPosition != null) {
	    	String query = currentPosition.getSearchCondition();
	    	if(!TextUtils.isEmpty(query)) {
	    		item.expandActionView();
	    		searchView.setQuery(query, false);
	    		searchView.clearFocus();
	    	}
	    }
	    searchView.setOnQueryTextListener(this);
	    
		return true;
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
        	backToLibrary();
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

    private void backToLibrary() {
		Log.d(TAG, "Back to Library.");
		
        Bundle appData = new Bundle();
        if(currentPosition != null) {
            appData.putString(LibraryActivity.IKEY_JSON_LIBRARY, currentPosition.toString());
        }
        if(logic != null) {
        	logic.setToBundleForBackToLibrary(appData);
        }
    	Intent intent = new Intent(this, LibraryActivity.class);
    	intent.setAction(Intent.ACTION_VIEW);
    	intent.putExtra(LibraryActivity.IKEY_BUNDLE, appData);
		startActivity(intent);
    }

    //
    // handle intent
    //
    
    // return true if need finish
    private boolean handleIntent(Intent intent) {
    	String act = intent.getAction();
    	Bundle initData = new Bundle();

    	if(Intent.ACTION_SEARCH.equals(act)) {
    		Log.d(TAG, "Handle Intent.ACTION_SEARCH.");
    		Bundle appData = intent.getBundleExtra(SearchManager.APP_DATA);
        	if(appData == null) {
                initData.putString(KEY_LIBRARY_PATH, libPath);
                initData.putString(KEY_JSON_LIBRARY, currentPosition.toString());
        	} else {
                initData.putString(KEY_LIBRARY_PATH, appData.getString(IKEY_LIBRARY_PATH));
                initData.putString(KEY_JSON_LIBRARY, appData.getString(IKEY_JSON_LIBRARY));
        	}
        	initData.putString(KEY_SEARCH_QUERY, intent.getStringExtra(SearchManager.QUERY));
    	} else if(Intent.ACTION_VIEW.equals(act)) {
    		Log.d(TAG, "Handle Intent.ACTION_VIEW.");
    		Bundle appData = intent.getBundleExtra(IKEY_BUNDLE);
            initData.putString(KEY_LIBRARY_PATH, appData.getString(IKEY_LIBRARY_PATH));
            initData.putString(KEY_JSON_LIBRARY, appData.getString(IKEY_JSON_LIBRARY));
            initData.putString(KEY_JSON_SHELF, appData.getString(IKEY_JSON_SHELF));
    	} else {
    		Log.e(TAG, "Recieve NoMean Intent[" + act + "].");
    		backToLibrary();
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

    // return true if need finish
    private boolean init(Bundle savedInstanceState) {
    	boolean ret = false;
    	setCurrentPosition(savedInstanceState);
    	String query = savedInstanceState.getString(KEY_SEARCH_QUERY);
    	if(TextUtils.isEmpty(query)) {
    		String jsonShelfStr = savedInstanceState.getString(KEY_JSON_SHELF);
        	logic = new LogicShelf(this, jsonShelfStr);
    	} else {
        	logic = new LogicSearch(this, libPath, query);
    	}
    	if(!logic.isValidParameter(currentPosition)) {
    		backToLibrary();
    		return true;
    	}
    	logic.startReadBooks();
    	return ret;
    }
    
    // make library info from bundle, or default
    private void setCurrentPosition(Bundle savedInstanceState) {
		libPath = savedInstanceState.getString(KEY_LIBRARY_PATH);
		String libJsonStr = savedInstanceState.getString(KEY_JSON_LIBRARY);
		currentPosition = new Library(libJsonStr);
		currentPosition.setPage(PageType.Shelf);
    }
    
    // write library.json
    private void writeCurrentPosition() {
    	currentPosition.removeBookPath();
		String libfnm = getString(R.string.fname_library_json);
		JsonLibrary f = new JsonLibrary(libPath + libfnm, null);
    	if(!f.write(currentPosition)) {
			Util.printToast(this, "write failed.[" + libfnm + "]");
    	}
    }
    
    //
    // interface
    //
    
    // SearchView.OnQueryTextListener
    public  boolean onQueryTextChange(String newText) {
    	return false;
    }
    // SearchView.OnQueryTextListener
    public boolean onQueryTextSubmit(String qeury) {
		searchView.clearFocus();
    	return false;
    }
}
