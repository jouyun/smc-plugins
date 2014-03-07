package rplugins;
import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import ij.plugin.*;
import ij.plugin.frame.*;
import javax.swing.*;
import javax.swing.filechooser.*;
import ij.io.DirectoryChooser;
import java.io.*;

public class BatchRename implements PlugIn {

	public void run(String arg) {
		//JFileChooser fc = new JFileChooser();
		//fc.showOpenDialog(this);
		
		//File dir= fc.getSelectedFile();

		DirectoryChooser dc = new DirectoryChooser("Choose Folder to Batch");
		String directory = dc.getDirectory();
		File dir = new File(directory);
		File[] files = dir.listFiles();
		for(File f : files){
			String name =  f.getName();
			if(name.contains(".CNTRL.")){
				String path = dir.getPath();
				if(!path.endsWith(File.separator)){
					path = path+File.separator;
				}
				File newName = new File(path+name.replace(".CNTRL.", "_CNTRL_"));
				//IJ.log(path+name.replace(" copy", ""));
				f.renameTo(newName);
			}
		}
	}


}
