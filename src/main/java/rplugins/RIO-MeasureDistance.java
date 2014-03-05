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
import ij.measure.*;
import ij.process.*;
import ij.plugin.frame.RoiManager;
import ij.plugin.frame.RoiManager.*;
import java.awt.Rectangle;
import ij.gui.*;
import ij.macro.Interpreter;
import java.util.*;
import java.util.zip.*;

public class RIO-MeasureDistance implements PlugIn {

	//public int TOTslices = 999999;	//This variable keeps track of the smallest stack.
	public File dir;
	public File[] fileList;
	public RoiManager roim;

	public void run(String arg) {
		//dir = new File(IJ.getDirectory("Choose a Directory "));
		//fileList = dir.listFiles();
		//processFiles();
		measureDistance()
		IJ.showStatus("done");
	}
	
	public void measureDistance(){
		ImagePlus imp = IJ.getImage();
		int centerx = 500;	//object center
		int centery = 500;
		int width = imp.width;	//image width
		int height = imp.height;	//image height
		int diag = (int)ceil(sqrt((width*width)+(height*height)));	//this is the longest distance the points could be from each other
		//first take the mean to find a region to search
		
		//then search pixels above 0
		int foundx = -1;
		int foundy = -1;
		double dist = diag+100;
		for(int num=1; num<diag; num++){
			int negnum = 0-num;
			for(int x=negnum; x<=num; x++){
				if(abs(x)==num){
					for(int y=negnum; y<=num; y++){
						int[] pixArray = imp.getPixel(x,y);
						float pixel = (float)pixArray[0];	//assume grayscale image
						if(pixel>0){
							int distTest = sqrt(((centerx-x)*(centerx-x))+((centery-y)*(centery-y)));
							if(distTest<dist){
								dist = distTest;
								foundx = x;
								foundy = y;
							}
						}
					}
				}else{
					//only need pos and neg num
					int y = negnum;
					int[] pixArray = imp.getPixel(x,y);
					float pixel = (float)pixArray[0];	//assume grayscale image
					if(pixel>0){
						int distTest = sqrt(((centerx-x)*(centerx-x))+((centery-y)*(centery-y)));
						if(distTest<dist){
							dist = distTest;
							foundx = x;
							foundy = y;
						}
					}
					y=num;
					pixArray = imp.getPixel(x,y);
					pixel = (float)pixArray[0];	//assume grayscale image
					if(pixel>0){
						int distTest = sqrt(((centerx-x)*(centerx-x))+((centery-y)*(centery-y)));
						if(distTest<dist){
							dist = distTest;
							foundx = x;
							foundy = y;
						}
					}
				}
			}
			if(dist<=diag){
				break;
			}
		}
		
		IJ.log("x:"+foundx+"  y:"+foundy+"   dist:"+dist);
	}
	
