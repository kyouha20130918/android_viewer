package jp.oreore.hk.iface;

import jp.oreore.hk.json.obj.Library;
import android.os.Bundle;

public interface IShelfLogic {

	boolean isValidParameter(Library currentPosition);
	void startReadBooks();
	void setExitTasksEarly(boolean exit);
	void setToBundleForBackToLibrary(Bundle appData);
}
