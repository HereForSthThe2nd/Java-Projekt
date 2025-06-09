package funkcja;

//dotyczy próby stworzenia nowej nazwy funkcji zmiennej lub stałej, np. kedy ta pokrywa się z już zdefiniowaną funkcją
public class IncorrectNameException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1522571844337057752L;
	final public String messageForUser;
	public IncorrectNameException(String message) {
		super(message);
		messageForUser = message;
	}

}
