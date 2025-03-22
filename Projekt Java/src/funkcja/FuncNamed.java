package funkcja;

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
	final protected boolean equals(Function f) {
		if(f.type == Functions.NAMED) {
			if(((FuncNamed)f).name.equals(this.name))
				return true;
		}
		return false;
	}
	@Override
	protected Function simplify(Settings setting) {
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
	protected Bool<Function> expand() {
		return new Bool<Function>(this, false);
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
	protected Bool<Function> expand() {
		return new Bool<Function>(this, false);
	}
	@Override
	final protected Function simplify(Settings set) {
		if(set.evaluateConstants)
			return new FuncNumConst(evaluate(new Complex[] {}));
		return this;
	}
}

class FuncGivenName extends FuncNamed{
	final Function f;
	protected FuncGivenName(Function f, String name) {
		super(f.nofArg, name);
		this.f=f;
	}

	@Override
	protected Complex evaluate(Complex[] arg) {
		return f.evaluate(arg);
	}

	@Override
	protected Function putArguments(Function[] args) {
		return new FuncComp(this, args);
	}
	
	@Override
	protected Bool<Function> expand() {

		return new Bool<Function>(f,true);
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
	protected Function putArguments(Function[] args) {
		if(Functions.argsAreIdentities(args, f.nofArg))//TODO:nieprzetestowane jezcze
			return this;
		return f.putArguments(args);
	}
	
	@Override
	protected Bool<Function> expand() {
		return new Bool<Function>(f,true);
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
	final protected Function simplify(Settings set) {
		if(set.evaluateConstants)
			return new FuncNumConst(evaluate(new Complex[] {}));
		return this;
	}
	
	@Override
	final protected Bool<Function> expand() {
		return new Bool<Function>(f,true);
	}
}
