package splugins;

import java.io.File;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.Duplicator;
import ij.plugin.PlugIn;

public class SaveMultipageImageSequence implements PlugIn {

	@Override
	public void run(String arg0) {
		ImagePlus img=WindowManager.getCurrentImage();
		int frames=img.getNFrames();
		GenericDialog gd = new GenericDialog("Save Multipage Image Sequence");
		gd.addStringField("Path to save to:  ", "C:\\Users\\smc\\Desktop\\tmp\\");
		gd.showDialog();
		
		String save_directory=gd.getNextString(); 
		save_sequence(img, save_directory);
	}
	
	static public void save_sequence(ImagePlus img, String save_directory)
	{
		int frames=img.getNFrames();
		for (int f=0; f<frames; f++)
		{
			ImagePlus current_img=new Duplicator().run(img, 1, img.getNChannels(), 1, img.getNSlices(), f+1, f+1);
			current_img.show();
			current_img.updateAndDraw();
			IJ.run("Save As Tiff", "save=["+save_directory+File.separator+"Tiffs"+IJ.pad(f, 4)+".tif]");
			//IJ.saveAsTiff(current_img, save_directory+File.separator+"Tiffs"+IJ.pad(f, 4)+".tif");
			current_img.close();
		}
	}
	
	static public void save_sequence(ImagePlus img, String save_directory, String file_prefix)
	{
		int frames=img.getNFrames();
		for (int f=0; f<frames; f++)
		{
			ImagePlus current_img=new Duplicator().run(img, 1, img.getNChannels(), 1, img.getNSlices(), f+1, f+1);
			current_img.show();
			current_img.updateAndDraw();
			IJ.run("Save As Tiff", "save=["+save_directory+File.separator+file_prefix+IJ.pad(f, 4)+".tif]");
			//IJ.saveAsTiff(current_img, save_directory+File.separator+file_prefix+IJ.pad(f, 4)+".tif");
			current_img.close();
		}
	}
	

}
