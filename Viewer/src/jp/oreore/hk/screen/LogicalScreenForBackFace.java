package jp.oreore.hk.screen;

import jp.oreore.hk.viewer.R;
import android.app.Activity;

public class LogicalScreenForBackFace {

	public float ratio;
	
	public LogicalScreenForBackFace(Activity act, int rawHeight) {
		int logical_height_in_inchi = Integer.valueOf(act.getString(R.string.logical_max_height_of_backface_in_inchi));
		int dpi_of_backface = Integer.valueOf(act.getString(R.string.dpi_of_backface));
		ratio = 1.0f * rawHeight / (1.0f * logical_height_in_inchi * dpi_of_backface);
	}
}
