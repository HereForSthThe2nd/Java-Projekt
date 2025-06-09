/*
 * definiowane funkcje i zmienne
 * tutaj też są metody do dodawania nowych funkcji zdefiniowanych przez użytkownika
 * sprawdzanie czy jakaś nazwa to nazwa funkcji / zmiennej zachodzi tutaj
 */

package funkcja;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import Inne.Complex;
import funkcja.Function;

/*
 * NonStandardFuncStr do definiowania funkcji typu ln{α}, dla których istnieje nieskończona ilość możliwych nazw
 */
interface NonStandardFuncStr extends FuncChecker, Serializable{
	Function returnFunc(String str);
	boolean check(String str);
	default boolean check(Function f) {
		if(f.type==Functions.NAMED)
			if(check(((FuncWthName)f).name))
				return true;
		return false;
	};
}

public class Functions {
	final static int 
			NUMCONST = 1,
			NAMED = 2,
			ADD=3,
			MULT=4,
			POW=5,
			COMPOSITE=6;
	public final static String zapisaneFunkcjePlik = "funkcje_Zapisane.ser";
	public final static String zapisaneZmiennePlik = "zmienne_Zapisane.ser";
	
	static Log logChecker = new Log();
	static Pow powChecker = new Pow();
	static  BscVariables xAndYchecker = new BscVariables();
	static Identities idChecker = new Identities();
	
