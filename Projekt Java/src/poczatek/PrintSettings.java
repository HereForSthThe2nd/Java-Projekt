package poczatek;

public class PrintSettings {
	int doubleAcc;
	final static PrintSettings defaultSettings = new PrintSettings(3);
	public PrintSettings(int doubleAcc) {
		if(doubleAcc <= 1)
			throw new IllegalArgumentException("doubleAcc musi być równe co najmniej 1, a jest równe " + doubleAcc + ".");
		this.doubleAcc = doubleAcc;
	}

}
