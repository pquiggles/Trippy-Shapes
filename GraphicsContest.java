/*
 * File:GraphicsContest.java
 * -------------------
 * Name:Paul Quigley
 */

import acm.graphics.*;
import acm.program.*;
import acm.util.*;
import java.util.*;
import java.applet.*;
import java.awt.*;
import java.awt.event.*;

public class GraphicsContest extends GraphicsProgram{
	
	
	/**Variables updated by mouse events and used for the creation of polygons.*/
	private double x1;
	private double y1;
	private double x2;
	private double y2;
	private double r;
	
	GPolygon poly = new GPolygon();
	
	public static void main(String[] args) {
		new GraphicsContest().start(args);
		}
	
	/** An array of all created shapes used for screen-wide manipulations. */
	private ArrayList<GPolygon> shapes = new ArrayList<GPolygon>();
	
	/**Map used to map every color to a new color for a "rave-like" effect.*/
	private Map<Color,Color> colorizer = new HashMap<Color,Color>();
	
	/**Random generator used to color polygons and simulate Brownian motion with degrade effect.*/
	private RandomGenerator rgen = RandomGenerator.getInstance();
	private Color rectColor = rgen.nextColor(); //Color of first shape drawn.

	/**Labels and corresponding booleans, all of which correspond to some visual effect.
	 * Each label functions essentially like a button.
	 * Turns corresponding effect on or off, as appropriate.*/
	private GLabel degradeLabel;
	private boolean degrading = false;
	private GLabel colorizeLabel;
	private boolean colorizing = false;
	private GLabel rotateLabel;
	private boolean rotating = false;
	private GLabel clearLabel;
	private boolean triangle = true;
	private GLabel triangleLabel;
	private boolean rectangle = false;
	private GLabel rectangleLabel;
	private boolean threeD = false;
	private GLabel flatLabel;
	private boolean flat = true;
	private GLabel threeDLabel;
	private boolean shrinking=false;
	private GLabel shrinkLabel;
	private boolean enlarging=false;
	private GLabel enlargeLabel;
	
	private static final int NUM_LABELS = 10;
	
	/**An array of labels used to make the manipulation labels easier.*/
	private GLabel[] labels = new GLabel[NUM_LABELS];
	
	private static final double LABEL_OFFSET = 29; //Used to position labels vertically;
	
	public void run(){
		setUp();
		while(true){
			if(colorizing){
				generateColorizer();
			}
			intensifyPolygons();
			sendLabelsToFront();
			pause(pauseAlgorithm());
		}
	}
	
	/**Sets up window and labels.*/
	private void setUp(){
		setSize(1024, 576); 
		addMouseListeners();
		setUpLabels();
		setBackground(Color.black);
		triangleLabel.setColor(Color.red);
		flatLabel.setColor(Color.red);
		setBackground(Color.black);
	}
	
	private void setUpLabels(){
		setLabelPositions();
		addLabelsToArray();
		styleLabelsUp();
		shiftLabelsOver();
		addLabelsToCanvas();
		triangleLabel.setColor(Color.red); //default when user turns program on is flat triangles.
		flatLabel.setColor(Color.red);
	}

	/**Sets labels in asthetically pleasing position on screen.*/
	private void setLabelPositions(){
		//bottom row of labels, corresponding to effects.
		degradeLabel = new GLabel("Degrade", 
				getWidth()/6, 
				getHeight()-LABEL_OFFSET);
		colorizeLabel = new GLabel("Colorize",
				getWidth()/3, 
				getHeight()-LABEL_OFFSET);
		rotateLabel = new GLabel("Rotate",
				getWidth()/2,
				getHeight()-LABEL_OFFSET);
		shrinkLabel = new GLabel("Shrink",
				getWidth()*2/3,
				getHeight()-LABEL_OFFSET);
		enlargeLabel = new GLabel("Enlarge",
				getWidth()*5/6,
				getHeight()-LABEL_OFFSET);
		
		//top row of labels, corresponding to creation and removal of polygons.
		threeDLabel = new GLabel("3D", 
				getWidth()/6, 
				LABEL_OFFSET);
		flatLabel = new GLabel("Flat",
				getWidth()/3, 
				LABEL_OFFSET);
		clearLabel = new GLabel("Clear", 
				getWidth()/2,
				LABEL_OFFSET);
		rectangleLabel = new GLabel("Rectangle", 
				getWidth()*5/6,
				LABEL_OFFSET);
		triangleLabel = new GLabel("Triangle", 
				getWidth()*2/3, 
				LABEL_OFFSET);
	}
	
