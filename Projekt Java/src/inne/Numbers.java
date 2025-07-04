package inne;

public class Numbers {
	static public String toStr(double a, int doubleAcc, int whenToShorten) {
		//dla doubla
		String format = "%."+doubleAcc;
		if((Math.abs(a) < Math.pow(10, whenToShorten) && Math.abs(a) >= Math.pow(10, -whenToShorten+1)) || a == 0) {
			format += "f";
			return String.format(format, a);
		}
		format += "e";
		return String.format(format, a);
	}

	static public double lesserWholeOf(double d) {
		//(lesser (or equal) in absolute value)
		d = Math.abs(d);
 		int order = (int)Math.floor(Math.log10(d));
 		int subOrder = 1;
 		if(d <= 2 * Math.pow(10, order))
 			subOrder = 1;
 		if(2 * Math.pow(10, order) < d && d <= 5 * Math.pow(10, order))
 			subOrder = 2;
 		if(5 * Math.pow(10, order) < d)
 			subOrder = 5;
 		return Math.pow(10, order) * subOrder;
	}
	
	static public double greaterWholeOf(double d) {
		//greater(or equal) in absolute value
		d = Math.abs(d);
 		int order = (int)Math.floor(Math.log10(d));
 		int subOrder = 1;
 		if(d <= 2 * Math.pow(10, order))
 			subOrder = 2;
 		if(2 * Math.pow(10, order) < d && d <= 5 * Math.pow(10, order))
 			subOrder = 5;
 		if(5 * Math.pow(10, order) < d)
 			subOrder = 10;
 		return Math.pow(10, order) * subOrder;

	}
}
