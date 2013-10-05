package jp.oreore.hk.viewer.library;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import jp.oreore.hk.file.dir.DirLibrary;
import jp.oreore.hk.file.json.JsonLibrary;
import jp.oreore.hk.iface.IShelvesMaker;
import jp.oreore.hk.iface.ITabSelectedInform;
import jp.oreore.hk.json.obj.Library;
import jp.oreore.hk.json.obj.Shelf;
import jp.oreore.hk.task.ShelfInfoReader;
import jp.oreore.hk.types.ItemType;
import jp.oreore.hk.types.PageType;
import jp.oreore.hk.viewer.R;
import jp.oreore.hk.viewer.Util;
import jp.oreore.hk.viewer.settings.SettingsLibraryActivity;
import jp.oreore.hk.viewer.shelf.ShelfActivity;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ArrayAdapter;
import android.widget.SearchView;

public class LibraryActivity extends Activity implements ITabSelectedInform, IShelvesMaker {
	private static final String TAG = "LibraryAction";
	
	// for intent bundle
	public static final String IKEY_BUNDLE = "bundle";
	// for intent bundle key
	public static final String IKEY_JSON_LIBRARY = "library";
	public static final String IKEY_JSON_SHELF = "shelf";
	
	private Library currentPosition;
	private DirLibrary currentDir;
	private List<Shelf> foundShelves;
	private ShelfInfoReader shelfFinder;
	private ArrayAdapter<String> tabAdapter;
	private static class SelectedShelf {
		public Shelf shelf;
		public int position = -1;
	};
	SelectedShelf shouldBeShelfSelected;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate Start.");
		super.onCreate(savedInstanceState);

		PreferenceManager.setDefaultValues(this, R.xml.settings_library, false);

		if(handleIntent(getIntent())) {
			finish();	// receive intent to move another page
			return;
		}

		if(init()) {
			finish();	// last finish at another page
			return;
		}
		
		setContentView(R.layout.activity_library);

		Button btn = (Button)findViewById(R.id.buttonAccessLibrary);
		btn.setOnClickListener(new BtnAccessOnClickListener());
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
	protected void onStart() {
		Log.d(TAG, "onStart Start.");
		super.onStart();
		
		setupView();
	}
	
