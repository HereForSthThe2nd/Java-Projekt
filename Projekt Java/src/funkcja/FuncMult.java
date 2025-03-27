package funkcja;

import java.util.ArrayList;

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
	
	SimplTwo putBasesTogether = new SimplTwo() {
		
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

		@Override
		public Function putTogether(Function base, Function exponent) {
			if(!canPutTogether(base, exponent))
				try {
					throw new IllegalArgumentException("Funckcje f oraz g muszą być składalne.\n"
							+ " Podane funkcja f: " + base.write(new Settings()) + " podana funkcja g: " + exponent.write(new Settings()));
				}catch(Exception e) {
					throw new IllegalArgumentException("Funckcje f oraz g muszą być składalne.\n Nie udało sie ich wyświetlić.");
				}
			if(base.check(exponent))
				return new FuncComp(Functions.pow, new Function[] {base, new FuncNumConst(new Complex(2))});
			if(sameBasesPom(base, exponent)) {
				Function fExponent = ((FuncComp)base).getInner(1);
				return new FuncComp(Functions.pow, new Function[] {exponent, new FuncSum(new Function[] {fExponent,new FuncNumConst(new Complex(1))})});
			}
			if(sameBasesPom(exponent,base)) {
				Function gExponent = ((FuncComp)exponent).getInner(1);
				return new FuncComp(Functions.pow, new Function[] {base, new FuncSum(new Function[] {gExponent,new FuncNumConst(new Complex(1))})});
			}
			if(sameBasesPom2(base,exponent)) {
				Function fExponent = ((FuncComp)base).getInner(1);
				Function gExponent = ((FuncComp)exponent).getInner(1);
				return new FuncComp(Functions.pow, new Function[] {((FuncComp)exponent).getInner(0), new FuncSum(new Function[] {gExponent,fExponent})});
			}
			if(expBasePom(base, exponent)) {
				Function fExponent = ((FuncComp)base).getInner(0);
				return new FuncComp(Functions.exp, new Function[] {new FuncSum(new Function[] {fExponent,new FuncNumConst(new Complex(1))})});
			}
			if(expBasePom(exponent,base)) {
				Function gExponent = ((FuncComp)exponent).getInner(0);
				return new FuncComp(Functions.exp, new Function[] {new FuncSum(new Function[] {gExponent,new FuncNumConst(new Complex(1))})});
			}
			if(expBasePom2(base,exponent)) {
				Function fExponent = ((FuncComp)base).getInner(0);
				Function gExponent = ((FuncComp)exponent).getInner(0);
				return new FuncComp(Functions.exp, new Function[] {new FuncSum(new Function[] {gExponent,fExponent})});
			}

			//System.out.println("FuncSum w PutTogether - coś poszło nie tak, program nie powinien tutaj dojść.");
			throw new IllegalArgumentException("coś poszło nie tak, program nie powinien tutaj dojść.");
		}
		
		@Override
		public boolean canPutTogether(Function base, Function exponent) {
			if(( base.check(exponent) && exponent.type != Functions.NUMCONST) || sameBasesPom(base, exponent) || sameBasesPom(exponent, base) || sameBasesPom2(base,exponent) || expBasePom(base, exponent) || expBasePom2(base, exponent))
				return true;
			return false;
		}
	};
	
	private boolean checkIfPow(Function f){
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
	protected Function re() throws WewnetzrnaFunkcjaZleZapisana {
		if(f.length == 1)
			return f[0].re();
		return new FuncSum(new Function[] {new FuncMult(f[0].re(), new FuncMult(FuncMethods.subList(f, 1, f.length)).re()),
				new FuncMult(new Function[] {new FuncNumConst(new Complex(-1)), f[0].im(), new FuncMult(FuncMethods.subList(f, 1, f.length)).im()})
		});
	}
	@Override
	protected Function im() throws WewnetzrnaFunkcjaZleZapisana {
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
	//TODO: niekiedy będzie zapewne lepiej zapisać mnożenie przez konkatenację a nie *
	//TODO: zrobić z możliwymi dzieleniami (znakami /)
	//TODO: usuwać nawiasy kiedy to możliwe
	@Override
	protected String write(Settings settings) {
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
	@Override
	protected Function simplify(Settings settings) throws WewnetzrnaFunkcjaZleZapisana { 
		//jest dziwna kombinacja arraylist i array, zapewne najlepiej byłoby po prostu wszystko zmienić na arraylist, ale mi się nie chce
		//trochę niezręczny kod, ale działa
		if(f.length == 1)
			return f[0].simplify(settings);
		ArrayList<Function> organisedMult = new ArrayList<Function>();
		Function[] simplMult = FuncMethods.simplifyAll(f, settings);
		ArrayList<Function> extendedMult = new ArrayList<Function>();
		for(int i=0;i<simplMult.length;i++) {
			if(simplMult[i].type == Functions.MULT) {
				for(int j=0;j<((FuncMult)simplMult[i]).f.length;j++) {
					extendedMult.add(((FuncMult)simplMult[i]).f[j]);
				}
			}
			else {
				extendedMult.add(simplMult[i]);
			}
		}
		Complex numConst = new Complex(1);

		ArrayList<Function> multPutTogether = putBasesTogether.putAlltogether(extendedMult);
		for(int i=0;i<multPutTogether.size();i++) {
			if(multPutTogether.get(i).type == Functions.NUMCONST) {
				numConst.mult(((FuncNumConst)multPutTogether.get(i)).a);
			}
		}
		if(numConst.equals(new Complex(0)))
			return new FuncNumConst(new Complex(0));
		if(!numConst.equals(new Complex(1)))
			organisedMult.add(new FuncNumConst(numConst));
		
		for(int i=0;i<multPutTogether.size();i++) {
			if(multPutTogether.get(i).nofArg == 0) {
				if(multPutTogether.get(i).type != Functions.NUMCONST)
					organisedMult.add(multPutTogether.get(i));
				multPutTogether.remove(i);
				i--;
			}
		}
		organisedMult.addAll(multPutTogether);

		return new FuncMult((Function[])(organisedMult.toArray(new Function[organisedMult.size()])));
	}
}