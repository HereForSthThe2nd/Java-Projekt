package funkcja;

public class FunctionPowloka {
	Function f;
	//String canonicForm; gólne by było równe f.write, chyba że użytkownik dopiero przed chwilą wpisał funkcję i jeszcze jej w żaden sposób nie zmodyfikował... 
		//może jeśli zmieni w ustawieniach znaczenie pow to może to mogłoby mieć sens? 
		//Poza tym jeśli chce się to gdzieś przepisać bez zmian czy coś
	
 	public FunctionPowloka(String str, Settings settings) throws WrongSyntaxException {
		if(str.equals("")) {
			f = new FuncNumConst(new Complex(0));
			return;
		}
		f = Function.read(new BlokList(Function.preliminaryChanges(str)), settings);
	}
	
 	public FunctionPowloka copy() {
 		return new FunctionPowloka(this.f);
 	}
 	
 	public int nofArg() {
 		return f.nofArg;
 	}
 	
 	public FunctionPowloka(Function f) {
		this.f = f;
	}
	
	public Complex evaluate(Complex[] z) {
		return f.evaluate(z);
	}
		
	public FunctionPowloka changeToNamed(String str) throws IncorrectNameException {
		return new FunctionPowloka(Functions.addNmdFunc(f, str));
	}
	
	public FunctionPowloka changeToVar(String str) throws IncorrectNameException {
		return new FunctionPowloka(Functions.addVar(f, str));
	}
		
	public boolean equals(FunctionPowloka fP) {
		return f.check(fP.f);
	}
	
	public FunctionPowloka expand() {
		return new FunctionPowloka(f.expand());
	}
	
	public FunctionPowloka re(Settings set) throws WewnetzrnaFunkcjaZleZapisana {
		FunctionPowloka ret = new FunctionPowloka(f.re());
		ret.simplify(set);
		return ret;
	}

	public FunctionPowloka im(Settings set) throws WewnetzrnaFunkcjaZleZapisana {
		FunctionPowloka ret = new FunctionPowloka(f.im());
		ret.simplify(set);
		return ret;
	}
	
	
	public FunctionPowloka splitByRealAndImaginery(Settings set) throws WewnetzrnaFunkcjaZleZapisana {
		return new FunctionPowloka(new FuncSum (new Function[] {this.re(set).f, new FuncMult(new FuncNumConst(Complex.i), this.im(set).f)}));
	}
	
	public FunctionPowloka simplifyOnce(Settings settings) throws WewnetzrnaFunkcjaZleZapisana {
		 return new FunctionPowloka(f.simplify(settings));
	}
	
	public FunctionPowloka simplifyPom(Settings settings) throws WewnetzrnaFunkcjaZleZapisana {
		int i = 0;
		Function fLast = f;
		Function fNew = fLast.simplify(settings);
		while(!fNew.check(fLast)) {
			fLast = fNew;
			fNew = fNew.simplify(settings);
			i++;
			if(i >= 100) {
				System.out.println("Podczas FuncPowloka simplify po 100 iteracjach program nadal mówi, że jaszcze się nie skończyło.");
				break;
			}
		}
		return new FunctionPowloka(fNew);
	}
	
	public FunctionPowloka simplify(Settings settings) throws WewnetzrnaFunkcjaZleZapisana {
		if(settings.evaluateConstants) {
			Settings temp = settings.copy();
			temp.evaluateConstants = false;
			FunctionPowloka fp = simplifyPom(temp);
			fp = fp.simplifyPom(settings);
			return fp;
		}
		return simplifyPom(settings);
	}
	
	public void print(Settings set) {
		System.out.println(f.write(set));
	}
	
	/*static public void test1() throws WrongSyntaxException {
		System.out.println("Powinno zwrócić 2 + 2i");
		FunctionPowloka fp = new FunctionPowloka("Ln(z+i-1)/w*z[2]");
		FunctionPowloka fp2 = new FunctionPowloka("exp(2i)-i+1");
		FunctionPowloka fp3 = new FunctionPowloka("z[1]/z[2]");
		fp.putTogether("f");
		fp2.changeToConst("alpha");
		fp3.changeToVar("zmienna");
		FunctionPowloka h = new FunctionPowloka("f(alpha, 1/zmienna, 1/z[2])");
		h.print(Settings.defaultSettings);
		Complex g = h.evaluate(new Complex[] {Complex.i, new Complex(1,1), Complex.i});
		g.print();
		h.expand();
		h.print(Settings.defaultSettings);
		h.evaluate(new Complex[] {Complex.i, new Complex(1,1), Complex.i}).print();
		h.expand();
		h.print(Settings.defaultSettings);
		h.evaluate(new Complex[] {Complex.i, new Complex(1,1), Complex.i}).print();
		h.expand();
		h.print(Settings.defaultSettings);
		h.evaluate(new Complex[] {Complex.i, new Complex(1,1), Complex.i}).print();

	}*/
	