	public void processFiles(){
		//expecting a stack with 4 z-slices... Blue, green, red, far red.
		for(int i = 0; i<fileList.length; i++){
			if(fileList[i].toString().endsWith(".tif") || fileList[i].toString().endsWith(".tiff") || fileList[i].toString().endsWith(".jpg") || fileList[i].toString().endsWith(".gif") || fileList[i].toString().endsWith(".zvi")){
				String imageName = fileList[i].getName();
				ImagePlus imp = new ImagePlus(fileList[i].toString());
				imp.setSlice(2);
				ImageStatistics stats = imp.getStatistics();
				if(stats.max>500){	//only process image if green chanel has stuff (no blank images)
					imp.show();
					//ImageStack istack = new ImageStack();
					//istack = imp.getImageStack();
					
					//Do far red mask (positive)
					imp.setSlice(4); 
					String kernel = "title=farRed";
					IJ.run("Duplicate...",kernel);
					IJ.selectWindow("farRed");
					ImagePlus impRed = IJ.getImage();
					IJ.setAutoThreshold(impRed, "Triangle dark");
					IJ.run("Convert to Mask");
					IJ.run("Dilate");
					IJ.run("Dilate");
					IJ.run("Fill Holes");
					IJ.run("Erode");
					IJ.run("Erode");
					
					IJ.run("Analyze Particles...", "size=20-Infinity circularity=0.00-1.00 show=Nothing include add");
					kernel = "Save="+dir+"\\processed\\"+imageName+"_roi.zip";
					IJ.run("ROI Manager...",kernel);
					
					// Do green mask (negative)
					IJ.selectWindow(imageName);
					imp.setSlice(2); 
					kernel = "title=green";
					IJ.run("Duplicate...",kernel);
					IJ.selectWindow("green");
					ImagePlus impGreen = IJ.getImage();
					IJ.setAutoThreshold(impGreen, "Triangle dark");
					IJ.run("Convert to Mask");
					
					// Loop through ROIs in RoiManager
					roim = RoiManager.getInstance2();	//Interpreter interp = Interpreter.getInstance(); //RoiManager roim =interp.getBatchModeRoiManager(); //
					int countAll = roim.getCount();
					
					
					//IJ.log(""+countAll);
					Roi[] roiArray = roim.getRoisAsArray();
					List<Integer> goodIndex = new ArrayList<Integer>();
					int[] allIndex = new int[countAll];
					for(int q=0; q<countAll; q++){
						allIndex[q] = q;
						Roi roi = roiArray[q];
						int current =q;
						//IJ.log(""+current);
						if(getStats(roi)){
							goodIndex.add(q);
						}
					}
					//Integer[] indexesInteger = goodIndex.toArray(new Integer[goodIndex.size()]);
					int[] indexes = toIntArray(goodIndex);
					String fname = dir+"\\"+imageName+"_pos("+countAll+").zip";
					if(countAll>0){
						saveMultiple(roiArray, allIndex, fname);	//save all
					}
					int countGood = indexes.length;
					fname = dir+"\\"+imageName+"_posneg("+countGood+").zip";
					if(countGood>0){
						saveMultiple(roiArray, indexes, fname);	//save only good ones
					}
					IJ.run("Close All");
					roim.close();
				}
			}
		}	
	}
	
	int[] toIntArray(List<Integer> list){
		int[] ret = new int[list.size()];
		for(int i = 0;i < ret.length;i++)
		ret[i] = list.get(i);
		return ret;
	}
	
	boolean saveMultiple(Roi[] roiArray, int[] indexes, String path) {
        try {
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(path));
            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(zos));
            RoiEncoder re = new RoiEncoder(out);
            for (int i=0; i<indexes.length; i++) {
                String label = roim.getName(indexes[i]);	//list.getItem(indexes[i]);
                Roi roi = roiArray[indexes[i]];				//(Roi)rois.get(label);
                if (!label.endsWith(".roi")) label += ".roi";
                zos.putNextEntry(new ZipEntry(label));
                re.write(roi);
                out.flush();
            }
            out.close();
        }
        catch (IOException e) {
            IJ.log(""+e);
            return false;
        }
        return true;
    }
	
	public boolean getStats(Roi roi){
		//String imageName = "green";
		//IJ.selectWindow(imageName);
		ImagePlus imp = IJ.getImage();
		//OvalRoi roi = new OvalRoi(538, 237, 110, 104);
		Rectangle boundRec = roi.getBounds();
		//IJ.log("x:"+boundRec.x+"  y:"+boundRec.y+"  width:"+boundRec.width+"  height:"+boundRec.height);
		float max = -1;
		float min = -1;
		float sum = 0;
		int count = 0;
		for(int i=boundRec.x; i<boundRec.x+boundRec.width; i++){
			//IJ.log(""+i);
			for(int j = boundRec.y; j<boundRec.y+boundRec.height; j++){
				//IJ.log(""+j);
				if(roi.contains(i,j)){
					int[] pixArray = imp.getPixel(i,j);
					float pixel = (float)pixArray[0];	//assume grayscale image
					sum = sum+pixel;
					count = count+1;
					if(max > -1){
						if(pixel>max){max=pixel;}
						if(pixel<min){min=pixel;}
					}else{
						max = pixel;
						min = pixel;
					}
				}
			}
		}
		float average = sum/((float)count);
		//IJ.log("area:"+count+"  avg:"+average +"  min:"+min+"  max:"+max);
		if(max==0){
			return true;
		}else{
			return false;
		}
	}

}
