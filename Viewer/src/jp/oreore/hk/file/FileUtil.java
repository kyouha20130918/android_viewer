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
}
