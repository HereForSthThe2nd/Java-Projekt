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
	public boolean check(Function f) {
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
	final int baseNofArgs;
	protected Func(String name, int nofArgs, Function[] args) {
		super(FuncMethods.countArguments(args), name);
		this.args = args;
		this.baseNofArgs = nofArgs;
	}
	@Override
	protected String write(Settings set) {
		if(baseNofArgs == 0) {
			return name + "()";
		}
		String str = name;
		str += "(";
		for(int i = 0;i < baseNofArgs-1; i++) {
			str += args[i].write(set) + ", ";
		}
		str += args[baseNofArgs-1].write(set) + ")";
		return str;
	}

	public final boolean check(Function f) {
		if(super.check(f))
			return FuncMethods.equals(((Func)f).args, args);
		return false;
	}

	
	@Override
	protected Function putArguments(Function[] args) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
			return this.getClass().getConstructor().newInstance(args);
	}

	
}

abstract class FuncDefault extends Func{
	protected FuncDefault(String name, int baseNofArgs, Function[] args) {
		super(name, baseNofArgs, args);
	}

	@Override
	protected Function expand() {
		return this;
	}
}

class FuncGivenName extends Func{
	final Function f;
	protected FuncGivenName(Function f, String name, int baseNofArgs, Function[] args) {
		super(name,baseNofArgs ,args);
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
	protected Function re() throws WewnetzrnaFunkcjaZleZapisana, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException { 
		return f.re();
	}

	@Override
	protected Function im() throws WewnetzrnaFunkcjaZleZapisana, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException { 
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
	protected Function re() throws WewnetzrnaFunkcjaZleZapisana, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException { 
		return f.re();
	}

	@Override
	protected Function im() throws WewnetzrnaFunkcjaZleZapisana, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException { 
		return f.im();
	}

}

class FuncConstGivenName extends FuncNamed{
	final Complex value;
	public FuncConstGivenName(String name, Function f) {
		super(0, name);
		value = f.evaluate(new Complex[] {});
	}
	
	@Override
	protected Function re() throws WewnetzrnaFunkcjaZleZapisana, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		return new FuncNumConst(new Complex((evaluate(new Complex[] {})).x));
	}

	@Override
	protected Function im() throws WewnetzrnaFunkcjaZleZapisana, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		return new FuncNumConst(new Complex((evaluate(new Complex[] {})).x));
	}

	@Override
	protected Function putArguments(Function[] args) throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		return this;
	}

	@Override
	protected Function expand() {
		return this;
	}

	@Override
	protected Complex evaluate(Complex[] arg) {
		return null;
	}
	
}