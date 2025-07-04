package grafika;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import inne.Complex;
import inne.ComplexCurve;
import inne.ComplexPolyCurve;
import inne.Numbers;
import inne.PolyCurve;

public class MiniMap extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4413471168310220870L;
	final int bok = 100;
	Complex ld, pg;
	final JLabel bokL = new JLabel();
	final CoordPanel panel;
	final ComplexPolyCurve current = new ComplexPolyCurve();
	double outEdge;
	Complex srodek;
	
	public MiniMap(Coordinates coords) {
		panel = new CoordPanel() {
			/**
			 * 
			 */
			private static final long serialVersionUID = -1475057761114096370L;

			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2d = (Graphics2D)g;
				current.draw(g2d, this, false);
				//zewnętrzny kwadrat
				//int xLew = (getWidth() - bok) / 2, yGor = PAD;
				g2d.setColor(Color.black);
				g2d.setStroke(new BasicStroke(3));
				g2d.drawRect(0, 0, bok, bok);

			}
		};
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));//new BoxLayout(this, BoxLayout.Y_AXIS));
		setArea(coords);
		bokL.setAlignmentX(Component.CENTER_ALIGNMENT);
		//bokL.setHorizontalAlignment(JLabel.CENTER);
		JPanel centering = new JPanel();
		CentralLayout layout = new CentralLayout(centering);
		panel.setPreferredSize(new Dimension(bok, bok));
		//layout.setPrefferedSize(new Dimension(bok,bok));
		centering.setLayout(layout);
		layout.add(panel, CentralLayout.CENTER, 0, 0);
		add(centering, BorderLayout.CENTER);
		add(bokL, BorderLayout.SOUTH);
	}

	public void setArea(Coordinates coords) {
		Point crdLD = coords.cmplxToPoint(coords.getLD()), crdPG = coords.cmplxToPoint(coords.getPG());
		PolyCurve<Point> obCoord = new PolyCurve<Point>(new LinkedList<Point>(List.of(
				crdLD,
				new Point(crdPG.x, crdLD.y),
				crdPG,
				new Point(crdLD.x, crdPG.y),
				crdLD
				)));
		current.setTo( new ComplexPolyCurve ( ComplexCurve.create(coords, coords.getLD(), coords.getPG(), 
				obCoord).toPoly(coords.accOfSmallerAr()) ) );
		Complex[] bounds = current.getBounds();
		outEdge = Numbers.greaterWholeOf(Math.max(bounds[1].x-bounds[0].x, bounds[1].y - bounds[0].y) * 1.2);
		srodek = Complex.mult(new Complex(0.5), Complex.add(bounds[0], bounds[1]));
		panel.coords = CoordsFactory.rect(Complex.subt(srodek, new Complex(outEdge/2, outEdge/2)), Complex.add(srodek, new Complex(outEdge/2, outEdge/2)), bok, bok);
		
		bokL.setText(Numbers.toStr(outEdge, 2, 1));
	}	
}
