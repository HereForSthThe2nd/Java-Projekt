/*
 * funkcja zawierająca stałą numeryczną
 */

package funkcja;

//publiczne tymczasowo na cele testow
public class FuncNumConst extends Function {
	//chodzi o to czy trzeba dodawać nawiasy przy wypisywaniu.
	final static int DODR=1, //1.09, i, 0   (i to jest szczególny przypadek)
			DODUR=2, //2i, 2.11i
			UJEMNYPIERWSZY=3,//-1, -i, -1+i
			ZES = 4;//1+i, 1-i
	final Complex a;
	final int form;
	public FuncNumConst(Complex a){ 
		super(Functions.NUMCONST, 0);
		this.a = a;
		if(a.x==0 && a.y==1) {
			form = DODR;
			return;
		}
		if(a.x>0 && a.y != 0) {
			form = ZES;
			return;
		}
		if(a.x<0 || (a.y<0 && a.x==0)) {
			form = UJEMNYPIERWSZY;
			return;
		}
		if(a.y>0) {
			form = DODUR;
			return;
		}
		form = DODR;
	}
	
	@Override
	protected Complex evaluate(Complex[] arg) {
		return a;
	}

	@Override
	protected String write(Settings settings) {
		return a.print(settings.doubleAcc);
	}

	@Override
	protected Function putArguments(Function[] args) {
		return this;
	}

	@Override
	protected Function expand() {

		return this;
	}

	@Override
	public boolean check(Function f) {
		if(f.type != this.type)
			return false;
		return this.a.equals( ((FuncNumConst)f).a );
	}

	@Override
	protected Function simplify(SimplifyRule rule) {
		return rule.simplify(this);
	}

	@Override
	protected Function re() {
		return new FuncNumConst(new Complex(a.x));
	}

	@Override
	protected Function im() {
		return new FuncNumConst(new Complex(a.y));
	}

	@Override
	protected Function replaceMatchers() {return this;}

	@Override
	protected Function removeInners() {
		return this;
	}	
}