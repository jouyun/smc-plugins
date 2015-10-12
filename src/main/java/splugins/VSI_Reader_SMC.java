package splugins;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Set;

import ij.IJ;
import ij.plugin.PlugIn;
import loci.formats.ChannelSeparator;
import loci.formats.FormatException;
import loci.formats.IFormatReader;
import loci.plugins.util.ImageProcessorReader;
import loci.plugins.util.LociPrefs;

public class VSI_Reader_SMC implements PlugIn {

	@Override
	public void run(String arg0) {
		// TODO Auto-generated method stub
		ImageProcessorReader r = new ImageProcessorReader(
				new ChannelSeparator(LociPrefs.makeImageReader()));
		try {
			try {
				IJ.log("Trying");
				r.setId("/home/smc/Data/SMC/OlympusScannerDemo/BatchStella/Folder_20151007/Image_Overview.vsi");
				IJ.log("Count, width, height: "+r.getImageCount()+","+r.getSizeX()+","+r.getSizeY());
				Hashtable<String, Object> metadata = r.getGlobalMetadata();
				Set <String> S=metadata.keySet();
				Object [] list_array=S.toArray();
				for (int i=0; i<list_array.length; i++)
				{
					String tmp=(String)list_array[i];
					IJ.log((String)list_array[i]+","+(String)metadata.get((String)list_array[i]));
				}
				String rtn=(String)metadata.get("SizeC");
				String val=(String)r.getMetadataValue("Origin #1");
				IJ.log("Going to tell you: ");
				IJ.log(rtn);
				IJ.log("I told you");
			} catch (FormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		catch(IOException exc)
		{
			IJ.error(exc.getMessage());
		}

	}

}
