package Inne;

//jednak nie będę potrzebował, ale zostawię wrazie co
public class Polynomial {
	//współczynniki numerowane od największego ([0]), do najmniejszego ([length-1])
	Complex[] wsp;
	public Complex evaluate(Complex z) {
		Complex ret = wsp[0];
		for(int i=1; i<wsp.length;i++) {
			ret = Complex.add(Complex.mult(z, ret), wsp[1]);
		}
		return ret;
	}
	
}
