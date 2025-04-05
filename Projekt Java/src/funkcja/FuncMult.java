/*
 * funkcja która może zawierać iloczyn funkcji, np. 2x, x*z*x*exp(z)
 * zawiera w sobie informacje o uproszczeniach typu x^2*x = x^3 itp.
 * */


package funkcja;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import funkcja.MatcherMethods.MatcherReturn;

class FuncMult extends Function {
	final Function[] f;
	public FuncMult(Function[] f) {
		super(Functions.MULT, FuncMethods.countArguments(f));
		if(f.length == 0) 
			f = new Function[] {new FuncNumConst(new Complex(1))};
		this.f=f;
	}
	public FuncMult(Function f, Function g) {
		super(Functions.MULT, FuncMethods.countArguments(new Function[] {f,g}));
		this.f=new Function[] {f,g};
	}
	public FuncMult(List<Function> fL) {
		super(Functions.MULT, FuncMethods.countArguments(fL));
		Function[] f = fL.toArray(new Function[fL.size()]);
		if(f.length == 0) 
			f = new Function[] {new FuncNumConst(new Complex(1))};
		this.f=f;
	}
	/*
 	static SimplifyTwo putSameBasesTogether = new SimplifyTwo() {
		
		private boolean sameBasesPom(Function f, Function g) {
			if(f.type == Functions.COMPOSITE) {
				if(((FuncComp)f).checkComponents(Functions.pow, g))
					return true;
			}
			return false;
		}
		
		private boolean expBasePom(Function f, Function g) {
			if(f.type == Functions.COMPOSITE)
				if(((FuncComp)f).getOuter().check(Functions.exp) && g.check(Functions.e))
					return true;
			return false;
					
		}
		
		private boolean expBasePom2(Function f, Function g) {
			if(f.type == Functions.COMPOSITE && g.type == Functions.COMPOSITE)
				if(((FuncComp)f).getOuter().check(Functions.exp) && ((FuncComp)g).getOuter().check(Functions.exp))
					return true;
			return false;
					
		}
		
		private boolean sameBasesPom2(Function f, Function g) {
			if(checkIfPow(f) && checkIfPow(g)) {
					if(((FuncComp)g).getInner(0).check(((FuncComp)f).getInner(0)))
							return true;
			}
			return false;
		}
		
		
		private boolean skracalneWymierne(Function f, Function g) {
			if(FuncMethods.isInt.check(f) && checkIfPow(g)) {
				if(FuncMethods.isInt.check(((FuncComp)g).getInner(0)) && (((FuncComp)g).getInner(1)).check(new FuncNumConst(new Complex(-1)))) {
					int licz = (int) ((FuncNumConst)f).a.x;
					int mian = (int) ((FuncNumConst)((FuncComp)g).getInner(0)).a.x;
					if(NWD(licz, mian) != 1)
						return true;
				}
			}
			return false;
		}

		private Function skrocWymierne(Function f, Function g) {
			if(!skracalneWymierne(f, g))
				try{
					throw new IllegalArgumentException("Podany iloraz nie jest skracalny. Podane funkcje: f: " + f.write(new Settings()) + " ,g: " + g.write(new Settings()));
				}catch(NullPointerException e) {
					throw new IllegalArgumentException("Podany iloraz nie jest skracalny. Funckcji w nim zawartych nie udało się wypisać.");
				}
			int licz = (int) ((FuncNumConst)f).a.x;
			int mian = (int) ((FuncNumConst)((FuncComp)g).getInner(0)).a.x;
			int nwd = NWD(licz,mian);
			return new FuncMult(new Function[] {new FuncNumConst(new Complex(licz/nwd)), new FuncComp(Functions.pow, new Function[] {new FuncNumConst(new Complex(mian/nwd)), new FuncNumConst(new Complex(-1))})});
		}
		
 		private int NWD(int a, int b) {
			if(a == 0 || b == 0)
				return 1;
			int sign = 1;
			if(b < 0)
				sign = -1;
			a = a>0 ? a : -a;
			b = b>0 ? b : -b;
			while(a!=b) {
				if(a > b)
					a -= b;
				else
					b -= a;
			}
			return a * sign;
		}
		
		@Override
		public Function putTogether(Function func1, Function func2) {
			if(!canPutTogether(func1, func2))
				try {
					throw new IllegalArgumentException("Funckcje f oraz g muszą być składalne.\n"
							+ " Podane funkcja f: " + func1.write(new Settings()) + " podana funkcja g: " + func2.write(new Settings()));
				}catch(Exception e) {
					throw new IllegalArgumentException("Funckcje f oraz g muszą być składalne.\n Nie udało sie ich wyświetlić.");
				}
			if(func1.check(func2)) {
				return new FuncComp(Functions.pow, new Function[] {func1, new FuncNumConst(new Complex(2))});
			}
			if(sameBasesPom(func1, func2)) {
				Function fExponent = ((FuncComp)func1).getInner(1);
				return new FuncComp(Functions.pow, new Function[] {func2, new FuncSum(new Function[] {fExponent,new FuncNumConst(new Complex(1))})});
			}
			if(sameBasesPom(func2,func1)) {
				Function gExponent = ((FuncComp)func2).getInner(1);
				return new FuncComp(Functions.pow, new Function[] {func1, new FuncSum(new Function[] {gExponent,new FuncNumConst(new Complex(1))})});
			}
			if(sameBasesPom2(func1,func2)) {
				Function fExponent = ((FuncComp)func1).getInner(1);
				Function gExponent = ((FuncComp)func2).getInner(1);
				return new FuncComp(Functions.pow, new Function[] {((FuncComp)func2).getInner(0), new FuncSum(new Function[] {gExponent,fExponent})});
			}
			if(expBasePom(func1, func2)) {
				Function fExponent = ((FuncComp)func1).getInner(0);
				return new FuncComp(Functions.exp, new Function[] {new FuncSum(new Function[] {fExponent,new FuncNumConst(new Complex(1))})});
			}
			if(expBasePom(func2,func1)) {
				Function gExponent = ((FuncComp)func2).getInner(0);
				return new FuncComp(Functions.exp, new Function[] {new FuncSum(new Function[] {gExponent,new FuncNumConst(new Complex(1))})});
			}
			if(expBasePom2(func1,func2)) {
				Function fExponent = ((FuncComp)func1).getInner(0);
				Function gExponent = ((FuncComp)func2).getInner(0);
				return new FuncComp(Functions.exp, new Function[] {new FuncSum(new Function[] {gExponent,fExponent})});
			}
			if(skracalneWymierne(func1, func2)) {
				return skrocWymierne(func1, func2);
			}
			if(skracalneWymierne(func2, func1)) {
				return skrocWymierne(func2, func1);
			}
			//System.out.println("FuncSum w PutTogether - coś poszło nie tak, program nie powinien tutaj dojść.");
			throw new IllegalArgumentException("coś poszło nie tak, program nie powinien tutaj dojść.");
		}
		
		@Override
		public boolean canPutTogether(Function func1, Function func2) {
			if(( func1.check(func2) && func2.type != Functions.NUMCONST) || sameBasesPom(func1, func2) || sameBasesPom(func2, func1) || sameBasesPom2(func1,func2)
					|| expBasePom(func1, func2) || expBasePom2(func1, func2) || skracalneWymierne(func1, func2) || skracalneWymierne(func2, func1))
				return true;
			return false;
		}
	};*/
	/*
	static class PutSameExponentsTogether implements SimplifyTwo {
		Settings set;
		public PutSameExponentsTogether(Settings settings) {
			set = settings;
		}
		
		@Override
		public Function putTogether(Function func1, Function func2) {
			if(!canPutTogether(func1, func2))
				try {
					throw new IllegalArgumentException("Funckcje f oraz g muszą być składalne.\n"
							+ " Podane funkcja f: " + func1.write(new Settings()) + " podana funkcja g: " + func2.write(new Settings()));
				}catch(Exception e) {
					throw new IllegalArgumentException("Funckcje f oraz g muszą być składalne.\n Nie udało sie ich wyświetlić.");
				}
			return new FuncComp(Functions.pow, new Function[] {new FuncMult(new Function[] {((FuncComp)func1).getInner(0), ((FuncComp)func2).getInner(0)}), ((FuncComp)func1).getInner(1)});
		}
		
		@Override
		public boolean canPutTogether(Function func1, Function func2) {
			if(checkIfPow(func1) && checkIfPow(func2)) {
				if(((FuncComp)func1).getInner(1).check(((FuncComp)func2).getInner(1))){
					if(set.strictPow && FuncMethods.isInt.check(((FuncComp)func1).getInner(1)))
						return true;
					if(!set.strictPow)
						return true;
				}
			}
			return false;
		}
	};
	
	private static boolean checkIfPow(Function f){
		if(f.type == Functions.COMPOSITE && ((FuncComp)f).getOuter().check(Functions.pow))
			return true;
		return false;
	}
						
	/*private boolean checkIfSameExponentsPom2(Function f, Function g, Settings set) {
		if(f.type == Functions.COMPOSITE && g.type == Functions.COMPOSITE) {
			if(!set.strictPow && ((FuncComp)f).getOuter().check(Functions.pow) && ((FuncComp)g).getOuter().check(Functions.pow) && ((FuncComp)f).getInner(1).check(((FuncComp)g).getInner(1)))
				return true;
			if(((FuncComp)f).getOuter().check(Functions.pow) && ((FuncComp)g).getOuter().check(Functions.pow) && ((FuncComp)f).getInner(1).check(((FuncComp)g).getInner(1)))
				if(((FuncComp)f).getInner(1).type == Functions.NUMCONST && ((FuncComp)f).getInner(1).evaluate(new Complex[] {}).y == 0 && ((FuncComp)f).getInner(1).evaluate(new Complex[] {}).x%1 == 0)
					return true;
				
		}
		return false;
	}*/
	
