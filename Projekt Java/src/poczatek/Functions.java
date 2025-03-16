package poczatek;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;


class FuncComp extends Function {
	FuncNamed f;
	Function[] g;
	public FuncComp(FuncNamed f, Function[] g) {
		super(Functions.COMPOSITE, Functions.countArguments(g));
		this.f = f;
		if(g.length<f.nofArg)
			throw new IllegalArgumentException("Funkcja " + f.name + " musi przyjmować " + f.nofArg + " argumenty a nie "+g.length);
		Function[] args = new Function[f.nofArg];
		for(int i = 0;i<f.nofArg;i++) {
			args[i] = g[i];
		}
		this.g = args;
	}
	@Override
	public Complex evaluate(Complex[] arg) {
		return f.evaluate(Functions.evaluate(g, arg));
	}
	@Override
	public String write(PrintSettings settings) {
		String str = f.name + "(" + g[0].write(settings);
		for(int i=1; i<g.length;i++) {
			str += ", " + g[i].write(settings);
		}
		str += ")";
		return str;
	}
	@Override
	public Function putArguments(Function[] args) {
		return new FuncComp(f, Functions.putArguments(g, args));
	}
	@Override
	public Bool<Function> expand() {
		Bool<Function> inner = f.expand();
		if(inner.p)
			return new Bool<Function> (inner.f.putArguments(g), inner.p);
		Bool<Function[]> a = Functions.expand(g);
		return new Bool<Function> (inner.f.putArguments(a.f), a.p);
	}
	@Override
	public boolean equals(Function f) {
		if(f.type == this.type)
			return Functions.equals(this.g, ((FuncComp)f).g);
		return false;
	}
}

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
		return this.a == ((FuncNumConst)f).a;
	}	
}



class FuncSum extends Function {
	Function[] f;
	public FuncSum(Function[] f) {
		super(Functions.ADD, Functions.countArguments(f));
		if(f.length == 0) 
			throw new IllegalArgumentException("Podany ciąg musi mieć co najmniej jeden element");
		this.f=f;
	}
	
	@Override
	public Complex evaluate(Complex[] arg) {
		Complex sum = new Complex(0);
		for(int i=0; i<f.length; i++) {
			sum = sum.add(f[i].evaluate(arg));
		}
		return sum;
	}
	//TODO: zrobić z możliwymi minusami jeśli będzie możliwość
	@Override
	public String write(PrintSettings settings) {

		String str = f[0].write(settings);
		for(int i=1;i<f.length;i++) {
			str += " + "+f[i].write(settings);
		}
		return str;
	}

	@Override
	public Function putArguments(Function[] args) {
		return new FuncSum(Functions.putArguments(f, args));
	}

	@Override
	public Bool<Function> expand() {
		Bool<Function[]>ret = Functions.expand(f);
		return new Bool<Function> (new FuncSum(ret.f), ret.p);
	}

	@Override
	public boolean equals(Function f) {
		if(f.type == this.type)
			return Functions.equals(this.f, ((FuncSum)f).f);
		return false;
	}
}

class FuncMult extends Function {
	Function[] f;
	public FuncMult(Function[] f) {
		super(Functions.MULT, Functions.countArguments(f));
		if(f.length == 0) 
			throw new IllegalArgumentException("Podany ciąg musi mieć co najmniej jeden element");
		this.f=f;
	}
	@Override
	public Complex evaluate(Complex[] arg) {
		Complex mult = new Complex(1);
		for(int i=0; i<f.length; i++) {
			mult = mult.mult(f[i].evaluate(arg));
		}
		return mult;
	}
	//TODO: niekiedy będzie zapewne lepiej zapisać mnożenie przez konkatenację a nie *
	//TODO: zrobić z możliwymi dzieleniami (znakami /)
	//TODO: usuwać nawiasy kiedy to możliwe
	@Override
	public String write(PrintSettings settings) {
		String str = "("+f[0].write(settings) + ")";
		for(int i=1;i<f.length;i++) {
			str += " * ("+f[i].write(settings) + ")";
		}
		return str;
	}
	@Override
	public Function putArguments(Function[] args) {
		return new FuncMult(Functions.putArguments(f, args));
	}
	@Override
	public Bool<Function> expand() {
		Bool<Function[]>ret = Functions.expand(f);
		return new Bool<Function> (new FuncMult(ret.f), ret.p);
	}
	@Override
	public boolean equals(Function f) {
		if(f.type == this.type)
			return Functions.equals(this.f, ((FuncMult)f).f);
		return false;
	}
}

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
		return new Bool<Function> (new FuncPow(fExp.f, gExp.f), fExp.p || gExp.p);
	}
	@Override
	public boolean equals(Function f) {
		if(f.type == this.type)
			return this.f.equals(((FuncPow)f).f) && this.g.equals(((FuncPow)f).g);
		return false;
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
	
	
	public static boolean equals(Function[] f, Function g[]) {
		if(f.length != g.length)
			return false;
		for(int i=0;i<f.length;i++) {
			if(!f[i].equals(g[i]))
				return false;
		}
		return true;
	}
	
	public static Bool<Function[]> expand(Function[] f) {
		Function[] g = new Function[f.length];
		boolean changed = false;
		for(int i=0; i< g.length; i++) {
			 Bool<Function> gib = f[i].expand();
			 g[i] = gib.f;
			 if(gib.p)
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
	final static Function e = new FuncConstDefault("e") {
		@Override
		public Complex evaluate(Complex[] arg) {
			return new Complex(Math.E);
		}
	};
	final static Function pi = new FuncConstDefault("pi") {
		@Override
		public Complex evaluate(Complex[] arg) {
			return new Complex(Math.PI);
		}
	};
	final static Function phi = new FuncConstDefault("phi") {
		@Override
		public Complex evaluate(Complex[] arg) {
			return new Complex((Math.sqrt(5)+1)/2);
		}
	};	
	final static Function i = new FuncConstDefault("i") {
		@Override
		public Complex evaluate(Complex[] arg) {
			return Complex.i;
		}
	};		

	
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