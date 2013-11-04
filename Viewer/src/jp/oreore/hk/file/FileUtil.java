package jp.oreore.hk.file;

import java.io.File;

import jp.oreore.hk.json.obj.Book;

public class FileUtil {

	private FileUtil() {}
	
	public static boolean isCover(String path, Book b) {
		File f = new File(path);
		String fname = f.getName();
		if(fname.startsWith(b.getAttributes().getCoverImagePrefix())) {
			return true;
		}
		if(fname.startsWith(b.getAttributes().getIntroduction())) {
			return true;
		}
		return false;
	}
	
	public static String getExcludeExtentionName(String path) {
		File f = new File(path);
		String fnm = f.getName();
		String pnm = f.getParent();
		String bnm = fnm.replaceAll("[.][^.]+$", "");
		return pnm + System.getProperty("file.separator") + bnm;
	}
	
	public static boolean existsFile(String path) {
		return (new File(path)).exists();
	}
}
