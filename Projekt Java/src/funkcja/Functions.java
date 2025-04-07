/*
 * definiowane funkcje i zmienne
 * tutaj też są metody do dodawania nowych funkcji zdefiniowanych przez użytkownika
 * sprawdzanie czy jakaś nazwa to nazwa funkcji / zmiennej zachodzi tutaj
 */

package funkcja;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import funkcja.Function;
import ogolne.Complex;
import ogolne.IncorrectNameException;
import ogolne.Settings;
import ogolne.WrongSyntaxException;

/*
 * FuncReturn do zamieniania stringów na funkcje, definiowania funkcji bazowych
 */

abstract class FuncReturn implements FuncChecker{
	final int nofArgs;
	final String name;
	FuncReturn(int nofArgs, String name){
		this.name = name;
		this.nofArgs = nofArgs;
	}
	abstract Function returnFunc(Function[] args);
	final boolean check(String str) {
		return str.equals(name);
	}
	@Override
	public boolean check(Function f) {
		if(f.type==Functions.NAMED)
			if(check(((FuncNamed)f).name))
				return true;
		return false;
	};
}

abstract class FuncReturnSpecial implements FuncChecker{
	abstract FuncReturn returnFuncReturn(String str);
	Function returnFunc(String str, Function[] args) {
		return returnFunc(str, args);
	}
	abstract boolean check(String str);
	@Override
	public
	boolean check(Function f) {
		if(f.type==Functions.NAMED)
			if(check(((FuncNamed)f).name))
				return true;
		return false;
	};
}

abstract class VarReturn implements FuncChecker{
	final String name;
	VarReturn(String str){
		this.name = str;
	}
	abstract Function returnFunc();
	boolean check(String str) {
		return str.equals(name);
	};
	@Override
	public boolean check(Function f) {
		if(f.type==Functions.NAMED)
			if(check(((FuncNamed)f).name))
				return true;
		return false;
	};
}

