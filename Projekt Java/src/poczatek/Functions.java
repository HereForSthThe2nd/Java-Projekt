package poczatek;

import java.text.DecimalFormat;

//pojawia się tylko w kontekście FuncComp ( na zewnątrz )
//istnieje ze względu na dużo inne zachowanie funkcji write w zależności od ustawienia rozszerzającego funkcje
class DefinedFunction {
	final boolean isComposed;
	final Function funkcja;
	final String name;
	public DefinedFunction(Function f, String name, boolean isComposed) {
		this.funkcja = f;
		this.name = name;
		this.isComposed = isComposed;
	}

	public String write(PrintSettings settings, Function[] arg) {
		if(settings.expand == false | !isComposed) {
			String str = name + "(" + arg[0].write(settings);
			for(int i=1; i<arg.length;i++) {
				str += ", " + arg[i];
			}
			str += ")";
			return str;
		}
		else {
			return writeExpand(settings);
		}
	}
	
	public String writeExpand(PrintSettings settings) {
		return funkcja.write(settings);
	}
	
}

class FuncConst extends Function {
	final static int DODR=1, //1.09, i, 0  //chodzi o to czy trzeba dodawać nawiasy przy wypisywaniu
			DODUR=2, //2i, 2.11i
			UJ=3,//-1, -i
			ZES = 4;//1+i
	final Complex a;
	final int form;
	FuncConst(Complex a){

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
}

class FuncComp extends Function {
	DefinedFunction f;
	Function[] g;
	public FuncComp(DefinedFunction f, Function[] g) {
		super(Functions.FUNC, Functions.countArguments(g));
		this.f = f;
		this.g = g;
	}
	@Override
	public Complex evaluate(Complex[] arg) {
		return f.funkcja.evaluate(Functions.evaluate(g, arg));
	}
	@Override
	public String write(PrintSettings settings) {
		return f.write(settings, g);
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
}


public class Functions {
	static public void main(String[] args) {
		System.out.println("0 błędów");
	}
	static int countArguments(Function[] f) {
		int max=0;
		for(int i=0;i<f.length; i++) {
			if(f[i].nofArg>max)
				max = f[i].nofArg;
		}
		return max;
	}
	protected static Complex[] evaluate(Function[] functions, Complex[] args) {
		Complex[] ret = new Complex[functions.length];
		for(int i=0; i<functions.length;i++) {
			ret[i] = functions[i].evaluate(args);
		}
		return ret;
	}
	final static int NUMCONST = 1,
			CONST = 2,
			ADD=3,
			MULT=4,
			POW=5,
			FUNC=6;
	final static DefinedFunction exp = new DefinedFunction (
			new Function(FUNC, 1) {
		@Override
		public Complex evaluate(Complex[] arg) {
			return Complex.exp(arg[0]);
		}

		@Override
		public String write(PrintSettings settings) {
			return "exp";
		}
	}, "exp", false),
	
	Ln = new DefinedFunction( new Function(FUNC, 1) {
		@Override
		public Complex evaluate(Complex[] arg) {
			return Complex.Ln(arg[0]);
		}

		@Override
		public String write(PrintSettings settings) {
			return "Ln";
		}
	}, "Ln", false);
	
	final static Function e = new Function(CONST, 0) {
		@Override
		public Complex evaluate(Complex[] arg) {
			return new Complex(Math.E);
		}
		@Override
		public String write(PrintSettings settings) {
			return "e";
		}
	};
	final static Function pi = new Function(CONST, 0) {
		@Override
		public Complex evaluate(Complex[] arg) {
			return new Complex(Math.PI);
		}

		@Override
		public String write(PrintSettings settings) {
			return "pi";
		}
	};
	final static Function phi = new Function(CONST, 0) {
		@Override
		public Complex evaluate(Complex[] arg) {
			return new Complex((1+Math.sqrt(5))/2);
		}

		@Override
		public String write(PrintSettings settings) {
			return "phi";
		}
	};
	
	final static FuncConst i = new FuncConst(Complex.i);
		
	final static Function Id = new Function(FUNC, 1) {
		@Override
		public Complex evaluate(Complex[] arg) {
			return arg[0];
		}

		@Override
		public String write(PrintSettings settings) {
			return "z";
		}
	};
	final static Function Re = new Function(FUNC, 1) {
		@Override
		public Complex evaluate(Complex[] arg) {
			return new Complex(arg[0].x);
		}

		@Override
		public String write(PrintSettings settings) {
			return "Re";
		}
	};
	final static Function Im = new Function(FUNC, 1) {

		@Override
		public Complex evaluate(Complex[] arg) {
			return new Complex(arg[0].y);
		}

		@Override
		public String write(PrintSettings settings) {
			return "Im";

		}
	};
}
