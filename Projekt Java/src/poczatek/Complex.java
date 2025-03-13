package poczatek;

public class Complex {
	double x;
	double y;
	public static final Complex i = new Complex(0,1);
	
	public void print() {
		System.out.println(x + " + i" + y);
	}
	
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
		return Math.sqrt(x*x+y*y);
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
		return mult(this,b);
	}
	static public Complex div(Complex a, Complex b) {
		if(b.x == 0 && b.y == 0) return null;
		double d = b.x*b.x + b.y*b.y;
		Complex l = Complex.mult(a, b.conj());
		return new Complex(l.x/d, l.y/d);
	}
	public Complex div(Complex b) {
		return div(this, b);
	}
	static public Complex add(Complex a, Complex b) {
		return new Complex(a.x+b.x, a.y+b.y);
	}
	public Complex add(Complex b) {
		return add(this, b);
	}
	public static Complex subt(Complex a, Complex b) {
		return new Complex(a.x-b.x, a.y-b.y);
	}
	public Complex subt(Complex b) {
		return subt(this, b);
	}
	public static Complex exp(Complex a) {
		return mult( new Complex(Math.exp(a.x)),   add( mult( i,new Complex(Math.sin(a.y)) ), new Complex(Math.cos(a.y)) ));
	}
	public static Complex Ln(Complex a) {
		//Im(Ln) w (-pi, pi]
		return new Complex(Math.log(a.mod()), a.arg());
	}
	public static Complex pow(Complex a, int n) {
		if(n==0)
			return new Complex(1);
		Complex w = new Complex(1,0);
		if(n<0)
			return Complex.div(new Complex(1.0), pow(a,-n));
		while(n > 1) {
			if(n%2 == 0) {
				n /= 2;
				a = mult(a,a);
			}else {
				w = w.mult(a);
				n -= 1;
			}
		}
		return mult(a,w);
	}
	public static Complex pow(Complex a, Complex b) {
		if(b.y == 0 && b.x == (int)b.x) {
			return pow(a, (int)b.x);
		}
		return exp(b.mult(Ln(a)));
	}
	public Complex pow(Complex b) {
		return pow(this,b);
	}
	public static void main(String[] args) {
		Complex a = pow(new Complex(2,1), new Complex(1,1));
		Complex c = new Complex(2,1);
		Complex b = Ln(new Complex(1,1));
		System.out.println(b.x + " + i" + b.y);
		System.out.println(c.mult(b).x + " + i"+c.mult(b).y);
		System.out.println(a.x + " + i" + a.y);
	}
}
