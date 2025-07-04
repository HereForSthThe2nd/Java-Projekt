package grafika;

import java.awt.Point;

import inne.Complex;

public abstract class Coordinates implements Cloneable{
	private Complex lewyDolny, prawyGorny;
	public abstract Complex pointToCmplx(double x, double y, Complex lewyDolny, Complex prawyGorny);
	public 	abstract Point cmplxToPoint(Complex z, Complex lewyDolny, Complex prawyGorny);
	//wykorzystywane przy powiększaniu / pomniejszaniu
	public abstract Complex[] powiekszenie(Point p, Complex lewyDolny, Complex prawyGorny, double alpha);
	public abstract Complex[] przesuniecie(Complex z, Point p, Complex lewyDolny, Complex prawyGorny);
	final public Complex getPG() {
		return prawyGorny;
	};
	final public Complex getLD() {
		return lewyDolny;
	};
	final public void setPG(Complex PG) {
		prawyGorny = PG;
	};
	final public void setLD(Complex LD) {
		lewyDolny = LD;
	};
	//zwraca jak dokładnie trzeba liczyć mniejszy obszar
	public abstract int accOfSmallerAr();
	
	public Complex pointToCmplx(Point p, Complex lewyDolny, Complex prawyGorny) {
		return pointToCmplx(p.x, p.y, lewyDolny, prawyGorny);
	};

	public Complex pointToCmplx(double x, double y) {
		return pointToCmplx(x, y, getLD(), getPG());
	}
	
	public Complex pointToCmplx(Point p) {
		return pointToCmplx(p, getLD(), getPG());
	};
	
	public Point cmplxToPoint(Complex z) {
		return cmplxToPoint(z, getLD(), getPG());
	};
	/*default void noweZewn(Point p, double alpha) {
		Complex[] noweRogi = powiekszenie(p, getLD(), getPG(), alpha);
		setLD(noweRogi[0]);
		setPG(noweRogi[1]);
	}*/
	public Complex[] powiekszenie(Point p, double alpha) {
		return powiekszenie(p, getLD(), getPG(), alpha);
	}
	
	public Complex[] przesuniecie(Complex z, Point p) {
		return przesuniecie(z, p, getLD(), getPG());
	}
	
	@Override
	public Coordinates clone() {
		
		try {
			Coordinates ret = (Coordinates)super.clone();
			ret.lewyDolny = lewyDolny.clone();
			ret.prawyGorny = prawyGorny.clone();
			return ret;
		} catch (CloneNotSupportedException e) {
			throw new IllegalStateException();
		}
	}
}