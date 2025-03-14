package poczatek;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
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
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import java.awt.image.BufferedImage;

public class GraphingFunction extends JLabel {
	BufferedImage img;
	JLabel label;
	public GraphingFunction(Function f, Complex lewyDolny, Complex prawyGorny) {
		double A = Math.sqrt((prawyGorny.x-lewyDolny.x)/(prawyGorny.y-lewyDolny.y));
		//setSize((int)(500*A), (int)(500/A));
		img = new BufferedImage((int)(500*A),(int)(500/A),BufferedImage.TYPE_INT_RGB);
		setIcon(new ImageIcon(img));
		double x;
		double y;
		System.out.println(img.getWidth());
		System.out.println(img.getHeight());
		for(int xI=0; xI<img.getWidth();xI++) {
			for(int yI=0;yI<img.getHeight();yI++) {
				x = xI*(prawyGorny.x-lewyDolny.x)/img.getWidth()+lewyDolny.x;
				y = yI*(prawyGorny.y-lewyDolny.y)/img.getWidth()+lewyDolny.y;
				Complex[] z = new Complex[] {new Complex(x,y)};
				int[] RGBColor = HSLToRGB(pointToHSL(f.evaluate(z)));
				img.setRGB(xI, yI, rgbToHex(RGBColor));
			}
		}
	}
	
	public void save(String str) throws IOException {

		File imgfile = new File(str+".png");
		ImageIO.write(img, "png", imgfile);
	}
	
	static double[] pointToHSL(Complex z) {
		//TODO: abyładnie wyglądało, zapewne dać użytkownikowi parę opcji
		double[] HSL = new double[3];
		HSL[0] = z.arg() + 2.0/3*Math.PI;
		HSL[1] = 1;
		HSL[2] = 2/Math.PI*Math.atan( Math.log((z.mod())+1) );
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

	
	public static void main(String[] args) throws WrongSyntaxException, IOException {
		//TODO: wygląda bardzo pixelowanie, zapewne trzeba będzie ten obraz wygładzić
		Function funkcja = Function.read(new BlokList(Function.preliminaryChanges("exp(z)")));
		System.out.println(funkcja.write(PrintSettings.defaultSettings));
		//funkcja.evaluate(null).print();
		
		GraphingFunction a = new GraphingFunction(funkcja, new Complex(-10,-10), new Complex(10,10));
		//a.save(funkcja.write(3, false));
		JFrame frame = new JFrame();
		frame.add(a);
		frame.setSize(a.img.getWidth(), a.img.getHeight());
		frame.setVisible(true);

	}
}