	@Override
	protected void onResume() {
		Log.d(TAG, "onResume Start.");
	    super.onResume();
	    
		if(shelfFinder != null) {
			shelfFinder.setExitTasksEarly(false);
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

		if(shelfFinder != null) {
			shelfFinder.setExitTasksEarly(true);
		}
		
		writeCurrentPosition();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
		Log.d(TAG, "onSaveInstanceState Start.");
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
		getMenuInflater().inflate(R.menu.library, menu);
		
	    // Associate searchable configuration with the SearchView
	    SearchManager searchManager = (SearchManager)getSystemService(Context.SEARCH_SERVICE);
	    SearchView searchView = (SearchView)menu.findItem(R.id.search).getActionView();
	    searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
	    
	    if(currentPosition != null) {
	    	String query = currentPosition.getSearchCondition();
	    	if(!TextUtils.isEmpty(query)) {
	    		searchView.setQuery(query, false);
	    	}
	    }
	    
		return true;
	}

    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    	case R.id.action_settings:
    		callSettingsForLibrary();
    		finish();
    		return true;
    	case R.id.action_finish:
    		finish();
    		return true;
    	}
    	return false;
    }

    //
    // call activity
    //
    
    private void callSettingsForLibrary() {
		Log.d(TAG, "Call Settings for Library.");
		
    	Intent intent = new Intent(this, SettingsLibraryActivity.class);
		startActivity(intent);
    }

    private enum SetNextPageType {
    	Yes,
    	No
    }
    private void callShelfForSearch(String query, SetNextPageType setNext) {
		Log.d(TAG, "Call Shelf for Search.");
		
        Bundle appData = new Bundle();
        appData.putString(ShelfActivity.IKEY_LIBRARY_PATH, currentDir.getPath());
        currentPosition.setSearchCondition(query);
        currentPosition.setShelfPath("");
        if(SetNextPageType.Yes == setNext) {
            currentPosition.setBookPath("");
            currentPosition.setPage(PageType.Shelf);
        }
        appData.putString(ShelfActivity.IKEY_JSON_LIBRARY, currentPosition.toString());
    	Intent intent = new Intent(this, ShelfActivity.class);
    	intent.setAction(Intent.ACTION_VIEW);
    	intent.putExtra(ShelfActivity.IKEY_BUNDLE, appData);
		startActivity(intent);
    }
    
    private void callMyself() {
		Log.d(TAG, "Call Myself.");
		
    	Intent intent = new Intent(LibraryActivity.this, LibraryActivity.class);
    	intent.setAction(Intent.ACTION_VIEW);
		startActivity(intent);
    }
    
    private void callShelfForDirectly(String shelfPath, SetNextPageType setNext) {
		Log.d(TAG, "Call Shelf for Directly.");
    	
        Bundle appData = new Bundle();
        appData.putString(ShelfActivity.IKEY_LIBRARY_PATH, currentDir.getPath());
        currentPosition.setSearchCondition("");
        currentPosition.setShelfPath(shelfPath);
        if(SetNextPageType.Yes == setNext) {
            currentPosition.setBookPath("");
            currentPosition.setPage(PageType.Shelf);
        }
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
    	boolean ret = false;
    	String act = intent.getAction();
    	if(Intent.ACTION_SEARCH.equals(act)) {
    		Log.d(TAG, "Handle Intent.ACTION_SEARCH.");
    		String query = intent.getStringExtra(SearchManager.QUERY);
    		callShelfForSearch(query, SetNextPageType.Yes);
    		ret = true;
    	} else if(Intent.ACTION_VIEW.equals(act)) {
    		Log.d(TAG, "Handle Intent.ACTION_VIEW.");
        	Bundle appData = intent.getBundleExtra(IKEY_BUNDLE);
    		if(appData != null) {
    			String libJsonStr = appData.getString(IKEY_JSON_LIBRARY);
    			setCurrentPosition(libJsonStr);
    			String shelfJsonStr = appData.getString(IKEY_JSON_SHELF);
    			if(!TextUtils.isEmpty(shelfJsonStr)) {
    				shouldBeShelfSelected = new SelectedShelf();
    				shouldBeShelfSelected.shelf = new Shelf(shelfJsonStr);
        			Log.d(TAG, "last shelf:[" + shouldBeShelfSelected.shelf.getName() + "]");
    			}
    		}
    		init();
    	} else {
    		Log.d(TAG, "Handle NoMean Intent[" + act + "].");
    	}
    	return ret;
    }
    
	//
	// for listener
	//

	// for Access-Library Button
    class BtnAccessOnClickListener implements  OnClickListener {
		@Override
		public void onClick(View v) {
			callMyself();
			setupView();
		}
    }
    
    //
    // internal methods
    //
    
    // return true if need finish
    private boolean init() {
		setCurrentDir();
		if(readCurrentPosition()) {
			if(TextUtils.isEmpty(currentPosition.getSearchCondition())) {
				callShelfForDirectly(currentPosition.getShelfPath(), SetNextPageType.No);
			} else {
				String query = currentPosition.getSearchCondition();
	    		callShelfForSearch(query, SetNextPageType.No);
			}
			return true;
		}
		if(currentDir.isValid()) {
			startReadShelves();
		}
		return false;
    }
    
    // set library path from preference
    private String getLibraryPath() {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		String libraryPath = sharedPref.getString(getString(R.string.prefkey_library_path), getString(R.string.default_library_path));
		//Log.d(TAG, "libraryPath : [" + libraryPath + "]");
		return libraryPath;
    }

    // check effective library or not
    private void setCurrentDir() {
    	if(currentDir != null) {
    		return;
    	}
		String libdir = getLibraryPath();
		String shelffname = getString(R.string.fname_shelf_json);
    	currentDir = new DirLibrary(libdir, shelffname);
    	currentDir.isValid();
    }
    
    // setup from intent info, or default
    private void setCurrentPosition(String libJsonStr) {
    	if(!TextUtils.isEmpty(libJsonStr)) {
    		currentPosition = new Library(libJsonStr);
    		currentPosition.setPage(PageType.Library);
    		return;
    	}
		String libjson = getString(R.string.json_default_library);
		currentPosition = new Library(libjson);
    }
    
    // read library.json and return true if should move shelf
    private boolean readCurrentPosition() {
    	if(currentPosition != null) {
    		return false;
    	}
		String libdir = getLibraryPath();
		String libfnm = getString(R.string.fname_library_json);
		String libjson = getString(R.string.json_default_library);
		JsonLibrary f = new JsonLibrary(libdir + libfnm, libjson);
		currentPosition = f.read();
		if(PageType.Library != currentPosition.getPage()
				&& (!TextUtils.isEmpty(currentPosition.getShelfPath()) || !TextUtils.isEmpty(currentPosition.getSearchCondition()))) {
			return true;
		}
		return false;
    }
    
    // write library.json
    private void writeCurrentPosition() {
    	if(!currentDir.isValid()) {
    		return;
    	}
    	currentPosition.removeBookPath();
    	currentPosition.removeShelfPath();
		String libdir = getLibraryPath();
		String libfnm = getString(R.string.fname_library_json);
		JsonLibrary f = new JsonLibrary(libdir + libfnm, null);
    	if(!f.write(currentPosition) && currentDir.isValid()) {
    		String msg = "write failed.[" + libfnm + "]";
    		Log.e(TAG, msg);
			Util.printToast(this, msg);
    	}
    }
    
    // read shelves with traversing directory in background
    // result set to foundShelves
    private void startReadShelves() {
		String jsonFname = getString(R.string.fname_shelf_json);
    	foundShelves = Collections.synchronizedList(new LinkedList<Shelf>());
    	if(shelfFinder != null) {
    		shelfFinder.setExitTasksEarly(true);
    	}
    	shelfFinder = new ShelfInfoReader(this, currentDir.getPath(), jsonFname, currentDir.getShelves(), foundShelves);
    	shelfFinder.exec();
    }

    //
    // around view
    //
    
    // make tab if effective library
    private void setupView() {
		if(!currentDir.isValid()) {
			switchNavigationBarMode(Util.NavMode.Standard);
			Util.printToast(this, "Invalid Library Directory.[" + currentDir.getPath() + "]");
			return;
		}
		switchNavigationBarMode(Util.NavMode.Tabs);
    }

    // set action-bar style
    private void switchNavigationBarMode(Util.NavMode m) {
    	final ActionBar actionBar = getActionBar();
    	boolean isModeTabs = (ActionBar.NAVIGATION_MODE_TABS == actionBar.getNavigationMode());
    	Log.d(TAG, "CurModeTabs:" + isModeTabs);
    	if(isModeTabs && Util.NavMode.Standard == m) {
    		actionBar.removeAllTabs();
    		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
    		setAccessLibraryBtn(Util.Display.ON);
    	} else if(!isModeTabs && Util.NavMode.Tabs == m) {
    		ItemType it = currentPosition.getTab();	// backup
    		setAccessLibraryBtn(Util.Display.OFF);
    		String tabNameBook = ItemType.Book.toString();
    		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
	        actionBar.addTab(actionBar.newTab()
	        		 .setText(tabNameBook)
	                 .setTabListener(new TabListener<TabBookFragment>(this, tabNameBook, TabBookFragment.class)));
       		String tabNameHtml = ItemType.Html.toString();
   	        actionBar.addTab(actionBar.newTab()
	        		 .setText(tabNameHtml)
   	                 .setTabListener(new TabListener<TabHtmlFragment>(this, tabNameHtml, TabHtmlFragment.class)));
   	        currentPosition.setTab(it);		// restore
   	        selectTab();
    	}
    }
    
    // access-library button on/off
    private void setAccessLibraryBtn(Util.Display d) {
		Button btn = (Button)findViewById(R.id.buttonAccessLibrary);
		int visibility = View.INVISIBLE;
		if(Util.Display.ON == d) {
			visibility = View.VISIBLE;
		}
		btn.setVisibility(visibility);
    }
    
    // select tab of currentPosition.tab
    private void selectTab() {
    	final ActionBar actionBar = getActionBar();
        String tabText = currentPosition.getTab().toString();
        for(int i = 0; i < actionBar.getTabCount(); i ++) {
        	Tab t = actionBar.getTabAt(i);
        	if(tabText.equalsIgnoreCase((String)t.getText())) {
        		t.select();
        		break;
        	}
        }
    }
    
    //
    // interface
    //

    // ITabSelectedInform
    public void informTabSelected(Tab t) {
    	String tabText = (String)t.getText();
    	ItemType it = ItemType.of(tabText);
    	currentPosition.setTab(it);
    	Log.d(TAG, "tab selected.[" + it.toString() + "]");
    }

    // IShelfMaker
    public List<Shelf> getShelfList(ItemType it) {
    	List<Shelf> ret = new ArrayList<Shelf>();
    	synchronized(foundShelves) {
    		Iterator<Shelf> itr = foundShelves.iterator();
    		while(itr.hasNext()) {
    			Shelf s = itr.next();
    			if(Shelf.EndMark == s) {
    				break;
    			}
    			if(it != s.getItemType()) {
    				continue;
    			}
    			ret.add(s);
    		}
    	}
    	return ret;
    }
    
    // at shelf-make complete or not
    private enum Complete { AtComplete, AtOther };
    // re-make view item
    private void resetItem(ItemType it, Complete c) {
    	tabAdapter.clear();
    	List<Shelf> ret = getShelfList(it);
    	for(int i = 0; i < ret.size(); i ++) {
    		Shelf s = ret.get(i);
    		if(it == s.getItemType()) {
        		tabAdapter.add(s.getName());
            	if(Complete.AtComplete == c
            			&& shouldBeShelfSelected != null
            			&& s.equals(shouldBeShelfSelected.shelf)) {
            		shouldBeShelfSelected.position = tabAdapter.getCount() - 1;
            		Log.d(TAG, "set selectedShelfPosition.[" + shouldBeShelfSelected.position + "]");
            	}
    		}
    	}
		tabAdapter.notifyDataSetChanged();
		Log.d(TAG, "notified.[" + it.toString() + ", cnt=" + tabAdapter.getCount() + "]");
    }
    
    // IShelfMaker
    public void registerTabInfo(ArrayAdapter<String> a, ItemType i) {
    	tabAdapter = a;
		resetItem(i, Complete.AtOther);
    }
    
    // IShelfMaker
    public void notifyComplete() {
    	if(tabAdapter == null) {
    		Log.d(TAG, "notify requested, but later.");
    		return;
    	}
		resetItem(currentPosition.getTab(), Complete.AtComplete);
    }
    
    // IShelfMaker
    public void informSelectedShelf(int pos, ItemType i) {
    	List<Shelf> ret = getShelfList(i);
    	Shelf s = ret.get(pos);
    	callShelfForDirectly(s.getPath(), SetNextPageType.Yes);
    	finish();
    }
    
    // IShelfMaker
    public Pair<Boolean, Integer> shouldBeSelected() {
    	if(shouldBeShelfSelected == null || shouldBeShelfSelected.position < 0) {
    		return Pair.create(Boolean.FALSE, Integer.valueOf(0));
    	}
    	Log.d(TAG, "return selected shelf info.");
    	int pos = shouldBeShelfSelected.position;
    	shouldBeShelfSelected = null;
    	return Pair.create(Boolean.TRUE, Integer.valueOf(pos));
    }
}
