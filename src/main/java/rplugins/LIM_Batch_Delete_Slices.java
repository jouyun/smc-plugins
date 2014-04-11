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

public class LIM_Batch_Delete_Slices implements PlugIn {

	public int TOTslices = 999999;	//This variable keeps track of the smallest stack.
	public File dir;
	public File[] fileList;

	public void run(String arg) {
		dir = new File(IJ.getDirectory("Choose a Directory "));
		fileList = dir.listFiles();
		
		checkFiles();
		processFiles();
		IJ.showStatus("done");
	}

	public void checkFiles(){

		for(int i = 0; i<fileList.length; i++){
			if(fileList[i].toString().endsWith(".tif") || fileList[i].toString().endsWith(".jpg") || fileList[i].toString().endsWith(".gif") || fileList[i].toString().endsWith(".zvi")){
				int nSlices;
				if(fileList[i].toString().endsWith(".tif")){
					FileInfo[] info=null;
					try{
						info = (new TiffDecoder(fileList[i].getParent()+"\\",fileList[i].getName())).getTiffInfo();
					} catch(IOException e){}
					FileInfo fi=info[0];
					nSlices=fi.nImages;
					//IJ.log(""+nSlices);
				}else{
					ImagePlus imp = new ImagePlus(fileList[i].toString());
					//imp.show();
					nSlices = imp.getStackSize();
					imp.close();
				}
				if(nSlices < TOTslices){
					TOTslices = nSlices;
				}
				//IJ.log(""+TOTslices);

			}
		}	
	}
	
	public void processFiles(){
	
		for(int i = 0; i<fileList.length; i++){
			if(fileList[i].toString().endsWith(".tif") || fileList[i].toString().endsWith(".jpg") || fileList[i].toString().endsWith(".gif") || fileList[i].toString().endsWith(".zvi")){
				ImagePlus imp = new ImagePlus(fileList[i].toString());
				//imp.show();
				int nSlices = imp.getStackSize();
				ImageStack istack = new ImageStack();
				istack = imp.getImageStack();
				if(nSlices != TOTslices){
					for(int j=TOTslices; j<nSlices; j++){
						istack.deleteLastSlice();
					}
				}
				String fname = dir+"\\sz_"+fileList[i].getName();
				new FileSaver(imp).saveAsTiffStack(fname);
				imp.close();

			}
		}	

	}


}



