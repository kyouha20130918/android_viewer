package jp.oreore.hk.types;

public enum ViewType {
	Book("book"),
	Html("html"),
	;
	private String typ;
	private ViewType(String s) {
		typ = s;
	}
	public static ViewType of(String s) {
		for(ViewType i : ViewType.values()) {
			if(i.typ.equals(s)) {
				return i;
			}
		}
		return Book;
	}
	@Override
	public String toString() {
		return typ;
	}
}
