/*
 * funkcja która może zawierać złączenie dwóch funkcji, np. exp(z^2)
 * zewnętrzna funkja musi być funkcją z nazwą, a każda funkcja z nazwą ( w kontekście funkcji danej przez użytkownika) jest zapisana jako funkcja zewnętrzna w FuncComp
 * zawiera w sobie informacje o uproszczeniach typu exp(ln(z)) = z 
 * */


package funkcja;

import Inne.Complex;

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
			throw new IllegalArgumentException("k poza granicami możliwych indeksów. k: " + k + ", maksymalny indeks(włącznie): " + f.nofArg + ".");
		return g[k];
	}
	@Override
	protected Complex evaluate(Complex[] arg) {
		if(f == Functions.diffX) {
			return g[0].diffX(1, new Settings()).evaluate(arg);
		}
		if(f == Functions.diffY) {
			return g[0].diffY(1, new Settings()).evaluate(arg);
		}
		return f.evaluate(FuncMethods.evaluate(g, arg));
	}
	@Override
	protected Function[] reim() {
		Function re;
		Function im;
		try {
			if(f == Functions.diffX || f == Functions.diffY) {
				Function[] g0reim = g[0].reim();
				re =  new FuncComp(f, new Function[] {g0reim[0]});
				im =  new FuncComp(f, new Function[] {g0reim[1]});
				return new Function[] {re, im};
			}
			if(checkComponents2(Functions.powChecker, new FuncNumConst( new Complex(0))))
				return new Function[] {new FuncNumConst(new Complex(1)), new FuncNumConst(new Complex(0))};
			if(checkComponents2(Functions.powChecker, new FuncNumConst(new Complex(-1)))) {
				FunctionPowloka rePom = new FunctionPowloka("z[0] / (z[0]^2 + z[1]^2)", new Settings());
				FunctionPowloka imPom = new FunctionPowloka("-z[1] / (z[0]^2 + z[1]^2)", new Settings());
				Function[] g0reim = g[0].reim();
				re = rePom.getFunction().putArguments(g0reim);
				im = imPom.getFunction().putArguments(g0reim);
				return new Function[] {re, im};
			}
			if(checkComponents2(Functions.powChecker, FuncMethods.isInt)) {
				if(FuncMethods.isNatural.check(g[1])) {
					Function[] pom = new Function[(int)g[1].evaluate(null).x];
					for(int i=0;i<pom.length;i++) {
						pom[i] = g[0];
					}
					return (new FuncMult(pom)).reim();
				}
				else {
					//TODO: chyba nie działa, sprawdzić to
					Function[] pom = new Function[(int)g[1].evaluate(null).x];
					Function pom2 = new FuncComp(f, new Function[] {g[0], new FuncNumConst(new Complex(-1))});
					for(int i=0;i<pom.length;i++) {
						pom[i] = pom2;
					}
					return (new FuncMult(pom)).reim();
				}
			}
		}catch(FunctionExpectedException e) {
			throw new IllegalStateException(e);
		}
		Function[] retPom = f.reim();	
		Function[] greim = new Function[f.nofArg*2];
		for(int i = 0;i<f.nofArg;i++) {
			Function[]greimi = g[i].reim();
			greim[2*i] = greimi[0];
			greim[2*i+1] = greimi[1];
		}
		return new Function[] {retPom[0].putArguments(greim), retPom[1].putArguments(greim)};
	}

	private String wypiszPotege(Settings settings) throws FunctionExpectedException {
		//zajmuje się poprawny postawieniem nawiasów
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
	protected String write(Settings settings) throws FunctionExpectedException {
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
		if(str.length() > 10000)
			throw new FunctionExpectedException("Funkcja jest za długa aby ją wypisać. Ma w zapisie > 10000 znaków.");
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
		if(checkComponents(Functions.powChecker, new FuncNumConst(new Complex(0)))) {
			if(FuncMethods.isNatural.check(g[1]))
				return new Bool<Function> (new FuncNumConst(new Complex(0)),true);
			return new Bool<Function> (this,false);
		}
		if(checkComponents(Functions.pow, Functions.pow) && !settings.strictPow)
			return new Bool<Function> (new FuncComp(Functions.pow, new Function[] { ((FuncComp)g[0]).g[0], new FuncMult( ((FuncComp)g[0]).g[1], g[1] )}), true);
		if(checkComponents(Functions.pow, Functions.pow) && settings.strictPow) {
			if(((FuncComp)g[0]).g[1].check(new FuncNumConst(new Complex(-1)))) {
				return new Bool<Function> (new FuncComp(Functions.pow, new Function[] { ((FuncComp)g[0]).g[0], new FuncMult( ((FuncComp)g[0]).g[1], g[1] )}), true);
			}
			if(FuncMethods.isInt.check(g[1])) {
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
			if(FuncMethods.isInt.check(g[1])) {
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
				if(funkcja.type == Functions.MULT && ((FuncMult)funkcja).f[0].type == Functions.NUMCONST && ((FuncMult)funkcja).f[1].check(Functions.pi) && ((FuncMult)funkcja).f.length == 2) {
					Complex c = ((FuncNumConst)((FuncMult)funkcja).f[0]).a;
					if(c.y==0 && (c.x*2) % 1 == 0)
						return true;
				}
				return false;
			}
			
			private boolean checkPom2(Function funkcja) {
				if(funkcja.nofArg > 0 || !(funkcja.type == Functions.MULT))
					return false;
				Function[] elements = ((FuncMult)funkcja).f;
				boolean piIsContained = FuncMethods.findElement(elements, Functions.pi).bool;
				boolean divBy2 = FuncMethods.findElement(elements, new FuncComp(Functions.pow, new Function[] {new FuncNumConst(new Complex(2)), new FuncNumConst(new Complex(-1))})).bool;
				boolean licz = FuncMethods.findElement(elements, new FuncChecker() {
					@Override
					public boolean check(Function func) {
						if(FuncMethods.isInt.check(func))
							return true;
						return false;
					}}).bool;
				if(!licz && ((FuncMult)funkcja).f.length != 2)
					return false;
				if(licz && ((FuncMult)funkcja).f.length != 3)
					return false;
				if(!piIsContained || !divBy2)
					return false;
				return true;
			}
			
			private boolean checkPom3(Function funkcja) {
				if(Functions.pi.check(funkcja) || (new FuncNumConst(new Complex(0)).check(funkcja)))
					return true;
				return false;
			}
			
			public int k(Function funkcja) {
				if(checkPom1(funkcja)) {
					return (int)(2*((FuncNumConst)((FuncMult)funkcja).f[0]).a.x);
				}
				if(checkPom2(funkcja)) {
					Function[] elements = ((FuncMult)funkcja).f;
					Bool<Integer> liczWthB = FuncMethods.findElement(elements, new FuncChecker() {
						@Override
						public boolean check(Function func) {
							if(FuncMethods.isInt.check(func))
								return true;
							return false;
						}});
					if(liczWthB.bool)
						return (int) ( ((FuncNumConst)((FuncMult)funkcja).f[liczWthB.f]).a.x);
					else
						return 1;
				}
				if(checkPom3(funkcja)) {
					if(funkcja.type == Functions.NUMCONST)
						return 0;
					return 2;
				}
				try {
					throw new IllegalArgumentException("Argument nipoprawny. funkcja: " + funkcja.write(new Settings()));
				} catch(FunctionExpectedException e) {
					throw new IllegalArgumentException("Argument nipoprawny. Funkcji nawet nie można wypisać.");
				}
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
			return (k-1) % 4 == 0 ? new Bool<Function>(new FuncNumConst(new Complex(1)), true) : new Bool<Function>(new FuncNumConst(new Complex(-1)), true);
		}
		
		return new Bool<Function>(this, false);
	}
	
	private Bool<Function> simplifyMiscellanious(Settings settings){
		if(checkComponents(Functions.sinh, new FuncNumConst(new Complex(0))))
			return new Bool<Function>(new FuncNumConst(new Complex(0)), true);
		if(checkComponents(Functions.cosh, new FuncNumConst(new Complex(0))))
			return new Bool<Function>(new FuncNumConst(new Complex(1)), true);
		if(checkComponents(Functions.cosh, new FuncNumConst(new Complex(0))))
			return new Bool<Function>(new FuncNumConst(new Complex(1)), true);
		//zapomina o tym czy liczba rzeczywista jest dodatnia czy ujemna, ale może warto?
		//TODO: kiedy wszystko inne bęzie zrobinone sprawdzić czy mogę usunąć poniższe
		//o co mi chdziło jak pisałem powyższe komentarze?
		if(checkComponents(Functions.arg, Functions.Re))
			return new Bool<Function>(new FuncNumConst(new Complex(0)), true);
		if(checkComponents(Functions.arg, Functions.Im))
			return new Bool<Function>(new FuncNumConst(new Complex(0)), true);
		if(checkComponents(Functions.Re, Functions.Re))
			return new Bool<Function>(new FuncComp(Functions.Re, new Function[] {((FuncComp)g[0]).g[0]}), true);
		if(checkComponents(Functions.Re, Functions.Im))
			return new Bool<Function>(new FuncComp(Functions.Im, new Function[] {((FuncComp)g[0]).g[0]}), true);
		if(checkComponents(Functions.Im, Functions.Re) || checkComponents(Functions.Im, Functions.Im))
			return new Bool<Function>(new FuncNumConst(new Complex(0)), true);


		return new Bool<Function>(this, false);
	}
	
	private Bool<Function> simplifyMinusy(Settings settings){
		if(checkComponents(Functions.sin, FuncMethods.minusOneTimes) || checkComponents(Functions.sinh, FuncMethods.minusOneTimes)) {
			FuncMult wewIloczyn = new FuncMult (new FuncMult(new FuncNumConst(new Complex(-1)), this.g[0]).removeInnerMult());
			return new Bool<Function>(new FuncMult(new FuncNumConst(new Complex(-1)), new FuncComp(this.f, new Function[] {wewIloczyn})), true);
		}
		if(checkComponents(Functions.cos, FuncMethods.minusOneTimes) || checkComponents(Functions.cosh, FuncMethods.minusOneTimes)) {
			FuncMult wewIloczyn = new FuncMult (new FuncMult(new FuncNumConst(new Complex(-1)), this.g[0]).removeInnerMult());
			return new Bool<Function>(new FuncComp(this.f, new Function[] {wewIloczyn}), true);
		}
		return new Bool<Function>(this,false);

	}
	
	@Override
	protected Function simplify(Settings setting) {
		//System.out.println("w funccomp początek.  " + this.write(setting) + "   " + calledSimp);
		//System.out.println("fueufufeu w  funccomp.simplify");
		//System.out.println(f.write(new Settings()) + "  " + g[0].write(new Settings()));
		if(f == Functions.diffX) {
			try {
				System.out.println(g[0].write(new Settings()) + " w funccomp.simplify");
			} catch (FunctionExpectedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return g[0].diffX(1, new Settings());
		}
		if(f == Functions.diffY) {
			return g[0].diffY(1, new Settings());
		}
		Bool<Function> fb;
		fb = simplifyPow(setting);
		if(fb.bool) {
			return fb.f;
		}
		fb = simplifyExp(setting);
		if(fb.bool)
			return fb.f;
		fb = simplifyTrig(setting);
		if(fb.bool)
			return fb.f;
		fb = simplifyMiscellanious(setting);
		if(fb.bool)
			return fb.f;
		fb = simplifyMinusy(setting);
		if(fb.bool)
			return fb.f;
		if(f.check(Functions.Re)) {
			return g[0].reim()[0];
		}
		if(f.check(Functions.Im))
			return g[0].reim()[1];
		if(nofArg == 0 && setting.evaluateConstants)
			return new FuncNumConst( evaluate(new Complex[] {}));
		//if(checkComponents(null, f))
		Function[] newArg = FuncMethods.simplifyAll(g, setting);
		return new FuncComp(f, newArg);
	}
	@Override
	protected Function diffX(int arg, Settings set) {
		if(arg == 0)
			throw new IllegalArgumentException("Numeracja zaczyna się od 1");
		if(f.nofArg == 0)
			return new FuncNumConst(new Complex(0));
		if(Functions.diffX.check(f))
			return (g[0].diffX(1, set)).diffX(arg, set);
		if(Functions.diffY.check(f))
			return (g[0].diffY(1, set)).diffX(arg, set);

		Function[] ret = new Function[2*f.nofArg];
		for(int i=0;i<f.nofArg;i++) {
			Function[] giDiffX = new Function[] {g[i].diffX(arg, set)}; 
			//System.out.println("w funccomp.diffX " + i + "  " + f.write(new Settings()) + "  " + g[i].write(new Settings()) + "  arg: " +arg);
			ret[2*i] = new FuncMult((f.diffX(i+1, set)).putArguments(g), new FuncComp(Functions.Re, giDiffX));
			//System.out.println("2w funccomp.diffX " + i + "  " + f.write(new Settings()));
			ret[2*i+1] = new FuncMult((f.diffY(i+1, set)).putArguments(g), new FuncComp(Functions.Im, giDiffX));
			//System.out.println("3w funccomp.diffX " + i + "  " + f.write(new Settings()));
		}
		return new FuncSum(ret);
	}
	@Override
	protected Function diffY(int arg, Settings set) {
		if(f.nofArg == 0)
			return new FuncNumConst(new Complex(0));
		if(Functions.diffX.check(f))
			return (g[0].diffX(1, set)).diffY(arg, set);
		if(Functions.diffY.check(f))
			return (g[0].diffY(1, set)).diffY(arg, set);

		Function[] ret = new Function[2*f.nofArg];
		for(int i=0;i<f.nofArg;i++) {
			ret[2*i] = new FuncMult((f.diffX(i+1, set)).putArguments(g), new FuncComp(Functions.Re, new Function[] {g[i].diffY(arg, set)}));
			ret[2*i+1] = new FuncMult((f.diffY(i+1, set)).putArguments(g), new FuncComp(Functions.Im, new Function[] {g[i].diffY(arg, set)}));
		}
		
		return new FuncSum(ret);
	}
	@Override
	Function removeDiff() {
		if(f == Functions.diffX) {
			return g[0].diffX(1, new Settings());
		}
		if(f == Functions.diffY) {
			return g[0].diffY(1, new Settings());
		}
		return new FuncComp(f, FuncMethods.removeDiffInAll(g));
	}
}