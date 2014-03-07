package rplugins;
import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.Point;
import ij.plugin.*;
import ij.plugin.frame.*;
//TODO: !!!!!!!!!    iterate through curve segments   !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
//TODO: !!!!!!!!!    does the same equation set work?
//TODO: !!!!!!!!!    or do we need new coeficients for non-end-point segments?
public class Bezier implements PlugIn {
	ImagePlus imp;
	ImageProcessor prc;
	public RoiManager roim;
	Polygon p;
	public void run(String arg) {
		imp = IJ.getImage();
		prc = imp.getProcessor();
		Roi roi = imp.getRoi(); 
		p = roi.getPolygon(); 
		int npoints = p.npoints;
		IJ.log("NUM: "+npoints);
		//start with 4 points only
		
		Point p0;
		Point p1;
		Point p2;
		Point p3;
		
		for (int i=0; i<p.npoints-1; i++){
			double[] previousCs = {0.0,0.0,0.0,0.0};
			if(i==0){
				p0 = new Point(p.xpoints[i], p.ypoints[i]);
				p1 = new Point(p.xpoints[i], p.ypoints[i]);
				p2 = new Point(p.xpoints[i+1], p.ypoints[i+1]);
				p3 = new Point(p.xpoints[i+2], p.ypoints[i+2]);
				previousCs = getBezierCurveStart(p0, p1, p2, p3);
			}else if(i==p.npoints-1){
				p0 = new Point(p.xpoints[i], p.ypoints[i]);
				p1 = new Point(p.xpoints[i], p.ypoints[i]);
				p2 = new Point(p.xpoints[i+1], p.ypoints[i+1]);
				p3 = new Point(p.xpoints[i+2], p.ypoints[i+2]);
			}else{
				p0 = new Point(p.xpoints[i-1], p.ypoints[i-1]);
				p1 = new Point(p.xpoints[i], p.ypoints[i]);
				p2 = new Point(p.xpoints[i+1], p.ypoints[i+1]);
				p3 = new Point(p.xpoints[i+2], p.ypoints[i+2]);
			}
			
			
		}
		
		
	}
	
//	public double[] getBezierCurveMid(Point p0, Point p1, Point p2, Point p3){
//		
//	}
	
	public double[] getBezierCurveStart(Point p0, Point p1, Point p2, Point p3){
		double p0x = p1.getX();
		double p0y = p1.getY();
		double p1x = p2.getX();
		double p1y = p2.getY();
		double p2x = p3.getX();
		double p2y = p3.getY();
		
		// double t1 = 34*p0x+32*p1x-9*p2x;
		// double t2 = (double)(1/59);
		// IJ.log("t1  ------   "+t1);
		// IJ.log("t2  ------   "+t2);
		double c0x = (34*p0x+32*p1x-9*p2x)/59;
		double c1x = (9*p0x+64*p1x-18*p2x)/59;
		double c2x = (-9*p0x+54*p1x-18*p2x)/59;
		double c3x = (-2*p0x+12*p1x+63*p2x)/59;
		double c4x = (2*p0x-12*p1x+55*p2x)/59;
		double c5x = (-p0x+6*p1x+2*p2x)/59;
		
		double c0y = (34*p0y+32*p1y-9*p2y)/59;
		double c1y = (9*p0y+64*p1y-18*p2y)/59;
		double c2y = (-9*p0y+54*p1y-18*p2y)/59;
		double c3y = (-2*p0y+12*p1y+63*p2y)/59;
		double c4y = (2*p0y-12*p1y+55*p2y)/59;
		double c5y = (-p0y+6*p1y+2*p2y)/59;
		
		IJ.log("P's");
		IJ.log("p0:  "+p0x+", "+p0y);
		IJ.log("p1:  "+p1x+", "+p1y);
		IJ.log("p2:  "+p2x+", "+p2y);
		
		IJ.log("C's");
		IJ.log("c0:  "+(int)c0x+", "+(int)c0y);
		IJ.log("c1:  "+c1x+", "+c1y);
		IJ.log("c2:  "+c2x+", "+c2y);
		IJ.log("c3:  "+c3x+", "+c3y);
		IJ.log("c4:  "+c4x+", "+c4y);
		
		// drawControlPoint((int)c0x, (int)c0y);
		// drawControlPoint((int)c1x, (int)c1y);
		// drawControlPoint((int)c2x, (int)c2y);
		// drawControlPoint((int)c3x, (int)c3y);
		
		prc.setColor(Color.red);
		prc.setLineWidth(5);
		prc.drawDot((int)c0x, (int)c0y);
		prc.drawDot((int)c1x, (int)c1y);
		// prc.drawDot((int)c2x, (int)c2y);
		// prc.drawDot((int)c3x, (int)c3y);
		// prc.drawDot((int)c4x, (int)c4y);
		
		//draw Bezier curve here
		//Pi(t) will be called Bx(t) and By(t)
		prc.setColor(Color.white);
		double eps = 1.0/100.0;
		for(double t=0; t<=1; t=t+eps){
			double Bx = (1-t)*(1-t)*(1-t)*p0x + 3*(1-t)*(1-t)*t*c0x + 3*(1-t)*t*t*c1x + t*t*t*p1x;
			double By = (1-t)*(1-t)*(1-t)*p0y + 3*(1-t)*(1-t)*t*c0y + 3*(1-t)*t*t*c1y + t*t*t*p1y;
			prc.set((int)Bx, (int)By, 255);
		}
		// for(double t=0; t<=1; t=t+eps){
			// double Bx = (1-t)*(1-t)*(1-t)*p1x + 3*(1-t)*(1-t)*t*c2x + 3*(1-t)*t*t*c3x + t*t*t*p2x;
			// double By = (1-t)*(1-t)*(1-t)*p1y + 3*(1-t)*(1-t)*t*c2y + 3*(1-t)*t*t*c3y + t*t*t*p2y;
			// prc.set((int)Bx, (int)By, 255);
		// }
		imp.updateAndDraw();
		
		//draw line between control points
		prc.setColor(Color.white);
		prc.setLineWidth(1);
		prc.drawLine((int)c1x,(int)c1y,(int)c2x,(int)c2y);
		
		imp.updateAndDraw();
		return(new double[]{c0x, c0y, c1x, c1y});
	}
	
	void drawControlPoint(int x, int y){
		for(int i=-5; i<=5; i++){
			for(int j=-5; j<=5; j++){
				prc.set(x+j, y+i, 255);
			}
		}
	}
	
	// void drawBezier(Point p0, point p1, point c0, point c1){
		////Pi(t) will be called Bx(t) and By(t)
		// double eps = 1.0/100.0;
		// for(double t=0; t<=1; t=t+eps){
			// double Bx = (1-t)*(1-t)*(1-t)*p0x + 3*(1-t)*(1-t)*t*c0x + 3*(1-t)*t*t*c1x + t*t*t*p1x;
			// double By = (1-t)*(1-t)*(1-t)*p0y + 3*(1-t)*(1-t)*t*c0y + 3*(1-t)*t*t*c1y + t*t*t*p1y;
		// }
	// }

}
