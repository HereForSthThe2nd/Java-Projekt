/*
 * funkcje są klasy Function
 * Function implementuje FuncChecker, 
 * czyli nie abstrakcyjne podklasy implementują metodę public void check, która sprawdza czy dwie funkcje są takie same
 * ta klasa zawiera też metodę interpretująca String - a
 */

package funkcja;

import java.io.Serializable;
import java.lang.classfile.instruction.ReturnInstruction;
import java.util.ArrayList;
import java.util.LinkedList;

import inne.Complex;

abstract public class Function implements FuncChecker, Serializable
 {	
		
	/**
	 * 
	 */
	private static final long serialVersionUID = 1650548978200084729L;
	final int type;
	
	final static int TOOBIG = 1000;//dla tej wartości .size() już nie warto wykonywac obliczeń
	
	final public int nofArg;
	protected Function(int type, int nofArg) {
		this.type=type;
		this.nofArg = nofArg;
	}
	
	//zwraca ocene wielkości funkcji
	protected abstract int size();
	
	protected abstract Complex evaluate(Complex[] arg) throws FunctionExpectedException;
	
	//w zwróconej funkcji z[2k] reprezentuje część rzeczywistą k-tej zmiennej, a z[2k+1] zmienną urojoną k-tej zmiennej
	protected abstract Function[] reim() throws FunctionExpectedException;
	
	//write nie musi wyglądać dobrze przed uproszczeniem funkcji
	public abstract String write(Settings settings) throws FunctionExpectedException;
	
	//public abstract boolean check(Function f);
	
	public abstract Function putArguments(Function[] args);
	
	public abstract Function expand();
	
	protected abstract Function expandSpecific(String name);
	
	//bardzo podstawowe
	protected abstract Function simplify(Settings setting) throws FunctionExpectedException;
	
	//zwraca nazwy funkcji które są w tej funkcji zawarte
	protected abstract LinkedList<String> checkDepecdencies();
	
	protected static String preliminaryChanges(String str) throws FunctionExpectedException {
		if(str.equals("")) 
			return "";
		str = str.replaceAll("\\s", "");
		if(str.charAt(0) == '=') str = str.substring(1);
		str = BlokList.configureStr(str);
		if(str.matches(".*[^"+BlokList.OPERATORY+"|[a-zA-Z0-9]|" +BlokList.GRECKIALFABET +"|"+  BlokList.SPECJALNE + "].*")) 
			throw new FunctionExpectedException(
					"Niepoprawny zapis : występuje niedozwolony znak(i): " + str.replaceAll(BlokList.OPERATORY+"|[a-zA-Z0-9]|"+BlokList.GRECKIALFABET +"|"+BlokList.SPECJALNE, ""));
		return str;
	}

	
	//arg - względem którego argumentu brana jest pochodna
	//dla funkcji zdefiniowanych przez użytkownika liczenie pochodnych może być bardzo wolne, ale naprawienie tego wymagaoby dużo pracy
	protected abstract Function diffX(int arg, Settings set) throws FunctionExpectedException;
	protected abstract Function diffY(int arg, Settings set) throws FunctionExpectedException;
	
	abstract Function removeDiff() throws FunctionExpectedException;
	
  	private static BlokList removeParenthases(BlokList bloki) throws FunctionExpectedException {
		if(bloki.arr.size() == 1 && bloki.arr.get(0).type != Blok.FUNCTION) {
			BlokList newBloki = new BlokList(BlokList.configureStr(bloki.arr.get(0).str));
			//System.out.println("usuwanie nawiasów na co zmienilo:");
			//newBloki.print();
			return newBloki;
		}
		return bloki;
	}
	 	
	protected static Function read(BlokList bloki, Settings settings) throws FunctionExpectedException {
		if(bloki.splitByComma().size()>1) 
			throw new FunctionExpectedException("Przecinek postawony w złym miejscu. Musi występować wewnątrz funkcji.");
		bloki = removeParenthases(bloki);
		if(bloki.arr.size() == 0){//wchodzi w grę jeśli jest plus lub minus z czymś tylko z jednej strony
			return new FuncNumConst(new Complex(0));
		}
		if(bloki.arr.size() == 1) {
			Blok blok = bloki.arr.get(0);
			switch(blok.type) {
			case Blok.NUMBER:
				return new FuncNumConst(new Complex(Double.parseDouble(blok.str)));
			case Blok.FUNCTION:
				LinkedList<BlokList> argsOfFunction = (new BlokList (blok.str.substring(1, blok.str.length()-1))).splitByComma();
				if(((BlokWthDefFunction)blok).funkcja.nofArg != argsOfFunction.size())
					throw new FunctionExpectedException("Funckcja " + ((BlokWthDefFunction)blok).funkcja.name
							+ " przyjmuje " + ((BlokWthDefFunction)blok).funkcja.nofArg + " argumentów, a podano ich "+ argsOfFunction.size() + ".");
				Function[] arg = new Function[argsOfFunction.size()];
				for(int i=0; i<argsOfFunction.size();i++) {
					arg[i] = read(argsOfFunction.get(i), settings);
				}
				return new FuncComp(((BlokWthDefFunction)blok).funkcja, arg);
			case Blok.WORD:
				if(Functions.ckeckIfVar(blok.str)) {
					return Functions.returnVar(blok.str);
				}
				throw new FunctionExpectedException(blok.str + " nie jest znaną nazwą ani funkcji ani zmiennej.");
			}
		}
		int splitIndex;
		splitIndex = bloki.find("+", 1);
		if(splitIndex != -1) {
			BlokList lStrona = bloki.subList(0, splitIndex);
			BlokList pStrona = bloki.subList(splitIndex+1, bloki.arr.size());
			if(pStrona.arr.size() == 0)
				throw new FunctionExpectedException("Występuje znak \"+\" bez elemntu z prawej strony");
			Function lFunc = read(lStrona, settings);
			Function rFunc = read(pStrona, settings);
			if(lStrona.arr.size() == 0)
				return rFunc;
			return new FuncSum(new Function[] {lFunc, rFunc});
		}
		splitIndex = bloki.find("-",-1);
		if(splitIndex != -1) {
			BlokList lStrona = bloki.subList(0, splitIndex);
			BlokList pStrona = bloki.subList(splitIndex+1, bloki.arr.size());
			Function lFunc = read(lStrona, settings);
			Function rFunc = read(pStrona, settings);
			if(pStrona.arr.size() == 0)
				throw new FunctionExpectedException("Występuje znak \"-\" bez elemntu z prawej strony");
			if(lStrona.arr.size() == 0)
				return new FuncMult(new FuncNumConst(new Complex(-1.0)), rFunc);
			return new FuncSum(new Function[] {lFunc, new FuncMult(new FuncNumConst(new Complex(-1.0)), rFunc)});
		}
		splitIndex = bloki.find("*",1);
		if(splitIndex != -1) {
			BlokList lStrona = bloki.subList(0, splitIndex);
			BlokList pStrona = bloki.subList(splitIndex+1, bloki.arr.size());
			Function lFunc = read(lStrona, settings);
			Function rFunc = read(pStrona, settings);
			if(pStrona.arr.size() == 0)
				throw new FunctionExpectedException("Występuje znak \"*\" bez elemntu z prawej strony");
			if(lStrona.arr.size() == 0)
				throw new FunctionExpectedException("Występuje znak \"*\" bez elemntu z lewej strony");
			return new FuncMult(lFunc, rFunc);
		}
		splitIndex = bloki.findConcatenation(1);
		if(splitIndex != -1) {
			Function lFunc = read(bloki.subList(0, splitIndex+1), settings);
			Function rFunc = read(bloki.subList(splitIndex+1, bloki.arr.size()), settings);
			return new FuncMult(lFunc, rFunc);
		}
		splitIndex = bloki.find("/",-1);
		if(splitIndex != -1) {
			BlokList lStrona = bloki.subList(0, splitIndex);
			BlokList pStrona = bloki.subList(splitIndex+1, bloki.arr.size());
			if(pStrona.arr.size() == 0)
				throw new FunctionExpectedException("Występuje znak \"/\" bez elemntu z prawej strony");
			if(lStrona.arr.size() == 0)
				throw new FunctionExpectedException("Występuje znak \"/\" bez elemntu z lewej strony");

			Function lFunc = read(lStrona, settings);
			Function rFunc = read(pStrona, settings);
			return new FuncMult(new Function[] {lFunc, new FuncComp(Functions.pow, new Function[] {rFunc, new FuncNumConst(new Complex(-1.0))})});
		}
		
		splitIndex = bloki.find("^",-1);
		if(splitIndex != -1) {
			BlokList lStrona = bloki.subList(0, splitIndex);
			BlokList pStrona = bloki.subList(splitIndex+1, bloki.arr.size());
			Function lFunc = read(lStrona, settings);
			Function rFunc = read(pStrona, settings);
			if(pStrona.arr.size() == 0)
				throw new FunctionExpectedException("Występuje znak \"^\" bez elemntu z prawej strony");
			if(lStrona.arr.size() == 0)
				throw new FunctionExpectedException("Występuje znak \"^\" bez elemntu z lewej strony");
			return new FuncComp(Functions.pow,	new Function[] {lFunc, rFunc});
		}
		throw new IllegalArgumentException("Nie powinno było tutaj dojść. Podany argument: " + bloki.write());
	}
	
	
}
