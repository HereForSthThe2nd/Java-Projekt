package grafika;

import java.awt.Point;

import inne.Complex;

public class Values implements Cloneable{
	private final int RES;
	private final CoordPanel panel;
	private Complex lewyDolnyPanel;//może być inny niż ten w coords: kiedy obszar jest większy/mniejszy od obszaru w którym znane są wartości funckji
	private Complex prawyGornyPanel;//może być inny niż ten w coords: kiedy obszar jest większy/mniejszy od obszaru w którym znane są wartości funckji
	private Complex[][] values; 


	public Values(int RES, CoordPanel panel) {
		this.RES = RES;
		this.panel = panel;
		values = new Complex[RES][RES];
	}
	
	Complex getValueAt(Point p, Coordinates coords) {
		Complex z = coords.pointToCmplx(p);
		Point pOnImg = coords.cmplxToPoint(z, lewyDolnyPanel, prawyGornyPanel);
		try {
			return values[pOnImg.x * RES / panel.getWidth()][pOnImg.y * RES / panel.getHeight()];
		}catch(ArrayIndexOutOfBoundsException e) {
			return Complex.NaN;
		}

	}
	
	public Complex getValueAt(int x, int y, Coordinates coords) {
		//x oraz y to współrzędne piksela
		return getValueAt(new Point(x, y), coords);
	}

	public Complex getValueAt(Point p) {
		return getValueAt(p, panel.coords);
	}
	
	public Complex getValueAt(int x, int y) {
		//x oraz y to współrzędne piksela
		return getValueAt(new Point(x, y));
	}

	public void setValue(Point p, Complex z) {
		values[p.x][p.y]= z; 
	}
	
	public void setValue(int x, int y, Complex z) {
		values[x][y] = z;
	}
	
	public int getRES() {
		return RES;
	}
	
	public Complex getPrawyGornyPanel() {
		return prawyGornyPanel;
	}
	
	public Complex getLewyDolnyPanel() {
		return lewyDolnyPanel;
	}
	
	public void setLewyDolnyPanel(Complex lewyDolnyPanel) {
		this.lewyDolnyPanel = lewyDolnyPanel;
	}
	
	public void setPrawyGornyPanel(Complex prawyGornyPanel) {
		this.prawyGornyPanel = prawyGornyPanel;
	}
	
	@Override
	protected Values clone() {
		Values val = new Values(RES, panel); 
		val.lewyDolnyPanel = lewyDolnyPanel.clone();
		val.prawyGornyPanel = prawyGornyPanel.clone();
		val.values = values;//świadomie tego nie klonuje
		
		return val;
	}
	
	public static void main(String[] args) {
		Complex[][] arr = new Complex[3][3];
		Complex[][] arr2 = new Complex[3][];
		for(int i =0;i<3;i++) {
			arr2[i] = arr[i].clone();
		}
		arr[0][0] = new Complex(0);
		arr[0][1] = new Complex(1);
		arr[1][0] = new Complex(2);
		arr[1][1] = new Complex(3);
		System.out.println(arr[0][0] + "  " +arr[0][1] + "  " +arr[1][0] + "  " +arr[1][1] );
		System.out.println(arr2[0][0] + "  " +arr2[0][1] + "  " +arr2[1][0] + "  " +arr2[1][1] );
	}
	
}
