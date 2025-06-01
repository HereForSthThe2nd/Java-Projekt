/*
 * błąd zachodzi jeśli użytkownik zle zapisze funkcję
 */

package funkcja;

public class WrongSyntaxException extends Exception {
	public String messageForUser;
	public WrongSyntaxException(String message, String messageForDebug) {
		super(message + "\n" + messageForDebug);
		TimeKeeping.endTimer("BlokList");
		this.messageForUser = message;
	};
	public WrongSyntaxException(String message) {
		super(message);
		messageForUser = message;
	};
}
