package jguis;

import java.awt.Rectangle;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import ij.IJ;
import ij.gui.PointRoi;
import ij.gui.Roi;
import ij.io.RoiEncoder;

public class multi_roi_writer{
	
	public static boolean writeRois(Roi[] rois,String path){
		if(rois.length<1) return false;
		try{
			ZipOutputStream zos=new ZipOutputStream(new FileOutputStream(path));
			DataOutputStream out=new DataOutputStream(new BufferedOutputStream(zos));
			RoiEncoder re=new RoiEncoder(out);
			for(int i=0;i<rois.length;i++){
				if(rois[i]!=null){
					String label=rois[i].getName();
					if(!label.endsWith(".roi")) label+=".roi";
					zos.putNextEntry(new ZipEntry(label));
					re.write(rois[i]);
					out.flush();
				}
			}
			out.close();
			return true;
		} catch(IOException e){
			IJ.log(e.toString());
			return false;
		}
	}
	
	public static Roi[] makeRois(float[][] coords){
		Roi[] rois=new Roi[coords.length];
		for(int i=0;i<coords.length;i++){
			int x=(int)coords[i][0];
			int y=(int)coords[i][1];
			int z=1+(int)coords[i][2];
			Roi temp=new PointRoi(x,y);
			temp.setName(getRoiLabel(temp,z));
			rois[i]=temp;
		}
		return rois;
	}
	
	public static String getRoiLabel(Roi roi,int slice){
		//basically this is slice-yc-xc with at minimum 4 digits
		//note that slice is the slice index (channel+slice*channels+frame*slices*channels+1), not the actual z slice
		Rectangle r=roi.getBounds();
		int xc=r.x+r.width/2;
		int yc=r.y+r.height/2;
		String xval="0000"+xc;
		xval=xval.substring(xval.length()-4);
		String yval="0000"+yc;
		yval=yval.substring(yval.length()-4);
		String zval="0000"+slice;
		zval=zval.substring(zval.length()-4);
		return zval+"-"+yval+"-"+xval;
	}
	
}
