/*
 * zawiera praktyczne metody itp.
 */

package funkcja;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

class FuncMethods {

	static final FuncChecker isNumConst = new FuncChecker() {
		@Override
		public boolean check(Function func) {
			return func.type == Functions.NUMCONST;
		}
	};
	
	static final FuncChecker isInt = new  FuncChecker() {
		@Override
		public boolean check(Function func) {
			if(func.type == Functions.NUMCONST) {
				Complex a = ((FuncNumConst)func).a;
				if(a.y == 0 && a.x % 1 == 0)
					return true;
			}
			return false;
		}
	};
	
	static final FuncChecker minusOneTimes = new FuncChecker() {

		@Override
		public boolean check(Function func) {
			if(func.type == Functions.MULT && ((FuncMult)func).f[0].type == Functions.NUMCONST) {
				FuncNumConst c = (FuncNumConst)((FuncMult)func).f[0];
				if(findElement(((FuncMult)func).f, isNumConst, new ArrayList<Integer>(List.of(0))).bool)
					return false;
				if(c.form == FuncNumConst.UJEMNYPIERWSZY)
					return true;
			}
			return false;
		} 
		
	};
	
	protected static int countArguments(Function[] f) {
		int max=0;
		for(int i=0;i<f.length; i++) {
			if(f[i].nofArg>max)
				max = f[i].nofArg;
		}
		return max;
	}

	protected static int countArguments(LinkedList<Function> f) {
		int max=0;
		for(int i=0;i<f.size(); i++) {
			if(f.get(i).nofArg>max)
				max = f.get(i).nofArg;
		}
		return max;
	}
	
	protected static Function[] simplifyAll(Function[] g, Settings settings) throws WewnetzrnaFunkcjaZleZapisana {
		Function[] g2 = new Function[g.length];
		for(int i = 0; i<g.length;i++) {
			g2[i] = g[i].simplify(settings);
		}
		return g2;
	}

	protected static LinkedList<Function> simplifyAll(List<Function> g, Settings settings) throws WewnetzrnaFunkcjaZleZapisana {
		LinkedList<Function> g2 = new LinkedList<Function>();//[g.size()];
		for(int i = 0; i<g.size();i++) {
			g2.add(g.get(i).simplify(settings));
		}
		return g2;
	}
	
	protected static boolean equals(Function[] f, Function g[]) {//zwraca prawdę, nawet jeśli drugie jest permutacją pierwszego
		if(f.length != g.length)
			return false;
		ArrayList<Integer> usedIndekses = new ArrayList<Integer>();
		outer:
		for(int i=0;i<f.length;i++) {
			for(int j=0;j<g.length;j++) {
				if(usedIndekses.contains(j))
					continue;
				if(f[i].check(g[j])) {
					usedIndekses.add(j);
					continue outer;
				}
			return false;
			}
		}
		return true;
	}

	protected static boolean equals(ArrayList<Function> f, ArrayList<Function> g) {//zwraca prawdę, nawet jeśli drugie jest permutacją pierwszego
		if(f.size() != g.size())
			return false;
		ArrayList<Integer> usedIndekses = new ArrayList<Integer>();
		outer:
		for(int i=0;i<f.size();i++) {
			inner:
			for(int j=0;j<g.size();j++) {
				if(usedIndekses.contains(j))
					continue inner;
				if(f.get(i).check(g.get(j))) {
					usedIndekses.add(j);
					continue outer;
				}
			}
			return false;
		}
		return true;
	}

	protected static Function[] expand(Function[] f) {
		Function[] g = new Function[f.length];
		for(int i=0; i< g.length; i++) {
			g[i] = f[i].expand();
		}
		return g;
	}

	protected static Complex[] evaluate(Function[] functions, Complex[] args) {
		Complex[] ret = new Complex[functions.length];
		for(int i=0; i<functions.length;i++) {
			ret[i] = functions[i].evaluate(args);
		}
		return ret;
	}

