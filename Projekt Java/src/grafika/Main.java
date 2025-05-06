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
		legenda.setPadx(100);
		JPanel zawieraTextFunckcji = new JPanel();
		JTextField funkcja = new JTextField("((((((((((((z^2+z)^2+z)^2+z)^2+z)^2+z)^2+z)^2+z)^2+z)^2+z)^2+z)^2+z)^2+z)^2+z");
		funkcja.setFont(new Font(funkcja.getFont().getName(), Font.ITALIC, 20));
		zawieraTextFunckcji.setLayout(new BoxLayout(zawieraTextFunckcji, BoxLayout.X_AXIS));
		JPanel panelMaly = new JPanel();
		nadFunkcja = new JLabel("Wpisz funkcję poniżej:");
		panelMaly.setLayout(new GridLayout(2,1));
		panelMaly.add(nadFunkcja,0);
		panelMaly.add(funkcja);
		zawieraTextFunckcji.add(Box.createRigidArea(new Dimension(5,0)));
		zawieraTextFunckcji.add(panelMaly);

		JPanel left = new JPanel();
		left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
	    Border border = BorderFactory.createLineBorder(Color.ORANGE);
		//funkcja.setMaximumSize(new Dimension(legenda.getWidth(), /*funkcja.getFontMetrics(funkcja.getFont()).getHeight() + */100000000));
		//funkcja.setBorder(new EmptyBorder(10, 5, 10, 0));
		//funkcja.setMinimumSize(new Dimension(10, funkcja.getFontMetrics(funkcja.getFont()).getHeight() + 10000));
		funkcja.addActionListener(new ActionListener() {
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
		//zawieraTextFunckcji.add(Box.createRigidArea(new Dimension(0,0)));
		//zawieraTextFunckcji.setBackground(Color.red);
		JPanel przyciski = new JPanel();
		JButton uprosc = new JButton("Uprość");
		uprosc.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				FunctionPowloka f;
				try {
					f = new FunctionPowloka(funkcja.getText(), new Settings());
					SwingWorker<Void,Void> uprosc = new SwingWorker<Void, Void>(){

						@Override
						protected Void doInBackground() throws Exception {
							FunctionPowloka fch = f.simplify(new Settings());
							funkcja.setText(fch.write(new Settings()));
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
					f = new FunctionPowloka(funkcja.getText(), new Settings());
					SwingWorker<Void,Void> rozbijFunc = new SwingWorker<Void,Void>(){

						@Override
						protected Void doInBackground() throws Exception {
							FunctionPowloka fch = f.splitByRealAndImaginery(new Settings());
							funkcja.setText(fch.write(new Settings()));
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
		legenda.gbc.gridy = 1;
		legenda.gbc.gridx = 0;
		legenda.layout.setConstraints(legenda.obraz, legenda.gbc);
		JPanel nadLegenda = new JPanel();
		nadLegenda.setLayout(new GridLayout(0,1));
		JComponent opcja;
		JComponent wybor;
		JPanel calaOpcja;
		/*
		calaOpcja = new JPanel();
		opcja = new JTextArea("Przedstawić obszar wokół nieskończoności?");
		wybor = new JCheckBox();
		calaOpcja.add(opcja);
		calaOpcja.add(wybor);
		calaOpcja.setBorder(border);
		nadLegenda.add(calaOpcja);
		
		calaOpcja = new JPanel();
		opcja = new JTextArea("Pokazać oznaczenia legendy?");
		wybor= new JCheckBox();
		calaOpcja.add(opcja);
		calaOpcja.add(wybor);
		calaOpcja.setBorder(border);
		nadLegenda.add(calaOpcja);
		
		calaOpcja = new JPanel();
		opcja = new JTextArea("Pokazać oznaczenia wykresu?");
		wybor= new JCheckBox();
		calaOpcja.add(opcja);
		calaOpcja.add(wybor);
		calaOpcja.setBorder(border);
		nadLegenda.add(calaOpcja);
		
		calaOpcja = new JPanel();
		opcja = new JPanel ();
		opcja.add(new JTextArea("Sposób pokolorowania legendy"));
		wybor= new JComboBox<String>();
		calaOpcja.setLayout(new GridLayout(2,1));
		calaOpcja.add(opcja);
		calaOpcja.add(wybor);
		calaOpcja.setBorder(border);
		nadLegenda.add(calaOpcja);
		*/
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
		calaOpcja.setBorder(border);
		nadLegenda.add(calaOpcja);

		legenda.gbc.gridy = 0;
		legenda.gbc.gridx = 0;
		//legenda.gbc.fill = GridBagConstraints.HORIZONTAL;
		legenda.add(nadLegenda, legenda.gbc);
		left.add(Box.createRigidArea(new Dimension(0,30)));
		left.add(przyciski);
		left.add(Box.createRigidArea(new Dimension(0,30)));
		left.add(legenda);
		add(zawieraTextFunckcji, BorderLayout.NORTH);
		add(left, BorderLayout.WEST);
		add(wykres, BorderLayout.CENTER);
		
		JPanel podLegenda = new JPanel();
		podLegenda.setLayout(new BoxLayout(podLegenda, BoxLayout.Y_AXIS));
		JLabel argumentLabel = new JLabel("Argument:");
		JLabel wartoscLabel = new JLabel("Wartość:");
		argument = new JTextField("-");
		wartosc = new JTextField("-");
		podLegenda.add(argumentLabel);
		podLegenda.add(argument);
		podLegenda.add(wartoscLabel);
		podLegenda.add(wartosc);
		addMouseMotionListener(new MouseMotionListener() {
			
			@Override
			public void mouseMoved(MouseEvent e) {
				Complex arg = wykres.wspolrzedneMyszy();
				Complex val = legenda.wspolrzedneMyszy();
				argument.setText(arg == null ? "-" : ""+arg.x+" + i"+arg.y);
				wartosc.setText(val== null ? "-" : ""+val.x+" + i"+val.y);
			}
			
			@Override
			public void mouseDragged(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		legenda.gbc.gridx = 0;
		legenda.gbc.gridy = 2;
		legenda.add(podLegenda, legenda.gbc);
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
