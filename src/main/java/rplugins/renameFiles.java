package rplugins;
import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import ij.plugin.*;
import ij.plugin.frame.*;
import java.io.*;

public class renameFiles implements PlugIn {

	public void run(String arg) {
		File dir = new File(IJ.getDirectory("Choose a Directory "));
		File[] fileList = dir.listFiles();
		 
		for (int i = 0; i < fileList.length; i++) {
		 	IJ.log("orig:    " + dir.getAbsolutePath()
							+ "\\" + fileList[i]);
		 
			File oldFile = fileList[i];
			int index = oldFile.getName().indexOf(".");
			
			if(!oldFile.isDirectory() && index >= 0 && index <5){
				File newFile = new File(dir + "\\" +oldFile.getName().replaceFirst("\\.", "_")); //must escape the "." character
			 	IJ.log("new:      " + newFile);
				
				boolean isFileRenamed = oldFile.renameTo(newFile);
			 
				if (isFileRenamed)
					IJ.log("......renamed");
				else
					IJ.log("......Error");
			}
			
		}
	}

}
