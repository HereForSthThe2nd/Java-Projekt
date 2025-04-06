/*
 * wyświetlanie wykresów
 * prowizoryczne
 */

package grafika;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.IOException;
import java.text.AttributedCharacterIterator;
import java.util.LinkedList;
import java.util.Random;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import funkcja.*;
import ogolne.Complex;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import java.awt.image.BufferedImage;

class threadAndItsBegg{
	final HasFinishBoolean thr;
	final long beggTime;
	public threadAndItsBegg(HasFinishBoolean worker, long time) {
		thr = worker;
		beggTime = time;
	}
}

abstract class HasFinishBoolean extends SwingWorker<Void,Void>{//bardzo prowizoryczne ale co tam
	boolean finish = false; 
}

public class Graph extends JPanel {
	BufferedImage img;
	Complex[][] values; //może trochę overkill, zajmuje z 5 razy więcej miejsca niż obraz, może np. zrobić by co 3 - 10 ^ 2 pikseli obliczało wartości po funkcji (i je tylko wtedy zapisywało) a reszę jakoś interpolowało
	Complex prawyGorny;
	Complex lewyDolny;
	long lastChange = System.nanoTime();
	long begginingOfLastChange = System.nanoTime();
	double colorSpeedChange;
	static int noOfRep = 0;
	GridBagConstraints gbc;
	GridBagLayout layout = new GridBagLayout();
	JLabel obraz;
	//TODO: usunac zmienna ponizej
	static int usunac = 0;
	public void setPadx(int padx) {
		gbc.ipadx = padx;
		//if(label == null || gbc == null)
		//	System.out.println("null" +"  " + gbc + "  " + label);
		layout.setConstraints(obraz, gbc);
	}

	public void setPady(int pady) {
		gbc.ipady = pady;
		layout.setConstraints(obraz, gbc);
	}

	static LinkedList<threadAndItsBegg> currentlyChanging = new LinkedList<threadAndItsBegg>();
	public Graph(FunctionPowloka f, Complex lewyDolny, Complex prawyGorny, double colorSpeedChange, int bok) {
		this.colorSpeedChange = colorSpeedChange;
		this.lewyDolny = lewyDolny;
		this.prawyGorny = prawyGorny;
		img = new BufferedImage(bok,bok,BufferedImage.TYPE_INT_RGB);
		obraz = new JLabel(new ImageIcon(img));
		setLayout(layout);
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.CENTER;
		add(obraz, gbc);
		setBackground(new Color(usunac,255-usunac,0));
		usunac += 55;
		usunac %= 226;
		setSize(img.getWidth()+200, img.getHeight()+200);
		values = new Complex[img.getWidth()][img.getHeight()];
		double x;
		double y;
		for(int xI=0; xI<img.getWidth();xI++) {
			for(int yI=0;yI<img.getHeight();yI++) {
				x = xI*(prawyGorny.x-lewyDolny.x)/img.getWidth()+lewyDolny.x;
				y = yI*(lewyDolny.y-prawyGorny.y)/img.getHeight()+prawyGorny.y;
				Complex[] z = new Complex[] {new Complex(x,y)};
				values[xI][yI] = f.evaluate(z);
			}
		}
		changeColor(colorSpeedChange);
	}
	
	public void save(String str) throws IOException {

		File imgfile = new File(str+".png");
		ImageIO.write(img, "png", imgfile);
	}
	public void change(FunctionPowloka f, Complex lewyDolny, Complex prawyGorny, double colorSpeedChange) {
		this.colorSpeedChange = colorSpeedChange;
		this.lewyDolny = lewyDolny;
		this.prawyGorny = prawyGorny;
		values = new Complex[img.getWidth()][img.getHeight()];
		double x;
		double y;
		for(int xI=0; xI<img.getWidth();xI++) {
			for(int yI=0;yI<img.getHeight();yI++) {
				x = xI*(prawyGorny.x-lewyDolny.x)/img.getWidth()+lewyDolny.x;
				y = yI*(lewyDolny.y-prawyGorny.y)/img.getHeight()+prawyGorny.y;
				Complex[] z = new Complex[] {new Complex(x,y)};
				values[xI][yI] = f.evaluate(z);
			}
		}
		changeColor(colorSpeedChange);
	}
	public void changeColor(double cSC) {
		//to jeszcze musi byc dopracowane
		colorSpeedChange = cSC;
		long beggining = System.nanoTime();
		HasFinishBoolean draw = new HasFinishBoolean(){
			//boolean finish;
			@Override
			protected Void doInBackground() throws Exception {

				for(int xI=0; xI<img.getWidth();xI++) {
					if(!finish)
						for(int yI=0;yI<img.getHeight();yI++) {
							int[] RGBColor = HSLToRGB(pointToHSL(values[xI][yI],colorSpeedChange));
							img.setRGB(xI, yI, rgbToHex(RGBColor));
						}
				}
				return null;
			}
			
			@Override
			protected void done() {
				if(beggining > begginingOfLastChange && !finish) {
					//potencjalnie moze tutaj sie innt thread zrobic i pokazac old news ale nawet jesli do tego dojdzie nie powinno byc to duzym problemem\
					begginingOfLastChange = beggining;
					while(currentlyChanging.size() > 0 && currentlyChanging.getFirst().beggTime <= begginingOfLastChange) {
						currentlyChanging.get(0).thr.finish = true;
						currentlyChanging.removeFirst();
					}
					repaint();
				}
			}
		};
		currentlyChanging.add(new threadAndItsBegg(draw, beggining));
		long thisTime = System.nanoTime();
		for(int i = 1; i<currentlyChanging.size();i++) {
			if(thisTime - currentlyChanging.get(i).beggTime < 1) {
				currentlyChanging.get(i).thr.finish = true;
				currentlyChanging.remove(i);
			}
		}
		draw.execute();

	}
	
	static double przedNorm(double r) {
		// nie da się dogodzić aby jedna funkcja zawsze ładnie wyglądała
		if(r<-0.00001) {
			throw new IllegalArgumentException("r musi być nieuemne. podane r: " + r);
		}
		r = r>0 ? r : 0;
		
		//funkcja 1 / z wygląda tak jak powinna (na wykresie w przedziale -3,-3,  3,3)
		return 2/Math.PI * (Math.atan(Math.sin(r / 20)*10 + Math.exp(r ) - 1));
		//return 2/Math.PI * (Math.atan(r));
	}
	
	static double[] pointToHSL(Complex z, double lSpeedChange) {
		//TODO: abyładnie wyglądało, zapewne dać użytkownikowi parę opcji
		double[] HSL = new double[3];
		HSL[0] = z.arg() + 2.0/3*Math.PI;
		HSL[1] = 1;
		//HSL[2] = 2/Math.PI*Math.atan( Math.log(Math.pow(z.mod(), 1/lSpeedChange)+1) );
		HSL[2] = przedNorm( Math.pow(z.mod(), lSpeedChange ));
		if(HSL[2]>1 || HSL[2]<0) {
		}
		return HSL;
	}
	static int[] HSLToRGB(double[] HSL) {
		if(HSL[2]==1) return new int[] {255,255,255};
		if(HSL[2]==0) return new int[] {0,0,0};
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
		
		int[] rgb = {(int) (nrgb[0]*255),(int) (nrgb[1]*255),(int) (nrgb[2]*255)};
		return rgb;
	}
	static int rgbToHex(int [] rgb) {
		return rgb[2] + 256*rgb[1]+ 256*256*rgb[0];
	}

	/*public static void main(String[] args) throws Exception {
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
	}*/
}
