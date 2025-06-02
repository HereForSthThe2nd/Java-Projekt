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
import javax.swing.ButtonGroup;
import javax.swing.DefaultBoundedRangeModel;
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
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;

import Inne.Complex;
import funkcja.Function;
import funkcja.FunctionPowloka;
import funkcja.Settings;
import funkcja.TimeKeeping;
import funkcja.WrongSyntaxException;
import grafika.Graph.Coordinates;

public class Main extends JFrame {
	Graph wykres;
	JPanel containsWykres;
	FunctionTextField funkcjaTextField;
	boolean txtFuncUpToDate = false;
	Graph legenda;
	LabelAboveFunction nadFunkcja;
	JTextField argument;
	JTextField wartosc;
	JCheckBox rysowanie;
	Settings ustawienia = new Settings();
	public Main() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setMinimumSize(new Dimension(600,500));
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		setLayout(new BorderLayout());
		doTheShortcuts();
		doTheMenu();
		try {
			legenda = new Graph(300);
			wykres = new Graph(600);
			wykres.setBackground(new Color(55,200,0));
			wykres.function = new FunctionPowloka("z^2", new Settings());
			legenda.change(new FunctionPowloka("z", new Settings()), legenda.rect(new Complex(-10,-10), new Complex(10,10)),Graph.basic ,0.5);
			wykres.change(wykres.function, wykres.rect(new Complex(-3,-3), new Complex(3,3)),Graph.basic, 0.5);
		} catch (WrongSyntaxException e) {
			throw new IllegalStateException(e);
		}
		JPanel zawieraTextFunckcji = new JPanel();
		funkcjaTextField = new FunctionTextField("((((((((((((z^2+z)^2+z)^2+z)^2+z)^2+z)^2+z)^2+z)^2+z)^2+z)^2+z)^2+z)^2+z)^2+z");
		funkcjaTextField.setFont(new Font(funkcjaTextField.getFont().getName(), Font.ITALIC, 20));
		zawieraTextFunckcji.setLayout(new BoxLayout(zawieraTextFunckcji, BoxLayout.X_AXIS));
		JPanel panelMaly = new JPanel();
		nadFunkcja = new LabelAboveFunction("Wpisz funkcję poniżej:");
		panelMaly.setLayout(new GridLayout(2,1));
		panelMaly.add(nadFunkcja,0);
		panelMaly.add(funkcjaTextField);
		zawieraTextFunckcji.add(Box.createRigidArea(new Dimension(5,0)));
		zawieraTextFunckcji.add(panelMaly);
		