abstract class VarReturnSpecial implements FuncChecker{
	abstract Function returnFunc(String str);
	abstract boolean check(String str);
	@Override
	public boolean check(Function f) {
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
			SPECIAL = 6;
	
	final protected static FuncReturn Ln  = new FuncReturn(1, "Ln"){
		@Override
		public Function returnFunc(Function[] args) {
			return new FuncDefault(name, nofArgs, args) {
				//przy upraszczaniu pamięta konkretną gałąz
				@Override
				protected Complex evaluate(Complex[] arg) {
					return Complex.Ln(args[0].evaluate(arg));
				}

				@Override
				protected Function re() {
					FunctionPowloka r;
					try {
						r = new FunctionPowloka("(z[0]^2+z[1]^2)^(1/2)", new Settings());
						return Ln.returnFunc(new Function[] {r.f.putArguments(FuncMethods.reim(args))});
					} catch (WrongSyntaxException e) {
						e.printStackTrace();
						throw new IllegalStateException(e);
					}
				}

				@Override
				protected Function im() {
					return arg.returnFunc(args);
				}

			};
		}
	};
	
	
	final protected static FuncReturn ln  = new FuncReturn(1, "ln"){
		//przy upraszczaniu niekoniecznie pamięta gałąź

		@Override
		public Function returnFunc(Function[] args) {
			return new FuncDefault(name, nofArgs, args){
				@Override
				protected Complex evaluate(Complex[] arg) {
					return Complex.Ln(args[0].evaluate(arg));
				}

				@Override
				protected Function re() {
					FunctionPowloka r;
					try {
						r = new FunctionPowloka("(z[0]^2+z[1]^2)^(1/2)", new Settings());
						return Ln.returnFunc(new Function[] {r.f.putArguments(FuncMethods.reim(args))});
					} catch (WrongSyntaxException e) {
						e.printStackTrace();
						throw new IllegalStateException(e);
					}
				}

				@Override
				protected Function im() {
					return arg.returnFunc(args);
				}

			};
		}
	};
	final protected static FuncReturn arg = new FuncReturn(1, "arg"){

		@Override
		public Function returnFunc(Function[] args) {
			return new FuncDefault(name, nofArgs, args){
				@Override
				protected Complex evaluate(Complex[] arg) {
					return new Complex (args[0].evaluate(arg).arg());
				}

				@Override
				protected Function re()  {
					return this;
				}

				@Override
				protected Function im() {
					return new FuncNumConst(new Complex(0));
				}

			};
		}

	};
	final protected static FuncReturn exp = new FuncReturn(1, "exp"){
		

		@Override
		public Function returnFunc(Function[] args) {
			return new FuncDefault(name, nofArgs, args){
				@Override
				protected Complex evaluate(Complex[] arg) {
					return Complex.exp(args[0].evaluate(arg));
				}

				@Override
				protected Function re() {
					FunctionPowloka pom;
					try {
						pom = new FunctionPowloka("exp(z[0])*cos(z[1])", new Settings());
					} catch (WrongSyntaxException e) {
						e.printStackTrace();
						throw new IllegalStateException(e);
					}
					return pom.f.putArguments(FuncMethods.reim(args));
				}

				@Override
				protected Function im() {
					FunctionPowloka pom;
					try {
						pom = new FunctionPowloka("exp(z[0])*sin(z[1])", new Settings());
					} catch (WrongSyntaxException e) {
						e.printStackTrace();
						throw new IllegalStateException(e);
					}
					return pom.f.putArguments(FuncMethods.reim(args));
				}

			};
		}
	};
	final protected static FuncReturn sin = new FuncReturn(1, "sin"){


		@Override
		public Function returnFunc(Function[] args) {
			return new FuncDefault(name, nofArgs, args){		
				
				@Override
				protected Complex evaluate(Complex[] arg) {
					return Complex.sin(args[0].evaluate(arg));
				}
	
				@Override
				protected Function re() {
					try {
						FunctionPowloka pom = new FunctionPowloka("cosh(z[1])*sin(z[0])", new Settings());
						return pom.f.putArguments(FuncMethods.reim(args));
					} catch (WrongSyntaxException e) {
						e.printStackTrace();
						throw new IllegalStateException(e);
					}
				}
	
				@Override
				protected Function im() {
					try {
						FunctionPowloka pom = new FunctionPowloka("sinh(z[1])*cos(z[0])", new Settings());
						return pom.f.putArguments(FuncMethods.reim(args));
					} catch (WrongSyntaxException e) {
						e.printStackTrace();
						throw new IllegalStateException(e);
					}
				}
			};
		}		
	};
	final protected static FuncReturn cos = new FuncReturn(1, "cos"){


		@Override
		public Function returnFunc(Function[] args) {
			return new FuncDefault(name, nofArgs, args){
				@Override
				protected Complex evaluate(Complex[] arg) {
					return Complex.cos(args[0].evaluate(arg));
				}

				@Override
				protected Function re() {
					try {
						FunctionPowloka pom = new FunctionPowloka("cosh(z[1])*cos(z[0])", new Settings());
						return pom.f.putArguments(FuncMethods.reim(args));
					} catch (WrongSyntaxException e) {
						e.printStackTrace();
						throw new IllegalStateException(e);
					}
				}

				@Override
				protected Function im() {
					try {
						FunctionPowloka pom = new FunctionPowloka("-sinh(z[1])*sin(z[0])", new Settings());
						return pom.f.putArguments(FuncMethods.reim(args));
					} catch (WrongSyntaxException e) {
						e.printStackTrace();
						throw new IllegalStateException(e);
					}
				}
			};
		}
	};
	final protected static FuncReturn sinh = new FuncReturn(1, "sinh"){


		@Override
		public Function returnFunc(Function[] args) {
			return new FuncDefault(name, nofArgs, args){
				@Override
				protected Complex evaluate(Complex[] arg) {
					return Complex.sinh(args[0].evaluate(arg));
				}

				@Override
				protected Function re() {
					try {
						FunctionPowloka pom = new FunctionPowloka("sinh(z[0])*cos(z[1])", new Settings());
						return pom.f.putArguments(FuncMethods.reim(args));
					} catch (WrongSyntaxException e) {
						e.printStackTrace();
						throw new IllegalStateException(e);
					}
				}

				@Override
				protected Function im() {
					try {
						FunctionPowloka pom = new FunctionPowloka("cosh(z[0])*sin(z[1])", new Settings());
						return pom.f.putArguments(FuncMethods.reim(args));
					} catch (WrongSyntaxException e) {
						e.printStackTrace();
						throw new IllegalStateException(e);
					}
				}

			};
		}
	};
	final protected static FuncReturn cosh = new FuncReturn(1, "cosh"){


		@Override
		public Function returnFunc(Function[] args) {
			return new FuncDefault(name, nofArgs, args){		@Override
				protected Complex evaluate(Complex[] arg) {
				return Complex.cosh(args[0].evaluate(arg));
			}

			@Override
			protected Function re() {
				try {
					FunctionPowloka pom = new FunctionPowloka("cosh(z[0])*cos(z[1])", new Settings());
					return pom.f.putArguments(FuncMethods.reim(args));
				} catch (WrongSyntaxException e) {
					e.printStackTrace();
					throw new IllegalStateException(e);
				}
			}

			@Override
			protected Function im() {
				try {
					FunctionPowloka pom = new FunctionPowloka("sinh(z[0])*sin(z[1])", new Settings());
					return pom.f.putArguments(FuncMethods.reim(args));
				} catch (WrongSyntaxException e) {
					e.printStackTrace();
					throw new IllegalStateException(e);
				}
			}
};
		}
	};
	final protected static FuncReturn Re = new FuncReturn(1, "Re"){


		@Override
		public Function returnFunc(Function[] args) {
			return new FuncDefault(name, nofArgs, args){
				@Override
				protected Complex evaluate(Complex[] arg) {
					return new Complex(args[0].evaluate(arg).x);
				}

				@Override
				protected Function re()  {
					return this;
				}

				@Override
				protected Function im() {
					return new FuncNumConst(new Complex(0));
				}
			};
		}
	};
	final protected static FuncReturn Im = new FuncReturn(1, "Im"){

	
		@Override
		public Function returnFunc(Function[] args) {
			return new FuncDefault(name, nofArgs, args){
				@Override
				protected Complex evaluate(Complex[] arg) {
					return new Complex(args[0].evaluate(arg).y);
				}
	
				@Override
				protected Function re()  {
					return this;
				}
	
				@Override
				protected Function im() {
					return new FuncNumConst(new Complex(0));
				}
	
			};
		}
	};

	final protected static FuncReturn pow = new FuncReturn(2, "pow"){
		

		@Override
		public Function returnFunc(Function[] args) {
			if(args.length < 2) {
				throw new IllegalArgumentException("Liczba argumentów mniejsza od 2. argumenty: " + args);
			}
			return new FuncDefault(name, nofArgs, args){
				//przy upraszczaniu niekoniecznie pamięta gałąź
				@Override
				protected Complex evaluate(Complex[] arg) {
					return args[0].evaluate(arg).pow(args[1].evaluate(arg));
				}
				@Override
				protected String write(Settings set) {
					if(set.writePow) {
						String str = "";
						if(args[0].type == Functions.ADD || args[0].type == Functions.MULT) {
							str += "(" + args[0].write(set) + ")";
						}else {
							if(args[0].type == Functions.NUMCONST && !(((FuncNumConst)args[0]).form == FuncNumConst.DODR)) {
								str += "(" + args[0].write(set) + ")"; 
							}else {
								str += args[0].write(set);
							}
						}
						str += " ^ ";
						if(args[1].type == Functions.ADD || args[1].type == Functions.MULT || (Functions.pow.check( args[1] ))) {
							str += "(" + args[1].write(set) + ")";
						}else {
							if(args[1].type == Functions.NUMCONST && !(((FuncNumConst)args[1]).form == FuncNumConst.DODR)) {
								str += "(" + args[1].write(set) + ")"; 
							}else {
								str += args[1].write(set);
							}
						}
						return str;
					}
					return super.write(set);
				}
				@Override
				protected Function re() {
					FunctionPowloka innyZapis;
					try {
						innyZapis = new FunctionPowloka("exp(z[1]*ln(z[0]))", new Settings());
						return innyZapis.f.re().putArguments(args);
					} catch (WrongSyntaxException e) {
						e.printStackTrace();
						throw new IllegalStateException(e);
					}
				}

				@Override
				protected Function im() {
					FunctionPowloka innyZapis;
					try {
						innyZapis = new FunctionPowloka("exp(z[1]*ln(z[0]))", new Settings());
						return innyZapis.f.im().putArguments(args);
					} catch (WrongSyntaxException e) {
						e.printStackTrace();
						throw new IllegalStateException(e);
					}
				}

			};
		}
	};
	
	final static FuncReturnSpecial logChecker = new FuncReturnSpecial() {
		
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
					catch(WrongSyntaxException e) {}
				}
				return false;
			}
	
