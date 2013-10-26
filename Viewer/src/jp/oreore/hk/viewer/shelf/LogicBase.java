package jp.oreore.hk.viewer.shelf;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.devsmart.android.ui.HorizontalListView;

import jp.oreore.hk.file.dir.DirBook;
import jp.oreore.hk.file.json.JsonBook;
import jp.oreore.hk.iface.IBookOpener;
import jp.oreore.hk.iface.IBooksMaker;
import jp.oreore.hk.iface.ICoverShower;
import jp.oreore.hk.iface.IShelfSwitcher;
import jp.oreore.hk.image.CalcUtil;
import jp.oreore.hk.image.ImageSize;
import jp.oreore.hk.json.obj.Book;
import jp.oreore.hk.screen.LogicalScreenForBackFace;
import jp.oreore.hk.screen.RawScreenSize;
import jp.oreore.hk.task.BookInfoReader;
import jp.oreore.hk.task.ImageCache;
import jp.oreore.hk.task.ImageFetcher;
import jp.oreore.hk.viewer.R;
import jp.oreore.hk.viewer.ViewerUtil;
import jp.oreore.hk.viewer.listener.ShelfBacksGesture;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;

public abstract class LogicBase implements IBooksMaker, AdapterView.OnItemClickListener, IShelfSwitcher {
	private static final String TAG = "LogicBase";

	protected Activity activity;		// also IBookOpener
	protected List<Book> foundBooks;
	protected BooksAdapter adapter;
	protected ImageFetcher imageFetcher;
	protected BookInfoReader task;
	protected RawScreenSize rawSize;
	protected LogicalScreenForBackFace logicalScreenForBackFace;
	protected int minBackFaceWidth;
	protected ViewerUtil.ShelfViewMode viewMode;
	protected ICoverShower coverShower;
	protected int bookIndex;
	protected ShelfBacksGesture gesture;
	
	protected LogicBase(Activity act, ViewerUtil.ShelfViewMode mode, int idx) {
		activity = act;
		viewMode = mode;
		bookIndex = idx;
		minBackFaceWidth = Integer.valueOf(activity.getString(R.string.min_pixel_of_backface_width));
	}

	public void startReadBooks() {
		String jsonFname = activity.getString(R.string.fname_book_json);
		Integer maxCount = Integer.valueOf(activity.getString(R.string.max_search_count_in_shelf));
		foundBooks = Collections.synchronizedList(new LinkedList<Book>());
		
		String attrDefaultStr = activity.getString(R.string.json_default_attributes);
		String attrJsonFname = activity.getString(R.string.fname_bookattr_json);
		String libPath = getLibPath();
		JsonBook.init(attrDefaultStr, attrJsonFname, libPath);
		
		if(task != null) {
			task.setExitTasksEarly(true);
		}
		task = null;

		ImageCache.ImageCacheParams cacheParams = new ImageCache.ImageCacheParams(activity);
		cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of app memory
		
        // The ImageFetcher takes care of loading images into our ImageView children asynchronously
        imageFetcher = new ImageFetcher(activity);
        imageFetcher.addImageCache(activity.getFragmentManager(), cacheParams);
        imageFetcher.setImageFadeIn(false);

		adapter = new BooksAdapter(activity, R.layout.viewitem_bookback);
		AdapterView<ListAdapter> listview = getListView();
		listview.setAdapter(adapter);
    	listview.setOnItemClickListener(this);
    	int minMoveLen = Integer.parseInt(activity.getString(R.string.min_move_pixel_of_fling));
    	gesture = new ShelfBacksGesture(activity, this, minMoveLen);
    	listview.setOnTouchListener(gesture);
    	
    	coverShower = makeCoverShower();

		setupViewMode(viewMode);
    	
		task = new BookInfoReader(this, getShelfPath(), getSearchQuery(), jsonFname, maxCount, foundBooks);
		task.exec();
	}

	public void setExitTasksEarly(boolean exit) {
		if(task != null) {
			task.setExitTasksEarly(exit);
		}
		if(imageFetcher != null) {
			imageFetcher.setExitTasksEarly(exit);
		}
	}
	
	//
	// abstract
	//
	
