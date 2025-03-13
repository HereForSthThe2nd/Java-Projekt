package poczatek;

import java.util.ArrayList;
import java.util.List;


class Blok{
	final static int 
		WORD = 1,
		FUNCTION = 2,
		NUMBER = 3,
		PARENTHASES = 4,
		OPERATION = 5;
	final int type;
	int pocz;
	int kon;
	String str;
	
	Blok(String wejsc, int pocz, int kon, int type){

		this.pocz = pocz;
		this.kon = kon;
		this.str = wejsc.substring(pocz, kon);
		this.type = type;
	}
	Blok(String wejsc, int[] konce, int type){

		this.pocz = konce[0];
		this.kon = konce[1];
		this.str = wejsc.substring(konce[0], konce[1]);
		this.type = type;
	}
	public void print() {
		System.out.println(str);
	}
}

class BlokWthFunction extends Blok{
	Func funkcja;
	BlokWthFunction(String wejsc, int[] konce, int type, Func funkcja) {
		super(wejsc, konce, type);
		//System.out.println(type);
		this.funkcja = funkcja;
		if(type != Blok.FUNCTION)
			throw new IllegalArgumentException("BlokWthFunction musi być typu Blok.FUNCTION");
	}
	BlokWthFunction(String wejsc, int pocz, int kon, int type, Func funkcja){
		super(wejsc, pocz,kon,type);
		this.pocz -=  wejsc.length();
		this.funkcja = funkcja;
		if(type != Blok.FUNCTION)
			throw new IllegalArgumentException("BlokWthFunction musi być typu Blok.FUNCTION");
	}
	
	@Override
	public void print() {
		System.out.println(funkcja.write() + str);
	}
}

class BlokList{
	protected final static String SPECJALNE2 = "[\\(\\)\\[\\]\\{\\}\\.,]";
	protected final static String SPECJALNE = "[\\^\\*/\\+\\-]";
	
	ArrayList<Blok> arr = new ArrayList<Blok>();
	
	BlokList(){};
	
	BlokList(String str) throws WrongSyntaxException{
		int index = 0;
		while(index<str.length()) {
			Blok blok = znajdzBlok(str, index);
			arr.add(blok);
			index = blok.kon;
		}
		if(arr.get(0).type == Blok.OPERATION || arr.get(arr.size()-1).type == Blok.OPERATION) {
			throw new WrongSyntaxException("operacja gdzieś żle zapisana");
		}
	}
	
	public int find(String str, int side) {
		if(side == 1) {
			for(int i=0;i<arr.size();i++) {
				if(arr.get(i).str.equals(str))
					return i;
			}
		}
		if(side == -1) {
			for(int i=str.length()-1;i>-1;i--) {
				if(arr.get(i).str.equals(str))
					return i;
			}
		}
		return -1;
	}
	
	public void print() {
		for(int i=0; i<arr.size(); i++) {
			arr.get(i).print();
		}
	}
	