		@Override
		FuncReturn returnFuncReturn(String str) {
			if(str.equals("ln"))
				return ln;
			if(str.equals("Ln"))
				return Ln;
			try {
				FunctionPowloka f = new FunctionPowloka(str.substring(3, str.length()-1), new Settings());
				double d = f.evaluate(new Complex[] {}).x;
				return new FuncReturn(1, "ln{"+d+"}") {
					
					@Override
					Function returnFunc(Function[] args) {
						return new FuncDefault(name, nofArgs, args) {
							@Override
							protected Complex evaluate(Complex[] arg) {
								return Complex.ln(args[0].evaluate(arg), d);
							}

							@Override
							protected Function re() {
								FunctionPowloka r;
								try {
									r = new FunctionPowloka("(x^2+y^2)^(1/2)", new Settings());
									return ln.returnFunc(new Function[] {r.f});
								} catch (WrongSyntaxException e) {
									e.printStackTrace();
									throw new IllegalStateException(e);
								}
							}

							@Override
							protected Function im() {
								return arg.returnFunc(new Function[] {idChecker.returnFunc("z")});
							}
						};
					}
				};
			}
			catch(WrongSyntaxException e){
				throw new IllegalArgumentException("Program nie powinien tutaj wogóle dojść.");
			}
		}
	};
	
	final static FuncReturnSpecial powChecker = new FuncReturnSpecial() {
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
					catch(WrongSyntaxException e) {}
				}
				return false;
				}
		
			@Override
			FuncReturn returnFuncReturn(String str) {
				if(str.equals("pow"))
					return pow;
				try {
					FunctionPowloka f = new FunctionPowloka(str.substring(4, str.length()-1), new Settings());
					double d = f.evaluate(new Complex[] {}).x;
					return new FuncReturn(2, "pow{"+d+"}") {
						
						@Override
						Function returnFunc(Function[] args) {
							return new FuncDefault(name, nofArgs, args) {
								@Override
								protected Complex evaluate(Complex[] arg) {
									return Complex.pow(args[0].evaluate(arg), args[1].evaluate(arg), d);
								}

								@Override
								protected Function re() {
									FunctionPowloka innyZapis;
									try {
										innyZapis = new FunctionPowloka("exp(z[1]*ln(z[0]))", new Settings());
										return innyZapis.f.re();
									} catch (WrongSyntaxException e) {
										e.printStackTrace();
										throw new IllegalStateException(e);
									}
								}

								@Override
								protected Function im() {
									FunctionPowloka innyZapis;
									try {
										innyZapis = new FunctionPowloka("exp(z[1]*ln(z[0]))", new Settings());
										return innyZapis.f.im();
									} catch (WrongSyntaxException e) {
										e.printStackTrace();
										throw new IllegalStateException(e);
									}
								}
							};
						}
					};
				}
				catch(WrongSyntaxException e){
					throw new IllegalArgumentException("Program nie powinien tutaj wogóle dojść.");
				}
			}
		};

	final static  BscVariables varChecker = new BscVariables();
	
	final static Identities idChecker = new Identities();
		
	final protected static VarReturn e =new VarReturn("e") {
		
		@Override
		Function returnFunc() {
			return 	new FuncConstDefault(name) {
				@Override
				protected Complex evaluate(Complex[] arg) {
					return new Complex(Math.E);
				}

				@Override
				protected Function re() {
					return this;
				}

				@Override
				protected Function im() {
					return new FuncNumConst(new Complex(0));
				}
			};
		}
	}; 
			
	final protected static VarReturn pi =new VarReturn("pi") {
		
		@Override
		Function returnFunc() {
			return 	new FuncConstDefault(name) {
				@Override
				protected Complex evaluate(Complex[] arg) {
					return new Complex(Math.PI);
				}

				@Override
				protected Function re() {
					return this;
				}

				@Override
				protected Function im() {
					return new FuncNumConst(new Complex(0));
				}
			};
		}
	}; 	
	
	final protected static VarReturn phi =new VarReturn("phi") {
		
		@Override
		Function returnFunc() {
			return 	new FuncConstDefault(name) {
				@Override
				protected Complex evaluate(Complex[] arg) {
					return new Complex((Math.sqrt(5) + 1) / 2);
				}

				@Override
				protected Function re() {
					return this;
				}

				@Override
				protected Function im() {
					return new FuncNumConst(new Complex(0));
				}
			};
		}
	}; 	
	final protected static VarReturn i =new VarReturn("i") {
		
		@Override
		Function returnFunc() {
			return 	new FuncConstDefault(name) {
				@Override
				protected Complex evaluate(Complex[] arg) {
					return Complex.i;
				}

				@Override
				protected Function re() {
					return this;
				}

				@Override
				protected Function im() {
					return new FuncNumConst(new Complex(1));
				}
			};
		}
	}; 			
	
	final private static ArrayList<VarReturn> defaultVar = new ArrayList<VarReturn>(List.of(e,pi,phi,i));//new ArrayList<String>(List.of("e", "pi", "phi", "i"));
	final private static ArrayList<FuncReturn> defaultFunctions = new ArrayList<FuncReturn>(List.of(exp, Ln,ln, Re, Im, pow, sin, cos, sinh, cosh, arg));
	private static ArrayList<FuncReturn> userFunctions = new ArrayList<FuncReturn>();
	private static ArrayList<VarReturn> userVar = new ArrayList<VarReturn>();
	
	static private void checkNameRequirements(String str) throws IncorrectNameException{
		//dotyczy dodawania nowych funkcji przez użytkownika
		if(ckeckIfVar(str) || checkIfNmdFunc(str))
			throw new IncorrectNameException("Już istnieje " + (ckeckIfVar(str) ? "zmienna lub stała":"funkcja") + " o takiej nazwie.");
		BlokList blok;
		try {
			blok = new BlokList(str);
		} catch (WrongSyntaxException e) {
			throw new IncorrectNameException("Nie udało się przetworzyć takiej nazwy: " + e.messageForUser);
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
	
	static protected void addNmdFunc(Function f0, String str) throws IncorrectNameException {
		checkNameRequirements(str);
		userFunctions.add(new FuncReturn(f0.nofArg, str) {
			@Override
			Function returnFunc(Function[] args) {
				return new FuncGivenName(f0, str, nofArgs, args);
			}
		});
	}
	
	
	
	
	private static boolean checkIfContained(List<FuncReturn> lista, String str) {
		for(FuncReturn fr : lista) {
			if(fr.check(str))
				return true;
		}
		return false;
	}
	
	private static Function functionOf(List<FuncReturn> lista, String str, Function[] args) {
		for(FuncReturn fr : lista) {
			if(fr.check(str))
				return fr.returnFunc(args);
		}
		throw new IllegalArgumentException("argument lista trzeba przepuścić przez checkIfContained nim się wywoła z nim tą funkcję.");
	}
	
	private static boolean checkIfVarContained(List<VarReturn> lista, String str) {
		for(VarReturn fr : lista) {
			if(fr.check(str))
				return true;
		}
		return false;
	}
	
	private static Function functionOf(List<VarReturn> lista, String str) {
		for(VarReturn fr : lista) {
			if(fr.check(str))
				return fr.returnFunc();
		}
		throw new IllegalArgumentException("argument lista trzeba przepuścić przez checkIfContained nim się wywoła z nim tą funkcję.");
	}
	
	
	static protected boolean checkIfNmdFunc(String str) {
		return checkIfContained(defaultFunctions, str)|| checkIfContained(userFunctions, str) || pow.check(str) || ln.check(str);
	}
	
	static protected FuncReturn returnNmdFuncReturner(String str) {
		for(FuncReturn i : defaultFunctions) {
			if(i.check(str))
				return i;
		}
		for(FuncReturn i : userFunctions) {
			if(i.check(str))
				return i;
		}
		if(powChecker.check(str))
			return powChecker.returnFuncReturn(str);
		if(logChecker.check(str))
			return logChecker.returnFuncReturn(str);
		throw new IllegalArgumentException("'str' trzeba najpierw przepuśić przez 'checkIfNmdFunction'. str: " + str + ".");
	}
	
	static protected Function returnNmdFunc(String str, Function[] args) throws WrongSyntaxException {
		return returnNmdFuncReturner(str).returnFunc(args);
	}

	
	static protected void addVar(Function f0, String str) throws IncorrectNameException {
		checkNameRequirements(str);
		userVar.add(new VarReturn(str) {
			@Override
			Function returnFunc() {
				return new VarGivenName(str, f0);
			}
		});
	}
	
	static boolean ckeckIfVar(String str) {
		return checkIfVarContained(userVar, str) || checkIfVarContained(defaultVar, str) || varChecker.check(str) || idChecker.check(str);
	}
	
	static Function returnVar(String str) {
		if(checkIfVarContained(defaultVar, str))
			return functionOf(defaultVar, str);
		if(checkIfVarContained(userVar, str))
			return functionOf(userVar, str);
		if(varChecker.check(str))
			return varChecker.returnFunc(str);
		if(idChecker.check(str))
			return idChecker.returnFunc(str);
		throw new IllegalArgumentException(str + " nie jest nazwą rzadnej zdefiniowanej zmiennej");
	}

	static class Identities extends VarReturnSpecial{

		@Override
		public Function returnFunc(String str) {
			if(str.equals("z")) 
				return returnFunc("z[0]");
			if(str.equals("w"))
				return returnFunc("z[1]");
			int k = Integer.parseInt(str.substring(2, str.length()-1));
			return new VarDefault(k+1, k == 0 ? "z" : str) {
				@Override
				protected Complex evaluate(Complex[] arg) {
					return arg[k];
				}

				@Override
				protected Function putArguments(Function[] args) {
					return args[k];
				}

				@Override
				protected Function re()  {
					return varChecker.returnFunc("x["+k+"]");
				}

				@Override
				protected Function im() {
					return varChecker.returnFunc("y["+k+"]");
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
	
	static class BscVariables extends VarReturnSpecial{

		@Override
		public Function returnFunc(String str) {

			if(str.equals("x")) 
				return returnFunc("x[0]");
			if(str.equals("y")) 
				return returnFunc("y[0]");
			//System.out.println(str);
			int k = Integer.parseInt(str.substring(2, str.length()-1));
			switch(str.charAt(0)) {
			case 'x':
				return new VarDefault(1, k == 0? "x" : "x["+k+"]") {
					
					@Override
					protected Function re() {
						return this;
					}
					
					@Override
					protected Function putArguments(Function[] args) {
						return Re.returnFunc(new Function[] {args[k]});
					}
					
					@Override
					protected Function im() {
						return new FuncNumConst(new Complex(0));
					}
										
					@Override
					protected Complex evaluate(Complex[] arg) {
						return new Complex(arg[k].x);
					}
				};
			case 'y':
				return new VarDefault(1, k == 0? "y" : "y["+k+"]") {
					
					@Override
					protected Function re() {
						return new FuncNumConst(new Complex(0));
					}
					
					@Override
					protected Function putArguments(Function[] args) {
						return Im.returnFunc(new Function[] {args[k]});
					}
					
					@Override
					protected Function im() {
						return this;
					}
									
					@Override
					protected Complex evaluate(Complex[] arg) {
						return new Complex(arg[k].y);
					}
				};
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