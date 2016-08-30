package splugins;

import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.NewImage;
import ij.plugin.PlugIn;

public class Create_Stack_From_SubRegions implements PlugIn {
	ImagePlus imp;
	@Override
	public void run(String arg0) {
		imp=WindowManager.getCurrentImage();
		
		//Ask for names
		GenericDialog gd=new GenericDialog("Pick params");
		gd.addNumericField("Window size:", 64, 0);
		gd.showDialog();
		int window_size=(int)gd.getNextNumber();
		
		ImagePlus new_img=GetSubRegionStack(imp, window_size);
		new_img.show();
		new_img.updateAndDraw();

	}
	
	static public ImagePlus GetSubRegionStack(ImagePlus inp, int window_size)
	{
		int width=inp.getWidth();
		int height=inp.getHeight();
		float [] pix=(float []) inp.getProcessor().getPixels();
		int x_tiles=(int) Math.floor(width/window_size*2-1);
		int y_tiles=(int) Math.floor(height/window_size*2-1);
		ImagePlus new_img=NewImage.createFloatImage("TempImg", window_size, window_size, x_tiles*y_tiles, NewImage.FILL_BLACK);
		for (int xt=0; xt<x_tiles; xt++)
		{
			for (int yt=0; yt<y_tiles; yt++)
			{
				float [] newpix=(float [])new_img.getStack().getProcessor(xt+yt*x_tiles+1).getPixels();
				for (int x=0; x<window_size; x++)
				{
					for (int y=0; y<window_size; y++)
					{
						newpix[x+y*window_size]=pix[xt*window_size/2+x+(yt*window_size/2+y)*width];
					}
				}
			}
		}
		return new_img;
	}

}
