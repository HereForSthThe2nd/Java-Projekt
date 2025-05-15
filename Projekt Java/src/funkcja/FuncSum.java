/*
 * suma funkcji
 */

package funkcja;

import java.util.ArrayList;
import java.util.LinkedList;

import Inne.Complex;
import Inne.Pair;

class FuncSum extends Function {
	final Function[] summands;
	protected FuncSum(Function[] f) {
		super(Functions.ADD, FuncMethods.countArguments(f));
		if(f.length == 0) 
			this.summands = new Function[] {new FuncNumConst(new Complex(0))};
		else
			this.summands=f;
	}
	
	SimplifyTwo sameStuff = new SimplifyTwo() {
		
		private static Function putTogetherTwoMult(FuncMult f, FuncMult g) {
			if(!canPutTogetherPom2(f, g)) 
				throw new IllegalArgumentException();
			Pair<ArrayList<Function>, ArrayList<Function>> fSplit= splitByConstants(f);
			Pair<ArrayList<Function>, ArrayList<Function>> gSplit= splitByConstants(g);
			Function summed = new FuncSum(new Function[] {new FuncMult( fSplit.first.toArray(new Function[fSplit.first.size()])), new FuncMult(gSplit.first.toArray(new Function[gSplit.first.size()]))});
			Function stayed = new FuncMult( fSplit.second.toArray(new Function[fSplit.second.size()]));
			FuncMult fRet = new FuncMult(new Function[] {summed, stayed});
			LinkedList<Function> czynniki = fRet.removeInnerMult();
			return new FuncMult(czynniki.toArray(new Function[czynniki.size()]));
		}
		
		@Override
		public Function putTogether(Function func1, Function func2) {
			if(!canPutTogether(func1, func2)) {
				String func1Str;
				String func2Str;
				try {
					func1Str = func1.write(new Settings());
					func2Str = func2.write(new Settings());
				}catch(Exception e) {
					throw new IllegalArgumentException("Funckcje f oraz g muszą być składalne.\n Nie udało sie ich wyświetlić.");
				}
				throw new IllegalArgumentException("Funckcje f oraz g muszą być składalne.\n"
						+ "\nPodana funkcja f: " + func1Str + "\npodana funkcja g: " + func2Str);
			}
			if(func1.check(func2))
				return new FuncMult(new FuncNumConst(new Complex(2)), func1);
			if(canPutTogetherPom(func1, func2)) {
				Complex fConst = ((FuncNumConst)((FuncMult)func1).f[0]).a;
				return new FuncMult(new FuncNumConst(Complex.add(fConst,new Complex(1))), func2);
			}
			if(canPutTogetherPom(func2,func1)) {
				Complex gConst = ((FuncNumConst)((FuncMult)func2).f[0]).a;
				return new FuncMult(new FuncNumConst(Complex.add(gConst,new Complex(1))), func1);
			}
			if(canPutTogetherPom2(func1,func2)) {
				return putTogetherTwoMult((FuncMult)func1, (FuncMult)func2);
			}
			throw new IllegalArgumentException("Coś poszło nie tak, program nie powinien tutaj dojść.");
		}
		
		private static boolean canPutTogetherPom(Function f, Function g) {
			if(f.type == Functions.MULT)
				if(((FuncMult)f).f.length == 2 && ( ((FuncMult)f).f[0].type == Functions.NUMCONST || (g.nofArg > 0 && ((FuncMult)f).f[0].nofArg == 0)) && ((FuncMult)f).f[1].check(g))
					return true;
			return false;
		}
		
		private static Pair<ArrayList<Function>, ArrayList<Function>> splitByConstants(FuncMult f){
			//dzieli podany iloczyn na elementy stałe i elemnty niestałe
			//jeśli funkcja jest stała dzieli ją na możliwą początkowa stałą numeryczną oraz resztę
			ArrayList<Function> front = new ArrayList<Function>();
			ArrayList<Function> back = new ArrayList<Function>();
			if(f.nofArg == 0) {
				int i=0;
				if(f.f[0].type == Functions.NUMCONST) {
					front.add(f.f[0]);
					i++;
				}
				for(i=i; i<f.f.length;i++) {
					back.add(f.f[i]);
				}
				return new Pair<ArrayList<Function>, ArrayList<Function>>(front,back);
			}
			for(int i=0; i<f.f.length;i++) {
				if(f.f[i].nofArg == 0)
					front.add(f.f[i]);
				else
					back.add(f.f[i]);
			}
			return new Pair<ArrayList<Function>, ArrayList<Function>>(front,back);
		}
		
		private static boolean canPutTogetherPom2(Function f, Function g) {
			if(f.type == Functions.MULT && g.type == Functions.MULT)
				if(FuncMethods.equals( splitByConstants((FuncMult)f).second, splitByConstants((FuncMult)g).second))
					return true;
			return false;
		}

		
		@Override
		public boolean canPutTogether(Function func1, Function func2) {
			if(func1.check(func2) || canPutTogetherPom(func1, func2) || canPutTogetherPom(func2, func1) || canPutTogetherPom2(func1,func2))
				return true;
			return false;
		}
	};
		
