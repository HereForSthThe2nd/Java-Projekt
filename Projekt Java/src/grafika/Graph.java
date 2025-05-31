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
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.IOException;
import java.text.AttributedCharacterIterator;
import java.util.LinkedList;
import java.util.Random;
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

import Inne.Complex;
import funkcja.*;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import java.awt.image.BufferedImage;

public class Graph extends JPanel{
	BufferedImage img;
	private Complex[][] values; //może trochę overkill, zajmuje z 5 razy więcej miejsca niż obraz, może np. zrobić by co 3 - 10 ^ 2 pikseli obliczało wartości po funkcji (i je tylko wtedy zapisywało) a reszę jakoś interpolowało
	Coordinates coords;
	FunctionPowloka function;
	CmplxToColor colorMap;
	double[] colorMapParams;
	JPanel obraz;
	Foreground foreGround;
	static CmplxToColor basic;
	static {
		basic = new CmplxToColor() {
			
			private static double przedNorm(double r) {
				if(r<-0.00001) {
					throw new IllegalArgumentException("r musi być nieuemne. podane r: " + r);
				}
				r = r>0 ? r : 0;
				//return 2/Math.PI * (Math.atan(r));
				return 2/Math.PI * (Math.atan(Math.log(r+1)));
			}
			
			private static double[] pointToHSL(Complex z, double lSpeedChange) {
				if(z == null || Double.isNaN(z.x) || Double.isNaN(z.y)) {
					return null;
				}
				double[] HSL = new double[4];
				HSL[3] = 255;
				HSL[0] = z.arg() + 2.0/3*Math.PI;
				HSL[1] = 1;
				//HSL[2] = 2/Math.PI*Math.atan( Math.log(Math.pow(z.mod(), 1/lSpeedChange)+1) );
				HSL[2] = przedNorm( Math.pow(z.mod(), lSpeedChange ));
				if(HSL[2]>1 || HSL[2]<0) {
				}
				return HSL;
			}
			
			@Override
			public Integer colorOf(Complex z, double... parameters) {
				if(parameters.length != 1) {
					throw new IllegalArgumentException("Ta funkcja przyjmuje tylko jeden parametr.");
				}
				int[] rgb = HSLToRGB(pointToHSL(z, parameters[0]));
				if(rgb == null) {
					return null;
				}
				return rgbToHex(rgb);
			}
		};
	}
	
	public Complex getValueAt(int x, int y) {
		//x oraz y to współrzędne piksela
		return values[x][y];
	}
	
	public Complex getValueAt(Point p) {
		return values[p.x][p.y];
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
	
	public Graph(int bok) {
		img = new BufferedImage(bok,bok,BufferedImage.TYPE_INT_ARGB);
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
		};

 
	}
	
	public Coordinates aroundInf(Complex lewyDolny, Complex prawyGorny) {
		//tworzy koordynaty w których środku znajduje się nieskończoność
		return new Coordinates() {
			private Complex lewDolny = lewyDolny;
			private Complex prawGorny = prawyGorny;		
			@Override
			public Complex pointToCmplx(Point p, Complex argLewDol, Complex argPrawGorny) {
				Complex arga = Complex.mult(new Complex(1.0/2,1.0/2), Complex.subt(argLewDol, argPrawGorny));
				Complex argb = Complex.mult(new Complex(1.0/2), Complex.add(argLewDol, argPrawGorny));
				Complex zeta = rect(new Complex(-1,-1), new Complex(1,1)).pointToCmplx(p);
				return Complex.div(Complex.add(arga, Complex.mult(argb, zeta)), zeta);
			}
			
			@Override
			public Point cmplxToPoint(Complex z, Complex argLewDol, Complex argPrawGorny) {
				Complex arga = Complex.mult(new Complex(1.0/2,1.0/2), Complex.subt(argLewDol, argPrawGorny));
				Complex argb = Complex.mult(new Complex(1.0/2), Complex.add(argLewDol, argPrawGorny));
				Complex zeta = Complex.div(arga, Complex.subt(z, argb));
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
			
		};
	}
	
	public void change(FunctionPowloka f, Coordinates coords ,CmplxToColor colorMap, double...parameters) {
		this.function = f;
		this.coords = coords;
		this.colorMap = colorMap;
		this.colorMapParams = parameters;
		values = new Complex[img.getWidth()][img.getHeight()];
		for(int xI=0; xI<img.getWidth();xI++) {
			for(int yI=0;yI<img.getHeight();yI++) {
				Complex z = coords.pointToCmplx(new Point(xI,yI));
				values[xI][yI] = f.evaluate(new Complex[] {z});
			}
		}
		setColor(basic, parameters);
	}
	
	public void change() {
		change(function, coords, colorMap, colorMapParams);
	}
	
	private void setColor(CmplxToColor colorMap, double... parameters) {
		SwingWorker<Void,Void> draw = new SwingWorker<Void, Void>() {
			boolean finish = false;
			@Override
			protected Void doInBackground() throws Exception {

				try {
					for(int xI=0; xI<img.getWidth();xI++) {
						if(!finish)
							for(int yI=0;yI<img.getHeight();yI++) {
								Integer color = colorMap.colorOf(values[xI][yI], parameters);
								if(color == null) {
									img.setRGB(xI, yI, rgbToHex((xI+yI)%40<20 ? Color.MAGENTA : new Color(230,230,230)));
									continue;
								}
								img.setRGB(xI, yI, color);
							}
					}
					return null;
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			}
			
			@Override
			protected void done() {
				repaint();
			}
		};
		draw.execute();

	}
	
	static int[] HSLToRGB(double[] HSL) {
		if(HSL == null) {
			return null;
		}
		if(HSL[2]==1) return new int[] {255,255,255, (int)HSL[3]};
		if(HSL[2]==0) return new int[] {0,0,0, (int)HSL[3]};
		double[] nrgb = new double[3];
		double normH = 3.0/Math.PI*HSL[0]+1;
		int iM = (int) Math.floor(normH/2);
		int[] order = {1,2,2,0,0,1};
		int im = order[ (int) Math.floor(normH) ];
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
		return (color.getAlpha() << 24) | (color.getBlue() << 16) | (color.getGreen() << 8) | color.getRed();
	}

	class Foreground extends JPanel{
		public Color szyba = new Color(0,0,0,0);
		Complex marker;
		Complex[] rect;
		/*--TODO: USUNĄĆ--*/
		static int tyleMniej;
		static int tyle;
		/*----------------*/
		private static final int MARKERWIDTH = 12;
		LinkedList<LinkedList<Complex>> krzywa = new LinkedList<LinkedList<Complex>>();
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
			if(krzywa.getLast().size() > 1 && coords.cmplxToPoint(krzywa.getLast().getLast()).distanceSq(wsp.getX(), wsp.getY()) <  getWidth() *getHeight() / 100 / 100 / 5) {
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
	}

	interface CmplxToColor{
		Integer colorOf(Complex z, double... parameters);
	}
	 
	interface Coordinates{
		Complex pointToCmplx(Point p, Complex lewyDolny, Complex prawyGorny);
		Point cmplxToPoint(Complex z, Complex lewyDolny, Complex prawyGorny);
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
	}
}
