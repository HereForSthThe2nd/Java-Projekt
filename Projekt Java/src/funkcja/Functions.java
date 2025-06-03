/*
 * definiowane funkcje i zmienne
 * tutaj też są metody do dodawania nowych funkcji zdefiniowanych przez użytkownika
 * sprawdzanie czy jakaś nazwa to nazwa funkcji / zmiennej zachodzi tutaj
 */

package funkcja;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import Inne.Complex;
import funkcja.Function;

/*
 * NonStandardFuncStr do definiowania funkcji typu ln{α}, dla których istnieje nieskończona ilość możliwych nazw
 */
interface NonStandardFuncStr extends FuncChecker{
	Function returnFunc(String str);
	boolean check(String str);
	default boolean check(Function f) {
		if(f.type==Functions.NAMED)
			if(check(((FuncNamed)f).name))
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
	
	final private static FuncNamed Ln = new FuncDefault(1, "Ln") {
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
	final static FuncNamed ln = new FuncDefault(1,"ln") {
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
	final protected static FuncNamed arg = new FuncDefault(1, "arg") {

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
	final protected static FuncNamed exp = new FuncDefault(1, "exp") {
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
	final protected static FuncNamed sin = new FuncDefault(1, "sin") {

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
	final protected static FuncNamed cos = new FuncDefault(1, "cos") {

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
	final protected static FuncNamed sinh = new FuncDefault(1, "sinh") {

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
	final protected static FuncNamed cosh = new FuncDefault(1, "cosh") {

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
	final protected static FuncNamed Re = new FuncDefault(1, "Re") {
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
	final protected static FuncNamed Im = new FuncDefault(1, "Im") {

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
	final protected static FuncNamed pow = new FuncDefault(2,"pow") {
		//przy upraszczaniu niekoniecznie pamięta gałąź
		@Override
		protected Complex evaluate(Complex[] arg) {
			return arg[0].pow(arg[1]);
		}
		@Override
		protected String write(Settings settings) {
			return "pow";
		}
		@Override
		protected Function[] reim() {
			FunctionPowloka innyZapis;
			try {
				innyZapis = new FunctionPowloka("exp(z[1]*ln(z[0]))", new Settings());
				return innyZapis.getFunction().reim();
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
	
	final protected static FuncNamed diffX = new FuncDefault(1, "diffX") {
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
	
	final protected static FuncNamed diffY = new FuncDefault(1, "diffY") {
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

	
	final protected static FuncNamed e = new FuncConstDefault("e") {
		@Override
		protected Complex evaluate(Complex[] arg) {
			return new Complex(Math.E);
		}

		@Override
		protected Function[] reim()  {
			return new Function [] {this, new FuncNumConst(new Complex(0))};
		}

	};
	final protected static FuncNamed pi = new FuncConstDefault("pi") {
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
		protected String write(Settings settings) {
			if(!settings.writeNeatVar)
				return super.write(settings);
			return "π";
		};
		
	};
	final protected static FuncNamed phi = new FuncConstDefault("phi") {
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
			return super.check(str) || str.equals("φ");
		};
		@Override
		protected String write(Settings settings) {
			if(!settings.writeNeatVar)
				return super.write(settings);
			return "φ";
		};

	};	
	final protected static Function i = new FuncNumConst(Complex.i);
		
	protected final static nameAndValue defaultVar = new nameAndValue(new ArrayList<String>(List.of("e", "pi", "phi")),
			new ArrayList<FuncNamed>(List.of(e ,pi, phi)));
	protected final static nameAndValue defaultFunctions = new nameAndValue(
			new ArrayList<String>(List.of("exp", "Ln","ln", "Re", "Im", "pow", "sin", "cos", "sinh", "cosh","arg", "diffX", "diffY")),
			new ArrayList<FuncNamed>(List.of(exp, Ln, ln, Re, Im, pow, sin, cos, sinh, cosh, arg, diffX, diffY)));
	protected static nameAndValue userFunctions = new nameAndValue();
	protected static nameAndValue userVar = new nameAndValue();
	
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
	
	static protected FuncNamed addNmdFunc(Function f0, String str) throws IncorrectNameException {
		checkNameRequirements(str);
		FuncNamed f = new FuncGivenName(f0, str);
		userFunctions.add(f, f.name);
		return f;
	}

	static Log logChecker = new Log();
	static Pow powChecker = new Pow();
	static  BscVariables varChecker = new BscVariables();
	static Identities idChecker = new Identities();
	

	static protected boolean checkIfNmdFunc(String str) {
		return defaultFunctions.checkIfContained(str) || userFunctions.checkIfContained(str) || logChecker.check(str) || powChecker.check(str);
	}
	
	static protected FuncNamed returnNmdFunc(String str) throws FunctionExpectedException {
		if(!checkIfNmdFunc(str))
			throw new IllegalArgumentException(str + " nie jest nazwą rzadnej zdefiniowanej funkcji.");
		if(logChecker.check(str))
			return logChecker.returnFunc(str);
		if(powChecker.check(str))
			return powChecker.returnFunc(str);
		return defaultFunctions.checkIfContained(str) ? defaultFunctions.functionOf(str) : userFunctions.functionOf(str);
	}

	static protected FuncNamed addVar(Function f0, String str) throws IncorrectNameException {
		checkNameRequirements(str);
		FuncNamed f;
		if(f0.nofArg == 0)
			f = new FuncConstGivenName(str, f0);
		else
			f = new VarGivenName(str, f0);
		userVar.add(f, f.name);
		return f;
	}
	
	static boolean ckeckIfVar(String str) {
		return userVar.checkIfContained(str) || defaultVar.checkIfContained(str) || varChecker.check(str) || idChecker.check(str) || "i".equals(str);
	}
	
	static Function returnVar(String str) {
		if(str.equals("i")) {
			return i;
		}
		if(defaultVar.checkIfContained(str))
			return defaultVar.functionOf(str);
		if(userVar.checkIfContained(str))
			return userVar.functionOf(str);
		if(varChecker.check(str))
			return varChecker.returnFunc(str);
		if(idChecker.check(str))
			return idChecker.returnFunc(str);
		throw new IllegalArgumentException(str + " nie jest nazwą rzadnej zdefiniowanej zmiennej");
	}

	static class nameAndValue{

		ArrayList<String> names = new ArrayList<String>();
		ArrayList<FuncNamed> values = new ArrayList<FuncNamed>();
		nameAndValue(){};
		nameAndValue(ArrayList<String> names,ArrayList<FuncNamed> val){
			if(names.size() != val.size())
				throw new IllegalArgumentException("names oraz val muszą mieć taką samą wielkość.\nPodane names: " + names + "podane val: " + val);
			this.names = names;
			this.values = val;
		}
		int indexOf(String str) {
			for(int i=0;i<names.size();i++) {
				if(values.get(i).check(str))
					return i;
			}
			return -1;
		}
		boolean checkIfContained(String str) {
			return indexOf(str)!=-1;
		}
		FuncNamed functionOf(String str){
			return values.get(indexOf(str));
		}
		void add(FuncNamed val, String str) {
			names.add(str);
			values.add(val);
		}
	}

	static class Log implements NonStandardFuncStr{
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
		public FuncDefault returnFunc(String str){
			if(str.equals("ln")||str.equals("Ln"))
				return (FuncDefault)defaultFunctions.functionOf(str);
			if(!check(str))
				throw new IllegalArgumentException();
			try {
				FunctionPowloka f = new FunctionPowloka(str.substring(3, str.length()-1), new Settings());
				double d = f.evaluate(new Complex[] {}).x;
				return new FuncDefault(1, "ln{"+d+"}") {
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
	
		@Override
		public FuncDefault returnFunc(String str){
			if(!check(str))
				throw new IllegalArgumentException();
			if(str.equals("pow"))
				return (FuncDefault)defaultFunctions.functionOf(str);
			try {
				FunctionPowloka f = new FunctionPowloka(str.substring(4, str.length()-1), new Settings());
				double d = f.evaluate(new Complex[] {}).x;
				return new FuncDefault(2, "pow{"+d+"}") {
					@Override
					protected Complex evaluate(Complex[] arg) {
						return Complex.pow(arg[0], arg[1], d);
					}

					@Override
					protected Function[] reim()  {
						FunctionPowloka innyZapis;
						try {
							innyZapis = new FunctionPowloka("exp(z[1]*ln(z[0]))", new Settings());
							return innyZapis.getFunction().reim();
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

	static class Identities implements NonStandardFuncStr{

		@Override
		public Function returnFunc(String str) {
			if(str.equals("z") || str.equals("z[0]")) 
				return new FuncNamed(1, "z") {

					@Override
					protected Complex evaluate(Complex[] arg) {
						return arg[0];
					}
					@Override
					protected Function putArguments(Function[] args) {
						return args[0];
					}
					@Override
					protected Function expand() {
						return this;
					}
					@Override
					protected Function[] reim()  {
						return new Function[] { new FuncComp(Re, new Function[] {this}), new FuncComp(Im, new Function[] {this})};
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
			};
			if(str.equals("w")) {
				return returnFunc("z[1]");
			}
			int k = Integer.parseInt(str.substring(2, str.length()-1));
			return new FuncNamed(k+1, k == 0 ? "z" : str) {
				@Override
				protected Complex evaluate(Complex[] arg) {
					return arg[k];
				}

				@Override
				protected Function putArguments(Function[] args) {
					return args[k];
				}

				@Override
				protected Function expand()  {
					return this;
				}

				@Override
				protected Function[] reim() {
					return new Function[] { new FuncComp(Re, new Function[] {this}), new FuncComp(Im, new Function[] {this})};
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