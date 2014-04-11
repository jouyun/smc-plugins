package rplugins;
import ij.*;
import ij.plugin.*;
import ij.plugin.frame.RoiManager;
import ij.gui.*;
import ij.gui.ShapeRoi;
import ij.process.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.geom.*;


public class YeastFeret implements PlugIn {
	Overlay overlay;
	ImagePlus ip;
	ImageProcessor imgProc;
	
	public void run(String arg) {
		ip = IJ.getImage();
		imgProc = ip.getProcessor();
		RoiManager roim = RoiManager.getInstance2();	//Interpreter interp = Interpreter.getInstance(); //RoiManager roim =interp.getBatchModeRoiManager(); //
		//roim.runCommand("Deselect");
		//roim.runCommand("Delete");
		//analyze particles here
		int countAll = roim.getCount();
		Roi[] roiArray = roim.getRoisAsArray();
		//double[] vals = getFeretValues(roiArray[0]);
		overlay = new Overlay(); 
		for(int i=0; i<roiArray.length; i++){
			double[] feret = roiArray[i].getFeretValues();
			double diameter = feret[0];
			double angle = feret[1];
			double min = feret[2];
			double fx = feret[3];
			double fy = feret[4];
			IJ.log("diameter: "+diameter+"   angle: "+angle+"  min: "+min+"  x: "+fx+"   y: "+fy);
			//Line line = getLine(fx,fy,diameter,angle);
			// Object[] lineRet = getLine(fx,fy,diameter,angle);
			// Line line = (Line)lineRet[0];
			// int[] x = (int[])lineRet[1];
			// int[] y = (int[])lineRet[2];
			// IJ.log("x:"+x[0]+","+x[1]+"     y:"+y[0]+","+y[1]);
			Object[] roiRet = getRois(fx,fy,diameter,angle,min);
			Line line = (Line)roiRet[0];
			PolygonRoi box = (PolygonRoi)roiRet[1];
			
			//maskEdges(roiArray[i], box);
			excludeBox(roiArray[i], box);
			
			overlay.addElement(line); 
			//overlay.addElement(box); 
			IJ.log("ol: "+overlay.size());
		}
		ip.setOverlay(overlay);
		
	}
	
	public void excludeBox(Roi roi, PolygonRoi broi){
		ShapeRoi yeast = new ShapeRoi(roi);
		ShapeRoi box = new ShapeRoi(broi);
		ShapeRoi edges = yeast.not(box);
		overlay.add(edges);
	}
	
	public void maskEdges(Roi yeast, PolygonRoi box){
		ImagePlus ip = IJ.getImage();
		Rectangle r = yeast.getBounds();
		int x = r.x; //top left coord of rectangle
		int y = r.y; //top left coord of rectangle
		int height = r.height;
		int width = r.width;
		
		for (int j=0; j<height; j++) {
			for (int i=0; i<width; i++) {
				if (yeast.contains(x+i,y+j) && box.contains(x+i,y+j)){
					imgProc.set(x+i,y+j,0);
				}	
			}
		}
		ip.draw();
	}
	
	public Object[] getRois(double lx1, double ly1, double length, double angle, double min){
		//calculate the endpoint and return a Line ROI
		//line endpoints are (lx1, ly1) and (lx2, ly2)
		angle = angle>90 ? angle-180 : angle;
		double xlength = length*Math.cos(Math.PI*angle/180);
		double ylength = length*Math.sin(Math.PI*angle/180);
		int lx2 = (int)(lx1+xlength);
		int ly2 = (int)(ly1-ylength);
		IJ.log("lx2:"+lx2+" ly2:"+ly2);
		
		//calculate 2 midpoints for the polygon (mx1,my1) and (mx2,my2)
		double percent = 0.20;//20% from either side
		IJ.log("xl1: "+ xlength +"   xl2: "+percent*xlength);
		double mx1 = lx1+(percent*xlength);
		double mx2 = lx1+((1-percent)*xlength);
		double my1 = ly1-(percent*ylength);
		double my2 = ly1-((1-percent)*ylength);
		IJ.log("m1: "+mx1+","+my1);
		IJ.log("m2: "+mx2+","+my2);
		overlay.add(new PointRoi(mx1,my1));
		overlay.add(new PointRoi(mx2,my2));
		// IJ.getImage().setOverlay(overlay);
		
		//extrude the midpoints at a 90 degree angle to get polygon points
		double mAngle = 90-angle;
		double mxlength = 1.5+(min)*Math.cos(Math.PI*mAngle/180)/2;
		double mylength = 1.5+(min)*Math.sin(Math.PI*mAngle/180)/2;
		
		int[] px = {(int)(mx1+mxlength), (int)(mx1-mxlength), (int)(mx2-mxlength), (int)(mx2+mxlength)};
		int[] py = {(int)(my1+mylength), (int)(my1-mylength), (int)(my2-mylength), (int)(my2+mylength)};
		
		return new Object[]{new Line(lx1,ly1,lx2,ly2), new PolygonRoi(px,py,4,Roi.POLYGON)};
	}
	
	// public Object[] getLine(double x, double y, double length, double angle){
		////calculate the endpoint and return a Line ROI
		// angle = angle>90 ? angle-180 : angle;
		// int lx = (int)(x+length*Math.cos(Math.PI*angle/180));
		// int ly = (int)(y-length*Math.sin(Math.PI*angle/180));
		// IJ.log("lx:"+lx+" ly:"+ly);
		////return new Line(x,y,lx,ly);
		// return new Object[]{new Line(x,y,lx,ly), new int[]{(int)x,lx}, new int[]{(int)y,ly}};
	// }
	
	public double[] getFeretValues(Roi roi) {
		//Roi[] rois = getRois();
		//if (rois!=null && rois.length==1)
			//return rois[0].getFeretValues();
		double min=Double.MAX_VALUE, diameter=0.0, angle=0.0;
		int p1=0, p2=0;
		double pw=1.0, ph=1.0;
		ShapeRoi sroi = new ShapeRoi(roi);
		Shape shape = sroi.getShape();
		Shape s = null;
		Rectangle2D r = shape.getBounds2D();
		double cx = r.getX() + r.getWidth()/2;
		double cy = r.getY() + r.getHeight()/2;
		AffineTransform at = new AffineTransform();
		at.translate(cx, cy);
		for (int i=0; i<181; i++) {
			at.rotate(Math.PI/180.0);
			s = at.createTransformedShape(shape);
			r = s.getBounds2D();
			double max2 = Math.max(r.getWidth(), r.getHeight());
			if (max2>diameter) {
				diameter = max2*pw;
				angle = i;
			}
			double min2 = Math.min(r.getWidth(), r.getHeight());
			min = Math.min(min, min2);
		}
		if (pw!=ph) {
			diameter = 0.0;
			angle = 0.0;
		}
		if (pw==ph)
			min *= pw;
		else {
			min = 0.0;
			angle = 0.0;
		}
		double[] a = new double[5];
		IJ.log("diameter:"+diameter+"   angle: "+angle+"   min: "+min);
		a[0] = diameter;
		a[1] = angle;
		a[2] = min;
		a[3] = 0.0; // FeretX
		a[4] = 0.0; // FeretY
		return a;
	}
	
}