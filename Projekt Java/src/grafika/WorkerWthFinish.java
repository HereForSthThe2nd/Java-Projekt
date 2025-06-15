package grafika;

import javax.swing.SwingWorker;

abstract public class WorkerWthFinish<T,V> extends SwingWorker<T,V>{
	abstract public void finish();
	Runnable executeWhenDone;
}
