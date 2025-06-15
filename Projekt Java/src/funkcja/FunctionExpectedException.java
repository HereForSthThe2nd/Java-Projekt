/*
 * błąd zachodzi jeśli użytkownik zle zapisze funkcję
 */

package funkcja;

public class FunctionExpectedException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 84463736441828268L;
	public String messageForUser;
	public FunctionExpectedException(String message, String messageForDebug) {
		super(message + "\n" + messageForDebug);
		this.messageForUser = message;
	};
	public FunctionExpectedException(String message) {
		super(message);
		messageForUser = message;
	};
}