	private void addLabelsToArray(){
		labels[0] = degradeLabel;
		labels[1] = rotateLabel;
		labels[2] = colorizeLabel;
		labels[3] = triangleLabel;
		labels[4] = clearLabel;
		labels[5] = rectangleLabel;
		labels[6] = threeDLabel;
		labels[7] = flatLabel;
		labels[8] = enlargeLabel;
		labels[9] = shrinkLabel;
	}
	
	private void addLabelsToCanvas(){
		for(int i=0; i<10;i++){
			add(labels[i]);
		}
	}
	
	private void styleLabelsUp(){
		for(int i=0; i<NUM_LABELS; i++){
			labels[i].setColor(Color.WHITE);
			labels[i].setFont("Helvetica-16");
		}
	}
	
	/** Makes labels appear centered*/
	private void shiftLabelsOver(){
		for(int i=0; i<NUM_LABELS; i++){
			labels[i].move(-labels[i].getWidth()/2,0);
		}
	}
	
	/**Performs activated operations on all polygons.*/
	private void intensifyPolygons(){
		for(int i=0; i<shapes.size(); i++){
				if(colorizing){
					colorize(shapes.get(i));
				}
				if(degrading){
					degrade(shapes.get(i));
				}
				if(rotating){
					shapes.get(i).rotate(3);
					}
				if(enlarging){
					shapes.get(i).scale(1.01);
				}
				if(shrinking){
					shapes.get(i).scale(.98);
				}
			}
		}
	
	/**I wanted it to rotate roughly to the beat of the song, and it was moving way too fast.
	 * So I played around a bit, and this usually works, at least on my processor
	 * @return time in milliseconds that it will be paused for.*/
	private double pauseAlgorithm(){
		double result = 12;
		result -= (double)shapes.size()/300;
		if(rotating) result *=.9;
		if(colorizing) result *=.9;
		if(degrading) result *=.9;
		if(result>0){
			return result;
		}
		else{
			return 0; //Obviously I can't have a negative pause, though it would be nice if I could speed my processor up.
		}
	}

	/**Sends labels to fronts to ensure that they remain clickable*/
	private void sendLabelsToFront(){
		for(int i=0;i<NUM_LABELS;i++){
			labels[i].sendToFront();
		}
	}
	
