package grafika;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;

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
	JLabel nadFunkcja;
	Complex lDolnyWykres = new Complex(-3,-3);
	Complex pGornyWykres = new Complex(3,3);
	JTextField argument;
	JTextField wartosc;
	public Main() throws WewnetzrnaFunkcjaZleZapisana {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setMinimumSize(new Dimension(600,500));
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
			wykres = new Graph(new FunctionPowloka("z^2", new Settings()), lDolnyWykres, pGornyWykres, 0.5, 600);
		} catch (WrongSyntaxException e) {
			throw new WewnetzrnaFunkcjaZleZapisana(e);
		}
		JPanel zawieraTextFunckcji = new JPanel();
		JTextField funkcjaTextField = new JTextField("((((((((((((z^2+z)^2+z)^2+z)^2+z)^2+z)^2+z)^2+z)^2+z)^2+z)^2+z)^2+z)^2+z)^2+z");
		funkcjaTextField.setFont(new Font(funkcjaTextField.getFont().getName(), Font.ITALIC, 20));
		zawieraTextFunckcji.setLayout(new BoxLayout(zawieraTextFunckcji, BoxLayout.X_AXIS));
		JPanel panelMaly = new JPanel();
		nadFunkcja = new JLabel("Wpisz funkcję poniżej:");
		panelMaly.setLayout(new GridLayout(2,1));
		panelMaly.add(nadFunkcja,0);
		panelMaly.add(funkcjaTextField);
		zawieraTextFunckcji.add(Box.createRigidArea(new Dimension(5,0)));
		zawieraTextFunckcji.add(panelMaly);

		JPanel left = new JPanel();
		left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
		funkcjaTextField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				FunctionPowloka f;
				try {
					f = new FunctionPowloka(e.getActionCommand(), new Settings());
					changeFunc(f);
				} catch (WrongSyntaxException e1) {
					nadFunkcja.setForeground(Color.red);
					nadFunkcja.setText(e1.messageForUser);
				}
			}
		});
		JPanel przyciski = new JPanel();
		JButton uprosc = new JButton("Uprość");
		uprosc.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				FunctionPowloka f;
				try {
					f = new FunctionPowloka(funkcjaTextField.getText(), new Settings());
					SwingWorker<Void,Void> uprosc = new SwingWorker<Void, Void>(){

						@Override
						protected Void doInBackground() throws Exception {
							FunctionPowloka fch = f.simplify(new Settings());
							funkcjaTextField.setText(fch.write(new Settings()));
							changeFunc(f.simplify(new Settings()));
							return null;
						}
						
					};
					uprosc.execute();
				} catch (WrongSyntaxException e1) {
					nadFunkcja.setForeground(Color.red);
					nadFunkcja.setText(e1.messageForUser);
				}
			}
		});
		JButton rzeczIUroj = new JButton("Rozbij na część rzeczywistą i urojoną");
		rzeczIUroj.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				FunctionPowloka f;
				try {
					f = new FunctionPowloka(funkcjaTextField.getText(), new Settings());
					SwingWorker<Void,Void> rozbijFunc = new SwingWorker<Void,Void>(){

						@Override
						protected Void doInBackground() throws Exception {
							FunctionPowloka fch = f.splitByRealAndImaginery(new Settings());
							funkcjaTextField.setText(fch.write(new Settings()));
							changeFunc(fch);
							return null;
						}
					};
					rozbijFunc.execute();
				} catch (WrongSyntaxException e1) {
					nadFunkcja.setForeground(Color.red);
					nadFunkcja.setText(e1.messageForUser);
				}
			}
		});
		przyciski.add(uprosc);
		przyciski.add(rzeczIUroj);
		JComponent opcja;
		JComponent wybor;
		JPanel calaOpcja;
		Border border = BorderFactory.createLineBorder(Color.orange);
		JPanel lewStr = new JPanel();
		lewStr.setLayout(new BoxLayout(lewStr, BoxLayout.Y_AXIS));
		lewStr.setBorder(BorderFactory.createLineBorder(Color.blue, 2));
		
		calaOpcja = new JPanel();
		opcja = new JTextArea("Przedstawić obszar wokół nieskończoności?");
		wybor = new JCheckBox();
		calaOpcja.add(opcja);
		calaOpcja.add(wybor);
		calaOpcja.setBorder(border);
		lewStr.add(calaOpcja);
		
		calaOpcja = new JPanel();
		opcja = new JTextArea("Pokazać oznaczenia legendy?");
		wybor= new JCheckBox();
		calaOpcja.add(opcja);
		calaOpcja.add(wybor);
		calaOpcja.setBorder(border);
		lewStr.add(calaOpcja);
		
		calaOpcja = new JPanel();
		opcja = new JTextArea("Pokazać oznaczenia wykresu?");
		wybor= new JCheckBox();
		calaOpcja.add(opcja);
		calaOpcja.add(wybor);
		calaOpcja.setBorder(border);
		lewStr.add(calaOpcja);
		
		calaOpcja = new JPanel();
		opcja = new JPanel ();
		opcja.add(new JTextArea("Sposób pokolorowania legendy"));
		wybor= new JComboBox<String>();
		calaOpcja.setLayout(new GridLayout(2,1));
		calaOpcja.add(opcja);
		calaOpcja.add(wybor);
		calaOpcja.setBorder(border);
		lewStr.add(calaOpcja);
		
		calaOpcja = new JPanel();
		wybor= new JButton("Zapisz");
		((JButton)wybor).addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					JFileChooser ch = new JFileChooser();
					/*
					ch.setFileFilter(new FileFilter() {

						   public String getDescription() {
						       return "JPG Images (*.jpg)";
						   }

						   public boolean accept(File f) {
						       if (f.isDirectory()) {
						           return true;
						       } else {
						           String filename = f.getName().toLowerCase();
						           return filename.endsWith(".jpg") || filename.endsWith(".jpeg") ;
						       }
						   }
						});
					*/
					int pot = ch.showSaveDialog(null);
					if(pot == JFileChooser.APPROVE_OPTION) {
						wykres.save(ch.getSelectedFile());
					}
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		calaOpcja.add(wybor);
		calaOpcja.setBorder(BorderFactory.createLineBorder(Color.red));
		lewStr.add(calaOpcja);

		left.add(Box.createRigidArea(new Dimension(0,30)));
		left.add(przyciski);
		left.add(Box.createRigidArea(new Dimension(0,30)));
		left.add(lewStr);
		lewStr.add(legenda);
		add(zawieraTextFunckcji, BorderLayout.NORTH);
		add(left, BorderLayout.WEST);
		add(wykres, BorderLayout.CENTER);
		
		JLabel argumentLabel = new JLabel("Argument:");
		JLabel wartoscLabel = new JLabel("Wartość:");
		argument = new JTextField("---");
		wartosc = new JTextField("---");
		lewStr.add(argumentLabel);
		lewStr.add(argument);
		lewStr.add(wartoscLabel);
		lewStr.add(wartosc);
		legenda.obraz.addMouseMotionListener(new MouseMotionListener() {
			
			@Override
			public void mouseMoved(MouseEvent e) {
				Rectangle rec = legenda.obraz.getBounds();
				Complex val = Complex.add(legenda.lewyDolny, new Complex (e.getX()/rec.getWidth()*(legenda.prawyGorny.x-legenda.lewyDolny.x), (1-e.getY()/rec.getHeight())*(legenda.prawyGorny.y-legenda.lewyDolny.y)));
				argument.setText("---");
				wartosc.setText(val.printE(2, 2));

			}
			
			@Override
			public void mouseDragged(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		wykres.obraz.addMouseMotionListener(new MouseMotionListener() {
			
			@Override
			public void mouseMoved(MouseEvent e) {
				Rectangle rec = wykres.obraz.getBounds();
				Complex arg = Complex.add(wykres.lewyDolny, new Complex (e.getX()/rec.getWidth()*(wykres.prawyGorny.x-wykres.lewyDolny.x), (1-e.getY()/rec.getHeight())*(wykres.prawyGorny.y-wykres.lewyDolny.y)));
				Complex val = wykres.values[e.getX()][e.getY()];
				argument.setText(arg.printE(2, 2));
				wartosc.setText(val.printE(2, 2));

			}
			
			@Override
			public void mouseDragged(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	
	private void changeFunc(FunctionPowloka f) {
		nadFunkcja.setForeground(Color.black);
		nadFunkcja.setText("W trakcie obliczania funkcji");
		ActionListenerWthStop timerListener = new ActionListenerWthStop() {
			static int liczKropki = 0;
			@Override
			public void actionPerformed(ActionEvent e) {
					if(stop) {
						return;
					}
					String kropki;
					switch(liczKropki){
					case 0:
						kropki = ".";
						break;
					case 1:
						kropki = "..";
						break;
					case 2:
						kropki = "...";
						break;
					default:
						kropki = "";
						break;
					}
					nadFunkcja.setText("W trakcie obliczania funkcji" + kropki);
					liczKropki++;
					liczKropki %= 4;
			}
				
		};
		Timer timer = new Timer(800, timerListener);
		SwingWorker<Void,Void> narysuj = new SwingWorker<Void, Void>(){

			@Override
			protected Void doInBackground() throws Exception {
				wykres.change(f, lDolnyWykres, pGornyWykres, 0.5);
				timerListener.stop = true;
				nadFunkcja.setForeground(Color.black);
				nadFunkcja.setText("Obliczono i pokazano funkcję.");
				return null;
			}
			
		};
		timer.start();
		narysuj.execute();
	}
	
	abstract class ActionListenerWthStop implements ActionListener{
		boolean stop = false;
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
