package rplugins;
import ij.*;
import ij.process.*;	
import ij.gui.*;
import java.awt.*;
import ij.plugin.*;

public class rladrawElipse implements PlugIn {

	public void run(String arg) {

		int height = 250;
		int width = 250;
		int a = 15;  //major axis (along x-axis if no rotation)
		int b = 15;  //minor axis (along y-axis if no rotation)
		int i = 2*height / 4;
		int j = 1*width / 4;

		byte[] pixels = new byte[height*width];
		int nangles = (int)(2.0*Math.PI*(double)a);
		double increment = 5.0/(nangles);
		//IJ.log(""+nangles);

		double phi= Math.PI/8;
		double cos_phi = Math.cos(phi);
		double sin_phi = Math.sin(phi);

int loop = 0;
		for(double theta = 0; theta<2*Math.PI; theta = theta+increment){
			double cos_theta = Math.cos(theta);
			double sin_theta = Math.sin(theta);

			int x= (int)(j +(a*(cos_theta*cos_phi)-b*(sin_theta*sin_phi)));
			int y = (int)(i + (a*(cos_theta*sin_phi) + b*(sin_theta*cos_phi)));
			int test = y*width;
			//if(x>0 && x<width && y>0 && y<height){
				pixels[x+y*width] = 1;
			//}
loop++;
		}
IJ.log(""+loop);

		ImageStack mystack = new ImageStack(width,height);
		mystack.addSlice("",pixels);
		new ImagePlus("ellipse",mystack).show();
		
	}

}