	final private static FuncWthName Ln = new FunctionDefault(1, "Ln") {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1110351581456933206L;

		//przy upraszczaniu pamięta konkretną gałąz
		@Override
		protected Complex evaluate(Complex[] arg) {
			return Complex.Ln(arg[0]);
		}

		@Override
		protected Function[] reim() {
			try {
				Function re = new FunctionPowloka("ln((z[0]^2+z[1]^2)^(1/2))", new Settings()).getFunction();
				Function im = new FuncComp(arg, new Function[] {new FunctionPowloka ("z[0]+i*z[1]", new Settings()).getFunction()});
				return new Function[] {re, im};
			} catch (FunctionExpectedException e) {
				throw new IllegalArgumentException(e);
			}
		}
		
		
		protected Function diffX(int arg, Settings set) {
			if(arg > 1)
				return new FuncNumConst(new Complex(0));
			return pow.putArguments(new Function[] {new FuncNumConst(new Complex(1)), idChecker.returnFunc("z")});
		};
		
		@Override
		protected Function diffY(int arg, Settings set) {
			if(arg > 1)
				return new FuncNumConst(new Complex(0));
			return pow.putArguments(new Function[] {new FuncNumConst(Complex.i), idChecker.returnFunc("z")});
		}
	};
	final static FuncWthName ln = new FunctionDefault(1,"ln") {
		/**
		 * 
		 */
		private static final long serialVersionUID = -9012789967668241939L;

		//przy upraszczaniu niekoniecznie pamięta gałąź
		@Override
		protected Complex evaluate(Complex[] arg) {
			return Complex.Ln(arg[0]);
		}

		@Override
		protected Function[] reim()  {
			try {
				Function re = new FunctionPowloka("ln((z[0]^2+z[1]^2)^(1/2))", new Settings()).getFunction();
				Function im = new FuncComp(arg, new Function[] {new FunctionPowloka ("z[0]+i*z[1]", new Settings()).getFunction()});
				return new Function[] {re, im};
			} catch (FunctionExpectedException e) {
				throw new IllegalArgumentException(e);
			}
		}

		@Override
		protected Function diffX(int arg, Settings set) {
			if(arg > 1)
				return new FuncNumConst(new Complex(0));
			return pow.putArguments(new Function[] {new FuncNumConst(new Complex(1)), idChecker.returnFunc("z")});
		}

		@Override
		protected Function diffY(int arg, Settings set) {
			if(arg > 1)
				return new FuncNumConst(new Complex(0));
			return pow.putArguments(new Function[] {new FuncNumConst(Complex.i), idChecker.returnFunc("z")});
		}
	};
	final protected static FuncWthName arg = new FunctionDefault(1, "arg") {

		/**
		 * 
		 */
		private static final long serialVersionUID = -119639873404520635L;

		@Override
		protected Complex evaluate(Complex[] arg) {
			return new Complex (arg[0].arg());
		}

		@Override
		protected Function[] reim()  {
			try {
				return new Function[] {new FuncComp(this, new Function[ ] {new FunctionPowloka("z[0]+i*z[1]", new Settings()).getFunction()}), new FuncNumConst(new Complex(0))};
			} catch (FunctionExpectedException e) {
				throw new IllegalStateException(e);
			}
		}


		@Override
		protected Function diffX(int arg, Settings set) {
			if(arg > 1)
				return new FuncNumConst(new Complex(0));
			try {
				return (new FunctionPowloka("-y/(x^2+y^2)", new Settings())).getFunction();
			} catch (FunctionExpectedException e) {
				throw new IllegalArgumentException(e);
			}
		}

		@Override
		protected Function diffY(int arg, Settings set) {
			if(arg > 1)
				return new FuncNumConst(new Complex(0));
			try {
				return (new FunctionPowloka("x/(x^2+y^2)", new Settings())).getFunction();
			} catch (FunctionExpectedException e) {
				throw new IllegalArgumentException(e);
			}
		}
	};
	final protected static FuncWthName exp = new FunctionDefault(1, "exp") {
		/**
		 * 
		 */
		private static final long serialVersionUID = 2206176788336161425L;

		@Override
		protected Complex evaluate(Complex[] arg) {
			return Complex.exp(arg[0]);
		}

		@Override
		protected Function[] reim()  {
			try {
				Function re = new FunctionPowloka("exp(z[0])*cos(z[1])", new Settings()).getFunction();
				Function im = new FunctionPowloka("exp(z[0])*sin(z[1])", new Settings()).getFunction();
				return new Function[] {re,im};
			} catch (FunctionExpectedException e) {
				throw new IllegalStateException(e);
			}
		}


		@Override
		protected Function diffX(int arg, Settings set) {
			if(arg > 1)
				return new FuncNumConst(new Complex(0));
			return this;
		}

		@Override
		protected Function diffY(int arg, Settings set) {
			if(arg > 1)
				return new FuncNumConst(new Complex(0));
			return new FuncMult(new FuncNumConst(Complex.i), this);
		}
	};
	final protected static FuncWthName sin = new FunctionDefault(1, "sin") {

		/**
		 * 
		 */
		private static final long serialVersionUID = 2937248837597662444L;

		@Override
		protected Complex evaluate(Complex[] arg) {
			return Complex.sin(arg[0]);
		}

		@Override
		protected Function[] reim()  {
			try {
				Function re = new FunctionPowloka("cosh(z[1])*sin(z[0])", new Settings()).getFunction();
				Function im = new FunctionPowloka("sinh(z[1])*cos(z[0])", new Settings()).getFunction();
				return new Function[] {re,im};
			} catch (FunctionExpectedException e) {
				throw new IllegalStateException(e);
			}
		}

		@Override
		protected Function diffX(int arg, Settings set) {
			if(arg > 1)
				return new FuncNumConst(new Complex(0));
			return new FuncComp(cos, new Function [] {idChecker.returnFunc("z")});
		}

		@Override
		protected Function diffY(int arg, Settings set) {
			if(arg > 1)
				return new FuncNumConst(new Complex(0));
			return new FuncMult(new FuncNumConst(Complex.i), new FuncComp(cos, new Function [] {idChecker.returnFunc("z")}));
		}
		
	};
	final protected static FuncWthName cos = new FunctionDefault(1, "cos") {

		/**
		 * 
		 */
		private static final long serialVersionUID = -696222417875775527L;

		@Override
		protected Complex evaluate(Complex[] arg) {
			return Complex.cos(arg[0]);
		}

		@Override
		protected Function[] reim()  {
			try {
				Function re = new FunctionPowloka("cosh(z[1])*cos(z[0])", new Settings()).getFunction();
				Function im = new FunctionPowloka("-sinh(z[1])*sin(z[0])", new Settings()).getFunction();
				return new Function[] {re,im};
			} catch (FunctionExpectedException e) {
				throw new IllegalStateException(e);
			}
		}

		@Override
		protected Function diffX(int arg, Settings set) {
			if(arg > 1)
				return new FuncNumConst(new Complex(0));
			return new FuncMult(new FuncNumConst(new Complex(-1)), new FuncComp(sin, new Function [] {idChecker.returnFunc("z")}));
		}

		@Override
		protected Function diffY(int arg, Settings set) {
			if(arg > 1)
				return new FuncNumConst(new Complex(0));
			return new FuncMult(new FuncNumConst(new Complex(0,-1)), new FuncComp(cos, new Function [] {idChecker.returnFunc("z")}));
		}
		
		
	};
	final protected static FuncWthName sinh = new FunctionDefault(1, "sinh") {

		/**
		 * 
		 */
		private static final long serialVersionUID = -9157071374432234383L;

		@Override
		protected Complex evaluate(Complex[] arg) {
			return Complex.sinh(arg[0]);
		}

		@Override
		protected Function[] reim()  {
			try {
				Function re = new FunctionPowloka("sinh(z[0])*cos(z[1])", new Settings()).getFunction();
				Function im = new FunctionPowloka("cosh(z[0])*sin(z[1])", new Settings()).getFunction();
				return new Function[] {re,im};
			} catch (FunctionExpectedException e) {
				throw new IllegalStateException(e);
			}
		}


		@Override
		protected Function diffX(int arg, Settings set) {
			if(arg > 1)
				return new FuncNumConst(new Complex(0));
			return new FuncComp(cosh, new Function [] {idChecker.returnFunc("z")});
		}

		@Override
		protected Function diffY(int arg, Settings set) {
			if(arg > 1)
				return new FuncNumConst(new Complex(0));
			return new FuncMult(new FuncNumConst(Complex.i), new FuncComp(cosh, new Function [] {idChecker.returnFunc("z")}));
		}
		
	};
	final protected static FuncWthName cosh = new FunctionDefault(1, "cosh") {

		/**
		 * 
		 */
		private static final long serialVersionUID = -2153131662667491153L;

		@Override
		protected Complex evaluate(Complex[] arg) {
			return Complex.cosh(arg[0]);
		}

		@Override
		protected Function[] reim()  {
			try {
				Function re = new FunctionPowloka("cosh(z[0])*cos(z[1])", new Settings()).getFunction();
				Function im = new FunctionPowloka("sinh(z[0])*sin(z[1])", new Settings()).getFunction();
				return new Function[] {re,im};
			} catch (FunctionExpectedException e) {
				throw new IllegalStateException(e);
			}
		}

		@Override
		protected Function diffX(int arg, Settings set) {
			if(arg > 1)
				return new FuncNumConst(new Complex(0));
			return new FuncComp(sinh, new Function [] {idChecker.returnFunc("z")});
		}

		@Override
		protected Function diffY(int arg, Settings set) {
			if(arg > 1)
				return new FuncNumConst(new Complex(0));
			return new FuncMult(new FuncNumConst(Complex.i), new FuncComp(sinh, new Function [] {idChecker.returnFunc("z")}));
		}
		
	};
	final protected static FuncWthName Re = new FunctionDefault(1, "Re") {
		/**
		 * 
		 */
		private static final long serialVersionUID = -8113154337037374698L;

		@Override
		protected Complex evaluate(Complex[] arg) {
			return new Complex(arg[0].x);
		}

		@Override
		protected Function[] reim()  {
			return new Function[] {idChecker.returnFunc("z[0]"), new FuncNumConst(new Complex(0))};
		}
		
		@Override
		protected Function diffX(int arg, Settings set) {
			if(arg > 1)
				return new FuncNumConst(new Complex(0));
			return new FuncNumConst(new Complex(1));
		}

		@Override
		protected Function diffY(int arg, Settings set) {
			if(arg > 1)
				return new FuncNumConst(new Complex(0));
			return new FuncNumConst(new Complex(0));
		}
	};
	final protected static FuncWthName Im = new FunctionDefault(1, "Im") {

		/**
		 * 
		 */
		private static final long serialVersionUID = -2012796293089506692L;

		@Override
		protected Complex evaluate(Complex[] arg) {
			return new Complex(arg[0].y);
		}

		@Override
		protected Function[] reim()  {
			return new Function[] {idChecker.returnFunc("z[1]"), new FuncNumConst(new Complex(0))};
		}

		@Override
		protected Function diffX(int arg, Settings set) {
			if(arg > 1)
				return new FuncNumConst(new Complex(0));
			return new FuncNumConst(new Complex(0));
		}

		@Override
		protected Function diffY(int arg, Settings set) {
			if(arg > 1)
				return new FuncNumConst(new Complex(0));
			return new FuncNumConst(new Complex(1));
		}
	};
	final protected static FuncWthName pow = new FunctionDefault(2,"pow") {
		/**
		 * 
		 */
		private static final long serialVersionUID = -7981696045541250720L;
		//przy upraszczaniu niekoniecznie pamięta gałąź
		@Override
		protected Complex evaluate(Complex[] arg) {
			return arg[0].pow(arg[1]);
		}
		@Override
		public String write(Settings settings) {
			//?????nie wiem czemu to tu jest
			return "pow";
		}
		@Override
		protected Function[] reim() {
			try {
				Function mnoz = new FunctionPowloka("exp(z[2]*ln(z[0]^2+z[1]^2)/2 - z[3] * arg(z[0]+i*z[1]))", new Settings()).getFunction();
				Function rePom = new FunctionPowloka("cos(ln(z[0]^2+z[1]^2)*z[3]/2+arg(z[0]+i*z[1])*z[2])", new Settings()).getFunction();
				Function imPom = new FunctionPowloka("sin(ln(z[0]^2+z[1]^2)*z[3]/2+arg(z[0]+i*z[1])*z[2])", new Settings()).getFunction();
				return new Function[] {new FuncMult(mnoz, rePom), new FuncMult(mnoz, imPom)};
			} catch (FunctionExpectedException e) {
				throw new IllegalStateException(e);
			}
		}

		@Override
		protected Function diffX(int arg, Settings set) {
			if(arg > 2)
				return new FuncNumConst(new Complex(0));
			try {
				if(arg==1) {
					return (new FunctionPowloka("z[1]*pow(z[0], z[1]-1)", new Settings())).getFunction();
				}
				if(arg==2) {
					return (new FunctionPowloka("pow(z[0],z[1])*ln(z[0])", new Settings())).getFunction();
				}
				throw new IllegalArgumentException("??? arg == " + arg);
			} catch (FunctionExpectedException e) {
				throw new IllegalArgumentException(e);
			}
		}
		@Override
		protected Function diffY(int arg, Settings set) {
			if(arg > 2)
				return new FuncNumConst(new Complex(0));
			try {
				if(arg==1) {
					return (new FunctionPowloka("i*z[1]*pow(z[0], z[1]-1)", new Settings())).getFunction();
				}
				if(arg==2) {
					return (new FunctionPowloka("i*pow(z[0],z[1])*ln(z[0])", new Settings())).getFunction();
				}
				throw new IllegalArgumentException("??? arg == " + arg);
			} catch (FunctionExpectedException e) {
				throw new IllegalArgumentException(e);
			}
		}
	};
	
