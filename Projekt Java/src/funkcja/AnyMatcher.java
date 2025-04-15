package funkcja;

import ogolne.Complex;
import ogolne.Settings;

abstract class Matcher extends FuncNamed{
	Function currentMatch = null;
	public Matcher(int nofArg, String name) {
		super(nofArg, name);
	}

	@Override
	public boolean check(Function f) {
		return currentMatch.check(f);
	}
	
	@Override
	protected void resetPomiedzy() {}

	@Override
	protected String write(Settings set) {
		if(currentMatch == null)
			return name+"{}";
		return name+"{="+currentMatch.write(set)+"}";
	}

	@Override
	protected Complex evaluate(Complex[] arg) {
		throw new IllegalStateException("Nie powinno nigdy tutaj dochodzić");
	}

	@Override
	protected Function re() {
		throw new IllegalStateException("Nie powinno nigdy tutaj dochodzić");
	}

	@Override
	protected Function im() {
		throw new IllegalStateException("Nie powinno nigdy tutaj dochodzić");
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
	protected Function replaceMatchers() {
		if(currentMatch == null) {
			(new Exception("Na razie program nie powinien tutaj dochodzić, może kiedyś w przyszłości to się przyda")).printStackTrace();
			return this;
		}
		return currentMatch;
	}

	@Override
	protected Function removeInners() {
		return this;
	}

	@Override
	protected Function simplify(SimplifyRule rule) {
		return this;
	}

	@Override
	protected Function copyPom(MatcherReturn matcherRet) {
		return matcherRet.returnFunc(name);
	}

	@Override
	protected FunctionInfo info() {
		if(currentMatch == null)
			return new FunctionInfo(name);
		return new FunctionInfo(null);
	}
	
}

class AnyMatcher extends Matcher{
	//dopasowuje się do jakiejkolwiek funkcji (any)
	Function currentMatch = null;
	protected AnyMatcher(int k) { 
		super(Functions.NAMED, "Any["+k+"]");
	}
	
	static boolean checkIfAnyMatcher(String str) {
		return str.matches("[Any]||(Any\\[[0-9]+\\])");
	}
	
	

	@Override
	protected boolean match(Function f, MatcherReturn mr) {
		if(currentMatch == null) {
			currentMatch = f;
			return true;
		}
		return currentMatch.check(f);
	}

}