	protected abstract String getLibPath();
	protected abstract String getShelfPath();
	protected abstract String getSearchQuery();
	
	//
	// adapter
	//
	
	protected static class ViewHolder {
		ImageView image;
	};
	
	protected class BooksAdapter extends ArrayAdapter<Book> {
		private LayoutInflater inflater;
		private int layout;

		public BooksAdapter(Context context, int resource) {
			super(context, resource);
			
			inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			layout = resource;
		}
		
		@Override
		public int getCount() {
			int count = 0;
			synchronized(foundBooks) {
				count = foundBooks.size();
			}
			return count;
		}
		
		private void setViewSize(View v, String imagePath) {
			ImageSize size = CalcUtil.getBackFaceSize(rawSize, logicalScreenForBackFace.ratio, imagePath);
			setViewSize(v, size);
		}
		
		private void setViewSize(View v, Drawable image) {
			ImageSize size = CalcUtil.getBackFaceSize(rawSize, logicalScreenForBackFace.ratio, image);
			setViewSize(v, size);
		}
		
		private void setViewSize(View v, ImageSize size) {
			boolean tooThin = (size.width < minBackFaceWidth);
			int iWidth = (tooThin ? minBackFaceWidth : size.width);
			LayoutParams params = v.getLayoutParams();
			if(params == null) {
				params = new RelativeLayout.LayoutParams(iWidth, rawSize.height);
			}
			params.width = iWidth;
			params.height = rawSize.height;
			v.setLayoutParams(params);
			int widthPadding = 0;
			if(tooThin) {
				widthPadding = (int)((iWidth - size.width) / 2.0f);
			}
			v.setPadding(widthPadding, 0, widthPadding, 0);
			if(v instanceof ImageView) {
				((ImageView)v).setScaleType(ImageView.ScaleType.FIT_END);
			}
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			View view = convertView;
			if (view == null) {
				view = inflater.inflate(layout, null);
				holder = new ViewHolder();
				holder.image = (ImageView)view.findViewById(R.id.imageViewBookBackFace);
				view.setTag(holder);
			} else {
				holder = (ViewHolder)view.getTag();
			}
			Book b = null;
			synchronized(foundBooks) {
				int count = foundBooks.size();
				if(position < count) {
					b = foundBooks.get(position);
				}
			}
			setupForCalcImageSize();
			DirBook dir = new DirBook(b, activity);
			holder.image.setContentDescription(b.getColophon().getTitle());
			String imagePath = dir.getBackFaceFname();
			if(TextUtils.isEmpty(imagePath)) {
				Drawable image = activity.getResources().getDrawable(R.drawable.noimage_backface);
				setViewSize(holder.image, image);
				holder.image.setImageDrawable(image);
				return view;
			}
			setViewSize(holder.image, imagePath);
			imageFetcher.loadImage(imagePath, holder.image);
			return view;
		}
	}
	
	//
	// internal method
	//
	
	protected AdapterView<ListAdapter> getListView() {
		@SuppressWarnings("unchecked")
		AdapterView<ListAdapter> listview = (AdapterView<ListAdapter>)activity.findViewById(R.id.listViewBookBacks);
		return listview;
	}
	
	protected ICoverShower makeCoverShower() {
		ICoverShower ret = null;
    	int minMoveLen = Integer.parseInt(activity.getString(R.string.min_move_pixel_of_fling));
		if(ViewerUtil.OrientationMode.Portrait == ViewerUtil.getOrientationMode(activity)) {
			ret = new CoverShowerPort(activity, imageFetcher, this, minMoveLen);
		} else {
			ret = new CoverShowerLand(activity, imageFetcher, this, minMoveLen);
		}
		return ret;
	}
	
	protected void setupViewMode(ViewerUtil.ShelfViewMode mode) {
		coverShower.clearImages();
		
		viewMode = mode;
		AdapterView<ListAdapter> listview = getListView();
    	if(ViewerUtil.ShelfViewMode.BackFace != viewMode) {
    		listview.setVisibility(View.GONE);
    		coverShower.setVisible();
    	} else {
    		coverShower.setGone();
    		listview.setVisibility(View.VISIBLE);
    	}
		if(activity instanceof IBookOpener) {
			((IBookOpener)activity).setViewMode(viewMode);
		}
	}
	
