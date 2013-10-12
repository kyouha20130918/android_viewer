package jp.oreore.hk.iface;

import jp.oreore.hk.json.obj.Book;
import jp.oreore.hk.screen.RawScreenSize;

public interface ICoverShower {

	void clearImages();
	void setVisible();
	void setGone();
	void showCover(Book b, RawScreenSize rawSize);
}
