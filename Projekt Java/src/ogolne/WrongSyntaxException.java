/*
 * błąd zachodzi jeśli użytkownik zle zapisze funkcję
 */

package ogolne;

public class WrongSyntaxException extends Exception {
	public String messageForUser;
	public WrongSyntaxException(String message, String messageForDebug) {
		super(message + "\n" + messageForDebug);
		this.messageForUser = message;
	};
	public WrongSyntaxException(String message) {
		super(message);
		messageForUser = message;
	};
}
