/*
 * zawarte klasy związane z FuncNamed
 * */


package funkcja;

import java.lang.reflect.InvocationTargetException;

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

abstract class Func extends FuncNamed{
	final Function[] args;
	protected Func(int nofArgs, String name, Function[] args) {
		super(nofArgs, name);
		this.args = args;
	}
	
	@Override
	protected Function putArguments(Function[] args) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
			return this.getClass().getConstructor().newInstance(args);
	}

	
}

abstract class FuncDefault extends Func{

	protected FuncDefault(int nofArg, String name, Function[] args) {
		super(nofArg, name, args);
	}

	@Override
	protected Function expand() {
		return this;
	}
}

class FuncGivenName extends Func{
	final Function f;
	protected FuncGivenName(Function f, String name, Function[] args) {
		super(f.nofArg, name, args);
		this.f=f;
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
	protected Function re() throws WewnetzrnaFunkcjaZleZapisana { 
		return f.re();
	}

	@Override
	protected Function im() throws WewnetzrnaFunkcjaZleZapisana { 
		return f.im();
	}
}

abstract class FuncConstDefault extends FuncNamed{
	protected FuncConstDefault(String name) {
		super(0, name);
	}

	@Override
	protected Function putArguments(Function[] args) {
		return this;
	}
	
	@Override
	protected Function expand() {
		return this;
	}
}


class VarGivenName extends FuncNamed{
	final Function f;
	protected VarGivenName(String name, Function f) {
		super(f.nofArg, name);
		this.f=f;
	}

	@Override
	protected Complex evaluate(Complex[] arg) {
		return f.evaluate(arg);
	}

	@Override
	protected Function putArguments(Function[] args) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
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

}

final class FuncConstGivenName extends FuncNamed{
	final Function f;
	protected FuncConstGivenName(String name, Function f) {
		super(0, name);
		if(f.nofArg != 0) {
			throw new IllegalArgumentException("Liczba argumentów musi podanej funkcji musi być równa 0. Podana funkxja: " + f.write(new Settings()));
		}
		this.f=f;
	}

	@Override
	protected Complex evaluate(Complex[] arg) {
		return f.evaluate(new Complex[] {});
	}

	@Override
	protected Function putArguments(Function[] args) {
		return this;
	}
	
	@Override
	final protected Function expand() {
		return f;
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
}
