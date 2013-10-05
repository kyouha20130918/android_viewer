package jp.oreore.hk.viewer.book;

import static jp.oreore.hk.image.CalcUtil.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;

import android.util.Pair;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import jp.oreore.hk.iface.IPageShower;
import jp.oreore.hk.image.ImageSize;
import jp.oreore.hk.json.obj.Book;
import jp.oreore.hk.screen.RawScreenSize;

public class ShowTwin extends ShowBase implements IPageShower {
	
	private static class QueueItem {
		public PagePos pos;
		public CalcResult result;
		public QueueItem(PagePos p, CalcResult r) {
			pos = p;
			result = r;
		}
	}

	private Map<PagePos, ImageView> imageViewMap;
	private Queue<QueueItem> showQueue;
	private float limitRatioForSolo;
	private float shorterRatioOfWidth;
	private String blankExtension;
	private boolean showSolo;
	
	public ShowTwin(Book b, List<String> l, RawScreenSize r, ImageView vLeft, ImageView vRight, ImageView vCenter, float limit, float shorter, String be) {
		super(b, l, RawScreenSize.getHalfWidth(r));
		imageViewMap = new HashMap<PagePos, ImageView>();
		if(book.isR2L()) {
			imageViewMap.put(PagePos.Even, vRight);
			imageViewMap.put(PagePos.Odd, vLeft);
		} else {
			imageViewMap.put(PagePos.Even, vLeft);
			imageViewMap.put(PagePos.Odd, vRight);
		}
		imageViewMap.put(PagePos.Center, vCenter);
		showQueue = new LinkedList<QueueItem>();
		limitRatioForSolo = limit;
		shorterRatioOfWidth = shorter;
		blankExtension = be;
		showSolo = false;
	}
	
	private void setQueue() {
		showSolo = false;
		String ppath = getCurrentPpath();
		CalcResult presult = new CalcResult(rawSize, ppath);
		boolean shouldBeSolo = isImageShouldBeSolo(presult, limitRatioForSolo);
		if(shouldBeSolo) {
			showQueue.offer(new QueueItem(PagePos.Center, presult));
			showSolo = true;
		} else {
			PagePos pos = getCurrentPagePos();
			showQueue.offer(new QueueItem(pos, presult));
			
			String opath = getPname(getOpositePageIdx());
			CalcResult oresult = new CalcResult(rawSize, opath);
			showQueue.offer(new QueueItem(getOpositePagePos(), oresult));
		}
	}

	@Override
	public void setFirst(int i) {
		setIdx(i);
		setQueue();
	}

	@Override
	public int getCurrentIdx() {
		return super.getCurrentIdx();
	}

	@Override
	public String getCurrentPpath() {
		return super.getCurrentPpath();
	}
	
	private boolean isPageShowLeft(PagePos pos) {
		boolean showLeft = false;
		boolean isR2L = book.isR2L();
		if(PagePos.Odd == pos && isR2L) {
			showLeft = true;
		} else if(PagePos.Even == pos && !isR2L) {
			showLeft = true;
		}
		return showLeft;
	}
	
	private void setImageViewMargin(PagePos pos, CalcResult result, ImageView v) {
		int left = 0;
		int right = 0;
		boolean shorter = isImageWidthShorter(result, shorterRatioOfWidth);
		if(shorter) {
			int marginWidth = rawSize.width / 3;;
			if(isPageShowLeft(pos)) {
				right = marginWidth;
			} else {
				left = marginWidth;
			}
		}
		ImageSize fit = result.fitSize;
		MarginLayoutParams mparams = new MarginLayoutParams(v.getLayoutParams());
		mparams.setMargins(left, 0, right, 0);
		LinearLayout.LayoutParams lparams = new LinearLayout.LayoutParams(mparams);
		lparams.width = fit.width;
		lparams.height = fit.height;
		v.setLayoutParams(lparams);
	}
	
	private class Iter implements Iterator<Pair<String, ImageView>> {
		@Override
		public boolean hasNext() {
			return !showQueue.isEmpty();
		}
		@Override
		public Pair<String, ImageView> next() {
			if(!hasNext()) {
				throw new NoSuchElementException();
			}
			QueueItem qi = showQueue.remove();
			PagePos pos = qi.pos;
			CalcResult result = qi.result;
			String path = result.path;
			if(pos != PagePos.Center && pos != getCurrentPagePos()) {
				boolean shouldBeSolo = isImageShouldBeSolo(result, limitRatioForSolo);
				if(shouldBeSolo) {
					path = blankExtension;
				} else {
					path = getPname(getOpositePageIdx());
				}
			}
			ImageView v = imageViewMap.get(pos);
			setImageViewMargin(pos, result, v);
			return Pair.create(path, v);
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
	public void turnToForward() {
		toForward();
		if(!showSolo) {
			toForward();
		}
		setQueue();
	}

	@Override
	public void turnToBackward() {
		toBackward();
		if(!showSolo) {
			toBackward();
		}
		setQueue();
	}

	@Override
	public void clearView() {
		for(PagePos p : PagePos.values()) {
			imageViewMap.get(p).setImageDrawable(null);
		}
	}
}
