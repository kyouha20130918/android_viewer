package jp.oreore.hk.viewer.library;

import jp.oreore.hk.types.ViewType;
import jp.oreore.hk.viewer.R;

public class TabBookFragment extends TabFragment {
	//private static final String TAG = "TabBookFragment";
	
	int getLayoutId() {
		return R.layout.fragment_tabbook;
	}
	
	ViewType getItemType() {
		return ViewType.Book;
	}
	
}
