/*
 * błąd zachodzi jeśli użytkownik zle zapisze funkcję
 */

package funkcja;

public class FunctionExpectedException extends Exception {
	public String messageForUser;
	public FunctionExpectedException(String message, String messageForDebug) {
		super(message + "\n" + messageForDebug);
		TimeKeeping.endTimer("BlokList");
		this.messageForUser = message;
	};
	public FunctionExpectedException(String message) {
		super(message);
		messageForUser = message;
	};
}
