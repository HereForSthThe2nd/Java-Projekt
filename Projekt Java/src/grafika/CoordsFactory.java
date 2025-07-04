package grafika;

import java.awt.Point;

import inne.Complex;

public class CoordsFactory {
	
	static public int accOfSmaller= 200;
	
	static public Coordinates rect(Complex ld, Complex pg, int width, int height) {
		Coordinates ret = new Coordinates() {
			@Override
			public Complex pointToCmplx(double x, double y, Complex argLewDolny, Complex argPrawGorny) {
				Complex diag = Complex.subt(argPrawGorny, argLewDolny);
				Complex ret = Complex.add(argLewDolny, new Complex(	diag.x * x/width, diag.y * (height-y)/height));
				//System.out.println(p + "  " + ret.print(2) + " " + prawyGorny.print(2) + " " + lewyDolny.print(2) + " " + width);
				return ret;
			}
			
			@Override
			public Point cmplxToPoint(Complex z, Complex argLewDolny, Complex argPrawGorny) {
				Complex diag = Complex.subt(argPrawGorny, argLewDolny);
				Complex zWzgl = Complex.subt(z, argLewDolny);
				Point ret = new Point((int)(width*zWzgl.x/diag.x), (int)(height - height*zWzgl.y/diag.y));
				return ret;
			}

			@Override
			public Complex[] powiekszenie(Point p, Complex lewyDolny, Complex prawyGorny, double alpha) {
				Complex z0 = pointToCmplx(p);
				Complex[] ret = new Complex[] {Complex.add(z0, Complex.mult(new Complex(alpha), Complex.subt(getLD(), z0))), 
						Complex.add(z0, Complex.mult(new Complex(alpha), Complex.subt(getPG(), z0)))};
				return ret;
			}

			@Override
			public int accOfSmallerAr() {
				return 4;
			}

			@Override
			public Complex[] przesuniecie(Complex z, Point p, Complex lewyDolny, Complex prawyGorny) {
				Complex dz = Complex.subt(pointToCmplx(p), z);
				return new Complex[] {Complex.subt(lewyDolny, dz), Complex.subt(prawyGorny, dz)};
			}
		};
		ret.setLD(ld);
		ret.setPG(pg);
		return ret;
 
	}
	
	static public Coordinates aroundInf(Complex lewyDolny, Complex prawyGorny, int width, int height) {
		//tworzy koordynaty w których środku znajduje się nieskończoność
		Coordinates ret = new Coordinates() {
			@Override
			public Complex pointToCmplx(double x, double y, Complex argLewDol, Complex argPrawGorny) {
				Complex a = Complex.mult(new Complex(1.0/2,1.0/2), Complex.subt(argPrawGorny, argLewDol));
				Complex b = Complex.mult(new Complex(1.0/2), Complex.add(argLewDol, argPrawGorny));
				Complex zeta = rect(new Complex(-1,-1), new Complex(1,1), width, height).pointToCmplx(x, y);
				return Complex.div(Complex.add(a, Complex.mult(b, zeta)), zeta);
			}
			
			@Override
			public Point cmplxToPoint(Complex z, Complex argLewDol, Complex argPrawGorny) {
				Complex a = Complex.mult(new Complex(1.0/2,1.0/2), Complex.subt(argPrawGorny, argLewDol));
				Complex b = Complex.mult(new Complex(1.0/2), Complex.add(argLewDol, argPrawGorny));
				Complex zeta = Complex.div(a, Complex.subt(z, b));
				return rect(new Complex(-1,-1), new Complex(1,1), width, height).cmplxToPoint(zeta);
			}
			
			@Override
			public Complex[] powiekszenie(Point p, Complex lewyDolny, Complex prawyGorny, double alpha) {
				Complex a = Complex.mult(new Complex(1.0/2,1.0/2), Complex.subt(prawyGorny, lewyDolny));
				Complex b = Complex.mult(new Complex(1.0/2), Complex.add(lewyDolny, prawyGorny));
				Complex z = pointToCmplx(p);
				Complex zeta = Complex.div(a, Complex.subt(z, b));
				Complex aNow = Complex.mult(new Complex(alpha), a);
				Complex bNow = Complex.add(Complex.div(Complex.mult(a, new Complex(1-alpha)), zeta), b);
				return new Complex[] {Complex.add(Complex.div(aNow, new Complex(1,1)), bNow), Complex.subt(bNow, Complex.div(aNow, new Complex(1,1)))};
			}

			@Override
			public int accOfSmallerAr() {
				return accOfSmaller;
			}

			@Override
			public Complex[] przesuniecie(Complex z, Point p, Complex lewyDolny, Complex prawyGorny) {
				Coordinates zetaCoords = rect(new Complex(-1,-1), new Complex(1,1), width, height);
				Complex zeta = zetaCoords.pointToCmplx(p);
				Complex a = Complex.mult(new Complex(1.0/2,1.0/2), Complex.subt(prawyGorny, lewyDolny));//a = aNow
				Complex bNow = Complex.subt(z, Complex.div(a, zeta));
				return new Complex[] {Complex.subt(bNow, Complex.div(a, new Complex(1,1))), Complex.add(Complex.div(a, new Complex(1,1)), bNow)};
			}

		};
		ret.setLD(lewyDolny);
		ret.setPG(prawyGorny);
		return ret;

	}
	
	static public Coordinates  logarithmic(Complex lewyDolny, Complex prawyGorny, int width, int height) {
		Coordinates ret = new Coordinates() {
			@Override
			public Complex pointToCmplx(double x, double y, Complex lewyDolny, Complex prawyGorny) {
				Complex aKsi = Complex.mult(lewyDolny, new Complex ((Math.exp(lewyDolny.mod())-1) / lewyDolny.mod()));
				Complex bKsi = Complex.mult(prawyGorny, new Complex ((Math.exp(prawyGorny.mod())-1) / prawyGorny.mod()));
				Complex ksi = rect(aKsi, bKsi, width, height).pointToCmplx(x, y);
				if(ksi.x == 0 && ksi.y == 0)
					return new Complex(0);
				return Complex.mult(ksi, new Complex(Math.log(ksi.mod()+1)/ksi.mod()));
			}
			
			@Override
			public Point cmplxToPoint(Complex z, Complex lewyDolny, Complex prawyGorny) {
				Complex aKsi = Complex.mult(lewyDolny, new Complex ((Math.exp(lewyDolny.mod())-1) / lewyDolny.mod()));
				Complex bKsi = Complex.mult(prawyGorny, new Complex ((Math.exp(prawyGorny.mod())-1) / prawyGorny.mod()));
				Complex ksi;
				if(z.x == 0 && z.y == 0)
					ksi = new Complex(0);
				else
					ksi = Complex.mult(z, new Complex ((Math.exp(z.mod())-1) / z.mod()));
				return rect(aKsi, bKsi, width, height).cmplxToPoint(ksi);
			}

			@Override
			public Complex[] powiekszenie(Point p, Complex lewyDolny, Complex prawyGorny, double alpha) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public int accOfSmallerAr() {
				return accOfSmaller;
			}

			@Override
			public Complex[] przesuniecie(Complex z1, Point p2, Complex lewyDolny, Complex prawyGorny) {
				// TODO Auto-generated method stub
				return null;
			}
			
		};
		ret.setLD(lewyDolny);
		ret.setPG(prawyGorny);
		return ret;
	}

}