	final protected static FuncWthName diffX = new FunctionDefault(1, "diffX") {
		/**
		 * 
		 */
		private static final long serialVersionUID = 7955277656102383344L;

		//zapewne w inny sposób powinno byc zaimplementowane, najlepiej zmienić całys sposób działania funkcji bazowych
		@Override
		protected Complex evaluate(Complex[] arg) {
			throw new IllegalArgumentException("pochodna");
		}

		@Override
		protected Function[] reim()  {
			throw new IllegalArgumentException("pochodna");
		}

		
		@Override
		protected Function diffX(int arg, Settings set) {
			throw new IllegalArgumentException("pochodna");
		}

		@Override
		protected Function diffY(int arg, Settings set) {
			throw new IllegalArgumentException("pochodna");
		}
	};
	
	final protected static FuncWthName diffY = new FunctionDefault(1, "diffY") {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1450882303364199505L;

		//zapewne w inny sposób powinno byc zaimplementowane, najlepiej zmienić całys sposób działania funkcji bazowych
		@Override
		protected Complex evaluate(Complex[] arg) {
			throw new IllegalArgumentException("pochodna");
		}

		@Override
		protected Function[] reim()  {
			throw new IllegalArgumentException("pochodna");
		}

		@Override
		protected Function diffX(int arg, Settings set) {
			throw new IllegalArgumentException("pochodna");
		}

		@Override
		protected Function diffY(int arg, Settings set) {
			throw new IllegalArgumentException("pochodna");
		}
	};

	
	final protected static FuncWthName e = new FuncConstDefault("e") {
		/**
		 * 
		 */
		private static final long serialVersionUID = 5738869165532584210L;

		@Override
		protected Complex evaluate(Complex[] arg) {
			return new Complex(Math.E);
		}

		@Override
		protected Function[] reim()  {
			return new Function [] {this, new FuncNumConst(new Complex(0))};
		}

	};
	final protected static FuncWthName pi = new FuncConstDefault("pi") {
		/**
		 * 
		 */
		private static final long serialVersionUID = 3394572557035232119L;
		@Override
		protected Complex evaluate(Complex[] arg) {
			return new Complex(Math.PI);
		}

		@Override
		protected Function[] reim()  {
			return new Function [] {this, new FuncNumConst(new Complex(0))};
		}

		@Override
		boolean check(String str) {
			return super.check(str) || str.equals("π");
		}
		@Override
		public String write(Settings settings) {
			if(!settings.writeNeatVar)
				return super.write(settings);
			return "π";
		};
		
	};
	final protected static FuncWthName phi = new FuncConstDefault("PHI") {
		/**
		 * 
		 */
		private static final long serialVersionUID = 8146831552882843232L;
		@Override
		protected Complex evaluate(Complex[] arg) {
			return new Complex((Math.sqrt(5)+1)/2);
		}

		@Override
		protected Function[] reim()  {
			return new Function [] {this, new FuncNumConst(new Complex(0))};
		}

		@Override
		boolean check(String str) {
			return super.check(str) || str.equals("\u03d5");
		};
		@Override
		public String write(Settings settings) {
			if(!settings.writeNeatVar)
				return super.write(settings);
			return "\u03d5";
		};

	};	
	final protected static Function i = new FuncNumConst(Complex.i);	
	
