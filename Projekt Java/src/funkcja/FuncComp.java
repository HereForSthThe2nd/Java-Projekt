package funkcja;

class FuncComp extends Function {
	FuncNamed f;
	Function[] g;
	protected FuncComp(FuncNamed f, Function[] g) {
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
	protected Complex evaluate(Complex[] arg) {
		return f.evaluate(Functions.evaluate(g, arg));
	}
	private String wypiszPotege(Settings settings) {
		String str = "";
		if(g[0].type == Functions.ADD || g[0].type == Functions.MULT) {
			str += "(" + g[0].write(settings) + ")";
		}else {
			if(g[0].type == Functions.NUMCONST && !(((FuncNumConst)g[0]).form == FuncNumConst.DODR)) {
				str += "(" + g[0].write(settings) + ")"; 
			}else {
				str += g[0].write(settings);
			}
		}
		str += " ^ ";
		if(g[1].type == Functions.ADD || g[1].type == Functions.MULT || (g[1].type == Functions.COMPOSITE && ((FuncComp)g[1]).f.equals(Functions.pow))) {
			str += "(" + g[1].write(settings) + ")";
		}else {
			if(g[1].type == Functions.NUMCONST && !(((FuncNumConst)g[1]).form == FuncNumConst.DODR)) {
				str += "(" + g[1].write(settings) + ")"; 
			}else {
				str += g[1].write(settings);
			}
		}
		return str;
	}
	@Override
	protected String write(Settings settings) {
		if(settings.writePow && f.name.equals("pow"))
			return wypiszPotege(settings);
		String str = f.write(settings) + "(" + g[0].write(settings);
		for(int i=1; i<g.length;i++) {
			str += ", " + g[i].write(settings);
		}
		str += ")";
		return str;
	}
	@Override
	protected Function putArguments(Function[] args) {
		return new FuncComp(f, Functions.putArguments(g, args));
	}
	@Override
	protected Bool<Function> expand() {
		Bool<Function> inner = f.expand();
		if(inner.bool)
			return new Bool<Function> (inner.f.putArguments(g), inner.bool);
		Bool<Function[]> a = Functions.expand(g);
		return new Bool<Function> (inner.f.putArguments(a.f), a.bool);
	}
	@Override
	protected boolean equals(Function f) {
		if(f.type == this.type)
			return Functions.equals(this.g, ((FuncComp)f).g);
		return false;
	}

	
	protected boolean checkComponents1(String nameOuter, Function inner) {
		if(f.name == nameOuter) {
			if( g[0].equals(inner) )
				return true;
		}
		return false;
	}
	
	protected boolean checkComponents2(String nameOuter, Function inner) {
		if(f.name == nameOuter && f.nofArg >= 2) {
			if( g[1].equals(inner) )
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
	protected Function simplify(Settings setting) {
		if(checkComponents("exp", "Ln") || checkComponents("ln", "exp") || checkComponents("exp", "ln"))
			return ((FuncComp)g[0]).g[0];
		if(checkComponents2("pow", new FuncNumConst(new Complex(0))) || checkComponents1("pow", new FuncNumConst(new Complex(1)))) 
			return new FuncNumConst(new Complex(1));
		if(checkComponents2("pow", new FuncNumConst(new Complex(1))))
			return g[0];
		if(checkComponents1("pow", new FuncNumConst(new Complex(0))))
			return new FuncNumConst(new Complex(0));
		if(checkComponents("pow", "pow") && !setting.strictPow)
			return new FuncComp(Functions.pow, new Function[] { ((FuncComp)g[0]).g[0], new FuncMult( ((FuncComp)g[0]).g[1], g[1] )});
		if(checkComponents("pow", "pow") && setting.strictPow) {
			System.out.println("w funcconmp.simplify: strictpow");
			if(((FuncComp)g[0]).g[1].equals(new FuncNumConst(new Complex(-1)))) {
				return new FuncComp(Functions.pow, new Function[] { ((FuncComp)g[0]).g[0], new FuncMult( ((FuncComp)g[0]).g[1], g[1] )});
			}
			if(g[1].type == Functions.NUMCONST) {
				if(((FuncNumConst)g[1]).a.y == 0 && ((FuncNumConst)g[1]).a.x%1 == 0)
					return new FuncComp(Functions.pow, new Function[] { ((FuncComp)g[0]).g[0], new FuncMult( ((FuncComp)g[0]).g[1], g[1] )});
			}
		}
		Function[] newArg = Functions.simplifyAll(g, new Settings());
		return new FuncComp(f, newArg);
	}
}