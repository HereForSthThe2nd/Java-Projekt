package funkcja;

public class Settings {
	int doubleAcc;	//do wypisywania
	boolean writePow; //czy wypisuje ^
	boolean strictPow; //czy podczas upraszczania potęga zachowuje się ściśle
	public Settings() {
		this.doubleAcc = 3;
		this.strictPow = true;
		this.writePow = true;
	}
	public Settings(int doubleAcc) {
		this.doubleAcc = doubleAcc;
		this.strictPow = true;
		this.writePow = true;
	}
	public Settings(int doubleAcc, boolean writePow, boolean strictPow) {
		if(doubleAcc < 1)
			throw new IllegalArgumentException("doubleAcc musi być równe co najmniej 1, a jest równe " + doubleAcc + ".");
		this.doubleAcc = doubleAcc;
		this.strictPow = strictPow;
		this.writePow = writePow;
	}

}
