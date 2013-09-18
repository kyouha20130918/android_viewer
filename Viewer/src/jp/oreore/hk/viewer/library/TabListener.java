package jp.oreore.hk.viewer.library;

import jp.oreore.hk.iface.IShelvesMaker;
import jp.oreore.hk.iface.ITabSelectedInform;
import jp.oreore.hk.viewer.R;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.ActionBar;
import android.app.FragmentManager;
import android.app.FragmentTransaction;

// ref : http://yan-note.blogspot.jp/2012/10/android-fragmenttab.html
// android BUG : onCreate runs twice at orientation
// android BUG : argument of FragmentTransaction is always null
public class TabListener<T extends Fragment> implements ActionBar.TabListener {
    private Fragment mFragment;
    private final Activity mActivity;  
    private final String mTag;
    private final Class<T> mClass;
    
    public TabListener(Activity activity, String tag, Class<T> clz) {
    	mActivity = activity;
    	mTag = tag;
    	mClass = clz;
    	mFragment = mActivity.getFragmentManager().findFragmentByTag(mTag);
		if(mFragment != null && mActivity instanceof IShelvesMaker && mFragment instanceof TabFragment) {
			((TabFragment)mFragment).init((IShelvesMaker)mActivity);
		}
    }
    
    public void onTabSelected(Tab tab, FragmentTransaction ft) {
    	if (mFragment == null) {
    		mFragment = Fragment.instantiate(mActivity, mClass.getName());
    		FragmentManager fm = mActivity.getFragmentManager();
    		fm.beginTransaction().add(R.id.container, mFragment, mTag).commit();
    		if(mActivity instanceof IShelvesMaker && mFragment instanceof TabFragment) {
    			((TabFragment)mFragment).init((IShelvesMaker)mActivity);
    		}
  		} else {
    		if (mFragment.isDetached()) {
	    		FragmentManager fm = mActivity.getFragmentManager();
	    		fm.beginTransaction().attach(mFragment).commit();
	   		}
	    }
    	if(mActivity instanceof ITabSelectedInform) {
    		((ITabSelectedInform)mActivity).informTabSelected(tab);
    	}
	}
    
    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
    	if (mFragment != null) {
    		FragmentManager fm = mActivity.getFragmentManager();
    		fm.beginTransaction().detach(mFragment).commit();
    	}
    }
    
    public void onTabReselected(Tab tab, FragmentTransaction ft) {
    	;
    }
}
