package funkcja;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import funkcja.Function;


class FuncNumConst extends Function {
	final static int DODR=1, //1.09, i, 0  //chodzi o to czy trzeba dodawać nawiasy przy wypisywaniu. i (1*i) to jest szczególny przypadek
			DODUR=2, //2i, 2.11i
			UJEMNYPIERWSZY=3,//-1, -i, -1+i
			ZES = 4;//1+i, 1-i
	final Complex a;
	final int form;
	FuncNumConst(Complex a){ 
		super(Functions.NUMCONST, 0);
		this.a = a;
		if(a.x==0 && a.y==1) {
			form = DODR;
			return;
		}
		if(a.x>0 && a.y != 0) {
			form = ZES;
			return;
		}
		if(a.x<0 || (a.y<0 && a.x==0)) {
			form = UJEMNYPIERWSZY;
			return;
		}
		if(a.y>0) {
			form = DODUR;
			return;
		}
		form = DODR;
	}
	
	@Override
	protected Complex evaluate(Complex[] arg) {
		return a;
	}

	@Override
	protected String write(Settings settings) {
		return a.print(settings.doubleAcc);
	}

	@Override
	protected Function putArguments(Function[] args) {
		return this;
	}

	@Override
	protected Bool<Function> expand() {

		return new Bool<Function>(this, false);
	}

	@Override
	protected boolean equals(Function f) {
		if(f.type != this.type)
			return false;
		return this.a.equals( ((FuncNumConst)f).a );
	}

	@Override
	protected Function simplify(Settings setting) {
		return this;
	}	
}

interface NonStandardFuncStr{
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
	
	protected static int countArguments(Function[] f) {
		int max=0;
		for(int i=0;i<f.length; i++) {
			if(f[i].nofArg>max)
				max = f[i].nofArg;
		}
		return max;
	}

	protected static Function[] simplifyAll(Function[] g, Settings settings) {
		Function[] g2 = new Function[g.length];
		for(int i = 0; i<g.length;i++) {
			g2[i] = g[i].simplify(settings);
		}
		return g2;
	}

	protected static boolean equals(Function[] f, Function g[]) {//zwraca prawdę, nawet jeśli drugie jest permutacją pierwszego
		if(f.length != g.length)
			return false;
		ArrayList<Integer> usedIndekses = new ArrayList<Integer>();
		outer:
		for(int i=0;i<f.length;i++) {
			for(int j=0;j<g.length;j++) {
				if(usedIndekses.contains(j))
					continue;
				if(f[i].equals(g[j])) {
					usedIndekses.add(j);
					continue outer;
				}
			return false;
			}
		}
		return true;
	}
	
	protected static boolean equals(ArrayList<Function> f, ArrayList<Function> g) {//zwraca prawdę, nawet jeśli drugie jest permutacją pierwszego
		if(f.size() != g.size())
			return false;
		ArrayList<Integer> usedIndekses = new ArrayList<Integer>();
		outer:
		for(int i=0;i<f.size();i++) {
			for(int j=0;j<g.size();j++) {
				if(usedIndekses.contains(j))
					continue;
				if(f.get(i).equals(g.get(j))) {
					usedIndekses.add(j);
					continue outer;
				}
			return false;
			}
		}
		return true;
	}
	
	protected static Bool<Function[]> expand(Function[] f) {
		Function[] g = new Function[f.length];
		boolean changed = false;
		for(int i=0; i< g.length; i++) {
			 Bool<Function> gib = f[i].expand();
			 g[i] = gib.f;
			 if(gib.bool)
				 changed = true;
		}
		return new Bool<Function[]>(g,changed);
	}

	protected static Complex[] evaluate(Function[] functions, Complex[] args) {
		Complex[] ret = new Complex[functions.length];
		for(int i=0; i<functions.length;i++) {
			ret[i] = functions[i].evaluate(args);
		}
		return ret;
	}

	protected static Function[] putArguments(Function[] functions, Function[] args) {
		Function[] ret = new Function[functions.length];
		for(int i=0; i<functions.length;i++) {
			ret[i] = functions[i].putArguments(args);
		}
		return ret;
	}

	protected static void write(Function[] g) {
		for(int i=0;i<g.length;i++) {
			System.out.println(g[i].write(new Settings()));
		}
	}
	
	protected static boolean argsAreIdentities(Function[] g, int k) {
		//k <= g.lenght
		for(int i=0;i<k;i++) {
			if(!(g[i].equals(returnVar("z["+i+"]"))))
				return false;
		}
		return true;
	}
	