	protected static FuncWthName r = new VarGivenName("r", new FuncComp(pow, new Function[] {new FuncSum(new Function []{
			new FuncComp(pow, new Function[] {xAndYchecker.returnFunc("x"), new FuncNumConst(new Complex(2))}),
			new FuncComp(pow, new Function[] {xAndYchecker.returnFunc("y"), new FuncNumConst(new Complex(2))})
			}),
			new FuncNumConst(new Complex(0.5))}));// new VarGivenName("r", "(x^2+y^2)^1/2", 1);
	protected static FuncWthName kat = new VarGivenName("phi", new FuncComp(arg, new Function[] {idChecker.returnFunc("z")})) {
		/**
		 * 
		 */
		private static final long serialVersionUID = -6641376489766538468L;
		@Override
		protected Function[] reim() {
			return new Function[] {this, new FuncNumConst(new Complex(0))};
		};
		@Override
		public String write(Settings settings) {
			if(settings.writeNeatVar)
				return "\u03c6";
			return super.write(settings);
		};
	};
	
	
	
	public final static NameAndValue defaultVar = new NameAndValue(/*new ArrayList<String>(List.of("e", "pi", "phi", "\u03c6", "r")),*/
			new ArrayList<FuncWthName>(List.of(e ,pi, phi, kat, r)));
	public final static NameAndValue defaultFunctions = new NameAndValue(
			/*new ArrayList<String>(List.of("exp", "Ln","ln", "Re", "Im", "pow", "sin", "cos", "sinh", "cosh","arg", "diffX", "diffY")),*/
			new ArrayList<FuncWthName>(List.of(exp, Ln, ln, Re, Im, pow, sin, cos, sinh, cosh, arg, diffX, diffY)));
	public static NameAndValue userFunctions = new NameAndValue();
	public static NameAndValue userVar = new NameAndValue();
	static {
		//opcja szybkiego i prostego zresetowania zapisanego pliku, jeśli coś z nim poszło nie tak
		if(false)
			try {
				FileOutputStream file = new FileOutputStream(Functions.zapisaneFunkcjePlik);
				ObjectOutputStream out = new ObjectOutputStream(file);
				out.writeObject(Functions.userFunctions);
				out.close();
				file.close();
			}catch (IOException e) {
				e.printStackTrace();
			}
		try {
            FileInputStream file = new FileInputStream(zapisaneFunkcjePlik);
            ObjectInputStream in = new ObjectInputStream(file);
            userFunctions = (NameAndValue)in.readObject();
            in.close();
            file.close();
		}catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
	}
	
	
	static private void checkNameRequirements(String str) throws IncorrectNameException{
		if(ckeckIfVar(str) || checkIfNmdFunc(str))
			throw new IncorrectNameException("Już istnieje " + (ckeckIfVar(str) ? "zmienna lub stała":"funkcja") + " o takiej nazwie.");
		BlokList blok;
		try {
			blok = new BlokList(str);
		} catch (FunctionExpectedException e) {
			throw new IncorrectNameException("Nie udało się przetworzyć takiej nazwy.");
		}
		if(blok.arr.size() > 1)
			throw new IncorrectNameException("Podana nazwa składa się z co najmniej dwóch elementów.");
		if(blok.arr.size() == 0)
		switch(blok.arr.get(0).type) {
		case(Blok.NUMBER):
			throw new IncorrectNameException("Podana nazwa nie może być liczbą.");
		case(Blok.OPERATION):
			throw new IncorrectNameException("Podana nazwa nie może być operatorem.");
		case(Blok.PARENTHASES):
			throw new IncorrectNameException("Podana nazwa nie może zawierać nawiasów.");
		}
	}
	