	public BlokList subList(int begin, int end) {
		//kreuje podlistę od indeksu begin do end-1 włącznie
		if(begin >= arr.size() || end <= 0 || end <= begin) {
			//System.out.println("ostrzezenie");
			return new BlokList();
		}
		if(begin < 0 || end > arr.size())
			throw new IllegalArgumentException("Podane indeksy są złe.");
		BlokList sub = new BlokList();
		for(int i=begin;i<end;i++) 
			sub.arr.add(arr.get(i));
		return sub;
	}
	/*
	protected static boolean contains(String str, char chr) {
		return str.indexOf(chr)==-1 ? false : true;
	}
	*/
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
		return (""+chr).matches(".*[a-zA-Z].*");
	}		
	
	protected static boolean isNum(char chr) {
		return (""+chr).matches(".*[1-9].*");
	}
	
	protected static boolean isSpecial(char chr) {
		return (""+chr).matches(".*"+SPECJALNE+".*");
	}		
	
	protected static String configureStr(String str) {
		while(str.charAt(0) == '(' && str.charAt(str.length()-1) == ')') {
			str = str.substring(1, str.length()-1);
		}
		return str;
	}
	
	static class OznaczonaListaStr{
		final ArrayList<String> arr;
		final ArrayList<Integer> znaki;
		OznaczonaListaStr(ArrayList<String> arr, ArrayList<Integer> znaki){
			this.arr = arr;
			this.znaki = znaki;
		}
	}
	
	@Deprecated
	protected static OznaczonaListaStr splitWthPM(String str){
		//dzieli str wg plusów i minusów i zapamiętuje znak
		//zakłada że nie występują dwa takie znaki z rzędu, oraz że nie ma takiego znaku na początku czy końcu str
		//ignoruje nawiasy ( zapewne do usunięcia )
		ArrayList<String> arr = new ArrayList<String>();
		ArrayList<Integer> znaki = new ArrayList<Integer>();
		char ostatniZnak = '+';
		int index1 = 0;
		for(int index2 = 0; index2<str.length()-1; index2++) {
			if(str.charAt(index2)).match("[+-]")){
				arr.add(str.substring(index1, index2));
				znaki.add(ostatniZnak == '+' ? 1 : -1);
				ostatniZnak = str.charAt(index2);
				index2++;
				index1 = index2;
			}
		}
		arr.add(str.substring(index1, str.length()));
		znaki.add(ostatniZnak == '+' ? 1 : -1);
		return new OznaczonaListaStr(arr, znaki);
	}
	
 	private static int[] wNawiasachKwPom(String str, int index1, int index2) {
		//zakłada, że pomiędzy indeksami inex1 i index2 występuje odpowiednia ilość nawiasów
		//zwraca położenia najbardziej wewnętrznych możliwych możliwych
		//niedozwolone są znaki SPECJALNE ani nawiasy normalne
		int[] konce = {index1, index2};
		int napotkaneNawiasyL = 0;
		int napotkaneNawiasyP = 0;
		if(index1 == index2 && (""+str.charAt(index1)).matches("[\\[\\]]")) {
			switch(str.charAt(index2)) {
			case '[':
				napotkaneNawiasyL += 1;
				break;
			case ']':
				napotkaneNawiasyP -= 1;
				break;
			}
		}
		if(index1 != index2){
			if((""+str.charAt(index1)).matches("[\\[\\]]"))
				napotkaneNawiasyL += str.charAt(index1) == '[' ? 1:-1;
			if((""+str.charAt(index2)).matches("[\\[\\]]"))
				napotkaneNawiasyP += str.charAt(index2) == '[' ? 1:-1;
		}
		while(napotkaneNawiasyL != 1) {
			konce[0] -= 1;
			if(konce[0] == -1) break;
			if( (""+str.charAt(konce[0])).matches(SPECJALNE + "|[\\(\\)]") ) {
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
		while(napotkaneNawiasyP != -1) {
			konce[1] += 1;
			if(konce[1] == str.length()) {
				konce[1] = -1;
				break;
			}
			if((""+str.charAt(konce[1])).matches(SPECJALNE)) {
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
		while(konceNow[0] != -1 && konceNow[1] != -1) {
			konce = konceNow;
			if(konce[0]==0 || konce[1] == str.length()-1)
				break;
			konceNow = wNawiasachKwPom(str, konceNow[0]-1, konceNow[1]+1);
		}
		return konce;
	}

	protected static int[] wNawiasach(String str, int index) {
		//sprawdza czy pole o indeksie index jest zawarte pomiędzy dwoma nawiasami ()
		//jeśli tak zwraca ich położenia jeśli nie zwraca -1 dla odpowiednich stron. wynik {-1, (.)!=-1} lub odwrotnie oznacza błędną składnię
		//zwraca położenia najbardzei jwewnętrznych nawiasów możliwych
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
	
	private static boolean checkSquareBrackets(String str, int index, int[] konce) throws WrongSyntaxException {
		//tylko i wyłącznie dla metody blok
		int[] temp = wNawiasachKw(str, index);
		if(temp[0] * temp[1] < 0) {
			throw new WrongSyntaxException(
				String.format("Kwadratowe nawiasy się nie zamykają\nindex: %d.  końce:  %d, %d. cały str: %s.",index, konce[0], konce[1], str),
				"Kwadratowe nawiasy się nie zamykają.");
		}
		if(temp[0] > -1 && temp[1] > -1) {
			konce[0] = temp[0] = temp[0]-1;
			konce[1] = temp[1]+1;
			return true;
		}
		return false;
	}
	
	private static Blok znajdzBlokPom(String str, int index) throws WrongSyntaxException {
		//zwraca położenia końców bloku wokół index. Zwraca indeks lewej strony oraz indeks+1 prawej.
		//nie włącza funkcji i ich argumentów w jeden blok
		if(index < 0)
			throw new IllegalArgumentException("Niepoprawne argumenty");
		if(index >= str.length())
			throw new IllegalArgumentException("Niepoprawne argumenty");
		int[] konce = {index, index};
		int type;
		boolean isParenthases = (""+str.charAt(index)).matches("[\\(\\)]");
		boolean isWord = isLetter(str.charAt(index));
		boolean isNum = isNum(str.charAt(index));
		boolean isOperation = (str.charAt(index) + "").matches(SPECJALNE);
		boolean hasSquareBrackets = false;
		if(checkSquareBrackets(str, index, konce)) {
			isWord = true;
			isNum = false;
			isParenthases = false;
			isOperation = false;
			hasSquareBrackets = true;
		}
		if(isOperation) {
			type = Blok.OPERATION;
			return new Blok(str, index, index+1, type);
		}
		if(isWord) {
			type = Blok.WORD;
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
			return new Blok(str, konce[0], konce[1], type);
		}
		
		if(isNum){
			type = Blok.NUMBER;
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
			return new Blok(str, konce, type);
		}
		if(isParenthases) {
			type = Blok.PARENTHASES;
			int[] temp = wNawiasach(str, index);
			konce[0] = temp[0];
			konce[1] = temp[1]+1;
			return new Blok(str, konce, type);
		}
		else
			throw new IllegalArgumentException("Niepoprawne argumenty. Podanemu indeksowi nie można przypisać ani cyfry ani litery (ani [])\nindex:"+
						index+".  cos na indeksie:"+str.charAt(index) + ".  cały str: " + str+".");
	}

	private static Blok znajdzBlok(String str, int index) throws WrongSyntaxException {
		//zwraca położenia końców bloku wokół index. Zwraca indeks lewej strony oraz indeks+1 prawej.
		Blok blok = znajdzBlokPom(str, index);
		if(blok.type == Blok.WORD) {
			if(contains(Func.knownFunctions, blok.str)) {
				if(blok.kon == str.length()+1)
					throw new IllegalArgumentException("Zawarta jest funkcja bez argumentu." + "\n zawara funckja: " + blok.str);
				Blok prawaStrona = znajdzBlokPom(str, blok.kon);
				if(prawaStrona.type != Blok.PARENTHASES)
					throw new IllegalArgumentException("Zawarta jest funkcja bez argumentu." + "\n zawara funckja: " + blok.str);
				return new BlokWthFunction(str, prawaStrona.pocz, prawaStrona.kon, Blok.FUNCTION, Func.knownFunctionsVal[indexOf(Func.knownFunctions, blok.str )]);
			}
				
		}
		if(blok.type == Blok.PARENTHASES) {
			if(blok.pocz == 0)
				return blok;
			Blok lewaStrona = znajdzBlokPom(str, blok.pocz-1);
			if(lewaStrona.type == Blok.WORD && contains(Func.knownFunctions, lewaStrona.str))
				return new BlokWthFunction(str, blok.pocz, blok.kon, Blok.FUNCTION, Func.knownFunctionsVal[indexOf(Func.knownFunctions, lewaStrona.str )]);
		}
		return blok;

	}
	
	public static void main(String[] args) throws WrongSyntaxException {
		znajdzBlokPom("[]", 0).print();
	}
}
