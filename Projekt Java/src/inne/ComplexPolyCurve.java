package inne;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Path2D;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import funkcja.FunctionExpectedException;
import funkcja.FunctionPowloka;
import grafika.CoordPanel;
import grafika.Coordinates;

public class ComplexPolyCurve extends PolyCurve<Complex>{

	private static final double distMin = 4;
	public static int S = 1;
	public static boolean antialiasing = true;
	public static float outlineThickness = 1;
	
	
	public LinkedList<Color> colorsOfVert = new LinkedList<Color>();
	
	public ComplexPolyCurve() {
		super();
	}
	
	public void setTo(ComplexPolyCurve toCopy) {
		this.colorsOfVert = toCopy.colorsOfVert;
		this.vert = toCopy.getVert();
	}
	
	public ComplexPolyCurve(PolyCurve<Complex> from) {
		this.vert = from.vert;
	}
	
	public void drawWthOutline(Graphics2D g2d, CoordPanel panel, boolean markEnds) {
		g2d.setColor(new Color(250,250,250, 200));
		g2d.setStroke(new BasicStroke(((BasicStroke)g2d.getStroke()).getLineWidth()+outlineThickness));
		draw(g2d, panel, markEnds);
		g2d.setColor(Color.BLACK);
		g2d.setStroke(new BasicStroke(((BasicStroke)g2d.getStroke()).getLineWidth()-outlineThickness));
		draw(g2d, panel, markEnds);

	}
	
	public void draw(Graphics2D g2d, CoordPanel panel, boolean markEnds) {
		if(antialiasing)
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		
		Path2D p2d= new Path2D.Double();
		Point pLast, p1,p2,p3;
		p1 = panel.coords.cmplxToPoint(vert.get(0));
		pLast = p1;
		p2d.moveTo(p1.x, p1.y);
		int j = 1;
		for(;j<vert.size()+1-S;) {
			switch(S) {
			case 1:
				while(p1.distanceSq(pLast)<distMin && j < vert.size())
					p1 = panel.coords.cmplxToPoint(vert.get(j++));
				pLast = p1;
				p2d.lineTo(p1.x, p1.y);
				break;
			case 2:
				while(p1.distanceSq(pLast)<distMin && j < vert.size())
					p1 = panel.coords.cmplxToPoint(vert.get(j++));
				pLast = p1;
				p2 = panel.coords.cmplxToPoint(vert.get(j++));
				p2d.quadTo(p1.x, p1.y, p2.x, p2.y);
				break;
			case 3:
				while(p1.distanceSq(pLast)<distMin && j < vert.size())
					p1 = panel.coords.cmplxToPoint(vert.get(j++));
				pLast = p1;
				p2 = panel.coords.cmplxToPoint(vert.get(j++));
				p3 = panel.coords.cmplxToPoint(vert.get(j++));
				p2d.curveTo(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y);
				break;
			default:
				throw new IllegalStateException();
			}
		}
		
		switch(vert.size() - j) {
		case 0:
			break;
		case 1:
			p1 = panel.coords.cmplxToPoint(vert.get(j++));
			p2d.lineTo(p1.x, p1.y);
			break;
		case 2:
			p1 = panel.coords.cmplxToPoint(vert.get(j++));
			p2 = panel.coords.cmplxToPoint(vert.get(j++));
			p2d.quadTo(p1.x, p1.y, p2.x, p2.y);
			break;
		default:
			throw new IllegalStateException(""+j);
				
		}
		g2d.draw(p2d);
		
	}

	public ComplexPolyCurve image(FunctionPowloka f) {
		ComplexPolyCurve ret = new ComplexPolyCurve();
		for(Complex z : vert)
			try {
				ret.addPoint(f.evaluate(new Complex[] {z}));
			} catch (FunctionExpectedException e) {
				ret.addPoint(Complex.NaN);
			}
		return ret;
	}
	
	public Complex[] getBounds() {
		double xMin = vert.get(0).x, yMin=vert.get(0).y, xMax=vert.get(0).x, yMax=vert.get(0).y;
		for(Complex z : vert) {
			xMin = Math.min(xMin, z.x);
			yMin = Math.min(yMin, z.y);
			xMax = Math.max(xMax, z.x);
			yMax = Math.max(yMax, z.y);
		}
		return new Complex[] {new Complex(xMin, yMin), new Complex(xMax, yMax)};
		
	}
}
