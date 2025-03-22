package funkcja;

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
	
	private boolean checkIfPow(Function f){
		if(f.type == Functions.COMPOSITE && ((FuncComp)f).f.equals(Functions.pow))
			return true;
		return false;
	}
	
	private boolean sameBasesPom(Function f, Function g) {
		if(f.type == Functions.COMPOSITE) {
			if(((FuncComp)f).checkComponents("pow", g))
				return true;
		}
		return false;
	}
	
	private boolean sameBasesPom2(Function f, Function g) {
		if(checkIfPow(f) && checkIfPow(g)) {
				if(((FuncComp)g).g[0].equals(((FuncComp)f).g[0]));
						return true;
		}
		return false;
	}
	
	private boolean sameBases(Function f, Function g) {
		if(( f.equals(g) && g.type != Functions.NUMCONST) || sameBasesPom(f, g) || sameBasesPom(g, f) || sameBasesPom2(f,g))
			return true;
		return false;
	}
	
	private Function putExponentsTogether(Function f, Function g) {
		if(!sameBases(f, g))
			try {
				throw new IllegalArgumentException("Funckcje f oraz g muszą być składalne.\n"
						+ " Podane funkcja f: " + f.write(new Settings()) + " podana funkcja g: " + g.write(new Settings()));
			}catch(Exception e) {
				throw new IllegalArgumentException("Funckcje f oraz g muszą być składalne.\n Nie udało sie ich wyświetlić.");
			}
		if(f.equals(g))
			return new FuncComp(Functions.pow, new Function[] {f, new FuncNumConst(new Complex(2))});
		if(sameBasesPom(f, g)) {
			Function fExponent = ((FuncComp)f).g[1];
			return new FuncComp(Functions.pow, new Function[] {g, new FuncSum(new Function[] {fExponent,new FuncNumConst(new Complex(1))})});
		}
		if(sameBasesPom(g,f)) {
			Function gExponent = ((FuncComp)g).g[1];
			return new FuncComp(Functions.pow, new Function[] {f, new FuncSum(new Function[] {gExponent,new FuncNumConst(new Complex(1))})});
		}
		if(sameBasesPom2(f,g)) {
			Function fExponent = ((FuncComp)f).g[1];
			Function gExponent = ((FuncComp)g).g[1];
			return new FuncComp(Functions.pow, new Function[] {((FuncComp)g).g[0], new FuncSum(new Function[] {gExponent,fExponent})});
		}
		//System.out.println("FuncSum w PutTogether - coś poszło nie tak, program nie powinien tutaj dojść.");
		throw new IllegalArgumentException("coś poszło nie tak, program nie powinien tutaj dojść.");
	}
	
	private ArrayList<Function> putAllSameBasesTogether(ArrayList<Function> arr){
		ArrayList<Integer> uzyteIndeksy = new ArrayList<Integer>(); 
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
				if(sameBases(arr.get(j), arr.get(i))) {
						ret.set(countIndex, putExponentsTogether(arr.get(j), ret.get(countIndex)));
						uzyteIndeksy.add(j);
				}
			}
			countIndex++;
		}
		return ret;
	}
	
	//private boolean checkIfSameExponentsPom(Function f, Function g)
	
	private boolean checkIfSameExponentsPom2(Function f, Function g, Settings set) {
		if(f.type == Functions.COMPOSITE && g.type == Functions.COMPOSITE) {
			if(!set.strictPow && ((FuncComp)f).f.equals(Functions.pow) && ((FuncComp)g).f.equals(Functions.pow) && ((FuncComp)f).g[1].equals(((FuncComp)g).g[1]))
				return true;
			if(((FuncComp)f).f.equals(Functions.pow) && ((FuncComp)g).f.equals(Functions.pow) && ((FuncComp)f).g[1].equals(((FuncComp)g).g[1]))
				if(((FuncComp)f).g[1].type == Functions.NUMCONST && ((FuncComp)f).g[1].evaluate(new Complex[] {}).y == 0 && ((FuncComp)f).g[1].evaluate(new Complex[] {}).x%1 == 0)
					return true;
				
		}
		return false;
	}
	
	@Override
	protected Complex evaluate(Complex[] arg) {
		Complex mult = new Complex(1);
		for(int i=0; i<f.length; i++) {
			mult.mult(f[i].evaluate(arg));
		}
		return mult;
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
		if(f[0].equals(new FuncNumConst(new Complex(-1)))) {
			if(f.length == 1)
				return "-1";
			i++;
			if(f[i].type == Functions.COMPOSITE && ((FuncComp)f[i]).f.name.equals("pow")) {
				if(((FuncComp)f[i]).g[1].equals(new FuncNumConst(new Complex(-1)))) {
					if(putParenthases(((FuncComp)f[i]).g[0], true))
						str += "1 / ("+((FuncComp)f[i]).g[0].write(settings) + ")";
					else
						str += "1 / "+((FuncComp)f[i]).g[0].write(settings);
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
			if(f[i].type == Functions.COMPOSITE && ((FuncComp)f[i]).f.name.equals("pow")) {
					if(((FuncComp)f[i]).g[1].equals(new FuncNumConst(new Complex(-1)))) {
						if(putParenthases(((FuncComp)f[i]).g[0], true))
							str += " / ("+((FuncComp)f[i]).g[0].write(settings) + ")";
						else
							str += " / "+((FuncComp)f[i]).g[0].write(settings);
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
		return new FuncMult(Functions.putArguments(f, args));
	}
	@Override
	protected Bool<Function> expand() {
		Bool<Function[]>ret = Functions.expand(f);
		return new Bool<Function> (new FuncMult(ret.f), ret.bool);
	}
	@Override
	protected boolean equals(Function f) {
		if(f.type == this.type)
			return Functions.equals(this.f, ((FuncMult)f).f);
		return false;
	}
	@Override
	protected Function simplify(Settings settings) {
		//jest dziwna kombinacja arraylist i array, zapewne najlepiej byłoby po prostu wszystko zmienić na arraylist, ale mi się nie chce
		//trochę niezręczny kod, ale działa
		if(f.length == 1)
			return f[0].simplify(settings);
		ArrayList<Function> organisedMult = new ArrayList<Function>();
		Function[] simplMult = Functions.simplifyAll(f, settings);
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

		ArrayList<Function> multPutTogether = putAllSameBasesTogether(extendedMult);
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