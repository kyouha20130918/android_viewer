package jp.oreore.hk.file.json;

import jp.oreore.hk.json.obj.Book;

public class JsonBook extends JsonBase<Book> {
	private String spath;

	public JsonBook(String shelfPath, String fnm) {
		super(shelfPath + fnm);
		spath = shelfPath;
	}

	@Override
	Book defaultValues() {
		Book b = Book.getEmptyInstance();
		b.setPath(spath);
		return b;
	}

	@Override
	Book makeValues(String s) {
		Book ret = new Book(s);
		ret.setPath(spath);
		return ret;
	}

	@Override
	public boolean write(Book t) {
		throw new IllegalStateException("not allowed writing of BookJson.");
	}
}
