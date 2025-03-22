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
	public FunctionPowloka(Function f) {
		this.f = f;
	}
	
	public Complex evaluate(Complex[] z) {
		return f.evaluate(z);
	}
		
	public void changeToNamed(String str) throws IncorrectNameException {
		f = Functions.addNmdFunc(f, str);
	}
	
	public void changeToVar(String str) throws IncorrectNameException {
		f = Functions.addVar(f, str);
	}
		
	public boolean equals(FunctionPowloka fP) {
		return f.equals(fP.f);
	}
	
	public void expand() {

		f = f.expand().f;
	}
	
	public void simplifyOnce(Settings settings) {
		 f = f.simplify(settings);
	}
	
	public void simplify(Settings settings) throws Exception {
		int i = 0;
		Function fNew = f.simplify(settings);
		while(!fNew.equals(f)) {
			f = fNew;
			fNew = f.simplify(settings);
			i++;
			if(i >= 100) {
				System.out.println("Podczas FuncPowloka simplify po 100 iteracjach program nadal mówi, że jaszcze się nie skończyło.");
				break;
			}
		}
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
		Settings set = new Settings();
		FunctionPowloka f = new FunctionPowloka("1/2/i*(exp(z*i)-exp(-z*i)) + z[1]+z[2]", set);
		FunctionPowloka c = new FunctionPowloka("1/2+pi*e^2+i", set);
		c.simplify(set);
		f.simplify(set);
		f.print(set);
		f.changeToNamed("sin");
		c.changeToVar("const");
		FunctionPowloka g = new FunctionPowloka("4pi*x*e - 2*2/2*8*2pi*e*x", set);
		g.print(set);
		g.simplify(set);
		g.print(set);
		set.evaluateConstants = true;
		g.simplifyOnce(set);
		g.print(set);
		g.simplify(set);
		g.print(set);
		/*
		FunctionPowloka r = new FunctionPowloka("(x^2+y^2)^(1/2)",set);
		r.changeToVar("r");
		FunctionPowloka h = new FunctionPowloka("6.63*10^(-34)", set);
		h.changeToVar("h");
		FunctionPowloka rh = new FunctionPowloka("r+h+w",set);
		rh.changeToNamed("rh");
		FunctionPowloka rf = new FunctionPowloka("rh(z/z, 0)",set);
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
