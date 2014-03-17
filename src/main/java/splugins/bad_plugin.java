package splugins;

import java.io.File;

import ij.IJ;
import ij.ImagePlus;
import ij.io.DirectoryChooser;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;

public class bad_plugin implements PlugIn {

	@Override
	public void run(String arg0) {
		DirectoryChooser dc=new DirectoryChooser("Choose input");
		File indirec=new File(dc.getDirectory());
		String [] file_list=indirec.list();
		DirectoryChooser outdc=new DirectoryChooser("Choose output");
		ResultsTable results=ResultsTable.getResultsTable();
		for (int i=0; i<file_list.length; i++)
		{
			if (!file_list[i].endsWith(".tif")) continue;
			ImagePlus myimg=IJ.openImage(dc.getDirectory()+file_list[i]);
			//Histogram_Normalize_Percentile.NormalizeByte(myimg, 10.0, 90, 10, 220, 120);
			byte [] pix=(byte [])myimg.getProcessor().getPixels();
			int max=0, min=2000;
			for (int j=0; j<pix.length; j++) 
			{
				int tmp=pix[j]&0xff;
				if (tmp<min) min=tmp;
				if (tmp>max) max=tmp;
				pix[j]=(byte) (pix[j]/(byte)2);
			}
			results.incrementCounter();
			results.addValue("File", file_list[i]);
			results.addValue("Min", min);
			results.addValue("Max", max);
			IJ.saveAsTiff(myimg, outdc.getDirectory()+file_list[i]);
		}
		results.show("Results");
	}
}
