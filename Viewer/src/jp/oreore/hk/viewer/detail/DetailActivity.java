package jp.oreore.hk.viewer.detail;

import jp.oreore.hk.file.json.JsonNote;
import jp.oreore.hk.iface.IMarkEditor;
import jp.oreore.hk.json.obj.Book;
import jp.oreore.hk.json.obj.Library;
import jp.oreore.hk.json.obj.Mark;
import jp.oreore.hk.json.obj.Note;
import jp.oreore.hk.types.PageType;
import jp.oreore.hk.viewer.R;
import jp.oreore.hk.viewer.adapter.MarksAdapter;
import jp.oreore.hk.viewer.book.BookActivity;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.TextView;

public class DetailActivity extends Activity implements OnClickListener, IMarkEditor {
	private static final String TAG = "DetailActivity";

	// for intent bundle
	public static final String IKEY_BUNDLE = "bundle";
	// for intent bundle key
	public static final String IKEY_LIBRARY_PATH = "libpath";
	public static final String IKEY_JSON_LIBRARY = "library";
	public static final String IKEY_JSON_BOOK = "book";
	public static final String IKEY_JSON_PAGENAME = "pagename";
	// for bundle of local
	private static final String KEY_LIBRARY_PATH = "libpath";
	private static final String KEY_JSON_LIBRARY = "library";
	private static final String KEY_JSON_BOOK = "book";
	private static final String KEY_JSON_PAGENAME = "pagename";

