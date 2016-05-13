package com.midwestinstruments.watermeter;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by byronh on 5/12/16.
 */
public class ScanListAdapter implements ListAdapter {

	private final ExpireList<ScanData> list = new ExpireList<>();
	private final Set<DataSetObserver> observers = new HashSet<>();

	private Context mContext;

	public ScanListAdapter(Context c) {
		mContext = c;
	}

	public void add(ScanData item) {
		list.add(item, System.currentTimeMillis());
		notifyObservers();
	}

	private void notifyObservers() {
		for(DataSetObserver observer:observers) {
			observer.onChanged();
		}
	}

	@Override
	public boolean areAllItemsEnabled() {
		return true;
	}

	@Override
	public boolean isEnabled(int position) {
		return true;
	}

	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		observers.add(observer);
	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		observers.remove(observer);
	}

	@Override
	public int getCount() {
		return list.getList().size();
	}

	@Override
	public Object getItem(int position) {
		return list.getList().get(position);
	}

	@Override
	public long getItemId(int position) {
		return list.getList().get(position).getName().hashCode();
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if(v == null) {
			LayoutInflater inflater = (LayoutInflater)parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.scan_list_item, parent, false);
		}

		((TextView)v.findViewById(R.id.textView3)).setText(list.getList().get(position).getName());
		((TextView)v.findViewById(R.id.textView2)).setText(Integer.toString(list.getList().get(position).getRssi()));
		return v;
	}

	@Override
	public int getItemViewType(int position) {
		return 0;
	}

	@Override
	public int getViewTypeCount() {
		return 1;
	}

	@Override
	public boolean isEmpty() {
		return list.getList().isEmpty();
	}
}
