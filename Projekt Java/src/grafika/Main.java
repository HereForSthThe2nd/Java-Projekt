package grafika;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import funkcja.Complex;
import funkcja.Function;
import funkcja.FunctionPowloka;
import funkcja.Settings;
import funkcja.WewnetzrnaFunkcjaZleZapisana;
import funkcja.WrongSyntaxException;

public class Main extends JFrame {
	Graph wykres;
	JPanel containsWykres;
	Graph legenda;
	public Main() throws WewnetzrnaFunkcjaZleZapisana {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		setLayout(new BorderLayout());
		JMenuBar menuBar = new JMenuBar();
		JMenu Ustawienia = new JMenu("Ustawienia");
		JTextField doUsunieca = new JTextField("nie ma jeszcze rzadnych ustawień");
		Ustawienia.add(doUsunieca);
		menuBar.add(Ustawienia);
		setJMenuBar(menuBar);
		try {
			legenda = new Graph(new FunctionPowloka("z", new Settings()), new Complex(-10,-10), new Complex(10,10), 0.5, 300);
			wykres = new Graph(new FunctionPowloka("z^2", new Settings()), new Complex(-10,-10), new Complex(10,10), 0.5, 600);
		} catch (WrongSyntaxException e) {
			throw new WewnetzrnaFunkcjaZleZapisana(e);
		}
		legenda.setPadx(100);
		JPanel left = new JPanel();
		left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
	    Border border = BorderFactory.createLineBorder(Color.ORANGE);
		JComponent comp = (JComponent) Box.createRigidArea(new Dimension(100,100));
		comp.setBorder(border);
		JPanel zawieraTextFunckcji = new JPanel();
		JTextField funkcja = new JTextField(10);
		funkcja.setFont(new Font(funkcja.getFont().getName(), Font.ITALIC, 20));
		//funkcja.setMaximumSize(new Dimension(legenda.getWidth(), /*funkcja.getFontMetrics(funkcja.getFont()).getHeight() + */100000000));
		//funkcja.setBorder(new EmptyBorder(10, 5, 10, 0));
		//funkcja.setMinimumSize(new Dimension(10, funkcja.getFontMetrics(funkcja.getFont()).getHeight() + 10000));
		funkcja.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					wykres.change(new FunctionPowloka(e.getActionCommand(), new Settings()), new Complex(-10,-10), new Complex(10,10), 0.5);
				} catch (WrongSyntaxException e1) {
					e1.printStackTrace();
				}
			}
		});
		//zawieraTextFunckcji.add(Box.createRigidArea(new Dimension(0,0)));
		//zawieraTextFunckcji.setBackground(Color.red);
		zawieraTextFunckcji.setLayout(new BoxLayout(zawieraTextFunckcji, BoxLayout.X_AXIS));
		JPanel panelMaly = new JPanel();
		JLabel nadFunkcja = new JLabel();
		panelMaly.setLayout(new GridLayout(2,1));
		panelMaly.add(nadFunkcja,0);
		panelMaly.add(funkcja);
		zawieraTextFunckcji.add(Box.createRigidArea(new Dimension(5,0)));
		zawieraTextFunckcji.add(panelMaly);
		JPanel przyciski = new JPanel();
		JButton uprosc = new JButton("Uprość");
		uprosc.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					FunctionPowloka fSimpl = (new FunctionPowloka(funkcja.getText(), new Settings())).simplify(new Settings());
					nadFunkcja.setText(""+Function.calledSimp);
					Function.calledSimp = 0;
					String simplTxt = fSimpl.write(new Settings());
					funkcja.setText(simplTxt);// = new JTextField();
					wykres.change(fSimpl, new Complex(-10,-10), new Complex(10,10), 0.5);
				} catch (WrongSyntaxException | WewnetzrnaFunkcjaZleZapisana e1) {
					e1.printStackTrace();
				}
			}
		});
		JButton rzeczIUroj = new JButton("Rozbij na część rzeczywistą i urojoną");
		rzeczIUroj.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					FunctionPowloka fRoz = (new FunctionPowloka(funkcja.getText(), new Settings())).splitByRealAndImaginery(new Settings());
					nadFunkcja.setText(""+Function.calledSimp);
					Function.calledSimp = 0;
					String rozTxt = fRoz.write(new Settings());
					funkcja.setText(rozTxt);// = new JTextField();
					wykres.change(fRoz, new Complex(-10,-10), new Complex(10,10), 0.5);
				} catch (WrongSyntaxException | WewnetzrnaFunkcjaZleZapisana e1) {
					e1.printStackTrace();
				}
			}
		});
		przyciski.add(uprosc);
		przyciski.add(rzeczIUroj);
		left.add(comp);
		left.add(Box.createRigidArea(new Dimension(0,30)));
		left.add(przyciski);
		left.add(Box.createRigidArea(new Dimension(0,30)));
		left.add(legenda);
		add(zawieraTextFunckcji, BorderLayout.NORTH);
		add(left, BorderLayout.WEST);
		add(wykres, BorderLayout.CENTER);
	}
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				try {
					Main main = new Main();
					main.setVisible(true);
				} catch (WewnetzrnaFunkcjaZleZapisana e) {
					e.printStackTrace();
				}
			}
		});
	}
}
