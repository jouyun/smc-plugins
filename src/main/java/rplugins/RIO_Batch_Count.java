//**********
//Given user inputed folder, this plugin finds all ROI zip files with the specified
//naming convention, sums the number of ROIs in those files, and creates a new file
//in that folder containing the total number or ROIs in the name.
//Created by Richard Alexander (RLA) for the Linheng Li Lab and the Stowers Institute for Medical Research.
//**********

import ij.*;
import ij.io.*;
import java.io.*;
import ij.plugin.*;
import ij.plugin.frame.RoiManager;
import ij.plugin.frame.RoiManager.*;
import ij.gui.*;

public class RIO_Batch_Count implements PlugIn{

	//public int TOTslices = 999999;	//This variable keeps track of the smallest stack.
	public File dir;
	public File[] fileList;
	public RoiManager roim;
	public File processDir;
	public String matchName="_FREDpos_GREENneg";
	
	public void run(String arg) {
		//get directory
		dir = new File(IJ.getDirectory("Choose a Directory "));		
		//get naming convention
		
		GenericDialog gd = new GenericDialog("Sum ROIs");
		gd.addStringField("Filename Contains: ", matchName, 30);
		gd.showDialog();
		if (gd.wasCanceled()) return;
		matchName = gd.getNextString();
		fileList = dir.listFiles();
		processFiles();
		IJ.showStatus("done");
	}
	
	public void processFiles(){
		IJ.run("ROI Manager...");
		roim = RoiManager.getInstance2();
		//make sure roim is empty before starting
		while(roim.getCount()>0){
			roim.runCommand("Delete");
		}
		for(int i = 0; i<fileList.length; i++){
			// What name will the zip file contain?
			if(fileList[i].toString().contains(matchName) && fileList[i].toString().endsWith(".zip") ){
				roim.runCommand("Open", fileList[i].toString());
				//IJ.log(""+roim.getCount());
			}
		}	
		int total = roim.getCount();
		//if(total>0){
			IJ.log(dir+"\\"+matchName+" total= "+total);
			File f = new File(dir+"\\"+matchName+" total= "+total);
			try{
				//f.mkdirs();
				f.createNewFile();
			}catch(Exception e){
				IJ.log("Error:  "+e);
			}
		// }else{
			// MessageDialog(this, "Error", "No files were found containing '"+matchName+"'in the name");
		// }
		roim.close();
	}
}
