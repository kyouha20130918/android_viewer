package jp.oreore.hk.viewer.book;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import android.util.Pair;
import android.widget.ImageView;
import jp.oreore.hk.iface.IPageShower;
import jp.oreore.hk.json.obj.Book;
import jp.oreore.hk.screen.RawScreenSize;

public class ShowSolo extends ShowBase implements IPageShower {
	private ImageView imageView;
	
	public ShowSolo(Book b, List<String> l, RawScreenSize r, ImageView v, String be) {
		super(b, l, r, be);
		imageView = v;
	}

	@Override
	public void setFirst(int i) {
		setIdx(i);
		while(isBlankPage(super.getCurrentPpath())) {
			toForward();
		}
	}
	
	@Override
	public int getCurrentIdx() {
		return super.getCurrentIdx();
	}

	@Override
	public String getCurrentPpath() {
		return super.getCurrentPpath();
	}
	
	private class Iter implements Iterator<Pair<String, ImageView>> {
		private int i = 1;
		@Override
		public boolean hasNext() {
			return (i > 0);
		}
		@Override
		public Pair<String, ImageView> next() {
			if(!hasNext()) {
				throw new NoSuchElementException();
			}
			i --;
			return Pair.create(getCurrentPpath(), imageView);
		}
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	};

	@Override
	public Iterator<Pair<String, ImageView>> iterator() {
		return new Iter();
	}
	
	@Override
	public String getNextPpath() {
		String ret = super.getNextPpath(1);
		if(isBlankPage(ret)) {
			ret = super.getNextPpath(2);
		}
		return ret;
	}

	@Override
	public void turnToForward() {
		toForward();
		while(isBlankPage(super.getCurrentPpath())) {
			toForward();
		}
	}

	@Override
	public void turnToBackward() {
		toBackward();
		while(isBlankPage(super.getCurrentPpath())) {
			toBackward();
		}
	}

	@Override
	public void clearView() {
		imageView.setImageDrawable(null);
	}
}
