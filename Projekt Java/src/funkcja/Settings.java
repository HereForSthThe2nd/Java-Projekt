/*
 * ustawienia głównie sposobu wypisywania,
 * poza tym też sposobu upraszcznia 
 */

package funkcja;

public class Settings {
	public int doubleAcc = 3;	//do wypisywania
	public boolean writePow = true; //czy wypisuje ^
	public boolean strictPow = true; //czy podczas upraszczania potęga zachowuje się ściśle (np. czy zamienia (z^2)^(1/2) na z czy nie)
	public boolean writeRealVar = true; // czy pisze Re(z) czy x 
	public boolean evaluateConstants = false; //czy podczas upraszczania zamienia np.exp(3*pi) na stałą numeryczną
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
	public Settings copy() {
		return new Settings(doubleAcc, writePow, strictPow, evaluateConstants, writeRealVar);
	}
}