	protected static Function[] re(Function[] functions) throws WewnetzrnaFunkcjaZleZapisana {
		Function[] ret = new Function[functions.length];
		for(int i=0;i<functions.length;i++) {
			ret[i] = functions[i].re();
		}
		return ret;
	}

	protected static Function[] im(Function[] functions) throws WewnetzrnaFunkcjaZleZapisana {
		Function[] ret = new Function[functions.length];
		for(int i=0;i<functions.length;i++) {
			ret[i] = functions[i].im();
		}
		return ret;
	}

	protected static Function[] putArguments(Function[] functions, Function[] args) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		Function[] ret = new Function[functions.length];
		for(int i=0; i<functions.length;i++) {
			ret[i] = functions[i].putArguments(args);
		}
		return ret;
	}

	protected static Function[] reim(Function[] f) throws WewnetzrnaFunkcjaZleZapisana {
		return new Function[] {f[0].re(), f[0].im()};
	}
	
	protected static Function[] subList(Function[] functions, int a, int b) {
		//od a do b-1 włącznie
		if(a<0 || b>functions.length || a>=b)
			throw new IllegalArgumentException("Podane krańce poza granicami listy lub pierwszy kraniec większy od drugiego. Podane krańce: " + a+", "+b+", lista: " + FuncMethods.write(functions));
		Function[] ret = new Function[b-a];
		for(int i=a;i<b;i++)
			ret[i-a] = functions[i];
		return ret;
	}

	protected static Bool<Integer> findElement(Function[] bigger, FuncChecker contained){
		for(int i=0;i<bigger.length;i++) {
			if(contained.check(bigger[i]))
				return new Bool<Integer>(i, true);	
		}
		return new Bool<Integer>(null, false);
	}

	protected static Bool<Integer> findElement(Function[] bigger, FuncChecker contained, ArrayList<Integer> zabronioneIndeksy){
		for(int i=0;i<bigger.length;i++) {
			if(contained.check(bigger[i]) && !zabronioneIndeksy.contains(i))
				return new Bool<Integer>(i, true);
		}
		return new Bool<Integer>(null, false);
	}

	protected static Bool<ArrayList<Integer>> findSubList(Function[] bigger, FuncChecker[] contained) {
		ArrayList<Integer> zuzyteIndeksy = new ArrayList<Integer>();
		boolean foundEverything = true;
		for(int i=0;i<contained.length;i++) {
			Bool<Integer> index = findElement(bigger, contained[i], zuzyteIndeksy);
			if(index.bool) 
				zuzyteIndeksy.add(index.f);
			else
				foundEverything = false;
		}
		return new Bool<ArrayList<Integer>>(zuzyteIndeksy, foundEverything);
	}

	protected static Bool<ArrayList<Integer>> findSubList(Function[] bigger, FuncChecker[] contained, ArrayList<Integer> zabronioneIndeksy) {
		ArrayList<Integer> zuzyteIndeksy = zabronioneIndeksy;
		boolean foundEverything = true;
		for(int i=0;i<contained.length;i++) {
			Bool<Integer> index = findElement(bigger, contained[i], zuzyteIndeksy);
			if(index.bool)
				zuzyteIndeksy.add(index.f);
			else
				foundEverything = false;
		}
		return new Bool<ArrayList<Integer>>(zuzyteIndeksy, foundEverything);
	}

	protected static String write(Function[] g) {
		String str = "";
		str+="[";
		for(int i=0;i<g.length;i++) {
			str += g[i].write(new Settings()) + ",  ";
		}
		str +="]";
		return str;
	}

	protected static boolean argsAreIdentities(Function[] g, int k) {
		//k <= g.lenght
		for(int i=0;i<k;i++) {
			if(!(g[i].check(Functions.returnVar("z["+i+"]"))))
				return false;
		}
		return true;
	}
	
}

