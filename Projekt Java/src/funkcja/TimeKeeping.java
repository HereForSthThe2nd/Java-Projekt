package funkcja;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class TimeKeeping {
	//nie działa dobrze jeśli jest dużo wątków
	private static LinkedList<Object> keys = new LinkedList<Object>(List.of("BlokList", "function"));
	private static LinkedList<Long> totalTimes;
	private static LinkedList<Long> lastStart; 
	private static LinkedList<Integer> ignore;
	private static LinkedList<List<Integer>> called;
	static {
		reset();
	}
	static public void addTimeKeeping(Object key) {
		keys.add(key);
		totalTimes.add(0L);
		lastStart.add(null);
		ignore.add(0);
	}
	static public void startTimer(Object key) {
		int index = keys.indexOf(key);

	called.set(index, List.of(called.get(index).get(0)+1, called.get(index).get(1)));
		if(lastStart.get(index) == null && ignore.get(index) > 0) {
			throw new IllegalStateException(key+", " +lastStart + ", " + ignore);
		}
		if(lastStart.get(index) != null) {
			ignore.set(index, ignore.get(index)+1);
			return;
		}
		lastStart.set(index, System.currentTimeMillis());
	}
	static public void endTimer(Object key) {
		int index = keys.indexOf(key);
		called.set(index, List.of(called.get(index).get(0), called.get(index).get(1)+1));
		if(ignore.get(index) != 0) {
			ignore.set(index, ignore.get(index)-1);
			return;
		}
		long difference = System.currentTimeMillis()-lastStart.get(index);
		totalTimes.set(index, totalTimes.get(index)+difference);
		lastStart.set(index, null);
		for(int i = 0 ;i<lastStart.size();i++) {
			if(lastStart.get(i) != null)
				totalTimes.set(i, totalTimes.get(i) - difference);
		}
	}
	
	static public void reset() {
		keys = new LinkedList<Object>(List.of("BlokList", "function"));
		totalTimes = new LinkedList<Long>(List.of(0L,0L));
		lastStart = new LinkedList<Long>(Arrays.asList(null,null)); 
		ignore = new LinkedList<Integer>(List.of(0,0));
		called = new LinkedList<List<Integer>>(List.of(List.of(0,0), List.of(0,0)));

	}
	
	static public void writeAndReset() {
		for(int i=0;i<keys.size();i++) {
			System.out.println(keys.get(i) + ": " + totalTimes.get(i));
		}
		reset();
	}
	static public void writeALot() {
		System.out.println(called + ", " + lastStart);
	}
}
