package jp.oreore.hk.viewer.shelf;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import jp.oreore.hk.file.json.JsonBook;
import jp.oreore.hk.iface.IBookOpener;
import jp.oreore.hk.iface.IBooksMaker;
import jp.oreore.hk.image.CalcUtil;
import jp.oreore.hk.image.ImageSize;
import jp.oreore.hk.json.obj.Book;
import jp.oreore.hk.screen.LogicalScreenForBackFace;
import jp.oreore.hk.screen.RawScreenSize;
import jp.oreore.hk.task.BookInfoReader;
import jp.oreore.hk.task.ImageCache;
import jp.oreore.hk.task.ImageFetcher;
import jp.oreore.hk.viewer.R;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;

public abstract class LogicBase implements IBooksMaker, AdapterView.OnItemClickListener {
	private static final String TAG = "LogicBase";

	protected Activity activity;
	protected List<Book> foundBooks;
	protected BooksAdapter adapter;
	protected ImageFetcher imageFetcher;
	protected BookInfoReader task;
	protected RawScreenSize rawSize;
	protected LogicalScreenForBackFace logicalScreenForBackFace;
	protected int minBackFaceWidth;
	
	protected LogicBase(Activity act) {
		activity = act;
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
		@SuppressWarnings("unchecked")
		AdapterView<ListAdapter> listview = (AdapterView<ListAdapter>)activity.findViewById(R.id.listViewBookBacks);
		listview.setAdapter(adapter);
    	listview.setOnItemClickListener(this);
		
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
	
	public void closeCache() {
		if(imageFetcher != null) {
			imageFetcher.closeCache();
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
	
	static class ViewHolder {
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
			if(rawSize == null) {
				rawSize = new RawScreenSize(activity);
				logicalScreenForBackFace = new LogicalScreenForBackFace(activity, rawSize.height);
			}
			ImageSize size = CalcUtil.getBackFaceSize(rawSize, logicalScreenForBackFace.ratio, imagePath);
			boolean tooThin = (size.width < minBackFaceWidth);
			int iWidth = (tooThin ? minBackFaceWidth : size.width);
			LayoutParams params = v.getLayoutParams();
			if(params == null) {
				params = new LinearLayout.LayoutParams(iWidth, rawSize.height);
			}
			params.width = iWidth;
			params.height = rawSize.height;
			v.setLayoutParams(params);
			if(tooThin) {
				int padding = (int)((iWidth - size.width) / 2.0f);
				v.setPadding(padding, 0, padding, 0);
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
			String imagePath = b.getBackFaceFname();
			setViewSize(holder.image, imagePath);
			imageFetcher.loadImage(imagePath, holder.image);
			return view;
		}
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
    }

    // AdapterView.OnItemClickListener
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    	Log.d(TAG, "onItemClick. position=" + position);
    	
    	Book b = null;
		synchronized(foundBooks) {
			if(position < foundBooks.size()) {
				b = foundBooks.get(position);
			}
		}
		if(b != null && activity instanceof IBookOpener) {
			((IBookOpener)activity).openBook(b);
		}
    }

}
