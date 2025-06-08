/*
 * zawarte klasy związane z FuncNamed
 * */


package funkcja;

import Inne.Complex;

/*
 * funkcja typu funcnamed to np. exp, ln, oraz funkcje zdefiniowane przez użytkownika
 */
abstract class FuncWthName extends Function{
	//żadne 2 funkcje FuncNamed nie mogą mieć tego samego pola name
	final String name;
	public FuncWthName(int nofArg, String name) {
		super(Functions.NAMED, nofArg);
		this.name = name;
	}
	@Override
	protected String write(Settings settings) {
			return name;
	}
	@Override
	public
	final boolean check(Function f) {
		if(f.type == Functions.NAMED) {
			if(((FuncWthName)f).name.equals(this.name))
				return true;
		}
		return false;
	}
	@Override
	final protected Function simplify(Settings setting) {
		if(setting.evaluateConstants && nofArg == 0)
			return new FuncNumConst( evaluate(new Complex[] {}) );
		return this;
	}
	boolean check(String str) {
		return str.equals(name);
	}
}

abstract class FunctionDefault extends FuncWthName{

	protected FunctionDefault(int nofArg, String name) {
		super(nofArg, name);
	}

	@Override
	protected Function putArguments(Function[] args) {
		return new FuncComp(this, args);
	}

	@Override
	protected Function expand() {
		return this;
	}

	@Override
	Function removeDiff() {
		throw new IllegalArgumentException("Nie powinno do tegodojść " + name);
	}
}

abstract class FuncConstDefault extends FuncWthName{
	protected FuncConstDefault(String name) {
		super(0, name);
	}
	@Override
	final protected Function diffX(int arg, Settings set) {
		return new FuncNumConst(new Complex(0));
	}
	@Override
	final protected Function diffY(int arg, Settings set) {
		return new FuncNumConst(new Complex(0));
	}
	@Override
	protected Function putArguments(Function[] args) {
		return this;
	}
	@Override
	protected Function expand() {
		return this;
	}
	@Override
	Function removeDiff() {
		return this;
	}
}

abstract class FuncSurrWthName extends FuncWthName{
	final Function f;

	public FuncSurrWthName(String name, Function f) {
		super(f.nofArg, name);
		this.f = f;
	}
	
	public FuncSurrWthName(String name, String f, int nofArg) {
		super(nofArg, name);
		try {
			this.f = new FunctionPowloka(f, new Settings()).getFunction();
		} catch (FunctionExpectedException e) {
			throw new IllegalStateException("Wewnątrz programu. Wpisana funkcja: " + f + ", błąd: " + e);
		}
	}
	
	@Override
	protected Complex evaluate(Complex[] arg) {
		return f.evaluate(arg);
	}

	@Override
	protected Function expand() {
		return f;
	}

	@Override
	protected Function diffX(int arg, Settings set) {
		return f.diffX(arg, set);
	}
	
	@Override
	protected Function diffY(int arg, Settings set) {
		return f.diffY(arg, set);
	}
	
	@Override
	protected Function[] reim() { 
		return f.reim();
	}	
}

class FuncGivenName extends FuncSurrWthName{
	protected FuncGivenName(Function f, String name) {
		super(name, f);
	}

	@Override
	protected Function putArguments(Function[] args) {
		return new FuncComp(this, args);
	}
	
	@Override
	Function removeDiff() {
		throw new IllegalArgumentException("Nie powinno do tegodojść " + name);
	}
}

class VarGivenName extends FuncSurrWthName{
	protected VarGivenName(String name, Function f) {
		super(name, f);
	}

	public VarGivenName(String name, String f, int nofArg) {
		super(name, f, nofArg);
	}
	
	@Override
	protected Function putArguments(Function[] args) {
		if(FuncMethods.argsAreIdentities(args, f.nofArg))//TODO:nieprzetestowane jezcze
			return this;
		return f.putArguments(args);
	}
	
	@Override
	protected Function expand() {
		return f;
	}

	@Override
	Function removeDiff() {
		return this;
	}
}

final class FuncConstGivenName extends FuncSurrWthName{
	protected FuncConstGivenName(String name, Function f) {
		super(name, f);
		if(f.nofArg != 0) {
			try {
				throw new IllegalArgumentException("Liczba argumentów musi podanej funkcji musi być równa 0. Podana funkxja: " + f.write(new Settings()));
			} catch(FunctionExpectedException e) {
				throw new IllegalArgumentException("Liczba argumentów musi podanej funkcji musi być równa 0. Nie udało się jej wypisać");
			}
		}
	}

	@Override
	protected Function putArguments(Function[] args) {
		return this;
	}
	

	@Override
	protected Function[] reim() { 
		if(evaluate(new Complex[] {}).y == 0)
			return new Function[] {this, new FuncNumConst(new Complex(0))};
		if(evaluate(new Complex[] {}).x == 0)
			return new Function[] {new FuncNumConst(new Complex(0)), this};
		return f.reim();
	}

	
	@Override
	Function removeDiff() {
		return this;
	}
}
