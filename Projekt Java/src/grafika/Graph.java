/*
 * wyświetlanie wykresów
 * prowizoryczne
 */

package grafika;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.IOException;
import java.text.AttributedCharacterIterator;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.CompletionException;

import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import Inne.Bool;
import Inne.Complex;
import funkcja.*;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;

import java.awt.image.BufferedImage;

public class Graph extends JPanel{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8436283896706339087L;
	//w zmianie funkcji na ile bloków dzieli graf (w poziomie * w pionie)
	private static final int PODZIALRYSOWANIA = 10;
	//co ile pikseli oblicza wartość funkcji
	private static final int DELTA = 4;
	BufferedImage img;
	BufferedImage imgSmaller;
	private Complex[][] values; //może trochę overkill, zajmuje z 5 razy więcej miejsca niż obraz, może np. zrobić by co 3 - 10 ^ 2 pikseli obliczało wartości po funkcji (i je tylko wtedy zapisywało) a reszę jakoś interpolowało
	Coordinates coords;
	FunctionPowloka function;
	CmplxToColor colorMap;
	double[] colorMapParams;
	JPanel obraz;
	Foreground foreGround;
	static CmplxToColor basic;
	private static CmplxToColor noArg;
	private static CmplxToColor halfPlane;
	private static CmplxToColor circle;
	private static CmplxToColor poziomice;
	public static CmplxToColor[] listaKolorowan;
	
	private static double normalizacja(double r) {
		//zwraca liczbę z przedziału 0, 1
		if(r<-0.00001) {
			throw new IllegalArgumentException("r musi być nieuemne. podane r: " + r);
		}
		r = r>0 ? r : 0;
		//return 2/Math.PI * (Math.atan(r));
		if(Double.isInfinite(r)) {
			return 1;
		}
		return 2/Math.PI * (Math.atan(Math.log(r+1)));
	}
		
	public Graph(int bok) {
		img = new BufferedImage(bok,bok,BufferedImage.TYPE_INT_ARGB);
		imgSmaller = new BufferedImage(bok/DELTA, bok/DELTA, BufferedImage.TYPE_INT_ARGB);
		obraz = new JPanel() {
			private static final long serialVersionUID = 1L;
			@Override
			public void paint(Graphics g) {
				super.paintComponent(g);
				g.drawImage(img, 0, 0, this);
            }
		};
		obraz.setSize(bok, bok);
		obraz.setPreferredSize(new Dimension(bok,bok));
		foreGround = new Foreground();
		foreGround.setSize(bok, bok);
		foreGround.setPreferredSize(new Dimension(bok, bok));
		add(foreGround);
		add(obraz);
		Border obrBorder = BorderFactory.createLineBorder(Color.black, 3);
		foreGround.setBorder(obrBorder);
		setPreferredSize(new Dimension(img.getWidth()+10, img.getHeight()+10));
		setSize(new Dimension(img.getWidth()+10, img.getHeight()+10));
		int dodValX = (DELTA - (getWidth() - 1) % DELTA) % DELTA;
		int dodValY = (DELTA - (getHeight() - 1) % DELTA) % DELTA;
		values = new Complex[img.getWidth() + dodValX][img.getHeight() + dodValY];
	}
	
