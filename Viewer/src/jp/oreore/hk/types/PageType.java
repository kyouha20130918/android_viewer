package jp.oreore.hk.types;

public enum PageType {
	Library("library"),
	Shelf("shelf"),
	Book("book"),
	;
	private String typ;
	private PageType(String s) {
		typ = s;
	}
	public static PageType of(String s) {
		for(PageType p : PageType.values()) {
			if(p.typ.equals(s)) {
				return p;
			}
		}
		return Library;
	}
	@Override
	public String toString() {
		return typ;
	}
}
