package jp.oreore.hk.viewer.book;

import java.util.List;

import jp.oreore.hk.json.obj.Book;
import jp.oreore.hk.screen.RawScreenSize;

public class ShowBase {
	protected Book book;
	protected List<String> pageList;
	protected RawScreenSize rawSize;
	protected int idx;
	protected CalcIndex calcIdx;

	protected enum PagePos {
		Odd,
		Even,
		Center
	}

	public ShowBase(Book b, List<String> l, RawScreenSize r) {
		book = b;
		pageList = l;
		rawSize = r;
		calcIdx = new CalcIndex(pageList.size(), 1);
	}
	
	protected String getCurrentPpath() {
		return pageList.get(idx);
	}
	
	protected String getPname(int i) {
		return pageList.get(calcIdx.wrap(i));
	}
	
	protected int getCurrentIdx() {
		return idx;
	}
	
	protected int setIdx(int i) {
		idx = calcIdx.wrap(i);
		return idx;
	}
	
	protected int getOpositePageIdx() {
		if(PagePos.Odd == getCurrentPagePos()) {
			return calcIdx.wrapBackward(idx);
		}
		return calcIdx.wrapForward(idx);
	}

	protected PagePos getCurrentPagePos() {
		if((idx % 2) == 0) {
			return PagePos.Odd;
		}
		return PagePos.Even;
	}
	
	protected PagePos getOpositePagePos() {
		PagePos pos = getCurrentPagePos();
		if(PagePos.Odd == pos) {
			return PagePos.Even;
		}
		return PagePos.Odd;
	}
	
	protected void toForward() {
		idx = calcIdx.wrapForward(idx);
	}

	protected void toBackward() {
		idx = calcIdx.wrapBackward(idx);
	}
}
