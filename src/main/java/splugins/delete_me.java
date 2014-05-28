package splugins;

import ij.*;  
import ij.plugin.PlugIn;  
import ij.process.*;  
import ij.io.Opener;  
import imagescience.feature.Differentiator;
import imagescience.image.Aspects;
import imagescience.image.Dimensions;
import imagescience.image.FloatImage;
import imagescience.image.Image;

import java.util.concurrent.atomic.AtomicInteger;

public class delete_me implements PlugIn {

	@Override
	public void run(String arg0) {
		final ImagePlus dot_blot = new Opener().openURL("http://rsb.info.nih.gov/ij/images/blobs.gif");  
		final ImageProcessor [] my_list=new ImageProcessor[5];
		for (int i=0; i<5; i++)
		{
			ImageProcessor ip = dot_blot.getProcessor().duplicate();

			ImagePlus imp = new ImagePlus("Threshold " + i, ip);
			
			Image img = Image.wrap(imp);
			
			Image newimg = new FloatImage(img);
			Differentiator diff = new Differentiator();
			diff.run(newimg,i+1,2,2,0);
			my_list[i]=newimg.imageplus().getProcessor();
		}
		final ImageStack stack = new ImageStack(dot_blot.getWidth(),  dot_blot.getHeight());  
		for (int i=0; i< my_list.length; i++) 
		{  
			stack.addSlice(Integer.toString(i), my_list[i]);  
		}  

		new ImagePlus("Results", stack).show();  

	}

}
