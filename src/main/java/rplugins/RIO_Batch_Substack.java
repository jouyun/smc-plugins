package rplugins;
//**********
//Given user inputed folder, this plugin gets FileInfo for every image in the folder (without opening for TIF),
//measures the stack size,and remembers the smallest stack size. It then opens each image and
//deletes slices off the end of the stack until every image is the same size as the smallest image
//stack. Output images aresaved with prescript "sz_" (for sized). Looks only for .tif / .jpg / .gif / or .zvi
//images to open. 
//Created by Richard Alexander (RLA) for Limei Ma and the Stowers Institute for Medical Research.
//**********

import ij.*;
import ij.io.*;
import java.io.*;
import ij.plugin.*;

public class RIO_Batch_Substack implements PlugIn {

	//public int TOTslices = 999999;	//This variable keeps track of the smallest stack.
	public File dir;
	public File[] fileList;

	public void run(String arg) {
		dir = new File(IJ.getDirectory("Choose a Directory "));
		fileList = dir.listFiles();
		makeFolder();
		processFiles();
		IJ.showStatus("done");
	}
	
	public void makeFolder(){
		File processDir = new File(dir+"\\processed");
		if (!processDir.exists()){
			processDir.mkdir();
		}
	}
	
	public void processFiles(){
	
		for(int i = 0; i<fileList.length; i++){
			if(fileList[i].toString().endsWith(".tif") || fileList[i].toString().endsWith(".tiff") || fileList[i].toString().endsWith(".jpg") || fileList[i].toString().endsWith(".gif") || fileList[i].toString().endsWith(".zvi")){
				ImagePlus imp = new ImagePlus(fileList[i].toString());
				//imp.show();
				ImageStack istack = new ImageStack();
				istack = imp.getImageStack();
				////int[] delArray = {12,11,9,8,6,5,3,2}; //keep 1st of every 3 images
				////int[] delArray = {12,10,9,7,6,4,3,1}; //keep 2nd of every 3 images
				int[] delArray = {11,10,8,7,5,4,2,1}; //keep 3rd of every 3 images
				for(int slice : delArray){
					istack.deleteSlice(slice);
				}
				String fname = dir+"\\processed\\sub3_"+fileList[i].getName();
				new FileSaver(imp).saveAsTiffStack(fname);
				imp.close();

			}
		}	

	}

}



