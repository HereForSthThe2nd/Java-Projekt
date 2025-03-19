package poczatek;

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
		if(inner.bool)
			return new Bool<Function> (inner.f.putArguments(g), inner.bool);
		Bool<Function[]> a = Functions.expand(g);
		return new Bool<Function> (inner.f.putArguments(a.f), a.bool);
	}
	@Override
	public boolean equals(Function f) {
		if(f.type == this.type)
			return Functions.equals(this.g, ((FuncComp)f).g);
		return false;
	}
	
	protected boolean checkComponents(String nameOuter, Function inner) {
		//tylko jeśli zewnętrzna funkcja jest jedno argumentowa
		if(f.name == nameOuter && f.nofArg == 1) {
			if( (((FuncComp)g[0]).f).equals(inner))
				return true;
		}
		return false;
	}
	
	protected boolean checkComponents(String nameOuter, String nameInner) {
		//sprawdza czy zewnętrzna funkcja jest odpowiednia oraz czy pierwszy argument funkcji wewnętrznej jest odpowiedni
		if(f.name == nameOuter) {
			if(g[0].type == Functions.COMPOSITE)
				if(((FuncComp)g[0]).f.type == Functions.NAMED)
					if( ((FuncNamed)((FuncComp)g[0]).f).name == nameInner)
						return true;
		}
		return false;
	}
	
	@Override
	public Function simplify() {
		if(checkComponents("exp", "Ln"))
			return ((FuncComp)g[0]).g[0];
		Function[] newArg = Functions.simplifyAll(g);
		return new FuncComp(f, newArg);
	}
}