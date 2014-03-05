import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import java.io.*;
import java.lang.reflect.Array;
import java.util.Arrays;
import ij.plugin.*;
import ij.io.FileInfo;
import javax.swing.JOptionPane;


public class HNC_Stack_Splicer implements PlugIn {

	ImageStack newStack = new ImageStack(512,512);
	String newTitle = "";
	int width = 0;
	int height = 0;
	int nChannels = 0;
	int nSlices = 0;
	int nFrames = 0;

	public void run(String arg) {

		String slicesBellowStr= JOptionPane.showInputDialog("Number of slices bellow max:   "); 
		String totSlicesStr= JOptionPane.showInputDialog("Total number of slices:   "); 
		
		int slicesBellow = Integer.parseInt(slicesBellowStr);
		int totSlices = Integer.parseInt(totSlicesStr);

		String directory = IJ.getDirectory("Choose a Directory "); //***Contains backslash on end***
		File dir = new File(directory);
		String[] FileList = dir.list();
		Arrays.sort(FileList);
//		IJ.log(directory);
//		for(int xy = 0; xy<Array.getLength(FileList); xy++){
//			IJ.log(FileList[xy]);
//		}

			

		 for (int j=0; j<Array.getLength(FileList); j++) {
        			String path = directory+FileList[j];
			if(FileList[j].endsWith(".lsm")){
				if (!path.endsWith("/")){

					IJ.open(path);
					int chan = 1;	//*********Input which channel contains the flourescence***********
					int t = 1;
		
					ImagePlus imp1 = WindowManager.getCurrentImage();
					String origTitle = imp1.getTitle();
					int[] dim1 = imp1.getDimensions();
					width = dim1[0];
					height = dim1[1];
					nChannels = dim1[2];
					nSlices = dim1[3];
					nFrames = dim1[4];
					ImageStack myStack = imp1.getStack();
			
					int numPix = width*height;
					int[] maxIntens = new int[nSlices];
					int[] topSlice = new int[2];
					topSlice[0]=0;
					topSlice[1]=0;
					int topSliceIndex = 0;
					//***Get the pixels for each slice in the above specified fluoreschence channel***
					for(int z=1; z<=nSlices; z++){
						int index = imp1.getStackIndex(chan, z, t);
						//IJ.log("" +index);
						short[] mypixels = (short[])myStack.getPixels(index);
			
							//***Now sum the pixels***
						int sumpix = 0;
						for(int n=0; n<numPix; n++){
							sumpix = sumpix + mypixels[n];
						}
						maxIntens[z-1] = sumpix;
							//***Now find the max Intensity plane***
						if(topSlice[1] < sumpix){
							topSlice[0] = z;
							topSlice[1] = sumpix;
							topSliceIndex = index;
						}
						//IJ.log(""+topSliceIndex);
						//IJ.log(""+z+":  "+maxIntens[z-1]);
					}
			
			
					
					for(int index = topSliceIndex-(slicesBellow*nChannels); index<=topSliceIndex-(slicesBellow*nChannels)+(totSlices*nChannels)-1; index++){		//***Assuming red fluorescence is Ch1, this applies the user inputed number of slices bellow max and total slices.***
						//int index = imp1.getStackIndex(chan, z, t);
						String label = myStack.getSliceLabel(index);
						newStack.addSlice(label, myStack.getProcessor(index));
					}
					newTitle = origTitle + "_processed";
					imp1.close();
				}
			}

		}
		ImagePlus newImage = new ImagePlus(newTitle, newStack);
		int[] newDim = newImage.getDimensions();	//***Channels = newDim[2]  and Slices = newDim[3]***
		newImage.setDimensions(nChannels,newDim[3]/nChannels,newDim[4]);
		newImage.show();
		newImage.setOpenAsHyperStack(true);
		newImage.setPosition(2,1,1);
		newImage.resetDisplayRange();
		IJ.run("Grays");
		IJ.save(newImage,directory+newTitle+".tif");
		//newImage.close();

	}

}
