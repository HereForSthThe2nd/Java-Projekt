/*
 * niekiedy w celu prostoty wewnątrz programu funkcje zostały zapisane przy pomocy zamiany stringa
 * ten błąd się wyrzuca jeśli taka funkcja zoestała błędnie zapisana
 */

package funkcja;
@Deprecated
public class WewnetzrnaFunkcjaZleZapisana extends Exception {

	public WewnetzrnaFunkcjaZleZapisana() {
		// TODO Auto-generated constructor stub
	}

	public WewnetzrnaFunkcjaZleZapisana(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public WewnetzrnaFunkcjaZleZapisana(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public WewnetzrnaFunkcjaZleZapisana(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public WewnetzrnaFunkcjaZleZapisana(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}
