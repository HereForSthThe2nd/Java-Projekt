package poczatek;

class StrOperations{
	protected final static String ALFABET = "abcdefghijklmnopqrstuvwxyz";
	protected final static String SPECJALNE = "^*/+-";
	protected final static String CYFRY = "0123456789.";
	
	protected static boolean contains(String str, char chr) {
		return str.indexOf(chr)==-1 ? false : true;
	}
	
	protected static int indexOf(String[] arr, String str) {
		for(int i=0;i<arr.length;i++) {
			if(arr[i].equals(str))
				return i;
		}
		return -1;
	}
	
	protected static boolean contains(String[] arr, String str) {
		return indexOf(arr, str) == -1 ? false : true;
	}		
	
	protected static boolean isLetter(char chr) {
		return contains(ALFABET, chr);
	}		
	
	protected static boolean isNum(char chr) {
		return contains(CYFRY, chr);
	}
	
	protected static boolean isSpecial(char chr) {
		return contains(SPECJALNE, chr);
	}		
	
	private static int[] wNawiasachKwPom(String str, int index1, int index2) {
		//zakłada, że pomiędzy indeksami inex1 i index2 występuje odpowiednia ilość nawiasów
		//zwraca położenia najbardziej wewnętrznych możliwych możliwych
		//niedozwolone są znaki SPECJALNE ani nawiasy normalne
		int[] konce = {index1, index2};
		boolean nieRobicL = false;
		boolean nieRobicP = false;
		int napotkaneNawiasyL = 0;
		int napotkaneNawiasyP = 0;
		if(str.charAt(index1) == '[') {
			napotkaneNawiasyL += 1;
			napotkaneNawiasyP += index1 == index2 ? 0 : 1;
		}
		if(str.charAt(index2) == ']') {
			napotkaneNawiasyP += -1;
			napotkaneNawiasyL += index1 == index2 ? 0 : -1;
		}
		while(napotkaneNawiasyL != 1 && !nieRobicL) {
			konce[0] -= 1;
			if(konce[0] == -1) break;
			if(StrOperations.contains(SPECJALNE + "()", str.charAt(konce[0]))) {
				konce[0] = -1;
				break;
			}
			switch(str.charAt(konce[0])) {
			case '[':
				napotkaneNawiasyL++;
				break;
			case ']':
				napotkaneNawiasyL--;
				break;
			}
		}
		while(napotkaneNawiasyP != -1 && !nieRobicP) {
			konce[1] += 1;
			if(konce[1] == str.length()) {
				konce[1] = -1;
				break;
			}
			if(StrOperations.contains(SPECJALNE, str.charAt(konce[1]))) {
				konce[1] = -1;
				break;
			}
			switch(str.charAt(konce[1])) {
			case '[':
				napotkaneNawiasyP++;
				break;
			case ']':
				napotkaneNawiasyP--;
				break;
			}
		}
		return konce;
	} 
	
	private static int[] wNawiasachKw(String str, int index) {
		//sprawdza czy pole o indeksie index jest zawarte pomiędzy dwoma kwadratowymi nawiasami [].
		//jeśli tak zwraca ich położenia jeśli nie zwraca -1 dla odpowiednich stron. wynik {-1, (.)!=-1} lub odwrotnie oznacza błędną składnię
		//zwraca położenia najbardziej zewnętrznych możliwych możliwych
		int[] konceNow = wNawiasachKwPom(str, index, index);
		int[] konce = wNawiasachKwPom(str, index, index);
		while(konceNow[0] != -1 && konceNow[1] != -1 && konceNow[0] > 0 && konceNow[1] < str.length()-1) {
			konce = konceNow;
			konceNow = wNawiasachKwPom(str, konceNow[0]-1, konceNow[1]+1);
		}
		return konce;
	}

