package poczatek;

import java.util.ArrayList;

class Bool<T>{
	final T f;
	final boolean p;
	Bool(T f, boolean p){
		this.f = f;
		this.p = p;
	}
}

abstract public class Function
 {
	static int calledReadCounter = 0;
	
	final int type;
	final int nofArg;
	
	public Function(int type, int nofArg) {
		this.type=type;
		this.nofArg = nofArg;
	}
	abstract public Complex evaluate(Complex[] arg);
	
	abstract public String write(PrintSettings settings);
	
	abstract public boolean equals(Function f);
	
	abstract public Function putArguments(Function[] args);
	
	//TODO: w przyszłości dodać ustawienia do expand, aby istaniała decyzja czy rozszerzać zmienne oraz stałe, czy nie
	abstract public Bool<Function> expand();
	
	protected static String preliminaryChanges(String str) throws WrongSyntaxException {
		if(str.charAt(0) == '=') str = str.substring(1);
		str = str.replaceAll("\\s", "");
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
			return new FuncNumConst(new Complex(0));
		if(bloki.arr.size() == 1) {
			Blok blok = bloki.arr.get(0);
			switch(blok.type) {
			case Blok.NUMBER:
				return new FuncNumConst(new Complex(Double.parseDouble(blok.str)));
			case Blok.FUNCTION:
				String[] strArg = blok.str.substring(1, blok.str.length()-1).split(",");
				Function[] arg = new Function[strArg.length];
				for(int i=0; i<strArg.length;i++) {
					arg[i] = read(new BlokList(strArg[i]));

				}
				return new FuncComp(((BlokWthDefFunction)blok).funkcja, arg);
			case Blok.WORD:
				if(Functions.ckeckIfVar(blok.str)) {
					return Functions.returnVar(blok.str);
				}
				throw new WrongSyntaxException(blok.str + " nie jest znaną nazwą ani funkcji ani zmiennej ani stałej.");
			}
		}
		int splitIndex;
		splitIndex = bloki.find("+", 1);
		if(splitIndex != -1) {
			Function lFunc = read(bloki.subList(0, splitIndex));
			Function rFunc = read(bloki.subList(splitIndex+1, bloki.arr.size()));
			return new FuncSum(new Function[] {lFunc, rFunc});
		}
		splitIndex = bloki.find("-",-1);
		if(splitIndex != -1) {
			Function lFunc = read(bloki.subList(0, splitIndex));
			Function rFunc = read(bloki.subList(splitIndex+1, bloki.arr.size()));
			return new FuncSum(new Function[] {lFunc, new FuncMult(new Function[] {new FuncNumConst(new Complex(-1.0)), rFunc})});
		}
		splitIndex = bloki.find("*",1);
		if(splitIndex != -1) {
			Function lFunc = read(bloki.subList(0, splitIndex));
			Function rFunc = read(bloki.subList(splitIndex+1, bloki.arr.size()));
			return new FuncMult(new Function[] {lFunc, rFunc});
		}
		splitIndex = bloki.find("/",-1);
		if(splitIndex != -1) {
			Function lFunc = read(bloki.subList(0, splitIndex));
			Function rFunc = read(bloki.subList(splitIndex+1, bloki.arr.size()));
			return new FuncMult(new Function[] {lFunc, new FuncPow(rFunc, new FuncNumConst(new Complex(-1.0)))});
		}
		splitIndex = bloki.find("^",-1);
		if(splitIndex != -1) {
			int leftPowIndex = splitIndex;
			while(leftPowIndex>=2 && bloki.arr.get(leftPowIndex-2).str == "^") {
				leftPowIndex -= 2;
			}
			BlokList lBlok = bloki.subList(0, leftPowIndex-2);
			BlokList rBlok = bloki.subList(splitIndex+2, bloki.arr.size());
			Function lFunc = lBlok.arr.size()==0 ? new FuncNumConst(new Complex(1)) : read(lBlok);
			Function rFunc = rBlok.arr.size()==0 ? new FuncNumConst(new Complex(1)) : read(rBlok);

			return new FuncMult(new Function[] {lFunc, rFunc,
					new FuncPow(read(bloki.subList(leftPowIndex-1, splitIndex)), read(bloki.subList(splitIndex+1, bloki.arr.size()))) });
		}
		return new FuncMult(new Function[] {read(bloki.subList(0, 1)), read(bloki.subList(1, bloki.arr.size()))});
	}

	private static void test() throws WrongSyntaxException {
		BlokList bloki = new BlokList(preliminaryChanges("exp(z+w)/exp(x+i*y)/exp(z[1])"));
		bloki.print();
		
		Function f = read(bloki);

		f.evaluate(new Complex[] {new Complex(1,0), new Complex(3,1)}).print();
		System.out.println(f.write(PrintSettings.defaultSettings));

	}
	
	private static void test2() throws WrongSyntaxException {
		BlokList bloki1 = new BlokList(preliminaryChanges("z + z[1] * z[3]+0.78+i"));
		Function f0 = read(bloki1);
		System.out.println("f0 liczba argumentów 4 == " + f0.nofArg);
		BlokList bloki2 = new BlokList(preliminaryChanges("z[2]+z[1]"));
		Function f2 = read(bloki2);
		System.out.println("f2 liczba argumentów 3 == " + f2.nofArg);
		BlokList bloki3 = new BlokList(preliminaryChanges("x[2]"));
		Function f3 = read(bloki3);
		System.out.println("f3 liczba argumentów 3 == " + f3.nofArg);
		BlokList bloki4 = new BlokList(preliminaryChanges("exp(z+w)/exp(x+i*y)/exp(z[1])"));
		Function f4 = read(bloki4);
		System.out.println("f4 liczba argumentów 2 == " + f4.nofArg);
		BlokList bloki5 = new BlokList(preliminaryChanges("z"));
		Function f5 = read(bloki5);
		System.out.println("f5 liczba argumentów 1 == " + f5.nofArg);
		Complex[] ars = new Complex[] { new Complex(2,3), new Complex(1,1), new Complex(1.4,2) };
		Function g1 = f0.putArguments(new Function[] {f2,f3,f4,f5});
		System.out.println("g1 liczba argumentów 3 == " + g1.nofArg);
		System.out.println(g1.write(PrintSettings.defaultSettings));
		g1.evaluate(ars).print();
		
		Function g2 = read(new BlokList(preliminaryChanges(g1.write(PrintSettings.defaultSettings))));
		System.out.println(g2.write(PrintSettings.defaultSettings));
		g2.evaluate(ars).print();
		
		FuncNamed f = new FuncGivenName(f0, "f");
		Function g = new FuncComp(f, new Function[] {f2,f3,f4,f5});
		System.out.println(g.write(PrintSettings.defaultSettings));
		Function gEx = g.expand();
		System.out.println(gEx.write(PrintSettings.defaultSettings));
	}
	
	public static void main(String[] args) throws WrongSyntaxException {

	}
}
