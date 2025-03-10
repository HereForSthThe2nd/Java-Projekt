package poczatek;

import java.util.ArrayList;

class FuncMult extends Func {
	Func f;
	Func g;
	public FuncMult(Func f, Func g) {
		this.f = f;
		this.g=g;
	}
	@Override
	public Complex evaluate(Complex z) {
		return Complex.mult(f.evaluate(z), g.evaluate(z));
	}
}

class FuncAdd extends Func {
	Func f;
	Func g;
	public FuncAdd(Func f, Func g) {
		this.f = f;
		this.g=g;
	}
	@Override
	public Complex evaluate(Complex z) {
		return Complex.add(f.evaluate(z), g.evaluate(z));
	}
}

class FuncSum extends Func {
	Func[] f;
	
	public FuncSum(Func[] f, int[] znaki) {
		this.f = new Func[f.length];
		for(int i=0; i<f.length;i++) {
			this.f[i] = f[i].multiply(new Complex((double) znaki[i]));
		}
	}
	@Override
	public Complex evaluate(Complex z) {
		Complex sum = new Complex(0);
		for(int i=0; i<f.length; i++) {
			sum = sum.add(f[i].evaluate(z));
		}
		return sum;
	}
}

class FuncComp extends Func {
	Func f;
	Func g;
	public FuncComp(Func f, Func g) {
		this.f = f;
		this.g = g;
	}
	@Override
	public Complex evaluate(Complex z) {
		return f.evaluate(g.evaluate(z));
	}
}

class FuncPow extends Func{
	Func a;
	Func b;
	
	FuncPow(Func a, Func b){
		this.a = a;
		this.b = b;
	}
	@Override
	public Complex evaluate(Complex z) {
		return a.evaluate(z).pow(b.evaluate(z));
	}
	
}

abstract public class Func {
	final static int CONST = 1;
	final static int NONCONST = 0;
	final static Func exp = new Func() {
		@Override
		public Complex evaluate(Complex z) {
			return Complex.exp(z);
		}
	};
	final static Func Ln = new Func() {
		@Override
		public Complex evaluate(Complex z) {
			return Complex.Ln(z);
		}
	};
	static String[] var = {"z", "x", "y"};//po kolei: zmienna zespolona, część rzeczywista, część urojona
	static String[] knownConstants = {"e", "pi", "phi", "i"};
	static Complex[] knownConstantsVal = {new Complex(Math.E),new Complex(Math.PI), new Complex((Math.sqrt(5)+1)/2), Complex.i};
	static String[] knownFunctions = {
			"exp",
			"Ln"
	};
	static Func[] knownFunctionsVal = {
			exp,
			Ln
	};
	int type; //CONST lub NONCONST
	
	abstract public Complex evaluate(Complex z);
	
	public Func multiply(Complex C) {
		return new Func() {
			@Override
			public Complex evaluate(Complex z) {
				return Complex.mult(C, evaluate(z));
			}
		};
	}
	
