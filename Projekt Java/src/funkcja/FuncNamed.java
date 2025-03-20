package funkcja;

abstract class FuncNamed extends Function{
	//żadne 2 funkcje FuncNamed nie mogą mieć tego samego pola name
	final String name;
	public FuncNamed(int nofArg, String name) {
		super(Functions.NAMED, nofArg);
		this.name = name;
	}
	@Override
	public String write(Settings settings) {
			return name;
	}
	@Override
	public boolean equals(Function f) {
		if(f.type == Functions.NAMED) {
			if(((FuncNamed)f).name.equals(this.name))
				return true;
		}
		return false;
	}
	@Override
	public Function simplify(Settings setting) {
		return this;
	}

}

abstract class FuncDefault extends FuncNamed{

	public FuncDefault(int nofArg, String name) {

		super(nofArg, name);
	}

	@Override
	public Function putArguments(Function[] args) {
		return new FuncComp(this, args);
	}

	@Override
	public Bool<Function> expand() {
		return new Bool<Function>(this, false);
	}
}

abstract class FuncConstDefault extends FuncNamed{
	public FuncConstDefault(String name) {
		super(0, name);
	}

	@Override
	public Function putArguments(Function[] args) {
		return this;
	}
	@Override
	public Bool<Function> expand() {
		return new Bool<Function>(this, false);
	}
}

class FuncGivenName extends FuncNamed{
	final Function f;
	public FuncGivenName(Function f, String name) {
		super(f.nofArg, name);
		this.f=f;
	}

	@Override
	public Complex evaluate(Complex[] arg) {
		return f.evaluate(arg);
	}

	@Override
	public Function putArguments(Function[] args) {
		return new FuncComp(this, args);
	}
	
	@Override
	public Bool<Function> expand() {

		return new Bool<Function>(f,true);
	}
}

class FuncVar extends FuncNamed{
	final Function f;
	public FuncVar(String name, Function f) {
		super(f.nofArg, name);
		this.f=f;
	}

	@Override
	public Complex evaluate(Complex[] arg) {
		return f.evaluate(arg);
	}

	@Override
	public Function putArguments(Function[] args) {
		return f.putArguments(args);
	}
	
	@Override
	public Bool<Function> expand() {
		return new Bool<Function>(f,true);
	}

}

class FuncConstGivenName extends FuncNamed{
	final Function f;
	public FuncConstGivenName(String name, Function f) {
		super(0, name);
		if(f.nofArg != 0) {
			throw new IllegalArgumentException("Liczba argumentów musi podanej funkcji musi być równa 0. Podana funkxja: " + f.write(new Settings()));
		}
		this.f=f;
	}

	@Override
	public Complex evaluate(Complex[] arg) {
		return f.evaluate(new Complex[] {});
	}

	@Override
	public Function putArguments(Function[] args) {
		return this;
	}

	@Override
	public Bool<Function> expand() {
		return new Bool<Function>(f,true);
	}
}
