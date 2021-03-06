package jp.oreore.hk.types;

public enum ItemType {
	Book("book"),
	Gtxt("gtxt"),
	Html("html"),
	Pdf("pdf"),
	;
	private String typ;
	private ItemType(String s) {
		typ = s;
	}
	public static ItemType of(String s) {
		for(ItemType i : ItemType.values()) {
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
