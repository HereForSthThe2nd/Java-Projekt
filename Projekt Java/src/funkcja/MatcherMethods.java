package funkcja;

import java.util.LinkedList;
import java.util.List;

import funkcja.MatcherMethods.MatcherReturn;
import ogolne.*;

//matchery mogą się przyrownać do wielu funkcji. 
//zapamiętują do której funkcji się przyrównały i w przyszłości mogą tylko do tej konkretnej funkcji się przypasować
class MatcherMethods{
		
	/*protected static MatcherReturn match(Function[] args, Function[] matchers) {
		FunctionInfo[] matcherInfo = FuncMethods.info(matchers);
		
	}*/
	
	final static class AnyMatcher extends FuncNamed{
		//dopasowuje się do jakiejkolwiek funkcji (any)
		Function currentMatch = null;
 		protected AnyMatcher(int k) { 
			super(Functions.NAMED, "Any["+k+"]");
		}
 		
		@Override
		public boolean check(Function func) {
			if(currentMatch == null) {
				currentMatch = func;
				return true;
			}
			return currentMatch.check(func);
		}

		@Override
		protected String write(Settings set) {
			if(currentMatch == null) 
				return super.write(set);
			return "({"+name+"}"+currentMatch.write(set)+")";
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
			return new FunctionInfo("");
		}
	}

	static class MatcherReturn extends VarReturnSpecial{
		//zarządza metcherami
		private LinkedList<String> matcherNames = new LinkedList<String>();
		private LinkedList<AnyMatcher> matcherList = new LinkedList<AnyMatcher>();
				
		protected static int returnNumber(String str) {
			//zakłada że wiadomo już, że str jest odpowiedniego rodzaju
			return Integer.parseInt(str.substring(4, str.length()-1));
		}

		@Override
		Function returnFunc(String name) {
			if(!matcherNames.contains(name)) {
				int k = returnNumber(name);
				matcherNames.add(name);
				matcherList.add(new AnyMatcher(k));
			}
			return matcherList.get(matcherNames.indexOf(name));
		}

		Function returnFunc(int k) {
			if(!matcherNames.contains("Any["+k+"]")) {
				AnyMatcher matcher = new AnyMatcher(k);
				matcherNames.add(matcher.name);
				matcherList.add(matcher);
			}
			return matcherList.get(matcherNames.indexOf("Any["+k+"]"));
		}

		void reset(List<String> matchers) {
			for(AnyMatcher i : this.matcherList)
				if(matchers.contains(i.name))
					i.currentMatch = null;
		}
		
		@Override
		boolean check(String str) {
			return str.matches("[Any]||(Any\\[[0-9]+\\])");
		}
	}
}

abstract class Pomiedzy extends Function{
	//klasa do wsadzania nad Sumy, iloczyny oraz funkcje z nawiasami
	//ma na celu zapamiętanie etapu na którym zostało zostawione sprawdzanie czy dwie funkcje są równe (czy można je dopasować by do siebie pasowały (.check))
	private Function funkcja;
	final String name;//tylko na cele teoretycznego ładnego wyświetlania
	
	protected Pomiedzy(Function funkcja, String name) {
		super(funkcja.type, funkcja.nofArg);
		this.funkcja = funkcja;
		this.name = name;
	}

	@Override
	protected Complex evaluate(Complex[] arg) {
		return funkcja.evaluate(arg);
	}

	@Override
	protected Function re() {
		funkcja = funkcja.re();
		return this;
	}

	@Override
	protected Function im() {
		funkcja = funkcja.im();
		return this;
	}

	@Override
	protected String write(Settings settings) {
		return name+"{"+funkcja.write(settings)+"}";
	}

	@Override
	protected Function putArguments(Function[] args) {
		funkcja = funkcja.putArguments(args);
		return this;
	}

	@Override
	protected Function replaceMatchers() {
		return funkcja;
	}

	@Override
	protected Function expand() {
		funkcja = funkcja.expand();
		return this;
	}

	@Override
	protected Function removeInners() {
		funkcja = funkcja.removeInners();
		return this;
	}

	@Override
	protected Function copyPom(MatcherReturn matcherRet) {
		throw new IllegalStateException("To nie powinno się wogóle stać");
	}

	@Override
	protected FunctionInfo info() {
		return funkcja.info();
	}

	@Override
	protected Function simplify(SimplifyRule rule) {
		funkcja = funkcja.simplify(rule);
		return this;
	}
}

//pamięta etap w check iloczynu oraz sumy
class CheckStageMS extends Pomiedzy{

	protected CheckStageMS(Function funkcja) {
		super(funkcja, "Pamiętacz");
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean check(Function func) {
		return false;
	}
	
}

//pamięta etap w check funkcji (nawiasowej)
class CheckstageFunc extends Pomiedzy{

	protected CheckstageFunc(Function funkcja) {
		super(funkcja, "Iloczyn");
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean check(Function func) {
		// TODO Auto-generated method stub
		return false;
	}
	
}