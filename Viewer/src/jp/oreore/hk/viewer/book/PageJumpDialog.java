package jp.oreore.hk.viewer.book;

import jp.oreore.hk.iface.IPageTurner;
import jp.oreore.hk.viewer.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

public class PageJumpDialog extends DialogFragment {

	private IPageTurner turner;
	private SeekBar seekbar;
	private TextView pageinfo;
	
	public static PageJumpDialog newInstance(IPageTurner t) {
		PageJumpDialog dialog = new PageJumpDialog();
		dialog.turner = t;
		return dialog;
	}
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
	}
	
	private class SeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
		}
		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
		}
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			pageinfo.setText(turner.getPageInfo(progress));
		}
	}
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
    	LayoutInflater inflater = LayoutInflater.from(getActivity());
    	View dialogView = inflater.inflate(R.layout.dialog_pagejump, null);
    	
    	int curIdx = turner.getCurrentIdx();
    	pageinfo = (TextView)dialogView.findViewById(R.id.pageInfo);
		pageinfo.setText(turner.getPageInfo(curIdx));
		
    	seekbar = (SeekBar)dialogView.findViewById(R.id.pageSeekBar);
    	seekbar.setMax(turner.getPageCount() - 1);
    	seekbar.setOnSeekBarChangeListener(new SeekBarChangeListener());
    	seekbar.setProgress(curIdx);
    	seekbar.setSecondaryProgress(curIdx);
    	if(turner.isR2L()) {
    		seekbar.setRotation(180.0f);
    	}

    	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    	builder.setView(dialogView);

    	builder.setPositiveButton(getString(R.string.dialog_pagejump_button_jump), 
    			new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
				    	int idx = seekbar.getProgress();
				    	turner.turnPageDirect(idx);
					}
    			}
    	);
    	builder.setNegativeButton(getString(R.string.dialog_pagejump_button_cancel), 
    			new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
    			}
    	);

    	builder.setCancelable(true);
    	AlertDialog dialog = builder.create();
    	return dialog;
	}
}
