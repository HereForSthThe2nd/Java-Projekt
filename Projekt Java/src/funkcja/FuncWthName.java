/*
 * zawarte klasy związane z FuncNamed
 * */


package funkcja;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import Inne.Complex;

/*
 * funkcja typu funcnamed to np. exp, ln, oraz funkcje zdefiniowane przez użytkownika
 */
abstract public class FuncWthName extends Function{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5660417902115773356L;
	//żadne 2 funkcje FuncNamed nie mogą mieć tego samego pola name
	final public String name;
	public FuncWthName(int nofArg, String name) {
		super(Functions.NAMED, nofArg);
		this.name = name;
	}
	@Override
	public String write(Settings settings) {
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
	@Override
	protected Function expandSpecific(String name) {
		return this.check(name) ? this.expand() : this;
	}
}

abstract class FunctionDefault extends FuncWthName{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7394738283914950742L;

	protected FunctionDefault(int nofArg, String name) {
		super(nofArg, name);
	}

	@Override
	public Function putArguments(Function[] args) {
		return new FuncComp(this, args);
	}

	@Override
	public Function expand() {
		return this;
	}

	@Override
	Function removeDiff() {
		throw new IllegalArgumentException("Nie powinno do tegodojść " + name);
	}

	@Override
	protected LinkedList<String> checkDepecdencies() {
		return new LinkedList<String>(List.of(this.name));
	}
}

abstract class FuncConstDefault extends FuncWthName{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6608176858072865533L;

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
	public Function putArguments(Function[] args) {
		return this;
	}
	@Override
	public Function expand() {
		return this;
	}
	@Override
	Function removeDiff() {
		return this;
	}

	@Override
	protected LinkedList<String> checkDepecdencies() {
		return new LinkedList<String>(List.of(this.name));
	}
}

abstract class FuncSurrWthName extends FuncWthName{
	/**
	 * 
	 */
	private static final long serialVersionUID = -961712837872623963L;
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
	public Function expand() {
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

	@Override
	protected LinkedList<String> checkDepecdencies() {
		LinkedList<String>ret = f.checkDepecdencies();
		ret.add(name);
		return ret;
	}
}

class FuncGivenName extends FuncSurrWthName{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6691458202768274748L;

	protected FuncGivenName(Function f, String name) {
		super(name, f);
	}

	@Override
	public Function putArguments(Function[] args) {
		return new FuncComp(this, args);
	}
	
	@Override
	Function removeDiff() {
		throw new IllegalArgumentException("Nie powinno do tegodojść " + name);
	}
}

class VarGivenName extends FuncSurrWthName{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3694781086877775964L;

	protected VarGivenName(String name, Function f) {
		super(name, f);
	}

	public VarGivenName(String name, String f, int nofArg) {
		super(name, f, nofArg);
	}
	
	@Override
	public Function putArguments(Function[] args) {
		if(FuncMethods.argsAreIdentities(args, f.nofArg))//TODO:nieprzetestowane jezcze
			return this;
		return f.putArguments(args);
	}
	
	@Override
	public Function expand() {
		return f;
	}

	@Override
	Function removeDiff() {
		return this;
	}
}

final class FuncConstGivenName extends FuncSurrWthName{
	/**
	 * 
	 */
	private static final long serialVersionUID = 928736742916336200L;


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
	public Function putArguments(Function[] args) {
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
