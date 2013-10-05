package jp.oreore.hk.viewer.book;

import android.util.Log;

public class CalcIndex {
	private static final String TAG = "CalcIdx";
	
	private int pagecount;
	private int showcount;

	public CalcIndex(int pcount, int scount) {
		pagecount = pcount;
		showcount = scount;
		Log.d(TAG, "pageCnt=" + pagecount + ", showcount=" + showcount + ".");
	}
	
	public int wrap(int pno) {
		if(pno < 0) {
			return pno += pagecount;
		} if(pno >= pagecount) {
			pno -= pagecount;
		}
		return pno;
	}

	public int wrapForward(int pno) {
		pno += showcount;
		return wrap(pno);
	}

	public int wrapBackward(int pno) {
		pno -= showcount;
		return wrap(pno);
	}
}
