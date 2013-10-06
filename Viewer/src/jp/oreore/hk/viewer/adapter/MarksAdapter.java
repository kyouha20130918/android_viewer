package jp.oreore.hk.viewer.adapter;

import java.util.List;

import jp.oreore.hk.iface.IMarkEditor;
import jp.oreore.hk.json.obj.Mark;
import jp.oreore.hk.json.obj.Note;
import jp.oreore.hk.viewer.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

public class MarksAdapter extends ArrayAdapter<Mark> {

	protected static class ViewHolder {
		TextView pagename;
		TextView comment;
		Button delete;
	}
	
	private LayoutInflater inflater;
	private int layout;
	private Note currentNote;
	private boolean canEdit;
	private List<Mark> marklist;
	private IMarkEditor editor;

	public MarksAdapter(Context context, int resource, Note n, boolean ce, IMarkEditor e) {
		super(context, resource);
		
		inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layout = resource;
		currentNote = n;
		canEdit = ce;
		editor = e;
		
		marklist = currentNote.getPermanentMarkList();
	}
	
	@Override
	public int getCount() {
		int count = marklist.size();
		return count;
	}
	
	private class DeleteListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			Mark m = (Mark)v.getTag();
			currentNote.removeMark(m);
			editor.setEditable();
			reloadMark();
			MarksAdapter.this.notifyDataSetChanged();
		}
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		View view = convertView;
		if (view == null) {
			view = inflater.inflate(layout, null);
			holder = new ViewHolder();
			holder.pagename = (TextView)view.findViewById(R.id.textValueItemPageName);
			holder.comment = (TextView)view.findViewById(R.id.textValueItemMarkComment);
			holder.delete = (Button)view.findViewById(R.id.buttonItemMarkDelete);
			view.setTag(holder);
		} else {
			holder = (ViewHolder)view.getTag();
		}
		int idVisible = (canEdit ? View.VISIBLE : View.GONE);
		holder.delete.setVisibility(idVisible);
		
		Mark m = marklist.get(position);
		holder.pagename.setText(m.getPageName());
		holder.comment.setText(m.getComment());
		if(canEdit) {
			holder.delete.setTag(m);
			holder.delete.setOnClickListener(new DeleteListener());
		}
		
		return view;
	}
	
	public void reloadMark() {
		marklist = currentNote.getPermanentMarkList();
	}

	public Mark getMark(int idx) {
		return marklist.get(idx);
	}
}
