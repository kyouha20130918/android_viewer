package jp.oreore.hk.file.json;

import java.util.Stack;

import android.text.TextUtils;
import jp.oreore.hk.file.dir.DirSimple;
import jp.oreore.hk.json.obj.Book;
import jp.oreore.hk.types.BookAttrOnPath;

public class JsonBook extends JsonBase<Book> {
	private static Book defaultAttrBook;
	private static String attrFnm;
	private static String libPath;
	
	public static void init(String defaultAttrStr, String attrFileName, String libraryPath) {
		defaultAttrBook = new Book(defaultAttrStr);
		attrFnm = attrFileName;
		libPath = libraryPath;
	}

	public JsonBook(String bookInfoPath) {
		super(bookInfoPath);
		if(defaultAttrBook == null) {
			throw new IllegalStateException("Not initialized.");
		}
	}

	@Override
	Book defaultValues() {
		Book b = Book.getEmptyInstance();
		b.setPath(getFpath());
		return b;
	}

	@Override
	Book makeValues(String s) {
		Book ret = new Book(s);
		ret.setPath(getFpath());
		return ret;
	}
	
	private void setAttrFromPath(Book bRet, String dnm) {
		for(BookAttrOnPath ba : BookAttrOnPath.values()) {
			if(ba.isMatch(dnm)) {
				bRet.getAttributes().setFromPath(ba);
			}
		}
	}
	
	private void readAndSetAttr(Book bRet) {
		Stack<String> st = new Stack<String>();
		String dirStr = getFpath();
		while(!TextUtils.isEmpty(dirStr) && !dirStr.equals(libPath)) {
			st.push(dirStr);
			DirSimple d = new DirSimple(dirStr);
			dirStr = d.getParent();
		}
		while(!st.isEmpty()) {
			dirStr = st.pop();
			String fnm = dirStr + attrFnm;
			JsonBook attr = new JsonBook(fnm);
			Book a = attr.readAttr();
			bRet.setOverride(a);
			DirSimple d = new DirSimple(dirStr);
			String dnm = d.getCurrent();
			setAttrFromPath(bRet, dnm);
		}
	}
	
	@Override
	public Book read() {
		Book bRead = super.read();
		Book bRet = defaultValues();
		bRet.setOverride(defaultAttrBook);
		readAndSetAttr(bRet);
		bRet.setOverride(bRead);
		return bRet;
	}
	
	private Book readAttr() {
		return super.read();
	}

	@Override
	public boolean write(Book t) {
		throw new IllegalStateException("not allowed writing of BookJson.");
	}
}