	@Override
	protected Complex evaluate(Complex[] arg) {
		Complex sum = new Complex(0);
		for(int i=0; i<summands.length; i++) {
			sum.add(summands[i].evaluate(arg));
		}
		return sum;
	}
	
	@Override
	protected Function re() throws WewnetzrnaFunkcjaZleZapisana {
		return new FuncSum(FuncMethods.re(summands));
	}
	@Override
	protected Function im() throws WewnetzrnaFunkcjaZleZapisana {
		return new FuncSum(FuncMethods.im(summands));
	}
	
	@Override
	protected String write(Settings settings) {
		String str = summands[0].write(settings);
		for(int i=1;i<summands.length;i++) {
			if(summands[i].type == Functions.MULT) {
				if(((FuncMult)summands[i]).f[0].check(new FuncNumConst(new Complex(-1))))
					str += " " + summands[i].write(settings);
				else
					str += " + "+summands[i].write(settings);
			}else
				str += " + "+summands[i].write(settings);
		}
		return str;
	}

	@Override
	protected Function putArguments(Function[] args) {
		return new FuncSum(FuncMethods.putArguments(summands, args));
	}

	@Override
	protected Function expand() {
		return new FuncSum(FuncMethods.expand(summands));
	}
	
	@Override
	public boolean check(Function f) {
		if(f.type == this.type)
			return FuncMethods.equals(this.summands, ((FuncSum)f).summands);
		return false;
	}

	@Override
	protected Function simplify(Settings settings) throws WewnetzrnaFunkcjaZleZapisana {
		calledSimp++;
		//System.out.println("w funcsum zwróci .  " + this.write(settings) + "   " + calledSimp);
		//jest dziwna kombinacja arraylist i array, zapewne najlepiej byłoby po prostu wszystko zmienić na arraylist, ale mi się nie chce
		//trochę niezręczny kod, ale działa
		if(summands.length == 1)
			return summands[0].simplify(settings);
		Function[] simplSummands = FuncMethods.simplifyAll(summands, settings);
		ArrayList<Function> extendedSummands = new ArrayList<Function>();
		for(int i=0;i<simplSummands.length;i++) {
			if(simplSummands[i].type == Functions.ADD) {
				for(int j=0;j<((FuncSum)simplSummands[i]).summands.length;j++) {
					extendedSummands.add(((FuncSum)simplSummands[i]).summands[j]);
				}
			}
			else {
				extendedSummands.add(simplSummands[i]);
			}
		}
		ArrayList<Integer> zabronioneIndeksy = new ArrayList<Integer>();
		Complex numConst = new Complex(0);
		for(int i=0;i<extendedSummands.size();i++) {
			if(extendedSummands.get(i).type == Functions.NUMCONST) {
				numConst.add(((FuncNumConst)extendedSummands.get(i)).a);
				zabronioneIndeksy.add(i);
			}
		}
		ArrayList<Function> summandsPutTogether = sameStuff.puAllTogether(extendedSummands, zabronioneIndeksy);
		ArrayList<Function> organisedSummands = new ArrayList<Function>();
		if(!numConst.equals(new Complex(0))) {
			organisedSummands.add(new FuncNumConst(numConst));
		}
		for(int i=0;i<summandsPutTogether.size();i++) {
			if(summandsPutTogether.get(i).nofArg == 0) {
				organisedSummands.add(summandsPutTogether.get(i));
				summandsPutTogether.remove(i);
				i--;
			}
		}
		organisedSummands.addAll(summandsPutTogether);

		if(organisedSummands.size() == 0)
			return new FuncNumConst(new Complex(0));
		return new FuncSum((organisedSummands.toArray(new Function[organisedSummands.size()])));
	}

	@Override
	protected Function diffX(int arg, Settings set) {
		Function[] summandsMod = new Function[summands.length];
		for(int i=0;i<summands.length;i++) {
			summandsMod[i] = summands[i].diffX(arg, set);
		}
		return new FuncSum(summandsMod);
	}
	@Override
	protected Function diffY(int arg, Settings set) {
		Function[] summandsMod = new Function[summands.length];
		for(int i=0;i<summands.length;i++) {
			summandsMod[i] = summands[i].diffY(arg, set);
		}
		return new FuncSum(summandsMod);
	}

}