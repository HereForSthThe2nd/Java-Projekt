package grafika;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.SwingWorker;
import javax.swing.Timer;

import inne.Runn;

//zakłada że nawet jak metoda ma parametry, to w danym momencie wywołania te parametry zawsze będą takie same, i dlatego nie ma znaczenia którą funkcję się przepuści, a którą nie
public class InstanceManager<E> {
	static int total = 0;
	private LinkedList<Integer> instances = new LinkedList<Integer>();
	Timer t;
	Long lastExec = null;
	private long lastAdded;
	private int delay;
	
	private LinkedList<Integer> runAferKeys = new LinkedList<Integer>();
	private long minWait;
	private long lastRun = System.currentTimeMillis();
	private Runn<E> run;
	private Runnable afterExecution;
	SwingWorker<Void, Void> worker;
	
	private int longestWait;
	private Runnable afterExecutionSlow;
	public boolean currentlyWaiting;
	
	public InstanceManager(int minimumWait){
		currentlyWaiting = false;
		this.minWait = minimumWait;
	};
	
	public InstanceManager(Runn<E> r, int minWait) {
		currentlyWaiting = false;
		this.run = r;
		this.minWait = minWait;
	}
	
	public void setRunnable(Runn<E> r) {
		this.run = r;
	}
	
	public void whenDone(Runnable r) {
		this.afterExecution = r;
	}
	
	public void run() {
		System.out.println("run");
		worker = new SwingWorker<Void, Void>(){
			@Override
			protected Void doInBackground() throws Exception {
				int key = addInstance();
				run.run(key);
				return null;
			}
			
			@Override
			protected void done() {
				if(afterExecution != null)
					afterExecution.run();
				if(doSlowAfter) {
					doSlowAfter = false;
					InstanceManager.this.doneInn();
				}
			}
		};
		if(currentlyWaiting)
			return;
		long now = System.currentTimeMillis();
		if(now - lastRun > minWait) {
			worker.execute();
			lastRun = now;
		}
		else {
			currentlyWaiting = true;
			Timer t = new Timer((int)(minWait - (System.currentTimeMillis() - lastRun)), new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					worker.execute();
					currentlyWaiting = false;
					lastRun = System.currentTimeMillis();
				}
			});
			t.setRepeats(false);
			t.start();
		}
	}
	
	private synchronized int addInstance(){
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
	
	public void executeAfterDone(int delay, int longestWait, Runnable run) {
		this.afterExecutionSlow = run;
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
		if(key == -1)
			return true;
		for(Integer inst : instances) {
			if(inst.equals(key)) {
				return true;
			}
		}
		return false;
	}

	static public void executeSparingly(Runnable r) {
		
	}
	
	private boolean doSlowAfter = false;
	public void doSlowAfter() {
		doSlowAfter = true;
	}
	
	private void doneInn() {
		Timer t = new Timer(delay, new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(System.currentTimeMillis() - lastAdded >= delay || ( lastExec != null && System.currentTimeMillis() - lastExec >= longestWait )) {
					if(afterExecutionSlow != null) {
						afterExecutionSlow.run();
						lastExec = System.currentTimeMillis();
					}
				}
			}
		});
		t.setRepeats(false);
		t.start();
	}
}
