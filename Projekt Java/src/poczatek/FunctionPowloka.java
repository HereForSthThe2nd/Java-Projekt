package poczatek;

public class FunctionPowloka {
	Function f;
	//String canonicForm; gólne by było równe f.write, chyba że użytkownik dopiero przed chwilą wpisał funkcję i jeszcze jej w żaden sposób nie zmodyfikował... 
		//może jeśli zmieni w ustawieniach znaczenie pow to może to mogłoby mieć sens? 
		//Poza tym jeśli chce się to gdzieś przepisać bez zmian czy coś
	
	public FunctionPowloka(String str) throws WrongSyntaxException {
		f = Function.read(new BlokList(Function.preliminaryChanges(str)));
	}
	public FunctionPowloka(Function f, PrintSettings set) {
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
	
	public void expand() {
		f = f.expand();
	}
	
	public void print(PrintSettings set) {
		System.out.println(f.write(set));
	}
	public static void main(String[] args) throws WrongSyntaxException {
		FunctionPowloka fp = new FunctionPowloka("Ln(z+i-1)/w");
		System.out.println("a");
		FunctionPowloka fp2 = new FunctionPowloka("exp(2i)-i+1");
		System.out.println("b");
		fp2.changeToConst("alpha");
		System.out.println("c");
		fp.putTogether("f");
		System.out.println("d");
		FunctionPowloka h = new FunctionPowloka("f(alpha, z)");
		System.out.println("e");
		Complex g = h.evaluate(new Complex[] {Complex.i});
		System.out.println("f");
		g.print();
	}
}
