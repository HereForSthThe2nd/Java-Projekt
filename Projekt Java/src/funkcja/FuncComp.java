package funkcja;

class FuncComp extends Function {
	private FuncNamed f;
	private Function[] g;
	protected FuncComp(FuncNamed f, Function[] g) {
		super(Functions.COMPOSITE, FuncMethods.countArguments(g));
		this.f = f;
		if(g.length<f.nofArg)
			throw new IllegalArgumentException("Funkcja " + f.name + " musi przyjmować " + f.nofArg + " argumenty a nie "+g.length);
		Function[] args = new Function[f.nofArg];
		for(int i = 0;i<f.nofArg;i++) {
			args[i] = g[i];
		}
		this.g = args;
	}
	protected Function getOuter() {
		return f;
	}
	protected Function[] getInner() {
		return g;
	}
	protected Function getInner(int k) {
		if(k<0 || k>f.nofArg)
			throw new IllegalArgumentException("k poza granicami możliwych indeksów. k: " + k + ".");
		return g[k];
	}
	@Override
	protected Complex evaluate(Complex[] arg) {
		return f.evaluate(FuncMethods.evaluate(g, arg));
	}
	@Override
	protected Function re() throws WewnetzrnaFunkcjaZleZapisana {
		return f.re().putArguments(g);
	}
	@Override
	protected Function im() throws WewnetzrnaFunkcjaZleZapisana {
		return f.im().putArguments(g);
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
		if(g[1].type == Functions.ADD || g[1].type == Functions.MULT || (g[1].type == Functions.COMPOSITE && ((FuncComp)g[1]).f.check(Functions.pow))) {
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
		if(settings.writePow && f.check(Functions.pow))
			return wypiszPotege(settings);
		if(settings.writeRealVar && checkComponents(Functions.Re, Functions.idChecker))
			return Functions.varChecker.returnStr("x", Functions.idChecker.returnNumber(((FuncNamed)g[0]).name));
		if(settings.writeRealVar && checkComponents(Functions.Im, Functions.idChecker))
			return Functions.varChecker.returnStr("y", Functions.idChecker.returnNumber(((FuncNamed)g[0]).name));
		String str = f.write(settings) + "(" + g[0].write(settings);
		for(int i=1; i<g.length;i++) {
			str += ", " + g[i].write(settings);
		}
		str += ")";
		return str;
	}
	@Override
	protected Function putArguments(Function[] args) {
		return new FuncComp(f, FuncMethods.putArguments(g, args));
	}
	@Override
	protected Function expand() {
		Function outer = f.expand();
		if(!outer.check(f))
			return outer.putArguments(g);
		return outer.putArguments(FuncMethods.expand(g));
	}
	@Override
	public boolean check(Function f) {
		if(f.type == this.type)
			return FuncMethods.equals(this.g, ((FuncComp)f).g) && this.f.check(((FuncComp)f).f);
		return false;
	}	
	protected boolean checkComponents(FuncChecker outer, FuncChecker inner) {
		if(f.nofArg == 0)
			return false;
		if(outer.check(f)) {
			if( inner.check(g[0]) )
				return true;
			if(g[0].type == Functions.COMPOSITE)
				if(inner.check(((FuncComp)g[0]).f))
					return true;
		}
		return false;
	}
	
	protected boolean checkComponents2(FuncChecker outer, FuncChecker inner) {
		if(f.nofArg < 2)
			return false;
		if(outer.check(f)) {
			if( inner.check(g[1]) )
				return true;
			if(g[1].type == Functions.COMPOSITE)
				if(inner.check(((FuncComp)g[1]).f))
					return true;
		}
		return false;
	}
		
	private Bool<Function> simplifyPow(Settings settings) {
		if(checkComponents2(Functions.powChecker, new FuncNumConst(new Complex(0))) || checkComponents(Functions.pow, new FuncNumConst(new Complex(1)))) 
			return new Bool<Function> (new FuncNumConst(new Complex(1)), true);
		if(checkComponents(Functions.pow, Functions.e))
			return new Bool<Function> (new FuncComp(Functions.exp, new Function[] {g[1]}), true);
		if(checkComponents2(Functions.powChecker, new FuncNumConst(new Complex(1))))
			return new Bool<Function> (g[0], true);
		if(checkComponents(Functions.powChecker, new FuncNumConst(new Complex(0))))
			return new Bool<Function> (new FuncNumConst(new Complex(0)),true);
		if(checkComponents(Functions.pow, Functions.pow) && !settings.strictPow)
			return new Bool<Function> (new FuncComp(Functions.pow, new Function[] { ((FuncComp)g[0]).g[0], new FuncMult( ((FuncComp)g[0]).g[1], g[1] )}), true);
		if(checkComponents(Functions.pow, Functions.pow) && settings.strictPow) {
			if(((FuncComp)g[0]).g[1].check(new FuncNumConst(new Complex(-1)))) {
				return new Bool<Function> (new FuncComp(Functions.pow, new Function[] { ((FuncComp)g[0]).g[0], new FuncMult( ((FuncComp)g[0]).g[1], g[1] )}), true);
			}
			if(g[1].type == Functions.NUMCONST) {
				if(((FuncNumConst)g[1]).a.y == 0 && ((FuncNumConst)g[1]).a.x%1 == 0)
					return new Bool<Function> (new FuncComp(Functions.pow, new Function[] { ((FuncComp)g[0]).g[0], new FuncMult( ((FuncComp)g[0]).g[1], g[1] )}), true);
			}
		}
		return new Bool<Function> (this, false);
	}
	
	private Bool<Function> simplifyExp(Settings settings) {
		if(checkComponents(Functions.exp, new FuncNumConst(new Complex(0))))
			return new Bool<Function> (new FuncNumConst(new Complex(1)), true);
		if(checkComponents(Functions.exp, new FuncNumConst(new Complex(1))))
			return new Bool<Function> (Functions.e, true);
		if(checkComponents(Functions.pow, Functions.exp) && !settings.strictPow)
			return new Bool<Function> (new FuncComp(Functions.exp, new Function[] { new FuncMult( ((FuncComp)g[0]).g[0], g[1] )}), true);
		if(checkComponents(Functions.pow, Functions.exp) && settings.strictPow) {
			if(((FuncComp)g[0]).g[0].check(new FuncNumConst(new Complex(-1)))) {
				return new Bool<Function> (new FuncComp(Functions.exp, new Function[] { new FuncMult( ((FuncComp)g[0]).g[0], g[1] )}), true);
			}
			if(g[1].type == Functions.NUMCONST) {
				if(((FuncNumConst)g[1]).a.y == 0 && ((FuncNumConst)g[1]).a.x%1 == 0)
					return new Bool<Function> (new FuncComp(Functions.exp, new Function[] { new FuncMult( ((FuncComp)g[0]).g[0], g[1] )}), true);
			}
		}
		if(checkComponents(Functions.ln, Functions.exp) || checkComponents(Functions.exp, Functions.logChecker))
			return new Bool<Function> ( ((FuncComp)g[0]).g[0], true);
		
		return new Bool<Function> (this, false);
	}
	
	private Bool<Function> simplifyTrig(Settings settings){
		
		class MultOfPi implements FuncChecker{
			
			@Override
			public boolean check(Function func) {
				if(checkPom1(func) || checkPom2(func) || checkPom3(func))
					return true;
				return false;
			}
			
			private boolean checkPom1(Function funkcja) {
				if(funkcja.nofArg > 0)
					return false;
				if(funkcja.type == Functions.MULT && ((FuncMult)funkcja).f[0].type == Functions.NUMCONST && ((FuncMult)funkcja).f[1].check(Functions.pi)) {
					Complex c = ((FuncNumConst)((FuncMult)funkcja).f[0]).a;
					if(c.y==0 && (c.x*2) % 1 == 0)
						return true;
				}
				return false;
			}
			
			private boolean checkPom2(Function funkcja) {
				if(funkcja.nofArg > 0 || !(funkcja.type == Functions.MULT) || ((FuncMult)funkcja).f.length != 3 )
					return false;
				Function[] elements = ((FuncMult)funkcja).f;
				boolean piIsContained = FuncMethods.findElement(elements, Functions.pi).bool;
				boolean divBy2 = FuncMethods.findElement(elements, new FuncComp(Functions.pow, new Function[] {new FuncNumConst(new Complex(2)), new FuncNumConst(new Complex(-1))})).bool;
				boolean licz = FuncMethods.findElement(elements, new FuncChecker() {
					@Override
					public boolean check(Function func) {
						if(func.type == Functions.NUMCONST && ((FuncNumConst)func).a.y == 0 && ((FuncNumConst)func).a.x % 1 == 0)
							return true;
						return false;
					}}).bool;
				if(!piIsContained || !divBy2 || !licz)
					return false;
				return true;
			}
			
			private boolean checkPom3(Function funkcja) {
				if(Functions.pi.check(funkcja) || (new FuncNumConst(new Complex(0)).check(funkcja)))
					return true;
				return false;
			}
			
			public int k(Function funkcja) {
				if(checkPom1(funkcja))
					return (int)(2*((FuncNumConst)((FuncMult)funkcja).f[0]).a.x);
				if(checkPom2(funkcja)) {
					Function[] elements = ((FuncMult)funkcja).f;
					int licz = FuncMethods.findElement(elements, new FuncChecker() {
						@Override
						public boolean check(Function func) {
							if(func.type == Functions.NUMCONST && ((FuncNumConst)func).a.y == 0 && ((FuncNumConst)func).a.x % 1 == 0)
								return true;
							return false;
						}}).f;
					return (int) ((FuncNumConst)((FuncMult)funkcja).f[licz]).a.x;
				}
				if(checkPom3(funkcja)) {
					if(funkcja.type == Functions.NUMCONST)
						return 0;
					return 1;
				}
				throw new IllegalArgumentException("Argument nipoprawny. funkcja: " + funkcja.write(new Settings()));
			}

		}

		MultOfPi multOfPi = new MultOfPi();
		if(checkComponents(Functions.cos, multOfPi)) {
			int k = multOfPi.k(g[0]);
			if(k%2 != 0)
				return new Bool<Function>(new FuncNumConst(new Complex(0)), true);
			return k % 4 == 0 ? new Bool<Function>(new FuncNumConst(new Complex(1)), true) : new Bool<Function>(new FuncNumConst(new Complex(-1)), true);
		}
		if(checkComponents(Functions.sin, multOfPi)) {
			int k = multOfPi.k(g[0]);
			if(k%2 == 0)
				return new Bool<Function>(new FuncNumConst(new Complex(0)), true);
			return (k-1) % 2 == 0 ? new Bool<Function>(new FuncNumConst(new Complex(1)), true) : new Bool<Function>(new FuncNumConst(new Complex(-1)), true);
		}
		
		return new Bool<Function>(this, false);
	}
	
	@Override
	protected Function simplify(Settings setting) throws WewnetzrnaFunkcjaZleZapisana {
		Bool<Function> fb;
		fb = simplifyPow(setting);
		if(fb.bool)
			return fb.f;
		fb = simplifyExp(setting);
		if(fb.bool)
			return fb.f;
		fb = simplifyTrig(setting);
		if(fb.bool)
			return fb.f;
		if(f.check(Functions.Re))
			return g[0].re();
		if(f.check(Functions.Im))
			return g[0].im();
		if(nofArg == 0 && setting.evaluateConstants)
			return new FuncNumConst( evaluate(new Complex[] {}));
		//if(checkComponents(null, f))
		Function[] newArg = FuncMethods.simplifyAll(g, setting);
		return new FuncComp(f, newArg);
	}
}