	static {
		basic = new CmplxToColor() {
			
			private static double[] complexToHSL(Complex z, double colorSpeedChange) {
				if(z == null || Double.isNaN(z.x) || Double.isNaN(z.y)) {
					return null;
				}
				double[] HSL = new double[4];
				HSL[3] = 255; // przejrzystość
				//HSL[0] w -pi/3, 5/3pi
				HSL[0] = z.arg() + 2.0/3*Math.PI;
				HSL[1] = 1;
				//HSL[2] = 2/Math.PI*Math.atan( Math.log(Math.pow(z.mod(), 1/lSpeedChange)+1) );
				HSL[2] = normalizacja( Math.pow(z.mod(), colorSpeedChange ) / (Math.abs(colorSpeedChange*1.5)+0.6));
				if(HSL[2]>1 || HSL[2]<0) {
					throw new IllegalStateException();
				}
				return HSL;
			}
			
			@Override
			public Integer colorOf(Complex z, double... parameters) {
				if(parameters.length != 1) {
					throw new IllegalArgumentException("Ta funkcja przyjmuje tylko jeden parametr.");
				}
				int[] rgb = HSLToRGB(complexToHSL(z, parameters[0]));
				if(rgb == null) {
					return null;
				}
				return rgbToHex(rgb);
			}

			@Override
			public double[] defaultParams() {
				return new double[] {0.5};
			}

			@Override
			public String[] paramsNames() {
				return new String[] {"Szybkość zmiany koloru"};
			}

			@Override
			public String name() {
				return "Podstawowy";
			}
			
		};

		noArg = new CmplxToColor() {
			
			private static double[] complexToHSL(Complex z, double colorChangeSpeed) {
				if(z == null || Double.isNaN(z.x) || Double.isNaN(z.y)) {
					return null;
				}
				double HSL[] = new double[4];
				HSL[1] = 1;
				HSL[2] = 0.5;
				HSL[3] = 255;
				HSL[0] = 2*Math.PI * (0.85*normalizacja( Math.pow(z.mod(), colorChangeSpeed ))) - Math.PI/3;
				return HSL;
			}
			
			@Override
			public Integer colorOf(Complex z, double... parameters) {
				if(parameters.length != 1) {
					throw new IllegalArgumentException("Ta funkcja przyjmuje tylko jeden parametr.");
				}
				int[] rgb = HSLToRGB(complexToHSL(z, parameters[0]));
				if(rgb == null) {
					return null;
				}
				return rgbToHex(rgb);
			}

			@Override
			public double[] defaultParams() {
				return new double[] {0.5};
			}

			@Override
			public String[] paramsNames() {
				return new String[] {"Szybkość zmiany koloru"};
			}

			@Override
			public String name() {
				return "Moduł";
			}
		};
	
		halfPlane = new CmplxToColor() {
			
			@Override
			public String[] paramsNames() {
				return new String[] {"Re punktu na granicy", "Im punktu na granicy", "Kąt[stopnie]"};
			}
			
			@Override
			public double[] defaultParams() {
				return new double[] {0,0,0};
			}
			
			@Override
			public Integer colorOf(Complex z, double... parameters) {
				Complex z1 = Complex.subt(z, new Complex(parameters[0], parameters[1]));
				Complex rotated = Complex.mult(z1, Complex.exp(new Complex(0,-parameters[2] * Math.PI / 180)));
				return rgbToHex(rotated.y > 0 ? Color.black : rotated.y == 0 ? Color.red : Color.WHITE);
			}

			@Override
			public String name() {
				return "Półpłaszczyzna";
			}
		};
	
		circle = new CmplxToColor() {
			
			@Override
			public String[] paramsNames() {
				return new String[] {"x", "y", "r"};
			}
			
			@Override
			public double[] defaultParams() {
				return new double[] {0,0,1};
			}
			
			@Override
			public Integer colorOf(Complex z, double... parameters) {
				if(z == null)
					return null;
				double r = Complex.subt(z, new Complex(parameters[0], parameters[1])).mod();
				if(r == parameters[2])
					return rgbToHex(Color.red);
				return r < parameters[2] ? rgbToHex(Color.black) :rgbToHex(Color.white);
			}

			@Override
			public String name() {
				return "Koło";
			}
		};
	
		poziomice = new CmplxToColor() {
			
			@Override
			public String[] paramsNames() {
				return new String[] {"Gęstość poziomic", "Grubość poziomic"};
			}
			
			@Override
			public double[] defaultParams() {
				return new double[] {2, 0.3};
			}
			
			@Override
			public Integer colorOf(Complex z, double... parameters) {
				if(Double.isNaN(z.x) || Double.isNaN(z.y) || z == null) {
					return null;
				}
				int l;
				double rNorm = normalizacja(z.mod());
				l = (int)(235*rNorm+10);
				return rgbToHex( Math.abs(Math.log(z.mod()+1)% (1 / parameters[0])) < parameters[1]/10 ? new Color(l,0,l) : new Color(0,200,0));
			}

			@Override
			public String name() {
				return "Poziomice";
			}
		};
	
		listaKolorowan = new CmplxToColor[] {basic, noArg, halfPlane, circle, poziomice};
	}
	
	public Complex getValueAt(int x, int y) {
		//x oraz y to współrzędne piksela
		return getValueAt(new Point(x, y));
	}
	
	public Complex getValueAt(Point p) {
		try {
			return values[p.x][p.y];
		}catch(ArrayIndexOutOfBoundsException e) {
			return null;
		}
	}
	
	//dba o to aby graf był zawsze wyśrodkowany
	@Override
	public void doLayout() {
		super.doLayout();
		for(int i=0;i<getComponentCount();i++) {
			Component child = getComponent(i);
			child.setBounds((int) ( getWidth() / 2  - child.getPreferredSize().width / 2) , (int) ( getHeight() / 2  - child.getPreferredSize().height / 2),
					child.getPreferredSize().width, child.getPreferredSize().height);
		}
	}
		
