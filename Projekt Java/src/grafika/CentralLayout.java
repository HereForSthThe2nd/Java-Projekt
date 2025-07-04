package grafika;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serial;
import java.util.ArrayList;

import javax.swing.JLayeredPane;
import javax.swing.JPanel;

public class CentralLayout implements LayoutManager {

	public final static int CENTER = 0,
			RIGHT = 1,
			LEFT = 2;			
	
	private Container parent;
	private ArrayList<Integer> central = new ArrayList<Integer>();
	private ArrayList<Integer> left = new ArrayList<Integer>();
	private ArrayList<Integer> right = new ArrayList<Integer>();
	private ArrayList<Integer> xPadL = new ArrayList<Integer>();
	private ArrayList<Integer> yPadL = new ArrayList<Integer>();
	private ArrayList<Integer> xPadR = new ArrayList<Integer>();
	private ArrayList<Integer> yPadR = new ArrayList<Integer>();
	
	private JPanel tempLine1, tempLine2;
	
	private Dimension prefferedSize = new Dimension(100,100);
	
	public CentralLayout(Container parent) {
		this.parent = parent;
		tempLine1 = new JPanel();
		tempLine2 = new JPanel();
		parent.add(tempLine1);
		parent.add(tempLine2);
	}
	
	public void add(Component m) {
		add(m, CENTER, 0, 0);
	}
	
	public void add(Component m, int i, int xPad, int yPad) {
		if(m == null)
			return;
		//usuwanie nie zostało przewidziane
		switch (i) {
		case CENTER: 
			central.add(parent.getComponentCount());
			break;
		case LEFT:
			yPadL.add(yPad);
			xPadL.add(xPad);
			left.add(parent.getComponentCount());
			break;
		case RIGHT:
			yPadR.add(yPad);
			xPadR.add(xPad);
			right.add(parent.getComponentCount());
			break;			
		default:
			throw new IllegalArgumentException("Unexpected value: " + i);
		}
		parent.add(m);
	}
	
	@Override
	public void layoutContainer(Container parent) {
		int maxXl = 0, minXr = parent.getWidth();
		int centerWidth = 0;
		if(parent != this.parent || parent.getComponentCount() != left.size()+right.size()+central.size() + 2)
			throw new IllegalArgumentException();
		for(int i=0;i<parent.getComponentCount();i++) {
			parent.getComponent(i).setSize(parent.getComponent(i).getPreferredSize());
		}
		Component child;
		if(yPadL.size() > 0) {
			int yLeft = yPadL.get(0);
			for(int i=0;i<left.size()-1;i++) {
				child = parent.getComponent(left.get(i));
				child.setLocation(xPadL.get(i), yLeft);
				yLeft += child.getPreferredSize().height;
				yLeft += (yPadL.get(i+1) > yPadL.get(i) ? yPadL.get(i+1) : yPadL.get(i));
				if(maxXl < child.getX() + child.getWidth())
					maxXl = child.getX() + child.getWidth();
			}
			int i = left.size()-1;
			child = parent.getComponent(left.get(i));
			child.setLocation(xPadL.get(i), yLeft);
			if(maxXl < child.getX() + child.getWidth())
				maxXl = child.getX() + child.getWidth();
		}
		if(yPadR.size() > 0) {
			int yRight = yPadR.get(0);
			for(int i=0;i<right.size()-1;i++) {
				child = parent.getComponent(right.get(i));
				child.setLocation(parent.getWidth() - child.getPreferredSize().width - xPadR.get(i), yRight);
				yRight += child.getPreferredSize().height;
				yRight += yPadR.get(i+1) > yPadR.get(i) ? yPadR.get(i+1) : yPadR.get(i);
				if(minXr > child.getX())
					minXr = child.getX();
			}
			
			int i = right.size()-1;
			child = parent.getComponent(right.get(i));
			child.setLocation(parent.getWidth() - child.getPreferredSize().width - xPadR.get(i), yRight);
			if(minXr > child.getX())
				minXr = child.getX();
		}
		for(int i=0;i<central.size();i++) {
			child = parent.getComponent(central.get(i));
			if(centerWidth < child.getWidth())
				centerWidth = child.getWidth();
		}

		for(int i=0;i<central.size();i++) {
			child = parent.getComponent(central.get(i));
			child.setLocation((int) ( (minXr + maxXl  - child.getPreferredSize().width) / 2), (int) ( parent.getHeight() / 2  - child.getPreferredSize().height / 2));
		}
		tempLine1.setSize(new Dimension(2, parent.getHeight()));
		tempLine2.setSize(new Dimension(2, parent.getHeight()));
		tempLine1.setBackground(Color.red);
		tempLine2.setBackground(Color.red);
		tempLine1.setLocation(maxXl, 0);
		tempLine2.setLocation(minXr, 0);

	}
	
	@Override
	public void addLayoutComponent(String name, Component comp) {
		new IllegalStateException("Added layout componennt").printStackTrace();
	}

	@Override
	public void removeLayoutComponent(Component comp) {
		new IllegalStateException("Added layout componennt").printStackTrace();
	}

	@Override
	public Dimension preferredLayoutSize(Container parent) {
		return prefferedSize;
	}

	public void setPrefferedSize(Dimension prefferedSize) {
		this.prefferedSize = prefferedSize;
	}
	
	@Override
	public Dimension minimumLayoutSize(Container parent) {
		return new Dimension(0,0);
	}

}
