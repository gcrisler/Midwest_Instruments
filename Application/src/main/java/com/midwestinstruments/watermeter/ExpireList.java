package com.midwestinstruments.watermeter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by byronh on 5/12/16.
 */
public class ExpireList<T extends Comparable> {

	private final ArrayList<Node> nodes = new ArrayList<>();

	class Node {
		long updateTime;
		T item;
	}

	public void add(T item, long time) {
		Node match = null;
		for(Node node:nodes) {
			if(item.equals(node.item)) {
				match = node;
				break;
			}
		}
		if(match == null) {
			match = new Node();
			nodes.add(match);
		}
		match.item = item;
		match.updateTime = time;
		sort();
	}

	private void sort() {
		Collections.sort(nodes, new Comparator<Node>() {
			@Override
			public int compare(Node lhs, Node rhs) {
				return lhs.item.compareTo(rhs.item);
			}
		});
	}

	public void update(long trimTime) {
		for(int i=0; i<nodes.size(); i++) {
			if(nodes.get(i).updateTime <= trimTime) {
				nodes.remove(i);
				i--;
			}
		}
		sort();
	}

	public ArrayList<T> getList() {
		ArrayList<T> result = new ArrayList<>();
		for(Node node:nodes) {
			result.add(node.item);
		}
		return result;
	}
}