	public Graph(int bok, FunctionPowloka f, Coordinates coords,CmplxToColor colorMap, double...parameters) {
		this(bok);
		change(f, coords, colorMap, parameters);
	}
	
	public void save(File imgfile) throws IOException {
		ImageIO.write(img, "png", imgfile);
	}
	
	public Coordinates rect(Complex lewyDolny, Complex prawyGorny) {
		return new Coordinates() {
			private Complex lewDolny = lewyDolny;
			private Complex prawDolny = prawyGorny;
			@Override
			public Complex pointToCmplx(Point p, Complex argLewDolny, Complex argPrawGorny) {
				Complex diag = Complex.subt(prawDolny, argLewDolny);
				Complex ret = Complex.add(argLewDolny, new Complex(	diag.x * p.x/img.getWidth(), diag.y * (img.getHeight()-p.y)/img.getHeight()));
				//System.out.println(p + "  " + ret.print(2) + " " + prawyGorny.print(2) + " " + lewyDolny.print(2) + " " + width);
				return ret;
			}
			
			@Override
			public Point cmplxToPoint(Complex z, Complex argLewDolny, Complex argPrawGorny) {
				Complex diag = Complex.subt(prawDolny, argLewDolny);
				Complex zWzgl = Complex.subt(z, argLewDolny);
				Point ret = new Point((int)(img.getWidth()*zWzgl.x/diag.x), (int)(img.getHeight() - img.getHeight()*zWzgl.y/diag.y));
				return ret;
			}

			@Override
			public Complex getPG() {
				return prawDolny;
			}

			@Override
			public Complex getLD() {
				return lewDolny;
			}

			@Override
			public void setPG(Complex PG) {
				prawDolny = PG;
			}

			@Override
			public void setLD(Complex LD) {
				lewDolny = LD;
			}

			@Override
			public Complex[] noweZewn(Point p, Complex lewyDolny, Complex prawyGorny, double alpha) {
				Complex z0 = pointToCmplx(p);
				Complex[] ret = new Complex[] {Complex.add(z0, Complex.mult(new Complex(alpha), Complex.subt(getLD(), z0))), 
						Complex.add(z0, Complex.mult(new Complex(alpha), Complex.subt(getPG(), z0)))};
				return ret;
			}
		};

 
	}
	
	public Coordinates aroundInf(Complex lewyDolny, Complex prawyGorny) {
		//tworzy koordynaty w których środku znajduje się nieskończoność
		return new Coordinates() {
			private Complex lewDolny = lewyDolny;
			private Complex prawGorny = prawyGorny;		
			@Override
			public Complex pointToCmplx(Point p, Complex argLewDol, Complex argPrawGorny) {
				Complex a = Complex.mult(new Complex(1.0/2,1.0/2), Complex.subt(argPrawGorny, argLewDol));
				Complex b = Complex.mult(new Complex(1.0/2), Complex.add(argLewDol, argPrawGorny));
				Complex zeta = rect(new Complex(-1,-1), new Complex(1,1)).pointToCmplx(p);
				return Complex.div(Complex.add(a, Complex.mult(b, zeta)), zeta);
			}
			
			@Override
			public Point cmplxToPoint(Complex z, Complex argLewDol, Complex argPrawGorny) {
				Complex a = Complex.mult(new Complex(1.0/2,1.0/2), Complex.subt(argPrawGorny, argLewDol));
				Complex b = Complex.mult(new Complex(1.0/2), Complex.add(argLewDol, argPrawGorny));
				Complex zeta = Complex.div(a, Complex.subt(z, b));
				return rect(new Complex(-1,-1), new Complex(1,1)).cmplxToPoint(zeta);
			}
			
			@Override
			public Complex getPG() {
				return prawGorny;
			}
			
			@Override
			public Complex getLD() {
				return lewDolny;
			}
			@Override
			public void setPG(Complex PG) {
				prawGorny = PG;
			}
			
			@Override
			public void setLD(Complex LD) {
				lewDolny = LD;
			}

			@Override
			public Complex[] noweZewn(Point p, Complex lewyDolny, Complex prawyGorny, double alpha) {
				Complex a = Complex.mult(new Complex(1.0/2,1.0/2), Complex.subt(prawyGorny, lewyDolny));
				Complex b = Complex.mult(new Complex(1.0/2), Complex.add(lewyDolny, prawyGorny));
				Complex z = pointToCmplx(p);
				Complex zeta = Complex.div(a, Complex.subt(z, b));
				Complex aNow = Complex.mult(new Complex(alpha), a);
				Complex bNow = Complex.add(Complex.div(Complex.mult(a, new Complex(1-alpha)), zeta), b);
				return new Complex[] {Complex.subt(Complex.div(aNow, new Complex(1,1)), bNow), Complex.add(Complex.div(aNow, new Complex(1,1)), bNow)};
			}

		};
	}
	
