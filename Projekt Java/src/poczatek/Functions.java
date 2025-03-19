package poczatek;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;


class FuncNumConst extends Function {
	final static int DODR=1, //1.09, i, 0  //chodzi o to czy trzeba dodawać nawiasy przy wypisywaniu
			DODUR=2, //2i, 2.11i
			UJ=3,//-1, -i
			ZES = 4;//1+i
	final Complex a;
	final int form;
	FuncNumConst(Complex a){
		super(Functions.NUMCONST, 0);
		this.a = a;
		if(a.x==0 && a.y==1) {
			form = DODR;
			return;
		}
		if(a.x!=0 && a.y != 0) {
			form = ZES;
			return;
		}
		if(a.x<0 || a.y<0) {
			form = UJ;
			return;
		}
		if(a.y>0) {
			form = DODUR;
			return;
		}
		form = DODR;
	}
	
	@Override
	public Complex evaluate(Complex[] arg) {
		return a;
	}

	@Override
	public String write(PrintSettings settings) {
		return a.print(settings.doubleAcc);
	}

	@Override
	public Function putArguments(Function[] args) {
		return this;
	}

	@Override
	public Bool<Function> expand() {

		return new Bool<Function>(this, false);
	}

	@Override
	public boolean equals(Function f) {
		if(f.type != this.type)
			return false;
		return this.a.equals( ((FuncNumConst)f).a );
	}

	@Override
	public Function simplify() {
		return this;
	}	
}


@Deprecated
class FuncPow extends Function{
	Function f;
	Function g;
	FuncPow(Function a, Function b){
		super(Functions.POW, Functions.countArguments(new Function[] {a,b}));
		this.f = a;
		this.g = b;
	}
	@Override
	public Complex evaluate(Complex[] arg) {
		return f.evaluate(arg).pow(g.evaluate(arg));
	}
	@Override
	public String write(PrintSettings settings) {
		return "("+f.write(settings)+")^("+g.write(settings)+")";
	}
	@Override
	public Function putArguments(Function[] args) {
		return new FuncPow(f.putArguments(args), g.putArguments(args));
	}
	@Override
	public Bool<Function> expand() {
		Bool<Function> fExp = f.expand();
		Bool<Function> gExp = g.expand();
		return new Bool<Function> (new FuncPow(fExp.f, gExp.f), fExp.bool || gExp.bool);
	}
	@Override
	public boolean equals(Function f) {
		if(f.type == this.type)
			return this.f.equals(((FuncPow)f).f) && this.g.equals(((FuncPow)f).g);
		return false;
	}
	@Override
	public Function simplify() {
		// TODO Auto-generated method stub
		return this;
	}
}


public class Functions {
	final static int 
			NUMCONST = 1,
			NAMED = 2,
			ADD=3,
			MULT=4,
			POW=5,
			COMPOSITE=6;
	
	static int countArguments(Function[] f) {
		int max=0;
		for(int i=0;i<f.length; i++) {
			if(f[i].nofArg>max)
				max = f[i].nofArg;
		}
		return max;
	}

	public static Function[] simplifyAll(Function[] g) {
		Function[] g2 = new Function[g.length];
		for(int i = 0; i<g.length;i++) {
			g2[i] = g[i].simplify();
		}
		return g2;
	}

