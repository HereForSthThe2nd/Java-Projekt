package grafika;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

class LabelAboveFunction extends JLabel{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7070361413824931144L;
	Timer timer;
	
	public LabelAboveFunction(String text) {
		super(text);
	}
	
	@Override
	public void setText(String text) {
		if(timer != null)
			timer.stop();
		if(text.equals(getText())) {
			Thread t = new Thread() {
				
				@Override
				public void run() {
					try {
						long time = 200;
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								setFont(new Font(getFont().getName(), Font.PLAIN, getFont().getSize()));
							}
						});
						Thread.sleep(time);
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								setFont(new Font(getFont().getName(), Font.BOLD, getFont().getSize()));
							}
						});

					}catch(InterruptedException e) {
						e.printStackTrace();
					}
				};
			};
			t.run();
		}
		setForeground(Color.black);
		super.setText(text);
	}
	
	public void setColoredText(String text, Color c) {
		setForeground(c);
		if(timer != null)
			timer.stop();
		if(text.equals(getText())) {
			Thread t = new Thread() {
				
				@Override
				public void run() {
					try {
						long time = 200;
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								setFont(new Font(getFont().getName(), Font.PLAIN, getFont().getSize()));
							}
						});
						Thread.sleep(time);
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								setFont(new Font(getFont().getName(), Font.BOLD, getFont().getSize()));
							}
						});

					}catch(InterruptedException e) {
						e.printStackTrace();
					}
				};
			};
			t.run();
		}
		super.setText(text);

	}


	public void setErrorText(String text) {
		setColoredText(text, Color.RED);
	}
	
	public void setWarningText(String text) {
		setColoredText(text, Color.ORANGE);
	}
	
	
	
	public void setTextAnimated(String text){
		setForeground(Color.black);
		ActionListenerWthStop timerListener = new ActionListenerWthStop() {
			static int liczKropki = 0;
			long timeOld = System.currentTimeMillis();
			long timeNew = System.currentTimeMillis();
			@Override
			public void actionPerformed(ActionEvent e) {
					if(stop) {
						return;
					}
					String kropki;
					switch(liczKropki){
					case 0:
						kropki = ".";
						break;
					case 1:
						kropki = "..";
						break;
					case 2:
						kropki = "...";
						break;
					default:
						kropki = "";
						break;
					}
					LabelAboveFunction.super.setText(text + kropki);
					liczKropki++;
					liczKropki %= 4;
					timeNew = System.currentTimeMillis();
					timeOld = timeNew; 
			}
		};
		if(timer != null)
			timer.stop();
		timer = new Timer(800, timerListener);
		timer.start();
	}
}