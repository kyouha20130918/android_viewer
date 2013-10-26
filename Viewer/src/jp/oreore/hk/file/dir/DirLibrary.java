package jp.oreore.hk.file.dir;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import jp.oreore.hk.json.obj.Shelf;

public class DirLibrary extends DirBase {
	private final String jsonFname;
	private File[] flist;
	private boolean checked = false;

	public DirLibrary(String dir, String fn) {
		super(dir);
		jsonFname = fn;
		flist = new File[0];
	}
	
	class JsonFileFilter extends PlainFileFilter {
		public boolean check(File f) {
			String fname = f.getName();
			if(jsonFname.equals(fname)) {
				return true;
			}
			return false;
		}
	}
	
	class ShelfFilter extends DirFilter {
		public boolean check(File f) {
			File [] flist = f.listFiles(new JsonFileFilter());
			return (flist.length > 0);
		}
	}

	public boolean isValid() {
		if(!checked) {
			List<File> list = Arrays.asList(select(new ShelfFilter()));
			Collections.sort(list, new FileSorter());
			flist = list.toArray(new File[0]);
			checked = true;
		}
		return (flist != null && flist.length > 0);
	}
	
	public List<Shelf> getShelves() {
		List<Shelf> ret = new ArrayList<Shelf>();
		if(flist == null || flist.length == 0) {
			return ret;
		}
		for(File f : flist) {
			Shelf s = Shelf.getEmptyInstance();
			s.setPath(f.getAbsolutePath().replace(super.path, "") + "/");
			ret.add(s);
		}
		return ret;
	}
}
