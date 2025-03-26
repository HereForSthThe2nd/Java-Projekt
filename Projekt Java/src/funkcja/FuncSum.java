package funkcja;

import java.util.ArrayList;

class FuncSum extends Function {
	final Function[] summands;
	protected FuncSum(Function[] f) {
		super(Functions.ADD, FuncMethods.countArguments(f));
		if(f.length == 0) 
			this.summands = new Function[] {new FuncNumConst(new Complex(0))};
		else
			this.summands=f;
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
	
	private static Function putTogetherTwoMult(FuncMult f, FuncMult g) {
		if(!canPutTogetherPom2(f, g)) 
			throw new IllegalArgumentException();
		Pair<ArrayList<Function>, ArrayList<Function>> fSplit= splitByConstants(f);
		Pair<ArrayList<Function>, ArrayList<Function>> gSplit= splitByConstants(g);
		Function summed = new FuncSum(new Function[] {new FuncMult( fSplit.first.toArray(new Function[fSplit.first.size()])), new FuncMult(gSplit.first.toArray(new Function[gSplit.first.size()]))});
		Function stayed = new FuncMult( fSplit.second.toArray(new Function[fSplit.second.size()]));
		return new FuncMult(new Function[] {summed, stayed});
	}
	
	private static boolean canPutTogether(Function f, Function g) {
		if(f.check(g) || canPutTogetherPom(f, g) || canPutTogetherPom(g, f) || canPutTogetherPom2(f,g))
			return true;
		return false;
	}
	
	private Function putTogether(Function f, Function g) {
		if(!canPutTogether(f, g))
			try {
				throw new IllegalArgumentException("Funckcje f oraz g muszą być składalne.\n"
						+ " Podane funkcja f: " + f.write(new Settings()) + " podana funkcja g: " + g.write(new Settings()));
			}catch(Exception e) {
				throw new IllegalArgumentException("Funckcje f oraz g muszą być składalne.\n Nie udało sie ich wyświetlić.");
			}
		if(f.check(g))
			return new FuncMult(new FuncNumConst(new Complex(2)), f);
		if(canPutTogetherPom(f, g)) {
			Complex fConst = ((FuncNumConst)((FuncMult)f).f[0]).a;
			return new FuncMult(new FuncNumConst(Complex.add(fConst,new Complex(1))), g);
		}
		if(canPutTogetherPom(g,f)) {
			Complex gConst = ((FuncNumConst)((FuncMult)g).f[0]).a;
			return new FuncMult(new FuncNumConst(Complex.add(gConst,new Complex(1))), f);
		}
		if(canPutTogetherPom2(f,g)) {
			System.out.println("w funcsum.puttogether, pod cnaputtogether2pom");
			return putTogetherTwoMult((FuncMult)f, (FuncMult)g);
		}
		throw new IllegalArgumentException("Coś poszło nie tak, program nie powinien tutaj dojść.");
	}
	
	private ArrayList<Function> putEveryThingTogether(ArrayList<Function> arr, ArrayList<Integer> zabronioneIndeksy){
		ArrayList<Integer> uzyteIndeksy = zabronioneIndeksy; 
		if(arr.size() == 0)
			throw new IllegalArgumentException("arr musi mieć w sobie co najmniej jeden element.");
		ArrayList<Function> ret = new ArrayList<Function>();
		int countIndex = 0;
		for(int i=0;i<arr.size();i++) {
			if(uzyteIndeksy.contains(i))
				continue;
			ret.add(arr.get(i));
			for(int j=i+1;j<arr.size();j++) {
				if(uzyteIndeksy.contains(j))
					continue;
				if(canPutTogether(arr.get(j), arr.get(i))) {
						ret.set(countIndex, putTogether(arr.get(j), ret.get(countIndex)));
						uzyteIndeksy.add(j);
				}
			}
			countIndex++;
		}
		return ret;
	}
	
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
		ArrayList<Function> summandsPutTogether = putEveryThingTogether(extendedSummands, zabronioneIndeksy);
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
}