		funkcjaTextField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>(){

					@Override
					protected Void doInBackground() throws Exception {
						try {
							System.out.println(e.getActionCommand());
							TimeKeeping.reset();
							wykres.function = new FunctionPowloka(e.getActionCommand(), ustawienia);
							TimeKeeping.writeAndReset();
							changeFunc(wykres.function.removeDiff());
							funkcjaTextField.setText(wykres.function.write(ustawienia));
							funkcjaTextField.setCaretPosition(funkcjaTextField.getText().length());
						} catch (WrongSyntaxException e1) {
							nadFunkcja.setForeground(Color.red);
							nadFunkcja.setText(e1.messageForUser);
						}
						return null;
					}
					
				};
				worker.execute();
			}
		});
		
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
				mouseMoved(e);
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
					if(rysowanie.isSelected() && wykres.foreGround.krzywa.size() != 0) {
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
					SwingWorker<Void,Void> work = changeFunc();
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
				
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
			}
		});
		
		add(zawieraTextFunckcji, BorderLayout.NORTH);
		add(wykres, BorderLayout.CENTER);
		
		doTheLeft();

	}
	
	private void doTheShortcuts() {
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
		JCheckBoxMenuItem oblStale = new JCheckBoxMenuItem("Podczas upraszczania oblicza wartości stałych");
		JCheckBoxMenuItem uprPow = new JCheckBoxMenuItem("Nieścisłe upraszczanie potęg");
		JCheckBoxMenuItem potWyp = new JCheckBoxMenuItem("Wypisuj potęgi w postaci pow(.,.)");
		JCheckBoxMenuItem ladneStale = new JCheckBoxMenuItem("Ładnie wypisuje stałe");
		JPanel dokStalych = new JPanel();
		JSlider dokSt = new JSlider(new DefaultBoundedRangeModel(3, 0, 1, 9));
		JLabel dokStLab = new JLabel("Ilość wyświetlanych miejsc po przecinku : " + dokSt.getValue());
		dokStalych.add(dokStLab);
		dokStalych.add(dokSt);
		Ustawienia.add(oblStale);
		Ustawienia.add(uprPow);
		Ustawienia.add(potWyp);
		Ustawienia.add(ladneStale);
		Ustawienia.add(dokStalych);
		oblStale.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				ustawienia.evaluateConstants = oblStale.isSelected();
			}
		});
		dokSt.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				ustawienia.doubleAcc = dokSt.getValue();
				dokStLab.setText("Ilość wyświetlanych miejsc po przecinku : " + dokSt.getValue());
				if(funkcjaTextField.isUpToDate)
					funkcjaTextField.setText(wykres.function.write(ustawienia));
			}
		});
		ladneStale.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				ustawienia.writeNeatVar = ladneStale.isSelected();
				if(funkcjaTextField.isUpToDate)
					funkcjaTextField.setText(wykres.function.write(ustawienia));
			}
		});
		uprPow.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				ustawienia.strictPow = !uprPow.isSelected();
			}
		});


		potWyp.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				ustawienia.writePow = !potWyp.isSelected();
				if(funkcjaTextField.isUpToDate)
					funkcjaTextField.setText(wykres.function.write(ustawienia));
			}
		});
		
		JMenu wykresILegendaMenu = new JMenu("Wykres");
		JMenu legendaMenu = new JMenu("Legenda");
		JMenu wykresMenu = new JMenu("Wykres");
		wykresILegendaMenu.add(legendaMenu);
		wykresILegendaMenu.add(wykresMenu);
		JCheckBoxMenuItem osieLegendy = new JCheckBoxMenuItem("Osie legendy");
		JRadioButtonMenuItem legendaTyp = new JRadioButtonMenuItem("Normalna skala");
		JRadioButtonMenuItem legendaLogSkala = new JRadioButtonMenuItem("Moduł w skali logarytmicznej");
		JRadioButtonMenuItem legndaInf = new JRadioButtonMenuItem("Wokół nieksończoności");
		JCheckBoxMenuItem legendaKwadrat = new JCheckBoxMenuItem("Obszar musi byc kwadratem");
		ButtonGroup legBG = new ButtonGroup();
		
		legBG.add(legendaTyp);
		legBG.add(legndaInf);
		legBG.add(legendaLogSkala);
		legendaMenu.add(osieLegendy);
		legendaTyp.setSelected(true);
		
		JCheckBoxMenuItem osieWykresu = new JCheckBoxMenuItem("Osie wykresu");
		JRadioButtonMenuItem wykresTyp = new JRadioButtonMenuItem("Normalna skala");
		JRadioButtonMenuItem wykresLogSkala = new JRadioButtonMenuItem("Moduł w skali logarytmicznej");
		JRadioButtonMenuItem wykresInf = new JRadioButtonMenuItem("Wokół nieskończoności");
		JCheckBoxMenuItem wykresKwadrat = new JCheckBoxMenuItem("Obszar musi byc kwadratem");

		ButtonGroup wykBG = new ButtonGroup();
		wykBG.add(wykresTyp);
		wykBG.add(wykresInf);
		wykBG.add(wykresLogSkala);
		wykresTyp.setSelected(true);
		
		legendaMenu.add(legendaTyp);
		legendaMenu.add(legendaLogSkala);
		legendaMenu.add(legndaInf);
		legendaMenu.add(legendaKwadrat);

		wykresMenu.add(osieWykresu);
		wykresMenu.add(wykresTyp);
		wykresMenu.add(wykresLogSkala);
		wykresMenu.add(wykresInf);
		wykresMenu.add(wykresKwadrat);
		
		menuBar.add(wykresILegendaMenu);
		
		legendaTyp.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				legenda.coords = legenda.rect(legenda.coords.getLD(), legenda.coords.getPG());
				legenda.change();
			}
		});
		
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
	
		legendaLogSkala.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				legenda.coords = legenda.logarithmic(legenda.coords.getLD(), legenda.coords.getPG());
				legenda.change();
			}
		});
		
		wykresTyp.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				wykres.coords = wykres.rect(wykres.coords.getLD(), wykres.coords.getPG());
				wykres.change();
			}
		});
		
		wykresInf.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(wykresInf.isSelected()) {
					wykres.coords = wykres.aroundInf(wykres.coords.getLD(), wykres.coords.getPG());
					changeFunc();
				}
				else {
					wykres.coords = wykres.rect(wykres.coords.getLD(), wykres.coords.getPG());
					changeFunc();
				}
			}
		});

		wykresLogSkala.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				wykres.coords = wykres.logarithmic(wykres.coords.getLD(), wykres.coords.getPG());
				wykres.change();
			}
		});
		
	}
	
	private void doTheLeft() {
		JPanel left = new JPanel();
		left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
		JPanel przyciski = new JPanel();
		JButton uprosc = new JButton("Uprość");
		uprosc.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
					
					SwingWorker<Void,Void> uprosc = new SwingWorker<Void, Void>(){

						@Override
						protected Void doInBackground() throws Exception {
							try {
								TimeKeeping.reset();
								long pocz = System.currentTimeMillis();
								wykres.function = new FunctionPowloka(funkcjaTextField.getText(), ustawienia);
								long srodek = System.currentTimeMillis();
								TimeKeeping.writeAndReset();
								System.out.println(srodek - pocz + " <--wczytanie");

								 pocz = System.currentTimeMillis();
								FunctionPowloka fch = wykres.function.simplify(ustawienia);
								long pocz2 = System.currentTimeMillis();
								System.out.println(pocz2 - pocz + "ms  <--czas uproszczenia");
								funkcjaTextField.setText(fch.write(ustawienia));
								changeFunc(fch);
								nadFunkcja.setText("Wypisano nową funkcję.");
								return null;
							} catch (WrongSyntaxException e) {
								nadFunkcja.setForeground(Color.red);
								nadFunkcja.setText(e.messageForUser);
								return null;
							}
						}
						
					};
					uprosc.execute();
			}
		});
		JButton rzeczIUroj = new JButton("Rozbij na część rzeczywistą i urojoną");
		rzeczIUroj.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
					SwingWorker<Void,Void> rozbijFunc = new SwingWorker<Void,Void>(){

						@Override
						protected Void doInBackground() throws Exception {
							try {
								//nadFunkcja.setTextAnimated("Wczytywanie funkcji");
								TimeKeeping.reset();
								long pocz = System.currentTimeMillis();
								FunctionPowloka f = new FunctionPowloka(funkcjaTextField.getText(), ustawienia);
								long srodek = System.currentTimeMillis();
								TimeKeeping.writeAndReset();
								System.out.println(srodek - pocz + " <--wczytanie");
	
								pocz = System.currentTimeMillis();
								//nadFunkcja.setTextAnimated("Rozbijanie funkcji");
								System.out.println("Poczatek rozbijania");
								FunctionPowloka fch = f.splitByRealAndImaginery(ustawienia);
								System.out.println("Koniec rozbijania");
								long kon = System.currentTimeMillis();
								System.out.println(kon-pocz + " <-- czas na rozdzielenie");
								funkcjaTextField.setText(fch.write(ustawienia));
								pocz = System.currentTimeMillis();
								changeFunc(fch);
								kon = System.currentTimeMillis();
								System.out.println(kon-pocz + " <-- czas na wypisanie");
							} catch (WrongSyntaxException e1) {
								nadFunkcja.setForeground(Color.red);
								nadFunkcja.setText(e1.messageForUser);
							} catch(Exception e) {
								System.out.println("acb");
							}

							return null;
						}
					};
					rozbijFunc.execute();
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

		JButton wyczysc = new JButton("Wyczyść");
		wyczysc.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
		    	wykres.foreGround.resetCurve();
		    	legenda.foreGround.resetCurve();
		    	repaint();
			}
		});
		
		calaOpcja.add(wyczysc);
		
		calaOpcja.setBorder(BorderFactory.createLineBorder(Color.red));
		lewStr.add(calaOpcja);
		
		wybor= new JButton("Zapisz wykres");
		((JButton)wybor).addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					JFileChooser ch = new JFileChooser();
					
					ch.setFileFilter(new FileFilter() {

						   public String getDescription() {
						       return "JPG Images (*.jpg)";
						   }

						   public boolean accept(File f) {
							   System.out.println(f.toString());
						       if (f.isDirectory()) {
						           return true;
						       } else {
						           String filename = f.getName().toLowerCase();
						           return filename.endsWith(".jpg") || filename.endsWith(".jpeg") ;
						       }
						   }
						});
					
					int pot = ch.showSaveDialog(null);
					if(pot == JFileChooser.APPROVE_OPTION) {
						if(!ch.getSelectedFile().getPath().matches(".*\\.(jpg|jpeg)")){
							wykres.save(new File(ch.getSelectedFile().getPath() + ".jpg"));
						}
						else
							wykres.save(ch.getSelectedFile());
					}
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(null, "Nie udało się zapisać pliku.", "Błąd!", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		
		lewStr.add(wybor);
		
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
		add(left, BorderLayout.WEST);

		
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
		
	}
	
	private SwingWorker<Void, Void> changeFunc() {
		nadFunkcja.setForeground(Color.black);
		nadFunkcja.setTextAnimated("W trakcie obliczania funkcji");
		SwingWorker<Void,Void> narysuj = new SwingWorker<Void, Void>(){

			@Override
			protected Void doInBackground() throws Exception {
				try {
					long pocz = System.currentTimeMillis();
					wykres.change(wykres.function, wykres.coords ,Graph.basic, 0.5);
					long kon = System.currentTimeMillis();
					System.out.println(kon - pocz + "ms  <-- czas obliczenia func we wszytkich pkt i jej pokazania");
					
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
					
					nadFunkcja.setForeground(Color.black);
					nadFunkcja.setText("Obliczono i pokazano funkcję.");
				}catch(Exception e) {
					e.printStackTrace();
				}
					return null;
			}
			
		};
		narysuj.execute();
		return narysuj;
	}
	
	private void changeFunc(FunctionPowloka f) {
		nadFunkcja.setForeground(Color.black);
		nadFunkcja.setTextAnimated("W trakcie obliczania funkcji");
			try {
				long pocz = System.currentTimeMillis();
				wykres.change(wykres.function, wykres.coords ,Graph.basic, 0.5);
				long kon = System.currentTimeMillis();
				System.out.println(kon - pocz + "ms  <-- czas obliczenia func we wszytkich pkt i jej pokazania");
				
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
				
				nadFunkcja.setForeground(Color.black);
				nadFunkcja.setText("Obliczono i pokazano funkcję.");
			}catch(Exception e) {
				e.printStackTrace();
			}
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

abstract class ActionListenerWthStop implements ActionListener{
	boolean stop = false;
}

class FunctionTextField extends JTextField{
	//opcja dodania listenera wyłapującego tylko zmiany dokonane przez użytkownika
	private int doneByProgramFlag = 0;
	//zapamiętuje czy ostatnia zmiana została wykonana przez użytkownika czy przez program
	public boolean isUpToDate;
	public FunctionTextField(String string) {
		super(string);
		isUpToDate = true;
		getDocument().addDocumentListener(new DocumentListener() {
			
			@Override
			public void removeUpdate(DocumentEvent e) {
				if(doneByProgramFlag>0) {
					doneByProgramFlag--;
					return;
				}
				isUpToDate = false;

			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {

				if(doneByProgramFlag>0) {
					doneByProgramFlag--;
					return;
				}
				isUpToDate = false;

			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				if(doneByProgramFlag>0) {
					doneByProgramFlag--;
					return;
				}
				isUpToDate = false;

			}
		});

	}
	@Override
	public void setText(String t) {
		doneByProgramFlag += 2;
		isUpToDate = true;
		super.setText(t);
	}
}

class LabelAboveFunction extends JLabel{
	Timer timer;
	
	public LabelAboveFunction(String text) {
		super(text);
	}
	
	@Override
	public void setText(String text) {
		if(timer != null)
			timer.stop();
		if(text.equals(getText())) {
			Thread t = new Thread() {
				
				@Override
				public void run() {
					try {
						long time = 200;
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								setFont(new Font(getFont().getName(), Font.PLAIN, getFont().getSize()));
							}
						});
						Thread.sleep(time);
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								setFont(new Font(getFont().getName(), Font.BOLD, getFont().getSize()));
							}
						});

					}catch(InterruptedException e) {
						e.printStackTrace();
					}
				};
			};
			t.run();
		}
		super.setText(text);
	}
	
	public void setTextAnimated(String text){
		ActionListenerWthStop timerListener = new ActionListenerWthStop() {
			static int liczKropki = 0;
			long timeOld = System.currentTimeMillis();
			long timeNew = System.currentTimeMillis();
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
					LabelAboveFunction.super.setText(text + kropki);
					liczKropki++;
					liczKropki %= 4;
					timeNew = System.currentTimeMillis();
					System.out.println(timeNew - timeOld + "  text");
					timeOld = timeNew; 
			}
		};
		if(timer != null)
			timer.stop();
		timer = new Timer(800, timerListener);
		timer.start();
	}
}