	@Override
	protected Complex evaluate(Complex[] arg) {
		Complex mult = new Complex(1);
		for(int i=0; i<f.length; i++) {
			mult.mult(f[i].evaluate(arg));
		}
		return mult;
	}
	
	@Override
	protected Function re() {
		if(f.length == 1)
			return f[0].re();
		return new FuncSum(new Function[] {new FuncMult(f[0].re(), new FuncMult(FuncMethods.subList(f, 1, f.length)).re()),
				new FuncMult(new Function[] {new FuncNumConst(new Complex(-1)), f[0].im(), new FuncMult(FuncMethods.subList(f, 1, f.length)).im()})
		});
	}
	@Override
	protected Function im() {
		if(f.length == 1)
			return f[0].im();
		return new FuncSum(new Function[] {new FuncMult(f[0].re(), new FuncMult(FuncMethods.subList(f, 1, f.length)).im()),
				new FuncMult(f[0].im(), new FuncMult(FuncMethods.subList(f, 1, f.length)).re())
		});
	}

	private boolean putParenthases(Function f, boolean division) {
		if(f.type == Functions.ADD)
			return true;
		if(f.type == Functions.NUMCONST) {
			switch(((FuncNumConst)f).form) {
			case FuncNumConst.UJEMNYPIERWSZY, FuncNumConst.ZES:
				return true;
			case FuncNumConst.DODR, FuncNumConst.DODUR:
				return false;
			}
		}
		if(division && f.type == Functions.MULT)
			return true;
		return false;
	}
	@Override
	protected String write(Settings settings) {
		int i = 0;
		String str = "";
		if(f[0].check(new FuncNumConst(new Complex(-1)))) {
			if(f.length == 1)
				return "-1";
			i++;
			if(Functions.pow.check(f[i])) {
				if(((Func)f[i]).args[0].check(new FuncNumConst(new Complex(-1)))) {
					if(putParenthases(((Func)f[i]).args[0], true))
						str += "1 / ("+((Func)f[i]).args[0].write(settings) + ")";
					else
						str += "1 / "+((Func)f[i]).args[0].write(settings);
				}else
					str += "- "+f[i].write(settings);
			}else {
				if(putParenthases(f[i], false))
					str += "-("+f[i].write(settings) + ")";
				else
					str += "- "+f[i].write(settings);
			}
			
		}else
			if(putParenthases(f[i], false))
				str += "("+f[i].write(settings) + ")";
			else
				str += f[i].write(settings);
		
		for(i++;i<f.length;i++) {
			if(Functions.pow.check(f[i])) {
					if(((Func)f[i]).args[1].check(new FuncNumConst(new Complex(-1)))) {
						if(putParenthases(((Func)f[i]).args[0], true))
							str += " / ("+((Func)f[i]).args[0].write(settings) + ")";
						else
							str += " / "+((Func)f[i]).args[0].write(settings);
					}else
						str += " * "+f[i].write(settings);
			}else {
				if(putParenthases(f[i], false))
					str += " * ("+f[i].write(settings) + ")";
				else
					str += " * "+f[i].write(settings);
			}
		}
		return str;
	}
	@Override
	protected Function putArguments(Function[] args) {
		return new FuncMult(FuncMethods.putArguments(f, args));
	}
	@Override
	protected Function expand() {
		return new FuncMult(FuncMethods.expand(f));
	}
	@Override
	public boolean check(Function f) {
		if(f.type == this.type)
			return FuncMethods.equals(this.f, ((FuncMult)f).f);
		return false;
	}
	
