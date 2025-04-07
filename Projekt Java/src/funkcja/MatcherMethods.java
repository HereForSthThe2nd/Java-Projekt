package funkcja;

import java.util.LinkedList;
import java.util.List;

import funkcja.MatcherMethods.MatcherReturn;
import ogolne.*;

//matchery mogą się przyrownać do wielu funkcji. 
//zapamiętują do której funkcji się przyrównały i w przyszłości mogą tylko do tej konkretnej funkcji się przypasować
class MatcherMethods{
		
	protected static boolean match(Function matcher,Function arg, MatcherReturn mr) {
		int marker = mr.setMarker();
		if(matcher.match(arg, mr)) {
			return true;
		}
		else {
			mr.resetTo(marker);
			matcher.resetPomiedzy();
			return false;
		}
	}
	
	protected static boolean match(Function[] args, Function[] matchers, RememberStageMS stage, MatcherReturn mr, int startMarker) {
		System.out.println("dotarlo sie tutaj 1");
		//dopasowuje matchers do args. kontynuuje od momentu stage i znajduje następne najbliższe dopasowanie
		//jeśli już niema więcej możliwych dopasowań zwraca false
		//TODO: dodać aby przed iściem do przodu sprawdziło czy w tych samych pozycjach może jeszcze coś zrobić
		if(stage.returnsFalse)
			return false;
		final FunctionInfo[] matcherInfo = FuncMethods.info(matchers);
		final LinkedList<Integer>argsMatched = stage.argsMatched;//indeksy elemtów args
		final LinkedList<Integer>staticMatchers = stage.staticMatchers;//matchery które się nie zmieniają bo wywołaniu .match
		final LinkedList<Integer> matchers2 = stage.matchers2;//indeksy kolejnych matcherów, które będą robione. Nie zawiera bezpośrednio matcherów które się zmieniają. (np.'sin(Any)' może zarzeć a 'Any' już nie)
		final LinkedList<Integer> matcherToArgsMap = stage.matcherToArgMap;//w sumie to zawiera informację również o argsMatched
		System.out.println("dotarlo sie tutaj 2");
		if(!stage.goneThroughOnce) {
			System.out.println("dotarlo sie tutaj 3");
			for(int i = 0;i<matchers.length;i++) {
				matcherToArgsMap.add(-1);
			}
			
			/*---sprawdza elementy które nie zawierają w sobie matcherów które mogą się zmieniać----*/
			for(int j=0;j<matchers.length;j++) {
				if(!matcherInfo[j].containedAnyMatchers.equals(new LinkedList<String>()))
					continue;
				System.out.println("dotarlo sie tutaj 3.01: " + matcherInfo[j].containedAnyMatchers);
				staticMatchers.add(j);
				for(int i = 0;i<args.length;i++) {
					if(!argsMatched.contains(i) && matchers[j].check(args[i])) {
						matcherToArgsMap.set(j, i);
						argsMatched.add(i);
						break;
					}
				}
				System.out.println("dotarlo sie tutaj 3.1");
				stage.returnsFalse = true;
				return false;
			}
			
			/*-----*/
			for(int i = 0;i<matchers.length;i++) {
				if(argsMatched.contains(i))
					continue; 
				//TODO:zmienić kolejność by usprawnić program
				matchers2.add(i);
			}
			
		}
		System.out.println("dotarlo sie tutaj 4");
		if(staticMatchers.size() == args.length && staticMatchers.size() == matchers.length) {
			if(stage.goneThroughOnce) 
				return false;
			else
				return true;
		}
		if(staticMatchers.size() + matchers2.size() > args.length || (staticMatchers.size() + matchers2.size() == matchers.length && matchers.length < args.length)) {
			stage.returnsFalse = true;
			return false;
		}
		/*---sprawdza matchery które są jeszcze w czymś zawarte / nie są bezpośrednio mtcherami ( są zawarte w orderOfMatching ) -----*/
		//( bardzo )niezoptymalizowane
		System.out.println("dotarlo sie tutaj 5");
		System.out.println(matchers2.size());
		outer:
		for(int i = stage.i0;i<matchers2.size();i++) {
			for(int j = 0;j<args.length;j++) {
				if(i == stage.i0 && j < stage.j0) {
					j = stage.j0;
				}
				if(argsMatched.contains(j))
					continue;
				System.out.println("i, j: " + i + ", " + j);
				if(match(matchers[matchers2.get(i)], args[j], mr)) {
					argsMatched.add(j);
					matcherToArgsMap.set(matchers2.get(i), j);
					continue outer;
				}
			}
			
			if(i == 0) {
				System.out.println("dotarlo sie tutaj 5.1");
				stage.returnsFalse = true;
				return false;
			}
			
			if(match(matchers[matchers2.get(i-1)], args[matcherToArgsMap.get(matchers2.get(i-1))], mr)) {
				i = i-1;
				stage.i0 = i;
				stage.j0 = 0;
				continue outer;
			}else {
				argsMatched.removeLast();
				matcherToArgsMap.set(matchers2.get(i-1), -1);
				i = i-2;
				stage.i0 = i-1;
				stage.j0 = 0;
				continue outer;
			}
		}
		/*-----*/
		System.out.println("dotarlo sie tutaj 6");
		stage.i0 = matchers2.size()-1;
		stage.j0 = matcherToArgsMap.get(matchers2.get(stage.i0));
		//warunek początkowy rekurencji:
		if(matcherInfo[ matchers2.getLast() ].matchersContainedInSM.equals(new LinkedList<List<String>>())) {
			stage.j0++;
			while(stage.i0 >= 0) {
				while(stage.j0 < args.length) {
					if(argsMatched.contains(stage.j0)) {
						stage.j0++;
						continue;
					}
					break;
				}
				if(stage.j0 == args.length) {
					argsMatched.removeLast();
					matcherToArgsMap.set(matchers2.get(stage.i0), -1);
					stage.j0 = 0;
					stage.i0--;
				}
				else
					break;
			}
		}
		System.out.println("dotarlo sie tutaj 7");
		if(stage.i0 < 0)
			stage.returnsFalse = true;
		return true;
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

		@Override
		protected void resetPomiedzy() {}
	}

