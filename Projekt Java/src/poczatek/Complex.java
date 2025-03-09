package poczatek;

public class Complex {
	double x;
	double y;
	public Complex(double x, double y){
		this.x = x;
		this.y = y;
	}
	public Complex(double x) {
		this.x = x;
		this.y = 0;
	}
	public Complex conj() {
		return new Complex(x, -y);
	}
	public double mod() {
		return x*x+y*y;
	}
	public double arg() {
		//w przedziale (-pi, pi]
		return Math.atan2(y, x);
	}
	static public Complex mult(Complex a, Complex b) {
		double s1 = a.x*b.x;
		double s2 = a.y*b.y;
		double s3 = (a.x+a.y)*(b.x+b.y);
		return new Complex(s1-s2, s3-s1-s2);
	}
	public Complex mult(Complex b) {
		return Complex.mult(this,b);
	}
	static public Complex div(Complex a, Complex b) {
		if(b.x == 0 && b.y == 0) throw new IllegalArgumentException("Dzielenie przez 0");
		double d = b.x*b.x + b.y*b.y;
		Complex l = Complex.mult(a, b.conj());
		return new Complex(l.x/d, l.y/d);
	}
	public Complex div(Complex b) {
		return Complex.div(this, b);
	}
	static public Complex add(Complex a, Complex b) {
		return new Complex(a.x+b.x, a.y+b.y);
	}
	public Complex add(Complex b) {
		return Complex.add(this, b);
	}
	public static Complex subt(Complex a, Complex b) {
		return new Complex(a.x-b.x, a.y-b.y);
	}
	public Complex subt(Complex b) {
		return Complex.subt(this, b);
	}
}
