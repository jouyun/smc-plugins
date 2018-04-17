package splugins;

import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.NewImage;
import ij.plugin.ChannelSplitter;
import ij.plugin.PlugIn;
import ij.plugin.RGBStackMerge;

public class Pad_To_Certain_Channels implements PlugIn {

	int width;
	int height;
	int slices;
	int cur_slice;
	int frames;
	int cur_frame;
	int cur_channel;
	int channels;
	ImagePlus imp;
	ImagePlus target_imp;
	ImagePlus new_imp;
	public void run (String arg)
	{
		imp=WindowManager.getCurrentImage();
		width=imp.getWidth(); 
		height=imp.getHeight();
		slices=imp.getNSlices();
		frames=imp.getNFrames();
		channels=imp.getNChannels();
		cur_slice=imp.getSlice()-1;
		cur_frame=imp.getFrame()-1;
		cur_channel=imp.getChannel()-1;
		
		GenericDialog gd=new GenericDialog("Number channels");
		gd.addNumericField("Number of channels:  ",3, 0);
		gd.showDialog();
		int number_channels=(int)gd.getNextNumber();
		
		if (number_channels<=channels) return;
		
		ImagePlus [] img_array = ChannelSplitter.split(imp);
		ImagePlus [] new_img_array=new ImagePlus [number_channels];
		
		for (int i=0; i<channels; i++) new_img_array[i]=img_array[i];
		for (int i=channels; i<number_channels; i++) new_img_array[i]=img_array[0].duplicate();
		
		ImagePlus new_img=RGBStackMerge.mergeChannels(new_img_array, false);
		String img_name=imp.getTitle();
		new_img.show();
		new_img.updateAndDraw();
		imp.close();
		new_img.setTitle(img_name);
	}

}
