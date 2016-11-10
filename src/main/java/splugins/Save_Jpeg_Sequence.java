package splugins;

import java.io.File;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

public class Save_Jpeg_Sequence implements PlugIn{

	@Override
	public void run(String arg0) {
		GenericDialog gd=new GenericDialog("Save JPEG");
		gd.addStringField("Title", "A");
		gd.addStringField("Directory", "/");
		gd.showDialog();
		String base_name=gd.getNextString();
		String direc=gd.getNextString();
		save(WindowManager.getCurrentImage(), base_name, direc);
	}
	
	static public void save(ImagePlus imp, String base_name, String directory)
	{
		for (int i=0; i<imp.getStack().getSize(); i++)
    	{
    		ImagePlus tmp=new ImagePlus("Title", (ImageProcessor)(imp.getStack().getProcessor(i+1).clone()));
    		IJ.saveAs(tmp, "Jpeg", directory+File.separator+base_name+IJ.pad(i+1, 8)+".jpg");
    	}
	}
	

}
