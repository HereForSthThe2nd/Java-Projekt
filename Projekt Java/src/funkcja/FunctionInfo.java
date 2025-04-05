package funkcja;

import java.util.LinkedList;
import java.util.List;

class FunctionInfo {
	final List<Integer> containedAnyMatchers;
	final List<List<Integer>> matchersContainedInSM;
	final int anyMatcherIndex; //którym Any[k] jest, jeśli nie jest żadnym to -1
	public FunctionInfo(int k) {
		this.anyMatcherIndex = k;
		this.containedAnyMatchers = new LinkedList<Integer>();
		this.matchersContainedInSM = new LinkedList<List<Integer>>();
	}
	public FunctionInfo(FunctionInfo[] info, boolean fromSM) {
		containedAnyMatchers = new LinkedList<Integer>();
		matchersContainedInSM = new LinkedList<List<Integer>>();
		//fromSM == true : jeśli info pochodzi od iloczynu lub sumy
		//fromSM == false : jeśli info pochodzi od funkcji ( jej argumentów )
		LinkedList<Integer> directlyContainedAnyMatchers = new LinkedList<Integer>();
		for(FunctionInfo i : info) {
			for(int j : i.containedAnyMatchers) {
				if(!containedAnyMatchers.contains(j))
					containedAnyMatchers.add(j);
			}
			/*--------*/
			matchersContainedInSM.addAll(i.matchersContainedInSM);
			/*-------*/
			if(i.anyMatcherIndex != -1 && fromSM)
				directlyContainedAnyMatchers.add(i.anyMatcherIndex);
		}
		if(!directlyContainedAnyMatchers.equals(new LinkedList<Integer>()))
			matchersContainedInSM.add(directlyContainedAnyMatchers);
		anyMatcherIndex = -1;
	}
}
