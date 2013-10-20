package jp.oreore.hk.file.dir;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import jp.oreore.hk.iface.ITaskStatusChecker;
import jp.oreore.hk.json.obj.Book;
import jp.oreore.hk.viewer.R;

public class DirBook extends DirBase {
	
	//
	// companion class for file-name-parts
	//
	
	private static class FnameParts {
		public String backFacePart;
		public String coverFrontPart;
		public String coverRearPart;
		public String sleeveFrontPart;
		public String sleeveRearPart;
		public String imageExtension;
		public String blankExtension;
	}
	
	/////////////////////////

	Book book;
	FnameParts fnameParts;
	
	private void setupFnameParts(Context con) {
		fnameParts = new FnameParts();
		fnameParts.backFacePart = con.getString(R.string.fixed_part_backface_fname);
		fnameParts.coverFrontPart = con.getString(R.string.fixed_part_coverfront_fname);
		fnameParts.coverRearPart = con.getString(R.string.fixed_part_coverrear_fname);
		fnameParts.sleeveFrontPart = con.getString(R.string.fixed_part_sleevefront_fname);
		fnameParts.sleeveRearPart = con.getString(R.string.fixed_part_sleeverear_fname);
		fnameParts.imageExtension = con.getString(R.string.fixed_expression_image_extension);
		fnameParts.blankExtension = con.getString(R.string.blank_page_extension);
	}
	
	public DirBook(Book b, Context con) {
		super(b.getPath());
		book = b;
		setupFnameParts(con);
	}
	
	class SingleFileFilter extends PlainFileFilter {
		String fname;
		SingleFileFilter(String fnm) {
			fname = fnm;
		}
		public boolean check(File f) {
			return f.getName().matches(fname);
		}
	}
	
	private String getSingleFname(String fname) {
		String ret = "";
		PlainFileFilter filter = new SingleFileFilter(fname);
		File[] list = select(filter);
		if(list.length > 0) {
			ret = list[0].getAbsolutePath();
		}
		return ret;
	}

	private String getSingleFnameDeeply(String fname) {
		String ret = "";
		PlainFileFilter filter = new SingleFileFilter(fname);
		List<File> list = selectFileDeeply(filter, null);
		if(list.size() > 0) {
			ret = list.get(0).getAbsolutePath();
		}
		return ret;
	}

	private List<String> getFnameDeeply(String fname, ITaskStatusChecker checker) {
		PlainFileFilter filter = new SingleFileFilter(fname);
		List<File> list = selectFileDeeply(filter, checker);
		List<String> ret = new ArrayList<String>();
		for(File f : list) {
			ret.add(f.getAbsolutePath());
		}
		Collections.sort(ret);
		return ret;
	}

	public String getBackFaceFname() {
		String fname = book.getAttributes().getBackImagePrefix() + fnameParts.backFacePart + fnameParts.imageExtension;
		return getSingleFname(fname);
	}

	public String getIntrductionFname() {
		String fname = book.getAttributes().getIntroduction() + fnameParts.imageExtension;
		if(fname.startsWith(book.getAttributes().getCoverImagePrefix())) {
			String ret = getSingleFname(fname);
			if(!TextUtils.isEmpty(ret)) {
				return ret;
			}
		}
		return getSingleFnameDeeply(fname);
	}
	
	public String getCoverFrontFname() {
		String fname = book.getAttributes().getCoverImagePrefix() + fnameParts.coverFrontPart + fnameParts.imageExtension;
		return getSingleFname(fname);
	}

	public String getCoverRearFname() {
		String fname = book.getAttributes().getCoverImagePrefix() + fnameParts.coverRearPart + fnameParts.imageExtension;
		return getSingleFname(fname);
	}

	public String getSleeveFrontFname() {
		String fname = book.getAttributes().getCoverImagePrefix() + fnameParts.sleeveFrontPart + fnameParts.imageExtension;
		return getSingleFname(fname);
	}

	public String getSleeveRearFname() {
		String fname = book.getAttributes().getCoverImagePrefix() + fnameParts.sleeveRearPart + fnameParts.imageExtension;
		return getSingleFname(fname);
	}
	
	public List<String> getPageList(ITaskStatusChecker checker) {
		List<String> list = new ArrayList<String>();
		list.add(getCoverFrontFname());
		String fnm = getSleeveFrontFname();
		if(TextUtils.isEmpty(fnm)) {
			fnm = book.getAttributes().getCoverImagePrefix() + fnameParts.sleeveFrontPart + fnameParts.blankExtension;
		}
		list.add(fnm);
		fnm = book.getAttributes().getSearchPagenoFormat() + fnameParts.imageExtension;
		List<String> plist = getFnameDeeply(fnm, checker);
		list.addAll(plist);
		fnm = getSleeveRearFname();
		String sleeveRearBlankFnm = book.getAttributes().getCoverImagePrefix() + fnameParts.sleeveRearPart + fnameParts.blankExtension;
		if((plist.size() % 2) != 0) {
			if(!TextUtils.isEmpty(fnm)) {
				list.add(sleeveRearBlankFnm);
				list.add(fnm);
			}
		} else {
			if(TextUtils.isEmpty(fnm)) {
				fnm = sleeveRearBlankFnm;
			}
			list.add(fnm);
		}
		fnm = getCoverRearFname();
		if(TextUtils.isEmpty(fnm)) {
			fnm = book.getAttributes().getCoverImagePrefix() + fnameParts.coverRearPart + fnameParts.blankExtension;
		}
		list.add(fnm);
		return list;
	}
}
