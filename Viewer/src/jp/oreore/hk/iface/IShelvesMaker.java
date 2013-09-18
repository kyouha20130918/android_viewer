package jp.oreore.hk.iface;

import java.util.List;

import android.util.Pair;
import android.widget.ArrayAdapter;
import jp.oreore.hk.json.obj.Shelf;
import jp.oreore.hk.types.ItemType;

public interface IShelvesMaker {
	List<Shelf> getShelfList(ItemType i);
	void registerTabInfo(ArrayAdapter<String> a, ItemType i);
	void notifyComplete();
	void informSelectedShelf(int pos, ItemType i);
	Pair<Boolean, Integer> shouldBeSelected();
}