	static class MatcherReturn extends VarReturnSpecial{
		
		//zarządza metcherami
		final private LinkedList<String> matcherNames = new LinkedList<String>();
		final private LinkedList<AnyMatcher> matcherList = new LinkedList<AnyMatcher>();
		private LinkedList<LinkedList<AnyMatcher>> situationAtMarker = new LinkedList<LinkedList<AnyMatcher>>();
		
 		protected static int returnNumber(String str) {
			//zakłada że wiadomo już, że str jest odpowiedniego rodzaju
			return Integer.parseInt(str.substring(4, str.length()-1));
		}

		public void resetAgo(int i) {
			resetTo(situationAtMarker.size()-i);
		}

		List<String> getNotDone(Function function) {
			LinkedList<String> ret = new LinkedList<String>();
			LinkedList<String> inFunction = function.info().containedAnyMatchers;
			for(AnyMatcher m : matcherList) {
				if(inFunction.contains(m.name) && m.currentMatch == null)
					ret.add(m.name);
			}
			return ret;
		}
		
		LinkedList<AnyMatcher> getMatched(){
			LinkedList<AnyMatcher> ret = new LinkedList<MatcherMethods.AnyMatcher>();
			for(AnyMatcher m : matcherList)
				if(m.currentMatch != null)
					ret.add(m);
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
	
		void resetTo(int marker) {
			for(AnyMatcher i : matcherList) {
				if(!situationAtMarker.get(marker).contains(i))
					i.currentMatch = null;
			}
			for(int i = marker+1;i<situationAtMarker.size();i++) {
				situationAtMarker.removeLast();
			}
		}
		
		int setMarker() {
			//zakłada że po użyciu resetTo(marker) już się nie użyje resetTo(>marker) 
			situationAtMarker.add(getMatched());
			return situationAtMarker.size()-1;
		}
		
	}
}

abstract class Pomiedzy extends Function{
	//klasa do wsadzania nad Sumy, iloczyny oraz funkcje z nawiasami
	//ma na celu zapamiętanie etapu na którym zostało zostawione sprawdzanie czy dwie funkcje są równe (czy można je dopasować by do siebie pasowały (.check))
	Function funkcja;
	final String name;//tylko na cele teoretycznego ładnego wyświetlania
	
	@Override
	public boolean check(Function func) {
		return funkcja.check(func);
	}
	
	protected Pomiedzy(Function funkcja, String name) {
		super(Functions.SPECIAL, funkcja.nofArg);
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
		return funkcja.replaceMatchers();
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
	final protected FunctionInfo info() {
		return funkcja.info();
	}

	@Override
	protected Function simplify(SimplifyRule rule) {
		funkcja = funkcja.simplify(rule);
		funkcja = rule.simplify(funkcja);
		
		return this;
	}

	@Override
	protected void resetPomiedzy() { 
		funkcja.resetPomiedzy();
	}
}

class RememberStageMS{
	LinkedList<Integer> matcherToArgMap;
	int i0;
	int j0;
	LinkedList<Integer> staticMatchers;
	LinkedList<Integer> matchers2;
	LinkedList<Integer> argsMatched;
	boolean goneThroughOnce;
	boolean returnsFalse;
	public RememberStageMS() {
		reset();
	}
	public void reset() {
		matcherToArgMap = new LinkedList<Integer>();
		matchers2 = new LinkedList<Integer>();
		argsMatched = new LinkedList<Integer>();
		staticMatchers = new LinkedList<Integer>();
		goneThroughOnce = false;
		returnsFalse = false;
		i0 = 0;
		j0 = 0;
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
			return false;
			}
		int startMarker = mr.setMarker();
		boolean ret = MatcherMethods.match(args, this.args, stage, mr, startMarker);
		return ret;
	}

	@Override
	protected void resetPomiedzy() {
		super.resetPomiedzy();
		stage.reset();
	}
	
}


//TODO: najprawdopoobniej zbędne, usunąć kiedyś
//pamięta etap w check funkcji (nawiasowej)
class CheckstageFunc extends Pomiedzy{

	protected CheckstageFunc(Function funkcja) {
		super(funkcja, "PomiedzFunc");
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean match(Function func, MatcherReturn mr) {
		if(func.type != Functions.NAMED || !((FuncNamed)funkcja).name.equals(((FuncNamed)func).name))
			return false;
		int num = ((Func)funkcja).args.length;
		Function[] matchers = ((Func)this.funkcja).args;
		Function[] args = ((Func)func).args;
		int i = 0;
		while(i<num && i >= 0) {
			System.out.println("w chackstagefunc.match " + i + "  " +matchers[i].write(new Settings()) + "  " +  args[i].write(new Settings()));
			if(MatcherMethods.match(matchers[i], args[i], mr)) {
				i++;
			}else {
				i--;
			}
		}
		return i < 0 ? false : true;
	}

	@Override
	protected void resetPomiedzy() {
		super.resetPomiedzy();
		// TODO Auto-generated method stub		
	}
	
}