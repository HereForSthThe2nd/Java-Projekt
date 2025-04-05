package funkcja;

import java.util.LinkedList;

class MatcherMethods{
	
	final static class AnyMatcher extends FuncNamed{
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
			System.out.println("W aymatcher.replacematchers");
			if(currentMatch == null)
				throw new IllegalStateException("Coś poszło nie tak");
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
	}

	static class AnyMatcherReturn extends VarReturnSpecial{
		private LinkedList<Integer> doneIndekses = new LinkedList<Integer>();
		private LinkedList<AnyMatcher> matcherList = new LinkedList<AnyMatcher>();
				
		protected static int returnNumber(String str) {
			//zakłada że wiadomo już, że str jest odpowiedniego rodzaju
			return Integer.parseInt(str.substring(4, str.length()-1));
		}

		@Override
		Function returnFunc(String str) {
			if(str.equals("Any"))
				return returnFunc("Any[0]");
			int k = returnNumber(str);
			if(!doneIndekses.contains(k)) {
				doneIndekses.add(k);
				matcherList.add(new AnyMatcher(k));
			}
			return matcherList.get(doneIndekses.indexOf(k));
		}

		Function returnFunc(int k) {
			if(!doneIndekses.contains(k)) {
				doneIndekses.add(k);
				matcherList.add(new AnyMatcher(k));
			}
			return matcherList.get(doneIndekses.indexOf(k));
		}

		
		@Override
		boolean check(String str) {
			return str.matches("[Any]||(Any\\[[0-9]+\\])");
		}
	}

}