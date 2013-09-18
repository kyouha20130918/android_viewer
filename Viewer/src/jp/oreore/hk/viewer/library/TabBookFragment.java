package jp.oreore.hk.viewer.library;

import jp.oreore.hk.types.ItemType;
import jp.oreore.hk.viewer.R;

public class TabBookFragment extends TabFragment {
	//private static final String TAG = "TabBookFragment";
	
	int getLayoutId() {
		return R.layout.fragment_tabbook;
	}
	
	ItemType getItemType() {
		return ItemType.Book;
	}
	
}
