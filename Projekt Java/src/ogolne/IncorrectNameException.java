package ogolne;

//dotyczy próby stworzenia nowej nazwy funkcji zmiennej lub stałej, np. kedy ta pokrywa się z już zdefiniowaną funkcją
public class IncorrectNameException extends Exception {
	final String messageForUser;
	public IncorrectNameException(String message) {
		super(message);
		messageForUser = message;
	}

}
