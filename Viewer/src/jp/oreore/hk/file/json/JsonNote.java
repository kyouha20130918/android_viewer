package jp.oreore.hk.file.json;

import jp.oreore.hk.json.obj.Note;

public class JsonNote extends JsonBase<Note> {

	public JsonNote(String fnm) {
		super(fnm);
	}
	
	@Override
	Note defaultValues() {
		return Note.getEmptyInstance();
	}
	
	@Override
	Note makeValues(String s) {
		return new Note(s);
	}
}