	private static void readMult(String sfunc) {
		//wczytuje funkcję ze stringa jeśli ona zawiera tylko konkatenację mnożenie oraz dzielenie (bez nawiasów)
		// funkja ta będzie postaci C*z^n[0]*x^n[1]*y^n[2]
		Complex C = new Complex(1.0);
		int[] n = {0,0,0};
		
		int inDenom = 1; //+1 jeśli rozważamy licznik, -1 jeśli mianownik
		while(true) {
			inDenom = 1;
			if(sfunc.charAt(0) == '/') {
				inDenom = -1;
				sfunc = sfunc.substring(1);
			}
			if(sfunc.charAt(0) == '*') {
				sfunc = sfunc.substring(1);
			}
			int[] blok = StrOperations.blok(sfunc, 0);
			//wiadomo, że blok[0] = 0
			if(StrOperations.isNum(sfunc.charAt(blok[0]))) {
				switch(inDenom) {
				case 1: 
					C = Complex.mult(C, new Complex(Double.parseDouble(sfunc.substring(blok[0], blok[1]))));
					break;
				case -1:
					C = Complex.div(C, new Complex(Double.parseDouble(sfunc.substring(blok[0], blok[1]))));
					break;
				}
			}
			if(StrOperations.isLetter(sfunc.charAt(blok[0]))) {
				if(StrOperations.contains(var, sfunc.substring(blok[0], blok[1]))) {
					n[StrOperations.indexOf(var, sfunc.substring(blok[0], blok[1]))] += inDenom;
				}
				else {
				if(StrOperations.contains(knownConstants, sfunc.substring(blok[0], blok[1]))) {
					switch(inDenom) {
					case 1:
						C = Complex.mult(C, knownConstantsVal[StrOperations.indexOf(knownConstants, sfunc.substring(blok[0], blok[1]))]);
						break;
					case -1:
						C = Complex.div(C, knownConstantsVal[StrOperations.indexOf(knownConstants, sfunc.substring(blok[0], blok[1]))]);
						break;
					}
				}
				else {
					throw new IllegalArgumentException("Ciąg znaków " + sfunc.substring(blok[0], blok[1]) + " nie jest ani zmienną, ani stałą, ani funkcją.");
				}
				}
			}
			if(blok[1] == sfunc.length())
				break;
			sfunc = sfunc.substring(blok[1]);
			//System.out.println(sfunc);
			//System.out.println(blok[0]);
			//System.out.println(blok[1]);
		}
		System.out.println("1");
		System.out.println("C = " + C.x + " + i" + C.y + "\n" + 
							"nz = " + n[0] + "\n" + 
							"nx = " + n[1] + "\n" + 
							"ny = " + n[2] + "\n");
	}

	private static void readPow(String sfunc) {

		//wczytuje funkcję ze stringa jeśli ona zawiera tylko potęgi i mniejsze (bez nawiasów)
		if(StrOperations.contains(StrOperations.SPECJALNE, sfunc.charAt(0))) 
			throw new IllegalArgumentException("Niepoprawny zapis");
		for(int i=0;i<sfunc.length();i++) {
			String c = "" + sfunc.charAt(i);
			//if(var.contains())
		}
		int i = sfunc.indexOf('^');
		
		if(i != -1) {
			
		}
	}
	
	public static void read(String sfunc) {
		sfunc = sfunc.replaceAll("\\s", "");
		sfunc = sfunc.replaceAll("{", "(");
		sfunc = sfunc.replaceAll("}", ")");
		sfunc = sfunc.replaceAll(",", ".");
		if(sfunc.charAt(0) == '=') sfunc = sfunc.substring(1);
		for(int i=0;i<sfunc.length(); i++) {
			char cTeraz = sfunc.charAt(i);
			char cNast = sfunc.charAt( (i+1)%sfunc.length() );
			if(!StrOperations.contains(StrOperations.ALFABET, cTeraz) && !StrOperations.contains(StrOperations.SPECJALNEaz))
				throw new IllegalArgumentException("Niepoprawny zapis : występuje błędny znak");
			if(StrOperations.contains(StrOperations.SPECJALNE + ".", cTeraz) && StrOperations.contains(StrOperations.SPECJALNE + ".", cNast))
				throw new IllegalArgumentException("Niepoprawny zapis : dwa operatry obo siebie");
		}
		
	}

	public static void main(String[] args) {
		//System.out.println("ssss".charAt(5));
		String str = "67.6abc[d35[[]5]..3]po";
		for(int i=0;i<str.length();i++) {
			System.out.println(str.substring(StrOperations.blok(str, i)[0], StrOperations.blok(str,i)[1]));
		}
		//readMult("5*5/x*x*z*z*z/y/y/x/pi");
		/*String str2 = "[frfr]";
		for(int i=0;i<str2.length();i++) {
			System.out.print(StrOperations.wNawiasachKw(str2, i)[0] + "  ");
			System.out.println(StrOperations.wNawiasachKw(str2, i)[1]);
		}*/

	}

}
