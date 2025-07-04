package inne;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import grafika.Coordinates;

abstract public class ComplexCurve  extends Curve<Complex>{
		
	public static ComplexCurve create(Coordinates coords, Complex coordLD, Complex coordPG, PolyCurve<Point> polyCurve) {
		ComplexCurve ret =  new ComplexCurve() {
						
			@Override
			public Complex c(double t) {
				double r = t % 1;
				try {
					if(r == 0)
						return coords.pointToCmplx( polyCurve.getVert().get((int)t) , coordLD, coordPG);
					Point p1 = polyCurve.getVert().get((int)(t-r)), 
							p2 = polyCurve.getVert().get((int)(t-r) + 1);
					double x = p1.x*(1-r)+p2.x*r, y = p1.y*(1-r)+p2.y*r;
					return coords.pointToCmplx(x, y, coordLD, coordPG);
					/*
					Complex z1 = coords.pointToCmplx(p1, coordLD, coordPG),
							z2 = coords.pointToCmplx(p2, coordLD, coordPG);
					return Complex.add(Complex.mult(z1, new Complex(1-r)), Complex.mult(z2, r));
					*/
					
				}catch (IndexOutOfBoundsException e) {
					System.out.println(t +"  " + r + "  " + (int)Math.floor(t-r) + "  " + (int)Math.floor(t+1-r));
					throw e;
				}
			}
			@Override
			public double getm() {
				return 0;
			}

			@Override
			public double getM() {
				return polyCurve.getVert().size()-1;
			}
		};
		
		
		
		return ret;
	}

}
