package splugins;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.NewImage;
import ij.plugin.ZProjector;

public class stitch_generic {
	static public void stitch_img(ImagePlus imp, float [] x_pos, float [] y_pos, String save_directory, int channel_to_project)
	{
		write_tile_config( save_directory, x_pos, y_pos);
		project_and_dump_to_directory(imp, save_directory, channel_to_project);
		do_stitch(save_directory, imp.getNChannels(), imp.getNSlices());
	}
	
	static public void stitch_projection_img(ImagePlus imp, float [] x_pos, float [] y_pos, String save_directory)
	{
		write_tile_config( save_directory, x_pos, y_pos);
		project_all_and_dump_to_directory(imp, save_directory);
		do_stitch_no_delete(save_directory, imp.getNChannels(), imp.getNSlices());
	}
	
	static public void project_and_dump_to_directory(ImagePlus imp, String save_directory, int channel_to_project)
	{
		int width=imp.getWidth(); 
		int height=imp.getHeight();
		int slices=imp.getNSlices();
		int frames=imp.getNFrames();
		int channels=imp.getNChannels();
		
		ZProjector zproj=new ZProjector(imp);
		zproj.setMethod(ZProjector.MAX_METHOD);
		zproj.setStartSlice(1);
		zproj.setStopSlice(slices);
		zproj.doHyperStackProjection(true);
		ImagePlus z_projection=zproj.getProjection();
		
		for (int f=0; f<frames; f++)
		{
			ImagePlus new_img=NewImage.createShortImage("NewImg", width, height, channels*slices+1, NewImage.FILL_BLACK);
			System.arraycopy((short [])(z_projection.getStack().getProcessor(channel_to_project+1+f*channels).getPixels()), 0, (short [])new_img.getStack().getProcessor(1).getPixels(), 0, width*height);
			for (int c=0; c<channels; c++)
			{
				for (int z=0; z<slices; z++)
				{
					short [] src=(short [])imp.getStack().getProcessor(c+1+z*channels+f*channels*slices).getPixels();
					short [] dest=(short [])new_img.getStack().getProcessor(c+1+z*channels+1).getPixels();
					System.arraycopy(src, 0, dest, 0, width*height);
				}
			}
			new_img.setDimensions(channels*slices+1, 1, 1);
			IJ.saveAsTiff(new_img, save_directory+"Img"+IJ.pad(f, 4)+".tif");
		}
		
	}
	
	static public void project_all_and_dump_to_directory(ImagePlus imp, String save_directory)
	{
		project_all_and_dump_to_directory(imp, save_directory, ZProjector.MAX_METHOD);
	}
	
	static public void project_all_and_dump_to_directory(ImagePlus imp, String save_directory, int projection_method)
	{
		int width=imp.getWidth(); 
		int height=imp.getHeight();
		int slices=imp.getNSlices();
		int frames=imp.getNFrames();
		int channels=imp.getNChannels();
		
		ZProjector zproj=new ZProjector(imp);
		zproj.setMethod(projection_method);
		zproj.setStartSlice(1);
		zproj.setStopSlice(slices);
		zproj.doHyperStackProjection(true);
		ImagePlus z_projection=zproj.getProjection();
		
		SaveMultipageImageSequence.save_sequence(z_projection, save_directory, "Img");
		
	}
	
	static public void do_stitch(String save_directory, int channels, int slices)
	{
		IJ.run("Grid/Collection stitching", "type=[Positions from file] order=[Defined by TileConfiguration] directory=["+save_directory+"] layout_file=out.txt fusion_method=[Linear Blending] regression_threshold=0.30 max/avg_displacement_threshold=2.50 absolute_displacement_threshold=3.50 compute_overlap computation_parameters=[Save computation time (but use more RAM)] image_output=[Fuse and display]");
		//IJ.run("Grid/Collection stitching", "type=[Positions from file] order=[Defined by TileConfiguration] directory="+save_directory+" layout_file=out.txt fusion_method=[Linear Blending] regression_threshold=0.30 max/avg_displacement_threshold=2.50 absolute_displacement_threshold=3.50 computation_parameters=[Save computation time (but use more RAM)] image_output=[Fuse and display]");
		if (WindowManager.getCurrentImage().getTitle().equals("Fused")) 
		{
			IJ.log("Channels slices" +channels+" "+slices);
			IJ.log("Image channels slices frames "+WindowManager.getCurrentImage().getNChannels()+" "+WindowManager.getCurrentImage().getNSlices()+" "+WindowManager.getCurrentImage().getNFrames()+" " );
			IJ.run("Delete Slice", "delete=channel");
			IJ.run("Stack to Hyperstack...", "order=xyczt(default) channels="+channels+" slices="+slices+" frames=1 display=Grayscale");
			

		}

	}
	
	static public void do_stitch_no_delete(String save_directory, int channels, int slices)
	{
		IJ.run("Grid/Collection stitching", "type=[Positions from file] order=[Defined by TileConfiguration] directory=["+save_directory+"] layout_file=out.txt fusion_method=[Max. Intensity] regression_threshold=0.30 max/avg_displacement_threshold=2.50 absolute_displacement_threshold=3.50 compute_overlap computation_parameters=[Save computation time (but use more RAM)] image_output=[Fuse and display]");
	}
	
	
	static public void write_tile_config(String save_directory, float [] x_pos, float [] y_pos)
	{
		try {
			FileOutputStream fos=new FileOutputStream(save_directory+"out.txt");
			Writer w= new BufferedWriter(new OutputStreamWriter(fos));
			w.write("dim = 2\n");
			
			for (int i=0; i<x_pos.length; i++)
			{
				w.write("Img"+String.format("%04d", i)+".tif; ; ("+x_pos[i]+", "+y_pos[i]+")\n");
			}
			w.flush();
			w.close();
			//if(j==0) return;
		}
		catch (Exception e) {}
	}

}
