package poczatek;

public class Functions {
	final static int CONST = 1;
	final static int NONCONST = 0;
	final static Func exp = new Func() {
		@Override
		public Complex evaluate(Complex z) {
			return Complex.exp(z);
		}

		@Override
		public String write() {
			return "exp";
		}
	};
	final static Func Ln = new Func() {
		@Override
		public Complex evaluate(Complex z) {
			return Complex.Ln(z);
		}

		@Override
		public String write() {
			return "Ln";
		}
	};
	final static Func e = new Func() {
		@Override
		public Complex evaluate(Complex z) {
			return new Complex(Math.E);
		}
		@Override
		public String write() {
			return "e";
		}
	};
	final static Func pi = new Func() {
		@Override
		public Complex evaluate(Complex z) {
			return new Complex(Math.PI);
		}

		@Override
		public String write() {
			return "pi";
		}
	};
	final static Func phi = new Func() {
		@Override
		public Complex evaluate(Complex z) {
			return new Complex((1+Math.sqrt(5))/2);
		}

		@Override
		public String write() {
			return "phi";
		}
	};
	final static Func Id = new Func() {
		@Override
		public Complex evaluate(Complex z) {
			return z;
		}

		@Override
		public String write() {
			return "z";
		}
	};
	final static Func Re = new Func() {
		@Override
		public Complex evaluate(Complex z) {
			return new Complex(z.x);
		}

		@Override
		public String write() {
			return "Re";
		}
	};
	final static Func Im = new Func() {
		@Override
		public Complex evaluate(Complex z) {
			return new Complex(z.y);
		}

		@Override
		public String write() {
			return "Im";
		}
	};

}
