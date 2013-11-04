package jp.oreore.hk.viewer.library;

import java.util.ArrayList;
import java.util.List;

import jp.oreore.hk.iface.IShelvesMaker;
import jp.oreore.hk.json.obj.Shelf;
import jp.oreore.hk.types.ViewType;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView;

public abstract class TabFragment extends Fragment
						implements AdapterView.OnItemClickListener, ViewTreeObserver.OnGlobalLayoutListener {
	private static final String TAG = "TabFragment";
	
	protected ArrayAdapter<String> adapter;
	protected IShelvesMaker maker;
	
    @Override  
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	return inflater.inflate(getLayoutId(), container, false);
    }
    
    @Override
    public void onStart() {
    	Log.d(TAG, "onStart started.[" + getItemType().toString() + "]");
    	super.onStart();
    	
    	adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, getItemList());
    	ListView v = (ListView)getView();
    	v.setAdapter(adapter);
    	v.setOnItemClickListener(this);
    	ViewTreeObserver vt = v.getViewTreeObserver();
    	vt.addOnGlobalLayoutListener(this);
    }
    
    @Override
    public void onResume() {
    	Log.d(TAG, "onResume started.[" + getItemType().toString() + "]");
    	super.onResume();
    	
    	maker.registerTabInfo(adapter, getItemType());
    }
    
    @Override
    public void onPause() {
    	Log.d(TAG, "onPause started.[" + getItemType().toString() + "]");
    	super.onPause();
    }
    
    @Override
    public void onDestroy() {
    	Log.d(TAG, "onDestroy started.[" + getItemType().toString() + "]");
    	super.onDestroy();
    }
    
    //
    // helper method
    //
    
    // initialization
    public void init(IShelvesMaker m) {
    	maker = m;
    }
    
    // get view item
	protected List<String> getItemList() {
		List<String> ret = new ArrayList<String>();
		List<Shelf> shelves = maker.getShelfList(getItemType());
		for(Shelf s : shelves) {
			ret.add(s.getName());
		}
		Log.d(TAG, "getItemList.[" + getItemType().toString() + ", cnt=" + ret.size() + "]");
		return ret;
	}
	
	// self callback for selection
	private void setSelectedShelf() {
		if(maker != null) {
			ListView v = (ListView)getView();
			if(v != null) {
				Pair<Boolean, Integer> ret = maker.shouldBeSelected();
				if(ret.first.booleanValue()) {
					v.clearFocus();
					int pos = ret.second.intValue();
					v.setSelection(pos);
					Log.d(TAG, "set selected position.[" + pos + "]");
				}
			}
		}
	}
    
    //
    // abstract method
    //
    
    abstract int getLayoutId();
    abstract ViewType getItemType();
    
    //
    // interface
    //

    // AdapterView.OnItemClickListener
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    	if(maker == null) {
    		return;
    	}
    	maker.informSelectedShelf(position, getItemType());
    }
    
    // ViewTreeObserver.OnGlobalLayoutListener
    @Override
    public void onGlobalLayout() {
    	Log.d(TAG, "call onGlobalLayout.");
    	if(maker == null) {
    		return;
    	}
    	setSelectedShelf();
    }
}
