package jp.oreore.hk.file.dir;

import java.io.File;

import android.text.TextUtils;

public class DirSimple {
	private final File f;
	
	public DirSimple(String dir) {
		if(TextUtils.isEmpty(dir)) {
			throw new IllegalArgumentException("empty string.");
		}
		f = new File(dir);
	}
	
	public boolean isValid() {
		return (f.isDirectory() && f.canRead());
	}
	
	public String getCurrent() {
		return f.getName();
	}
	
	public String getParent() {
		String p = f.getParent();
		if(!p.endsWith("/")) {
			p = p + "/";
		}
		return p;
	}
}
