package jp.oreore.hk.file.dir;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
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
	
	private File[] selectFile(File f, PlainFileFilter filter) {
		File[] flist = f.listFiles(filter);
		return flist;
	}
	
	public File[] select(PlainFileFilter filter) {
		File f = new File(path);
		return selectFile(f, filter);
	}
	
	static class OnlyDirFilter extends DirFilter {
		public boolean check(File f) {
			return true;
		}
	}
	
	static class FileSorter implements Comparator<File> {
		@Override
		public int compare(File s, File t) {
			return s.getAbsolutePath().compareTo(t.getAbsolutePath());
		}
	}

	private void selectDirRecursively(File current, List<File> ret, DirFilter filter, int maxCnt, ITaskStatusChecker checker) {
		if(checker.shouldBeBreak()) {
			return;
		}
		File[] list = current.listFiles(new OnlyDirFilter());
		for(int i = 0; i < list.length; i ++) {
			if(checker.shouldBeBreak()) {
				return;
			}
			File f = list[i];
			if(filter.check(f)) {
				ret.add(f);
				if(ret.size() >= maxCnt) {
					break;
				}
				continue;
			}
			selectDirRecursively(f, ret, filter, maxCnt, checker);
			if(ret.size() >= maxCnt) {
				break;
			}
		}
	}
	
	public List<File> selectDirDeeply(DirFilter filter, int maxCnt, ITaskStatusChecker checker) {
		File f = new File(path);
		List<File> ret = new ArrayList<File>(maxCnt);
		selectDirRecursively(f, ret, filter, maxCnt, checker);
		if(checker.shouldBeBreak()) {
			ret.clear();
		}
		return ret;
	}
	
	private void selectFileRecursively(File current, List<File> ret, PlainFileFilter filter, ITaskStatusChecker checker) {
		if(checker != null && checker.shouldBeBreak()) {
			return;
		}
		File[] list = current.listFiles(new OnlyDirFilter());
		for(int i = 0; i < list.length; i ++) {
			if(checker != null && checker.shouldBeBreak()) {
				return;
			}
			File f = list[i];
			ret.addAll(Arrays.asList(selectFile(f, filter)));
			selectFileRecursively(f, ret, filter, checker);
		}
	}
	
	public List<File> selectFileDeeply(PlainFileFilter filter, ITaskStatusChecker checker) {
		List<File> ret = new ArrayList<File>();
		ret.addAll(Arrays.asList(select(filter)));
		if(checker != null && checker.shouldBeBreak()) {
			ret.clear();
			return ret;
		}
		File f = new File(path);
		selectFileRecursively(f, ret, filter, checker);
		if(checker != null && checker.shouldBeBreak()) {
			ret.clear();
		}
		return ret;
	}
}
