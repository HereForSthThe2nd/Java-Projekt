package funkcja;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

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
	
	static private int nextAvailable(int j, List<Integer> argsMatched) {
		while(argsMatched.contains(j))
			j++;
		return j;
	}
	
	protected static boolean match(Function[] args, Function[] matchers, RememberStageMS stage, MatcherReturn mr, int startMarker) {
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
		final LinkedList<Integer> markersOfMatchers = stage.markersOfMatchersAtMatching;
		if(stage.nextTimResetTo != -1)
			mr.resetTo(stage.nextTimResetTo);
		if(!stage.goneThroughOnce) {
			for(int i = 0;i<matchers.length;i++) {
				matcherToArgsMap.add(-1);
				markersOfMatchers.add(-1);
			}
			
			/*---sprawdza elementy które nie zawierają w sobie matcherów które mogą się zmieniać----*/
			for(int j=0;j<matchers.length;j++) {
				if(!matcherInfo[j].containedAnyMatchers.equals(new LinkedList<String>()))
					continue;
				staticMatchers.add(j);
				for(int i = 0;i<args.length;i++) {
					if(!argsMatched.contains(i) && matchers[j].check(args[i])) {
						matcherToArgsMap.set(j, i);
						argsMatched.add(i);
						break;
					}
				}
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
		int j = 0;
		outer:
		for(int i = stage.i0;i<matchers2.size();i++) {
			for(j = 0;j<args.length;j++) {
				if(i == stage.i0 && j < stage.j0) {
					j = stage.j0;
				}
				if(argsMatched.contains(j))
					continue;
				if(match(matchers[matchers2.get(i)], args[j], mr)) {
					argsMatched.add(j);
					matcherToArgsMap.set(matchers2.get(i), j);
					markersOfMatchers.set(matchers2.get(i), mr.setMarker());
					continue outer;
				}
			}
			
			if(i == 0) {
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
		
		stage.i0 = matchers2.size()-1;
		stage.j0 = matcherToArgsMap.get(matchers2.get(stage.i0));
		//warunek początkowy rekurencji: - nie ma szansy by ponownie mogło się zmatchiwać w inny sposób
		if(matcherInfo[ matchers2.get(stage.i0) ].matchersContainedInSM.equals(new LinkedList<List<String>>())) {
			argsMatched.removeLast();
			matcherToArgsMap.set(matchers2.getLast(), -1);
			stage.j0++;
			while(stage.i0 >= 0) {
				stage.j0 = nextAvailable(stage.j0, argsMatched);
				if(stage.j0 == args.length && stage.i0 == 0) {
					stage.i0--;
					break;
				}
				if(stage.j0 == args.length && stage.i0 > 0) {
					stage.i0--;
					stage.j0 = matcherToArgsMap.get(matchers2.get(stage.i0));
					argsMatched.removeLast();
					matcherToArgsMap.set(matchers2.get(stage.i0), -1);
					if(matcherInfo[ matchers2.get(stage.i0) ].matchersContainedInSM.equals(new LinkedList<List<String>>())) {
						stage.nextTimResetTo = markersOfMatchers.get(matchers2.get(stage.i0))-1;
						stage.j0++;
					}
				}
				else
					break;
			}
		}
		if(stage.i0 < 0)
			stage.returnsFalse = true;
		stage.goneThroughOnce = true;
		return true;
	}
}