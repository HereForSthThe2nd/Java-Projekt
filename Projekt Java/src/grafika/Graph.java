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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import Inne.Complex;
import funkcja.*;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import java.awt.image.BufferedImage;

public class Graph extends JPanel{
	BufferedImage img;
	private Complex[][] values; //może trochę overkill, zajmuje z 5 razy więcej miejsca niż obraz, może np. zrobić by co 3 - 10 ^ 2 pikseli obliczało wartości po funkcji (i je tylko wtedy zapisywało) a reszę jakoś interpolowało
	Complex prawyGorny;
	Complex lewyDolny;
	Coordinates coords;
	FunctionPowloka function;
	CmplxToColor colorMap;
	double[] colorMapParams;
	long lastChange = System.nanoTime();
	long begginingOfLastChange = System.nanoTime();
	static int noOfRep = 0;
	JPanel obraz;
	Foreground foreGround;
	//TODO: usunac zmienna ponizej
	static int usunac = 0;
	static CmplxToColor basic;
	Coordinates rectangle;
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
		setBackground(new Color(usunac,255-usunac,0));
		usunac += 55;
		usunac %= 226;
		setPreferredSize(new Dimension(img.getWidth()+10, img.getHeight()+10));
		setSize(new Dimension(img.getWidth()+10, img.getHeight()+10));
		
		rectangle = new Coordinates() {
			
			@Override
			public Complex pointToCmplx(Point p) {
				Complex diag = Complex.subt(prawyGorny, lewyDolny);
				Complex ret = Complex.add(lewyDolny, new Complex(	diag.x * p.x/img.getWidth(), diag.y * (img.getHeight()-p.y)/img.getHeight()));
				//System.out.println(p + "  " + ret.print(2) + " " + prawyGorny.print(2) + " " + lewyDolny.print(2) + " " + width);
				return ret;
			}
			
			@Override
			public Point cmplxToPoint(Complex z) {
				Complex diag = Complex.subt(prawyGorny, lewyDolny);
				Complex zWzgl = Complex.subt(z, lewyDolny);
				Point ret = new Point((int)(img.getWidth()*zWzgl.x/diag.x), (int)(img.getHeight() - img.getHeight()*zWzgl.y/diag.y));
				return ret;
			}
		};

		
	}
	
	public Graph(int bok, FunctionPowloka f, Complex lewyDolny, Complex prawyGorny, Coordinates coords,CmplxToColor colorMap, double...parameters) {
		this(bok);
		int width = img.getWidth();
		int height = img.getHeight();

		change(f, lewyDolny, prawyGorny, coords, colorMap, parameters);
	}
	
	public void save(File imgfile) throws IOException {
		ImageIO.write(img, "png", imgfile);
	}
	
	
	public void change(FunctionPowloka f, Complex lewyDolny, Complex prawyGorny, Coordinates coords ,CmplxToColor colorMap, double...parameters) {
		this.function = f;
		this.lewyDolny = lewyDolny;
		this.prawyGorny = prawyGorny;
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
		change(function, lewyDolny, prawyGorny, coords, colorMap, colorMapParams);
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

	
	/*zakomentowane, ponieważ nie chcę za każdm razem zmieniać pliku aby włączać Main
	public static void main(String[] args) throws Exception {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				//TODO: wygląda bardzo pixelowanie, zapewne trzeba będzie ten obraz wygładzić
				Settings set = new Settings();
				FunctionPowloka f;
				try {
					f = new FunctionPowloka("1 / z", set);
					//FunctionPowloka f = new FunctionPowloka("sin(e^z)", set);
					f.print(set);
					Complex lDolny = new Complex(-1,-1);
					Complex pGorny = new Complex(1,1);
					Graph graf = new Graph(f, lDolny, pGorny, 1, 400);
					JFrame frame = new JFrame();
					frame.setLayout(new FlowLayout());
					frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					frame.setVisible(true);
					frame.add(graf, 0);
					frame.setSize(graf.img.getWidth() + 100, graf.img.getHeight() + 50);
					JSlider slider = new JSlider(JSlider.VERTICAL, 0, 10, 1);
					slider.addChangeListener(new ChangeListener() {
						
						@Override
						public void stateChanged(ChangeEvent e) {
							graf.changeColor(slider.getValue());
						}
					});
					frame.add(slider);
					frame.setVisible(true);
				}
				catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

	}
	 */
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
			//int[] wsp = cmplxToCor(z);
			/*if(krzywa.getLast().size() > 1 && krzywa.getLast().getLast().distanceSq(wsp[0], wsp[1]) <  getWidth() *getHeight() / 100 / 100 / 5) {
				//System.out.println("tyle Mniej: " + tyleMniej++);
				return;
			}*/
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
				g2D.drawLine(rectP[0].x,rectP[0].y, rectP[0].x, rectP[1].y);
				g2D.drawLine(rectP[0].x, rectP[1].y, rectP[1].x, rectP[1].y);
				g2D.drawLine(rectP[1].x, rectP[1].y, rectP[1].x, rectP[0].y);
				g2D.drawLine(rectP[1].x, rectP[0].y, rectP[0].x, rectP[0].y);
			}
		}
		
	}

	interface CmplxToColor{
		Integer colorOf(Complex z, double... parameters);
	}
	 
	interface Coordinates{
		Complex pointToCmplx(Point p);
		Point cmplxToPoint(Complex z);
	}
}