	/**Creates a shape corresponding to the one the user selected.
	 * Gives the shape a color.
	 * Color shapes are given changes roughly once every ten shapes created.*/
	public void mouseDragged(MouseEvent e){
		x2 = e.getX();
		y2 = e.getY();
		poly = new GPolygon(x1,y1);
		r = Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2));
		if(rectangle){
			if(threeD){
				threeDRectangle();
			}
			if(flat){
				flatRectangle();
			}
		}
		if(triangle){
			if(threeD){
				threeDTriangle();
			}
			if(flat){
				flatTriangle();
			}
		}
		if (rgen.nextBoolean(.2)){ //Causes shapes to change color roughly one in every five shapes.
			rectColor=rgen.nextColor();
		}
		poly.setColor(rectColor);
		add(poly);
		shapes.add(poly);
	}
	
	/**Changes the effects activated, and the shapes created depending on the labels clicked.*/
	public void mouseClicked(MouseEvent e){
		GObject objectClicked = getElementAt(e.getX(), e.getY());	
		if (objectClicked == degradeLabel){
				if(degrading){
					degradeLabel.setColor(Color.white);
					degrading = false;
				}
				else{
					degradeLabel.setColor(Color.RED);
					degrading = true;
				}
			}
			if (objectClicked == colorizeLabel){
				if(colorizing){
					colorizing = false;
					colorizeLabel.setColor(Color.white);
				}
				else{
					colorizeLabel.setColor(Color.RED);
					colorizing = true;
				}	
		}
		if (objectClicked == rotateLabel){
				if(rotating){
					rotating = false;
					rotateLabel.setColor(Color.white);
				}
				else{
					rotating = true;
					rotateLabel.setColor(Color.RED);
				}
			}
			if (objectClicked == shrinkLabel){
				if(shrinking){
					shrinking = false;
					shrinkLabel.setColor(Color.white);
				}
				else{
					shrinking = true;
					shrinkLabel.setColor(Color.RED);
					enlarging = false;
					enlargeLabel.setColor(Color.white);
				}
			}
			if (objectClicked == enlargeLabel){
				if(enlarging){
					enlarging = false;
					enlargeLabel.setColor(Color.white);
			}
			else{
				enlarging = true;
				enlargeLabel.setColor(Color.RED);
				shrinking = false;
				shrinkLabel.setColor(Color.white);
			}
		}
		if (objectClicked == clearLabel){
			rotating = false;
			degrading = false;
			colorizing = false;
			enlarging = false;
			shrinking = false;
			rotateLabel.setColor(Color.white);
			colorizeLabel.setColor(Color.white);
			degradeLabel.setColor(Color.white);
			shrinkLabel.setColor(Color.white);
			enlargeLabel.setColor(Color.white);
			removeFigures();
		}
		if (objectClicked == rectangleLabel){
			rectangleLabel.setColor(Color.red);
			rectangle = true;
			triangle = false;
			triangleLabel.setColor(Color.white);
		}
		if(objectClicked == triangleLabel){
			triangleLabel.setColor(Color.red);
			triangle = true;
			rectangle = false;
			rectangleLabel.setColor(Color.white);
			
		}
		if(objectClicked == flatLabel){
			flat = true;
			threeD = false;
			flatLabel.setColor(Color.red);
			threeDLabel.setColor(Color.white);
		}
		if(objectClicked == threeDLabel){
			flat = false;
			threeD = true;
			flatLabel.setColor(Color.white);
			threeDLabel.setColor(Color.red);
		}
	}
	
	
	/** Clears all the figures from the screen.
	 * Also empties the array of shapes to avoid a null pointer error. */
	private void removeFigures(){
		colorizer.clear();
		for(int i=0; i<shapes.size(); i++){
			remove(shapes.get(i));
		}
		shapes.clear();
	}
	
	
	/**Moves and rotates shapes randomly. Makes it look like large chunks of polygons are dissolving. */
	private void degrade(GPolygon g){
			g.rotate(rgen.nextInt(-3,3));
			g.move(rgen.nextInt(-6,6), rgen.nextInt(-3,3));
			}
	
	/**Gets the reference point for shapes created when mouse is clicked.*/
	public  void mousePressed(MouseEvent e){
		x1 = e.getX();
		y1 = e.getY();
	}
	
	
	/**Creates a hashmap of every shape's current color. Maps it to a new random color.*/
	private void generateColorizer(){
		colorizer.clear();
		for(int i =0; i<shapes.size(); i++){
			colorizer.put(shapes.get(i).getColor(), rgen.nextColor());
		}
	}
	
	/**Sets the polygon's color to the color the colorizer has randomly mapped it too.*/
	private void colorize(GPolygon g){
		g.setColor(colorizer.get(g.getColor()));
	}
	
	/**Creates a rectangle that will look 3d when multiple are created with one "drag"*/
	private void threeDRectangle(){
		poly = new GPolygon(x2,y2);
		poly.addVertex(0, 0);
		poly.addVertex(0, r);
		poly.addVertex(r, r);
		poly.addVertex(r, 0);
	}
	
	/**Creates a rectangle that looks flat when multiple are created with one "drag"*/
	private void flatRectangle(){
		poly.addVertex(x2-x1, y2-y1);
		poly.addVertex(0, y2-y1);
		poly.addVertex(0, 0);
		poly.addVertex(x2-x1, 0);
	}
	
	/**Creates a trianglee that will look 3d when multiple are created with one "drag"*/
	private void threeDTriangle(){
		poly = new GPolygon(x2,y2);
		poly.addEdge(x2-x1, y2-y1);
		poly.addPolarEdge(r, -120);
		poly.addPolarEdge(r, 0);
	}
	
	/**Creates a triangle that looks flat when multiple are created with one "drag"*/
	private void flatTriangle(){
		poly.addVertex(0, 0);
		poly.addVertex(x2-x1,y2-y1);
		//poly.addPolarEdge(r,0); //Alternate method for creating triangles that looks less cool.
		//poly.addPolarEdge(r,120);
		poly.addPolarEdge(r,-90);
	}
}