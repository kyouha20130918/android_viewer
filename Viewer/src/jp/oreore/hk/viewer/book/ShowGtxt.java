package jp.oreore.hk.viewer.book;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import jp.oreore.hk.file.FileUtil;
import jp.oreore.hk.image.CalcUtil;
import jp.oreore.hk.image.ImageSize;
import jp.oreore.hk.image.CalcUtil.CalcResult;
import jp.oreore.hk.json.obj.Book;
import jp.oreore.hk.screen.RawScreenSize;
import jp.oreore.hk.task.AsyncTask;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ShowGtxt extends ShowSolo {
	private static final String TAG = "ShowGtxt";
	
	private TextView textView;
	private String extname;		// include period
	private boolean isLandscape;
	private int flingLimit;

	public ShowGtxt(Book b, List<String> l, RawScreenSize r, ImageView v, String be, TextView tv, String ext, boolean land) {
		super(b, l, r, v, be);
		textView = tv;
		extname = ext;
		isLandscape = land;
		flingLimit = -1;
	}
	
	private String getTextFname(String path) {
		return FileUtil.getExcludeExtentionName(path) + extname;
	}
	
	// return flingLimit(=image height)
	private int adjustImageView(String path) {
		ImageSize bsize = CalcUtil.getOriginalSize(path);
		int rwidth = rawSize.width;
		int rheight = rawSize.height;
		
		int iwidth = rwidth;
		int iheight = (int)(1.0d * bsize.height / bsize.width * rwidth);
		if(bsize.width < bsize.height && !isLandscape) {
			iheight = rwidth;
			iwidth = (int)(1.0d * bsize.width * iheight / bsize.height);
		} else if(bsize.width >= bsize.height && isLandscape) {
			iwidth = rheight;
			iheight = (int)(1.0d * bsize.height * iwidth / bsize.width);
		} else if(bsize.width < bsize.height && isLandscape) {
			iheight = (int)(1.0d * rheight * 0.75d);
			iwidth = (int)(1.0d * bsize.width * iheight / bsize.height);
		}
		if(iheight >= (int)(1.0d * rheight * 0.75d)) {
			iheight = (int)(1.0d * rheight * 0.75d);
			iwidth = (int)(1.0d * bsize.width * iheight / bsize.height);
		}
		Log.d(TAG, "width = " + bsize.width + "->" + iwidth + ", height = " + bsize.height + "->" + iheight);
		
		ViewGroup.LayoutParams iprm = imageView.getLayoutParams();
		if(iprm == null) {
			iprm = new LinearLayout.LayoutParams(iwidth, iheight);
		} else {
			iprm.width = iwidth;
			iprm.height = iheight;
		}
		imageView.setLayoutParams(iprm);
		
		return iheight;
	}
	
	private void fitImageView(String path) {
		CalcResult presult = new CalcResult(rawSize, path);
		LinearLayout.LayoutParams iprm = new LinearLayout.LayoutParams(presult.fitSize.width, presult.fitSize.height);
		iprm.setMargins(0, 0, 0, 0);
		iprm.gravity = Gravity.CENTER;
		imageView.setLayoutParams(iprm);
	}

	protected class Iter implements Iterator<ImageInfo> {
		private int i = 1;
		@Override
		public boolean hasNext() {
			return (i > 0);
		}
		@Override
		public ImageInfo next() {
			if(!hasNext()) {
				throw new NoSuchElementException();
			}
			i --;
			
			flingLimit = -1;
			String path = getCurrentPpath();
			textView.setText("");
			if(!FileUtil.isCover(path, book)) {
				String textfnm = getTextFname(path);
				if(FileUtil.existsFile(textfnm)) {
					flingLimit = adjustImageView(path);
				}
			} else {
				fitImageView(path);
			}
			return new ImageInfo(path, imageView);
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
	public void clearView() {
		imageView.setImageDrawable(null);
	}

	@Override
	public void additionalAction(ImageInfo i) {
		String textfnm = getTextFname(i.path);
		if(!FileUtil.existsFile(textfnm)) {
			return;
		}
		TextReader task = new TextReader();
		task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, textfnm);
		Log.d(TAG, "Text Read started. [" + textfnm + "]");
	}
	
	@Override
	public int getAllowFlingLimit() {
		return flingLimit;
	}

    //
    // task
    //
    
    private class TextReader extends AsyncTask<String, Void, String> {
    	protected void onPostExecute(String result) {
    		textView.setText(result);
    		Log.d(TAG, "Text Read finished.");
    	}
		@Override
		protected String doInBackground(String... params) {
	    	StringBuilder sb = new StringBuilder();
	    	String lf = System.getProperty("line.separator");
	    	
	    	BufferedReader br = null;
	    	try {
				br = new BufferedReader(new FileReader(new File(params[0])));
				String s;
				while((s = br.readLine()) != null) {
					sb.append(s).append(lf);
				}
			} catch (Exception e) {
				Log.e(TAG, "text read failure.", e);
			} finally {
				if(br != null) {
					try {
						br.close();
					} catch (IOException e) {
						Log.e(TAG, "text close failure.", e);
					}
				}
			}
	    	
	    	return sb.toString();
		}
    }
}
