package jp.oreore.hk.viewer.shelf;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import jp.oreore.hk.file.dir.DirBook;
import jp.oreore.hk.iface.ICoverShower;
import jp.oreore.hk.iface.IShelfSwitcher;
import jp.oreore.hk.json.obj.Book;
import jp.oreore.hk.screen.RawScreenSize;
import jp.oreore.hk.task.ImageFetcher;
import jp.oreore.hk.viewer.R;
import jp.oreore.hk.viewer.ViewerUtil;

public class CoverShowerPort extends CoverShowerBase implements ICoverShower {

	public CoverShowerPort(Activity act, ImageFetcher f, IShelfSwitcher s) {
		super(act, f, s);
		setGestureListener(getLayout());
	}
	
	private ImageView getView() {
		return (ImageView)activity.findViewById(R.id.imageViewCoverPortrait);
	}
	
	private LinearLayout getLayout() {
		return (LinearLayout)activity.findViewById(R.id.LinearLayoutCover);
	}
	
	@Override
	public void clearImages() {
		ImageView iv = getView();
		if(iv.getDrawable() != null) {
			iv.setImageDrawable(null);
		}
	}
	
	@Override
	public void setVisible() {
		LinearLayout layout = getLayout();
		layout.setVisibility(View.VISIBLE);
	}

	@Override
	public void setGone() {
		LinearLayout layout = getLayout();
		layout.setVisibility(View.GONE);
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
		ImageView ivc = getView();
		imageFetcher.loadImage(cpath, ivc);
	}
}