	public Coordinates  logarithmic(Complex lewyDolny, Complex prawyGorny) {
		return new Coordinates() {
			Complex lDolny = lewyDolny;
			Complex pGorny = prawyGorny;
			@Override
			public Complex pointToCmplx(Point p, Complex lewyDolny, Complex prawyGorny) {
				Complex aKsi = Complex.mult(lewyDolny, new Complex ((Math.exp(lewyDolny.mod())-1) / lewyDolny.mod()));
				Complex bKsi = Complex.mult(prawyGorny, new Complex ((Math.exp(prawyGorny.mod())-1) / prawyGorny.mod()));
				Complex ksi = rect(aKsi, bKsi).pointToCmplx(p);
				if(ksi.x == 0 && ksi.y == 0)
					return new Complex(0);
				return Complex.mult(ksi, new Complex(Math.log(ksi.mod()+1)/ksi.mod()));
			}
			
			@Override
			public Point cmplxToPoint(Complex z, Complex lewyDolny, Complex prawyGorny) {
				Complex aKsi = Complex.mult(lewyDolny, new Complex ((Math.exp(lewyDolny.mod())-1) / lewyDolny.mod()));
				Complex bKsi = Complex.mult(prawyGorny, new Complex ((Math.exp(prawyGorny.mod())-1) / prawyGorny.mod()));
				Complex ksi;
				if(z.x == 0 && z.y == 0)
					ksi = new Complex(0);
				else
					ksi = Complex.mult(z, new Complex ((Math.exp(z.mod())-1) / z.mod()));
				return rect(aKsi, bKsi).cmplxToPoint(ksi);
			}

			
			@Override
			public Complex getPG() {
				return pGorny;
			}
			
			@Override
			public Complex getLD() {
				return lDolny;
			}
			
			@Override
			public void setPG(Complex PG) {
				pGorny = PG;
			}
			
			@Override
			public void setLD(Complex LD) {
				lDolny = LD;
			}

			@Override
			public Complex[] noweZewn(Point p, Complex lewyDolny, Complex prawyGorny, double alpha) {
				// TODO Auto-generated method stub
				return null;
			}
			
		};
	}
	
	private void calculateAllVal(FunctionPowloka f, Point lewyDolny, Point prawyGorny, boolean[] alrCalculatedSides) {
		//jest w stanie obliczyć jeśli różnica pr.gornego i lewego dolnego jest niewygodna względem DELTA, al preferowane jest aby tak nie było
		//dol i gora jako odpowiednio mniejsze i większe y: na końcowym obrazie jest odwrotnie
		//nie jest maksymalnie zoptymalizowane, ale powinno być okej
		int xD = (lewyDolny.x / DELTA) * DELTA;
		int xG = (prawyGorny.x / DELTA) * DELTA;
		int yD = (lewyDolny.y / DELTA) * DELTA;
		int yG = (prawyGorny.y / DELTA) * DELTA;
		xD = alrCalculatedSides[0] ? xD + DELTA : xD;
		xG = alrCalculatedSides[2] ? xG - DELTA : xG;
		yD = alrCalculatedSides[1] ? yD + DELTA : yD;
		yG = alrCalculatedSides[2] ? yG - DELTA : yG;
		System.out.println(xD +", " + xG +", " + yD + ", " +yG );
		System.out.println(lewyDolny + ", " + prawyGorny);
		System.out.println(alrCalculatedSides[0] + ", " + alrCalculatedSides[1]+ ", "+alrCalculatedSides[2]+ ", "+alrCalculatedSides[3]);
		for(int xI = xD; xI <= xG; xI += DELTA) {
			for(int yI = yD; yI <= yG; yI += DELTA) {
				try {
					values[xI][yI] = f.evaluate(new Complex[] {coords.pointToCmplx(new Point(xI,yI))});
				} catch (FunctionExpectedException e) {
					values[xI][yI] = Complex.NaN;
				}
			}
		}
		int a = 0;
		//liczy w niektórych punktach podwójnie
		for(int xI = lewyDolny.x;xI<prawyGorny.x;xI++) {
			for(int yI=lewyDolny.y;yI<prawyGorny.y;yI++) {
				int xDok = xI - xI%DELTA;
				int yDok = yI - yI%DELTA;
				try {
					values[xI][yI] = Complex.div(Complex.add(new Complex[] {
						Complex.mult(values[xDok][yDok], taxMetric(xI,yI,xDok,yDok)),
						Complex.mult(values[xDok+DELTA][yDok], taxMetric(xI,yI,xDok+DELTA,yDok)),
						Complex.mult(values[xDok][yDok+DELTA], taxMetric(xI,yI,xDok,yDok+DELTA)),
						Complex.mult(values[xDok+DELTA][yDok+DELTA], taxMetric(xI,yI,xDok+DELTA,yDok+DELTA)) 
					}), new Complex(4*DELTA));
				}catch (NullPointerException e) {
					if(a++<1)
						System.out.println(xI + ", " + yI + ", " + xDok + ", " + yDok + ", (null)" + ", " + values[xDok][yDok]+ ", " +values[xDok+DELTA][yDok] + ", " +values[xDok][yDok+DELTA] + ", " + values[xDok+DELTA][yDok+DELTA]);
				}
			}
		}
	}
	
