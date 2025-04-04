package funkcja;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;

abstract class Matcher  extends Function{
	final Function match;
	protected Matcher(int type, Function match) {
		super(type, match.nofArg);
		this.match = match;
		// TODO Auto-generated constructor stub
	}

	//abstract matchingParams f();
}

class AnyMatcher extends FuncNamed{
	Function currentMatch = null;
	protected AnyMatcher(int k) {
		super(Functions.NAMED, "Any["+k+"]");
	}

	@Override
	public boolean check(Function func) {
		if(currentMatch == null)
			return true;
		return currentMatch.check(func);
	}

	@Override
	protected Complex evaluate(Complex[] arg) {
		throw new IllegalArgumentException("Nie powinno nigdy tutaj dochodzić");
	}

	@Override
	protected Function re() throws WewnetzrnaFunkcjaZleZapisana, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		throw new IllegalArgumentException("Nie powinno nigdy tutaj dochodzić");
	}

	@Override
	protected Function im() throws WewnetzrnaFunkcjaZleZapisana, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		throw new IllegalArgumentException("Nie powinno nigdy tutaj dochodzić");
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
}

class matchingParams{
	LinkedList<Integer> elements = null; //indeksy jeśli matchowano sumę / iloczyn
	
}