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
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
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
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;

import Inne.Complex;
import funkcja.Function;
import funkcja.FunctionPowloka;
import funkcja.Settings;
import funkcja.WrongSyntaxException;
import grafika.Graph.Coordinates;

public class Main extends JFrame {
	Graph wykres;
	JPanel containsWykres;
	Graph legenda;
	JLabel nadFunkcja;
	JTextField argument;
	JTextField wartosc;
	JCheckBox rysowanie;
	@Deprecated
	//zamiast tego można użyć wykres.function
	FunctionPowloka currentFunction ;
	public Main() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setMinimumSize(new Dimension(600,500));
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		setLayout(new BorderLayout());
		doTheMenu();
		try {
			currentFunction = new FunctionPowloka("z^2", new Settings());
			legenda = new Graph(300);
			wykres = new Graph(600);
			legenda.change(new FunctionPowloka("z", new Settings()), legenda.rect(new Complex(-10,-10), new Complex(10,10)),Graph.basic ,0.5);
			wykres.change(currentFunction, wykres.rect(new Complex(-3,-3), new Complex(3,3)),Graph.basic, 0.5);
		} catch (WrongSyntaxException e) {
			throw new IllegalStateException(e);
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
				try {
					currentFunction = new FunctionPowloka(e.getActionCommand(), new Settings());
					changeFunc(currentFunction.removeDiff());
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
				try {
					currentFunction = new FunctionPowloka(funkcjaTextField.getText(), new Settings());
					SwingWorker<Void,Void> uprosc = new SwingWorker<Void, Void>(){

						@Override
						protected Void doInBackground() throws Exception {
							try {
								FunctionPowloka fch = currentFunction.simplify(new Settings());
								funkcjaTextField.setText(fch.write(new Settings()));
								changeFunc(fch);
								nadFunkcja.setText("Wypisano nową funkcję.");
								return null;
							} catch (Exception e) {
								e.printStackTrace();
								throw new IllegalArgumentException(e);
							}
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
		
		Border border = BorderFactory.createLineBorder(Color.orange);
		JPanel lewStr = new JPanel();
		lewStr.setLayout(new BoxLayout(lewStr, BoxLayout.Y_AXIS));
		lewStr.setBorder(BorderFactory.createLineBorder(Color.blue, 2));
		
		JComponent opcja;
		JComponent wybor;
		JPanel calaOpcja;

		calaOpcja = new JPanel();
		calaOpcja.setLayout(new GridLayout(2,1));
		opcja = new JPanel ();
		opcja.add(new JLabel("Sposób pokolorowania legendy"));
		wybor= new JComboBox<String>();
		calaOpcja.add(opcja);
		calaOpcja.add(wybor);
		calaOpcja.setBorder(border);
		lewStr.add(calaOpcja);
		
		calaOpcja = new JPanel();
		calaOpcja.add(new JLabel("Rysowanie:"));
		rysowanie = new JCheckBox();
		calaOpcja.add(rysowanie);

		wybor= new JButton("Zapisz wykres");
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
		
		calaOpcja = new JPanel();
		opcja = new JLabel("Całka po krzywej: ");
		wybor = new JTextArea("---");
		((JTextArea)wybor).setEditable(false);
		calaOpcja.add(opcja);
		calaOpcja.add(wybor);
		calaOpcja.setBorder(border);
		lewStr.add(calaOpcja);
		
		lewStr.add(new JLabel("Legenda:"));
		
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
		argument.setEditable(false);
		wartosc.setEditable(false);
		lewStr.add(argumentLabel);
		lewStr.add(argument);
		lewStr.add(wartoscLabel);
		lewStr.add(wartosc);
		
		legenda.obraz.addMouseMotionListener(new MouseMotionListener() {
			
			@Override
			public void mouseMoved(MouseEvent e) {
				Rectangle rec = legenda.obraz.getBounds();
				//Complex val = Complex.add(legenda.coords.getLD(), new Complex (e.getX()/rec.getWidth()*(legenda.prawyGorny.x-legenda.coords.getLD().x), (1-e.getY()/rec.getHeight())*(legenda.prawyGorny.y-legenda.coords.getLD().y)));
				Complex val = legenda.coords.pointToCmplx(e.getPoint());
				wartosc.setText(val.printE(2, 2));
				legenda.foreGround.marker = val;
				legenda.repaint();
			}
			
			@Override
			public void mouseDragged(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		legenda.obraz.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseExited(MouseEvent e) {
				super.mouseExited(e);
				wartosc.setText("---");
				legenda.foreGround.marker = null;
				legenda.repaint();

			};
		});
		wykres.obraz.addMouseMotionListener(new MouseMotionListener() {
			
			@Override
			public void mouseMoved(MouseEvent e) {
				Rectangle rec = wykres.obraz.getBounds();
				//Complex arg = Complex.add(wykres.coords.getLD(), new Complex (e.getX()/rec.getWidth()*(wykres.prawyGorny.x-wykres.coords.getLD().x), (1-e.getY()/rec.getHeight())*(wykres.prawyGorny.y-wykres.coords.getLD().y)));
				Complex arg = wykres.coords.pointToCmplx(e.getPoint());
				Complex val = wykres.getValueAt(e.getX(), e.getY());
				argument.setText(arg.printE(2, 2));
				wartosc.setText(val.printE(2, 2));
				wykres.foreGround.marker = arg;
				legenda.foreGround.marker = val;
				legenda.repaint();
				wykres.repaint();
			}
			
			@Override
			public void mouseDragged(MouseEvent e) {
				Rectangle rec = wykres.obraz.getBounds();
				if(0<=e.getX() && e.getX()<rec.getWidth() && 0<=e.getY() && e.getY()<rec.getHeight()) {
					//Complex arg = Complex.add(wykres.coords.getLD(), new Complex (e.getX()/rec.getWidth()*(wykres.prawyGorny.x-wykres.coords.getLD().x), (1-e.getY()/rec.getHeight())*(wykres.prawyGorny.y-wykres.coords.getLD().y)));
					Complex arg = wykres.coords.pointToCmplx(e.getPoint());
					Complex val = wykres.getValueAt(e.getX(), e.getY());
					argument.setText(arg.printE(2, 2));
					wartosc.setText(val.printE(2, 2));
					wykres.foreGround.marker = arg;
					legenda.foreGround.marker = val;
					if(rysowanie.isSelected()) {
							wykres.foreGround.addPointToCurve(arg);
							legenda.foreGround.addPointToCurve(val);
							legenda.repaint();
							wykres.repaint();
					}
					else {
						if(wykres.foreGround.rect != null) {
							wykres.foreGround.rect[1] = wykres.coords.pointToCmplx( e.getPoint() );
							legenda.repaint();
							wykres.repaint();
						}
					}
				}
			}
		});
		wykres.obraz.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
				if(!rysowanie.isSelected() && wykres.foreGround.rect != null) {
					wykres.coords.setLD( new Complex(wykres.foreGround.rect[0].x < wykres.foreGround.rect[1].x ? 
						wykres.foreGround.rect[0].x : wykres.foreGround.rect[1].x,
						wykres.foreGround.rect[0].y < wykres.foreGround.rect[1].y ? 
						wykres.foreGround.rect[0].y : wykres.foreGround.rect[1].y) );
					wykres.coords.setPG( new Complex(wykres.foreGround.rect[0].x > wykres.foreGround.rect[1].x ? 
							wykres.foreGround.rect[0].x : wykres.foreGround.rect[1].x,
							wykres.foreGround.rect[0].y > wykres.foreGround.rect[1].y ? 
							wykres.foreGround.rect[0].y : wykres.foreGround.rect[1].y) );
					System.out.println(wykres.coords.getLD().print(2) + "  " + wykres.coords.getPG().print(2));
					wykres.foreGround.rect = null;
					wykres.foreGround.szyba = new Color(0,0,0,50);
					wykres.foreGround.repaint();
					SwingWorker<Void,Void> work = changeFunc(wykres.function);
					work.addPropertyChangeListener(new PropertyChangeListener() {
						@Override
						public void propertyChange(PropertyChangeEvent evt) {
						    if (evt.getPropertyName().equals("state") &&
						        evt.getNewValue() == SwingWorker.StateValue.DONE) {
								wykres.foreGround.szyba = new Color(0,0,0,0);
								wykres.foreGround.repaint();
						    }
						}
					});
				}
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				if(rysowanie.isSelected()) {
					wykres.foreGround.addNewCurve();
					legenda.foreGround.addNewCurve();
				}
				else {
					wykres.foreGround.rect = new Complex[] {wykres.coords.pointToCmplx( e.getPoint()), wykres.coords.pointToCmplx( e.getPoint() )};
				}
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				argument.setText("---");
				wartosc.setText("---");
				wykres.foreGround.marker = null;
				legenda.foreGround.marker = null;
				legenda.repaint();
				wykres.repaint();
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub
			}
		});
		JComponent rootPane = getRootPane();
		Object rysowanieToggleKey = 0;
		rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ctrl R"), rysowanieToggleKey);
		rootPane.getActionMap().put(rysowanieToggleKey, new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				rysowanie.setSelected(!rysowanie.isSelected());
			}
			
		});
		rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ctrl W"), "usunKrzywe");
		rootPane.getActionMap().put("usunKrzywe", new AbstractAction() {
		    @Override
		    public void actionPerformed(ActionEvent e) {
		    	wykres.foreGround.resetCurve();
		    	legenda.foreGround.resetCurve();
		    	repaint();
		    }
		});
		rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("SLASH"), "przejdz do pola tekstowego");
		rootPane.getActionMap().put("przejdz do pola tekstowego", new AbstractAction() {
		    @Override
		    public void actionPerformed(ActionEvent e) {
		    	if(funkcjaTextField.isFocusOwner())
		    		return;
		    	Timer timer = new Timer(0, new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						funkcjaTextField.setEnabled(true);
					}
				});
		    	timer.setRepeats(false);
				funkcjaTextField.setEnabled(false);
		    	timer.start();
		    	funkcjaTextField.requestFocus();
		    	funkcjaTextField.setCaretPosition(funkcjaTextField.getText().length());
		    }
		});
		Object wyjdzZziekszeniaKey = 3;
		rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), wyjdzZziekszeniaKey);
		rootPane.getActionMap().put(wyjdzZziekszeniaKey, new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if(wykres.foreGround.rect != null) {
					wykres.foreGround.rect = null;
					wykres.repaint();
				}
				
			}
		});

	}
	
	private void doTheMenu() {
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		JMenu Ustawienia = new JMenu("Ustawienia");
		menuBar.add(Ustawienia);
		JTextField doUsunieca = new JTextField("nie ma jeszcze rzadnych ustawień");
		doUsunieca.setEnabled(false);
		Ustawienia.add(doUsunieca);
		JMenu wykresMenu = new JMenu("Wykres");
		JCheckBoxMenuItem osieLegendy = new JCheckBoxMenuItem("Osie legendy");
		JCheckBoxMenuItem osieWykresu = new JCheckBoxMenuItem("Osie wykresu");
		JCheckBoxMenuItem legendaLogSkala = new JCheckBoxMenuItem("Moduł legendy w skali logarytmicznej");
		JCheckBoxMenuItem wykresLogSkala = new JCheckBoxMenuItem("Moduł dziedziny(wyresu) w skali logarytmicznej");
		JCheckBoxMenuItem legndaInf = new JCheckBoxMenuItem("Legenda wokół nieksończoności");
		JCheckBoxMenuItem wykresInf = new JCheckBoxMenuItem("Wykres wokół nieskończoności");
		JCheckBoxMenuItem legendaKwadrat = new JCheckBoxMenuItem("Obszar legendy musi byc kwadratem");
		JCheckBoxMenuItem wykresKwadrat = new JCheckBoxMenuItem("Obszar wykresu musi byc kwadratem");
		wykresMenu.add(osieLegendy);
		wykresMenu.add(osieWykresu);
		wykresMenu.add(legendaLogSkala);
		wykresMenu.add(wykresLogSkala);
		wykresMenu.add(legndaInf);
		wykresMenu.add(wykresInf);
		wykresMenu.add(legendaKwadrat);
		wykresMenu.add(wykresKwadrat);
		menuBar.add(wykresMenu);
		
		legndaInf.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(legndaInf.isSelected()) {
					legenda.coords = legenda.aroundInf(legenda.coords.getLD(), legenda.coords.getPG());
					legenda.change();
				}
				else {
					legenda.coords = legenda.rect(legenda.coords.getLD(), legenda.coords.getPG());
					legenda.change();
				}
			}
		});
		
		wykresInf.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(wykresInf.isSelected()) {
					wykres.coords = wykres.aroundInf(wykres.coords.getLD(), wykres.coords.getPG());
					changeFunc(wykres.function);
				}
				else {
					wykres.coords = wykres.rect(wykres.coords.getLD(), wykres.coords.getPG());
					changeFunc(wykres.function);
				}
			}
		});

	}
	
	private SwingWorker<Void,Void> changeFunc(FunctionPowloka f) {
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
				try {
					wykres.change(f, wykres.coords ,Graph.basic, 0.5);
					
					legenda.foreGround.resetCurve();
					for(LinkedList<Complex> krzywa : wykres.foreGround.krzywa) {
						legenda.foreGround.addNewCurve();
						for(Complex z : krzywa) {
							//System.out.println(p);
							//System.out.println(wykres.getValueAt(p));
							Point p = wykres.coords.cmplxToPoint( z );
							if(p.x >= 0 && p.y >= 0 && p.x < wykres.img.getWidth() && p.y < wykres.img.getHeight())
								legenda.foreGround.addPointToCurve(wykres.getValueAt(p));
						}
					}
					legenda.repaint();
					
					timerListener.stop = true;
					nadFunkcja.setForeground(Color.black);
					nadFunkcja.setText("Obliczono i pokazano funkcję.");
				}catch(Exception e) {
					e.printStackTrace();
				}
					return null;
			}
			
		};
		timer.start();
		narysuj.execute();
		return narysuj;
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
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
