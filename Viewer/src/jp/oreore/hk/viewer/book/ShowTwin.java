package jp.oreore.hk.viewer.book;

import static jp.oreore.hk.image.CalcUtil.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;

import android.view.ViewGroup.MarginLayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import jp.oreore.hk.file.FileUtil;
import jp.oreore.hk.iface.IPageShower;
import jp.oreore.hk.image.CalcUtil;
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
	private boolean showSolo;
	
	public ShowTwin(Book b, List<String> l, RawScreenSize r, ImageView vLeft, ImageView vRight, ImageView vCenter, float limit, float shorter, String be) {
		super(b, l, RawScreenSize.getHalfWidth(r), be);
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
		showSolo = false;
	}

	// return true if setting complete.
	private boolean setQueue() {
		showSolo = false;
		String ppath = getCurrentPpath();
		CalcResult presult = new CalcResult(rawSize, ppath);
		boolean shouldBeSolo = isImageShouldBeSolo(presult, limitRatioForSolo);
		if(shouldBeSolo) {
			CalcUtil.getFitSizeForSolo(presult);
			showQueue.offer(new QueueItem(PagePos.Center, presult));
			showSolo = true;
			return true;
		}
		PagePos pos = getCurrentPagePos();
		
		String opath = getPname(getOpositePageIdx());
		if(isBlankPage(ppath) && isBlankPage(opath)) {
			// skip blank page
			return false;
		}
		
		CalcResult oresult = new CalcResult(rawSize, opath);
		boolean oShouldBeSolo = isImageShouldBeSolo(oresult, limitRatioForSolo);
		if(isBlankPage(ppath) && oShouldBeSolo) {
			// skip blank page
			return false;
		}
		
		if(!oShouldBeSolo
				&& !isBlankPage(ppath)
				&& !isBlankPage(opath)
				&& presult.fitSize.height != oresult.fitSize.height) {
			CalcUtil.adjustHeight(presult, oresult);
		}
		
		showQueue.offer(new QueueItem(pos, presult));
		showQueue.offer(new QueueItem(getOpositePagePos(), oresult));
		return true;
	}

	@Override
	public void setFirst(int i) {
		setIdx(i);
		while(!setQueue()) {
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
		int marginWidth = 0;
		boolean shorter = isImageWidthShorter(result, shorterRatioOfWidth);
		if(shorter) {
			marginWidth = rawSize.width / 6;
		} else if(!FileUtil.isCover(result.path, book)) {
			marginWidth = 10;
		}
		if(isPageShowLeft(pos)) {
			right = marginWidth;
		} else {
			left = marginWidth;
		}
		ImageSize fit = result.fitSize;
		MarginLayoutParams mparams = new MarginLayoutParams(v.getLayoutParams());
		mparams.setMargins(left, 0, right, 0);
		LinearLayout.LayoutParams lparams = new LinearLayout.LayoutParams(mparams);
		lparams.width = fit.width;
		lparams.height = fit.height;
		v.setLayoutParams(lparams);
	}
	
	private class Iter implements Iterator<ImageInfo> {
		@Override
		public boolean hasNext() {
			return !showQueue.isEmpty();
		}
		@Override
		public ImageInfo next() {
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
					showSolo = true;
				} else {
					path = getPname(getOpositePageIdx());
				}
			}
			ImageView v = imageViewMap.get(pos);
			setImageViewMargin(pos, result, v);
			return new ImageInfo(path, v);
		}
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	};

	@Override
	public Iterator<ImageInfo> iterator() {
		return new Iter();
	}
	
	@Override
	public String getNextPpath() {
		int nidx = 2;
		if(showSolo) {
			nidx = 1;
		} else {
			PagePos pos = getCurrentPagePos();
			if(PagePos.Odd == pos) {
				nidx = 1;
			}
		}
		String ret = super.getNextPpath(nidx);
		if(isBlankPage(ret)) {
			ret = super.getNextPpath(nidx + 1);
		}
		return ret;
	}

	@Override
	public void turnToForward() {
		toForward();
		if(!showSolo) {
			toForward();
		}
		while(!setQueue()) {
			toForward();
		}
	}

	@Override
	public void turnToBackward() {
		toBackward();
		if(!showSolo) {
			toBackward();
		}
		while(!setQueue()) {
			toBackward();
		}
	}

	@Override
	public void clearView() {
		for(PagePos p : PagePos.values()) {
			imageViewMap.get(p).setImageDrawable(null);
		}
	}
	
	@Override
	public void additionalAction(ImageInfo i) {
		;
	}
	
	@Override
	public int getAllowFlingLimit() {
		return -1;
	}
}
