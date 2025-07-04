package inne;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

public class KeyBeingPressed {
	JFrame frm;
	private ArrayList<Object> keys = new ArrayList<Object>();
	private ArrayList<Boolean> isPressed = new ArrayList<Boolean>();
	
	public KeyBeingPressed(JFrame frm) {
		this.frm= frm;
	}
	
	public boolean isPressed(String key) {
		return isPressed.get(keys.indexOf(key));
	}
 	public void add(String keyStroke, String key) {
		keys.add(key);
		isPressed.add(false);
		frm.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(keyStroke.equals("CONTROL") ? "ctrl pressed CONTROL" : keyStroke), key + "pressed");
		frm.getRootPane().getActionMap().put(key + "pressed", new AbstractAction() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 2974179797231632977L;

			@Override
			public void actionPerformed(ActionEvent e) {
				isPressed.set(keys.indexOf(key), true);
			}
		});
		
		frm.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("released " + keyStroke), key + "released");
		frm.getRootPane().getActionMap().put(key + "released", new AbstractAction() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 2974179797231632977L;

			@Override
			public void actionPerformed(ActionEvent e) {
				isPressed.set(keys.indexOf(key), false);
			}
		});
	}

}
