package inne;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.LinkedList;
import java.util.List;

import grafika.CoordPanel;

public class PolyCurve<E>{
	LinkedList<E> vert = new LinkedList<E>();
	
	public PolyCurve(){};
	
	public PolyCurve(LinkedList<E> l){
		vert = l;
	}
	
	public void addPoint(E z) {
		vert.add(z);
	}
	
	public LinkedList<E> getVert(){
		return vert;
	}

}
