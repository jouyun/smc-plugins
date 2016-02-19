package splugins;

import java.awt.Frame;
import java.awt.Point;
import java.awt.Polygon;
import java.util.ArrayList;

import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.NewImage;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;
import ij.process.ByteProcessor;
import ij.text.TextWindow;

public class Calculate_Anisotropy implements PlugIn {

	ImagePlus imp, new_img;
	int width, height, channels, slices, frames, cur_channel, cur_frame, cur_slice;
	float background, SNR, percentile;
	boolean several;
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
        gd.addNumericField("Background: ", -1, 0);
        gd.addNumericField("SNR:  ", 10.0, 0);
        gd.addNumericField("Percentile:  ", 30.0, 0);
        gd.addCheckbox("Generate several SNRs?: ", false);
        gd.showDialog();
        background=(float)gd.getNextNumber();
        SNR=(float)gd.getNextNumber();
        percentile=(float)gd.getNextNumber();
        several=gd.getNextBoolean();
        
        if (imp.getRoi()!=null)
        {
        	do_ROI();
        }
        else
        {
        	do_whole_image();
        }
	}
	public void do_ROI()
	{
    	Polygon p=imp.getRoi().getPolygon();
    	
		float [] pix=(float [])imp.getStack().getProcessor(1+cur_slice*channels+cur_frame*channels*slices).convertToFloat().getPixels();
		float [] pixB=(float [])imp.getStack().getProcessor(2+cur_slice*channels+cur_frame*channels*slices).convertToFloat().getPixels();
		int ctr=0;
		float cum=0;

		for (int x=0; x<width; x++)
		{
			for (int y=0; y<height; y++)
			{
				Point P=new Point(x,y);
				if (p.contains(P)) 
				{
					float pp=(pix[x+width*y]-background);
					float ss=pixB[x+width*y]-background;
							
					float tmp=(pp-ss)/(pp+2*ss);
					cum+=tmp;
					ctr++;
				}
			}
		}
    	Frame window=WindowManager.getFrame("Anisotropy");
    	if (window==null)
    	{
    		ResultsTable the_table=new ResultsTable();
    		the_table.incrementCounter();
    		the_table.addValue("Image",imp.getTitle());
    		the_table.addValue("AvgAnisotropy",cum/ctr);
    		the_table.show("Anisotropy");	
    	}
    	else
    	{
    		ResultsTable the_table=((TextWindow)window).getTextPanel().getResultsTable();
    		the_table.incrementCounter();
    		the_table.addValue("Image",imp.getTitle());
    		the_table.addValue("AvgAnisotropy",cum/ctr);
    		the_table.show("Anisotropy");
    	}
		
	}
	public void do_whole_image()
	{
        int min=1, step=5, steps=40;
        if (!several) steps=1;
        
        if (several) new_img=NewImage.createFloatImage("Img", width, height, steps, NewImage.FILL_BLACK);
        else new_img=NewImage.createFloatImage("Img", width, height, frames*slices, NewImage.FILL_BLACK);
        
        ResultsTable pt_table=null;
        if (!several) 
        {
        	Frame winow=WindowManager.getFrame("Points");
        	if (winow==null)
        	{
        		pt_table=new ResultsTable();
        	}
        	else
        	{
        		pt_table=((TextWindow)winow).getTextPanel().getResultsTable();
        	}
        }
        
        for (int st=0; st<steps; st++)
        {
        	double cum=0;
        	int ctr=0;
        	for (int s=0; s<slices; s++)
        	{
        		for (int f=0; f<frames; f++)
        		{	
        			float [] pix=(float [])imp.getStack().getProcessor(1+s*channels+f*channels*slices).convertToFloat().getPixels();
        			float [] pixB=(float [])imp.getStack().getProcessor(2+s*channels+f*channels*slices).convertToFloat().getPixels();
        			byte [] flag;
        			double [] avg=Percentile_Threshold.find_average_sigma(pix, width, height, (float) percentile);
        			if (background==-1) background=(float) avg[0];
        			if (!several) 
        			{
        				flag=Percentile_Threshold.get_mask(pix,  width,  height, (float) percentile,  (float)SNR);
        			}
        			else 
        			{
        				flag=Percentile_Threshold.get_mask(pix,  width,  height, (float) percentile,  (float)(min+st*step));
        			}
        			
        			float [] new_pix;
        			
        			if (!several) new_pix=(float[])new_img.getStack().getProcessor(1+s*channels+f*channels*slices).getPixels();
        			else new_pix=(float[])new_img.getStack().getProcessor(1+st).getPixels();
        			
        			for (int i=0; i<width*height; i++)
        			{
        				float pa=pix[i]-background, pb=pixB[i]-background;
        				if (flag[i]!=0) 
        				{
        					new_pix[i]=(pa-pb)/(pa+2*pb);
        					cum+=new_pix[i];
        					ctr++;
        					if (!several)
        					{
        		        		pt_table.incrementCounter();
        		        		pt_table.addValue("Image",imp.getTitle());
        		        		pt_table.addValue("Brightness",pa);
        		        		pt_table.addValue("Anisotropy",new_pix[i]);
        		        		
        					}
        				}
        				else new_pix[i]=-10;
        			}
        		}
        	}
        	Frame window=WindowManager.getFrame("Anisotropy");
        	if (window==null)
        	{
        		ResultsTable the_table=new ResultsTable();
        		the_table.incrementCounter();
        		the_table.addValue("Image",imp.getTitle());
        		the_table.addValue("AvgAnisotropy",cum/ctr);
        		if (several) 
        		{
        			the_table.addValue("SNR", min+st*step);
        			the_table.addValue("Frame", st+1);
        		}
        		the_table.show("Anisotropy");	
        	}
        	else
        	{
        		ResultsTable the_table=((TextWindow)window).getTextPanel().getResultsTable();
        		the_table.incrementCounter();
        		the_table.addValue("Image",imp.getTitle());
        		the_table.addValue("AvgAnisotropy",cum/ctr);
        		if (several) 
        		{
        			the_table.addValue("SNR", min+st*step);
        			the_table.addValue("Frame", st+1);
        		}
        		the_table.show("Anisotropy");
        	}
        	if (!several) pt_table.show("Points");	
        }
        new_img.show();
		new_img.updateAndDraw();
		
	}

}
