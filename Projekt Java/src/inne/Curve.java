package inne;

public abstract class Curve<E> {
	abstract E c(double t);
	abstract double getm();
	abstract double getM();
	public PolyCurve<E> toPoly(int acc){
		PolyCurve<E> ret = new PolyCurve<E>();
		
		for(double t = getm(); t < getM(); t += (getM() - getm()) / acc) {
			ret.addPoint(c(t));
		}
		
		ret.addPoint(c(getM()));
		return ret;
	}
}
