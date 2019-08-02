package splugins;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.NewImage;
import ij.plugin.PlugIn;

public class Translate_XYZ implements PlugIn {

	@Override
	public void run(String arg0) {
		// TODO Auto-generated method stub
		ImagePlus img=WindowManager.getCurrentImage();
		GenericDialog dlg=new GenericDialog("Dialog");
		dlg.addNumericField("X shift" , 0, 0);
		dlg.addNumericField("Y shift" , 0, 0);
		dlg.addNumericField("Z shift" , 0, 0);
		dlg.showDialog();
		
		int xshift=(int)dlg.getNextNumber();
		int yshift=(int)dlg.getNextNumber();
		int zshift=(int)dlg.getNextNumber();
		
		int slices=img.getNSlices();
		int frames=img.getNFrames();
		int channels=img.getNChannels();
		
		int width=img.getWidth();
		int height=img.getHeight();
		
		img=WindowManager.getCurrentImage();
		slices=img.getNSlices();
		frames=img.getNFrames();
		channels=img.getNChannels();
		width=img.getWidth();
		height=img.getHeight();
		int nslices=slices+2*(int)Math.abs(zshift);
		
		ImagePlus new_img=NewImage.createFloatImage("Result", width, height, channels*(nslices)*frames, NewImage.FILL_BLACK);
		
		for (int c=0; c<channels; c++)
		{
			for (int f=0; f<frames; f++)
			{
				for (int s=0; s<slices; s++)
				{
					float [] pix=(float [])img.getStack().getProcessor(1+c+s*channels+f*channels*slices).getPixels();
					float [] npix=(float [])new_img.getStack().getProcessor(1+c+(s+zshift+(int)Math.abs(zshift))*channels+f*channels*nslices).getPixels();
					System.arraycopy(pix,  0,  npix,  0,  width*height);
				}
			}
		}
		new_img.setDimensions(channels,  nslices,  frames);
		new_img.setDisplayMode(IJ.COMPOSITE);
		new_img.setOpenAsHyperStack(true); 
		new_img.setDisplayRange(0, 100000);
		new_img.updateAndDraw();
		new_img.show();
		
		IJ.run("Canvas Size...", "width="+(width+2*(int)Math.abs(xshift))+" height="+(height+2*(int)Math.abs(yshift))+" position=Center zero");
		IJ.run("Translate...", "x="+xshift+" y="+yshift+" interpolation=None stack");


	}

}
