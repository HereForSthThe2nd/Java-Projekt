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
		mr = new MatcherReturn();
		SimplifyFunction sqOFsum =  new SimplifyFunction(new FuncSum(new Function[] {
					Functions.pow.returnFunc(new Function[] {mr.returnFunc(0), new FuncNumConst(new Complex(2))}), 
					new FuncMult(new Function[] {new FuncNumConst(new Complex(2)), mr.returnFunc(0), mr.returnFunc(1)}),
					Functions.pow.returnFunc(new Function[] {mr.returnFunc(1), new FuncNumConst(new Complex(2))})
				}),
				Functions.pow.returnFunc(new Function[] {
						new FuncSum(new Function[] {mr.returnFunc(0), mr.returnFunc(1)}), new FuncNumConst(new Complex(2))
				})
				);
		
		mr = new MatcherReturn();
		SimplifyFunction mult =  new SimplifyFunction(new FuncMult(new Function[] {
				mr.returnFunc(0), mr.returnFunc(1)
			}),
			mr.returnFunc(0)
		);

		mr = new MatcherReturn();
		SimplifyFunction pow =  new SimplifyFunction(Functions.pow.returnFunc(new Function[] {
				new FuncNumConst(new Complex(1)), new FuncNumConst(new Complex(1))
				}),
				new FuncNumConst(new Complex(1))
		);

		current.add(pow);
		//current.add(expOFlog);
		current.add(sqOFsum);
		//current.add(mult);
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
		System.out.println(f.write(new Settings()) + "  " +  matcherCopy.write(new Settings()));
		if(matcherCopy.match(f, mr)) {
			System.out.println("udalo sie");
			System.out.println(ret.write(new Settings()));
			Function ret = this.ret.copyPom(mr);
			System.out.println(ret.write(new Settings()));
			ret = ret.replaceMatchers();
			System.out.println(ret.write(new Settings()));
			return ret;
		}
		return f;
	}
}