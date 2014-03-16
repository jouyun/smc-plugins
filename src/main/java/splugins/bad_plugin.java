package splugins;

import java.io.File;

import ij.IJ;
import ij.ImagePlus;
import ij.io.DirectoryChooser;
import ij.plugin.PlugIn;

public class bad_plugin implements PlugIn {

	@Override
	public void run(String arg0) {
		DirectoryChooser dc=new DirectoryChooser("Choose input");
		File indirec=new File(dc.getDirectory());
		String [] file_list=indirec.list();
		DirectoryChooser outdc=new DirectoryChooser("Choose output");
		for (int i=0; i<file_list.length; i++)
		{
			if (!file_list[i].endsWith(".tif")) continue;
			ImagePlus myimg=IJ.openImage(dc.getDirectory()+file_list[i]);
			//Histogram_Normalize_Percentile.NormalizeByte(myimg, 10.0, 90, 10, 220, 120);
			byte [] pix=(byte [])myimg.getProcessor().getPixels();
			for (int j=0; j<pix.length; j++) pix[j]=(byte) (pix[j]/(byte)2);
			IJ.saveAsTiff(myimg, outdc.getDirectory()+file_list[i]);
		}
	}

}
