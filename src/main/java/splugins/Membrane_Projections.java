package splugins;

import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.NewImage;
import ij.plugin.PlugIn;
import ij.plugin.ZProjector;
import ij.plugin.filter.Convolver;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

public class Membrane_Projections implements PlugIn {
	
	ImagePlus imp;
	int width;
	int height;
	int slices;
	int frames;
	int channels;
	int nAngles;
	int cur_slice;
	int cur_frame;
	int cur_channel;

	@Override
	public void run(String arg0) {
		imp=WindowManager.getCurrentImage();
		width=imp.getWidth(); 
		height=imp.getHeight();
		slices=imp.getNSlices();
		frames=imp.getNFrames();
		channels=imp.getNChannels();
		cur_slice=imp.getSlice()-1;
		cur_frame=imp.getFrame()-1;
		cur_channel=imp.getChannel()-1;
		
		nAngles=30;
		addMembraneFeatures(19,1);
		
	}	
	
	public void addMembraneFeatures(int patchSize, int membraneSize)
	{
		ImagePlus newimg=NewImage.createFloatImage("Membrane filtered", width, height, channels*slices*frames, NewImage.FILL_BLACK);

		//create membrane patch
		ImageProcessor membranePatch = new FloatProcessor(patchSize, patchSize);
		int middle = Math.round(patchSize / 2);
		int startX = middle - (int) Math.floor(membraneSize/2.0);
		int endX = middle + (int) Math.ceil(membraneSize/2.0);
		
		for (int x=startX; x<=endX; x++)
			for (int y=0; y<patchSize; y++)
				membranePatch.setf(x, y, 1f);
											
		ImageProcessor [] rotatedPatches=new ImageProcessor[nAngles];
	    final double rotationAngle = 180/nAngles;
		for (int i=0; i<nAngles; i++)
		{
			ImageProcessor cur=membranePatch.duplicate();
			cur.rotate(i*rotationAngle);
			rotatedPatches[i]=cur;
		}

	    
	    // Get channel(s) to process

		final Convolver con = new Convolver();
		
		for (int s=0; s<slices; s++)
		{
			for (int f=0; f<frames; f++)
			{
				for (int c=0; c<channels; c++)
				{
					ImageStack proj_stack=new ImageStack(width,height);		
							
					for (int i=0; i<nAngles; i++)
					{
						ImageProcessor curI=imp.getStack().getProcessor(1+c+s*channels+f*channels*slices).duplicate();
						float [] kernel=(float[])(rotatedPatches[i].getPixels());
						con.convolveFloat(curI, kernel, patchSize, patchSize);
						proj_stack.addSlice(curI);
					}
					/*ImagePlus display=new ImagePlus("HiThere", proj_stack);
					display.show();
					display.updateAndDraw();*/
					float [] pix=(float [])newimg.getStack().getProcessor(1+c+s*channels+f*channels*slices).getPixels();
					for (int p=0; p<width*height; p++)
					{
						pix[p]=((float [])proj_stack.getProcessor(1).getPixels())[p];
					}
					for (int i=0; i<nAngles; i++)
					{
						for (int p=0; p<width*height; p++)
						{
							float tmp=((float [])proj_stack.getProcessor(i+1).getPixels())[p];
							if (tmp<pix[p]) pix[p]=tmp;
						}
					}
				}
			}
		}
		newimg.setOpenAsHyperStack(true);
		newimg.setDimensions(channels,  slices,  frames);
		newimg.show();
		newimg.updateAndDraw();
	}


}

