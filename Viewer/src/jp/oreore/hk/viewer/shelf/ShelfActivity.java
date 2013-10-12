package jp.oreore.hk.viewer.shelf;

import jp.oreore.hk.file.dir.DirSimple;
import jp.oreore.hk.file.json.JsonLibrary;
import jp.oreore.hk.iface.IBookOpener;
import jp.oreore.hk.iface.IShelfLogic;
import jp.oreore.hk.json.obj.Book;
import jp.oreore.hk.json.obj.Library;
import jp.oreore.hk.types.ItemType;
import jp.oreore.hk.types.PageType;
import jp.oreore.hk.viewer.R;
import jp.oreore.hk.viewer.ViewerUtil;
import jp.oreore.hk.viewer.book.BookActivity;
import jp.oreore.hk.viewer.library.LibraryActivity;
import android.net.Uri;
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
				implements SearchView.OnQueryTextListener, IBookOpener {
	private static final String TAG = "ShelfActivity";
	
	// for intent bundle
	public static final String IKEY_BUNDLE = "bundle";
	// for intent bundle key
	public static final String IKEY_LIBRARY_PATH = "libpath";
	public static final String IKEY_JSON_LIBRARY = "library";
	// for bundle of savedInstanceState
	private static final String KEY_LIBRARY_PATH = "libpath";
	private static final String KEY_JSON_LIBRARY = "library";
	private static final String KEY_VIEW_MODE = "viewmode";
	private static final String KEY_VIEW_INDEX = "viewindex";
	
	private String libPath;
	private Library currentPosition;
	private IShelfLogic logic;
	private boolean needWriteLibrary = false;
	private ViewerUtil.ShelfViewMode viewMode;
	private int viewIndex;
	
	private SearchView searchView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_shelf);

		getActionBar().setDisplayHomeAsUpEnabled(true);

		viewMode = ViewerUtil.ShelfViewMode.BackFace;
		viewIndex = 0;
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
		outState.putString(KEY_VIEW_MODE, viewMode.name());
		outState.putInt(KEY_VIEW_INDEX, viewIndex);
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
    
    private void callBook(String bookPath) {
        Bundle appData = new Bundle();
        appData.putString(BookActivity.IKEY_LIBRARY_PATH, libPath);
        if(!TextUtils.isEmpty(bookPath)) {
            currentPosition.setBookPath(bookPath);
        }
        currentPosition.setPage(PageType.Book);
        appData.putString(BookActivity.IKEY_JSON_LIBRARY, currentPosition.toString());
    	Intent intent = new Intent(this, BookActivity.class);
    	intent.setAction(Intent.ACTION_VIEW);
    	intent.putExtra(BookActivity.IKEY_BUNDLE, appData);
		startActivity(intent);
    }

    private void callHtml(Book b) {
    	String path = b.getPath();
    	String fpath = path + b.getAttributes().getFirstPage();
    	Uri uri = Uri.parse("file://" + fpath);
    	Intent intent = new Intent(Intent.ACTION_VIEW);
    	intent.setDataAndType(uri, b.getAttributes().getMimeType());
    	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
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
    		// comes at search on shelf activity
    		Log.d(TAG, "Handle Intent.ACTION_SEARCH.");
            initData.putString(KEY_LIBRARY_PATH, libPath);
        	String query = intent.getStringExtra(SearchManager.QUERY);
        	currentPosition.setSearchCondition(query);
        	currentPosition.setShelfPath("");
            initData.putString(KEY_JSON_LIBRARY, currentPosition.toString());
    	} else if(Intent.ACTION_VIEW.equals(act)) {
    		Log.d(TAG, "Handle Intent.ACTION_VIEW.");
    		Bundle appData = intent.getBundleExtra(IKEY_BUNDLE);
    		if(appData != null) {
                initData.putString(KEY_LIBRARY_PATH, appData.getString(IKEY_LIBRARY_PATH));
                initData.putString(KEY_JSON_LIBRARY, appData.getString(IKEY_JSON_LIBRARY));
    		}
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
		String viewmodeStr = savedInstanceState.getString(KEY_VIEW_MODE);
		if(!TextUtils.isEmpty(viewmodeStr)) {
			viewMode = ViewerUtil.ShelfViewMode.of(viewmodeStr);
		}
		viewIndex = savedInstanceState.getInt(KEY_VIEW_INDEX, 0);
    }

    // return true if need finish
    private boolean init(Bundle savedInstanceState) {
    	boolean ret = false;
    	if(setCurrentPosition(savedInstanceState)) {
    		callBook("");
    		return true;
    	}
    	String query = currentPosition.getSearchCondition();
    	if(TextUtils.isEmpty(query)) {
    		String shelfPath = currentPosition.getShelfPath() + getString(R.string.fname_shelf_json);
        	logic = new LogicShelf(this, viewMode, viewIndex, libPath, shelfPath);
    	} else {
        	logic = new LogicSearch(this, viewMode, viewIndex, libPath, query);
    	}
    	if(!logic.isValidParameter(currentPosition)) {
    		backToLibrary();
    		return true;
    	}
    	logic.startReadBooks();
    	return ret;
    }
    
    // make library info if bundled, return true if move to book
    private boolean setCurrentPosition(Bundle savedInstanceState) {
		String lpath = savedInstanceState.getString(KEY_LIBRARY_PATH);
		if(!TextUtils.isEmpty(lpath)) {
			libPath = lpath;
			String libJsonStr = savedInstanceState.getString(KEY_JSON_LIBRARY);
			currentPosition = new Library(libJsonStr);
			if(PageType.Book == currentPosition.getPage() && !TextUtils.isEmpty(currentPosition.getBookPath())) {
				return true;
			}
		}
		currentPosition.setPage(PageType.Shelf);
		return false;
    }
    
    // write library.json
    private void writeCurrentPosition() {
		DirSimple libdir = new DirSimple(libPath);
		if(!libdir.isValid()) {
			return;
		}
    	currentPosition.removeBookPath();
		String libfnm = getString(R.string.fname_library_json);
		JsonLibrary f = new JsonLibrary(libPath + libfnm, null);
    	if(!f.write(currentPosition)) {
    		String msg = "write failed.[" + libfnm + "]";
    		Log.e(TAG, msg);
			ViewerUtil.printToast(this, msg);
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
    
    // IBookOpener
    public void openBook(Book b) {
    	ItemType itemType = b.getAttributes().getItemType();
    	if(ItemType.Book == itemType) {
    		callBook(b.getPath());
    		finish();
    	} else if(ItemType.Html == itemType) {
    		callHtml(b);
    	} else {
    		Log.e(TAG, "unknown itemType[" + itemType + "]");
    	}
    }
    
    // IBookOpener
    public void setViewMode(ViewerUtil.ShelfViewMode mode) {
    	viewMode = mode;
    }

    // IBookOpener
    public void setBacksIndex(int idx) {
    	viewIndex = idx;
    }
}
