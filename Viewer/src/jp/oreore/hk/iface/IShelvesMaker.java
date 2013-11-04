package jp.oreore.hk.iface;

import java.util.List;

import android.util.Pair;
import android.widget.ArrayAdapter;
import jp.oreore.hk.json.obj.Shelf;
import jp.oreore.hk.types.ViewType;

public interface IShelvesMaker {
	List<Shelf> getShelfList(ViewType i);
	void registerTabInfo(ArrayAdapter<String> a, ViewType i);
	void notifyComplete();
	void informSelectedShelf(int pos, ViewType i);
	Pair<Boolean, Integer> shouldBeSelected();
}
