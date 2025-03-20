package funkcja;

import java.util.ArrayList;

class FuncSum extends Function {
	final Function[] summands;
	public FuncSum(Function[] f) {
		super(Functions.ADD, Functions.countArguments(f));
		if(f.length == 0) 
			throw new IllegalArgumentException("Podany ciąg musi mieć co najmniej jeden element");
		this.summands=f;
	}
	
	private boolean canPutTogetherPom(Function f, Function g) {
		if(f.type == Functions.MULT)
			if(((FuncMult)f).f.length == 2 && ((FuncMult)f).f[0].type == Functions.NUMCONST && ((FuncMult)f).f[1].equals(g))
				return true;
		return false;
	}
	
	private boolean canPutTogetherPom2(Function f, Function g) {
		if(f.type == Functions.MULT && g.type == Functions.MULT)
			if(((FuncMult)f).f.length == 2 && ((FuncMult)f).f[0].type == Functions.NUMCONST &&
				((FuncMult)g).f.length == 2 && ((FuncMult)g).f[0].type == Functions.NUMCONST &&
				((FuncMult)g).f[1].equals(((FuncMult)f).f[1]))
				return true;
		return false;
	}
	
	private boolean canPutTogether(Function f, Function g) {
		if(f.equals(g) || canPutTogetherPom(f, g) || canPutTogetherPom(g, f) || canPutTogetherPom2(f,g))
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
		if(f.equals(g))
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
			Complex fConst = ((FuncNumConst)((FuncMult)f).f[0]).a;
			Complex gConst = ((FuncNumConst)((FuncMult)g).f[0]).a;
			Function func = ((FuncMult)f).f[1];
			return new FuncMult(new FuncNumConst(Complex.add(fConst, gConst)), func);
		}
		System.out.println("FuncSum w PutTogether - coś poszło nie tak, program nie powinien tutaj dojść.");
		return null;
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
	public Complex evaluate(Complex[] arg) {
		Complex sum = new Complex(0);
		for(int i=0; i<summands.length; i++) {
			sum.add(summands[i].evaluate(arg));
		}
		return sum;
	}

	@Override
	public String write(Settings settings) {
		String str = summands[0].write(settings);
		for(int i=1;i<summands.length;i++) {
			if(summands[i].type == Functions.MULT) {
				if(((FuncMult)summands[i]).f[0].equals(new FuncNumConst(new Complex(-1))))
					str += summands[i].write(settings);
				else
					str += " + "+summands[i].write(settings);
			}else
				str += " + "+summands[i].write(settings);
		}
		return str;
	}

	@Override
	public Function putArguments(Function[] args) {
		return new FuncSum(Functions.putArguments(summands, args));
	}

	@Override
	public Bool<Function> expand() {
		Bool<Function[]>ret = Functions.expand(summands);
		return new Bool<Function> (new FuncSum(ret.f), ret.bool);
	}

	@Override
	public boolean equals(Function f) {
		if(f.type == this.type)
			return Functions.equals(this.summands, ((FuncSum)f).summands);
		return false;
	}

	@Override
	public Function simplify(Settings settings) {
		//jest dziwna kombinacja arraylist i array, zapewne najlepiej byłoby po prostu wszystko zmienić na arraylist, ale mi się nie chce
		//trochę niezręczny kod, ale działa
		if(summands.length == 1)
			return summands[0].simplify(settings);
		ArrayList<Function> organisedSummands = new ArrayList<Function>();
		Function[] simplSummands = Functions.simplifyAll(summands, settings);
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
		return new FuncSum((Function[])(organisedSummands.toArray(new Function[organisedSummands.size()])));
	}
}