	final private static FuncNamed Ln = new FuncDefault(1, "Ln") {
		//przy upraszczaniu pamięta konkretną gałąz
		@Override
		protected Complex evaluate(Complex[] arg) {
			return Complex.Ln(arg[0]);
		}
	};
	final static FuncNamed ln = new FuncDefault(1,"ln") {
		//przy upraszczaniu niekoniecznie pamięta gałąź
		@Override
		protected Complex evaluate(Complex[] arg) {
			return Complex.Ln(arg[0]);
		}
	};
	final protected static FuncNamed exp = new FuncDefault(1, "exp") {
		@Override
		protected Complex evaluate(Complex[] arg) {
			return Complex.exp(arg[0]);
		}
	};
	final protected static FuncNamed Re = new FuncDefault(1, "Re") {
		@Override
		protected Complex evaluate(Complex[] arg) {
			return new Complex(arg[0].x);
		}
	};
	final protected static FuncNamed Im = new FuncDefault(1, "Im") {

		@Override
		protected Complex evaluate(Complex[] arg) {
			return new Complex(arg[0].y);
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
			if(settings.strictPow)
				return "Pow";
			return "pow";
		}
	};
	final protected static FuncNamed e = new FuncConstDefault("e") {
		@Override
		protected Complex evaluate(Complex[] arg) {
			return new Complex(Math.E);
		}
	};
	final protected static FuncNamed pi = new FuncConstDefault("pi") {
		@Override
		protected Complex evaluate(Complex[] arg) {
			return new Complex(Math.PI);
		}
	};
	final protected static FuncNamed phi = new FuncConstDefault("phi") {
		@Override
		protected Complex evaluate(Complex[] arg) {
			return new Complex((Math.sqrt(5)+1)/2);
		}
	};	
	final protected static Function i = new FuncNumConst(Complex.i);
		
	protected final static nameAndValue<Function> defaultVar = new nameAndValue<Function>(new ArrayList<String>(List.of("e", "pi", "phi", "i")),
			new ArrayList<Function>(List.of(e ,pi, phi, i)));
	protected final static nameAndValue<FuncNamed> defaultFunctions = new nameAndValue<FuncNamed>(new ArrayList<String>(List.of("exp", "Ln","ln", "Re", "Im", "pow")),
			new ArrayList<FuncNamed>(List.of(exp, Ln,ln, Re, Im, pow)));
	protected static nameAndValue<FuncNamed> userFunctions = new nameAndValue<FuncNamed>();
	protected static nameAndValue<FuncNamed> userVar = new nameAndValue<FuncNamed>();
	
	static private void checkNameRequirements(String str) throws IncorrectNameException{
		if(ckeckIfVar(str) || checkIfNmdFunc(str))
			throw new IncorrectNameException("Już istnieje " + (ckeckIfVar(str) ? "zmienna lub stała":"funkcja") + " o takiej nazwie.");
		BlokList blok;
		try {
			blok = new BlokList(str);
		} catch (WrongSyntaxException e) {
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
		return defaultFunctions.checkIfContained(str) || userFunctions.checkIfContained(str) || logChecker.check(str) || logChecker.check(str);
	}
	
	static protected FuncNamed returnNmdFunc(String str) throws WrongSyntaxException {
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
		return userVar.checkIfContained(str) || defaultVar.checkIfContained(str) || varChecker.check(str) || idChecker.check(str);
	}
	
	static Function returnVar(String str) {
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

	static class nameAndValue<rFunc>{

		ArrayList<String> names = new ArrayList<String>();
		ArrayList<rFunc> values = new ArrayList<rFunc>();
		nameAndValue(){};
		nameAndValue(ArrayList<String> names,ArrayList<rFunc> val){
			if(names.size() != val.size())
				throw new IllegalArgumentException("names oraz val muszą mieć taką samą wielkość.\nPodane names: " + names + "podane val: " + val);
			this.names = names;
			this.values = val;
		}
		int indexOf(String str) {
			for(int i=0;i<names.size();i++) {
				if(names.get(i).equals(str))
					return i;
			}
			return -1;
		}
		boolean checkIfContained(String str) {
			return indexOf(str)!=-1;
		}
		rFunc functionOf(String str){
			return values.get(indexOf(str));
		}
		void add(rFunc val, String str) {
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
					catch(WrongSyntaxException e) {}
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
				};
			}
			catch(WrongSyntaxException e){
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
				FunctionPowloka f = new FunctionPowloka(str.substring(3, str.length()-1), new Settings());
				double d = f.evaluate(new Complex[] {}).x;
				return new FuncDefault(2, "pow{"+d+"}") {
					@Override
					protected Complex evaluate(Complex[] arg) {
						return Complex.pow(arg[0], arg[1], d);
					}
					
				};
			}
			catch(WrongSyntaxException e){
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
				catch(WrongSyntaxException e) {}
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
					protected Bool<Function> expand() {

						return new Bool<Function>(this, false);
					}
				};
			if(str.equals("w")) {
				return new FuncNamed(2, "z[1]") {
					@Override
					protected Complex evaluate(Complex[] arg) {
						return arg[1];
					}

					@Override
					protected Function putArguments(Function[] args) {
						return args[1];
					}

					@Override
					protected Bool<Function> expand() {
						return new Bool<Function>(this, false);
					}
				};
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
				protected Bool<Function> expand() {
					return new Bool<Function>(this, false);
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
				return new FuncComp(Re, new Function[] {idChecker.returnFunc("z")});
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
	
	static public void main(String[] args) {
		System.out.println("0 błędów");
		System.out.println(ckeckIfVar("pi"));
	}

}