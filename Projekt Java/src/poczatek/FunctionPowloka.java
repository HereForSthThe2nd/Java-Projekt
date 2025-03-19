package poczatek;

public class FunctionPowloka {
	Function f;
	//String canonicForm; gólne by było równe f.write, chyba że użytkownik dopiero przed chwilą wpisał funkcję i jeszcze jej w żaden sposób nie zmodyfikował... 
		//może jeśli zmieni w ustawieniach znaczenie pow to może to mogłoby mieć sens? 
		//Poza tym jeśli chce się to gdzieś przepisać bez zmian czy coś
	
	public FunctionPowloka(String str) throws WrongSyntaxException {
		f = Function.read(new BlokList(Function.preliminaryChanges(str)));
	}
	public FunctionPowloka(Function f) {
		this.f = f;
	}
	
	public Complex evaluate(Complex[] z) {
		return f.evaluate(z);
	}
		
	public void putTogether(String str) {
		f = new FuncGivenName(f, str);
		Functions.userFunctions.add((FuncNamed)f, str);
	}
	
	public void changeToVar(String str) {
		f = new FuncVar(str, f);
		Functions.userVar.add((FuncNamed)f, str);
	}
	
	public void changeToConst(String str) {
		f = new FuncConstGivenName(str, f);
		Functions.userVar.add((FuncNamed)f, str);
	}
	
	public boolean equals(FunctionPowloka fP) {
		return f.equals(fP.f);
	}
	
	public void expand() {

		f = f.expand().f;
	}
	
	public void simplifyOnce() {
		 f = f.simplify();
	}
	
	public void simplify() throws Exception {
		int i = 0;
		Function fNew = f.simplify();
		while(!fNew.equals(f)) {
			f = fNew;
			
			fNew = f.simplify();
			i++;
			if(i >= 5000)//zmienić na bardziej konkretny błąd, ale jeszcze nie wiem co
				throw new Exception("Podczas simplify po 5000 iteracjach program nadal mówi, że jaszcze się ni skończyło.");
		}
	}
	
	public void print(PrintSettings set) {
		System.out.println(f.write(set));
	}
	
	static public void test1() throws WrongSyntaxException {
		System.out.println("Powinno zwrócić 2 + 2i");
		FunctionPowloka fp = new FunctionPowloka("Ln(z+i-1)/w*z[2]");
		FunctionPowloka fp2 = new FunctionPowloka("exp(2i)-i+1");
		FunctionPowloka fp3 = new FunctionPowloka("z[1]/z[2]");
		fp.putTogether("f");
		fp2.changeToConst("alpha");
		fp3.changeToVar("zmienna");
		FunctionPowloka h = new FunctionPowloka("f(alpha, 1/zmienna, 1/z[2])");
		h.print(PrintSettings.defaultSettings);
		Complex g = h.evaluate(new Complex[] {Complex.i, new Complex(1,1), Complex.i});
		g.print();
		h.expand();
		h.print(PrintSettings.defaultSettings);
		h.evaluate(new Complex[] {Complex.i, new Complex(1,1), Complex.i}).print();
		h.expand();
		h.print(PrintSettings.defaultSettings);
		h.evaluate(new Complex[] {Complex.i, new Complex(1,1), Complex.i}).print();
		h.expand();
		h.print(PrintSettings.defaultSettings);
		h.evaluate(new Complex[] {Complex.i, new Complex(1,1), Complex.i}).print();

	}
	
	public static void main(String[] args) throws Exception {
		//System.out.println((new FunctionPowloka("exp(x^2+1+2)")).equals(new FunctionPowloka("exp(x^2+1+2)")));
		//FunctionPowloka f = new FunctionPowloka(new FuncSum(new Function[] {Function.read(new BlokList("exp(z)")), Function.read(new BlokList("9")), Function.read(new BlokList("exp(z)"))}));//new FunctionPowloka("exp(z)+9+exp(z)");
		FunctionPowloka g = new FunctionPowloka("exp(exp(Ln(2i*1/2^x)))*(1+i) + exp(exp(Ln(2i*1/2^x)))*(25)");
		//f.print(PrintSettings.defaultSettings);
		g.print(PrintSettings.defaultSettings);
		//f.simplify();
		g.simplify();
		//f.print(PrintSettings.defaultSettings);
		g.print(PrintSettings.defaultSettings);
		g.simplify();
		g.print(PrintSettings.defaultSettings);
	}
}
