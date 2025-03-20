package grafika;

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

public class PointToColor {
	static double[] pointToHSL(double x, double y) {
		double[] HSL = new double[3];
		HSL[0] = Math.atan2(y, x) + 2.0/3*Math.PI;
		HSL[1] = 1;
		HSL[2] = 2/Math.PI*Math.atan(Math.sqrt(x*x+y*y));
		if(HSL[2]>1 || HSL[2]<0) {
			System.out.println(HSL[2]);
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
	public static void main(String[] args) {
		BufferedImage img = new BufferedImage(500,500,BufferedImage.TYPE_INT_RGB);
		int[] c = HSLToRGB(new double[] {Math.PI/2, 1,0.22});
		System.out.println(c[0]);
		System.out.println(c[1]);
		System.out.println(c[2]);
		for(int x=0; x<img.getWidth();x++) {
			for(int y=0;y<img.getHeight();y++) {
				img.setRGB(x, y, rgbToHex(HSLToRGB(pointToHSL((x-img.getWidth()/2)/60.0, (y-img.getHeight()/2)/60.0))));
			}
		}
		JLabel label = new JLabel(new ImageIcon(img));
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(label);
		frame.setSize(img.getWidth(), img.getHeight());
		frame.setVisible(true);
		File imgfile = new File("screenshot.png");
		try {
			ImageIO.write(img, "png", imgfile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
