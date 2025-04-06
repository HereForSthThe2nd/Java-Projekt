/*
 * zawarte klasy związane z FuncNamed
 * */


package funkcja;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import funkcja.MatcherMethods.MatcherReturn;
import ogolne.Complex;
import ogolne.Settings;

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
		if(super.check(f) && args.length == ((Func)f).args.length) {
			for(int i=0;i<args.length;i++) {
				if(! args[i].check(((Func)f).args[i]) ) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
		
	@Override
	protected Function replaceMatchers() {
		return Functions.returnNmdFuncReturner(name).returnFunc(FuncMethods.replaceMatchers(args));
	} 

	protected Function putArguments(Function[] args) {
		return Functions.returnNmdFuncReturner(name).returnFunc(FuncMethods.putArguments(this.args, args));
	}

	@Override
	protected Function removeInners() {
		return Functions.returnNmdFuncReturner(name).returnFunc(FuncMethods.removeInners(args));
	}
	
	@Override
	protected Function copyPom(MatcherReturn matcherRet) {
		return Functions.returnNmdFuncReturner(name).returnFunc(FuncMethods.copyAll(args, matcherRet));
	}
	
	@Override
	protected Function simplify(SimplifyRule rule) {
		return rule.simplify(Functions.returnNmdFuncReturner(name).returnFunc(FuncMethods.simplifyAll(args, rule)));
	}

	@Override
	protected FunctionInfo info() {
		return new FunctionInfo(FuncMethods.info(args), false);
	}

	@Override
	protected boolean match(Function f, MatcherReturn mr) {
		throw new IllegalStateException("Przy wywoływaniu match funkcja powinna najpierw zostać skopiowana");
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
	protected Function re() { 
		return f.re();
	}

	@Override
	protected Function im() { 
		return f.im();
	}

}

abstract class Variable extends FuncNamed{
	//nie może być np. Any ( w sensie matchera )
	public Variable(int nofArg, String name) {
		super(nofArg, name);
	}
	
	@Override
	final protected Function removeInners() {
		return this;
	}
	
	@Override
	final protected Function replaceMatchers() {
		return this;
	}
	
	@Override
	final protected Function simplify(SimplifyRule rule) {
		return rule.simplify(this);
	}
	
	@Override
	final protected Function copyPom(MatcherReturn matcherRet) {
		return this;
	}

	@Override
	protected FunctionInfo info() {
		return new FunctionInfo("");
	}
}


abstract class VarDefault extends Variable{
	public VarDefault(int nofArg, String name) {
		super(nofArg, name);
	}

	@Override
	final protected Function expand() {
		return this;
	}
}

abstract class FuncConstDefault extends VarDefault{
	protected FuncConstDefault(String name) {
		super(0, name);
	}
	
	@Override
	final protected Function putArguments(Function[] args) {
		return this;
	}
	
	@Override
	protected boolean match(Function f, MatcherReturn mr) {
		return check(f);
	}
}

class VarGivenName extends Variable{
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
	protected Function putArguments(Function[] args) {
		if(FuncMethods.argsAreIdentities(args, f.nofArg))
			return this;
		return f.putArguments(args);
	}
	
	@Override
	protected Function expand() {
		return f;
	}

	@Override
	protected Function re() { 
		return f.re();
	}

	@Override
	protected Function im() { 
		return f.im();
	}

	@Override
	protected boolean match(Function f, MatcherReturn mr) {
		return check(f);
	}
}

class FuncConstGivenName extends Variable{
	final Complex value;
	public FuncConstGivenName(String name, Function f) {
		super(0, name);
		value = f.evaluate(new Complex[] {});
	}
	
	@Override
	final protected Function re() {
		return new FuncNumConst(new Complex((evaluate(new Complex[] {})).x));
	}

	@Override
	final protected Function im() {
		return new FuncNumConst(new Complex((evaluate(new Complex[] {})).x));
	}

	@Override
	final protected Function putArguments(Function[] args) {
		return this;
	}

	@Override
	final protected Function expand() {
		return this;
	}

	@Override
	final protected Complex evaluate(Complex[] arg) {
		return value;
	}

	@Override
	protected boolean match(Function f, MatcherReturn mr) {
		return check(f);
	}
}