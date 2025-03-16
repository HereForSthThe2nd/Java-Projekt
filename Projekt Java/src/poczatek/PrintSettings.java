package poczatek;

public class PrintSettings {
	int doubleAcc;
	final static PrintSettings defaultSettings = new PrintSettings(3);
	public PrintSettings(int doubleAcc) {
		this.doubleAcc = doubleAcc;
	}

}
