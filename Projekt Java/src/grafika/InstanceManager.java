package grafika;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.SwingWorker;
import javax.swing.Timer;

public class InstanceManager {
	static int total = 0;
	private LinkedList<Integer> instances = new LinkedList<Integer>();
	Timer t;
	Runnable afterExecution = null;
	Long lastExec = null;
	long lastAdded;
	private int delay;
	private LinkedList<Integer> runAferKeys = new LinkedList<Integer>();
	private Runnable run;
	private int longestWait;
	
	public synchronized int addInstance(){
		instances.add(total);
		lastAdded = System.currentTimeMillis();
		return total++;
	}
	
	public synchronized void stopAll() {
		instances = new LinkedList<Integer>();
	}
	
	public void executeAfter(int key) {
		runAferKeys.add(key);
	}
	
	public void executeAfter(int delay, int longestWait, Runnable run) {
		this.run = run;
		this.delay = delay;
		this.longestWait = longestWait;
	}
	
	public void keepLast(int amount) {
		synchronized(this) {
			while(instances.size() > amount)
				instances.removeFirst();
		}
	}
	
	public synchronized boolean stillActive(int key) {
		for(Integer inst : instances) {
			if(inst.equals(key)) {
				return true;
			}
		}
		return false;
	}

	public void done() {
		Timer t = new Timer(delay, new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(System.currentTimeMillis() - lastAdded >= delay || ( lastExec != null && System.currentTimeMillis() - lastExec >= longestWait )) {
					if(run != null) {
						run.run();
					}
				}
			}
		});
		t.setRepeats(false);
		t.start();
	}
}
