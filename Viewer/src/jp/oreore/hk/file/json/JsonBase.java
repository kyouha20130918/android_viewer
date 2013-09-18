package jp.oreore.hk.file.json;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import android.util.Log;

public abstract class JsonBase<T> {
	private static final String TAG = "BaseJson";
	
	private final String path;
	
	public JsonBase(String fnm) {
		path = fnm;
	}
	
	public T read() {
		File f = new File(path);
		if(!f.canRead()) {
			return defaultValues();
		}
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
			StringBuilder bf = new StringBuilder();
			String line = "";
			while((line = br.readLine()) != null) {
				bf.append(line);
			}
			Log.d(TAG, "read done.[" + f.getAbsolutePath() + "]");
			if(bf.length() == 0) {
				Log.w(TAG, "size 0, so set default.");
				return defaultValues();
			}
			return makeValues(bf.toString());
		} catch (Exception e) {
			Log.e(TAG, "read failed.[" + f.getAbsolutePath() + "]", e);
			return defaultValues();
		} finally {
			if(br != null) {
				try {
					br.close();
				} catch (IOException e) {
					Log.e(TAG, "close failed.[" + f.getAbsolutePath() + "]", e);
				}
			}
		}
	}
	
	abstract T defaultValues();
	abstract T makeValues(String s);
	
	public boolean write(T t) {
		File f = new File(path);
		if(t == null) {
			Log.w(TAG, "NULL.[" + f.getAbsolutePath() + "]");
			return false;
		}
		if(!f.canWrite()) {
			Log.w(TAG, "not writable.[" + f.getAbsolutePath() + "]");
			return false;
		}
		if(f.exists()) {
			if(!f.delete()) {
				Log.w(TAG, "delete failed.[" + f.getAbsolutePath() + "]");
				return false;
			}
		}
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f)));
			bw.write(t.toString());
			Log.d(TAG, "write done.[" + f.getAbsolutePath() + "]");
			return true;
		} catch (Exception e) {
			Log.e(TAG, "write failed.[" + f.getAbsolutePath() + "]", e);
			return false;
		} finally {
			if(bw != null) {
				try {
					bw.close();
				} catch (IOException e) {
					Log.e(TAG, "close failed.[" + f.getAbsolutePath() + "]", e);
				}
			}
		}
	}
}
