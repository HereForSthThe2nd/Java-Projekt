package grafika;

import java.awt.BorderLayout;
import java.awt.CardLayout;
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
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Executable;
import java.util.LinkedList;
import java.util.Random;

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
import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import funkcja.FuncComp;
import funkcja.FuncMethods;
import funkcja.FuncWthName;
import funkcja.Function;
import funkcja.FunctionPowloka;
import funkcja.Functions;
import funkcja.IncorrectNameException;
import funkcja.Settings;
import funkcja.Functions.NameAndValue;
import funkcja.FunctionExpectedException;
import grafika.Graph.CmplxToColor;
import grafika.Main.TxtFieldForZes;
import inne.Complex;
import inne.KeyBeingPressed;
import inne.Numbers;
import inne.Runn;
import inne.ComplexPolyCurve;

public class Main extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6184366862447560671L;
	Graph wykres;
	MiniMap miniMap;
	FunctionTextField funkcjaTextField;
	boolean txtFuncUpToDate = false;
	Graph legenda;
	LabelAboveFunction nadFunkcja;
	
	JPanel zapisFun = new JPanel();
	JPanel wykresAndZap;
	JPanel containsTable;
	JTable tabZapisanychFunk;
	JTable tabZapisanychVar;
	JScrollPane scrlPnTablicaFunkcji;
	JScrollPane scrlPaneTablicaVar;
	JTextField funkcjaDoZap;
	
	JScrollPane scrollLeft;
	JTextField argument;
	JTextField wartosc;
	TxtFieldForZes lewDolnyTxt;
	TxtFieldForZes prawyGornyTxt;
	
	JCheckBox rysowanie;
	JTextArea calkaTxtArea;
	
	Settings ustawienia = new Settings();
	JCheckBoxMenuItem graphIsSquare;
	
	private Complex pressedRightAt;
	
	private Complex[] rogiBuffer = null;
	
	KeyBeingPressed kbp = new KeyBeingPressed(this);
	
	public Main() {
		doTempStuff();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setMinimumSize(new Dimension(600,500));
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		kbp.add("CONTROL", "ctrl");
		setLayout(new BorderLayout());
		try {
			BufferedImage buff = ImageIO.read(new File("logo.jpg"));
			setIconImage((new ImageIcon(buff)).getImage());
		} catch (IOException e) {
			e.printStackTrace();
		}

		doTheShortcuts();
		doTheMenu();
				
		try {
			legenda = new Graph(300, new FunctionPowloka("z", new Settings()), CoordsFactory.rect(new Complex(-4, -4), new Complex(4,4), 300, 300),Graph.basic ,0.5);
			wykres = new Graph(600, new FunctionPowloka("z", new Settings()), CoordsFactory.rect(new Complex(-1, -1), new Complex(1,1), 600, 600),Graph.basic ,0.5);
			wykres.setBackground(new Color(55,200,0));
		} catch (FunctionExpectedException e) {
			throw new IllegalStateException(e);
		}
		
		doAroundGraph();
		
		JPanel zawieraTextFunckcji = new JPanel();
		funkcjaTextField = new FunctionTextField("z");
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
			boolean finishedWthError = false;
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>(){
					FunctionPowloka funcTemp;
					@Override
					protected Void doInBackground() throws Exception {
						finishedWthError = false;
						try {
							wykres.function = new FunctionPowloka(e.getActionCommand(), ustawienia);
							if(wykres.function.nofArg() > 1) {
								nadFunkcja.setErrorText("Podana funkcja powinna zależeć od jednej zmiennej, a zależy od " + funcTemp.nofArg() + ".");
								return null;
							}
							funcTemp = wykres.function.removeDiff();
							int caretPosition = funkcjaTextField.getCaretPosition();
							funkcjaTextField.setText(wykres.function.write(ustawienia));
							if(caretPosition < funkcjaTextField.getText().length())
								funkcjaTextField.setCaretPosition(caretPosition);//funkcjaTextField.getText().length());
							else
								funkcjaTextField.setCaretPosition(funkcjaTextField.getText().length());
						} catch (FunctionExpectedException e1) {
							nadFunkcja.setErrorText(e1.messageForUser);
							finishedWthError = true;
						} catch(Exception e) {
							e.printStackTrace();
							nadFunkcja.setErrorText("Coś poszło nie tak podczas wczytywania funkcji.");
							finishedWthError = true;
						}
						return null;
					}
					@Override
					protected void done() {
						super.done();
						if(!finishedWthError)
							changeFunc(new Runnable() {
							@Override
							public void run() {
								nadFunkcja.setText("Obliczono i pokazano funkcję.");
							}
							
						}, funcTemp, 10);

					}
					
				};
				worker.execute();
			}
		});
		
		wykres.scM.setRunnable(new Runn<Void>() {
			@Override
			public Void run(int key) {
				long time= System.currentTimeMillis();
				System.out.println("runBegg");
				if(rogiBuffer != null) {
					setWykresBounds(rogiBuffer[0], rogiBuffer[1]);
					rogiBuffer = null;
				}
				else
					setWykresBounds(wykres.obraz.coords.getLD(), wykres.obraz.coords.getPG());
				wykres.setColor(key);
				System.out.println("run end  " + (System.currentTimeMillis() - time));
				return null;
			}
		});
		
		wykres.scM.whenDone(new Runnable() {
			@Override
			public void run() {
				System.out.println("repaint");
				
				wykres.repaint();
			}
		});
		
		wykres.scM.executeAfterDone(350, 2000, new Runnable() {
			
			@Override
			public void run() {
				changeFunc(1);
			}
		});
		
		wykres.obraz.addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				if(rogiBuffer == null)
					rogiBuffer = new Complex[] {wykres.obraz.coords.getLD(), wykres.obraz.coords.getPG()};
				wykres.changeM.stopAll();
				if(current != null)
					current.finish();
				if(kbp.isPressed("ctrl"))
					rogiBuffer = wykres.obraz.coords.powiekszenie(e.getPoint(), rogiBuffer[1], rogiBuffer[0], Math.pow(1.05, e.getWheelRotation()));
				else
					rogiBuffer = wykres.obraz.coords.powiekszenie(e.getPoint(), rogiBuffer[1], rogiBuffer[0], Math.pow(1.2, e.getWheelRotation()));
				//wykres.scM.keepLast(0);
				wykres.scM.doSlowAfter();
				wykres.scM.run();
			}
		});
		
		legenda.obraz.addMouseMotionListener(new MouseMotionListener() {
			
			@Override
			public void mouseMoved(MouseEvent e) {
				//Complex val = Complex.add(legenda.obraz.coords.getLD(), new Complex (e.getX()/rec.getWidth()*(legenda.prawyGorny.x-legenda.obraz.coords.getLD().x), (1-e.getY()/rec.getHeight())*(legenda.prawyGorny.y-legenda.obraz.coords.getLD().y)));
				Complex val = legenda.obraz.coords.pointToCmplx(e.getPoint());
				wartosc.setText(val.printE(3, 3));
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
				//Complex arg = Complex.add(wykres.obraz.coords.getLD(), new Complex (e.getX()/rec.getWidth()*(wykres.prawyGorny.x-wykres.obraz.coords.getLD().x), (1-e.getY()/rec.getHeight())*(wykres.prawyGorny.y-wykres.obraz.coords.getLD().y)));
				Complex arg = wykres.obraz.coords.pointToCmplx(e.getPoint());
				Complex val = wykres.getValueAt(e.getX(), e.getY());
				if(arg!=null)
					argument.setText(arg.printE(3, 3));
				else
					argument.setText("Jeszcze nie obliczone");
				if(val != null)
					wartosc.setText(val.printE(3, 3));
				else
					wartosc.setText("Jeszcze nie obliczone");
				wykres.foreGround.marker = arg;
				legenda.foreGround.marker = val;
				legenda.foreGround.repaint();
				wykres.foreGround.repaint();
			}
			
			@Override
			public void mouseDragged(MouseEvent e) {
				Rectangle rec = wykres.obraz.getBounds();
				if(0<=e.getX() && e.getX()<rec.getWidth() && 0<=e.getY() && e.getY()<rec.getHeight()) {
					if(SwingUtilities.isRightMouseButton(e)){
						//Complex[] nr = wykres.obraz.coords.przesuniecie(lastPressedRight, e.getPoint());
						//setWykresBounds(nr[0], nr[1]);

						
						if(rogiBuffer == null)
							rogiBuffer = new Complex[] {wykres.obraz.coords.getLD(), wykres.obraz.coords.getPG()};
						
						rogiBuffer = wykres.obraz.coords.przesuniecie(pressedRightAt, e.getPoint(), rogiBuffer[0], rogiBuffer[1]);
												
						wykres.scM.doSlowAfter();
						wykres.scM.run();
					}
					//Complex arg = Complex.add(wykres.obraz.coords.getLD(), new Complex (e.getX()/rec.getWidth()*(wykres.prawyGorny.x-wykres.obraz.coords.getLD().x), (1-e.getY()/rec.getHeight())*(wykres.prawyGorny.y-wykres.obraz.coords.getLD().y)));
					Complex arg = rogiBuffer == null ? wykres.obraz.coords.pointToCmplx(e.getPoint()) : wykres.obraz.coords.pointToCmplx(e.getPoint(), rogiBuffer[0], rogiBuffer[1]);
					Complex val = wykres.getValueAt(e.getX(), e.getY());
					if(arg != null)
						argument.setText(arg.printE(3, 3));
					if(val != null)
						wartosc.setText(val.printE(3, 3));
					wykres.foreGround.marker = arg;
					legenda.foreGround.marker = val;
					if(SwingUtilities.isLeftMouseButton(e)) {
						if(rysowanie.isSelected() && wykres.foreGround.krzywe.size() != 0) {
								wykres.foreGround.addPointToCurve(arg);
								legenda.foreGround.addPointToCurve(val);
								calkaTxtArea.setText(wykres.integralOfCurve().printE(2, 2));
						}
						else {
							if(wykres.foreGround.rect != null) {
								wykres.foreGround.rect[1] = wykres.obraz.coords.pointToCmplx( e.getPoint() );
								wykres.foreGround.rect = correctGraphBounds(wykres.foreGround.rect[0], wykres.foreGround.rect[1], false);
							}
						}
						wykres.foreGround.repaint();
						legenda.foreGround.repaint();
					}

				}
			}
		});
		wykres.obraz.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
				if(!rysowanie.isSelected() && wykres.foreGround.rect != null && SwingUtilities.isLeftMouseButton(e)) {
					setWykresBounds(wykres.foreGround.rect[0], wykres.foreGround.rect[1]);
					wykres.foreGround.rect = null;
					wykres.foreGround.szyba = new Color(0,0,0,50);
					wykres.foreGround.repaint();
					changeFunc(()->{
						nadFunkcja.setText("Obliczono i pokazano funkcję.");
						wykres.foreGround.szyba = new Color(0,0,0,0);
						wykres.foreGround.repaint();
					}, wykres.function, 10);
				}
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				if(SwingUtilities.isLeftMouseButton(e)) {
					if(rysowanie.isSelected()) {
						wykres.foreGround.addNewCurve();
						legenda.foreGround.addNewCurve();
					}
					else {
						wykres.foreGround.rect = new Complex[] {wykres.obraz.coords.pointToCmplx( e.getPoint()), wykres.obraz.coords.pointToCmplx( e.getPoint() )};
					}
				}
				if(SwingUtilities.isRightMouseButton(e)) {
					pressedRightAt = wykres.obraz.coords.pointToCmplx(e.getPoint());
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
		
		wykresAndZap = new JPanel();
		wykresAndZap.setLayout(new CardLayout());
		wykresAndZap.add(wykres, "wykres");
		wykresAndZap.add(zapisFun, "zapis");
		
		doZapisane();
		
		add(zawieraTextFunckcji, BorderLayout.NORTH);
		add(wykresAndZap, BorderLayout.CENTER);
		
		doTheLeft();

	}

	private void doAroundGraph() {
		miniMap = new MiniMap(wykres.obraz.coords);
		miniMap.setPreferredSize(new Dimension(120,130));
		
		JButton min = new JButton("[]");
		min.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				min.setText(scrollLeft.isVisible() ? "[ ]" : "[]");
				scrollLeft.setVisible(!scrollLeft.isVisible());
				revalidate();
			}
		});
		wykres.layout.add(min, CentralLayout.LEFT, 0, 0);
		wykres.layout.add(miniMap, CentralLayout.RIGHT, 5, 5);
	}
	
	private void doTempStuff() {
		
		JSlider temp = new JSlider(0, 50, 0);
		JButton temp2 = new JButton("anuluj");
		JTextField temp3 = new JTextField(10);
		JComboBox<String> gladkosc = new JComboBox<String>(new String[] {"Linowe", "Kwadratowe", "Beziera"});
		JLabel lab = new JLabel("Antialiasing");
		JCheckBox anti = new JCheckBox();
		if(ComplexPolyCurve.antialiasing)
			anti.setSelected(true);
		JTextField podz = new JTextField("" + CoordsFactory.accOfSmaller);
		podz.setColumns(7);
		JTextField outlineThickness = new JTextField(ComplexPolyCurve.outlineThickness+"");
		
		JFrame tempFrm = new JFrame();
		JPanel panel = new JPanel();
		panel.add(temp2);
		panel.add(temp);
		panel.add(temp3);
		panel.add(podz);
		panel.add(lab);
		panel.add(anti);
		panel.add(gladkosc);
		panel.add(outlineThickness);
		tempFrm.add(panel);
		
		temp.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				wykres.colorMapParams[0] = temp.getValue();
				wykres.scM.run();
				wykres.repaint();
			}
		});
		
		temp2.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				wykres.changeM.stopAll();
				if(current != null)
					current.finish();
			}
		});
		
		temp3.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					wykres.setRES(Integer.parseInt(temp3.getText()));
					changeFunc(10);
				} catch (NumberFormatException e1) {
					System.err.println(temp3.getText());
				}
			}
		});

		gladkosc.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				ComplexPolyCurve.S = gladkosc.getSelectedIndex()+1;
				wykres.foreGround.repaint();
			}
		});
		
		podz.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					CoordsFactory.accOfSmaller = Integer.parseInt(podz.getText());
					wykres.foreGround.repaint();
				}catch (NumberFormatException e1) {
					podz.setText("Nie udało się zamienić wpisanego tekstu a liczbę");
				}
			}
		});
		
		anti.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				ComplexPolyCurve.antialiasing = anti.isSelected();
				wykres.foreGround.repaint();
			}
		});
		
		outlineThickness.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					ComplexPolyCurve.outlineThickness = Float.parseFloat(outlineThickness.getText());
					wykres.foreGround.repaint();
					legenda.foreGround.repaint();
				}catch (NumberFormatException e1) {
					outlineThickness.setText("Nie udało się rozczytać z liczby");
				}
			}
		});		
		
		rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ctrl F3"), "technical");
		rootPane.getActionMap().put("technical", new AbstractAction() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 8874345292635390481L;

			@Override
		    public void actionPerformed(ActionEvent e) {
				tempFrm.setSize(300,300);
				tempFrm.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				tempFrm.setVisible(true);
		    }
		});
	}
	
	private void doTheShortcuts() {
		JComponent rootPane = getRootPane();
		Object rysowanieToggleKey = 0;
		rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ctrl R"), rysowanieToggleKey);
		rootPane.getActionMap().put(rysowanieToggleKey, new AbstractAction() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 657885571196520622L;

			@Override
			public void actionPerformed(ActionEvent e) {
				rysowanie.setSelected(!rysowanie.isSelected());
			}
			
		});
		rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ctrl W"), "usunKrzywe");
		rootPane.getActionMap().put("usunKrzywe", new AbstractAction() {
		    /**
			 * 
			 */
			private static final long serialVersionUID = 2887395622133930115L;

			@Override
		    public void actionPerformed(ActionEvent e) {
		    	wykres.foreGround.resetCurve();
		    	legenda.foreGround.resetCurve();
		    	calkaTxtArea.setText("---");
		    	repaint();
		    }
		});
		rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ctrl SLASH"), "przejdz do pola tekstowego");
		rootPane.getActionMap().put("przejdz do pola tekstowego", new AbstractAction() {
		    /**
			 * 
			 */
			private static final long serialVersionUID = -2021440080332044727L;

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

			/**
			 * 
			 */
			private static final long serialVersionUID = -990853042581357055L;

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
						doTheTables();
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
						doTheTables();
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
						doTheTables();
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
		JCheckBoxMenuItem osieLegendy = new JCheckBoxMenuItem("Osie");
		JRadioButtonMenuItem legendaCoTypowe = new JRadioButtonMenuItem("Normalna skala");
		JRadioButtonMenuItem legendaLogSkala = new JRadioButtonMenuItem("Moduł w skali logarytmicznej");
		JRadioButtonMenuItem legndaInf = new JRadioButtonMenuItem("Wokół nieksończoności");
		ButtonGroup legBG = new ButtonGroup();
		
		legBG.add(legendaCoTypowe);
		legBG.add(legndaInf);
		legBG.add(legendaLogSkala);
		legendaMenu.add(osieLegendy);
		legendaCoTypowe.setSelected(true);
		
		JCheckBoxMenuItem osieWykresu = new JCheckBoxMenuItem("Osie");
		graphIsSquare = new JCheckBoxMenuItem("Musi być kwadratem");
		JRadioButtonMenuItem wykresTyp = new JRadioButtonMenuItem("Normalna skala");
		JRadioButtonMenuItem wykresLogSkala = new JRadioButtonMenuItem("Moduł w skali logarytmicznej");
		JRadioButtonMenuItem wykresInf = new JRadioButtonMenuItem("Wokół nieskończoności");

		ButtonGroup wykBG = new ButtonGroup();
		wykBG.add(wykresTyp);
		wykBG.add(wykresInf);
		wykBG.add(wykresLogSkala);
		wykresTyp.setSelected(true);
		
		legendaMenu.add(legendaCoTypowe);
		legendaMenu.add(legendaLogSkala);
		legendaMenu.add(legndaInf);

		wykresMenu.add(osieWykresu);
		wykresMenu.add(graphIsSquare);
		wykresMenu.add(wykresTyp);
		wykresMenu.add(wykresLogSkala);
		wykresMenu.add(wykresInf);
		
		menuBar.add(wykresILegendaMenu);
		
		legendaCoTypowe.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				legenda.obraz.coords = legenda.rect(legenda.obraz.coords.getLD(), legenda.obraz.coords.getPG());
				legenda.changeM.run();
			}
		});
		
		legndaInf.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				legenda.obraz.coords = legenda.aroundInf(legenda.obraz.coords.getLD(), legenda.obraz.coords.getPG());
				legenda.changeM.run();
				}
		});
	
		legendaLogSkala.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				legenda.obraz.coords = legenda.logarithmic(legenda.obraz.coords.getLD(), legenda.obraz.coords.getPG());
				legenda.changeM.run();
			}
		});
		
		osieLegendy.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				legenda.foreGround.osie = osieLegendy.isSelected();
				legenda.foreGround.repaint();
			}
		});
		
		wykresTyp.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				wykres.obraz.coords = wykres.rect(wykres.obraz.coords.getLD(), wykres.obraz.coords.getPG());
				setWykresBounds(wykres.obraz.coords.getLD(), wykres.obraz.coords.getPG());
				changeFunc(10);
			}
		});
		
		wykresInf.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				wykres.obraz.coords = wykres.aroundInf(wykres.obraz.coords.getLD(), wykres.obraz.coords.getPG());
				setWykresBounds(wykres.obraz.coords.getLD(), wykres.obraz.coords.getPG());
				changeFunc(10);
			}
		});

		wykresLogSkala.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				wykres.obraz.coords = wykres.logarithmic(wykres.obraz.coords.getLD(), wykres.obraz.coords.getPG());
				setWykresBounds(wykres.obraz.coords.getLD(), wykres.obraz.coords.getPG());
				changeFunc(10);
			}
		});
		
		osieWykresu.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				wykres.foreGround.osie = osieWykresu.isSelected();
				wykres.foreGround.repaint();
			}
		});
		
		graphIsSquare.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Timer t = new Timer(10, new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						setWykresBounds(wykres.obraz.coords.getLD(), wykres.obraz.coords.getPG());
						changeFunc(10);
					}
				});
				//zrobione w taki sposób, bo jeśli za szybk się zrobi może nie zarejestrować, że już jest włączony checkbox
				t.setRepeats(false);
				t.start();
			}
		});
	}
	
	private void doZapisane() {
		JPanel gora = new JPanel();
		JPanel srodek = new JPanel();
		JButton zapisz = new JButton("Zapisz");
		JButton fCv = new JButton("Zmienne");
		JButton usun = new JButton("Usuń");
		
		JLabel funkNazwLab = new JLabel("Nazwa funkcji:");
		JTextField funkNazw = new JTextField();
		JLabel funkjaDoZapLab = new JLabel("Funckja:");
		funkcjaDoZap = new JTextField();
		funkcjaTextField.setConnected(funkcjaDoZap);
		funkNazw.setFont(new Font(funkNazw.getFont().getName(), Font.PLAIN, funkNazw.getFont().getSize()+5));
		funkNazw.setPreferredSize(new Dimension(70, funkNazw.getPreferredSize().height + 10));
		funkcjaDoZap.setFont(new Font(funkcjaDoZap.getFont().getName(), Font.PLAIN, funkcjaDoZap.getFont().getSize()+5));
		funkcjaDoZap.setPreferredSize(new Dimension(800, funkcjaDoZap.getPreferredSize().height + 10));
				
		gora.add(zapisz);
		gora.add(fCv);
		gora.add(usun);
		srodek.add(funkNazwLab);
		srodek.add(funkNazw);
		srodek.add(funkjaDoZapLab);
		srodek.add(funkcjaDoZap);
		
		containsTable = new JPanel();
		containsTable.setLayout(new CardLayout());
		
		doTheTables();
		
		funkcjaDoZap.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				zapisz.doClick();
			}
		});
		
		funkNazw.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				funkcjaDoZap.requestFocus();
				funkcjaDoZap.selectAll();
			}
		});
		
		zapisz.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					FunctionPowloka fp = new FunctionPowloka(funkcjaDoZap.getText(), ustawienia);
					FileOutputStream file;
					if(scrlPnTablicaFunkcji.isVisible()) {
						FunctionPowloka changed = fp.changeToNamed(funkNazw.getText());
						file = new FileOutputStream(Functions.zapisaneFunkcjePlik);
						DefaultTableModel model = (DefaultTableModel) tabZapisanychFunk.getModel();
						model.addRow(doARow((FuncWthName)changed.getFunction()));
					}else {
						FunctionPowloka changed = fp.changeToVar(funkNazw.getText());
						file = new FileOutputStream(Functions.zapisaneZmiennePlik);
						DefaultTableModel model = (DefaultTableModel) tabZapisanychVar.getModel();
						model.addRow(doARow((FuncWthName)changed.getFunction()));
					}
					ObjectOutputStream out = new ObjectOutputStream(file);
					out.writeObject(Functions.userFunctions);
					out.close();
					file.close();
					nadFunkcja.setText("Pomyślnie zapisano funkcję.");
				} catch (FunctionExpectedException e1) {
					nadFunkcja.setErrorText(e1.messageForUser);
				} catch (IncorrectNameException e1) {
					nadFunkcja.setErrorText(e1.messageForUser);
				}catch(FileNotFoundException e1) { 
					e1.printStackTrace();
				}catch (IOException e1) {
					e1.printStackTrace();
					JOptionPane.showMessageDialog(null, "Nie udało się zapisać funkcji.", "Błąd!", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		
		fCv.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				((CardLayout)containsTable.getLayout()).next(containsTable);
				if(scrlPnTablicaFunkcji.isVisible()) {
					fCv.setText("Zmienne");
					tabZapisanychVar.clearSelection();
				}
				else {
					fCv.setText("Funkcje");
					tabZapisanychFunk.clearSelection();
				}
			}
		});
		
		usun.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				for(int i : tabZapisanychFunk.getSelectedRows()) {
					String nazwa = (String) tabZapisanychFunk.getValueAt(i, 0);
					LinkedList<String> safe = Functions.chackIfSafeToRemove(nazwa);
					if(safe.size() != 0) {
						String listOfUnsafeFunc = "";
						for(String str : safe) {
							listOfUnsafeFunc += str + ", ";
						}
						int wybor = JOptionPane.showConfirmDialog(null, "Funkcje " + listOfUnsafeFunc + " zależą od funkcji " + nazwa+". Na pewno usunąć?", "Uwaga", JOptionPane.INFORMATION_MESSAGE);
						if(wybor == JOptionPane.NO_OPTION || wybor == JOptionPane.CANCEL_OPTION)
							return;
						Functions.expandAllSpecific(nazwa);
					}
					boolean jestBazowe = Functions.defaultFunctions.checkIfContained(nazwa);
					if(jestBazowe) {
						JOptionPane.showMessageDialog(null, "Funkcja "+nazwa+" jest wbudowana. Nie można jej usunąć.", "Błąd", JOptionPane.ERROR_MESSAGE);
						continue;
					}
					Functions.userFunctions.removeFunc(nazwa);
					try {
						FileOutputStream file = new FileOutputStream(Functions.zapisaneFunkcjePlik);
						ObjectOutputStream out = new ObjectOutputStream(file);
						out.writeObject(Functions.userFunctions);
						file.close();
						out.close();
					}catch (IOException e1) {
						JOptionPane.showMessageDialog(null, "Nie udało się usunąć pliku.", "Błąd", JOptionPane.ERROR_MESSAGE);
					}
				}
				for(int i : tabZapisanychVar.getSelectedRows()) {
					String nazwa = (String) tabZapisanychVar.getValueAt(i, 0);
					boolean jestBazowe = Functions.defaultVar.checkIfContained(nazwa);
					if(jestBazowe) {
						JOptionPane.showMessageDialog(null, "Funkcja "+nazwa+" jest wbudowana. Nie można jej usunąć.", "Błąd", JOptionPane.ERROR_MESSAGE);
						continue;
					}
					Functions.userVar.removeFunc(nazwa);
					try {
						FileOutputStream file = new FileOutputStream(Functions.zapisaneZmiennePlik);
						ObjectOutputStream out = new ObjectOutputStream(file);
						out.writeObject(Functions.userVar);
						file.close();
						out.close();
					}catch (IOException e1) {
						JOptionPane.showMessageDialog(null, "Nie udało się usunąć pliku.", "Błąd", JOptionPane.ERROR_MESSAGE);
					}
				}
				
				doTheTables();

			}
		});
		
		JPanel uwagi = new JPanel();
		
		JLabel czerwTlo = new JLabel("Kolor czerwony oznacza funkcję wbudowaną. Nie można ich usuwać.");
		JLabel zupelnosc = new JLabel("Prócz widocznych funkcji istnieją jeszcze pow{.}, oraz ln{.}");
		uwagi.add(czerwTlo);
		uwagi.add(zupelnosc);
		zapisFun.setLayout(new BoxLayout(zapisFun, BoxLayout.Y_AXIS));
		zapisFun.add(gora);
		zapisFun.add(srodek);
		zapisFun.add(containsTable);
		zapisFun.add(uwagi);
	}

	private void doTheTables() {
		containsTable.removeAll();
		
		tabZapisanychFunk = doATable(Functions.defaultFunctions, Functions.userFunctions);
		tabZapisanychVar = doATable(Functions.defaultVar, Functions.userVar);
		
		scrlPnTablicaFunkcji = new JScrollPane(tabZapisanychFunk);
		scrlPaneTablicaVar = new JScrollPane(tabZapisanychVar);
		
		containsTable.add(scrlPnTablicaFunkcji, "funkcje");
		containsTable.add(scrlPaneTablicaVar, "zmienne");
		
		containsTable.revalidate();
	}

	private void doTheLeft() {
		JPanel left = new JPanel();
		scrollLeft = new JScrollPane(left);
		//scroll.add(left);
		left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
		JPanel przyciski = new JPanel();
		przyciski.setLayout(new GridLayout(2, 2));
		JButton uprosc = new JButton("Uprość");
		uprosc.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
					
					SwingWorker<Void,Void> uprosc = new SwingWorker<Void, Void>(){
						boolean finishedWthError;
						@Override
						protected Void doInBackground() throws Exception {
							finishedWthError = false;
							try {
								nadFunkcja.setTextAnimated("Wczytywanie funkcji");
								FunctionPowloka fTemp = new FunctionPowloka(funkcjaTextField.getText(), ustawienia);
								nadFunkcja.setTextAnimated("Upraszczanie funckji");
								wykres.function = fTemp.simplify(ustawienia);
								funkcjaTextField.setText(wykres.function.write(ustawienia));
								return null;
							} catch (FunctionExpectedException e) {
								finishedWthError = true;
								nadFunkcja.setErrorText(e.messageForUser);
								return null;
							}
						}
						@Override
						protected void done() {
							super.done();
							if(!finishedWthError)
								changeFunc(10);
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
						boolean finisheWthErrors;
						@Override
						protected Void doInBackground() throws Exception {
							finisheWthErrors = false;
							try {
								nadFunkcja.setTextAnimated("Wczytywanie funkcji");
								FunctionPowloka f = new FunctionPowloka(funkcjaTextField.getText(), ustawienia);
								nadFunkcja.setTextAnimated("Rozbijanie funkcji");
								wykres.function = f.splitByRealAndImaginery(ustawienia);
								funkcjaTextField.setText(wykres.function.write(ustawienia));
							} catch (FunctionExpectedException e1) {
								nadFunkcja.setErrorText(e1.messageForUser);
							} catch(Exception e) {
								e.printStackTrace();
							}
							return null;
						}
						@Override
						protected void done() {
							super.done();
							if(!finisheWthErrors)
								changeFunc(10);
						}
					};
					rozbijFunc.execute();
			}
		});
		JButton expand = new JButton("Rozwiń funkcje złożone");
		JButton zapisaneF = new JButton("Zapisane");
		
		zapisaneF.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				((CardLayout)wykresAndZap.getLayout()).next(wykresAndZap);
				if(wykres.isVisible())
					zapisaneF.setText("Zapisz");
				else
					zapisaneF.setText("Wykres");
			}
		});
		
		expand.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					wykres.function = new FunctionPowloka(funkcjaTextField.getText(), ustawienia); 
					wykres.function = wykres.function.expand();
					funkcjaTextField.setText(wykres.function.write(ustawienia));
				} catch (FunctionExpectedException e1) {
					e1.printStackTrace();
				}
			}
		});
		
		przyciski.add(uprosc);
		przyciski.add(rzeczIUroj);
		przyciski.add(expand);
		przyciski.add(zapisaneF);
		
		JPanel lewStr = new JPanel();
		lewStr.setLayout(new BoxLayout(lewStr, BoxLayout.Y_AXIS));
		lewStr.setBorder(BorderFactory.createLineBorder(Color.blue, 2));
		
		BoundsTxt boundsTxt = new BoundsTxt();
		
		lewStr.add(boundsTxt);
		
		JComponent comp1;
		JComponent comp2;
		JPanel calaOpcja;

		calaOpcja = new JPanel();
		calaOpcja.setLayout(new BoxLayout(calaOpcja, BoxLayout.Y_AXIS));
		JPanel dolWybKoloru = new JPanel();
		comp1 = new JPanel ();
		comp1.add(new JLabel("Sposób pokolorowania legendy"));
	
		String[] nazwyKolorowan = new String[Graph.listaKolorowan.length];
		for(int i=0;i<nazwyKolorowan.length;i++)
			nazwyKolorowan[i] = Graph.listaKolorowan[i].name();
		JComboBox<String> colorCB= new JComboBox<String>(nazwyKolorowan);
		
		JComboBox<String> colorParams = new JComboBox<String>();
		
		for(int i=0;i<legenda.colorMap.paramsNames().length;i++) {
			colorParams.addItem(legenda.colorMap.paramsNames()[i]);	
		}
		
		JTextField paramValTxt = new JTextField(""+legenda.colorMapParams[colorParams.getSelectedIndex()]);
		paramValTxt.setColumns(6);
		paramValTxt.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					wykres.colorMapParams[colorParams.getSelectedIndex()] = Double.parseDouble(paramValTxt.getText());
				}catch (NumberFormatException e1) {
					nadFunkcja.setErrorText("Nie można rozczytać wartości ze wprowadzonej liczby.");
				}
				legenda.colorMapParams = wykres.colorMapParams;
				legenda.scM.run();
				wykres.scM.run();
				nadFunkcja.setText("Zmieniono kolor.");

			}
		});
		paramValTxt.addMouseWheelListener(new MouseWheelListener() {
			
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				wykres.scM.keepLast(1);
				SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>(){

					@Override
					protected Void doInBackground() throws Exception {
						double paramToChange = wykres.colorMapParams[colorParams.getSelectedIndex()];
						if(kbp.isPressed("ctrl")) {
							if(Math.abs(paramToChange) > 1)
								wykres.colorMapParams[colorParams.getSelectedIndex()] -= 0.1 * e.getWheelRotation();
							if(Math.abs(paramToChange) <= 1)
								wykres.colorMapParams[colorParams.getSelectedIndex()] = changeByStep(paramToChange, -e.getWheelRotation());
						}
						else {
							if(Math.abs(paramToChange) >= 10)
								wykres.colorMapParams[colorParams.getSelectedIndex()] = changeByStep(paramToChange, -e.getWheelRotation());
							if(Math.abs(paramToChange) < 10)
								wykres.colorMapParams[colorParams.getSelectedIndex()] -= 1 * e.getWheelRotation();
						}
						legenda.colorMapParams = wykres.colorMapParams;
						paramValTxt.setText(Numbers.toStr(wykres.colorMapParams[colorParams.getSelectedIndex()], 2, 2));
						wykres.scM.run();
						legenda.scM.run();
						nadFunkcja.setText("Zmieniono kolor.");
						return null;
					}
				};
				worker.execute();
			}
		});
		
		paramValTxt.setPreferredSize(new Dimension(40, paramValTxt.getPreferredSize().height));
		
		colorCB.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				int wybrane = ((JComboBox<String>)colorCB).getSelectedIndex();
				wykres.colorMap = Graph.listaKolorowan[wybrane];
				legenda.colorMap = Graph.listaKolorowan[wybrane];
				SwingUtilities.invokeLater(new Runnable() {
					
					@Override
					public void run() {
						colorParams.removeAllItems();
						wykres.colorMapParams = wykres.colorMap.defaultParams();
						wykres.scM.run();
						legenda.colorMapParams = legenda.colorMap.defaultParams();
						legenda.scM.run();
						for(int i=0;i<legenda.colorMap.paramsNames().length;i++) {
							colorParams.addItem(legenda.colorMap.paramsNames()[i]);	
						}
						wykres.repaint();
						legenda.repaint();
						paramValTxt.setText(wykres.colorMapParams[0]+"");
						nadFunkcja.setText("Zmieniono kolor");
					}
				});
			}
		});

		colorParams.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				int ind = colorParams.getSelectedIndex();
				if(ind != -1) {
					paramValTxt.setText(""+legenda.colorMapParams[colorParams.getSelectedIndex()]);
				}
			}
		});
		
		dolWybKoloru.add(colorCB);
		dolWybKoloru.add(colorParams);
		dolWybKoloru.add(paramValTxt);
		
		calaOpcja.add(comp1);
		calaOpcja.add(dolWybKoloru);
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
		
		lewStr.add(calaOpcja);
		
		comp2= new JButton("Zapisz wykres");
		((JButton)comp2).addActionListener(new ActionListener() {
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
					}
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(null, "Nie udało się zapisać pliku.", "Błąd!", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		
		lewStr.add(comp2);
		
		calaOpcja = new JPanel();
		comp1 = new JLabel("Całka po krzywej: ");
		calkaTxtArea = new JTextArea("---");
		calkaTxtArea.setPreferredSize(new Dimension(200, calkaTxtArea.getPreferredSize().height));
		calkaTxtArea.setMaximumSize(getPreferredSize());
		calkaTxtArea.setEditable(false);
		calaOpcja.add(comp1);
		calaOpcja.add(calkaTxtArea);
		lewStr.add(calaOpcja);
		
		
		lewStr.add(new JLabel("Legenda:"));
		
		lewStr.add(legenda);
		add(scrollLeft, BorderLayout.WEST);

		
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
		
		left.add(Box.createRigidArea(new Dimension(0,5)));
		left.add(przyciski);
		left.add(Box.createRigidArea(new Dimension(0,5)));
		left.add(lewStr);
		
	}
	
	private void setWykresBounds(Complex z1, Complex z2){
		//Complex[] newBounds = correctGraphBounds(z1, z2, true);
		//z1 = newBounds[0];
		//z2 = newBounds[1];
		wykres.obraz.coords.setLD(z1);
		wykres.obraz.coords.setPG(z2);
		lewDolnyTxt.setZesp(z1);
		prawyGornyTxt.setZesp(z2);
		miniMap.setArea(wykres.obraz.coords);
	}
	
	private JTable doATable(NameAndValue def, NameAndValue user) {
		String header[] = new String[] {"Nazwa", "Ilość argómentów", "Funkcja bazowa"};
		Object[][] dataFunc = new Object[def.size() + user.size()][3];
		for(int i=0;i<def.size();i++) {
			dataFunc[i] = doARow(def.getValues().get(i));
		}
		int d = def.size();
		for(int i=0;i<user.size();i++) {
			dataFunc[i+d] = doARow(user.getValues().get(i));
		}		
		JTable tabZapisanych = new JTable(new DefaultTableModel(dataFunc, header));
		tabZapisanych.getColumnModel().getColumn(0).setPreferredWidth(100);
		tabZapisanych.getColumnModel().getColumn(1).setPreferredWidth(110);
		tabZapisanych.getColumnModel().getColumn(2).setPreferredWidth(1000);
		
		tabZapisanych.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
		    /**
			 * 
			 */
			private static final long serialVersionUID = 1684200323320088826L;

			@Override
		    public Component getTableCellRendererComponent(JTable table, Object value,
		            boolean isSelected, boolean hasFocus, int row, int column) {
		        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		        if(row >= def.size()) {
			        if (!isSelected)
			            c.setBackground(row % 2 == 0 ? new Color(220,220,220) : Color.WHITE);
			        if(isSelected)
			            c.setBackground(row % 2 == 0 ? new Color(164,187,209) : new Color(184,207,229));
		        }else {
			        if (!isSelected)
			            c.setBackground(row % 2 == 0 ? new Color(220,180,180) : new Color(255,200,200));
			        if(isSelected)
			            c.setBackground(row % 2 == 0 ? new Color(169,140,189) : new Color(189,145,165));

		        }
		        return c;
		    }

		});
		return tabZapisanych;

	}
	
	private Object[] doARow(FuncWthName f) {
		try {
			return new Object[] {f.name, f.nofArg, f.expand().putArguments(FuncMethods.returnIdentities(f.nofArg)).write(ustawienia)};
		} catch (FunctionExpectedException e) {
			e.printStackTrace();
			throw new IllegalStateException();
		}
	}

	WorkerWthFinish<Void,Void> current;
	int timeIndTemp=0;
	private WorkerWthFinish<Void, Void> changeFunc(Runnable r, FunctionPowloka f, int podzial) {
		//int thisInd = timeIndTemp++;
		//nadFunkcja.setForeground(Color.black);
		//nadFunkcja.setTextAnimated("W trakcie obliczania funkcji");
		WorkerWthFinish<Void,Void> narysuj = new WorkerWthFinish<Void, Void>(){
			boolean finish = false;
			@Override
			protected Void doInBackground() {
				try {
					wykres.PODZIALRYSOWANIA = podzial;
					wykres.function = f;
					wykres.changeM.run();
					if(finish)
						return null;
					legenda.foreGround.resetCurve();
					for(ComplexPolyCurve krzywa : wykres.foreGround.krzywe) {
						legenda.foreGround.addNewCurve(krzywa.image(f));
					}
					calkaTxtArea.setText(wykres.integralOfCurve().printE(2, 2));
					//legenda.repaint();
				}catch(Exception e) {
					e.printStackTrace();
				}
					return null;
			}
			@Override
			protected void done() {
				super.done();
				legenda.foreGround.repaint();
				r.run();
				if(executeWhenDone != null)
					executeWhenDone.run();
			}
			@Override
			public void finish() {
				finish = true;
			}			
		};
		synchronized(this) {
			if(current != null && !current.isDone()) {
				current.executeWhenDone = new Runnable() {
					
					@Override
					public void run() {
						narysuj.execute();
						current = narysuj;
					}
				};
				wykres.changeM.stopAll();
				current.finish();
			}else {
				current = narysuj;
				current.execute();
			}
		}
		return narysuj;
	}
	
 	private SwingWorker<Void, Void> changeFunc(int podzial) {
 		return changeFunc(() -> {
 			nadFunkcja.setForeground(Color.black);
 			nadFunkcja.setText("Obliczono i pokazano funkcję.");
 		}, wykres.function, podzial);
	}

 	private double changeByStep(double d, int step) {
 		return d + step * Numbers.lesserWholeOf(d) / 10;
 	}
 	
 	private Complex[] correctGraphBounds(Complex z1, Complex z2, boolean changeOrder) {
 		if(changeOrder) {
	 		Complex zMin = new Complex(Math.min(z1.x, z2.x), Math.min(z1.y, z2.y));
	 		Complex zMax = new Complex(Math.max(z1.x, z2.x), Math.max(z1.y, z2.y));
	 		if(graphIsSquare.isSelected()) {
		 		double bok1 = z2.x - z1.x;
		 		double bok2 = z2.y - z1.y;
		 		double bok = Math.min(bok1, bok2);
		 		zMax = Complex.add(zMin, new Complex(bok,bok));
	 		}
	 		return new Complex[] {zMin, zMax};
 		}
 		if(graphIsSquare.isSelected()) {
	 		double bok1 = z2.x - z1.x;
	 		double bok2 = z2.y - z1.y;
	 		double bok = Math.min(Math.abs(bok1), Math.abs(bok2));
	 		z2 = Complex.add(z1, new Complex(bok * (bok1 < 0? -1:1),bok * (bok2 < 0 ? -1 : 1)));
 		}
 		return new Complex[] {z1, z2};

 		
 		
 	}
 	/*
	private void changeFunc(FunctionPowloka f) {
		nadFunkcja.setForeground(Color.black);
		nadFunkcja.setTextAnimated("W trakcie obliczania funkcji");
			try {
				wykres.change(f, wykres.obraz.coords ,wykres.colorMap, wykres.colorMapParams);
				
				legenda.foreGround.resetCurve();
				for(LinkedList<Complex> krzywa : wykres.foreGround.krzywa) {
					legenda.foreGround.addNewCurve();
					for(Complex z : krzywa) {
						//System.out.println(p);
						//System.out.println(wykres.getValueAt(p));
						Point p = wykres.obraz.coords.cmplxToPoint( z );
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
	*/
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				try {
					Main main = new Main();
					main.setVisible(true);
					//main.doTempStuff().setVisible(true);
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	class BoundsTxt extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1692883746308365709L;
		private FocusListener whenLosesFocus = new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {
				
				//if(!prawyGornyTxt.rzecz.hasFocus() && ! lewDolnyTxt.rzecz.hasFocus() && !lewDolnyTxt.ur.hasFocus() && !prawyGornyTxt.ur.hasFocus()) {
				if(e.getOppositeComponent() != null && 	!(e.getOppositeComponent().equals(lewDolnyTxt.rzecz) || e.getOppositeComponent().equals(lewDolnyTxt.ur) ||
						e.getOppositeComponent().equals(prawyGornyTxt.rzecz) || e.getOppositeComponent().equals(prawyGornyTxt.ur))) {
					lewDolnyTxt.setZesp(wykres.obraz.coords.getLD());
					prawyGornyTxt.setZesp(wykres.obraz.coords.getPG());
					prawyGornyTxt.setEditable(false);
					lewDolnyTxt.setEditable(false);
				}
			}
			
			@Override
			public void focusGained(FocusEvent e) {
				// TODO Auto-generated method stub
				
			}
		};
		
		public BoundsTxt() {
			Complex z1 = wykres.obraz.coords.getLD().clone();
			Complex z2 = wykres.obraz.coords.getPG().clone();
			JPanel txt = new JPanel();
			txt.setLayout(new BoxLayout(txt, BoxLayout.Y_AXIS));
			//obszarWykres.add(new JLabel("róg lewy dolny"));
			//obszarWykres.add(new JLabel("róg prawy górny"));
			lewDolnyTxt = new TxtFieldForZes(wykres.obraz.coords.getLD(), "Róg lewy dolny:");
			prawyGornyTxt = new TxtFieldForZes(wykres.obraz.coords.getPG(), "Róg prawy górny:");
			
			JButton zmienGranice = new JButton("Zmień obszar");
			
			txt.add(lewDolnyTxt);
			txt.add(prawyGornyTxt);
			add(txt);
			add(zmienGranice);
			
			lewDolnyTxt.setEditable(false);
			prawyGornyTxt.setEditable(false);

			zmienGranice.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					lewDolnyTxt.setEditable(true);
					prawyGornyTxt.setEditable(true);
					lewDolnyTxt.rzecz.requestFocus();
					lewDolnyTxt.rzecz.selectAll();
				}
			});
			
			lewDolnyTxt.rzecz.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						z1.x = Double.parseDouble(lewDolnyTxt.rzecz.getText());
						lewDolnyTxt.setZesp(new Complex(z1.x, lewDolnyTxt.getWart().y));
						lewDolnyTxt.ur.requestFocus();
						lewDolnyTxt.ur.selectAll();

					}catch(NumberFormatException e1) {
						nadFunkcja.setErrorText("Wpisana wartość nie mogła zostać zamieniona na liczbę.");
						lewDolnyTxt.setZesp(lewDolnyTxt.getWart());
						lewDolnyTxt.rzecz.selectAll();
					}
		
		
				}
			});
			lewDolnyTxt.ur.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						z1.y = Double.parseDouble(lewDolnyTxt.ur.getText());
						prawyGornyTxt.rzecz.requestFocus();
						prawyGornyTxt.rzecz.selectAll();
						lewDolnyTxt.setZesp(new Complex(lewDolnyTxt.getWart().x, z1.y));
					}catch(NumberFormatException e1) {
						lewDolnyTxt.setZesp(lewDolnyTxt.getWart());
						lewDolnyTxt.ur.selectAll();
						nadFunkcja.setErrorText("Wpisana wartość nie mogła zostać zamieniona na liczbę.");
					}
				}
			});
			prawyGornyTxt.rzecz.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						z2.x = Double.parseDouble(prawyGornyTxt.rzecz.getText());
						prawyGornyTxt.setZesp(new Complex(z2.x, prawyGornyTxt.getWart().y));
						prawyGornyTxt.ur.requestFocus();
						prawyGornyTxt.ur.selectAll();
					}catch(NumberFormatException e1) {
						nadFunkcja.setErrorText("Wpisana wartość nie mogła zostać zamieniona na liczbę.");
						prawyGornyTxt.setZesp(prawyGornyTxt.getWart());
						prawyGornyTxt.rzecz.selectAll();
					}
				}
			});
			prawyGornyTxt.ur.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						z2.y = Double.parseDouble(prawyGornyTxt.ur.getText());
						lewDolnyTxt.setEditable(false);
						prawyGornyTxt.setEditable(false);
						setWykresBounds(z1, z2);
						changeFunc(10);
					}catch(NumberFormatException e1) {
						nadFunkcja.setErrorText("Wpisana wartość nie mogła zostać zamieniona na liczbę.");
						lewDolnyTxt.setZesp(lewDolnyTxt.getWart());
						prawyGornyTxt.ur.selectAll();
					}
				}
			});
			lewDolnyTxt.rzecz.addFocusListener(whenLosesFocus);
			lewDolnyTxt.ur.addFocusListener(whenLosesFocus);
			prawyGornyTxt.rzecz.addFocusListener(whenLosesFocus);
			prawyGornyTxt.ur.addFocusListener(whenLosesFocus);
		}
		
	}

	
	static class TxtFieldForZes extends JPanel{
		/**
		 * 
		 */
		private static final long serialVersionUID = -7572209176711303733L;
		JTextField rzecz;
		JTextField ur;
		private Complex wart;
		public TxtFieldForZes(Complex z, String lab) {
			wart = z;
			rzecz = new JTextField(Numbers.toStr(z.x, 2, 2));
			ur = new JTextField(Numbers.toStr(z.y, 2, 2));
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
			rzecz.setText(Numbers.toStr(wart.x, 2, 2));
			ur.setText(Numbers.toStr(wart.y, 2, 2));
			
		}
		
		public void setEditable(boolean value) {
			rzecz.setEditable(value);
			ur.setEditable(value);
		}
		
		public Complex getWart(){
			return wart;
		}
		
	}
}

abstract class ActionListenerWthStop implements ActionListener{
	boolean stop = false;
}
