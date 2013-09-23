package jp.oreore.hk.image;

import android.graphics.BitmapFactory;
import jp.oreore.hk.screen.RawScreenSize;

public class CalcUtil {

	private CalcUtil() {}
	
	private enum Expand {
		Yes,
		No
	};
	
	public static ImageSize getFitSize(RawScreenSize rawSize, String imagePath, Expand expand) {
		ImageSize size = new ImageSize();
		ImageSize orgSize = getOriginalSize(imagePath);
		
		float wRatio = 1.0f * rawSize.width / orgSize.width;
		float hRatio = 1.0f * rawSize.height / orgSize.height;
		if(wRatio > 1.0f && hRatio >= 1.0f) {
			// small image
			if(Expand.No == expand) {
				return orgSize;
			}
			// expand
			float ratio = wRatio;
			if(wRatio < hRatio) {
				ratio = hRatio;
			}
			size.width = (int)Math.floor(1.0f * orgSize.width / ratio);
			size.height = (int)Math.floor(1.0f * orgSize.height / ratio);
			return size;
		}
		// large image
		float ratio = wRatio;
		if(wRatio > hRatio) {
			ratio = hRatio;
		}
		size.width = (int)Math.floor(1.0f * orgSize.width * ratio);
		size.height = (int)Math.floor(1.0f * orgSize.height * ratio);
		
		return size;
	}

	public static ImageSize getBackFaceSize(RawScreenSize rawSize, float ratio, String imagePath) {
		ImageSize orgSize = getOriginalSize(imagePath);
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
	
	private static ImageSize getOriginalSize(String imagePath) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);
		ImageSize size = new ImageSize();
		size.width = options.outWidth;
		size.height = options.outHeight;
		return size;
	}
}
