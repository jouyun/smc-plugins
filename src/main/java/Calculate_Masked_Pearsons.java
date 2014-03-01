import java.util.Stack;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;


public class Calculate_Masked_Pearsons implements PlugIn {

	@Override
	public void run(String arg) {
		// TODO Auto-generated method stub
		final ImagePlus[] admissibleImageList = createAdmissibleImageList();
		final String[] sourceNames = new String[admissibleImageList.length];	
		if (admissibleImageList.length == 0) return;
		for (int k = 0; (k < admissibleImageList.length); k++) 
		{
			sourceNames[k]=admissibleImageList[k].getTitle();
		}
		
		GenericDialog gd = new GenericDialog("In situ process");
		gd.addChoice("First Channel:", sourceNames, admissibleImageList[0].getTitle());
		gd.addChoice("Second Channel:", sourceNames, admissibleImageList[0].getTitle());
		gd.addChoice("Mask:", sourceNames, admissibleImageList[0].getTitle());
		gd.showDialog();
		if (gd.wasCanceled()) 
		{
			return;
		}
		ImagePlus A_img, B_img, mask_img;
		A_img=admissibleImageList[gd.getNextChoiceIndex()];
		B_img=admissibleImageList[gd.getNextChoiceIndex()];
		mask_img=admissibleImageList[gd.getNextChoiceIndex()];
		
		float [] A_pix=(float []) A_img.getProcessor().convertToFloat().getPixels();
		float [] B_pix=(float []) B_img.getProcessor().convertToFloat().getPixels();
		byte [] mask_pix=(byte []) mask_img.getProcessor().getPixels();
		int width=A_img.getWidth();
		int height=A_img.getHeight();
		double A_avg=0.0, B_avg=0.0;
		int ctr=0;
		for (int i=0; i<width*height; i++)
		{
			if (mask_pix[i]==0) continue;
			A_avg+=A_pix[i];
			B_avg+=B_pix[i];
			ctr++;
		}
		
		A_avg=A_avg/(double)ctr;
		B_avg=B_avg/(double)ctr;
		//IJ.log("ctr: "+ctr+" A avg: " + A_avg + " B avg: "+B_avg);
		double dA, dB;
		double pearson=0, A2=0, B2=0;
		for (int i=0; i<width*height; i++)
		{
			if (mask_pix[i]==0) continue;
			dA=(double)A_pix[i]-A_avg;
			dB=(double)B_pix[i]-B_avg;
			pearson=pearson+dA*dB;
			A2=A2+dA*dA;
			B2=B2+dB*dB;
		}
		//IJ.log("Pearson: "+pearson+ " A2: "+A2 + " B2: "+B2);
		IJ.log("Pearson coefficient:  "+(pearson/Math.sqrt(A2*B2)));
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
