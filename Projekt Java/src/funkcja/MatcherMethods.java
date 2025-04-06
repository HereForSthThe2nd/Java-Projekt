package funkcja;

import java.util.LinkedList;
import java.util.List;

import funkcja.MatcherMethods.MatcherReturn;
import ogolne.*;

//matchery mogą się przyrownać do wielu funkcji. 
//zapamiętują do której funkcji się przyrównały i w przyszłości mogą tylko do tej konkretnej funkcji się przypasować
class MatcherMethods{
		
	private static LinkedList<String> matchersToReset(int max, List<Integer> orderOfMatching, Function[] matchers){
		//TODO: zmieenić
		LinkedList<String> ret = new LinkedList<String>();
		for(int i=0;i<=max;i++) {
			ret.addAll(matchers[orderOfMatching.get(i)].info().containedAnyMatchers);
		}
		return ret;
	}
	
	protected static boolean match(Function[] args, Function[] matchers, RememberStageMS stage, MatcherReturn mr) {
		//dopasowuje matchers do args. kontynuuje od momentu stage i znajduje następne najbliższe dopasowanie
		//jeśli już niema więcej możliwych dopasowań zwraca false
		if(stage.returnsFalse)
			return false;
		FunctionInfo[] matcherInfo = FuncMethods.info(matchers);
		LinkedList<Integer>argsMatched = stage.argsMatched;//indeksy elemtów args
		LinkedList<Integer>staticMatchers = stage.staticMatchers;//matchery które się nie zmieniają bo wywołaniu .check
		LinkedList<Integer> orderOfMatching = stage.orderOfMatching;//indeksy kolejnych matcherów, które będą robione. Nie zawiera bezpośrednio matcherów które się zmieniają. (np.'sin(Any)' może zarzeć a 'Any' już nie)
		if(!stage.goneThroughOnce) {
			/*---sprawdza elementy które nie zawierają w sobie matcherów które mogą się zmieniać----*/
			for(int j=0;j<matchers.length;j++) {
				if(!matcherInfo[j].containedAnyMatchers.equals(new LinkedList<String>()))
					continue;
				staticMatchers.add(j);
				for(int i = 0;i<args.length;i++) {
					if(!argsMatched.contains(i) && matchers[j].check(args[i])) {
							argsMatched.add(i);
							break;
					}
				}
				stage.returnsFalse = true;
				return false;
			}
			/*-----*/
			for(int i = 0;i<matchers.length;i++) {
				if(argsMatched.contains(i) || matcherInfo[i].anyMatcherName.equals(""))
					continue; 
				//TODO:zmienić kolejność by usprawnić program
				orderOfMatching.add(i);
			}
			
		}
		if(staticMatchers.size() == args.length && staticMatchers.size() == matchers.length) {
			if(stage.goneThroughOnce)
				return false;
			else
				return true;
		}
		if(argsMatched.size() + orderOfMatching.size() > args.length) {
			stage.returnsFalse = true;
			return false;
		}
		/*---sprawdza matchery które są jeszcze w czymś zawarte / nie są bezpośrednio ( zawarte w orderOfMatching )-----*/
		int i0 = stage.orderOfMatchingIndeks;
		int j0 = stage.argsOrederOfMatchingIndeks;
		outer:
		for(int i = i0;i<orderOfMatching.size();i++) {
			for(int j = j0;j<args.length;j++) {
				if(argsMatched.contains(j))
					continue;
				List<String> startMatchers = mr.getNotDone(matchers[orderOfMatching.get(i)]);
				if(matchers[orderOfMatching.get(i)].check(args[j]))
					continue outer;
				else {
					mr.reset(startMatchers);
				}
			}
			
			stage.returnsFalse = true;
			return false;
		}
		/*-----*/
		stage.returnsFalse = true;
		return false;
	}
	
	final static class AnyMatcher extends FuncNamed{
		//dopasowuje się do jakiejkolwiek funkcji (any)
		Function currentMatch = null;
 		protected AnyMatcher(int k) { 
			super(Functions.NAMED, "Any["+k+"]");
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

		@Override
		protected boolean match(Function f, MatcherReturn mr) {
			if(currentMatch == null) {
				currentMatch = f;
				return true;
			}
			return currentMatch.check(f);
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

		protected List<String> getNotDone(Function function) {
			LinkedList<String> ret = new LinkedList<String>();
			LinkedList<String> inFunction = function.info().containedAnyMatchers;
			for(AnyMatcher m : matcherList) {
				if(inFunction.contains(m.name) && m.currentMatch == null)
					ret.add(m.name);
			}
			return ret;
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
	
	@Override
	public boolean check(Function func) {
		return funkcja.check(func);
	}
	
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

class RememberStageMS{
	int orderOfMatchingIndeks;
	int argsOrederOfMatchingIndeks;
	LinkedList<Integer> staticMatchers;
	LinkedList<Integer> orderOfMatching;
	LinkedList<Integer> argsMatched;
	boolean goneThroughOnce;
	boolean returnsFalse;
	public RememberStageMS() {
		orderOfMatching = new LinkedList<Integer>();
		argsMatched = new LinkedList<Integer>();
		staticMatchers = new LinkedList<Integer>();
		goneThroughOnce = false;
		returnsFalse = false;
		orderOfMatchingIndeks = 0;
		argsOrederOfMatchingIndeks = 0;
	}
}

//pamięta etap w check iloczynu oraz sumy
class CheckStageMS extends Pomiedzy{
	final private Function[] args;//summands dla sumy lub czynniki dla iloczynu
	final RememberStageMS stage;
	protected CheckStageMS(Function funkcja) {
		super(funkcja, "Pamiętacz");
		switch(funkcja.type) {
		case Functions.ADD:
			args = ((FuncSum)funkcja).summands;
			break;
		case Functions.MULT:
			args = ((FuncMult)funkcja).f;
			break;
		default:
			throw new IllegalArgumentException("Można podać tylko sumy oraz iloczyny jako argument. odany argument: " + 
					funkcja == null ? "null" : funkcja.write(new Settings()));
		}
		stage = new RememberStageMS();
	}

	@Override
	public boolean match(Function func, MatcherReturn mr) {
		Function[] args;
		switch(func.type) {
		case Functions.ADD:
			args = ((FuncSum)func).summands;
			break;
		case Functions.MULT:
			args = ((FuncMult)func).f;
			break;
		default:
			throw new IllegalArgumentException("Można podać tylko sumy oraz iloczyny jako argument. odany argument: " + 
					func == null ? "null" : func.write(new Settings()));
		}
		return MatcherMethods.match(args, this.args, stage, mr);
	}
	
}

//pamięta etap w check funkcji (nawiasowej)
class CheckstageFunc extends Pomiedzy{

	protected CheckstageFunc(Function funkcja) {
		super(funkcja, "Iloczyn");
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean match(Function func, MatcherReturn mr) {
		// TODO Auto-generated method stub
		return false;
	}
	
}