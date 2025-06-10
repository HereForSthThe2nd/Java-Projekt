/*
 * funkcja która może zawierać iloczyn funkcji, np. 2x, x*z*x*exp(z)
 * zawiera w sobie informacje o uproszczeniach typu x^2*x = x^3 itp.
 * */


package funkcja;

import java.util.ArrayList;
import java.util.LinkedList;

import Inne.Complex;

class FuncMult extends Function {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3731704753646330967L;
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
	public FuncMult(LinkedList<Function> fL) {
		super(Functions.MULT, FuncMethods.countArguments(fL));
		Function[] f = fL.toArray(new Function[fL.size()]);
		if(f.length == 0) 
			f = new Function[] {new FuncNumConst(new Complex(1))};
		this.f=f;
	}
	
 	static SimplifyTwo putSameBasesTogether = new SimplifyTwo() {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 2194079811477821517L;

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
				}catch(NullPointerException | FunctionExpectedException e) {
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
	};

	static class PutSameExponentsTogether implements SimplifyTwo {
		/**
		 * 
		 */
		private static final long serialVersionUID = -1254978749361225162L;
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
	protected Complex evaluate(Complex[] arg) throws FunctionExpectedException {
		Complex mult = new Complex(1);
		for(int i=0; i<f.length; i++) {
			mult.mult(f[i].evaluate(arg));
		}
		return mult;
	}
	
	@Override
	protected Function[] reim() throws FunctionExpectedException {
		Function[] f0reim = f[0].reim();
		if(f.length == 1)
			return f[0].reim();
		Function removedf0 = new FuncMult(FuncMethods.subList(f, 1, f.length));
		Function[] removedf0reim = removedf0.reim();
		Function re = new FuncSum(new Function[] {new FuncMult(f0reim[0], removedf0reim[0]),
				new FuncMult(new Function[] {new FuncNumConst(new Complex(-1)), f0reim[1], removedf0reim[1]})
				});
		Function im = new FuncSum(new Function[] {new FuncMult(f0reim[0], removedf0reim[1]),
				new FuncMult(f0reim[1], removedf0reim[0])
				});
		return new Function[] {re,im};
	}

	private boolean putParenthases(Function f, boolean division) {
		//na celu sprawdzenia czy trzeba postawić nawiasy podczas wypisywania
		if(f.type == Functions.ADD)
			return true;
		if(f.type == Functions.MULT) {
			if(((FuncMult)f).f[0].check(new FuncNumConst(new Complex(-1))))
				return true;
		}
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
	public String write(Settings settings) throws FunctionExpectedException {
		int i = 0;
		String str = "";
		if(f[0].check(new FuncNumConst(new Complex(-1)))) {
			if(f.length == 1)
				return "-1";
			i++;
			if(f[i].type == Functions.COMPOSITE && ((FuncComp)f[i]).getOuter().check(Functions.pow)) {
				if(((FuncComp)f[i]).getInner(1).check(new FuncNumConst(new Complex(-1)))) {
					if(putParenthases(((FuncComp)f[i]).getInner(0), true))
						str += "1 / ("+((FuncComp)f[i]).getInner(0).write(settings) + ")";
					else
						str += "1 / "+((FuncComp)f[i]).getInner(0).write(settings);
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
			if(f[i].type == Functions.COMPOSITE && ((FuncComp)f[i]).getOuter().check(Functions.pow)) {
					if(((FuncComp)f[i]).getInner(1).check(new FuncNumConst(new Complex(-1)))) {
						if(putParenthases(((FuncComp)f[i]).getInner(0), true))
							str += " / ("+((FuncComp)f[i]).getInner(0).write(settings) + ")";
						else
							str += " / "+((FuncComp)f[i]).getInner(0).write(settings);
					}else
						str += " * "+f[i].write(settings);
			}else {
				if(putParenthases(f[i], false))
					str += " * ("+f[i].write(settings) + ")";
				else
					str += " * "+f[i].write(settings);
			}
		}
		if(str.length() > 10000)
			throw new FunctionExpectedException("Funkcja jest za długa aby ją wypisać. Ma w zapisie > 10000 znaków.");
		
		return str;
	}
	@Override
	public Function putArguments(Function[] args) {
		return new FuncMult(FuncMethods.putArguments(f, args));
	}
	@Override
	public Function expand() {
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
	protected Function simplify(Settings settings) throws FunctionExpectedException { 
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

		return new FuncMult((Function[])(organisedMult.toArray(new Function[organisedMult.size()])));
	}
	@Override
	protected Function diffX(int arg, Settings set) throws FunctionExpectedException {
		if(f.length == 1)
			return f[0].diffX(arg, set);
		if(f.length == 2) 
			return new FuncSum(new Function[] {new FuncMult(f[0].diffX(arg, set), f[1]), new FuncMult(f[1].diffX(arg, set), f[0])});
		Function[] fMod = new Function[f.length-1];
		for(int i=1;i<f.length;i++) {
			fMod[i-1] = f[i];
		}
		Function fP = new FuncMult(f[0], new FuncMult(fMod));
		return fP.diffX(arg, set);
	}
	@Override
	protected Function diffY(int arg, Settings set) throws FunctionExpectedException {
		if(f.length == 1)
			return f[0].diffY(arg, set);
		if(f.length == 2) 
			return new FuncSum(new Function[] {new FuncMult(f[0].diffY(arg, set), f[1]), new FuncMult(f[1].diffY(arg, set), f[0])});
		Function[] fMod = new Function[f.length-1];
		for(int i=1;i<f.length;i++) {
			fMod[i] = f[i-1];
		}
		Function fP = new FuncMult(f[0], new FuncMult(fMod));
		return fP.diffY(arg, set);
	}
	@Override
	Function removeDiff() throws FunctionExpectedException {
		return new FuncMult(FuncMethods.removeDiffInAll(f));
	}
	@Override
	protected Function expandSpecific(String name) {
		return new FuncMult(FuncMethods.expandSpecificAll(f, name));
	}
	@Override
	protected LinkedList<String> checkDepecdencies() {
		return FuncMethods.checkDepAll(f);
	}
	@Override
	protected int size() {
		return FuncMethods.sizeOfAll(f) + 1;
	}
}