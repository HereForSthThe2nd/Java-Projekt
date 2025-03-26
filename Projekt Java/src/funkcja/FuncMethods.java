package funkcja;

import java.util.ArrayList;

class FuncMethods {

	FuncChecker isNumConst = new FuncChecker() {
		
		@Override
		public boolean check(Function func) {
			return func.type == Functions.NUMCONST;
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

	protected static Function[] simplifyAll(Function[] g, Settings settings) throws WewnetzrnaFunkcjaZleZapisana {
		Function[] g2 = new Function[g.length];
		for(int i = 0; i<g.length;i++) {
			g2[i] = g[i].simplify(settings);
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
			for(int j=0;j<g.size();j++) {
				if(usedIndekses.contains(j))
					continue;
				if(f.get(i).check(g.get(j))) {
					usedIndekses.add(j);
					continue outer;
				}
			return false;
			}
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

	protected static Function[] putArguments(Function[] functions, Function[] args) {
		Function[] ret = new Function[functions.length];
		for(int i=0; i<functions.length;i++) {
			ret[i] = functions[i].putArguments(args);
		}
		return ret;
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
