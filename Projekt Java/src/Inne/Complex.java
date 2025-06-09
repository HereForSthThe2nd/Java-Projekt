package Inne;

import java.io.Serializable;
import java.text.DecimalFormat;

public class Complex implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8998826487834311530L;
	public double x;
	public double y;
	public static final Complex NaN = new Complex(Double.NaN, Double.NaN);
	public static final Complex i = new Complex(0,1);
	
	public void print() {
		System.out.println(x + " + i" + y);
	}
	
	static public String toStr(double a, int doubleAcc, int whenToShorten) {
		//dla doubla
		String format = "%."+whenToShorten;
		if((Math.abs(a) < Math.pow(10, whenToShorten) && Math.abs(a) >= Math.pow(10, -whenToShorten)) || a == 0) {
			format += "f";
			return String.format(format, a);
		}
		format += "e";
		return String.format(format, a);
	}
	
	//TODO: chyba usunąć
	static public Double readStr(String str) {
		try {
	
			str = str.replaceAll("\\s+", "");
			
			int eind = str.indexOf("e");
			if(eind == -1)
				return Double.parseDouble(str);
		//TODO
			return 1.0;
			
		}catch(NumberFormatException e){
			return null;
		}
	}
	
	public String print(int doubleAcc) {
		DecimalFormat noplus = new DecimalFormat("0."+(new String(new char[doubleAcc])).replace("\0", "#"));
		DecimalFormat plus = new DecimalFormat("0."+(new String(new char[doubleAcc])).replace("\0", "#"));
		plus.setPositivePrefix("+");
		if(equals(new Complex(0)))
			return "0";
		if(y==1.0) {
			return x==0 ? "i" : noplus.format(x)+"+i";
		}
		if(x!=0 && y!=0)
			return noplus.format(x)+plus.format(y)+"i";
		if(y==0)
			return noplus.format(x);
		return noplus.format(y)+"i";
	}
	
	public String printE(int doubleAcc, int whenToShorten) {
		if(equals(new Complex(0)))
			return "0";
		if(x == 0)
			return toStr(y, doubleAcc, whenToShorten);
		if(y == 0)
			return toStr(x, doubleAcc, whenToShorten);
		if(y > 0)
			return toStr(x, doubleAcc, whenToShorten)+" + i"+toStr(y, doubleAcc, whenToShorten);
		if(y < 0)
			return toStr(x, doubleAcc, whenToShorten)+" - i"+toStr(-y, doubleAcc, whenToShorten);
		return x + ", i" + y;
	}
		
	public String printEShort(int doubleAcc, int whenToShorten) {
		if(equals(new Complex(0)))
			return "0";
		if(Math.abs(x) < 10e-12)
			return toStr(y, doubleAcc, whenToShorten);
		if(Math.abs(y) < 10e-12)
			return toStr(x, doubleAcc, whenToShorten);
		if(y > 0)
			return toStr(x, doubleAcc, whenToShorten)+" + i"+toStr(y, doubleAcc, whenToShorten);
		if(y < 0)
			return toStr(x, doubleAcc, whenToShorten)+" - i"+toStr(-y, doubleAcc, whenToShorten);
		return x + ", i" + y;
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
	public boolean equals(Complex z) {
		return x==z.x && y==z.y;
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
	public void mult(Complex b) {
		Complex cmplx = mult(this,b);
		this.x = cmplx.x;
		this.y = cmplx.y;
	}
	static public Complex div(Complex a, Complex b) {
		//if(b.x == 0 && b.y == 0) return null;
		if(b.x == 0 && b.y == 0) {
			return Complex.NaN;
		}
		double d = b.x*b.x + b.y*b.y;
		Complex l = Complex.mult(a, b.conj());
		return new Complex(l.x/d, l.y/d);
	}

	public void div(Complex b) {
		Complex cmplx = div(this, b);
		this.x = cmplx.x;
		this.y = cmplx.y;
	}
	static public Complex add(Complex a, Complex b) {
		return new Complex(a.x+b.x, a.y+b.y);
	}
	public void add(Complex b) {
		this.x += b.x;
		this.y += b.y;
	}
	public static Complex subt(Complex a, Complex b) {
		return new Complex(a.x-b.x, a.y-b.y);
	}
	public void subt(Complex b) {
		this.x -= b.x;
		this.y -= b.y;
	}
	public static Complex exp(Complex a) {
		return mult( new Complex(Math.exp(a.x)),   add( mult( i,new Complex(Math.sin(a.y)) ), new Complex(Math.cos(a.y)) ));
	}
	public static Complex Ln(Complex a) {

		//Im(Ln) w (-pi, pi]
		return new Complex(Math.log(a.mod()), a.arg());
	}
	public static Complex sin(Complex z) {
		return Complex.mult(new Complex(0,-0.5), Complex.subt(Complex.exp(Complex.mult(new Complex(0,1), z)), Complex.exp(Complex.mult(new Complex(0,-1), z))));
	}
	public static Complex cos(Complex z) {
		return Complex.mult(new Complex(0.5,0), Complex.add(Complex.exp(Complex.mult(new Complex(0,1), z)), Complex.exp(Complex.mult(new Complex(0,-1), z))));
	}
	public static Complex sinh(Complex z) {
		return Complex.mult(new Complex(0.5), Complex.subt(Complex.exp(z), Complex.exp(Complex.mult(z,new Complex(-1)))));
	}
	public static Complex cosh(Complex z) {
		return Complex.mult(new Complex(0.5), Complex.add(Complex.exp(z), Complex.exp(Complex.mult(z,new Complex(-1)))));
	}

	public static Complex ln(Complex a, double d) {
		Complex dNorm = new Complex(0,d+Math.PI);
		return Complex.add(Ln(Complex.mult(a, exp(Complex.mult(new Complex(-1) , dNorm)))), dNorm); 
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
				w.mult(a);
				n -= 1;
			}
		}
		return mult(a,w);
	}
	public static Complex pow(Complex a, Complex b) {
		if(b.y == 0 && b.x == (int)b.x) {
			return pow(a, (int)b.x);
		}
		if(a.x == 0 && a.y == 0 && (b.x != 0 || b.y != 0)) {
			return Complex.NaN;
		}
		return exp(mult(b, Ln(a)));
	}
	public Complex pow(Complex b) {
		return pow(this,b);
	}
	public static Complex pow(Complex a, Complex b, double d) {
		if(b.y == 0 && b.x == (int)b.x) {
			return pow(a, (int)b.x);
		}
		return exp(mult(b, ln(a, d)));
	}
	public Complex pow(Complex b, double d) {
		return pow(this,b, d);
	}
	public static void main(String[] args) {

	}
}

