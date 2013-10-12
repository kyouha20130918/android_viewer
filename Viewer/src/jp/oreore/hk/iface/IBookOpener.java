package jp.oreore.hk.iface;

import jp.oreore.hk.json.obj.Book;
import jp.oreore.hk.viewer.ViewerUtil;

public interface IBookOpener {

	void openBook(Book b);
	void setViewMode(ViewerUtil.ShelfViewMode mode);
	void setBacksIndex(int idx);
}