	private int taxMetric(int x1, int y1, int x2, int y2) {
		return Math.abs(x1-x2) + Math.abs(y1-y2);
	}
	
	//raczej okropnie zrobione...
	LinkedList<Bool<Integer>> instancesOfChange = new LinkedList<Bool<Integer>>();
	private synchronized int addCorrectly(){
		instancesOfChange.add(new Bool<Integer>(instancesOfChange.size(), true));
		return instancesOfChange.size()-1;
	}
	public synchronized void stopAllOngoingChangeMethods() {
		instancesOfChange = new LinkedList<Bool<Integer>>();
	}
	private synchronized boolean checkIfThisChangeISStillActive(int i) {
		for(Bool<Integer> inst : instancesOfChange) {
			if(inst.f.equals(i)) {
				if(!inst.bool)
					return false;
				else
					return true;
			}
		}
		return false;
	}
	public void change(FunctionPowloka f, Coordinates coords ,CmplxToColor colorMap, double...parameters) {
		int thisInstanceOfChange = addCorrectly();
		this.function = f;
		this.coords = coords;
		this.colorMap = colorMap;
		this.colorMapParams = parameters;
		Complex[] z = new Complex[1];
		int X = img.getWidth() / PODZIALRYSOWANIA;
		int Y = img.getHeight() / PODZIALRYSOWANIA;
		int xPI;
		int yPI;
		for(int n=0; n<PODZIALRYSOWANIA*2-1;n++) {
			boolean przekrPrz = (n > (PODZIALRYSOWANIA-1));
			for(int i = 0;i < (przekrPrz ? 2*PODZIALRYSOWANIA-n-1 : n+1); i++) {
				xPI = (przekrPrz ? n+1 - PODZIALRYSOWANIA : 0)+i;
				yPI = (przekrPrz ? 0 : PODZIALRYSOWANIA - (n+1))+i;
				if(true)
					for(int xI=X*xPI;xI<X*(xPI+1);xI++) {
						for(int yI = Y*yPI;yI<Y*(yPI+1);yI++) {
							if(!checkIfThisChangeISStillActive(thisInstanceOfChange)) {
								return;
							}
							z[0] = coords.pointToCmplx(new Point(xI,yI));
							try {
								values[xI][yI] = f.evaluate(z);
							} catch (FunctionExpectedException e) {
								values[xI][yI] = null;
							}
						}
					}
				else {
					boolean[] juzObl;
					juzObl = new boolean[] {xPI != 0, false, false, yPI != PODZIALRYSOWANIA - 1}; 
					calculateAllVal(f, new Point(X*xPI, Y*yPI),  new Point(X*(xPI+1), Y*(yPI+1)), juzObl);
				}
				//setColor(this.colorMap, new  Point(X*xPI, Y*yPI), new Point(X*(xPI+1), Y*(yPI+1)), parameters);
			}
		}
		/*
		for(int xI=0; xI<img.getWidth();xI++) {
			for(int yI=0;yI<img.getHeight();yI++) {
				if(!checkIfThisChangeISStillActive(thisInstanceOfChange)) {
					temp.get(thisNum).setForeground(Color.black);
					temp.get(thisNum).setText("---");
					return;
				}
				if(xI >= X*PODZIALRYSOWANIA || yI >= Y*PODZIALRYSOWANIA) {
					z[0] = coords.pointToCmplx(new Point(xI,yI));
					try {
						values[xI][yI] = f.evaluate(z);
					} catch (FunctionExpectedException e) {
						values[xI][yI] = null;
					}
				}
			}
		}*/
		setColor(this.colorMap, parameters);
	}
	
