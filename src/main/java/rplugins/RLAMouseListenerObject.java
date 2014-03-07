package rplugins;

import ij.*;
import ij.plugin.filter.PlugInFilter;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.lang.reflect.*;

	/**
	This plugin implements the MouseListener and MouseMotionListener interfaces 
	and listens for mouse events generated by the current image.
	*/

	public class RLAMouseListenerObject implements PlugInFilter, MouseListener, MouseMotionListener {
		public static int mouseExit = 0;
		public int oldx = 0;	//added
		public int oldy = 0;	//added

		ImagePlus img;
		ImageCanvas canvas;
		static Vector images = new Vector();

		public int setup(String arg, ImagePlus img) {
		this.img = img;
		IJ.register(RLAMouseListenerObject.class);
		return DOES_ALL+NO_CHANGES;
		}

	public void doListen(){
		ImageWindow win = img.getWindow();
			canvas = win.getCanvas();
			canvas.addMouseListener(this);
			canvas.addMouseMotionListener(this);
			//int tool = Toolbar.getInstance().addTool("Test Tool");
			//Toolbar.getInstance().setTool(tool);	
			//images.addElement(id);

		WaitForUserDialog wfud =new WaitForUserDialog("Mouse Listener", "Choose the 'Freehand Selections' tool to log mouse dragging. \n \nClick 'OK' when you are finished and have saved your data.");
		wfud.show();
		canvas.removeMouseListener(this);
		canvas.removeMouseMotionListener(this);
		doCloseTasks();
		return;
	}
	
	public void doCloseTasks(){
	}


	public void run(ImageProcessor ip) {
			ImageWindow win = img.getWindow();
			canvas = win.getCanvas();
			canvas.addMouseListener(this);
			canvas.addMouseMotionListener(this);
			//int tool = Toolbar.getInstance().addTool("Test Tool");
			//Toolbar.getInstance().setTool(tool);	
			//images.addElement(id);

		WaitForUserDialog wfud =new WaitForUserDialog("Mouse Listener", "Choose the 'Freehand Selections' tool to log mouse dragging. \n \nClick 'OK' when you are finished and have saved your data.");
		wfud.show();
		canvas.removeMouseListener(this);
		canvas.removeMouseMotionListener(this);
		ClosePlugin();
	}
	
	public void doReportMouse(int x, int y){
		IJ.log(x+","+y);
	}	

	public void mousePressed(MouseEvent e) {
				Component comp = e.getComponent();
				String name = comp.getName();
				Container cont = comp.getParent();
				String parent = cont.getName();
				IJ.log(name + "...   parent = "+parent);
				if(comp instanceof Canvas){ //ImageCanvas
					IJ.log("FOUND Image!!!!  IMAGE!");
				}
			int x = e.getX();
			int y = e.getY();
			int offscreenX = canvas.offScreenX(x);
			int offscreenY = canvas.offScreenY(y);
			//IJ.log("Mouse pressed: "+offscreenX+","+offscreenY+modifiers(e.getModifiers()));
			//IJ.log("Right button: "+((e.getModifiers()&Event.META_MASK)!=0));
			//IJ.log(offscreenX+","+offscreenY);

		doReportMouse(offscreenX, offscreenY);
	}

	public void mouseReleased(MouseEvent e) {
		//IJ.log("mouseReleased: ");
	}
	
	public void mouseDragged(MouseEvent e) {
		if (mouseExit == 0){
		int tool = ij.gui.Toolbar.getToolId();
		if(tool == 3){			//tool 3 is the "Freehand Selections" tool.

			int x = e.getX();
			int y = e.getY();
			int offscreenX = canvas.offScreenX(x);
			int offscreenY = canvas.offScreenY(y);
			//IJ.log("Mouse dragged: "+offscreenX+","+offscreenY+modifiers(e.getModifiers()));
			//IJ.log("oldx = "+oldx+"    oldy = "+oldy+"    x = "+offscreenX+"    y = "+offscreenY);
			if(offscreenX != oldx || offscreenY != oldy){				//added
				IJ.log(offscreenX+","+offscreenY);
			}						//added
			oldx = offscreenX;				//added
			oldy = offscreenY;				//added
		}
		}
	}

	public static String modifiers(int flags) {
		String s = " [ ";
		if (flags == 0) return "";
		if ((flags & Event.SHIFT_MASK) != 0) s += "Shift ";
		if ((flags & Event.CTRL_MASK) != 0) s += "Control ";
		if ((flags & Event.META_MASK) != 0) s += "Meta (right button) ";
		if ((flags & Event.ALT_MASK) != 0) s += "Alt ";
		s += "]";
		if (s.equals(" [ ]"))
 			s = " [no modifiers]";
		return s;
	}

	public void mouseExited(MouseEvent e) {
		mouseExit = 1;
	}

	public void mouseEntered(MouseEvent e) {
		mouseExit = 0;
	}

	
	public void mouseClicked(MouseEvent e) {}	
	public void mouseMoved(MouseEvent e) {}
	
	public void ClosePlugin() {
		return;
	}

}