	protected LinkedList<Function> removeInnerMult() { 
		LinkedList<Function> extendedMult = new LinkedList<Function>(); 
		for(int i=0;i<f.length;i++) {
			if(f[i].type == Functions.MULT) {
				LinkedList<Function> innerExtended = ((FuncMult)f[i]).removeInnerMult();
				extendedMult.addAll(innerExtended);
			}
			else {
				extendedMult.add(f[i]);
			}
		}
		return extendedMult;
	}
	
	@Override
	protected Function simplify(SimplifyRule rule) {
		//na początku tego zawsze już nie powninno nie mieć wewnętrznych nawiasów
		Function[] simpl = FuncMethods.simplifyAll(this.f, rule);
		LinkedList<Function> removedInner = new FuncMult(simpl).removeInnerMult();
		return rule.simplify(new FuncMult(removedInner));
		/*calledSimp++;
		//System.out.println("w funccomp mult.  " + this.write(settings) + "   " + calledSimp);
		//jest dziwna kombinacja arraylist i array, zapewne najlepiej byłoby po prostu wszystko zmienić na arraylist, ale mi się nie chce
		//trochę niezręczny kod, ale działa
		if(f.length == 1)
			return f[0].simplify(settings);
		
		LinkedList<Function> extendedMult;
		LinkedList<Function> simplMult;
		LinkedList<Function> multPutTogether1;
		LinkedList<Function> multPutTogether2;
		LinkedList<Function> organisedMult;
		
		extendedMult = removeInnerMult();
		simplMult = FuncMethods.simplifyAll(extendedMult, settings);
		multPutTogether1 = putSameBasesTogether.putAlltogether(simplMult);
		PutSameExponentsTogether putSameExponentsTOgether = new PutSameExponentsTogether(settings);
		multPutTogether2 = putSameExponentsTOgether.putAlltogether(multPutTogether1);
		
		Complex numConst = new Complex(1);

		for(int i=0;i<multPutTogether2.size();i++) {
			if(multPutTogether2.get(i).type == Functions.NUMCONST) {
				numConst.mult(((FuncNumConst)multPutTogether2.get(i)).a);
			}
		}
		if(numConst.equals(new Complex(0)))
			return new FuncNumConst(new Complex(0));
		
		organisedMult = new LinkedList<Function>();
		if(!numConst.equals(new Complex(1)))
			organisedMult.add(new FuncNumConst(numConst));
		
		for(int i=0;i<multPutTogether2.size();i++) {
			if(multPutTogether2.get(i).nofArg == 0) {
				if(multPutTogether2.get(i).type != Functions.NUMCONST)
					organisedMult.add(multPutTogether2.get(i));
				multPutTogether2.remove(i);
				i--;
			}
		}
		organisedMult.addAll(multPutTogether2);

		return new FuncMult((Function[])(organisedMult.toArray(new Function[organisedMult.size()])));*/
	}
	public static void main(String[] args) throws WrongSyntaxException {
		FunctionPowloka fp = new FunctionPowloka("2 / 3", new Settings());
		FunctionPowloka fp2 = new FunctionPowloka("-1", new Settings());
		fp.print(new Settings());
		System.out.println(Functions.pow.check(Functions.pow.returnFunc(new Function[] {fp.f, fp2.f})));
	}
	@Override
	protected Function replaceMatchers() {
		return new FuncMult(FuncMethods.replaceMatchers(f));
	}
	@Override
	protected Function removeInners() {
		LinkedList<Function> fExpanded = removeInnerMult();
		LinkedList<Function> fRemovedInners = new LinkedList<Function>();
		for(Function i : fExpanded) {
			fRemovedInners.add(i.removeInners());
		}
		return new FuncMult(fRemovedInners);
	}
	@Override
	protected Function copyPom(MatcherReturn matcherRet) {
		return new FuncMult(FuncMethods.copyAll(f, matcherRet));
	}
	@Override
	protected FunctionInfo info() {
		return new FunctionInfo(FuncMethods.info(f), true);
	}
}