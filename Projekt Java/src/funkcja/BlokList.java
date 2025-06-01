/*plik dotyczy rozczytywania stringa i zamieniania go na funkcję
 * string dzielony jest na listę bloków (np. 12.3, pi, (...), exp(...))
 * */
package funkcja;

import java.util.ArrayList;
import java.util.LinkedList; 

class Blok{
	final static int 
		WORD = 1,
		FUNCTION = 2,
		NUMBER = 3,
		PARENTHASES = 4,
		OPERATION = 5,
		PRZECINEK = 6,
		NULL = 7;
	final int type;
	int pocz;
	int kon;
	String str;
	
	Blok(String calyStr, int pocz, int kon, int type){
		this.pocz = pocz;
		this.kon = kon;
		this.type = type;
		if(type == NULL)
			return;
		this.str = calyStr.substring(pocz, kon);
	}
	Blok(String calyStr, int[] konce, int type){

		this.pocz = konce[0];
		this.kon = konce[1];
		this.type = type;
		if(type==NULL)
			return;
		this.str = calyStr.substring(konce[0], konce[1]);

	}
	public void print() {
		System.out.println(str + " type: " + type);
	}
	public String write() {
		return str + " type: " + type;
	}
}

class BlokWthDefFunction extends Blok{
	protected FuncNamed funkcja;
	BlokWthDefFunction(String wejsc, int[] konce, int type, FuncNamed funkcja) {
		super(wejsc, konce, type);
		//System.out.println(type);
		this.funkcja = funkcja;
		if(type != Blok.FUNCTION)
			throw new IllegalArgumentException("BlokWthFunction musi być typu Blok.FUNCTION");
	}
	BlokWthDefFunction(String wejsc, int pocz, int kon, int type, FuncNamed funkcja){
		super(wejsc, pocz,kon,type);
		this.pocz -=  wejsc.length();
		this.funkcja = funkcja;
		if(type != Blok.FUNCTION)
			throw new IllegalArgumentException("BlokWthFunction musi być typu Blok.FUNCTION");
	}
	
	@Override
	public void print() {


		System.out.println(funkcja.name + str + " type: 2");
	}
}

class BlokList{
	protected final static String SPECJALNE = "[\\(\\)\\[\\]\\{\\}\\.,]";
	protected final static String OPERATORY = "[\\^\\*/\\+\\-]";
	protected final static String GRECKIALFABET = "ςερτυθιοπασδφγηξκλζχψωβνμ";
	
	ArrayList<Blok> arr = new ArrayList<Blok>();
	
	BlokList(){};
	
	BlokList(String str) throws WrongSyntaxException{
		if(str.equals(""))
			throw new WrongSyntaxException("Otwarte nawiasy muszą coś zawierać.");
		int index = 0;
		while(index<str.length()) {
			Blok blok = znajdzBlok(str, index);
			if(blok.pocz == -1 || blok.kon == -1) {
				throw new WrongSyntaxException("Nawiasy się nie domykają.", "pełny string: " + str + "chyba w normalnych okolicznościach nie powinno do tego błędu dochodzić, sprawdzić to");
			}
			arr.add(blok);
			index = blok.kon;
		}
		if((arr.get(0).type == Blok.OPERATION && arr.get(0).str.matches("[^+-]"))) {
			throw new WrongSyntaxException("Operacja " + arr.get(0).str + " nie może stać na początku wyrażenia." , "cały str: "+str+".");
		}
		if((arr.get(arr.size()-1).type == Blok.OPERATION && arr.get(arr.size()-1).str.matches("[^+-]"))) {
			throw new WrongSyntaxException("Operacja " + arr.get(arr.size()-1).str + " nie może stać na końcu wyrażenia." , "cały str: "+str+".");
		}
	}
	
	protected int find(String str, int side) {
		if(side == 1) {
			for(int i=0;i<arr.size();i++) {
				if(arr.get(i).str.equals(str))
					return i;
			}
		}
		if(side == -1) {
			for(int i=arr.size()-1;i>-1;i--) {
				if(arr.get(i).str.equals(str))
					return i;
			}
		}
		return -1;
	}
	
	protected int findConcatenation(int side) {
		//jak są dwa nie-operatory napisane z rzędu, daje indeks pierwszego z nich, side = +-1
		boolean ostatniNieOperator = false;
		if(side == 1) {
			for(int i=0;i<arr.size();i++) {
				if(arr.get(i).type == Blok.OPERATION) {
					ostatniNieOperator = false;
					continue;
				}
				if(ostatniNieOperator)
					return i-1;
				ostatniNieOperator = true;
			}
		}
		if(side == -1) {
			for(int i=arr.size()-1;i>-1;i--) {
				if(arr.get(i).type == Blok.OPERATION) {
					ostatniNieOperator = false;
					continue;
				}
				if(ostatniNieOperator)
					return i;
				ostatniNieOperator = true;
			}
		}
		return -1;

	}
	
