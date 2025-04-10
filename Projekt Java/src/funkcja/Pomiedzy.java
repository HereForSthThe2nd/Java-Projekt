package funkcja;

import java.util.LinkedList;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import ogolne.Complex;
import ogolne.Settings;

abstract class Pomiedzy extends Function{
	//klasa do wsadzania nad Sumy, iloczyny oraz funkcje z nawiasami
	//ma na celu zapamiętanie etapu na którym zostało zostawione sprawdzanie czy dwie funkcje są równe (czy można je dopasować by do siebie pasowały (.check))
	Function funkcja;
	final String name;//tylko na cele teoretycznego ładnego wyświetlania
	
	@Override
	public boolean check(Function func) {
		return funkcja.check(func);
	}
	
	protected Pomiedzy(Function funkcja, String name) {
		super(Functions.SPECIAL, funkcja.nofArg);
		this.funkcja = funkcja;
		this.name = name;
	}

	@Override
	protected Complex evaluate(Complex[] arg) {
		return funkcja.evaluate(arg);
	}

	@Override
	protected Function re() {
		funkcja = funkcja.re();
		return this;
	}

	@Override
	protected Function im() {
		funkcja = funkcja.im();
		return this;
	}

	@Override
	protected String write(Settings settings) {
		return name+"{"+funkcja.write(settings)+"}";
	}

	@Override
	protected Function putArguments(Function[] args) {
		funkcja = funkcja.putArguments(args);
		return this;
	}

	@Override
	protected Function replaceMatchers() {
		return funkcja.replaceMatchers();
	}

	@Override
	protected Function expand() {
		funkcja = funkcja.expand();
		return this;
	}

	@Override
	protected Function removeInners() {
		funkcja = funkcja.removeInners();
		return this;
	}

	@Override
	protected Function copyPom(MatcherReturn matcherRet) {
		throw new IllegalStateException("To nie powinno się wogóle stać");
	}

	@Override
	final protected FunctionInfo info() {
		return funkcja.info();
	}

	@Override
	protected Function simplify(SimplifyRule rule) {
		funkcja = funkcja.simplify(rule);
		funkcja = rule.simplify(funkcja);
		
		return this;
	}

	@Override
	protected void resetPomiedzy() { 
		funkcja.resetPomiedzy();
	}
}

//pamięta etap w check iloczynu oraz sumy
class CheckStageMS extends Pomiedzy{
	final private Function[] args;//summands dla sumy lub czynniki dla iloczynu
	final RememberStageMS stage;
	protected CheckStageMS(Function funkcja) {
		super(funkcja, "Pamiętacz");
		switch(funkcja.type) {
		case Functions.ADD:
			args = ((FuncSum)funkcja).summands;
			break;
		case Functions.MULT:
			args = ((FuncMult)funkcja).f;
			break;
		default:
			throw new IllegalArgumentException("Można podać tylko sumy oraz iloczyny jako argument. odany argument: " + 
					funkcja == null ? "null" : funkcja.write(new Settings()));
		}
		stage = new RememberStageMS();
	}

	@Override
	public boolean match(Function func, MatcherReturn mr) {
		Function[] args;
		switch(func.type) {
		case Functions.ADD:
			args = ((FuncSum)func).summands;
			break;
		case Functions.MULT:
			args = ((FuncMult)func).f;
			break;
		default:
			return false;
			}
		int startMarker = mr.setMarker();
		boolean ret = MatcherMethods.match(args, this.args, stage, mr, startMarker);
		return ret;
	}

	@Override
	protected void resetPomiedzy() {
		super.resetPomiedzy();
		stage.reset();
	}
	
}

//TODO: najprawdopoobniej zbędne, usunąć kiedyś
//pamięta etap w check funkcji (nawiasowej)
class CheckstageFunc extends Pomiedzy{

	protected CheckstageFunc(Function funkcja) {
		super(funkcja, "PomiedzFunc");
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean match(Function func, MatcherReturn mr) {
		if(func.type != Functions.NAMED || !((FuncNamed)funkcja).name.equals(((FuncNamed)func).name))
			return false;
		int num = ((Func)funkcja).args.length;
		Function[] matchers = ((Func)this.funkcja).args;
		Function[] args = ((Func)func).args;
		int i = 0;
		while(i<num && i >= 0) {
			if(MatcherMethods.match(matchers[i], args[i], mr)) {
				i++;
			}else {
				i--;
			}
		}
		return i < 0 ? false : true;
	}

	@Override
	protected void resetPomiedzy() {
		super.resetPomiedzy();
		// TODO Auto-generated method stub		
	}
	
}

class RememberStageMS{
	LinkedList<Integer> matcherToArgMap;
	int i0;
	int j0;
	LinkedList<Integer> staticMatchers;
	LinkedList<Integer> matchers2;
	LinkedList<Integer> argsMatched;
	boolean goneThroughOnce;
	boolean returnsFalse;
	LinkedList<Integer> markersOfMatchersAtMatching;
	Integer nextTimResetTo;
	public RememberStageMS() {
		reset();
	}
	public void reset() {
		markersOfMatchersAtMatching = new LinkedList<Integer>();
		matcherToArgMap = new LinkedList<Integer>();
		matchers2 = new LinkedList<Integer>();
		argsMatched = new LinkedList<Integer>();
		staticMatchers = new LinkedList<Integer>();
		goneThroughOnce = false;
		returnsFalse = false;
		i0 = 0;
		j0 = 0;
		nextTimResetTo = -1;
	}
}