/*
 * FuncChecker-y sprawdzają czy funkcja spełnia jakieś wymagania
 */
interface FuncChecker{
	boolean check(Function func);
}

/*
 * SimplifyTwo łączy ze sobą elemnty w celu upraszczania (w funcsum i funcmult)
 */
interface SimplifyTwo{
	boolean canPutTogether(Function func1, Function f2);
	Function putTogether(Function func1, Function func2);
	
	default ArrayList<Function> puAllTogether(ArrayList<Function> arr, ArrayList<Integer> zabronioneIndeksy){
		ArrayList<Integer> uzyteIndeksy = zabronioneIndeksy; 
		if(arr.size() == 0)
			throw new IllegalArgumentException("arr musi mieć w sobie co najmniej jeden element.");
		ArrayList<Function> ret = new ArrayList<Function>();
		int countIndex = 0;
		for(int i=0;i<arr.size();i++) {
			if(uzyteIndeksy.contains(i))
				continue;
			ret.add(arr.get(i));
			for(int j=i+1;j<arr.size();j++) {
				if(uzyteIndeksy.contains(j))
					continue;
				if(canPutTogether(arr.get(j), arr.get(i))) {
					if(ret.get(countIndex)  != arr.get(i)) {
						System.out.println(arr.get(i).write(new Settings()) + "\n" + ret.get(0).write(new Settings()));
						System.out.println(arr.get(i+1).write(new Settings()));
						System.out.println("w FuncMethods: SimplTwo.putAllTogether jest problem");
						System.out.println("" + countIndex + "   " + (ret.size()-1) + "   " + i + "   " + j);
						System.out.println(canPutTogether(arr.get(j), arr.get(i)));
						System.out.println(canPutTogether(arr.get(j), ret.get(countIndex)));
						System.out.println(canPutTogether(arr.get(countIndex), arr.get(i)));
					}
					ret.set(countIndex, putTogether(arr.get(j), ret.get(countIndex)));
					uzyteIndeksy.add(j);
				}
			}
			countIndex++;
		}
		return ret;
	}

	@Deprecated
	default ArrayList<Function> putAlltogether(ArrayList<Function> arr){
		ArrayList<Integer> uzyteIndeksy = new ArrayList<Integer>(); 
		if(arr.size() == 0)
			throw new IllegalArgumentException("arr musi mieć w sobie co najmniej jeden element.");
		ArrayList<Function> ret = new ArrayList<Function>();
		int countIndex = 0;
		for(int i=0;i<arr.size();i++) {
			if(uzyteIndeksy.contains(i))
				continue;
			ret.add(arr.get(i));
			for(int j=i+1;j<arr.size();j++) {
				if(uzyteIndeksy.contains(j))
					continue;
				if(canPutTogether(arr.get(j), arr.get(i))) {
						ret.set(countIndex, putTogether(arr.get(j), ret.get(countIndex)));
						uzyteIndeksy.add(j);
				}
			}
			countIndex++;
		}
		return ret;
	}
	
	default LinkedList<Function> putAlltogether(LinkedList<Function> arr){
		LinkedList<Integer> uzyteIndeksy = new LinkedList<Integer>(); 
		if(arr.size() == 0)
			throw new IllegalArgumentException("arr musi mieć w sobie co najmniej jeden element.");
		LinkedList<Function> ret = new LinkedList<Function>();
		int countIndex = 0;
		for(int i=0;i<arr.size();i++) {
			if(uzyteIndeksy.contains(i))
				continue;
			ret.add(arr.get(i));
			for(int j=i+1;j<arr.size();j++) {
				if(uzyteIndeksy.contains(j))
					continue;
				if(canPutTogether(arr.get(j), arr.get(i))) {
						ret.set(countIndex, putTogether(arr.get(j), ret.get(countIndex)));
						uzyteIndeksy.add(j);
				}
			}
			countIndex++;
		}
		return ret;
	}
}