	public void change() {
		change(function, coords, colorMap, colorMapParams);
	}
	
	private void setColor(CmplxToColor colorMap, Point LDBound, Point PGBound, double... parameters) {
		//SwingWorker<Void,Void> draw = new SwingWorker<Void, Void>() {
		//	@Override
		//	protected Void doInBackground() throws Exception {
				Integer color;
				try {
					for(int xI=LDBound.x; xI<PGBound.x;xI+=DELTA) {
						for(int yI=LDBound.y;yI<PGBound.y;yI+=DELTA) {
							color = colorMap.colorOf(values[xI][yI], parameters);
							if(color == null) {
								imgSmaller.setRGB(xI/DELTA, yI/DELTA, rgbToHex((xI+yI)%40<20 ? Color.MAGENTA : new Color(230,230,230)));
								continue;
							}
							img.setRGB(xI/DELTA, yI/DELTA, color);
						}
					}
					Graphics2D g2D = img.createGraphics();
					
					g2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
					g2D.drawImage(imgSmaller.getScaledInstance(img.getWidth()*2, img.getHeight(), 0), 0, 0, img.getWidth(), img.getHeight(), null);
					g2D.dispose();
		//			return null;
				} catch (Exception e) {
					e.printStackTrace();
		//			return null;
				}
		//	}
			
		//	@Override
		//	protected void done() {
				repaint();
		//	}
		//};
		//draw.execute();

	}
	
	public void setColor(CmplxToColor colorMap, double... parameters) {
		setColor(colorMap, new Point(0,0), new Point(img.getWidth(), img.getHeight()), parameters);
	}
	
	static int[] HSLToRGB(double[] HSL) {
		if(HSL == null) {
			return null;
		}
		if(HSL[2]==1) return new int[] {255,255,255, (int)HSL[3]};
		if(HSL[2]==0) return new int[] {0,0,0, (int)HSL[3]};
		double[] nrgb = new double[3];
		//normH w 0, 6
		double normH = 3.0/Math.PI*HSL[0]+1;
		int iM = (int) Math.floor(normH/2);
		int[] order = {1,2,2,0,0,1};
		int ind = (int) Math.floor(normH);
		int im;
		if(ind  >= 6.0 && ind < 6.0+1e-10) {
			ind = 0;
			iM = 0;
		}
		im = order[ind];
		int isr =  (3-im-iM)%3;
		nrgb[iM] = HSL[2] + HSL[1]*(1-Math.abs(2*HSL[2]-1))/2;
		nrgb[im] = HSL[2] - HSL[1]*(1-Math.abs(2*HSL[2]-1))/2;
		nrgb[isr] = nrgb[im] + Math.pow(-1, Math.floor(normH)+1)*(normH-1-Math.floor(normH/2)*2)*(nrgb[iM]-nrgb[im]);
		//System.out.println(im);
		//System.out.println(iM);
		//System.out.println(isr);
		
		int[] rgb = {(int) (nrgb[0]*255),(int) (nrgb[1]*255),(int) (nrgb[2]*255), (int)HSL[3]};
		return rgb;
	}
	
	static int rgbToHex(int [] rgb) {
		return (rgb[3] << 24) | (rgb[2] << 16) | (rgb[1] << 8) | rgb[0];
	}
	
	static int rgbToHex(Color color) {
		return (color.getAlpha() << 24) | (color.getRed() << 16) | (color.getGreen() << 8) | color.getBlue();
	}
	
	public Complex integralOfCurve() {
		final int div = 100;
		Complex ret = new Complex(0);
		for(LinkedList<Complex> i: foreGround.krzywa) {
			Complex z0 = i.getFirst();
			for(Complex z : i) {
				for(int j = 0;j<div;j++) {
					Complex val = getValueAt(coords.cmplxToPoint( Complex.add(z0, Complex.mult(Complex.subt(z, z0), new Complex(((double)j)/div)) )));
					Complex delta = Complex.mult(Complex.subt(z, z0), new Complex(1.0/div));
					if(! (val == null))
						ret.add(Complex.mult(delta,val ));
					}
				z0 = z;
			}
		}
		return ret;
	}
	
