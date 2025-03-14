package poczatek;

public class PrintSettings {
	int doubleAcc;
	boolean expand;
	boolean defaultPowIsStrict;
	final static PrintSettings defaultSettings = new PrintSettings(3, false, false);
	public PrintSettings(int doubleAcc,	boolean expand,	boolean defaultPowIsStrict) {
		this.doubleAcc = doubleAcc;
		this.expand = expand;
		this.defaultPowIsStrict = defaultPowIsStrict; //czy podczas upraszczania przestrzeba się rygorystycznie gałęzi funkcji ^
	}

}
