package funkcja;

import java.util.ArrayList;

class Bool<T>{
	final T f;
	final boolean bool;
	Bool(T f, boolean p){
		this.f = f;
		this.bool = p;
	}
}

abstract public class Function implements FuncChecker
 {	
	final int type;
	final int nofArg;
	
	protected Function(int type, int nofArg) {
		this.type=type;
		this.nofArg = nofArg;
	}
	protected abstract Complex evaluate(Complex[] arg);
	
	protected abstract Function re() throws WewnetzrnaFunkcjaZleZapisana;
	protected abstract Function im() throws WewnetzrnaFunkcjaZleZapisana;
	
	//write nie musi wyglądać dobrze przed uproszczeniem funkcji
	protected abstract String write(Settings settings);
	
	//public abstract boolean check(Function f);
	
	protected abstract Function putArguments(Function[] args);
	
	protected abstract Function expand();
	//bardzo podstawowe
	protected abstract Function simplify(Settings setting) throws WewnetzrnaFunkcjaZleZapisana;
	
	protected static String preliminaryChanges(String str) throws WrongSyntaxException {
		if(str.equals("")) 
			return "";
		if(str.charAt(0) == '=') str = str.substring(1);
		str = str.replaceAll("\\s", "");
		str = BlokList.configureStr(str);
		if(str.matches(".*[^"+BlokList.OPERATORY+"|[a-zA-Z0-9]|" + BlokList.SPECJALNE + "].*")) 
			throw new WrongSyntaxException(
					"Niepoprawny zapis : występuje niedozwolony znak(i): " + str.replaceAll(BlokList.OPERATORY+"|[a-zA-Z0-9]|"+BlokList.SPECJALNE, ""));
		if(str.matches(".*["+BlokList.OPERATORY+"|[,.]]["+BlokList.OPERATORY+"|[,.]].*"))
			//TODO:to wyrzuca błąd jak się wpisze np. (-1)
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
	
	protected static Function read(BlokList bloki, Settings settings) throws WrongSyntaxException {
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
				if(((BlokWthDefFunction)blok).funkcja.nofArg != strArg.length)
					throw new WrongSyntaxException("Funckcja " + ((BlokWthDefFunction)blok).funkcja.name
							+ " przyjmuje " + ((BlokWthDefFunction)blok).funkcja.nofArg + " argumentów, a podano ich "+ strArg.length + ".");
				Function[] arg = new Function[strArg.length];
				for(int i=0; i<strArg.length;i++) {
					arg[i] = read(new BlokList(strArg[i]), settings);

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
			Function lFunc = read(bloki.subList(0, splitIndex), settings);
			Function rFunc = read(bloki.subList(splitIndex+1, bloki.arr.size()), settings);
			return new FuncSum(new Function[] {lFunc, rFunc});
		}
		splitIndex = bloki.find("-",-1);
		if(splitIndex != -1) {
			Function lFunc = read(bloki.subList(0, splitIndex), settings);
			Function rFunc = read(bloki.subList(splitIndex+1, bloki.arr.size()), settings);
			return new FuncSum(new Function[] {lFunc, new FuncMult(new FuncNumConst(new Complex(-1.0)), rFunc)});
		}
		splitIndex = bloki.find("*",1);
		if(splitIndex != -1) {
			Function lFunc = read(bloki.subList(0, splitIndex), settings);
			Function rFunc = read(bloki.subList(splitIndex+1, bloki.arr.size()), settings);
			return new FuncMult(lFunc, rFunc);
		}

		splitIndex = bloki.find("^",-1);
		if(splitIndex != -1) {
			int leftPowIndex = splitIndex;
			while(leftPowIndex>=2 && bloki.arr.get(leftPowIndex-2).str.equals("^")) {
				leftPowIndex -= 2;
			}
			BlokList lBlok = bloki.subList(0, leftPowIndex-1);
			BlokList rBlok = bloki.subList(splitIndex+2, bloki.arr.size());
			Function lFunc = lBlok.arr.size()==0 ? new FuncNumConst(new Complex(1)) : read(lBlok, settings);
			Function rFunc = rBlok.arr.size()==0 ? new FuncNumConst(new Complex(1)) : read(rBlok, settings);
			return new FuncMult(new Function[] {lFunc, rFunc,
					new FuncComp(Functions.pow,
							new Function[] {read(bloki.subList(leftPowIndex-1, splitIndex), settings), read(bloki.subList(splitIndex+1, bloki.arr.size()), settings)})});
		}
		splitIndex = bloki.find("/",-1);
		if(splitIndex != -1) {
			Function lFunc = read(bloki.subList(0, splitIndex), settings);
			Function divBy = read(bloki.subList(splitIndex+1, splitIndex+2), settings);
			Function rFunc = read(bloki.subList(splitIndex+2, bloki.arr.size()), settings);
			if(splitIndex + 2 == bloki.arr.size()) {
				rFunc = new FuncNumConst(new Complex(1));
			}
			return new FuncMult(new Function[] {lFunc, new FuncComp(Functions.pow, new Function[] {divBy, new FuncNumConst(new Complex(-1.0))}), rFunc});
		}
		return new FuncMult(new Function[] {read(bloki.subList(0, 1), settings), read(bloki.subList(1, bloki.arr.size()), settings)});
	}
	
	public static void main(String[] args) throws WrongSyntaxException {
	}
}