	class Foreground extends JPanel{
		/**
		 * 
		 */
		private static final long serialVersionUID = 6226797058189817506L;
		public Color szyba = new Color(0,0,0,0);
		Complex marker;
		Complex[] rect;
		private static final int MARKERWIDTH = 12;
		LinkedList<LinkedList<Complex>> krzywa = new LinkedList<LinkedList<Complex>>();
		boolean osie = false;
		final static int LICZBAOSI = 5; 
		
		public Foreground() {
			setOpaque(false);
		}
		
		public void resetCurve(){
			krzywa = new LinkedList<LinkedList<Complex>>();
		}
		
		public void addNewCurve() {
			krzywa.add(null);
		}
		
		public void addPointToCurve(Complex z) {
			if(krzywa.getLast() == null) {
				krzywa.set(krzywa.size()-1,new LinkedList<Complex>());
			}
			Point wsp = coords.cmplxToPoint(z);
			if(krzywa.getLast().size() > 1 && coords.cmplxToPoint(krzywa.getLast().getLast()).distanceSq(wsp.getX(), wsp.getY()) <  getWidth() *getHeight() / 100 / 100 / 10) {
				//System.out.println("tyle Mniej: " + tyleMniej++);
				return;
			}
			
			//System.out.println("tyle: " + tyle++);
			krzywa.getLast().add(z);
		}
				
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2D = (Graphics2D)g;
			g2D.setColor(szyba);
			g2D.fillRect(0, 0, getWidth(), getHeight());
			g.setFont(new Font(getFont().getName(), getFont().getStyle(), getFont().getSize() - 6 + (getWidth()+getHeight())/200 ));
			
			if(marker != null && !Double.isNaN(marker.x) && !Double.isNaN(marker.y)) {
				Point wsp = coords.cmplxToPoint(marker);
				g2D.setColor(Color.red);
				g2D.fillOval(wsp.x - MARKERWIDTH/2, wsp.y-MARKERWIDTH/2,MARKERWIDTH , MARKERWIDTH);
				g2D.setColor(Color.black);
				g2D.setStroke(new BasicStroke(2));
				g2D.drawOval(wsp.x - MARKERWIDTH/2, wsp.y-MARKERWIDTH/2,MARKERWIDTH , MARKERWIDTH);
			}
			
			for(LinkedList<Complex> podKrzywa : krzywa) {
				if(podKrzywa != null) {
					Point p = coords.cmplxToPoint( podKrzywa.get(0) );
					Point pLast = p;
					Point pLast2 = pLast;
					Point pLast3 = pLast2;
					for(int i=0;i<podKrzywa.size()+2;i++) {
						if(i < podKrzywa.size()) {
							p = coords.cmplxToPoint( podKrzywa.get(i) );
						}
						g2D.setColor(new Color(240,240,240,15));
						g2D.setStroke(new BasicStroke(6));
						g2D.drawLine(pLast.x, pLast.y, p.x, p.y);
						
						
						g2D.setColor(new Color(190,190,190,50));
						g2D.setStroke(new BasicStroke(4));
						g2D.drawLine(pLast2.x, pLast2.y, pLast.x, pLast.y);
						
						g2D.setColor(new Color(50,50,50, 240));
						g2D.setStroke(new BasicStroke(2));
						g2D.drawLine(pLast3.x, pLast3.y, pLast2.x, pLast2.y);
						
						pLast3 = pLast2;
						pLast2 = pLast;
						pLast = p;
					}
				}
			}
			
			if(rect != null) {
				g2D.setColor(Color.BLACK);
				g2D.setStroke(new BasicStroke(2));
				Point[] rectP = new Point[] {coords.cmplxToPoint(rect[0]), coords.cmplxToPoint(rect[1])};
				/*g2D.drawLine(rectP[0].x,rectP[0].y, rectP[0].x, rectP[1].y);
				g2D.drawLine(rectP[0].x, rectP[1].y, rectP[1].x, rectP[1].y);
				g2D.drawLine(rectP[1].x, rectP[1].y, rectP[1].x, rectP[0].y);
				g2D.drawLine(rectP[1].x, rectP[0].y, rectP[0].x, rectP[0].y);*/
				drawWillBeLine(g2D, rect[0], new Complex(rect[0].x, rect[1].y), 100);
				drawWillBeLine(g2D, rect[0], new Complex(rect[1].x, rect[0].y), 100);
				drawWillBeLine(g2D, rect[1], new Complex(rect[1].x, rect[0].y), 100);
				drawWillBeLine(g2D, rect[1], new Complex(rect[0].x, rect[1].y), 100);
			}
			
