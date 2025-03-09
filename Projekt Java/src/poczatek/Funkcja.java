package poczatek;

import java.util.ArrayList;

class FuncMult extends Funkcja {
	Funkcja f;
	Funkcja g;
	public FuncMult(Funkcja f, Funkcja g) {
		this.f = f;
		this.g=g;
	}
	@Override
	public Complex evaluate(Complex z) {
		return Complex.mult(f.evaluate(z), g.evaluate(z));
	}
}

class FuncAdd extends Funkcja {
	Funkcja f;
	Funkcja g;
	public FuncAdd(Funkcja f, Funkcja g) {
		this.f = f;
		this.g=g;
	}
	@Override
	public Complex evaluate(Complex z) {
		return Complex.add(f.evaluate(z), g.evaluate(z));
	}
}

class FuncComp extends Funkcja {
	Funkcja f;
	Funkcja g;
	public FuncComp(Funkcja f, Funkcja g) {
		this.f = f;
		this.g = g;
	}
	@Override
	public Complex evaluate(Complex z) {
		return f.evaluate(g.evaluate(z));
	}
}


abstract public class Funkcja {
	final static String ALFABET = "abcdefghijklmnopqrstuvwxyz";
	final static String SPECJALNE = "^*/+-()";
	final static String CYFRY = "0123456789.";
	final static int CONST = 1;
	final static int NONCONST = 0;
	static String[] var = {"z", "x", "y"};//po kolei: zmienna zespolona, część rzeczywista, część urojona
	static String[] knownConstants = {"e", "pi", "phi", "i"};
	static Complex[] knownConstantsVal = {new Complex(Math.E),new Complex(Math.PI), new Complex((Math.sqrt(5)+1)/2), new Complex(0.0,1.0)};
	static String[] knownFunctions = {};
	static Funkcja[] knownFunctionsVal = {};
	int type; //CONST lub NONCONST

	class StrOperations{
		
		private static boolean contains(String str, char chr) {
			return str.indexOf(chr)==-1 ? false : true;
		}
		
		private static int indexOf(String[] arr, String str) {
			for(int i=0;i<arr.length;i++) {
				if(arr[i].equals(str))
					return i;
			}
			return -1;
		}
		
		private static boolean contains(String[] arr, String str) {
			return indexOf(arr, str) == -1 ? false : true;
		}		
		
		private static boolean isLetter(char chr) {
			return contains(ALFABET, chr);
		}		
		
		private static boolean isNum(char chr) {
			return contains(CYFRY, chr);
		}
		
		private static boolean isSpecial(char chr) {
			return contains(SPECJALNE, chr);
		}		
		
		private static int[] blok(String str, int index) {
			if(index < 0 || index > str.length())
				throw new IllegalArgumentException("Niepoprawne argumenty");
			
			int[] konce = {index, index};
			if(isLetter(str.charAt(index))) {
				while(isLetter(str.charAt(konce[0]))) {
					konce[0] -= 1;
					if(konce[0] == -1)
						break;
				}
				while(isLetter(str.charAt(konce[1]))) {
					konce[1] += 1;
					if(konce[1] == str.length())
						break;
				}
				konce[0]++;
				return konce;
			}
			if(isNum(str.charAt(index))){
				int countCommas = str.charAt(index) == '.' ? -1 : 0;
				while(isNum(str.charAt(konce[0]))) {
					countCommas += str.charAt(konce[0]) == '.' ? 1 : 0;
					konce[0] -= 1;
					if(konce[0] == -1)
						break;
				}
				while(isNum(str.charAt(konce[1]))) {
					countCommas += str.charAt(konce[1]) == '.' ? 1 : 0;
					konce[1] += 1;
					if(konce[1] == str.length())
						break;
				}
				konce[0]++;
				if(countCommas > 1) 
					throw new IllegalArgumentException("Niepoprawny zapisis (coś z przecinkami)");
				return konce;
			}
			else
			throw new IllegalArgumentException("Niepoprawne argumenty");
		}
	}
	
	abstract public Complex evaluate(Complex z);
	
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
		if(StrOperations.contains(SPECJALNE, sfunc.charAt(0))) 
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
		sfunc = sfunc.replaceAll("[", "(");
		sfunc = sfunc.replaceAll("{", "(");
		sfunc = sfunc.replaceAll("]", ")");
		sfunc = sfunc.replaceAll("}", ")");
		sfunc = sfunc.replaceAll(",", ".");
		if(sfunc.charAt(0) == '=') sfunc = sfunc.substring(1);
		for(int i=0;i<sfunc.length(); i++) {
			char cTeraz = sfunc.charAt(i);
			char cNast = sfunc.charAt( (i+1)%sfunc.length() );
			if(!StrOperations.contains(ALFABET, cTeraz) && !StrOperations.contains(SPECJALNE, cTeraz))
				throw new IllegalArgumentException("Niepoprawny zapis : występuje błędny znak");
			if(StrOperations.contains(SPECJALNE + ".", cTeraz) && StrOperations.contains(SPECJALNE + ".", cNast))
				throw new IllegalArgumentException("Niepoprawny zapis : dwa operatry obo siebie");
		}
		
	}

	public static void main(String[] args) {
		//System.out.println("ssss".charAt(5));
		//String str = "6777,76 3ff84ff 45.2";
		readMult("5*5/x*x*z*z*z/y/y/x/pi");
	}

}
