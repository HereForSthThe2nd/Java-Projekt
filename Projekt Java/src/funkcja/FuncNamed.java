/*
 * zawarte klasy związane z FuncNamed
 * */


package funkcja;

import Inne.Complex;

/*
 * funkcja typu funcnamed to np. exp, ln, oraz funkcje zdefiniowane przez użytkownika
 */
abstract class FuncNamed extends Function{
	//żadne 2 funkcje FuncNamed nie mogą mieć tego samego pola name
	final String name;
	public FuncNamed(int nofArg, String name) {
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
			if(((FuncNamed)f).name.equals(this.name))
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
}

abstract class FuncDefault extends FuncNamed{

	protected FuncDefault(int nofArg, String name) {
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

abstract class FuncConstDefault extends FuncNamed{
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

abstract class UserFunction extends FuncNamed{
	final Function f;

	public UserFunction(String name, Function f) {
		super(f.nofArg, name);
		this.f = f;
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
	protected Function re() throws WewnetzrnaFunkcjaZleZapisana { 
		return f.re();
	}

	@Override
	protected Function im() throws WewnetzrnaFunkcjaZleZapisana { 
		return f.im();
	}
	
}

class FuncGivenName extends UserFunction{
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

class VarGivenName extends UserFunction{
	protected VarGivenName(String name, Function f) {
		super(name, f);
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
	protected Function re() throws WewnetzrnaFunkcjaZleZapisana { 
		return f.re();
	}

	@Override
	protected Function im() throws WewnetzrnaFunkcjaZleZapisana { 
		return f.im();
	}

	@Override
	Function removeDiff() {
		return this;
	}
}

final class FuncConstGivenName extends UserFunction{
	protected FuncConstGivenName(String name, Function f) {
		super(name, f);
		if(f.nofArg != 0) {
			throw new IllegalArgumentException("Liczba argumentów musi podanej funkcji musi być równa 0. Podana funkxja: " + f.write(new Settings()));
		}
	}

	@Override
	protected Function putArguments(Function[] args) {
		return this;
	}
	

	@Override
	protected Function re() throws WewnetzrnaFunkcjaZleZapisana { 
		if(evaluate(new Complex[] {}).y == 0)
			return this;
		return f.re();
	}

	@Override
	protected Function im() throws WewnetzrnaFunkcjaZleZapisana { 
		if(evaluate(new Complex[] {}).x == 0)
			return this;
		return f.im();
	}
	
	@Override
	Function removeDiff() {
		return this;
	}
}