			if(osie) {
				g2D.setColor(Color.BLACK);
				for(int i=0;i<LICZBAOSI+1;i++) {
					g2D.setColor(Color.BLACK);
					g2D.setStroke(new BasicStroke((getWidth() + getHeight())/2 < 400 ? 1 : (float)1.5));
					//g2D.setStroke(new BasicStroke((float)1));
					int x = getWidth()*i/(LICZBAOSI+1);
					int y = getHeight()*i/(LICZBAOSI+1);
					g2D.drawLine(0, y, getWidth(), y);
					g2D.drawLine(x, 0, x, getHeight());
				}
				for(int i=1;i<LICZBAOSI+1;i+=2) {
					int x = getWidth()*i/(LICZBAOSI+1);
					int y = getHeight()*i/(LICZBAOSI+1);
					int strx, stry;
					String str;
					str = coords.pointToCmplx(new Point(x, getHeight()/2)).printE(3,2);
					strx = x+4;
					stry = getHeight()/2 - 5;
					drawStringWthHighlight(g2D, str, strx, stry);
					str = coords.pointToCmplx(new Point(getWidth() / 2, y)).printE(3, 2);//Complex.toStr(coords.pointToCmplx(new Point(getWidth() / 2, y)).y, 2, 2);
					strx = getWidth()/2+5;
					stry = y-5;
					if(!(y==getHeight()/2))
						drawStringWthHighlight(g2D, str, strx, stry);

				}
			}

		}

		private void drawWillBeLine(Graphics2D g2D, Complex begginig, Complex end, int dok) {
			//rysuje coś co po zmienieniu granic wykresu przez przeciągnięcie będzie odcinkiem
			Complex[] krzyw = new Complex[dok+1];
			Point beggAf = coords.cmplxToPoint(begginig, rect[0], rect[1]);
			Point endAf = coords.cmplxToPoint(end, rect[0], rect[1]);
			for(int i = 0;i<dok+1;i++) {
				int onLinex = (int)(beggAf.x + ((double)i)/dok * (endAf.x - beggAf.x));
				int onLiney = (int)(beggAf.y + ((double)i)/dok * (endAf.y - beggAf.y));
				krzyw[i] = coords.pointToCmplx(new Point(onLinex, onLiney), rect[0], rect[1]);
			}
			Point pLast = coords.cmplxToPoint( krzyw[0] );
			for(Complex z : krzyw) {
				Point p = coords.cmplxToPoint(z);
				g2D.drawLine(pLast.x, pLast.y, p.x, p.y);
				pLast = p;
			}
		}
		
		private void drawStringWthHighlight(Graphics2D g, String txt, int x, int y) {
			FontMetrics fm = g.getFontMetrics();
			int width = fm.stringWidth(txt);
			int height = fm.getAscent();
			g.setColor(new Color(255,255,255,200));
			g.setFont(new Font(g.getFont().getName(), Font.BOLD, g.getFont().getSize()));
			g.fillRect(x-1, y-height+1, width+2, height+1);
			g.setColor(new Color(50,0,0));
			g.drawString(txt, x, y);
		}
	}

	interface CmplxToColor{
		Integer colorOf(Complex z, double... parameters);
		double[] defaultParams();
		String[] paramsNames();
		String name();
	}
	 
	interface Coordinates{
		Complex pointToCmplx(Point p, Complex lewyDolny, Complex prawyGorny);
		Point cmplxToPoint(Complex z, Complex lewyDolny, Complex prawyGorny);
		//wykorzystywane przy powiększaniu / pomniejszaniu
		Complex[] noweZewn(Point p, Complex lewyDolny, Complex prawyGorny, double alpha);
		Complex getPG();
		Complex getLD();
		void setPG(Complex PG);
		void setLD(Complex LD);
		default Complex pointToCmplx(Point p) {
			return pointToCmplx(p, getLD(), getPG());
		};
		default Point cmplxToPoint(Complex z) {
			return cmplxToPoint(z, getLD(), getPG());
		};
		default void noweZewn(Point p, double alpha) {
			Complex[] noweRogi = noweZewn(p, getLD(), getPG(), alpha);
			setLD(noweRogi[0]);
			setPG(noweRogi[1]);
		}
	}
	}
