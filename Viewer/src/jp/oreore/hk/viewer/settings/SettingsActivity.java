package jp.oreore.hk.viewer.settings;

import jp.oreore.hk.viewer.R;
import jp.oreore.hk.viewer.library.LibraryActivity;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

public class SettingsActivity extends Activity {
	private static final String TAG = "SettingsActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setAppTitleWithVersionName();
		
		 // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();

	}

	//
	// for menu
	//
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
        	backToLibrary();
        	finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // move to Library
    private void backToLibrary() {
		Log.d(TAG, "Call Library.");
		
    	Intent intent = new Intent(this, LibraryActivity.class);
    	intent.setAction(Intent.ACTION_VIEW);
		startActivity(intent);
    }
    
    //
    // internal method
    //
    
	private void setAppTitleWithVersionName() {
		String appName = (String)getTitle();
		
		PackageManager pkg = getPackageManager();
		String pkgName = getPackageName();
		try {
			PackageInfo info = pkg.getPackageInfo(pkgName, PackageManager.GET_META_DATA);
			appName = appName + " version " + info.versionName;
		} catch (NameNotFoundException e) {
			Log.w(TAG, e.getMessage());
		}
		setTitle(appName);
	}
    
	//
	// for setting fragment
	//
	
	public static class SettingsFragment extends PreferenceFragment
							implements OnSharedPreferenceChangeListener {
	    @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);

	        // Load the preferences from an XML resource
	        addPreferencesFromResource(R.xml.settings);
	    }
	    
	    @Override
	    public void onStart() {
	    	super.onStart();

	    	Log.d(TAG, "Fragment - onStart.");
	    	
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
			String prefKeyOfLibraryPath = getString(R.string.prefkey_library_path);
			setSummaryToCurretnValue(prefKeyOfLibraryPath, sharedPreferences);
	        
	        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
	    }

	    @Override
	    public void onResume() {
	        super.onResume();
	        getPreferenceScreen().getSharedPreferences()
	                .registerOnSharedPreferenceChangeListener(this);
	    }
	    
	    @Override
	    public void onPause() {
	        super.onPause();
	        getPreferenceScreen().getSharedPreferences()
	                .unregisterOnSharedPreferenceChangeListener(this);
	    }

		//
		// for reference change listener
		//
		
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			String prefKeyOfLibraryPath = getString(R.string.prefkey_library_path);
			if(prefKeyOfLibraryPath.equals(key)) {
				setSummaryToCurretnValue(prefKeyOfLibraryPath, sharedPreferences);
			}
		}
		
		//
		// internal methods
		//
		
		// set summary to current value
		private void setSummaryToCurretnValue(String prefKey, SharedPreferences sharedPreferences) {
			Preference pref = findPreference(prefKey);
			String value = sharedPreferences.getString(prefKey, "");
			pref.setSummary(value);
		}
	}
	
}
