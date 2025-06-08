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
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Executable;
import java.util.LinkedList;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.ImageIcon;
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
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import Inne.Complex;
import funkcja.Function;
import funkcja.FunctionPowloka;
import funkcja.Settings;
import funkcja.TimeKeeping;
import funkcja.FunctionExpectedException;
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
	TxtFieldForZes lewDolnyTxt;
	TxtFieldForZes prawyGornyTxt;
	JCheckBox rysowanie;
	JTextArea calkaTxtArea;
	Settings ustawienia = new Settings();
	
	public Main() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setMinimumSize(new Dimension(600,500));
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		setLayout(new BorderLayout());
		//ImageIcon a = new 
		try {
			BufferedImage buff = ImageIO.read(new File("logo.jpg"));
			setIconImage((new ImageIcon(buff)).getImage());
		} catch (IOException e) {
			e.printStackTrace();
		}
		doTheShortcuts();
		doTheMenu();
		try {
			legenda = new Graph(300);
			wykres = new Graph(600);
			wykres.setBackground(new Color(55,200,0));
			wykres.function = new FunctionPowloka("z^2", new Settings());
			legenda.change(new FunctionPowloka("z", new Settings()), legenda.rect(new Complex(-10,-10), new Complex(10,10)),Graph.basic ,0.5);
			wykres.change(wykres.function, wykres.rect(new Complex(-3,-3), new Complex(3,3)),Graph.basic, 0.5);
		} catch (FunctionExpectedException e) {
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
							FunctionPowloka funcTemp = new FunctionPowloka(e.getActionCommand(), ustawienia);
							if(funcTemp.nofArg() > 1) {
								nadFunkcja.setErrorText("Podana funkcja powinna zależeć od jednej zmiennej, a zależy od " + funcTemp.nofArg() + ".");
								return null;
							}
							wykres.function = funcTemp;
							changeFunc(wykres.function.removeDiff());
							wykres.function = funcTemp;
							int caretPosition = funkcjaTextField.getCaretPosition();
							funkcjaTextField.setText(wykres.function.write(ustawienia));
							if(caretPosition < funkcjaTextField.getText().length())
								funkcjaTextField.setCaretPosition(caretPosition);//funkcjaTextField.getText().length());
							else
								funkcjaTextField.setCaretPosition(funkcjaTextField.getText().length());
						} catch (FunctionExpectedException e1) {
							nadFunkcja.setErrorText(e1.messageForUser);
						} catch(Exception e) {
							e.printStackTrace();
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
				//Complex arg = Complex.add(wykres.coords.getLD(), new Complex (e.getX()/rec.getWidth()*(wykres.prawyGorny.x-wykres.coords.getLD().x), (1-e.getY()/rec.getHeight())*(wykres.prawyGorny.y-wykres.coords.getLD().y)));
				Complex arg = wykres.coords.pointToCmplx(e.getPoint());
				Complex val = wykres.getValueAt(e.getX(), e.getY());
				if(arg!=null)
					argument.setText(arg.printE(2, 2));
				else
					argument.setText("Jeszcze nie obliczone");
				if(val != null)
					wartosc.setText(val.printE(2, 2));
				else
					wartosc.setText("Jeszcze nie obliczone");
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
							calkaTxtArea.setText(wykres.integralOfCurve().printE(2, 2));
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
					Complex lewyDolny =  new Complex(
						wykres.foreGround.rect[0].x < wykres.foreGround.rect[1].x ? 
						wykres.foreGround.rect[0].x : wykres.foreGround.rect[1].x,
						wykres.foreGround.rect[0].y < wykres.foreGround.rect[1].y ? 
						wykres.foreGround.rect[0].y : wykres.foreGround.rect[1].y);
					Complex prawyGorny = new Complex(
							wykres.foreGround.rect[0].x > wykres.foreGround.rect[1].x ? 
							wykres.foreGround.rect[0].x : wykres.foreGround.rect[1].x,
							wykres.foreGround.rect[0].y > wykres.foreGround.rect[1].y ? 
							wykres.foreGround.rect[0].y : wykres.foreGround.rect[1].y);
					setWykresBounds(lewyDolny, prawyGorny);
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
		    	calkaTxtArea.setText("---");
		    	repaint();
		    }
		});
		rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("SLASH"), "przejdz do pola tekstowego");
		rootPane.getActionMap().put("przejdz do pola tekstowego", new AbstractAction() {
		    @Override
		    public void actionPerformed(ActionEvent e) {
		    	if(funkcjaTextField.isFocusOwner())
		    		return;
		    	Timer timer = new Timer(10, new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						funkcjaTextField.setEnabled(true);
				    	funkcjaTextField.selectAll();
					}
				});
		    	timer.setRepeats(false);
				funkcjaTextField.setEnabled(false);
		    	timer.start();
		    	funkcjaTextField.requestFocus();
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
					try {
						funkcjaTextField.setText(wykres.function.write(ustawienia));
					} catch (FunctionExpectedException e1) {
						nadFunkcja.setErrorText(e1.messageForUser);;
					}
			}
		});
		ladneStale.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				ustawienia.writeNeatVar = ladneStale.isSelected();
				if(funkcjaTextField.isUpToDate)
					try {
						funkcjaTextField.setText(wykres.function.write(ustawienia));
					} catch (FunctionExpectedException e1) {
						nadFunkcja.setErrorText(e1.messageForUser);
					}
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
					try {
						funkcjaTextField.setText(wykres.function.write(ustawienia));
					} catch (FunctionExpectedException e1) {
						nadFunkcja.setErrorText(e1.messageForUser);
					}
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
				changeFunc();
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
				changeFunc();
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
								nadFunkcja.setTextAnimated("Wczytywanie funkcji");
								wykres.function = new FunctionPowloka(funkcjaTextField.getText(), ustawienia);
								nadFunkcja.setTextAnimated("Upraszczanie funckji");
								FunctionPowloka fch = wykres.function.simplify(ustawienia);
								funkcjaTextField.setText(fch.write(ustawienia));
								changeFunc(fch);
								return null;
							} catch (FunctionExpectedException e) {
								nadFunkcja.setErrorText(e.messageForUser);
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
								nadFunkcja.setTextAnimated("Wczytywanie funkcji");
								FunctionPowloka f = new FunctionPowloka(funkcjaTextField.getText(), ustawienia);
	
								nadFunkcja.setTextAnimated("Rozbijanie funkcji");
								wykres.function = f.splitByRealAndImaginery(ustawienia);
								funkcjaTextField.setText(wykres.function.write(ustawienia));
								changeFunc();
							} catch (FunctionExpectedException e1) {
								nadFunkcja.setErrorText(e1.messageForUser);
							} catch(Exception e) {
								e.printStackTrace();
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
		
		JPanel obszarWykres = new JPanel();
		obszarWykres.setLayout(new GridLayout(2,2));
		//obszarWykres.add(new JLabel("róg lewy dolny"));
		//obszarWykres.add(new JLabel("róg prawy górny"));
		lewDolnyTxt = new TxtFieldForZes(wykres.coords.getLD(), "Róg lewy dolny:");
		prawyGornyTxt = new TxtFieldForZes(wykres.coords.getPG(), "Róg prawy górny:");
		lewDolnyTxt.rzecz.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					double xn = Double.parseDouble(lewDolnyTxt.rzecz.getText());
					setWykresBounds(new Complex(xn, lewDolnyTxt.getWart().y), prawyGornyTxt.getWart());
					if(xn >= prawyGornyTxt.getWart().x)
						nadFunkcja.setWarningText("Część rzeczywista lewego dolnego rogu powinna być mniejsza od części rzeczywistej prawego górnego rogu.");
					changeFunc(new Runnable() {
						
						@Override
						public void run() {
							if(xn >= prawyGornyTxt.getWart().x)
								nadFunkcja.setWarningText("Część rzeczywista lewego dolnego rogu powinna być mniejsza od części rzeczywistej prawego górnego rogu. Obliczono funkcję.");
						}
					});
					lewDolnyTxt.ur.requestFocus();
					lewDolnyTxt.ur.selectAll();
				}catch(NumberFormatException e1) {
					nadFunkcja.setErrorText("Wpisana wartość nie mogła zostać zamieniona na liczbę.");
					lewDolnyTxt.setZesp(lewDolnyTxt.getWart());
				}
			}
		});
		lewDolnyTxt.ur.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					double yn = Double.parseDouble(lewDolnyTxt.ur.getText());
					setWykresBounds(new Complex(lewDolnyTxt.getWart().x, yn), prawyGornyTxt.getWart());
					if(yn >= prawyGornyTxt.getWart().y)
						nadFunkcja.setWarningText("Część urojona lewego dolnego rogu powinna być mniejsza od części urojonej prawego górnego rogu.");
					changeFunc(new Runnable() {
						
						@Override
						public void run() {
							if(yn >= prawyGornyTxt.getWart().y)
								nadFunkcja.setWarningText("Część urojona lewego dolnego rogu powinna być mniejsza od części urojonej prawego górnego rogu. Obliczono funkcję.");
						}
					});
					prawyGornyTxt.rzecz.requestFocus();
					prawyGornyTxt.rzecz.selectAll();
				}catch(NumberFormatException e1) {
					nadFunkcja.setErrorText("Wpisana wartość nie mogła zostać zamieniona na liczbę.");
					lewDolnyTxt.setZesp(lewDolnyTxt.getWart());
				}
			}
		});
		prawyGornyTxt.rzecz.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					double xn = Double.parseDouble(prawyGornyTxt.rzecz.getText());
					setWykresBounds(lewDolnyTxt.getWart(), new Complex(xn, prawyGornyTxt.getWart().y));
					if(xn <= lewDolnyTxt.getWart().x)
						nadFunkcja.setWarningText("Część rzeczywista prawego górnego rogu powinna być większa od części rzeczywistej lewego  dolnego rogu.");
					changeFunc(new Runnable() {
						
						@Override
						public void run() {
							if(xn <= lewDolnyTxt.getWart().x)
								nadFunkcja.setWarningText("Część rzeczywista prawego górnego rogu powinna być większa od części rzeczywistej lewego  dolnego rogu. Obliczono funkcję.");							
						}
					});
					prawyGornyTxt.ur.requestFocus();
					prawyGornyTxt.ur.selectAll();
				}catch(NumberFormatException e1) {
					nadFunkcja.setErrorText("Wpisana wartość nie mogła zostać zamieniona na liczbę.");
					prawyGornyTxt.setZesp(prawyGornyTxt.getWart());
				}
			}
		});
		prawyGornyTxt.ur.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					double yn = Double.parseDouble(prawyGornyTxt.ur.getText());
					if(yn <= lewDolnyTxt.getWart().y)
						nadFunkcja.setWarningText("Część urojona prawego górnego rogu powinna być większa od części urojonej lewego dolnego rogu.");
					setWykresBounds(lewDolnyTxt.getWart(), new Complex(prawyGornyTxt.getWart().x, yn));
					changeFunc(new Runnable() {
						
						@Override
						public void run() {
							if(yn <= lewDolnyTxt.getWart().y)
								nadFunkcja.setWarningText("Część urojona prawego górnego rogu powinna być większa od części urojonej lewego dolnego rogu. Obliczono funkcję.");
						}
					});
				}catch(NumberFormatException e1) {
					nadFunkcja.setErrorText("Wpisana wartość nie mogła zostać zamieniona na liczbę.");
					lewDolnyTxt.setZesp(lewDolnyTxt.getWart());
				}
			}
		});

		obszarWykres.add(lewDolnyTxt);
		obszarWykres.add(prawyGornyTxt);
		lewStr.add(obszarWykres);
		
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
				calkaTxtArea.setText("---");
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
						       return "PNG"
						       		+ " files (*.png)";
						   }

						   public boolean accept(File f) {
						       if (f.isDirectory()) {
						           return true;
						       } else {
						           String filename = f.getName().toLowerCase();
						           return filename.endsWith(".png");
						       }
						   }
						});
					
					int pot = ch.showSaveDialog(null);
					if(pot == JFileChooser.APPROVE_OPTION) {
						if(ch.getSelectedFile().getPath().endsWith(".png")){
							wykres.save(new File(ch.getSelectedFile().getPath()));
							return;
						}
						wykres.save(new File(ch.getSelectedFile()+".png"));
						System.out.println("nic nie znalazło");
					}
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(null, "Nie udało się zapisać pliku.", "Błąd!", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		
		lewStr.add(wybor);
		
		calaOpcja = new JPanel();
		opcja = new JLabel("Całka po krzywej: ");
		calkaTxtArea = new JTextArea("---");
		calkaTxtArea.setPreferredSize(new Dimension(200, calkaTxtArea.getPreferredSize().height));
		calkaTxtArea.setMaximumSize(getPreferredSize());
		calkaTxtArea.setEditable(false);
		calaOpcja.add(opcja);
		calaOpcja.add(calkaTxtArea);
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
	
	private void setWykresBounds(Complex z1, Complex z2){
		wykres.coords.setLD(z1);
		wykres.coords.setPG(z2);
		lewDolnyTxt.setZesp(z1);
		prawyGornyTxt.setZesp(z2);
	}
	
 	private SwingWorker<Void, Void> changeFunc(Runnable r) {
		nadFunkcja.setForeground(Color.black);
		nadFunkcja.setTextAnimated("W trakcie obliczania funkcji");
		SwingWorker<Void,Void> narysuj = new SwingWorker<Void, Void>(){

			@Override
			protected Void doInBackground() throws Exception {
				try {
					wykres.change(wykres.function, wykres.coords ,Graph.basic, 0.5);
					
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
					calkaTxtArea.setText(wykres.integralOfCurve().printE(2, 2));
					legenda.repaint();
				}catch(Exception e) {
					e.printStackTrace();
				}
					return null;
			}
			@Override
			protected void done() {
				super.done();
				r.run();
			}
			
		};
		narysuj.execute();
		return narysuj;
	}
	
 	private SwingWorker<Void, Void> changeFunc() {
 		return changeFunc(() -> {
 			nadFunkcja.setText("Obliczono i pokazano funkcję.");
 		});
	}
 	
	private void changeFunc(FunctionPowloka f) {
		nadFunkcja.setForeground(Color.black);
		nadFunkcja.setTextAnimated("W trakcie obliczania funkcji");
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

	static class TxtFieldForZes extends JPanel{
		JTextField rzecz;
		JTextField ur;
		private Complex wart;
		public TxtFieldForZes(Complex z, String lab) {
			wart = z;
			rzecz = new JTextField(Complex.toStr(z.x, 2, 2));
			ur = new JTextField(Complex.toStr(z.y, 2, 2));
			rzecz.setPreferredSize(new Dimension(70, rzecz.getPreferredSize().height+5));
			ur.setPreferredSize(new Dimension(70, ur.getPreferredSize().height+5));
			JLabel srod = new JLabel(" + i");
			srod.setFont(new Font(getFont().getName(), Font.BOLD, getFont().getSize()));
			add(new JLabel(lab));
			add(rzecz);
			add(srod);
			add(ur);
		}
		
		public void setZesp(Complex z) {
			wart = z;
			rzecz.setText(Complex.toStr(wart.x, 2, 2));
			ur.setText(Complex.toStr(wart.y, 2, 2));
			
		}
				
		public Complex getWart(){
			return wart;
		}
		
	}
}

abstract class ActionListenerWthStop implements ActionListener{
	boolean stop = false;
}
