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
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.text.AttributedCharacterIterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.TimerTask;
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

import funkcja.*;
import inne.Bool;
import inne.Complex;
import inne.ComplexCurve;
import inne.ComplexPolyCurve;
import inne.PolyCurve;
import inne.Runn;

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
	
	CentralLayout layout;
	
	private static final long serialVersionUID = 8436283896706339087L;
	//w zmianie funkcji na ile bloków dzieli graf (w poziomie * w pionie)
	int PODZIALRYSOWANIA = 1;
	BufferedImage img;
	private Values values;
	
	//private Complex[][] valBuffer;
	
	FunctionPowloka function;
	CmplxToColor colorMap;
	double[] colorMapParams;
	CoordPanel obraz;
	Foreground foreGround;
	static CmplxToColor basic;
	private static CmplxToColor noArg;
	private static CmplxToColor halfPlane;
	private static CmplxToColor circle;
	private static CmplxToColor poziomice;
	public static CmplxToColor[] listaKolorowan;
	
	InstanceManager<Void> changeM = new InstanceManager<Void>(50);
	InstanceManager<Void> scM = new InstanceManager<Void>(10);
	
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
		layout = new CentralLayout(this);
		setLayout(layout);
		img = new BufferedImage(bok,bok,BufferedImage.TYPE_INT_ARGB);
		//imgSmaller = new BufferedImage(bok/DELTA, bok/DELTA, BufferedImage.TYPE_INT_ARGB);
		obraz = new CoordPanel() {
			private static final long serialVersionUID = 1L;
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2D = (Graphics2D)g;
				//g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				//Complex diagF = Complex.subt(prawyGornyImg, lewyDolnyImg);
				//Complex diagRz = Complex.subt(coords.getPG(), coords.getLD());
				//Image res = img.getScaledInstance((int)(img.getWidth() * (diagF.x / diagRz.x)), (int)(img.getHeight() * (diagF.x / diagRz.x)), Image.SCALE_SMOOTH);
				//g2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
				//g2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
				//g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				//Point bounds = coords.cmplxToPoint(new Complex(lewyDolnyImg.x, prawyGornyImg.y));
				g2D.drawImage(img, 0, 0, null);
			}
		};
		obraz.setSize(bok, bok);
		obraz.setPreferredSize(new Dimension(bok,bok));
		foreGround = new Foreground();
		foreGround.setSize(bok, bok);
		foreGround.setPreferredSize(new Dimension(bok, bok));
		layout.add(foreGround);
		layout.add(obraz);
		Border obrBorder = BorderFactory.createLineBorder(Color.black, 3);
		foreGround.setBorder(obrBorder);
		setPreferredSize(new Dimension(img.getWidth()+10, img.getHeight()+10));
		setSize(new Dimension(img.getWidth()+10, img.getHeight()+10));
		values = new Values(bok/2, obraz);
		
		changeM.setRunnable(new Runn<Void>() {
			
			@Override
			public Void run(int key) {
				change(key);
				return null;
			}
		});
	}

	public Graph(int bok, FunctionPowloka f, Coordinates coords ,CmplxToColor colorMap, double...parameters) {
		this(bok);
		change(-1, f, coords, colorMap, parameters);
	}
	
	public Coordinates rect(Complex lewyDolny, Complex prawyGorny) {
		return CoordsFactory.rect(lewyDolny, prawyGorny, img.getWidth(), img.getHeight());
	}
	
	public Coordinates aroundInf(Complex lewyDolny, Complex prawyGorny) {
		return CoordsFactory.aroundInf(lewyDolny, prawyGorny, img.getWidth(), img.getHeight());
	}
	
	public Coordinates logarithmic(Complex lewyDolny, Complex prawyGorny) {
		return CoordsFactory.logarithmic(lewyDolny, prawyGorny, img.getWidth(), img.getHeight());
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
				if(z == null ||Double.isNaN(z.x) || Double.isNaN(z.y)) {
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
	
	public Complex getValueAt(Point p) {
		return values.getValueAt(p);
	}
	
	public Complex getValueAt(int x, int y) {
		//x oraz y to współrzędne piksela
		return values.getValueAt(x, y);
	}
	
	private Complex getValueAt(int xI, int yI, Coordinates oldCoords) {
		return values.getValueAt(xI, yI, oldCoords);
	}
	
	public void save(File imgfile) throws IOException {
		ImageIO.write(img, "png", imgfile);
	}
	
	private void change(int key, FunctionPowloka f, Coordinates coords ,CmplxToColor colorMap, double...parameters) {
		this.function = f;
		this.obraz.coords = coords;
		values.setLewyDolnyPanel(coords.pointToCmplx( new Point(0,img.getHeight()) ));
		values.setPrawyGornyPanel(coords.pointToCmplx( new Point(img.getWidth(),0) ));
		this.colorMap = colorMap;
		this.colorMapParams = parameters;
		Complex[] z = new Complex[1];
				
		int X = values.getRES()/ PODZIALRYSOWANIA;
		int Y = values.getRES() / PODZIALRYSOWANIA;
		int xPI;
		int yPI;
		for(int n=0; n<PODZIALRYSOWANIA*2-1;n++) {
			boolean przekrPrz = (n > (PODZIALRYSOWANIA-1));
			for(int i = 0;i < (przekrPrz ? 2*PODZIALRYSOWANIA-n-1 : n+1); i++) {
				if(!changeM.stillActive(key)) {
					return;
				}
				xPI = (przekrPrz ? n+1 - PODZIALRYSOWANIA : 0)+i;
				yPI = (przekrPrz ? 0 : PODZIALRYSOWANIA - (n+1))+i;
				for(int xI=X*xPI;xI<X*(xPI+1);xI++) {
					for(int yI = Y*yPI;yI<Y*(yPI+1);yI++) {
						Point point = new Point(img.getWidth()*xI/values.getRES(), img.getHeight()*yI/values.getRES());
						z[0] = coords.pointToCmplx(point);
						try {
							values.setValue(xI, yI, f.evaluate(z));
						} catch (FunctionExpectedException e) {
							values.setValue(xI, yI, null);
						}
					}
				}
				setColor(-1, this.colorMap, new Point(img.getWidth() / values.getRES() *X*xPI, img.getHeight() / values.getRES() *Y*yPI), new Point(img.getWidth() / values.getRES() * X*(xPI+1), img.getHeight() / values.getRES() * Y*(yPI+1)), parameters);
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						repaint();
					}
				});
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
		//setColor(this.colorMap, parameters);
	}
	
	private void setColor(int key, CmplxToColor colorMap, Point LDBound, Point PGBound, double... parameters) {

		Integer color;
		BufferedImage imgBuffer = new BufferedImage(PGBound.x - LDBound.x + 1, PGBound.y - LDBound.y + 1, img.getType());
		
		Coordinates oldCoords = obraz.coords.clone();
		
		try {
			for(int xI=LDBound.x; xI<=PGBound.x && xI < img.getWidth();xI+=1) {
				if(!scM.stillActive(key))
					return;
				for(int yI=LDBound.y;yI<=PGBound.y && yI < img.getHeight();yI+=1) {
					color = colorMap.colorOf(getValueAt(xI, yI, oldCoords), parameters);
					if(color == null) {
						imgBuffer.setRGB(xI - LDBound.x, yI - LDBound.y, rgbToHex((xI+yI)%40<20 ? Color.MAGENTA : new Color(230,230,230)));
						continue;
					}
					imgBuffer.setRGB(xI - LDBound.x, yI - LDBound.y, color);
				}
			}
	
		} catch (Exception e) {
			e.printStackTrace();
		}
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				Graphics2D g2d = img.createGraphics();
				//g2d.clearRect(0, 0, img.getWidth(), img.getHeight());
				g2d.drawImage(imgBuffer, LDBound.x,LDBound.y, null);
				g2d.dispose();
			}
		});

	}
	


	void setColor(int key) {
		setColor(key, colorMap, new Point(0,0), new Point(img.getWidth(), img.getHeight()), colorMapParams);
	}
	
	private void change(int key) {
		change(key, function, obraz.coords, colorMap, colorMapParams);
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
		for(ComplexPolyCurve i: foreGround.krzywe) {
			Complex z0 = i.getVert().getFirst();
			for(Complex z : i.getVert()) {
				Complex delta = Complex.mult(Complex.subt(z, z0), new Complex(1.0/div));
				for(int j = 0;j<div;j++) {
					Complex val = getValueAt(obraz.coords.cmplxToPoint( Complex.add(z0, Complex.mult(Complex.subt(z, z0), new Complex(((double)j)/div)) )));
					if(! (val == null))
						ret.add(Complex.mult(delta,val ));
					}
				z0 = z;
			}
		}
		return ret;
	}
	
	public void setRES(int r) {
		values = new Values(r, obraz);
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
		LinkedList<ComplexPolyCurve> krzywe = new LinkedList<ComplexPolyCurve>();
		boolean osie = false;
		final static int LICZBAOSI = 5; 
		
		public Foreground() {
			setOpaque(false);
		}
		
		public void resetCurve(){
			krzywe = new LinkedList<ComplexPolyCurve>();
		}
		
		public void addNewCurve(ComplexPolyCurve curve) {
			krzywe.add(curve);
		}
		
		
		public void addNewCurve() {
			addNewCurve(null);
		}

		public void addPointToCurve(Complex z) {
			if(krzywe.getLast() == null) {
				krzywe.set(krzywe.size()-1,new ComplexPolyCurve());
			}
			krzywe.getLast().addPoint(z);
		}
				
		
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			
			Graphics2D g2D = (Graphics2D)g;
			g2D.setColor(szyba);
			g2D.fillRect(0, 0, getWidth(), getHeight());
			g.setFont(new Font(getFont().getName(), getFont().getStyle(), getFont().getSize() - 6 + (getWidth()+getHeight())/200 ));
			
			
			for(ComplexPolyCurve curve : krzywe) {
				if(curve != null) {
					g2D.setStroke(new BasicStroke(2));
					g2D.setColor(Color.black);
					curve.drawWthOutline(g2D, obraz, osie);
				}
			}
			
			if(rect != null) {
				drawZoomInHelper(g2D);
			}
			
			if(marker != null && !Double.isNaN(marker.x) && !Double.isNaN(marker.y)) {
				drawMarker(g2D);
			}
			
			if(osie) {
				drawGrid(g2D);
			}

		}

		private void drawMarker(Graphics2D g2D) {
			Point wsp = obraz.coords.cmplxToPoint(marker);
			g2D.setColor(Color.red);
			g2D.fillOval(wsp.x - MARKERWIDTH/2, wsp.y-MARKERWIDTH/2,MARKERWIDTH , MARKERWIDTH);
			g2D.setColor(Color.black);
			g2D.setStroke(new BasicStroke(2));
			g2D.drawOval(wsp.x - MARKERWIDTH/2, wsp.y-MARKERWIDTH/2,MARKERWIDTH , MARKERWIDTH);
		}
		
		private void drawZoomInHelper(Graphics2D g2D) {
			g2D.setColor(Color.BLACK);
			g2D.setStroke(new BasicStroke((float) 1.5));
			//g2D.setStroke(new BasicStroke(2));
			PolyCurve<Point> obRect = new PolyCurve<Point>(new LinkedList<Point>(List.of(
					new Point(0,0),
					new Point(0, img.getHeight()),
					new Point(img.getWidth(),img.getHeight()),
					new Point(img.getWidth(), 0),
					new Point(0, 0)
					)));
			ComplexPolyCurve ob = new ComplexPolyCurve ( ComplexCurve.create(obraz.coords, rect[0], rect[1], 
					obRect).toPoly(obraz.coords.accOfSmallerAr()) );
			//g2D.setStroke(new BasicStroke(2));
			ob.drawWthOutline(g2D, obraz, osie);
		}
		
		private String temp(Color r) {
			if(r.equals(Color.red))
				return "\033[0;31m";
			if(r.equals(Color.yellow))
				return "\033[0;33m";
			if(r.equals(Color.black))
				return "\033[0;30m";
			if(r.equals(Color.blue))
				return "\033[0;34m";
			return "\033[0m";
		}
		
		private void drawGrid(Graphics2D g2D) {
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
				str = obraz.coords.pointToCmplx(new Point(x, getHeight()/2)).printE(3,2);
				strx = x+4;
				stry = getHeight()/2 - 5;
				drawStringWthHighlight(g2D, str, strx, stry);
				str = obraz.coords.pointToCmplx(new Point(getWidth() / 2, y)).printE(3, 2);//Complex.toStr(coords.pointToCmplx(new Point(getWidth() / 2, y)).y, 2, 2);
				strx = getWidth()/2+5;
				stry = y-5;
				if(!(y==getHeight()/2))
					drawStringWthHighlight(g2D, str, strx, stry);

			}
		}
		
		private void drawWillBeLine(Graphics2D g2D, Complex begginig, Complex end, int dok) {
			//rysuje coś co po zmienieniu granic wykresu przez przeciągnięcie będzie odcinkiem
			Complex[] krzyw = new Complex[dok+1];
			Point beggAf = obraz.coords.cmplxToPoint(begginig, rect[0], rect[1]);
			Point endAf = obraz.coords.cmplxToPoint(end, rect[0], rect[1]);
			//System.out.println(temp(g2D.getColor()) +  begginig + ", " + end + ", "+ rect[0] +", "+ rect[1]+", "+beggAf+", "+endAf + temp(new Color(2,3,5)));
			for(int i = 0;i<dok+1;i++) {
				int onLinex = (int)(beggAf.x + ((double)i)/dok * (endAf.x - beggAf.x));
				int onLiney = (int)(beggAf.y + ((double)i)/dok * (endAf.y - beggAf.y));
				krzyw[i] = obraz.coords.pointToCmplx(new Point(onLinex, onLiney), rect[0], rect[1]);
			}
			Point pLast = obraz.coords.cmplxToPoint( krzyw[0] );
			for(Complex z : krzyw) {
				Point p = obraz.coords.cmplxToPoint(z);
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

}