	static protected FuncWthName addNmdFunc(Function f0, String str) throws IncorrectNameException {
		checkNameRequirements(str);
		FuncWthName f = new FuncGivenName(f0, str);
		userFunctions.add(f, f.name);
		return f;
	}	

	static protected boolean checkIfNmdFunc(String str) {
		return defaultFunctions.checkIfContained(str) || userFunctions.checkIfContained(str) || logChecker.check(str) || powChecker.check(str);
	}
	
	static protected FuncWthName returnNmdFunc(String str) throws FunctionExpectedException {
		if(!checkIfNmdFunc(str))
			throw new IllegalArgumentException(str + " nie jest nazwą rzadnej zdefiniowanej funkcji.");
		if(logChecker.check(str))
			return logChecker.returnFunc(str);
		if(powChecker.check(str))
			return powChecker.returnFunc(str);
		return defaultFunctions.checkIfContained(str) ? defaultFunctions.functionOf(str) : userFunctions.functionOf(str);
	}

	static protected FuncWthName addVar(Function f0, String str) throws IncorrectNameException {
		checkNameRequirements(str);
		FuncWthName f;
		if(f0.nofArg == 0)
			f = new FuncConstGivenName(str, f0);
		else
			f = new VarGivenName(str, f0);
		userVar.add(f, f.name);
		return f;
	}
	
	static boolean ckeckIfVar(String str) {
		return userVar.checkIfContained(str) || defaultVar.checkIfContained(str) || xAndYchecker.check(str) || idChecker.check(str) || "i".equals(str);
	}
	