 	public void print() {
		for(int i=0; i<arr.size(); i++) {
			arr.get(i).print();
		}
	}
 	
 	public String write() {
 		String ret = "";
		for(int i=0; i<arr.size(); i++) {
			ret += arr.get(i).write() + ",   ";
		}
		return ret;
 	}
	
	protected BlokList subList(int begin, int end) {
		//kreuje podlistę od indeksu begin do end-1 włącznie
		if(begin >= arr.size() || end <= 0 || end <= begin) {
			return new BlokList();
		}
		if(begin < 0 || end > arr.size())
			throw new IllegalArgumentException("Podane indeksy są złe.");
		BlokList sub = new BlokList();
		for(int i=begin;i<end;i++) 
			sub.arr.add(arr.get(i));
		return sub;
	}
	
	//TODO: poniższe funkcje chyba są zbędne, można usunąć
	/*
	protected static int indexOf(String[] arr, String str) {
		for(int i=0;i<arr.length;i++) {
			if(arr[i].equals(str))
				return i;
		}
		return -1;
	}
	
	protected static int indexOf(ArrayList<String> arr, String str) {
		for(int i=0;i<arr.size();i++) {
			if(arr.get(i).equals(str))
				return i;
		}
		return -1;

	}
	
	protected static boolean contains(String[] arr, String str) {
		return indexOf(arr, str) == -1 ? false : true;
	}
	*/
	LinkedList<BlokList> splitByComma() {
		LinkedList<BlokList> ret = new LinkedList<BlokList>();
		ret.add(new BlokList());
		for(Blok b : arr) {
			if(b.type == Blok.PRZECINEK) {
				ret.add(new BlokList());
				continue;
			}
			ret.getLast().arr.add(b);
		}
		return ret;
	}
	
	protected static boolean isSpecial(char chr) {
		return (""+chr).matches(SPECJALNE);
	}		
	