	protected void setupForCalcImageSize() {
		if(rawSize == null) {
			rawSize = new RawScreenSize(activity);
			logicalScreenForBackFace = new LogicalScreenForBackFace(activity, rawSize.height);
		}
	}
	
	protected int getIndexOfPodsition(int posx, int posy) {
		HorizontalListView listview = (HorizontalListView)activity.findViewById(R.id.listViewBookBacks);
		int idx = listview.pointToPosition(posx, posy);
		return idx;
	}
	
	protected void setBookIndex(int idx) {
		bookIndex = idx;
		if(activity instanceof IBookOpener) {
			((IBookOpener)activity).setBacksIndex(idx);
		}
	}
	
	protected int getBookCount() {
		int count = 0;
		synchronized(foundBooks) {
			count = foundBooks.size();
		}
		return count;
	}
	
	protected Book getBookAt(int idx) {
		int count = getBookCount();
		int position = (idx % count);
    	Book b = null;
		synchronized(foundBooks) {
			b = foundBooks.get(position);
		}
		return b;
	}

    // view set to cover pager
	protected void switchToCoverView(int idx) {
    	Book b = getBookAt(idx);
    	if(b == null) {
    		Log.w(TAG, "Book not found at idx=[" + idx + "].");
    		idx = 0;
    		b = getBookAt(idx);
    	}
    	setBookIndex(idx);
    	Log.d(TAG, "Switch View to Cover Mode. Selected Book is [" + b.getPath() + "].");
    	setupViewMode(ViewerUtil.ShelfViewMode.Cover);
    	setupForCalcImageSize();
    	coverShower.showCover(b, rawSize);
    }
	
	// view set to backs list
	protected void switchToBacksView() {
    	setupViewMode(ViewerUtil.ShelfViewMode.BackFace);
	}
    
	//
	// interface
	//

    // IBooksMaker
    public void notifyComplete() {
    	Log.d(TAG, "notifyComplete.");
    	
    	adapter.clear();
		synchronized(foundBooks) {
			for(Book b : foundBooks) {
				adapter.add(b);
			}
		}
		adapter.notifyDataSetChanged();
    	if(ViewerUtil.ShelfViewMode.BackFace != viewMode) {
    		setupForCalcImageSize();
    		coverShower.showCover(getBookAt(bookIndex), rawSize);
    	}
    }
    
    // AdapterView.OnItemClickListener
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    	Log.d(TAG, "onItemClick. position=" + position);
    	
    	Book b = getBookAt(position);
		if(b != null && activity instanceof IBookOpener) {
			((IBookOpener)activity).openBook(b);
		}
    }
    
    // IShelfSwitcher
    public void viewCover(int posx, int posy) {
    	int idx = getIndexOfPodsition(posx, posy);
    	if(idx < 0) {
    		Log.d(TAG, "FlingDown at Out Of BackFace.");
    		return;
    	}
    	switchToCoverView(idx);
    }
    
    // IShelfSwitcher
    public void viewBackFace() {
    	switchToBacksView();
    }

    // IShelfSwitcher
	public void viewBook() {
		if(activity instanceof IBookOpener) {
			((IBookOpener)activity).openBook(getBookAt(bookIndex));
		}
	}
    
    // IShelfSwitcher
	public void forwardCover() {
		int count = getBookCount();
		int nextIdx = bookIndex + 1;
		if(nextIdx >= count) {
			ViewerUtil.printToast(activity, "No More Book.");
			return;
		}
		setBookIndex(nextIdx);
    	coverShower.showCover(getBookAt(nextIdx), rawSize);
	}
    
    // IShelfSwitcher
	public void backwardCover() {
		int nextIdx = bookIndex - 1;
		if(nextIdx < 0) {
			ViewerUtil.printToast(activity, "No More Book.");
			return;
		}
		setBookIndex(nextIdx);
    	coverShower.showCover(getBookAt(nextIdx), rawSize);
	}

	// IShelfSwitcher
    public void viewLibrary() {
		if(activity instanceof IBookOpener) {
			((IBookOpener)activity).viewLibrary();
		}
    }
}
