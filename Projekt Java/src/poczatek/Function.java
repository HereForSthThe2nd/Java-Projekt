package poczatek;

import java.util.ArrayList;

abstract public class Function
 {
	static int calledReadCounter = 0;
	static String[] var = {"z", "x", "y"};//po kolei: zmienna zespolona, część rzeczywista, część urojona
	static String[] knownConstants = {"e", "pi", "phi", "i"};
	static Function[] knownConstantsVal = {Functions.e ,Functions.pi, Functions.phi, Functions.i};
	static String[] knownFunctions = {
			"exp",
			"Ln"
	};
	static DefinedFunction[] knownFunctionsVal = {
			Functions.exp,
			Functions.Ln
	};
	final int type;
	final int nofArg;
	
	public Function(int type, int nofArg) {
		this.type=type;
		this.nofArg = nofArg;
	}
	


	abstract public Complex evaluate(Complex[] arg);
	
	abstract public String write(PrintSettings settings);//doubleAcc >= 1
	
	protected static String preliminaryChanges(String str) throws WrongSyntaxException {
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
	
	public static Function read(BlokList bloki) throws WrongSyntaxException {
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
				String[] strArg = blok.str.split(",");
				System.out.println(strArg[0]);
				Function[] arg = new Function[strArg.length];
				for(int i=0; i<strArg.length;i++) {
					arg[i] = read(new BlokList(strArg[i]));
				}
				return new FuncComp(((BlokWthDefFunction)blok).funkcja, arg);
			case Blok.WORD:
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
			Function lFunc = read(bloki.subList(0, splitIndex));
			Function rFunc = read(bloki.subList(splitIndex+1, bloki.arr.size()));
			return new FuncSum(new Function[] {lFunc, rFunc});
		}
		splitIndex = bloki.find("-",1);
		if(splitIndex != -1) {
			Function lFunc = read(bloki.subList(0, splitIndex));
			Function rFunc = read(bloki.subList(splitIndex+1, bloki.arr.size()));
			return new FuncSum(new Function[] {lFunc, new FuncMult(new Function[] {new FuncConst(new Complex(-1.0)), rFunc})});
		}
		splitIndex = bloki.find("*",1);
		if(splitIndex != -1) {
			Function lFunc = read(bloki.subList(0, splitIndex));
			Function rFunc = read(bloki.subList(splitIndex+1, bloki.arr.size()));
			return new FuncMult(new Function[] {lFunc, rFunc});
		}
		splitIndex = bloki.find("/",1);
		if(splitIndex != -1) {
			Function lFunc = read(bloki.subList(0, splitIndex));
			Function rFunc = read(bloki.subList(splitIndex+1, bloki.arr.size()));
			return new FuncMult(new Function[] {lFunc, new FuncPow(rFunc, new FuncConst(new Complex(-1.0)))});
		}
		splitIndex = bloki.find("^",-1);
		if(splitIndex != -1) {
			int leftPowIndex = splitIndex;
			while(leftPowIndex>=2 && bloki.arr.get(leftPowIndex-2).str == "^") {
				leftPowIndex -= 2;
			}
			BlokList lBlok = bloki.subList(0, leftPowIndex-2);
			BlokList rBlok = bloki.subList(splitIndex+2, bloki.arr.size());
			Function lFunc = lBlok.arr.size()==0 ? new FuncConst(new Complex(1)) : read(lBlok);
			Function rFunc = rBlok.arr.size()==0 ? new FuncConst(new Complex(1)) : read(rBlok);

			return new FuncMult(new Function[] {lFunc, rFunc,
					new FuncPow(read(bloki.subList(leftPowIndex-1, splitIndex)), read(bloki.subList(splitIndex+1, bloki.arr.size()))) });
		}
		return new FuncMult(new Function[] {read(bloki.subList(0, 1)), read(bloki.subList(1, bloki.arr.size()))});
	}

	public static void main(String[] args) throws WrongSyntaxException {

		BlokList bloki = new BlokList(preliminaryChanges("exp(1+z^8/z^7+2pi*i-1)"));
		//bloki.print();
		
		Function f = read(bloki);

		//System.out.println(Complex.div(3.14, ));
		f.evaluate(new Complex[] {new Complex(0, Math.PI)}).print();
		System.out.println(f.write(PrintSettings.defaultSettings));
	}
}