	protected static String configureStr(String str) {
		//usuwa zewnętrzne nawiasy
		int[] konce = wNawiasach(str, 0);
		while(konce[0]==0 && konce[1] == str.length()-1) {
			str = str.substring(1, str.length()-1);
			if(str.equals(""))
				return "";
			konce = wNawiasach(str, 0);
		}
		return str;
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
			if( (""+str.charAt(konce[0])).matches(OPERATORY + "|[\\(\\)\\{\\}]") ) {
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
			if((""+str.charAt(konce[1])).matches(OPERATORY+"|[\\(\\)\\{\\}]")) {
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

	
	private static int[] wNawiasachKl(String str, int index) throws WrongSyntaxException {
		//sprawdza czy pole o indeksie index jest zawarte pomiędzy dwoma nawiasami {}. niedozwolona jest sytuacja, w której takie nawiasy są w sobie zwarte
		//jeśli tak zwraca ich położenia jeśli nie zwraca -1 dla odpowiednich stron. wynik {-1, (.)!=-1} lub odwrotnie oznacza błędną składnię
		//zwraca położenia najbardzei wewnętrznych nawiasów możliwych
		int[] konce = {index, index};
		boolean napotkanoL = false;
		boolean napotkanoP = false;
		boolean napotkanoOkrągły = false;
		if(str.charAt(index) == '{') napotkanoL = true;
		if(str.charAt(index) == '}') napotkanoP = true;
		while(!napotkanoL) {
			konce[0]--;
			if(konce[0] == -1)
				break;
			switch(str.charAt(konce[0])) {
			case '{':
				napotkanoL = true;
				break;
			case '}':
				konce[0] = -1;
				napotkanoL = true;
				break;
			case '(':
				napotkanoOkrągły = true;
				break;
			case ')':
				napotkanoOkrągły = true;
				break;
			}
		}
		while(!napotkanoP) {
			konce[1]++;
			if(konce[1] == str.length()) {
				konce[1] = -1;
				break;
			}
			switch(str.charAt(konce[1])) {
			case '{':
				konce[1] = -1;
				napotkanoP = true;
				break;
			case '}':
				napotkanoP = true;
				break;
			case '(':
				napotkanoOkrągły = true;
				break;
			case ')':
				napotkanoOkrągły = true;
				break;
			}
		}
		if(napotkanoOkrągły && konce[0] > 0 && konce[1]>0)
			throw new WrongSyntaxException("W nawiasach klamrowych nie mogą występować okrągłe nawiasy.\nString: " + str + "indek:" + index+".",
					"W nawiasach klamrowych nie mogą występować okrągłe nawiasy.");
		return konce;
	} 
	
	private static int[] wNawiasach(String str, int index) {
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
		//modyfikuje argumenty int[] konce, zwraca czy je zmieniło
		//modyfikuje je, tak aby wskazywały na nawiasy kwadratowe wokół indeksu
		int[] temp = wNawiasachKw(str, index);
		if((temp[0] == -1 && !(temp[1] == -1)) || (temp[1] == -1 && !(temp[0] == -1))) {
			throw new WrongSyntaxException("Kwadratowe nawiasy się nie zamykają. Występuje nawias " + (temp[0] == -1 ? "]" : "[") + " bez odpowiadającego mu nawiasu " +  (temp[0] == -1 ? "[." : "]."),
					String.format("\nindex: %d.  końce:  %d, %d. cały str: %s.",index, konce[0], konce[1], str));
		}
		if(temp[0] > -1 && temp[1] > -1) {
			konce[0] = temp[0];
			konce[1] = temp[1]+1;
			return true;
		}
		return false;
	}
	
	private static boolean checkKlamBrackets(String str, int index, int[] konce) throws WrongSyntaxException {
		//modyfikuje argumenty int[] konce, zwraca czy je zmieniło
		//modyfikuje je tak 
		int[] temp = wNawiasachKl(str, index);
		if((temp[0] == -1 && !(temp[1] == -1)) || (temp[1] == -1 && !(temp[0] == -1))) {
			throw new WrongSyntaxException("Kwadratowe nawiasy się nie zamykają. Występuje nawias " + (temp[0] == -1 ? "}" : "{") + " bez odpowiadającego mu nawiasu " +  (temp[0] == -1 ? "{." : "}."),
					String.format("\nindex: %d.  końce:  %d, %d. cały str: %s.",index, konce[0], konce[1], str));
		}
		if(temp[0] > -1 && temp[1] > -1) {
			konce[0] = temp[0];
			konce[1] = temp[1]+1;
			return true;
		}
		return false;
	}
	
	private static Blok znajdzBlokPom(String str, int index) throws WrongSyntaxException {
		//zwraca położenia końców bloku wokół index. Zwraca indeks lewej strony oraz indeks+1 prawej.
		//nie włącza funkcji i ich argumentów w jeden blok
		if(index < 0)
			throw new IllegalArgumentException("Niepoprawne argumenty. Indeks musi być >= 0. Indeks: "+index + " str: " + str);
		if(index >= str.length())
			return new Blok(str, new int[] {index+1,index+1}, Blok.NULL);
		int[] konce = {index, index};
		int type;
		boolean isParenthases = (""+str.charAt(index)).matches("[\\(\\)]");
		boolean isWord = (""+str.charAt(index)).matches("[a-zA-Z" + GRECKIALFABET+ "]");
		boolean isNum = (""+str.charAt(index)).matches("[0-9\\.]");
		boolean isOperation = (str.charAt(index) + "").matches(OPERATORY);
		boolean hasSquareBrackets = false;
		boolean hasKlBrackets = false;
		if(checkSquareBrackets(str, index, konce)) {
			konce[0] -= 1;
			if(konce[0] == -1)
				throw new WrongSyntaxException("Nawiasy kwadratowe nie mogą wystąpić na początku podanej funkcji.");
			isWord = true;
			isNum = false;
			isParenthases = false;
			isOperation = false;
			hasSquareBrackets = true;
		}else
			if(checkKlamBrackets(str, index, konce)) {
				konce[0] -= 1;
				if(konce[0] == -1)
					throw new WrongSyntaxException("Nawiasy klamrowe nie mogą wystąpić na początku podanej funkcji.");
				isWord = true;
				isNum = false;
				isParenthases = false;
				isOperation = false;
				hasKlBrackets = true;
			}
		if(isOperation) {
			type = Blok.OPERATION;
			return new Blok(str, index, index+1, type);
		}
		if(isWord) {
			type = Blok.WORD;
			while(konce[1] < str.length() && !hasSquareBrackets  && !hasKlBrackets && (""+str.charAt(konce[1])).matches("[a-zA-Z\\[\\{" + GRECKIALFABET +"]")) {
				switch(str.charAt(konce[1])) {
				case '[':
					checkSquareBrackets(str, konce[1], konce);
					hasSquareBrackets = true;
					konce[0] -= 1;
					break;
				case '{':
					checkKlamBrackets(str, konce[1], konce);
					hasKlBrackets = true;
					konce[0] -= 1;
					break;
				default: 
					konce[1] += 1;
					break;
				}
			}
			if(!(""+str.charAt(konce[0])).matches("[a-zA-Z" + GRECKIALFABET+"]"))
				throw new WrongSyntaxException("Przed nawiasem " + str.charAt(konce[0]+1) + " musi stać litera, a stoi znak " + str.charAt(konce[0]) + ".");
			while(konce[0] > -1 && (""+str.charAt(konce[0])).matches("[a-zA-Z" + GRECKIALFABET + "]")) {
					konce[0] -= 1;
			}
			konce[0]++;
			return new Blok(str, konce[0], konce[1], type);
		}
		
		if(isNum){
			type = Blok.NUMBER;
			int countCommas = str.charAt(index) == '.' ? -1 : 0;
			while((""+str.charAt(konce[0])).matches("[0-9\\.]")) {
				countCommas += str.charAt(konce[0]) == '.' ? 1 : 0;
				konce[0] -= 1;
				if(konce[0] == -1)
					break;
			}
			konce[0]++;
			while((""+str.charAt(konce[1])).matches("[0-9\\.]")) {
				countCommas += str.charAt(konce[1]) == '.' ? 1 : 0;
				konce[1] += 1;
				if(konce[1] == str.length())
					break;
			}
			if(countCommas > 1) 
				throw new WrongSyntaxException("Liczba \"" + str.substring(konce[0], konce[1]) + "\" zawiera w sobie więcej niż jedną kropkę.");
			if(countCommas == 1 && str.length() == 1)
				throw new WrongSyntaxException("Występuje kropka, która nie jest wewnątrz liczby.");
			return new Blok(str, konce, type);
		}
		if(isParenthases) {
			type = Blok.PARENTHASES;
			int[] temp = wNawiasach(str, index);
			if((temp[0] == -1 && !(temp[1] == -1)) || (temp[1] == -1 && !(temp[0] == -1)))
				throw new WrongSyntaxException(	"Nawiasy () nie domykają się poprawnie.\n "
								+ "Występuje nawias \'" + (temp[1]==-1 ? "(\'" : ")\'") + " bez odpowiadającego mu nawiasu \'" + (temp[0]==-1 ? "(\'." : ")\'."),
								"\n caly str: " + str);
			konce[0] = temp[0];
			konce[1] = temp[1]+1;
			return new Blok(str, konce, type);
		}
		if(str.charAt(index) == ',') {
			return new Blok(str, new int[] {index, index+1}, Blok.PRZECINEK);
		}
		else {
			throw new IllegalArgumentException("Niepoprawne argumenty. Podanemu indeksowi nie można przypisać ani cyfry ani litery (ani [])\nindex:"+
						index+".  cos na indeksie:"+str.charAt(index) + ".  cały str: " + str+".");
		}
	}

	private static Blok znajdzBlok(String str, int index) throws WrongSyntaxException {

		//zwraca położenia końców bloku wokół index. Zwraca indeks lewej strony oraz indeks+1 prawej.
		Blok blok = znajdzBlokPom(str, index);

		if(blok.type == Blok.WORD) {
			if(Functions.checkIfNmdFunc(blok.str)) {
				if(blok.kon == str.length()+1)
					throw new IllegalArgumentException("Zawarta jest funkcja bez argumentu." + "\n zawara funckja: " + blok.str);
				Blok prawaStrona = znajdzBlokPom(str, blok.kon);
				if(prawaStrona.type != Blok.PARENTHASES)
					throw new WrongSyntaxException("Funkcje muszą mieć po sobie nawias, a funkcja: " + blok.str + " go nie ma.");
				return new BlokWthDefFunction(str, prawaStrona.pocz, prawaStrona.kon, Blok.FUNCTION, Functions.returnNmdFunc(blok.str));
			}
				
		}
		if(blok.type == Blok.PARENTHASES) {
			if(blok.pocz == 0)
				return blok;
			Blok lewaStrona = znajdzBlokPom(str, blok.pocz-1);
			if(lewaStrona.type == Blok.WORD && Functions.checkIfNmdFunc(lewaStrona.str))
				return new BlokWthDefFunction(str, blok.pocz, blok.kon, Blok.FUNCTION, Functions.returnNmdFunc(lewaStrona.str));
		}
		return blok;

	}
	
	public static void main(String[] args) throws WrongSyntaxException {
		new BlokList("1.0a{5.90[]}d2.3dsa[32.13243[dsd[]][]]as[]j[]").print();
	}

}
