package jp.oreore.hk.types;

public enum BookDirection {
	R2L("Right2Left"),
	L2R("Left2Right")
	;
	private String val;
	private BookDirection(String v) {
		val = v;
	}
	
	public static BookDirection of(String s) {
		for(BookDirection d : BookDirection.values()) {
			if(d.val.equals(s)) {
				return d;
			}
		}
		return R2L;
	}
	
	@Override
	public String toString() {
		return val;
	}
}
