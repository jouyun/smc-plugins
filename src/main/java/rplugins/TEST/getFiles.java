import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import ij.plugin.*;
import ij.plugin.frame.*;
import javax.swing.*;
import javax.swing.filechooser.*;
import java.io.*;

public class getFiles extends JPanel implements PlugIn {
	File[] files;

	public void run(String arg) {
		JDialog dialog = new JDialog();
        		JFileChooser fc = new JFileChooser();
		int returnVal = fc.showOpenDialog(dialog);
		if(returnVal == JFileChooser.APPROVE_OPTION){
			File dir = fc.getCurrentDirectory();
			files = dir.listFiles();
			for(File f : files){
				String name = f.getName();
				IJ.log(""+name);
			}
		}
	}

//		JFrame frame = new JFrame("dummy");
//		frame.add(fc);
//		frame.pack();
//		frame.setVisible(true);
  //      		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
}
