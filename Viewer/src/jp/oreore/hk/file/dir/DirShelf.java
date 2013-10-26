package jp.oreore.hk.file.dir;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.text.TextUtils;
import jp.oreore.hk.file.json.JsonBook;
import jp.oreore.hk.iface.ITaskStatusChecker;
import jp.oreore.hk.json.obj.Book;

public class DirShelf extends DirBase {
	private String jsonFname;
	private String query;
	private int maxSearchCount;
	private ITaskStatusChecker checker;

	public DirShelf(String dir, String fnm, String q, int maxCnt, ITaskStatusChecker c) {
		super(dir);
		jsonFname = fnm;
		query = q;
		maxSearchCount = maxCnt;
		checker = c;
	}
	
	private Book readBookInfoFromFile(File f) {
		JsonBook jb = new JsonBook(f.getAbsolutePath());
		Book b = jb.read();
		return b;
	}
	
	private Book readBookInfoFromDir(File f) {
		JsonBook jb = new JsonBook(f.getAbsolutePath() + "/" + jsonFname);
		Book b = jb.read();
		return b;
	}

	class JsonFileFilter extends PlainFileFilter {
		String query;
		public JsonFileFilter(String q) {
			super();
			query = q;
		}
		private boolean match(Book b) {
			String title = b.getColophon().getTitle();
			String author = b.getColophon().getAuthor();
			if(title.indexOf(query) >= 0 || author.indexOf(query) >= 0) {
				return true;
			}
			return false;
		}
		public boolean check(File f) {
			String fname = f.getName();
			if(jsonFname.equals(fname)) {
				if(TextUtils.isEmpty(query)) {
					return true;
				}
				Book b = readBookInfoFromFile(f);
				return match(b);
			}
			return false;
		}
	}
	
	class BookFilter extends DirFilter {
		boolean check(File f) {
			File [] flist = f.listFiles(new JsonFileFilter(query));
			return (flist.length > 0);
		}
	}
	
	public List<Book> getBooks() {
		List<File> list = selectDirDeeply(new BookFilter(), maxSearchCount, checker);
		Collections.sort(list, new FileSorter());
		List<Book> ret = new ArrayList<Book>(maxSearchCount);
		if(checker.shouldBeBreak()) {
			return ret;
		}
		for(File f : list) {
			if(checker.shouldBeBreak()) {
				return ret;
			}
			Book b = readBookInfoFromDir(f);
			ret.add(b);
		}
		return ret;
	}
}
