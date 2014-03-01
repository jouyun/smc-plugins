import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import ij.plugin.filter.*;
public class MakeNormalizedSpectra_ implements PlugInFilter {
	ImagePlus imp;
	float[] values;
	public int setup(String arg, ImagePlus imp) {
		this.imp = imp;
		return DOES_ALL;
	}
	public float find_mean()
	{
		float rtn=0;
		for (int i=0; i<values.length; i++)
		{
			rtn=rtn+values[i];
		}
		return rtn/(float)(values.length);
	}
	public float find_std(float avg)
	{
		float rtn=0, tmp;
		for (int i=0; i<values.length; i++)
		{
			tmp=(avg-values[i]);
			rtn=rtn+tmp*tmp;
		}
		return (float)Math.sqrt(rtn/(float)(values.length));
	}
	
	public void run(ImageProcessor ip) {
		ImageStack stack = imp.getStack();
		ImageProcessor p = stack.getProcessor(1);
		int num_pixels;
		num_pixels=p.getWidth()*p.getHeight();
        int channels=imp.getNChannels(), slices=imp.getNSlices(), frames=imp.getNFrames();
        values = new float[channels];
        for (int s=0; s<slices; s++)
        {
        	IJ.showProgress(s, slices);
        	for (int f=0; f<frames; f++)
        	{
        		for (int j=0; j<num_pixels; j++) 
        		{
    				for (int i=0; i<channels; i++) 
    				{
    				
    					ImageProcessor tp = stack.getProcessor(channels*slices*f+channels*s+i+1);
    					float[] pixels = (float[])tp.getPixels();
    					
    					values[i] = (float)pixels[j];;
    					//if (j==0) IJ.log(rtn.toString()+"\n");
    				}
    				float avg, std;
    				avg=find_mean();
    				std=find_std(avg);
    				for (int i=0; i<channels; i++) {
    				
    					ImageProcessor tp = stack.getProcessor(channels*slices*f+channels*s+i+1);
    					float[] pixels = (float[])tp.getPixels();
    					
    					pixels[j] = (float)((pixels[j]-avg)/std);;
    				}
        		}
        	}
        }
        for (int i=0; i<imp.getStack().getSize(); i++) imp.getStack().getProcessor(i+1).setMinAndMax(0, 1);
		imp.updateAndDraw();
		
	}
}