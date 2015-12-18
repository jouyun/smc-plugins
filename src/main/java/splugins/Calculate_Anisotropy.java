package splugins;

import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.NewImage;
import ij.measure.Calibration;
import ij.plugin.PlugIn;
import ij.process.ByteProcessor;

public class Calculate_Anisotropy implements PlugIn {

	ImagePlus imp;
	int width, height, channels, slices, frames, cur_channel, cur_frame, cur_slice;
	@Override
	public void run(String arg0) {
		imp=WindowManager.getCurrentImage();
		Calibration my_cal=imp.getCalibration();
        width=imp.getWidth();
        height=imp.getHeight();
        channels=imp.getNChannels();
        slices=imp.getNSlices();
        frames=imp.getNFrames();
        cur_channel=imp.getChannel()-1;
        cur_frame=imp.getFrame()-1;
        cur_slice=imp.getSlice()-1;
        
        GenericDialog gd=new GenericDialog("Background");
        gd.addNumericField("Background: ", 0, 0);
        gd.addNumericField("SNR:  ", 10.0, 1);
        gd.addNumericField("Percentile:  ", 30.0, 0);
        gd.showDialog();
        float background=(float)gd.getNextNumber();
        float SNR=(float)gd.getNextNumber();
        float percentile=(float)gd.getNextNumber();
        
        ImagePlus new_img=NewImage.createFloatImage("Img", width, height, frames*slices, NewImage.FILL_BLACK);
        for (int s=0; s<slices; s++)
        {
        	for (int f=0; f<frames; f++)
        	{
        		float [] pix=(float [])imp.getStack().getProcessor(1+s*channels+f*channels*slices).getPixels();
        		float [] pixB=(float [])imp.getStack().getProcessor(2+s*channels+f*channels*slices).getPixels();
        		byte [] flag=Percentile_Threshold.get_mask(pix,  width,  height, (float) percentile,  (float)SNR);
        		float [] new_pix=(float[])new_img.getStack().getProcessor(1+s*channels+f*channels*slices).getPixels();
        		for (int i=0; i<width*height; i++)
        		{
        			float pa=pix[i]-background, pb=pixB[i]-background;
        			if (flag[i]!=0) new_pix[i]=(pa-pb)/(pa+2*pb);
        		}
        	}
        }
        new_img.show();
		new_img.updateAndDraw();

	}

}