	static Function returnVar(String str) {
		if(str.equals("i")) {
			return i;
		}
		if(defaultVar.checkIfContained(str))
			return defaultVar.functionOf(str);
		if(userVar.checkIfContained(str))
			return userVar.functionOf(str);
		if(xAndYchecker.check(str))
			return xAndYchecker.returnFunc(str);
		if(idChecker.check(str))
			return idChecker.returnFunc(str);
		throw new IllegalArgumentException(str + " nie jest nazwą rzadnej zdefiniowanej zmiennej");
	}

	static public LinkedList<String> chackIfSafeToRemove(String name) {
		//zwraca listę funkcji które od funkcji związanej z name zależą
		
		LinkedList<String> ret = new LinkedList<String>();
		if(!checkIfNmdFunc(name) && !ckeckIfVar(name))
			throw new IllegalArgumentException("Taka funkcja nie istnieje");
		for(FuncWthName f : userVar.getValues()) {
			if(!f.name.equals(name) && f.checkDepecdencies().contains(name))
				ret.add(f.name);
		}
		for(FuncWthName f : userFunctions.getValues()) {
			if(!f.name.equals(name) && f.checkDepecdencies().contains(name))
				ret.add(f.name);
		}
		return ret;
	}
	
	static public void expandAllSpecific(String name) {

		for(FuncWthName f : userFunctions.getValues()) {
			userFunctions.changeFunc(f.name, new FuncGivenName(((FuncSurrWthName)f).f.expandSpecific(name), f.name));
		}
		for(FuncWthName f : userVar.getValues()) {
			userFunctions.changeFunc(f.name, new VarGivenName(f.name, ((FuncSurrWthName)f).f.expandSpecific(name)));
		}
	}

	
	public static class NameAndValue implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = 7353690714676591302L;
		LinkedList<String> names = new LinkedList<String>();
		private ArrayList<FuncWthName> values = new ArrayList<FuncWthName>();
		NameAndValue(){};
		NameAndValue(ArrayList<FuncWthName> val){
			this.values = val;
			for(FuncWthName i : values) {
				names.add(i.name);
			}
		}
		int indexOf(String str) {
			for(int i=0;i<names.size();i++) {
				if(values.get(i).check(str))
					return i;
			}
			return -1;
		}
		public boolean checkIfContained(String str) {
			return indexOf(str)!=-1;
		}
		
		public void removeFunc(String str) {
			int i = names.indexOf(str);
			if(i==-1)
				throw new IllegalArgumentException("Najpierw powinno się sprawdzić czy str wogóle jest zawarty metodą checkIfContained. str: " + str);
			names.remove(i);
			values.remove(i);
		}
		
		public void changeFunc(String str, FuncWthName newFunc) {
			int i = names.indexOf(str);
			if(i==-1)
				throw new IllegalArgumentException("Najpierw powinno się sprawdzić czy str wogóle jest zawarty metodą checkIfContained. str: " + str);
			values.set(names.indexOf(str), newFunc);
			names.set(names.indexOf(str), newFunc.name);
		}
		
		FuncWthName functionOf(String str){
			return values.get(indexOf(str));
		}
		void add(FuncWthName val, String str) {
			names.add(str);
			values.add(val);
		}
		

