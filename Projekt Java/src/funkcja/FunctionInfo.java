package funkcja;

import java.util.LinkedList;
import java.util.List;

class FunctionInfo {
	//trzy dolne pola uwzględniają tylko matchery, które jeszcze się do niczego nie dopasowały
	final List<String> containedAnyMatchers;
	final List<List<String>> matchersContainedInSM;
	final String anyMatcherIndex; //którym matcherem jest, jeśli nie jest żadnym(pustym) to anyMatcherIndex == ""
	
	public FunctionInfo(String name) {
		this.anyMatcherIndex = name;
		this.containedAnyMatchers = new LinkedList<String>();
		this.matchersContainedInSM = new LinkedList<List<String>>();
	}
	public FunctionInfo(FunctionInfo[] info, boolean fromSM) {
		//fromSM == true : jeśli info pochodzi od iloczynu lub sumy
		//fromSM == false : jeśli info pochodzi od funkcji ( jej argumentów )
		containedAnyMatchers = new LinkedList<String>();
		matchersContainedInSM = new LinkedList<List<String>>();
		anyMatcherIndex = "";
		LinkedList<String> directlyContainedAnyMatchers = new LinkedList<String>();
		for(FunctionInfo i : info) {
			for(String j : i.containedAnyMatchers) {
				if(!containedAnyMatchers.contains(j))
					containedAnyMatchers.add(j);
			}
			/*--------*/
			matchersContainedInSM.addAll(i.matchersContainedInSM);
			/*-------*/
			if(!i.anyMatcherIndex.equals("") && fromSM)
				directlyContainedAnyMatchers.add(i.anyMatcherIndex);
		}
		if(!directlyContainedAnyMatchers.equals(new LinkedList<Integer>()))
			matchersContainedInSM.add(directlyContainedAnyMatchers);
	}
}
