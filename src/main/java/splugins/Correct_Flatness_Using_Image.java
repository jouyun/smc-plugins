package splugins;

import java.util.Stack;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;

public class Correct_Flatness_Using_Image implements PlugIn {

	@Override
	public void run(String arg0) {
		ImagePlus img=WindowManager.getCurrentImage();
		final ImagePlus [] lst = createAdmissibleImageList();
		
		final String[] sourceNames = new String[lst.length];	
		//IJ.log("Good ones:  "+admissibleImageList.length+"\n");		
		for (int k = 0; (k < lst.length); k++) 
		{
			sourceNames[k]=lst[k].getTitle();
		}
		
		GenericDialog gd = new GenericDialog("Correct Flatness From Image");
		gd.addChoice("Correction image:", sourceNames, lst[0].getTitle());
		gd.showDialog();
		int sourceIndex=gd.getNextChoiceIndex();
		
		ImagePlus correction_img = lst[sourceIndex];
		ImagePlus new_img=Correct_Flatness.DoCorrectUsingProvided(img, correction_img);
		new_img.setOpenAsHyperStack(true);
		new_img.setDimensions(img.getNChannels(), img.getNSlices(), img.getNFrames());
		new_img.show();
		new_img.updateAndDraw();
		
	}

	private ImagePlus[] createAdmissibleImageList () 
	{
		final int[] windowList = WindowManager.getIDList();
		//IJ.log(""+windowList[0]+"\n");
		final Stack stack = new Stack();
		for (int k = 0; ((windowList != null) && (k < windowList.length)); k++) 
		{
			final ImagePlus imp = WindowManager.getImage(windowList[k]);
			//IJ.log(imp.getTitle()+"\n");
			//if ((imp != null) && (imp.getType() == imp.GRAY32)) 
			{
				//IJ.log("got one");
				stack.push(imp);
			}
		}
		//IJ.log("Stack size:  " + stack.size() + "\n");
		final ImagePlus[] admissibleImageList = new ImagePlus[stack.size()];
		int k = 0;
		while (!stack.isEmpty()) {
			admissibleImageList[k++] = (ImagePlus)stack.pop();
		}
		if (k==0 && (windowList != null && windowList.length > 0 )){
			IJ.error("No float images, convert to float and try again");
		}
		return(admissibleImageList);
	} /* end createAdmissibleImageList */
}
