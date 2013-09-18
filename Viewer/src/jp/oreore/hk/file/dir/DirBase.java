package jp.oreore.hk.file.dir;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import jp.oreore.hk.iface.ITaskStatusChecker;

public abstract class DirBase {
	//private static final String TAG = "BaseDir";
	
	protected final String path;

	DirBase(String dir) {
		path = dir;
	}
	
	public String getPath() {
		return path;
	}
	
	abstract static class DirFilter implements FileFilter {
		DirFilter() {}
		public boolean accept(File f) {
			boolean accept = (f.isDirectory() && check(f));
			return accept;
		}
		
		abstract boolean check(File f);
	}
	
	abstract static class PlainFileFilter implements FileFilter {
		PlainFileFilter() {}
		public boolean accept(File f) {
			boolean accept = (f.isFile() && check(f));
			return accept;
		}
		
		abstract boolean check(File f);
	}
	
	public File[] select(DirFilter filter) {
		File f = new File(path);
		File[] flist = f.listFiles(filter);
		return flist;
	}
	
	public File[] select(PlainFileFilter filter) {
		File f = new File(path);
		File[] flist = f.listFiles(filter);
		return flist;
	}
	
	static class OnlyDirFilter extends DirFilter {
		public boolean check(File f) {
			return true;
		}
	}
	
	private void selectRecursively(File root, List<File> ret, DirFilter filter, int maxCnt, ITaskStatusChecker checker) {
		if(checker.shouldBeBreak()) {
			return;
		}
		File[] list = root.listFiles(new OnlyDirFilter());
		for(int i = 0; i < list.length; i ++) {
			if(checker.shouldBeBreak()) {
				return;
			}
			File f = list[i];
			if(f == null) {
				continue;
			}
			if(filter.check(f)) {
				ret.add(f);
				list[i] = null;
				if(ret.size() >= maxCnt) {
					break;
				}
				continue;
			}
			selectRecursively(f, ret, filter, maxCnt, checker);
			if(ret.size() >= maxCnt) {
				break;
			}
		}
	}
	
	public List<File> selectDeeply(DirFilter filter, int maxCnt, ITaskStatusChecker checker) {
		File f = new File(path);
		List<File> ret = new ArrayList<File>(maxCnt);
		selectRecursively(f, ret, filter, maxCnt, checker);
		return ret;
	}
}
