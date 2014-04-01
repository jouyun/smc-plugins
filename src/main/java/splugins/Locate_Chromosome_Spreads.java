package splugins;

import java.util.ArrayList;
import java.util.ListIterator;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;

public class Locate_Chromosome_Spreads implements PlugIn {

	@Override
	public void run(String arg0) {
		float pixel_size=0.5463f;
		
		//For now just process open image
		ImagePlus img=WindowManager.getCurrentImage();
		int width=img.getWidth(), height=img.getHeight();
		String info=img.getInfoProperty();
		
		//Extract the center of field
		int Xidx=info.indexOf("XPositionUm")+14;
		int Yidx=info.indexOf("YPositionUm")+14;
		int Xend=Xidx+1+info.substring(Xidx+1, info.length()).indexOf(",");
		int Yend=Yidx+1+info.substring(Yidx+1, info.length()).indexOf(",");
		
		float Xinit=Float.parseFloat(info.substring(Xidx,  Xend-1));
		float Yinit=Float.parseFloat(info.substring(Yidx, Yend-1));
		
		IJ.log("X base: "+Xinit+" Y base: "+Yinit);
		
		float [] fpix=new float[width*height];
		byte [] opix=(byte []) img.getProcessor().getPixels();
		for (int i=0; i<width*height; i++) fpix[i]=opix[i];
		ByteProcessor bp=new ByteProcessor(width, height, Percentile_Threshold.get_mask(fpix, width, height, 50, 20));
		bp.dilate();
		/*ImagePlus newimg=new ImagePlus("Whoah", (ImageProcessor)bp);
		newimg.show();
		newimg.updateAndDraw();*/
		short [] simg=new short [width*height];
		byte [] bpix=(byte []) bp.getPixels();
		for (int i=0; i<width*height; i++) simg[i]=bpix[i];
		Utility3D my3D=new Utility3D();
		ArrayList <ArrayList <int []>> mylist=my3D.find_blobs(simg, width, height, 1);
		my3D.filter_blob_array(mylist, 0, 15);
		simg=my3D.blobarray_to_imgarray(mylist, width, height, 1);
		for (int i=0; i<width*height; i++) 
		{
			if (simg[i]!=0) bpix[i]=(byte)255;
			else bpix[i]=(byte)0;
		}
		bp.erode();
		bp.erode();
		bp.erode();
		bp.erode();
		bp.erode();
		bp.erode();
		for (int i=0; i<width*height; i++) simg[i]=bpix[i];
		mylist=my3D.find_blobs(simg, width, height, 1);
		my3D.filter_blob_array(mylist, 1000, 10000000);
		simg=my3D.blobarray_to_imgarray(mylist, width, height, 1);
		ResultsTable rslt=ResultsTable.getResultsTable();
		for (ListIterator jF=mylist.listIterator(); jF.hasNext();)
		{
			rslt.incrementCounter();

			ArrayList <int []> current_blob=(ArrayList <int []>)jF.next();
			int [] rtn=Utility3D.find_center_of_mass(current_blob);
			IJ.log("X: "+rtn[0]+ " Y: "+rtn[1]);
			double X=(rtn[0]-width/2)*pixel_size, Y=(rtn[1]-height/2)*pixel_size;
			rslt.addValue("X", Xinit-X);
			rslt.addValue("Y", Yinit+Y);
		}
		rslt.show("Results");
	}

}