	public static boolean equals(Function[] f, Function g[]) {//zwraca prawdę, nawet jeśli drugie jest permutacją pierwszego
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
	
	public static Bool<Function[]> expand(Function[] f) {
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
	final private static FuncNamed Ln = new FuncDefault(1, "Ln") {
		//przy upraszczaniu pamięta konkretną gałąz
		@Override
		public Complex evaluate(Complex[] arg) {
			return Complex.Ln(arg[0]);
		}
	};	
	final private static FuncNamed exp = new FuncDefault(1, "exp") {
		@Override
		public Complex evaluate(Complex[] arg) {
			return Complex.exp(arg[0]);
		}
	};
	final private static FuncNamed Re = new FuncDefault(1, "Re") {
		@Override
		public Complex evaluate(Complex[] arg) {
			return new Complex(arg[0].x);
		}
	};
	final private static FuncNamed Im = new FuncDefault(1, "Im") {

		@Override
		public Complex evaluate(Complex[] arg) {
			return new Complex(arg[0].y);
		}
	};
	final static FuncNamed Pow = new FuncDefault(2,"Pow") {
		//przy upraszczaniu pamięta gałąz
		@Override
		public Complex evaluate(Complex[] arg) {
			return arg[0].pow(arg[1]);
		}
	};
	final static FuncNamed e = new FuncConstDefault("e") {
		@Override
		public Complex evaluate(Complex[] arg) {
			return new Complex(Math.E);
		}
	};
	final static FuncNamed pi = new FuncConstDefault("pi") {
		@Override
		public Complex evaluate(Complex[] arg) {
			return new Complex(Math.PI);
		}
	};
	final static FuncNamed phi = new FuncConstDefault("phi") {
		@Override
		public Complex evaluate(Complex[] arg) {
			return new Complex((Math.sqrt(5)+1)/2);
		}
	};	
	final static Function i = new FuncNumConst(Complex.i);
	
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
	
	public final static nameAndValue<Function> defaultVar = new nameAndValue<Function>(new ArrayList<String>(List.of("e", "pi", "phi", "i")),
			new ArrayList<Function>(List.of(e ,pi, phi, i)));
	public final static nameAndValue<FuncNamed> defaultFunctions = new nameAndValue<FuncNamed>(new ArrayList<String>(List.of("exp", "Ln", "Re", "Im")),
			new ArrayList<FuncNamed>(List.of(exp, Ln, Re, Im)));
	public static nameAndValue<FuncNamed> userFunctions = new nameAndValue<FuncNamed>();
	public static nameAndValue<FuncNamed> userVar = new nameAndValue<FuncNamed>();
	
	static void addNmdFunc(FuncNamed f) {
		if(ckeckIfVar(f.name) || checkIfNmdFunc(f.name))
			throw new IllegalArgumentException("Już istnieje " + (ckeckIfVar(f.name) ? "zmienna":"funkcja") + " o takiej nazwie.");
		userFunctions.add(f, f.name);
	}
	
	static boolean checkIfNmdFunc(String str) {
		return defaultFunctions.checkIfContained(str) || userFunctions.checkIfContained(str);
	}
	
	static FuncNamed returnNmdFunc(String str) {
		if(!checkIfNmdFunc(str))
			throw new IllegalArgumentException(str + " nie jest nazwą rzadnej zdefiniowanej funkcji.");
		return defaultFunctions.checkIfContained(str) ? defaultFunctions.functionOf(str) : userFunctions.functionOf(str);
	}

	static void addVar(FuncNamed f) {
		if(ckeckIfVar(f.name) || checkIfNmdFunc(f.name))
			throw new IllegalArgumentException("Już istnieje " + (ckeckIfVar(f.name) ? "zmienna":"funkcja") + " o takiej nazwie.");
		userVar.add(f, f.name);
	}
	
	static boolean ckeckIfVar(String str) {
		if(str.matches("([xyzw])||([xyz]\\[[0-9]+\\])"))
			return true;
		return userVar.checkIfContained(str) || defaultVar.checkIfContained(str);
	}
	
	static Function returnVar(String str) {
		if(!ckeckIfVar(str))
			throw new IllegalArgumentException(str + " nie jest nazwą rzadnej zdefiniowanej zmiennej");
		if(defaultVar.checkIfContained(str))
			return defaultVar.functionOf(str);
		if(userVar.checkIfContained(str))
			return userVar.functionOf(str);

		if(str.equals("z") || str.equals("z[0]")) 
			return new FuncNamed(1, "z") {

				@Override
				public Complex evaluate(Complex[] arg) {
					return arg[0];
				}
				@Override
				public Function putArguments(Function[] args) {
					return args[0];
				}
				@Override
				public Bool<Function> expand() {

					return new Bool<Function>(this, false);
				}
			};
		if(str.equals("x") || str.equals("x[0]")) 
			return new FuncNamed(1, "x") {
				@Override
				public Complex evaluate(Complex[] arg) {
					return new Complex(arg[0].x,0);
				}

				@Override
				public Function putArguments(Function[] args) {
					return new FuncComp(Re, new Function[] {args[0]});
				}

				@Override
				public Bool<Function> expand() {

					return new Bool<Function>(this, false);
				}			
			};
		if(str.equals("y") || str.equals("y[0]")) 
			return new FuncNamed(1, "y") {

				@Override
				public Complex evaluate(Complex[] arg) {
					return new Complex(0,arg[0].y);
				}

				@Override
				public Function putArguments(Function[] args) {
					return new FuncComp(Im, new Function[] {args[0]});
				}

				@Override
				public Bool<Function> expand() {

					return new Bool<Function>(this, false);
				}
			};
		if(str.equals("w")) {
			return new FuncNamed(2, "z[1]") {
				@Override
				public Complex evaluate(Complex[] arg) {
					return arg[1];
				}

				@Override
				public Function putArguments(Function[] args) {
					return args[1];
				}

				@Override
				public Bool<Function> expand() {
					return new Bool<Function>(this, false);
				}
			};
		}
		//System.out.println(str);
		int k = Integer.parseInt(str.substring(2, str.length()-1));
		switch(str.charAt(0)) {
		case 'z':
			return new FuncNamed(k+1, str) {
				@Override
				public Complex evaluate(Complex[] arg) {
					return arg[k];
				}

				@Override
				public Function putArguments(Function[] args) {
					return args[k];
				}

				@Override
				public Bool<Function> expand() {
					return new Bool<Function>(this, false);
				}
			};
		case 'x':
			return new FuncNamed(k+1, str) {


				@Override
				public Complex evaluate(Complex[] arg) {
					return new Complex(arg[k].x,0);
				}

				@Override
				public Function putArguments(Function[] args) {
					return new FuncComp(Re, new Function[] {args[k]});
				}

				@Override
				public Bool<Function> expand() {

					return new Bool<Function>(this, false);
				}
			};
		case 'y':
			return new FuncNamed(k+1, "y") {

				@Override
				public Complex evaluate(Complex[] arg) {
					return new Complex(0,arg[k].y);
				}

				@Override
				public Function putArguments(Function[] args) {
					return new FuncComp(Im, new Function[] {args[k]});
				}

				@Override
				public Bool<Function> expand() {

					return new Bool<Function>(this, false);
				}
			};
		}
		System.out.println("Kod nigdy nie powinien tutaj dojść. str: " + str);
		return null;
	}

	static public void main(String[] args) {
		System.out.println("0 błędów");
		System.out.println(ckeckIfVar("pi"));
	}
}