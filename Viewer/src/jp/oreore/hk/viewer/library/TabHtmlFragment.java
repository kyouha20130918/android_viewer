package jp.oreore.hk.viewer.library;

import jp.oreore.hk.types.ItemType;
import jp.oreore.hk.viewer.R;

public class TabHtmlFragment extends TabFragment {
	//private static final String TAG = "TabHtmlFragment";
	
	int getLayoutId() {
		return R.layout.fragment_tabhtml;
	}
	
	ItemType getItemType() {
		return ItemType.Html;
	}
}
