package grafika;

import javax.swing.JPanel;

public class CoordPanel extends JPanel implements Cloneable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9099591880036693915L;
	public Coordinates coords;
	
	public CoordPanel() {};
	
	public CoordPanel(Coordinates coords) {
		this.coords = coords;
	}
	
}
