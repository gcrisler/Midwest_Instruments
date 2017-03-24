package com.midwestinstruments.watermeter;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.HashSet;
import java.util.Objects;
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

	public void update(long offset) {
		list.update(System.currentTimeMillis() - offset);
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
		return Objects.hash(list.getList().get(position).getName());
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

		ScanData info = list.getList().get(position);

		((TextView)v.findViewById(R.id.scanItemName)).setText(info.getName());
		((TextView)v.findViewById(R.id.scanItemRssi)).setText("db: "+Integer.toString(info.getRssi()));
		//long time = System.currentTimeMillis() - list.getNodes().get(position).updateTime;
		//((TextView)v.findViewById(R.id.textView3)).setTextColor(Color.argb((int)(255*Math.exp(-time/10000.0)),0,0,0));
		//((TextView)v.findViewById(R.id.totalizer)).setTextColor(Color.argb((int)(255*Math.exp(-time/10000.0)),0,0,0));

		((TextView)v.findViewById(R.id.scanItemFlow)).setText("Flow: "+Display.formatFlowValue(info.getFlow(), info.getPipeIndex()));
		((TextView)v.findViewById(R.id.scanItemTotalizer)).setText("Total: " + Display.formatFlowValue(info.getTotalizer(), info
				.getPipeIndex()));
		((TextView)v.findViewById(R.id.scanItemResetTotalizer)).setText("Reset Total: " + Display.formatFlowValue(info
				.getResettableTotalizer(), info.getPipeIndex()));

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
