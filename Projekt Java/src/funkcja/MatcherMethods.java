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
	
	static private Integer findNextSpot(Integer thisSubSet, List<Integer> set, List<Integer> coveredPortionOfSet, Integer startIndex) {
		for(int i = startIndex;i<set.size();i++) {
			if(set.get(i) - coveredPortionOfSet.get(i) >= thisSubSet) {
				return i;
			}
		}
		return -1;
	}
	
	static private LinkedList<Integer> findNextSubSet(Integer thisSubSet ,List<Integer> set, List<Integer> coveredPortionOfSet, LinkedList<Integer> thisSubSetCurrent) {
		/*
		 * dotyczy znalezienia następnego możliwego podzbioru jakim subSet może być
		 * 
		 * 'thisSubset' to rodzaj podzbioru jaki chcemy znalezć
		 */
		Integer index;
		if(thisSubSetCurrent == null || thisSubSetCurrent.equals(new LinkedList<Integer>()))
			return null;
		index = findNextSpot(thisSubSet, set, coveredPortionOfSet, thisSubSetCurrent.getLast()+1);
		if(index != -1) {
			coveredPortionOfSet.set(thisSubSetCurrent.getLast(), coveredPortionOfSet.get(thisSubSetCurrent.getLast()) - thisSubSet);
			thisSubSetCurrent.set(thisSubSetCurrent.size()-1, index);
			coveredPortionOfSet.set(thisSubSetCurrent.getLast(), coveredPortionOfSet.get(thisSubSetCurrent.getLast()) + thisSubSet);
			return thisSubSetCurrent;
		}
		
		if(thisSubSetCurrent.size() == 1) {
			coveredPortionOfSet.set(thisSubSetCurrent.getLast(), coveredPortionOfSet.get(thisSubSetCurrent.getLast()) - thisSubSet);
			thisSubSetCurrent.removeLast();
			index = findNextSpot(thisSubSet, set, coveredPortionOfSet, 0);
			thisSubSetCurrent.add(index);
			coveredPortionOfSet.set(thisSubSetCurrent.getLast(), coveredPortionOfSet.get(thisSubSetCurrent.getLast()) + thisSubSet);
			index = findNextSpot(thisSubSet, set, coveredPortionOfSet, thisSubSetCurrent.getLast());
			if(index == -1)
				return new LinkedList<Integer>();
			thisSubSetCurrent.add(index);
			coveredPortionOfSet.set(thisSubSetCurrent.getLast(), coveredPortionOfSet.get(thisSubSetCurrent.getLast()) + thisSubSet);
			return thisSubSetCurrent;
		}
		int currLast = thisSubSetCurrent.getLast();
		thisSubSetCurrent.removeLast();
		LinkedList<Integer> retP = findNextSubSet(thisSubSet, set, coveredPortionOfSet, thisSubSetCurrent);
		coveredPortionOfSet.set(currLast, coveredPortionOfSet.get(currLast) - thisSubSet);
		if(!retP.equals(new LinkedList<Integer>())) {
			index = findNextSpot(thisSubSet, set, coveredPortionOfSet, retP.getLast());
			if(index != -1) {//to powinno być pewne?
				retP.add(index);
				coveredPortionOfSet.set(retP.getLast(), coveredPortionOfSet.get(retP.getLast()) + thisSubSet);
				return retP;
			}
		}
		return new LinkedList<Integer>();
	}
	
	
	static private boolean subsetsWtExclusionsCoverPom(List<Integer> subSets, List<Integer> set, RememberStageMS stage, List<Integer> coveredPortionOfSet, int[] lastToConsider, Integer lenOfSet){
		/*
		 * subsets - informacja na jakie podzbiory podzielić, zawiera informację czy któreś podzbiory muszą byc te same
		 * pierwszy indeks - ilość podzbiorów, które są niezależne od wszystkich innych
		 * drugi indeks - 1/2 iości podzbiorów, dla których istnieje drugi podzbiór, który musi wyglądać tak samo
		 * treci indeks - ...
		 * 
		 * set - informacja o zbiorze
		 * każdy element podaje ilość elementów które są te same. Czyli suma elementów tego List<nteger> to długość zbioru
		 * 
		 * lastToConsider - [int, int] : opisuje dla których z 'subSets' nie znaleziono jeszcze odpowiadającego im podzbioru 'set'
		 * pierwszy int - indeks tego subset(ilość tych samych elementów które trzeba do niego dopasować )
		 * drugi int - jączna ilość podzbiorów przed nim ( nie licząc dwoch podzbiorów które muszą być te same osobno )
		 * 
		 * lenOfSet - ilość elementów set, keśli
		 * 
		 * za każdym razem zwraca następny możliwy podział na podzbiory
		 * zwrócona wartość null oznacza że wyczerpano podzbiory
		 * 
		 * zwraca wartość przez stage.intoSubSets 
  		 */
		if(coveredPortionOfSet == null) {
			coveredPortionOfSet = new LinkedList<Integer>();
			for(int i = 0 ;i<set.size();i++) {
				coveredPortionOfSet.add(0);
			}
		}
		if(lenOfSet == null) {
			lenOfSet = 0;
			for(Integer sameSub : set) {
			lenOfSet += sameSub;
			}
		}
		if(lastToConsider == null) {
			int lastNum = 0;
			for(Integer i : subSets) {
				lastNum += i;
			}
			lastToConsider = new int[] {subSets.size(), lastNum};
		}
		if(stage.intoSubSets == null) {
			for(Integer i : subSets) {
				for(int j = 0;j<i;j++) {
					stage.intoSubSets.add(null);
				}
			}
		}
		
		if(stage.intoSubSets.get(lastToConsider[1]) == null) {
			int indeks = -1;
			for(int i = 0;i<set.size();i++) {
				if(set.get(i) - coveredPortionOfSet.get(i) >= lastToConsider[0]) {
					indeks = i;
					break;
				}
			}
			if(indeks == -1) {
				return false;
			}
			coveredPortionOfSet.set(indeks,  coveredPortionOfSet.get(indeks) + lastToConsider[0]);
			stage.intoSubSets.set(lastToConsider[1], new LinkedList<Integer>(List.of(indeks)));
			lastToConsider[1]--;
			if(lastToConsider[1] == 0) {
				return true;
			}
			while(subSets.get( lastToConsider[0] - 1) == 0) {
				lastToConsider[0]--;
			}
			boolean didSth = subsetsWtExclusionsCoverPom(subSets, set, stage, coveredPortionOfSet, lastToConsider, lenOfSet-1);
			
			if(didSth) {
				return true;
			}
			coveredPortionOfSet.set(indeks,  0);
			stage.intoSubSets.set(lastToConsider[1]+1, new LinkedList<Integer>(List.of(indeks)));
			return false;
		}
		
		int[] lastToConsiderCopy = lastToConsider.clone(); 
		lastToConsiderCopy[1]--;
		while(subSets.get( lastToConsiderCopy[0] - 1) == 0) {
			lastToConsiderCopy[0]--;
		}
		throw new IllegalStateException();
		/*LinkedList<LinkedList<Integer>>retP = subsetsWtExclusionsCoverPom(subSets, set, stage, coveredPortionOfSet, lastToConsiderCopy, lenOfSet-1);
		if(retP != null) { 
			LinkedList<LinkedList<Integer>> ret = new LinkedList<LinkedList<Integer>>();
		}
		stage.intoSubSets.get(lastToConsider[1])*/
	}
	
	protected static boolean match(Function[] args, Function[] matchers, RememberStageMS stage, MatcherReturn mr, int startMarker) {
		//dopasowuje matchers do args. kontynuuje od momentu stage i znajduje następne najbliższe dopasowanie
		//jeśli już niema więcej możliwych dopasowań zwraca false
		if(stage.returnsFalse)
			return false;
		final FunctionInfo[] matcherInfo = FuncMethods.info(matchers);
		final LinkedList<Integer>argsMatched = stage.argsMatched;//indeksy elemtów args
		final LinkedList<Integer>staticMatchers = stage.staticMatchers;//matchery które się nie zmieniają bo wywołaniu .match
		final LinkedList<Integer> matchers2 = stage.matchers2;//indeksy kolejnych matcherów, które będą robione. Nie zawiera bezpośrednio matcherów które się zmieniają. (np.'sin(Any)' może zarzeć a 'Any' już nie)
		final LinkedList<Integer> matcherToArgsMap = stage.matcherToArgMap;//informacja dotyczy tylko matcherów z matchers2
		final LinkedList<Integer> markersOfMatchers = stage.markersOfMatchersAtMatching;//zapamiętuje sytuację w momencie w którym dany matcher się zmatchował. Dotyczy tylko matcherów z matchers2
		if(stage.nextTimResetTo != -1)
			mr.resetTo(stage.nextTimResetTo);
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
				if(argsMatched.contains(i) || MatcherReturn.canBeMult.check(matchers[i]))
					continue; 
				//TODO:zmienić kolejność by usprawnić program
				matchers2.add(i);
			}
			for(int i = 0;i<matchers2.size();i++) {
				matcherToArgsMap.add(-1);
				markersOfMatchers.add(-1);
			}

		}
		if(staticMatchers.size() == args.length && staticMatchers.size() == matchers.length) {
			if(stage.goneThroughOnce) {
				stage.returnsFalse = true;
				return false;
			}
			else
				return true;
		}
		stage.goneThroughOnce = true;
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
					matcherToArgsMap.set(i, j);
					markersOfMatchers.set(i, mr.setMarker());
					continue outer;
				}
			}
			
			if(i == 0) {
				stage.returnsFalse = true;
				return false;
			}
			
			if(match(matchers[matchers2.get(i-1)], args[matcherToArgsMap.get(i-1)], mr)) {
				i = i-1;
				stage.i0 = i;
				stage.j0 = 0;
				continue outer;
			}else {
				argsMatched.removeLast();
				matcherToArgsMap.set(i-1, -1);
				i = i-2;	
				stage.i0 = i-1;
				stage.j0 = 0;
				continue outer;
			}
		}
		/*-----*/
		
		
		
		//przygotowania aby za następnym razem kiedy funkcja zostanie wywołana znalazło następne dopasowanie
		stage.i0 = matchers2.size()-1;
		stage.j0 = matcherToArgsMap.get(stage.i0);
		//warunek początkowy rekurencji: - jeśli nie ma szansy by ponownie mogło się zmatchiwać w inny sposób
		if(matcherInfo[ matchers2.get(stage.i0) ].matchersContainedInSM.equals(new LinkedList<List<String>>())) {
			argsMatched.removeLast();
			matcherToArgsMap.set(matchers2.size()-1, -1);
			stage.j0++;
			while(stage.i0 >= 0) {
				stage.j0 = nextAvailable(stage.j0, argsMatched);
				if(stage.j0 == args.length && stage.i0 == 0) {
					stage.i0--;
					break;
				}
				if(stage.j0 == args.length && stage.i0 > 0) {
					stage.i0--;
					stage.j0 = matcherToArgsMap.get(stage.i0);
					argsMatched.removeLast();
					matcherToArgsMap.set(stage.i0, -1);
					if(matcherInfo[ matchers2.get(stage.i0) ].matchersContainedInSM.equals(new LinkedList<List<String>>())) {
						stage.nextTimResetTo = markersOfMatchers.get(stage.i0)-1;
						stage.j0++;
					}
				}
				else
					break;
			}
		}
		if(stage.i0 < 0)
			stage.returnsFalse = true;
		return true;
	}

	static public void main(String[] args) {
		LinkedList<Integer> set = new LinkedList<Integer>(List.of(new Integer[] {1,3,1,1,11,3,4,1,6,3,1,6}));
		Integer thisSubsetType = 3;
		LinkedList<Integer> cov = new LinkedList<Integer>();
		for(int i=0;i<set.size();i++) {
			cov.add(0);
		}
		LinkedList<Integer> curr = new LinkedList<Integer>(List.of(findNextSpot(thisSubsetType, set, cov, 0)));
		cov.set(curr.get(0), thisSubsetType);
		for(int i = 0;i<Math.pow(2,4)*Math.pow(3, 2) * Math.pow(4, 1);i++) {
			if(curr==null) {
				System.out.println(Math.pow(2,4)*Math.pow(3, 2) * Math.pow(4, 1));
				break;
			}
			if(curr != null && curr.equals(new LinkedList<Integer>(List.of(new Integer[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 8, 9, 9, 10, 10, 11, 11, 11})))) {
				System.out.println(i);
			}
			if(true || i<100 || i % (Math.pow(2,5)*Math.pow(3,2)*Math.pow(4, 2)) == -1 || Math.abs(3000 - i) < 100) {
				System.out.println(curr + ", " + i);
				curr = findNextSubSet(thisSubsetType, set, cov, curr);
			}
		}
		System.out.println(curr +" kon");
	}

}