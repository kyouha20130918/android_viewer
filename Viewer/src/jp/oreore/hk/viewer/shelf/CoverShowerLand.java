package jp.oreore.hk.viewer.shelf;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import jp.oreore.hk.file.dir.DirBook;
import jp.oreore.hk.iface.ICoverShower;
import jp.oreore.hk.iface.IShelfSwitcher;
import jp.oreore.hk.image.CalcUtil.CalcResult;
import jp.oreore.hk.image.ImageSize;
import jp.oreore.hk.json.obj.Book;
import jp.oreore.hk.screen.RawScreenSize;
import jp.oreore.hk.task.ImageFetcher;
import jp.oreore.hk.viewer.R;
import jp.oreore.hk.viewer.ViewerUtil;

public class CoverShowerLand extends CoverShowerBase implements ICoverShower {
	public CoverShowerLand(Activity act, ImageFetcher f, IShelfSwitcher s) {
		super(act, f, s);
		setGestureListener(getLayoutTwin());
		setGestureListener(getLayoutSolo());
	}
	
	private ImageView getLeftView() {
		return (ImageView)activity.findViewById(R.id.imageViewCoverLandLeft);
	}
	
	private ImageView getRightView() {
		return (ImageView)activity.findViewById(R.id.imageViewCoverLandRight);
	}
	
	private ImageView getCenterView() {
		return (ImageView)activity.findViewById(R.id.imageViewCoverLandCenter);
	}
	
	private LinearLayout getLayoutTwin() {
		return (LinearLayout)activity.findViewById(R.id.LinearLayoutCoverTwin);
	}
	
	private LinearLayout getLayoutSolo() {
		return (LinearLayout)activity.findViewById(R.id.LinearLayoutCoverSolo);
	}
	
	@Override
	public void clearImages() {
		ImageView ivl = getLeftView();
		if(ivl.getDrawable() != null) {
			ivl.setImageDrawable(null);
		}
		ImageView ivr = getRightView();
		if(ivr.getDrawable() != null) {
			ivr.setImageDrawable(null);
		}
		ImageView ivc = getCenterView();
		if(ivc.getDrawable() != null) {
			ivc.setImageDrawable(null);
		}
	}

	@Override
	public void setVisible() {
	}
	
	private void setVisibleTwin() {
		LinearLayout layoutSolo = getLayoutSolo();
		layoutSolo.setVisibility(View.GONE);
		LinearLayout layoutTwin = getLayoutTwin();
		layoutTwin.setVisibility(View.VISIBLE);
	}
	
	private void setVisibleSolo() {
		LinearLayout layoutTwin = getLayoutTwin();
		layoutTwin.setVisibility(View.GONE);
		LinearLayout layoutSolo = getLayoutSolo();
		layoutSolo.setVisibility(View.VISIBLE);
	}

	@Override
	public void setGone() {
		LinearLayout layoutTwin = getLayoutTwin();
		layoutTwin.setVisibility(View.GONE);
		LinearLayout layoutSolo = getLayoutSolo();
		layoutSolo.setVisibility(View.GONE);
	}
	
	private void setViewSize(ImageView v, RawScreenSize rawSize, String path) {
		CalcResult cresult = new CalcResult(rawSize, path);
		ImageSize fit = cresult.fitSize;
		LinearLayout.LayoutParams lparams = new LinearLayout.LayoutParams(v.getWidth(), v.getHeight());
		lparams.width = fit.width;
		lparams.height = fit.height;
		v.setLayoutParams(lparams);
	}

	@Override
	public void showCover(Book b, RawScreenSize rawSize) {
		clearImages();
		
		DirBook db = new DirBook(b, activity);
		String cpath = db.getCoverFrontFname();
		if(TextUtils.isEmpty(cpath)) {
			ViewerUtil.printToast(activity, "No Cover Image.");
			return;
		}
		String ipath = db.getIntrductionFname();
		if(TextUtils.isEmpty(ipath)) {
			setVisibleSolo();
			ImageView iv = getCenterView();
			imageFetcher.loadImage(cpath, iv);
			return;
		}
		setVisibleTwin();
		ImageView ivc = getLeftView();
		ImageView ivi = getRightView();
		if(!b.isR2L()) {
			ImageView tmp = ivc;
			ivc = ivi;
			ivi = tmp;
		}
		setViewSize(ivc, rawSize, cpath);
		imageFetcher.loadImage(cpath, ivc);
		setViewSize(ivi, rawSize, ipath);
		imageFetcher.loadImage(ipath, ivi);
	}

}
