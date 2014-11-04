package splugins;

import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.NewImage;
import ij.plugin.PlugIn;

public class ZProject_Min_Avg implements PlugIn {

	@Override
	public void run(String arg0) {
		
		ImagePlus img=WindowManager.getCurrentImage();
		ImagePlus new_img=process(img);
		new_img.show();
		new_img.updateAndDraw();

	}
	
	public static ImagePlus process(ImagePlus img)
	{
		int width=img.getWidth(), height=img.getHeight(), channels=img.getNChannels(), slices=img.getNSlices(), frames=img.getNFrames();
		ImagePlus new_img=NewImage.createFloatImage("Projection", width, height, channels*frames, NewImage.FILL_BLACK);
		for (int f=0; f<frames; f++)
		{
			float [] new_red=(float [])new_img.getStack().getProcessor(f*channels+1).getPixels();
			float [] new_green=(float[])new_img.getStack().getProcessor(f*channels+2).getPixels();
			float [] new_blue=(float[])new_img.getStack().getProcessor(f*channels+3).getPixels();
			boolean [] flags=new boolean[width*height];
			for (int s=0; s<slices; s++)
			{
				float [] red=(float [])img.getStack().getProcessor(f*channels*slices+s*channels+1).getPixels();
				float [] green=(float [])img.getStack().getProcessor(f*channels*slices+s*channels+2).getPixels();
				float [] blue=(float [])img.getStack().getProcessor(f*channels*slices+s*channels+3).getPixels();
				for (int i=0; i<blue.length; i++)
				{
					if (green[i]==0&&blue[i]==0) flags[i]=true;
					new_red[i]+=red[i];
					new_blue[i]+=blue[i];
					new_green[i]+=green[i];
				}
			}
			for (int i=0; i<new_red.length; i++)
			{
				new_green[i]=new_green[i]/slices;
				new_red[i]=new_red[i]/slices;
				new_blue[i]=new_blue[i]/slices;
				if (flags[i])
				{
					new_green[i]=0;
					new_red[i]=0;
					new_blue[i]=0;
				}
			}
		}
		return new_img;
	}

}
