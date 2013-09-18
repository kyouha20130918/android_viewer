package jp.oreore.hk.file.json;

import jp.oreore.hk.json.obj.Shelf;

public class JsonShelf extends JsonBase<Shelf> {
	private String fname;

	public JsonShelf(String fnm) {
		super(fnm);
		fname = fnm;
	}

	@Override
	Shelf defaultValues() {
		throw new IllegalStateException("not allowed missing of ShelfJson.");
	}

	@Override
	Shelf makeValues(String s) {
		Shelf ret = new Shelf(s);
		ret.setPath(fname.replaceFirst("/[^/]+$", "/"));
		return ret;
	}

	@Override
	public boolean write(Shelf t) {
		throw new IllegalStateException("not allowed writing of ShelfJson.");
	}
}