	private String libPath;
	private Library currentPosition;
	private Book currentBook;
	private String currentPageName;
	private Note currentNote;
	private MarksAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate Start.");
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_detail);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);

		if(handleIntent(getIntent())) {
			backToBook();
			finish();
			return;
		}
		
		setViewValue();
	}

	@Override
	protected void onDestroy() {
		Log.d(TAG, "onDestroy Start.");
	    super.onDestroy();
	}
	
	//
	// for menu
	//
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.d(TAG, "onCreateOptionsMenu Start.");
		
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.detail, menu);
		
		return true;
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
        	backToBook();
        	finish();
            return true;
        case R.id.action_toBook:
        	backToBook();
        	finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //
    // call activity
    //

    private void backToBook() {
		Log.d(TAG, "Back to Book.");
	    
	    writeCurrentNote();
		
        Bundle appData = new Bundle();
        appData.putString(BookActivity.IKEY_LIBRARY_PATH, libPath);
        currentPosition.setPage(PageType.Book);
        appData.putString(BookActivity.IKEY_JSON_LIBRARY, currentPosition.toString());
        Intent intent = new Intent(this, BookActivity.class);
    	intent.setAction(Intent.ACTION_VIEW);
    	intent.putExtra(BookActivity.IKEY_BUNDLE, appData);
		startActivity(intent);
    }

    //
    // handle intent
    //
    
    // return true if need finish
    private boolean handleIntent(Intent intent) {
    	String act = intent.getAction();
    	Bundle initData = new Bundle();
    	
    	if(Intent.ACTION_VIEW.equals(act)) {
    		Log.d(TAG, "Handle Intent.ACTION_VIEW.");
    		Bundle appData = intent.getBundleExtra(IKEY_BUNDLE);
            initData.putString(KEY_LIBRARY_PATH, appData.getString(IKEY_LIBRARY_PATH));
            initData.putString(KEY_JSON_LIBRARY, appData.getString(IKEY_JSON_LIBRARY));
            initData.putString(KEY_JSON_BOOK, appData.getString(IKEY_JSON_BOOK));
            initData.putString(KEY_JSON_PAGENAME, appData.getString(IKEY_JSON_PAGENAME));
    	} else {
    		Log.e(TAG, "Recieve NoMean Intent[" + act + "].");
    		backToBook();
    		return true;
    	}
    	return init(initData);
    }
    
    //
    // internal methods
    //
    
    // return true if need backToBook and finish
    private boolean init(Bundle initData) {
    	setCurrentInfo(initData);
    	String fnote = currentPosition.getBookPath() + getString(R.string.fname_booknote_json);
    	JsonNote jNote = new JsonNote(fnote);
    	currentNote = jNote.read();
    	if(TextUtils.isEmpty(currentNote.getId())) {
    		currentNote.setId(currentBook.getColophon().getId());
    	}
    	return false;
    }
    
    // make library info from bundle
    private void setCurrentInfo(Bundle initData) {
		libPath = initData.getString(KEY_LIBRARY_PATH);
		String libJsonStr = initData.getString(KEY_JSON_LIBRARY);
		currentPosition = new Library(libJsonStr);
		String bookJsonStr = initData.getString(KEY_JSON_BOOK);
		currentBook = new Book(bookJsonStr);
		currentPageName = initData.getString(KEY_JSON_PAGENAME);
    }

    private void setViewValue() {
    	TextView title = (TextView)findViewById(R.id.textValueTitle);
    	title.setText(currentBook.getColophon().getTitle());
    	TextView author = (TextView)findViewById(R.id.textValueAuthor);
    	author.setText(currentBook.getColophon().getAuthor());
    	TextView publisher = (TextView)findViewById(R.id.textValuePublisher);
    	publisher.setText(currentBook.getColophon().getPublisher());
    	TextView pubdate = (TextView)findViewById(R.id.textValuePublishedDate);
    	pubdate.setText(currentBook.getColophon().getPubdate());

    	TextView pagename = (TextView)findViewById(R.id.textValuePageName);
    	String pnm = currentPageName;
    	String blankExtension = getString(R.string.blank_page_extension);
    	String blankPageName = getString(R.string.blank_page_name);
    	boolean isBlank = false;
    	if(pnm.endsWith(blankExtension)) {
    		pnm = blankPageName;
    		isBlank = true;
    	}
    	pagename.setText(pnm);
    	Button btn = (Button)findViewById(R.id.buttonMark);
    	Mark cur = currentNote.getMark(currentPageName);
    	if(cur != null || isBlank) {
        	EditText comment = (EditText)findViewById(R.id.textValueMarkComment);
        	if(cur != null) {
            	comment.setText(cur.getComment());
        	}
        	comment.setEnabled(false);
        	btn.setEnabled(false);
    	} else {
    		btn.setOnClickListener(this);
    	}
    	
    	@SuppressWarnings("unchecked")
		AdapterView<ListAdapter> marks = (AdapterView<ListAdapter>)findViewById(R.id.listViewMarksDetail);
    	adapter = new MarksAdapter(this, R.layout.viewitem_mark, currentNote, true, this);
    	marks.setAdapter(adapter);
    }
    
    private void addMarkAndDisable(View v) {
    	EditText comment = (EditText)findViewById(R.id.textValueMarkComment);
    	String cmnt = ((SpannableStringBuilder)comment.getText()).toString();
    	Mark m = Mark.getNewInstanceOfPermanent(currentPageName, cmnt);
    	currentNote.addMark(m);
    	Log.d(TAG, "add mark. pnm=" + currentPageName + ", comment=" + cmnt);
    	
    	v.setEnabled(false);
    	comment.setEnabled(false);

    	adapter.reloadMark();
    	adapter.notifyDataSetChanged();
    }
    
    // write booknote.json
    private void writeCurrentNote() {
    	String fnm = currentPosition.getBookPath() + getString(R.string.fname_booknote_json);
    	JsonNote f = new JsonNote(fnm);
    	if(!f.write(currentNote)) {
    		String msg = "write failed.[" + fnm + "]";
    		Log.e(TAG, msg);
    	}
    }
    
    //
    // interface
    //
    
    // onClickLitener
    public void onClick(View v) {
    	int id = v.getId();
    	if(id == R.id.buttonMark) {
    		addMarkAndDisable(v);
    	}
    }

    // IMarkEditor
    public void setEditable() {
    	Mark cur = currentNote.getMark(currentPageName);
    	if(cur == null) {
        	EditText comment = (EditText)findViewById(R.id.textValueMarkComment);
        	comment.setEnabled(true);
        	Button btn = (Button)findViewById(R.id.buttonMark);
        	btn.setEnabled(true);
    	}
    }
}
