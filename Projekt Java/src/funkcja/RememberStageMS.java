package funkcja;

import java.util.LinkedList;

class RememberStageMS{
	/*
	 * pamięta etap w którym znajduje się matchowanie listy do listy
	 */
	LinkedList<Integer> matcherToArgMap;
	int i0;
	int j0;
	LinkedList<Integer> staticMatchers;
	LinkedList<Integer> matchers2;
	LinkedList<Integer> argsMatched;
	boolean goneThroughOnce;
	boolean returnsFalse;
	LinkedList<Integer> markersOfMatchersAtMatching;
	Integer nextTimResetTo;
	//ostatni etap : dopasowanie matcherów bezpośrednio typu Any[k], które jeszcze nie przyjęły rzadnej wartości
	boolean goneThorughLastStageOnce;
	LinkedList<LinkedList<Integer>> intoSubSets; //pamięta jakim podzbiorem jest każdy subset
	LinkedList<Integer> directMatchedMatchers;
	LinkedList<Integer> argsMatchedBefore;
	int lastStageMarker;
	LinkedList<Integer> lastStageSet;
	
	
	public RememberStageMS() {
		reset();
	}
	public void reset() {
		markersOfMatchersAtMatching = new LinkedList<Integer>();
		matcherToArgMap = new LinkedList<Integer>();
		matchers2 = new LinkedList<Integer>();
		argsMatched = new LinkedList<Integer>();
		staticMatchers = new LinkedList<Integer>();
		intoSubSets = null;
		directMatchedMatchers = new LinkedList<Integer>();
		argsMatchedBefore = new LinkedList<Integer>();
		lastStageSet = null;
		goneThroughOnce = false;
		returnsFalse = false;
		goneThorughLastStageOnce = false;
		i0 = 0;
		j0 = 0;
		nextTimResetTo = -1;
		lastStageMarker = -1;
	}
}