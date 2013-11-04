package jp.oreore.hk.image;

import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.Log;
import jp.oreore.hk.screen.RawScreenSize;

public class CalcUtil {
	private static final String TAG = "CalcUtil";

	private CalcUtil() {}
	
	public enum Expand {
		Yes,
		No
	};
	
	public static class CalcResult {
		public RawScreenSize rawSize;
		public String path;
		public ImageSize orgSize;
		public ImageSize fitSize;
		public Expand expand;
		
		private void init(RawScreenSize r, String p, Expand e) {
			rawSize = r;
			path = p;
			expand = e;
			orgSize = CalcUtil.getOriginalSize(path);
			fitSize = CalcUtil.getFitSize(rawSize, path, expand);
		}
		
		public CalcResult(RawScreenSize r, String p) {
			init(r, p, Expand.Yes);
		}
		public CalcResult(RawScreenSize r, String p, Expand e) {
			init(r, p, e);
		}
	};
	
	private static ImageSize calcFitSize(ImageSize screenSize, ImageSize orgSize, Expand expand) {
		ImageSize size = new ImageSize();
		
		float wRatio = 1.0f * screenSize.width / orgSize.width;
		float hRatio = 1.0f * screenSize.height / orgSize.height;
		Log.d(TAG, "wRatio=" + wRatio + ", hRatio=" + hRatio);
		if(wRatio >= 1.0f && hRatio >= 1.0f) {
			// small image
			if(Expand.No == expand) {
				return orgSize;
			}
			// expand
			float ratio = wRatio;
			if(wRatio > hRatio) {
				ratio = hRatio;
			}
			size.width = (int)Math.floor(1.0f * orgSize.width * ratio);
			size.height = (int)Math.floor(1.0f * orgSize.height * ratio);
			Log.d(TAG, "fit width=" + size.width + ", height=" + size.height + ", ratio=" + ratio);
			return size;
		}
		// large image
		float ratio = wRatio;
		if(wRatio > hRatio) {
			ratio = hRatio;
		}
		size.width = (int)Math.floor(1.0f * orgSize.width * ratio);
		size.height = (int)Math.floor(1.0f * orgSize.height * ratio);
		Log.d(TAG, "fit width=" + size.width + ", height=" + size.height + ", ratio=" + ratio);
		
		return size;
	}
	
	private static ImageSize getFitSize(RawScreenSize rawSize, String imagePath, Expand expand) {
		ImageSize orgSize = getOriginalSize(imagePath);
		Log.d(TAG, "path=" + imagePath + ", width=" + orgSize.width + ", height=" + orgSize.height);

		ImageSize screenSize = new ImageSize();
		screenSize.width = rawSize.width;
		screenSize.height = rawSize.height;
		
		return calcFitSize(screenSize, orgSize, expand);
	}

	public static ImageSize getBackFaceSize(RawScreenSize rawSize, float ratio, String imagePath) {
		ImageSize orgSize = getOriginalSize(imagePath);
		return calcBackFaceSize(rawSize, ratio, orgSize);
	}
	
	public static ImageSize getBackFaceSize(RawScreenSize rawSize, float ratio, Drawable image) {
		ImageSize orgSize = getOriginalSize(image, rawSize);
		return calcBackFaceSize(rawSize, ratio, orgSize);
	}
	
	private static ImageSize calcBackFaceSize(RawScreenSize rawSize, float ratio, ImageSize orgSize) {
		ImageSize size = new ImageSize();
		float calcRatio = ratio;
		float hRatio = 1.0f * rawSize.height / orgSize.height;
		if(hRatio < ratio) {
			calcRatio = hRatio;
		}
		size.width = (int)Math.floor(1.0f * orgSize.width * calcRatio);
		size.height = (int)Math.floor(1.0f * orgSize.height * calcRatio);
		return size;
	}
	
	public static ImageSize getOriginalSize(String imagePath) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);
		ImageSize size = new ImageSize();
		size.width = options.outWidth;
		size.height = options.outHeight;
		return size;
	}
	
	private static ImageSize getOriginalSize(Drawable image, RawScreenSize rawSize) {
		ImageSize size = new ImageSize();
		size.width = (int)Math.floor(1.0f * image.getIntrinsicWidth() / rawSize.density);
		size.height = (int)Math.floor(1.0f * image.getIntrinsicHeight() / rawSize.density);
		return size;
	}
	
	public static boolean isImageShouldBeSolo(CalcResult result, float limitRatio) {
		ImageSize fit = result.fitSize;
		return (1.0f * fit.width / fit.height > limitRatio);
	}
	
	public static boolean isImageWidthShorter(CalcResult result, float shorterRatio) {
		ImageSize fit = result.fitSize;
		RawScreenSize raw = result.rawSize;
		return ((1.0f * fit.width / raw.width) < shorterRatio);
	}
	
	private static void adjustHeight(CalcResult result, int height) {
		Log.d(TAG, "adjust : path=" + result.path + ", width=" + result.orgSize.width + ", height=" + result.orgSize.height);

		ImageSize screenSize = new ImageSize();
		screenSize.width = result.rawSize.width;
		screenSize.height = height;
		
		ImageSize ret = calcFitSize(screenSize, result.orgSize, result.expand);
		result.fitSize.width = ret.width;
		result.fitSize.height = ret.height;
	}
	
	public static void adjustHeight(CalcResult fresult, CalcResult sresult) {
		if(fresult.fitSize.height < sresult.fitSize.height) {
			adjustHeight(sresult, fresult.fitSize.height);
		} else {
			adjustHeight(fresult, sresult.fitSize.height);
		}
	}

	public static void getFitSizeForSolo(CalcResult result) {
		ImageSize screenSize = new ImageSize();
		screenSize.width = result.rawSize.width * 2;
		screenSize.height = result.rawSize.height;
		
		ImageSize fitSize = calcFitSize(screenSize, result.orgSize, result.expand);
		result.fitSize.width = fitSize.width;
		result.fitSize.height = fitSize.height;
	}
}