		public int size() {
			return names.size();
		}
		public ArrayList<FuncWthName> getValues(){
			return values;
		}
	}

	static class Log implements NonStandardFuncStr{
		/**
		 * 
		 */
		private static final long serialVersionUID = 8222413127464547793L;

		@Override
		public boolean check(String str) {
				if(str.equals("ln")||str.equals("Ln"))
					return true;
				if(str.length() >= 4) {
					if(!str.matches("ln\\{.*\\}"))
						return false;
					try {
						FunctionPowloka f = new FunctionPowloka(str.substring(3, str.length()-1), new Settings());
						if(f.nofArg() == 0 && f.evaluate(new Complex[] {}).y == 0) {
							return true;
						}
					}
					catch(FunctionExpectedException e) {}
				}
				return false;
			}
	
		@Override
		public FunctionDefault returnFunc(String str){
			if(str.equals("ln")||str.equals("Ln"))
				return (FunctionDefault)defaultFunctions.functionOf(str);
			if(!check(str))
				throw new IllegalArgumentException();
			try {
				FunctionPowloka f = new FunctionPowloka(str.substring(3, str.length()-1), new Settings());
				double d = f.evaluate(new Complex[] {}).x;
				return new FunctionDefault(1, "ln{"+d+"}") {
					/**
					 * 
					 */
					private static final long serialVersionUID = 1147887075367181046L;

					@Override
					protected Complex evaluate(Complex[] arg) {
						return Complex.ln(arg[0], d);
					}

					@Override
					protected Function[] reim()  {
						try {
							Function re = new FunctionPowloka("ln((z[0]^2+z[1]^2)^(1/2))", new Settings()).getFunction();
							Function im = new FuncComp(arg, new Function[] {new FunctionPowloka ("z[0]+i*z[1]", new Settings()).getFunction()});
							return new Function[] {re, im};
						} catch (FunctionExpectedException e) {
							throw new IllegalStateException(e);
						}
					}


					@Override
					protected Function diffX(int arg, Settings set) {
						if(arg > 1)
							return new FuncNumConst(new Complex(0));
						return pow.putArguments(new Function[] {new FuncNumConst(new Complex(1)), idChecker.returnFunc("z")});
					}

					@Override
					protected Function diffY(int arg, Settings set) {
						if(arg > 1)
							return new FuncNumConst(new Complex(0));
						return pow.putArguments(new Function[] {new FuncNumConst(Complex.i), idChecker.returnFunc("z")});
					}
				};
			}
			catch(FunctionExpectedException e){
				throw new IllegalArgumentException("Program nie powinien tutaj wogóle dojść.");
			}
		}
	}
	
	static class Pow implements NonStandardFuncStr{
	
		/**
		 * 
		 */
		private static final long serialVersionUID = -7582922422622201608L;

		@Override
		public FunctionDefault returnFunc(String str){
			if(!check(str))
				throw new IllegalArgumentException();
			if(str.equals("pow"))
				return (FunctionDefault)defaultFunctions.functionOf(str);
			try {
				FunctionPowloka f = new FunctionPowloka(str.substring(4, str.length()-1), new Settings());
				double d = f.evaluate(new Complex[] {}).x;
				return new FunctionDefault(2, "pow{"+d+"}") {
					/**
					 * 
					 */
					private static final long serialVersionUID = -4670303628812155289L;
					@Override
					protected Complex evaluate(Complex[] arg) {
						return Complex.pow(arg[0], arg[1], d);
					}

					@Override
					protected Function[] reim()  {
						try {
							
							Function mnoz = new FunctionPowloka("exp(z[2]*ln(z[0]^2+z[1]^2)/2 - z[3] * arg(z[0]+i*z[1]))", new Settings()).getFunction();
							Function rePom = new FunctionPowloka("cos(ln(z[0]^2+z[1]^2)*z[3]/2+arg(z[0]+i*z[1])*z[2])", new Settings()).getFunction();
							Function imPom = new FunctionPowloka("sin(ln(z[0]^2+z[1]^2)*z[3]/2+arg(z[0]+i*z[1])*z[2])", new Settings()).getFunction();
							return new Function[] {new FuncMult(mnoz, rePom), new FuncMult(mnoz, imPom)};
						} catch (FunctionExpectedException e) {
							e.printStackTrace();
							throw new IllegalStateException("Funkcja wewnętrzna zle zapisana " + e.messageForUser);
						}
					}

					@Override
					protected Function diffX(int arg, Settings set) {
						if(arg > 2)
							return new FuncNumConst(new Complex(0));
						try {
							if(arg==1) {
								return (new FunctionPowloka("z[1]*pow(z[0], z[1]-1)", new Settings())).getFunction();
							}
							if(arg==2) {
								return (new FunctionPowloka("pow(z[0],z[1])*ln(z[0])", new Settings())).getFunction();
							}
							throw new IllegalArgumentException("??? arg == " + arg);
						} catch (FunctionExpectedException e) {
							throw new IllegalArgumentException(e);
						}
					}
					@Override
					protected Function diffY(int arg, Settings set) {
						if(arg > 2)
							return new FuncNumConst(new Complex(0));
						try {
							if(arg==1) {
								return (new FunctionPowloka("i*z[1]*pow(z[0], z[1]-1)", new Settings())).getFunction();
							}
							if(arg==2) {
								return (new FunctionPowloka("i*pow(z[0],z[1])*ln(z[0])", new Settings())).getFunction();
							}
							throw new IllegalArgumentException("??? arg == " + arg);
						} catch (FunctionExpectedException e) {
							throw new IllegalArgumentException(e);
						}
					}
				};
			}
			catch(FunctionExpectedException e){
				throw new IllegalArgumentException("Program nie powinien tutaj wogóle dojść.");
			}
		}
		
		@Override
		public boolean check(String str) {
			if(str.equals("pow"))
				return true;
			if(str.length() >= 5) {
				if(!str.matches("pow\\{.*\\}"))
					return false;
				try {
					FunctionPowloka f = new FunctionPowloka(str.substring(4, str.length()-1), new Settings());
					if(f.nofArg() == 0 && f.evaluate(new Complex[] {}).y == 0) {
						return true;
					}
				}
				catch(FunctionExpectedException e) {}
			}
			return false;
		}

	}

	static class Identities implements NonStandardFuncStr, Serializable{

		/**
		 * 
		 */
		private static final long serialVersionUID = 6379234201053473498L;

		@Override
		public Function returnFunc(String str) {
			if(str.equals("z") || str.equals("z[0]")) 
				return new FuncWthName(1, "z") {

					/**
					 * 
					 */
					private static final long serialVersionUID = 567997549838106631L;
					@Override
					protected Complex evaluate(Complex[] arg) {
						return arg[0];
					}
					@Override
					public Function putArguments(Function[] args) {
						return args[0];
					}
					@Override
					public Function expand() {
						return this;
					}
					@Override
					protected Function[] reim()  {
						return new Function[] {idChecker.returnFunc("z[0]"), idChecker.returnFunc("z[1]")};
						//return new Function[] { new FuncComp(Re, new Function[] {this}), new FuncComp(Im, new Function[] {this})};
					}
			
					@Override
					protected Function diffX(int arg, Settings set) {
						if(arg != 1)
							return new FuncNumConst(new Complex(0));
						return new FuncNumConst(new Complex(1));
					}
					@Override
					protected Function diffY(int arg, Settings set) {
						if(arg != 1)
							return new FuncNumConst(new Complex(0));
						return new FuncNumConst(Complex.i);					
					}
					@Override
					Function removeDiff() {
						return this;
					}
					@Override
					protected LinkedList<String> checkDepecdencies() {
						return new LinkedList<String>(List.of(this.name));
					}
			};
			if(str.equals("w")) {
				return returnFunc("z[1]");
			}
			int k = Integer.parseInt(str.substring(2, str.length()-1));
			return new FuncWthName(k+1, k == 0 ? "z" : str) {
				/**
				 * 
				 */
				private static final long serialVersionUID = -6900464010257719472L;

				@Override
				protected Complex evaluate(Complex[] arg) {
					return arg[k];
				}

				@Override
				public Function putArguments(Function[] args) {
					return args[k];
				}

				@Override
				public Function expand()  {
					return this;
				}

				@Override
				protected Function[] reim() {
					return new Function[] {idChecker.returnFunc("z["+2*k+"]"), idChecker.returnFunc("z["+(2*k+1)+"]")};
					//return new Function[] { new FuncComp(Re, new Function[] {this}), new FuncComp(Im, new Function[] {this})};
				}

				@Override
				protected Function diffX(int arg, Settings set) {
					if(arg != k)
						return new FuncNumConst(new Complex(0));
					return new FuncNumConst(new Complex(1));
				}
				@Override
				protected Function diffY(int arg, Settings set) {
					if(arg != k)
						return new FuncNumConst(new Complex(0));
					return new FuncNumConst(Complex.i);					
				}
				@Override
				Function removeDiff() {
					return this;
				}

				@Override
				protected LinkedList<String> checkDepecdencies() {
					return new LinkedList<String>(List.of(this.name));
				}
			};

		}

		@Override
		public boolean check(String str) {
			return str.matches("[zw]||(z\\[[0-9]+\\])");
		}
		
		public int returnNumber(String str) {
			//zakłada że wiadomo już, że str jest odpowiedniego rodzaju
			if(str.equals("z"))
				return 0;
			return Integer.parseInt(str.substring(2, str.length()-1));
		}
	}
	
	static class BscVariables implements NonStandardFuncStr{

		/**
		 * 
		 */
		private static final long serialVersionUID = 3865105473422732536L;

		@Override
		public Function returnFunc(String str) {

			if(str.equals("x") || str.equals("x[0]")) 
				return new FuncComp(Re, new Function[] {idChecker.returnFunc("z")});
			if(str.equals("y") || str.equals("y[0]")) 
				return new FuncComp(Im, new Function[] {idChecker.returnFunc("z")});
			//System.out.println(str);
			int k = Integer.parseInt(str.substring(2, str.length()-1));
			switch(str.charAt(0)) {
			case 'x':
				return new FuncComp(Re, new Function[] {idChecker.returnFunc("z["+k+"]")});
			case 'y':
				return new FuncComp(Im, new Function[] {idChecker.returnFunc("z["+k+"]")});
			}
			throw new IllegalArgumentException("Kod nie powinien tutaj dojść. Podano zły argument do funkcji. Argumenty najpierw trzeba sprawdzać checkiem.");
		}

		@Override
		public boolean check(String str) {
			if(str.matches("([xy])||([xy]\\[[0-9]+\\])"))
				return true;
			return false;
		}
		
		public String returnStr(String xOry, int k) {
			if(k<0)
				throw new IllegalArgumentException("k musi byc nieujemne a jest równe "+k+".");
			if(k==0)
				return xOry;
			return xOry+"["+k+"]";
		}
	}
}