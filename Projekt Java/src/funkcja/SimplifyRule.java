package funkcja;


import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import funkcja.MatcherMethods.AnyMatcher;
import funkcja.MatcherMethods.MatcherReturn;
import ogolne.*;


public abstract class SimplifyRule {
	static LinkedList<SimplifyRule> current = new LinkedList<SimplifyRule>();
	
	public static void init() {
		MatcherReturn mr = new MatcherReturn();
		SimplifyFunction expOFlog =  new SimplifyFunction(
				Functions.exp.returnFunc(new Function[] {Functions.ln.returnFunc(new Function[] {mr.returnFunc(0)})}),mr.returnFunc(0));
		current.add(expOFlog);
	}
	
	abstract Function simplify(Function f);
}

class SimplifyFunction extends SimplifyRule{
	final private Function matcher;
	final private Function ret;
 	protected SimplifyFunction(Function matcher, Function ret) {
		this.matcher = matcher.removeInners();
		this.ret = ret;
	}
	
	@Override
	Function simplify(Function f) {
		final MatcherReturn mr = new MatcherReturn();
		Function matcherCopy = matcher.copyPom(mr);
		if(matcherCopy.check(f)) {
			return ret.copyPom(mr).replaceMatchers();
		}
		return f;
	}
}