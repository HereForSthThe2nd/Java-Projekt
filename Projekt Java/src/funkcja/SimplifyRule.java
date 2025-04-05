package funkcja;

import funkcja.MatcherMethods.AnyMatcherReturn;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import funkcja.MatcherMethods.AnyMatcher;


abstract class SimplifyRule {
	static final SimplifyFunction eOfl = new SimplifyFunction(Functions.exp.returnFunc(new Function[] {Functions.ln.returnFunc(new Function[] {new AnyMatcher(0)})}));
	static LinkedList<SimplifyRule> current = new LinkedList<SimplifyRule>(List.of(eOfl));
	abstract Function simplify(Function f);
}

class SimplifyFunction extends SimplifyRule implements FuncChecker{
	final Function matcher;
	protected SimplifyFunction(Function matcher) {
		this.matcher = matcher;
	}
	
	@Override
	public boolean check(Function func) {
		//jak na razie nie radzi sobie dobrze z sumami oraz iloczynami
		return matcher.check(func);
	}

	@Override
	Function simplify(Function f) {
		if(check(f))
			return matcher.replaceMatchers();
		return f;
	}
}