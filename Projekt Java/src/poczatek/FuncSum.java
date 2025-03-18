package poczatek;

import java.util.ArrayList;

class FuncSum extends Function {
	final Function[] f;
	public FuncSum(Function[] f) {
		super(Functions.ADD, Functions.countArguments(f));
		if(f.length == 0) 
			throw new IllegalArgumentException("Podany ciąg musi mieć co najmniej jeden element");
		this.f=f;
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
						+ " Podane funkcja f: " + f.write(PrintSettings.defaultSettings) + " podana funkcja g: " + g.write(PrintSettings.defaultSettings));
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
	
	private Bool<ArrayList<Function>> putEveryThingTogether(Function[] arr, ArrayList<Integer> zabronioneIndeksy){
		ArrayList<Integer> uzyteIndeksy = zabronioneIndeksy; 
		if(arr.length == 0)
			throw new IllegalArgumentException("arr musi mieć w sobie co najmniej jeden element.");
		ArrayList<Function> ret = new ArrayList<Function>();
		boolean sthChanged = false;
		int countIndex = 0;
		for(int i=0;i<arr.length;i++) {
			if(uzyteIndeksy.contains(i))
				continue;
			ret.add(arr[i]);
			for(int j=i+1;j<arr.length;j++) {
				if(canPutTogether(arr[j], arr[i])) {
						ret.set(countIndex, putTogether(arr[j], ret.get(countIndex)));
						sthChanged = true;
						uzyteIndeksy.add(j);
				}
			}
			countIndex++;
		}
		return new Bool<ArrayList<Function>>(ret, sthChanged);
	}
	
	@Override
	public Complex evaluate(Complex[] arg) {
		Complex sum = new Complex(0);
		for(int i=0; i<f.length; i++) {
			sum.add(f[i].evaluate(arg));
		}
		return sum;
	}

	@Override
	public String write(PrintSettings settings) {
		String str = f[0].write(settings);
		for(int i=1;i<f.length;i++) {
			if(f[i].type == Functions.MULT) {
				if(((FuncMult)f[i]).f[0].equals(new FuncNumConst(new Complex(-1))))
					str += f[i].write(settings);
				else
					str += " + "+f[i].write(settings);
			}else
				str += " + "+f[i].write(settings);
		}
		return str;
	}

	@Override
	public Function putArguments(Function[] args) {
		return new FuncSum(Functions.putArguments(f, args));
	}

	@Override
	public Bool<Function> expand() {
		Bool<Function[]>ret = Functions.expand(f);
		return new Bool<Function> (new FuncSum(ret.f), ret.bool);
	}

	@Override
	public boolean equals(Function f) {
		if(f.type == this.type)
			return Functions.equals(this.f, ((FuncSum)f).f);
		return false;
	}

	@Override
	public Bool<Function> simplify() {
		//jest dziwna kombinacja arraylist i array, zapewne najlepiej byłoby po prostu wszystko zmienić na arraylist, ale mi się nie chce
		//trochę niezręczny kod, ale działa
		if(f.length == 1)
			return new Bool<Function> (f[0].simplify().f, true);
		ArrayList<Function> arr = new ArrayList<Function>();
		Bool<Function[]> f2p = Functions.simplifyAll(f);
		Function[] f2 = f2p.f;
		ArrayList<Function> f2Pom = new ArrayList<Function>();
		for(int i=0;i<f2.length;i++) {
			if(f2[i].type == Functions.ADD) {
				for(int j=0;j<((FuncSum)f2[i]).f.length;j++) {
					f2Pom.add(((FuncSum)f2[i]).f[j]);
				}
			}
			else {
				f2Pom.add(f2[i]);
			}
		}
		f2=f2Pom.toArray(new Function[f2Pom.size()]);
		boolean sthChanged = f2p.bool;
		ArrayList<Integer> zabronioneIndeksy = new ArrayList<Integer>();
		Complex numConst = new Complex(0);
		for(int i=0;i<f.length;i++) {
			if(f[i].type == Functions.NUMCONST) {
				numConst.add(((FuncNumConst)f[i]).a);
				sthChanged = i==0 ? false : true;
				zabronioneIndeksy.add(i);
			}
		}
		Bool<ArrayList<Function>> arrPomp= putEveryThingTogether(f2, zabronioneIndeksy);
		ArrayList<Function> arrPom = arrPomp.f;
		if(arrPomp.bool)
			sthChanged = true;
		if(!numConst.equals(new Complex(0))) {
			arr.add(new FuncNumConst(numConst));
		}
		for(int i=0;i<arrPom.size();i++) {
			if(arrPom.get(i).nofArg == 0) {
				arr.add(arrPom.get(i));
				arrPom.remove(i);
				i--;
			}else
				{sthChanged = true;}
		}
		arr.addAll(arrPom);
		if(arr.size() == 0)
			return new Bool<Function>(new FuncNumConst(new Complex(0)), true);
		return new Bool<Function>(new FuncSum((Function[])(arr.toArray(new Function[arr.size()]))), sthChanged);
	}
}