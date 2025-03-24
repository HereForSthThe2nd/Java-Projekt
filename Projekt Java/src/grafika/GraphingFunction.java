package grafika;


import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
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
import java.util.Random;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;

import funkcja.Complex;
import funkcja.Function;
import funkcja.FunctionPowloka;
import funkcja.Settings;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import java.awt.image.BufferedImage;

public class GraphingFunction extends JLabel {
	BufferedImage img;
	JLabel label;
	public GraphingFunction(FunctionPowloka f, Complex lewyDolny, Complex prawyGorny, double lSpeedChange) {
		int bok = 200;
		double A = Math.sqrt((prawyGorny.x-lewyDolny.x)/(prawyGorny.y-lewyDolny.y));
		//setSize((int)(500*A), (int)(500/A));
		img = new BufferedImage((int)(bok*A),(int)(bok/A),BufferedImage.TYPE_INT_RGB);
		setIcon(new ImageIcon(img));
		double x;
		double y;
		System.out.println(img.getWidth());
		System.out.println(img.getHeight());
		for(int xI=0; xI<img.getWidth();xI++) {
			for(int yI=0;yI<img.getHeight();yI++) {
				x = xI*(prawyGorny.x-lewyDolny.x)/img.getWidth()+lewyDolny.x;
				y = yI*(lewyDolny.y-prawyGorny.y)/img.getHeight()+prawyGorny.y;
				Complex[] z = new Complex[] {new Complex(x,y)};
				if(z[0].equals(Complex.i)) {
					z[0].print();
					System.out.println(xI + "   " + yI);
				}
				int[] RGBColor = HSLToRGB(pointToHSL(f.evaluate(z),lSpeedChange));
				img.setRGB(xI, yI, rgbToHex(RGBColor));
			}
		}
	}
	
	public void save(String str) throws IOException {

		File imgfile = new File(str+".png");
		ImageIO.write(img, "png", imgfile);
	}
	
	static double[] pointToHSL(Complex z, double lSpeedChange) {
		//TODO: abyładnie wyglądało, zapewne dać użytkownikowi parę opcji
		double[] HSL = new double[3];
		HSL[0] = z.arg() + 2.0/3*Math.PI;
		HSL[1] = 1;
		//HSL[2] = 2/Math.PI*Math.atan( Math.log(Math.pow(z.mod(), 1/lSpeedChange)+1) );
		HSL[2] = 2/Math.PI*Math.atan( Math.pow(Math.log((z.mod()+1)), 1/lSpeedChange ));
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

	
	public static void main(String[] args) throws Exception {
		//TODO: wygląda bardzo pixelowanie, zapewne trzeba będzie ten obraz wygładzić
		Settings set = new Settings();
		//FunctionPowloka f = new FunctionPowloka("exp(exp(x*z)+sin(z^2)^2+(1+i)cos(z)+5ln(z+x)^2) + sin(z)", set);
		FunctionPowloka f = new FunctionPowloka(new FuncNumConst(new Complex(Double.MAX_VALUE)), set);
		f.print(set);
		GraphingFunction graf = new GraphingFunction(f, new Complex(-5,-5), new Complex(5,5), 10);
		JFrame frame = new JFrame();
		frame.setLayout(new GridLayout(2,2));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(graf, 0);
		frame.setSize(graf.img.getWidth()*2, graf.img.getHeight()*2);
		
		f.simplify(set);
		f.print(set);
		
		GraphingFunction graf2 = new GraphingFunction(f, new Complex(-5,-5), new Complex(5,5), 10);
		frame.add(graf2, 1);

		f.splitByRealAndImaginery(set);
		f.print(set);
		
		GraphingFunction graf3 = new GraphingFunction(f, new Complex(-5,-5), new Complex(5,5), 10);
		frame.add(graf3, 2);
		frame.setVisible(true);

	}
}
