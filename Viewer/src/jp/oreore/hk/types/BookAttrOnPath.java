package jp.oreore.hk.types;

public enum BookAttrOnPath {
	L2R(".L2R"),
	gtxt(".gtxt"),
	html(".html"),
	pdf(".pdf"),
	solo(".solo"),
	all(".all"),
	;
	private String keyword;
	private BookAttrOnPath(String kw) {
		keyword = kw;
	}
	
	public boolean isMatch(String s) {
		return (s.indexOf(keyword) >= 0);
	}
}
