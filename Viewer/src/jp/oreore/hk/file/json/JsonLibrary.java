package jp.oreore.hk.file.json;

import jp.oreore.hk.json.obj.Library;

public class JsonLibrary extends JsonBase<Library> {
	private final String defaultJsonString;
	
	public JsonLibrary(String fnm, String defaultJson) {
		super(fnm);
		defaultJsonString = defaultJson;
	}

	@Override
	Library defaultValues() {
		return new Library(defaultJsonString);
	}

	@Override
	Library makeValues(String s) {
		return new Library(s);
	}

}
