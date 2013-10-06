package jp.oreore.hk.iface;

public interface IPageTurner {

	void showActionBar();
	void hideActionBar();
	void turnPageToForward();
	void turnPageToBackward();
	void turnPageDirect(int idx);
	void showPageDialog();
	void moveToDetail();
	void moveToBack();

	int getPageCount();
	int getCurrentIdx();
	String getPageInfo(int idx);
	int getPageIdx(String pnm);
	boolean isR2L();
}