	protected static int[] wNawiasach(String str, int index) {
		//sprawdza czy pole o indeksie index jest zawarte pomiędzy dwoma nawiasami ()
		//jeśli tak zwraca ich położenia jeśli nie zwraca -1 dla odpowiednich stron. wynik {-1, (.)!=-1} lub odwrotnie oznacza błędną składnię
		int[] konce = {index, index};
		boolean nieRobicL = false;
		boolean nieRobicP = false;
		if(str.charAt(index) == '(') nieRobicL = true;
		if(str.charAt(index) == ')') nieRobicP = true;
		int napotkaneNawiasy = 0;
		while(napotkaneNawiasy != 1 && !nieRobicL) {
			konce[0]--;
			if(konce[0] == -1) break;
			switch(str.charAt(konce[0])) {
			case '(':
				napotkaneNawiasy++;
				break;
			case ')':
				napotkaneNawiasy--;
				break;
			}
		}
		napotkaneNawiasy = 0;
		while(napotkaneNawiasy != -1 && !nieRobicP) {
			konce[1]++;
			if(konce[1] == str.length()) {
				konce[1] = -1;
				break;
			}
			switch(str.charAt(konce[1])) {
			case '(':
				napotkaneNawiasy++;
				break;
			case ')':
				napotkaneNawiasy--;
				break;
			}
		}
		return konce;
	} 
	
	private static boolean checkSquareBrackets(String str, int index, int[] konce) {
		//tylko i wyłącznie dla metody blok
		int[] temp = wNawiasachKw(str, index);
		if(temp[0] * temp[1] < 0) {
			throw new IllegalArgumentException("Kwadratowe nawiasy się nie zamykają");
		}
		if(temp[0] > -1 && temp[1] > -1) {
			konce[0] = temp[0] = temp[0]-1;
			konce[1] = temp[1]+1;
			return true;
		}
		return false;
	}
	protected static int[] blok(String str, int index) {
		//zwraca położenia końców bloku wokół index. Zwraca indeks lewej strony oraz indeks+1 prawej.
		if(index < 0 || index > str.length())
			throw new IllegalArgumentException("Niepoprawne argumenty");
		
		int[] konce = {index, index};
		boolean isWord = isLetter(str.charAt(index));
		boolean isNum = isNum(str.charAt(index));
		boolean hasSquareBrackets = false;
		if(checkSquareBrackets(str, index, konce)) {
			isWord = true;
			isNum = false;
			hasSquareBrackets = true;
		}
		
		if(isWord) {
			while(konce[1] < str.length() && !hasSquareBrackets && (isLetter(str.charAt(konce[1])) || str.charAt(konce[1]) == ']' || str.charAt(konce[1]) == '[')) {
				if(str.charAt(konce[1]) == ']' || str.charAt(konce[1]) == '['){
					checkSquareBrackets(str, konce[1], konce);
					hasSquareBrackets = true;
				}else 
					konce[1] += 1;
			}
			while(konce[0] > -1 && (isLetter(str.charAt(konce[0])) || str.charAt(konce[0]) == '[')) {
				if(str.charAt(konce[0]) == '[') {
					checkSquareBrackets(str, konce[0], konce);
					hasSquareBrackets = true;
				}else 
					konce[0] -= 1;
			}
			konce[0]++;
			return konce;
		}
		
		if(isNum){
			int countCommas = str.charAt(index) == '.' ? -1 : 0;
			while(isNum(str.charAt(konce[0]))) {
				countCommas += str.charAt(konce[0]) == '.' ? 1 : 0;
				konce[0] -= 1;
				if(konce[0] == -1)
					break;
			}
			konce[0]++;
			while(isNum(str.charAt(konce[1]))) {
				countCommas += str.charAt(konce[1]) == '.' ? 1 : 0;
				konce[1] += 1;
				if(konce[1] == str.length())
					break;
			}
			if(countCommas > 1) 
				throw new IllegalArgumentException("Niepoprawny zapisis (coś z przecinkami(kropkami))");
			return konce;
		}else
			throw new IllegalArgumentException("Niepoprawne argumenty. Podanemu indeksowi nie można przypisać ani cyfry ani litery (ani [])");
	}
	public static void main(String[] args) {
		//System.out.println("ssss".charAt(5));
		String str = "[[1]]ad33[[][[][]]]h[]";
		for(int i=0;i<str.length();i++) {
			System.out.println(str.substring(StrOperations.blok(str, i)[0], StrOperations.blok(str, i)[1]));
		}
		//readMult("5*5/x*x*z*z*z/y/y/x/pi");
		/*String str2 = "[frfr]";
		for(int i=0;i<str2.length();i++) {
			System.out.print(StrOperations.wNawiasachKw(str2, i)[0] + "  ");
			System.out.println(StrOperations.wNawiasachKw(str2, i)[1]);
		}*/

	}
}
