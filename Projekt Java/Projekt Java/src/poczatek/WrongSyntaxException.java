package poczatek;


public class WrongSyntaxException extends Exception {
	public String messageForUser;
	public WrongSyntaxException() {};
	public WrongSyntaxException(String message, String messageForUser) {
		super(message);
		this.messageForUser = messageForUser;
	};
	public WrongSyntaxException(String message) {
		super(message);
		messageForUser = message;
	};
}
