package funkcja;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.*;

class MatcherReturn extends VarReturnSpecial{
	//zarządza metcherami
	final private LinkedList<String> matcherNames = new LinkedList<String>();
	final private LinkedList<Matcher> matcherList = new LinkedList<Matcher>();
	private LinkedList<LinkedList<Matcher>> situationAtMarker = new LinkedList<LinkedList<Matcher>>();
	
	static FuncChecker canBeMult = new FuncChecker() {
		//TODO: gdzie to się miało przydać??
		@Override
		public boolean check(Function func) {
			if(func.type == Functions.NAMED) {
				return AnyMatcher.checkIfAnyMatcher(((FuncNamed)func).name);
			}
			return false;
		}
	}; 
	
	protected static int returnNumberForAny(String str) {
		//zakłada że wiadomo już, że str jest odpowiedniego rodzaju
		return Integer.parseInt(str.substring(4, str.length()-1));
	}

	public void resetAgo(int i) {
		resetTo(situationAtMarker.size()-i);
	}

	List<String> getNotDone(Function function) {
		LinkedList<String> ret = new LinkedList<String>();
		LinkedList<String> inFunction = function.info().containedAnyMatchers;
		for(Matcher m : matcherList) {
			if(inFunction.contains(m.name) && m.currentMatch == null)
				ret.add(m.name);
		}
		return ret;
	}
	
	LinkedList<Matcher> getMatched(){
		LinkedList<Matcher> ret = new LinkedList<Matcher>();
		for(Matcher m : matcherList)
			if(m.currentMatch != null)
				ret.add(m);
		return ret;
	}

	@Override
	Function returnFunc(String name) {
		if(!matcherNames.contains(name)) {
			int k = returnNumberForAny(name);
			matcherNames.add(name);
			matcherList.add(new AnyMatcher(k));
		}
		return matcherList.get(matcherNames.indexOf(name));
	}

	Function returnFuncAny(int k) {
		if(!matcherNames.contains("Any["+k+"]")) {
			AnyMatcher matcher = new AnyMatcher(k);
			matcherNames.add(matcher.name);
			matcherList.add(matcher);
		}
		return matcherList.get(matcherNames.indexOf("Any["+k+"]"));
	}

	void reset(List<String> matchers) {
		for(Matcher i : this.matcherList)
			if(matchers.contains(i.name))
				i.currentMatch = null;
	}
	
	@Override
	boolean check(String str) {
		return AnyMatcher.checkIfAnyMatcher(str);
	}

	void resetTo(int marker) {
		//resetuje wszystkie matchery które nie miały wartości w momencie stawienia markera. niekoniecznie pamięta jakie wtedy matchery miały wartości
		for(Matcher i : matcherList) {
			if(!situationAtMarker.get(marker).contains(i))
				i.currentMatch = null;
		}
	}
	
	int setMarker() {
		//zakłada że po użyciu resetTo(marker) już się nie użyje resetTo(>marker) 
		situationAtMarker.add(getMatched());
		return situationAtMarker.size()-1;
	}

}