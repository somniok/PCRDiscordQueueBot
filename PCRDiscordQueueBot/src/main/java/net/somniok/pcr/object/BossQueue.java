package net.somniok.pcr.object;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import net.somniok.pcr.main.Bot;
import net.somniok.pcr.util.PcrUtil;

public class BossQueue {
	protected List<String> queue;

	public BossQueue() {
		queue = Collections.synchronizedList(new ArrayList<String>());
	}

	public BossQueue(BossQueue oneHit, BossQueue normal) {
		
	}
	
	public void clear() {
		queue.clear();
	}

	public boolean isEmpty() {
		return queue.isEmpty();
	}

	public String get(int i) {
		return queue.get(i);
	}

//	public Stream<String> stream() {
//		return queue.stream();
//	}

	public int size() {
		return queue.size();
	}

	public boolean contains(String userId) {
		return queue.contains(userId);
	}

	public void add(String userId) {
		queue.add(userId);
	}

	public void remove(String userId) {
		queue.remove(userId);
	}
	
	public List<String> getQueue(){
		return queue;
	}
	
	public String getTagString() {
		return queue.stream().map(PcrUtil::userIdToTag).collect(Collectors.joining(" "));
	}
}
