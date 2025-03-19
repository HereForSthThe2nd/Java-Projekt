package poczatek;

import java.util.ArrayList;

class FuncMult extends Function {
	Function[] f;
	public FuncMult(Function[] f) {
		super(Functions.MULT, Functions.countArguments(f));
		if(f.length == 0) 
			throw new IllegalArgumentException("Podany ciąg musi mieć co najmniej jeden element");
		this.f=f;
	}
	public FuncMult(Function f, Function g) {
		super(Functions.MULT, Functions.countArguments(new Function[] {f,g}));
		this.f=new Function[] {f,g};
	}
	
	private boolean canPutTogetherPom(Function f, Function g) {
		if(f.type == Functions.COMPOSITE)
			if(((FuncComp)f).checkComponents("Pow", g))
				return true;
		return false;
	}
	
	private boolean canPutTogetherPom2(Function f, Function g) {
		if(f.type == Functions.COMPOSITE && g.type == Functions.COMPOSITE)
			if(((FuncComp)f).f.name == "Pow")
				if(((FuncComp)g).checkComponents("Pow", ((FuncComp)f).g[0]))
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
			return new FuncComp(Functions.Pow, new Function[] {f, new FuncNumConst(new Complex(2))});
		if(canPutTogetherPom(f, g)) {
			Function fExponent = ((FuncComp)f).g[1];
			return new FuncComp(Functions.Pow, new Function[] {g, new FuncSum(new Function[] {fExponent,new FuncNumConst(new Complex(1))})});
		}
		if(canPutTogetherPom(g,f)) {
			Function gExponent = ((FuncComp)g).g[1];
			return new FuncComp(Functions.Pow, new Function[] {f, new FuncSum(new Function[] {gExponent,new FuncNumConst(new Complex(1))})});
		}
		if(canPutTogetherPom2(f,g)) {
			Function fExponent = ((FuncComp)f).g[1];
			Function gExponent = ((FuncComp)g).g[1];
			return new FuncComp(Functions.Pow, new Function[] {((FuncComp)g).g[0], new FuncSum(new Function[] {gExponent,fExponent})});
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
		Complex mult = new Complex(1);
		for(int i=0; i<f.length; i++) {
			mult.mult(f[i].evaluate(arg));
		}
		return mult;
	}
	//TODO: niekiedy będzie zapewne lepiej zapisać mnożenie przez konkatenację a nie *
	//TODO: zrobić z możliwymi dzieleniami (znakami /)
	//TODO: usuwać nawiasy kiedy to możliwe
	@Override
	public String write(PrintSettings settings) {
		int i = 0;
		String str = "";
		if(f[0].equals(new FuncNumConst(new Complex(-1)))) {
			str += "-";
			i++;
		}
		str += "("+f[i].write(settings) + ")";
		for(i++;i<f.length;i++) {
			str += " * ("+f[i].write(settings) + ")";
		}
		return str;
	}
	@Override
	public Function putArguments(Function[] args) {
		return new FuncMult(Functions.putArguments(f, args));
	}
	@Override
	public Bool<Function> expand() {
		Bool<Function[]>ret = Functions.expand(f);
		return new Bool<Function> (new FuncMult(ret.f), ret.bool);
	}
	@Override
	public boolean equals(Function f) {
		if(f.type == this.type)
			return Functions.equals(this.f, ((FuncMult)f).f);
		return false;
	}
	@Override
	public Function simplify() {
		//jest dziwna kombinacja arraylist i array, zapewne najlepiej byłoby po prostu wszystko zmienić na arraylist, ale mi się nie chce
		//trochę niezręczny kod, ale działa
		if(f.length == 1)
			return f[0].simplify();
		ArrayList<Function> organisedMult = new ArrayList<Function>();
		Function[] simplMult = Functions.simplifyAll(f);
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
		ArrayList<Integer> zabronioneIndeksy = new ArrayList<Integer>();
		Complex numConst = new Complex(1);
		for(int i=0;i<f.length;i++) {
			if(f[i].type == Functions.NUMCONST) {
				numConst.mult(((FuncNumConst)f[i]).a);
				zabronioneIndeksy.add(i);
			}
		}
		ArrayList<Function> multPutTogether = putEveryThingTogether(extendedMult, zabronioneIndeksy);
		if(numConst.equals(new Complex(0)))
			return new FuncNumConst(new Complex(0));
		if(!numConst.equals(new Complex(1)))
			organisedMult.add(new FuncNumConst(numConst));
		
		for(int i=0;i<multPutTogether.size();i++) {
			if(multPutTogether.get(i).nofArg == 0) {
				organisedMult.add(multPutTogether.get(i));
				multPutTogether.remove(i);
				i--;
			}
		}
		organisedMult.addAll(multPutTogether);

		return new FuncMult((Function[])(organisedMult.toArray(new Function[organisedMult.size()])));
	}
}