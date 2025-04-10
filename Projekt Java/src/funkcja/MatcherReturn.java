package funkcja;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.*;

class MatcherReturn extends VarReturnSpecial{
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
		LinkedList<AnyMatcher> ret = new LinkedList<AnyMatcher>();
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
		//resetuje wszystkie matchery które nie miały wartości w momencie stawienia markera. niekoniecznie pamięta jakie wtedy matchery miały wartości
		for(AnyMatcher i : matcherList) {
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