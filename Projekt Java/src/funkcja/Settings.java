package funkcja;

public class Settings {
	int doubleAcc = 3;	//do wypisywania
	boolean writePow = true; //czy wypisuje ^
	boolean strictPow = true; //czy podczas upraszczania potęga zachowuje się ściśle
	boolean writeRealVar = true; // czy pisze Re(z) czy x 
	boolean evaluateConstants = false;
	public Settings() {
		this.doubleAcc = 3;
		this.strictPow = true;
		this.writePow = true;
		this.writeRealVar = true;
		this.evaluateConstants = false;
	}
	public Settings(int doubleAcc) {
		this.doubleAcc = doubleAcc;
		this.strictPow = true;
		this.writePow = true;
		this.writeRealVar = true;
		this.evaluateConstants = false;
	}
	public Settings(int doubleAcc, boolean writePow, boolean strictPow, boolean evaluateConstatns, boolean writeRealVar) {
		if(doubleAcc < 1)
			throw new IllegalArgumentException("doubleAcc musi być równe co najmniej 1, a jest równe " + doubleAcc + ".");
		this.doubleAcc = doubleAcc;
		this.strictPow = strictPow;
		this.writePow = writePow;
		this.evaluateConstants = evaluateConstatns;
		this.writeRealVar = writeRealVar;
	}

}
