package poczatek;

import java.util.ArrayList;

class FuncConst extends Func {
	final Complex a;

	FuncConst(Complex a){
		this.a = a;
	}
	
	@Override
	public Complex evaluate(Complex z) {

		return a;
	}

	@Override
	public String write() {
		// TODO Auto-generated method stub
		return null;
	}
	
}

class FuncMult extends Func {
	Func f;
	Func g;
	public FuncMult(Func f, Func g) {
		this.f = f;
		this.g=g;
	}
	@Override
	public Complex evaluate(Complex z) {
		//System.out.println("Funkcja mnożenie aktywowana");
		//f.evaluate(z).print();
		//g.evaluate(z).print();
		return Complex.mult(f.evaluate(z), g.evaluate(z));
	}
	@Override
	public String write() {
		// TODO Auto-generated method stub
		return null;
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
	@Override
	public String write() {
		// TODO Auto-generated method stub
		return null;
	}
}

/*
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
}*/

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
	@Override
	public String write() {
		// TODO Auto-generated method stub
		return null;
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
	@Override
	public String write() {
		// TODO Auto-generated method stub
		return null;
	}
	
}

abstract public class Func
 {
	static int calledReadCounter = 0;
	static String[] var = {"z", "x", "y"};//po kolei: zmienna zespolona, część rzeczywista, część urojona
	static String[] knownConstants = {"e", "pi", "phi", "i"};
	static Func[] knownConstantsVal = {Functions.e ,Functions.pi, Functions.phi, Functions.i};
	static String[] knownFunctions = {
			"exp",
			"Ln"
	};
	static Func[] knownFunctionsVal = {
			Functions.exp,
			Functions.Ln
	};
	int type; //CONST lub NONCONST
	
	abstract public Complex evaluate(Complex z);
	
	abstract public String write();
	
	private static String preliminaryChanges(String str) throws WrongSyntaxException {
		if(str.charAt(0) == '=') str = str.substring(1);
		str = str.replaceAll("\\s", "");
		str = str.replaceAll("[{]", "(");
		str = str.replaceAll("[}]", ")");
		str = BlokList.configureStr(str);
		if(str.matches(".*[^"+BlokList.OPERATORY+"|[a-zA-Z0-9]|" + BlokList.SPECJALNE + "].*")) 
			throw new WrongSyntaxException(
					"Niepoprawny zapis : występuje niedozwolony znak(i): " + str.replaceAll(BlokList.OPERATORY+"|[a-zA-Z0-9]|"+BlokList.SPECJALNE, ""));
		if(str.matches(".*["+BlokList.OPERATORY+"|[,.]]["+BlokList.OPERATORY+"|[,.]].*"))
			throw new WrongSyntaxException("Niepoprawny zapis : dwa operatry, przecinki lub kropki obok siebie");
		return str;
	}
	
	private static BlokList removeParenthases(BlokList bloki) throws WrongSyntaxException {
		if(bloki.arr.size() == 1 && bloki.arr.get(0).type != Blok.FUNCTION) {
			BlokList newBloki = new BlokList(BlokList.configureStr(bloki.arr.get(0).str));
			//System.out.println("usuwanie nawiasów na co zmienilo:");
			//newBloki.print();
			return newBloki;
		}
		return bloki;
	}
	
	public static Func read(BlokList bloki) throws WrongSyntaxException {
		calledReadCounter++;
		bloki = removeParenthases(bloki);
		if(bloki.arr.size() == 0)//wchodzi w grę jeśli jest plus lub minus z czymś tylko z jednej strony
			return new FuncConst(new Complex(0));
		if(bloki.arr.size() == 1) {
			Blok blok = bloki.arr.get(0);
			switch(blok.type) {
			
			case Blok.NUMBER:
				return new FuncConst(new Complex(Double.parseDouble(blok.str)));
			case Blok.FUNCTION:
				return new FuncComp(((BlokWthFunction)blok).funkcja, read(new BlokList(blok.str)));
			case Blok.WORD:
				if(blok.str.equals("i"))
					return new FuncConst(Complex.i);
				switch(BlokList.indexOf(var, blok.str)) {
				case -1:
					break;
				case 0:
					return Functions.Id;
				case 1:
					return Functions.Re;
				case 2:
					return Functions.Im;
				}
				if(BlokList.indexOf(knownConstants, blok.str) != -1) {
					return knownConstantsVal[BlokList.indexOf(knownConstants, blok.str)];
				}
				throw new WrongSyntaxException(blok.str + " nie jest znaną nazwą ani funkcji ani stałej");
			}
		}
		int splitIndex;
		splitIndex = bloki.find("+", 1);
		if(splitIndex != -1) {
			Func lFunc = read(bloki.subList(0, splitIndex));
			Func rFunc = read(bloki.subList(splitIndex+1, bloki.arr.size()));
			return new FuncAdd(lFunc, rFunc);
		}
		splitIndex = bloki.find("-",1);
		if(splitIndex != -1) {
			Func lFunc = read(bloki.subList(0, splitIndex));
			Func rFunc = read(bloki.subList(splitIndex+1, bloki.arr.size()));
			return new FuncAdd(lFunc, new FuncMult(new FuncConst(new Complex(-1.0)), rFunc));
		}
		splitIndex = bloki.find("*",1);
		if(splitIndex != -1) {
			Func lFunc = read(bloki.subList(0, splitIndex));
			Func rFunc = read(bloki.subList(splitIndex+1, bloki.arr.size()));
			return new FuncMult(lFunc, rFunc);
		}
		splitIndex = bloki.find("/",1);
		if(splitIndex != -1) {
			Func lFunc = read(bloki.subList(0, splitIndex));
			Func rFunc = read(bloki.subList(splitIndex+1, bloki.arr.size()));
			return new FuncMult(lFunc, new FuncPow(rFunc, new FuncConst(new Complex(-1.0))));
		}
		splitIndex = bloki.find("^",-1);
		if(splitIndex != -1) {
			int leftPowIndex = splitIndex;
			while(leftPowIndex>=2 && bloki.arr.get(leftPowIndex-2).str == "^") {
				leftPowIndex -= 2;
			}
			BlokList lBlok = bloki.subList(0, leftPowIndex-2);
			BlokList rBlok = bloki.subList(splitIndex+2, bloki.arr.size());
			Func lFunc = lBlok.arr.size()==0 ? new FuncConst(new Complex(1)) : read(lBlok);
			Func rFunc = rBlok.arr.size()==0 ? new FuncConst(new Complex(1)) : read(rBlok);

			return new FuncMult(lFunc, new FuncMult(rFunc,
					new FuncPow(read(bloki.subList(leftPowIndex-1, splitIndex)), read(bloki.subList(splitIndex+1, bloki.arr.size())))));
		}
		return new FuncMult(read(bloki.subList(0, 1)), read(bloki.subList(1, bloki.arr.size())));
	}

	public static void main(String[] args) throws WrongSyntaxException {

		BlokList bloki = new BlokList(preliminaryChanges("z^-2"));
		bloki.print();
		
		Func f = read(bloki);

		//System.out.println(Complex.div(3.14, ));
		f.evaluate(new Complex(0.8, 2)).print();
		System.out.println("b");
	}

}