	static public void test2(String str) throws Exception {
		//TODO: klarownie coś nie działa dużo rzeczy
		Settings set1 = new Settings(3);
		set1.strictPow = true;
		set1.writePow = true;
		Settings set2 = new Settings(1);
		set2.strictPow = false;
		set2.writePow = true;
		Settings set3 = new Settings(1);
		set3.strictPow = true;
		set3.writePow = false;
		Settings set4 = new Settings(1);
		set4.strictPow = false;
		set4.writePow = false;
		
		FunctionPowloka g1 = new FunctionPowloka(str, set1);
		FunctionPowloka g2 = new FunctionPowloka(str, set2);
		FunctionPowloka g3 = new FunctionPowloka(str, set3);
		FunctionPowloka g4 = new FunctionPowloka(str, set3);
		System.out.println("Przed uproszczeniem, set1:");
		g1.print(set1);
		g2.print(set1);
		g3.print(set1);
		g4.print(set1);
		System.out.println("Przed uproszczeniem, set2:");
		g1.print(set2);
		g2.print(set2);
		g3.print(set2);
		g4.print(set2);
		System.out.println("Przed uproszczeniem, set3:");
		g1.print(set3);
		g2.print(set3);
		g3.print(set3);
		g4.print(set3);
		System.out.println("Przed uproszczeniem, set4:");
		g1.print(set4);
		g2.print(set4);
		g3.print(set4);
		g4.print(set4);
		g1.simplify(set3);
		g2.simplify(set4);
		g3.simplify(set1);
		g4.simplify(set2);
		System.out.println("Po uproszczeniu, set1:");
		g1.print(set1);
		g2.print(set1);
		g3.print(set1);
		g4.print(set1);
		System.out.println("Po uproszczeniu, set2:");
		g1.print(set2);
		g2.print(set2);
		g3.print(set2);
		g4.print(set2);
		System.out.println("Po uproszczeniu, set3:");
		g1.print(set3);
		g2.print(set3);
		g3.print(set3);
		g4.print(set3);
		System.out.println("Po uproszczeniu, set4:");
		g1.print(set4);
		g2.print(set4);
		g3.print(set4);
		g4.print(set4);

	}
	
	public static void main(String[] args) throws Exception {
		Settings set = new Settings(4);
		set.strictPow = false;
		//set.evaluateConstants = true;
		FunctionPowloka f = new FunctionPowloka("e+3e+2.1-2i*i+e*sin(x) - pi*e*phi*sin(x)", set);
		f.print(set);
		f.simplify(set);
		f.print(set);
		
		/*FunctionPowloka c = new FunctionPowloka("1/2+pi*e^2+i", set);
		c.simplify(set);
		f.simplify(set);
		f.print(set);
		f.changeToNamed("sin[2]");
		c.changeToVar("const");
		FunctionPowloka g = new FunctionPowloka("y", set);
		g.print(set);
		g.simplify(set);
		g.print(set);
		set.evaluateConstants = true;
		g.simplifyOnce(set);
		g.print(set);
		g.simplify(set);
		g.print(set);
		
		FunctionPowloka r = new FunctionPowloka("(x^2+y^2)^(1/2)",set);
		r.changeToVar("r");
		FunctionPowloka h = new FunctionPowloka("6.63*10^(-34)", set);
		h.changeToVar("h");
		FunctionPowloka rh = new FunctionPowloka("r+h+w",set);
		rh.changeToNamed("rh");
		FunctionPowloka rf = new FunctionPowloka("rh(z/z+i, 0)",set);
		System.out.println("rf:");
		rf.print(set);
		rf.simplify(set);
		rf.print(set);
		rf.expand();
		rf.print(set);
		rf.expand();
		rf.simplify(set);
		rf.print(set);*/
	}
}
