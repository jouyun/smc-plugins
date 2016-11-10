package splugins;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.ListIterator;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.measure.Calibration;
import ij.plugin.PlugIn;

public class Stitch_PE_Data implements PlugIn {

	static Double number_worms;
	String [] slice_labels;
	
	class worm_wrapper
	{
		String name;
		ArrayList <Double> x_loc;
		ArrayList <Double> y_loc;
	};
	class blocks_wrapper
	{
		ArrayList<block> myblocks;
		blocks_wrapper()
		{
			myblocks=new ArrayList<block>();
		}
		block find_block(int idx)
		{
			for (ListIterator<block> myBlock=myblocks.listIterator(); myBlock.hasNext();)
			{
				block cur=myBlock.next();
				IJ.log("Existing:  "+cur.block_id+ ","+idx);
				if (cur.block_id==idx) return cur;
			}
			return null;
		}
	};
	class block
	{
		int block_id;
		ArrayList <position> dat;
		block()
		{
			dat=new ArrayList<position> ();
		}
	}
	class position
	{
		int frame;
		double x_loc;
		double y_loc;
		position(int a, double b, double c)
		{
			frame=a;
			x_loc=b;
			y_loc=c;
		}
	}
	@Override
	public void run(String arg0) {
		
		ImagePlus img=WindowManager.getCurrentImage();
		number_worms=(double) 0;
		
		GenericDialog gd = new GenericDialog("Merge Tiled Objects");
		gd.addStringField("Path to save to:  ", "C:\\Users\\smc\\Desktop\\tmp\\");
		gd.showDialog();
		
		Calibration my_cal=img.getCalibration();
		
		String save_directory=gd.getNextString();
		String save_tmp_directory=save_directory+"tmp/";
		
		File my_dir=new File(save_tmp_directory);
		my_dir.mkdir();
		
		slice_labels=new String[img.getNFrames()];
		for (int i=0; i<img.getNFrames(); i++)
		{
			slice_labels[i]=img.getStack().getSliceLabel(1+i*img.getNSlices()*img.getNChannels());
			IJ.log(slice_labels[i]);
		}
		
		
		String imgInfo=img.getInfoProperty();
		
		blocks_wrapper block_list=find_location_entries_new(imgInfo);
		
		
		//replace this
		IJ.runMacroFile("SaveMultipageImageSequence.ijm", save_tmp_directory);
		
		int width=img.getWidth(); 
		int height=img.getHeight();
		int cumulative_index=0;
		for (int j=0; j<block_list.myblocks.size(); j++)
		{
			try {
				FileOutputStream fos=new FileOutputStream(save_tmp_directory+"out"+(j+1)+".txt");
				IJ.log(save_tmp_directory+"out"+(j+1)+".txt");
				Writer w= new BufferedWriter(new OutputStreamWriter(fos));
				w.write("# Define the number of dimensions we are working on\n");
				if (img.getNSlices()>1)
				{
					w.write("dim = 3\n\n# Define the image coordinates\n");
				}
				else
				{
					w.write("dim = 2\n\n# Define the image coordinates\n");
				}
				
				block my_block=block_list.myblocks.get(j);
				double x_base=my_block.dat.get(0).x_loc;
				double y_base=my_block.dat.get(0).y_loc;
				
				for (int i=0; i<my_block.dat.size(); i++)
				{
					int cur_frame=my_block.dat.get(i).frame;
					double cur_x=my_block.dat.get(i).x_loc;
					double cur_y=my_block.dat.get(i).y_loc;
					IJ.log("Tiffs"+String.format("%04d", cur_frame-1)+".tif; ; ("+(cur_x-x_base)/my_cal.pixelWidth+", "+(cur_y-y_base)/my_cal.pixelWidth+")\n");
					if (img.getNSlices()>1)
					{
						w.write("Tiffs"+String.format("%04d", cur_frame-1)+".tif; ; ("+(cur_x-x_base)/my_cal.pixelWidth+", "+(cur_y-y_base)/my_cal.pixelWidth+",0.0)\n");
					}
					else
					{
						w.write("Tiffs"+String.format("%04d", cur_frame-1)+".tif; ; ("+(cur_x-x_base)/my_cal.pixelWidth+", "+(cur_y-y_base)/my_cal.pixelWidth+")\n");
					}
					
					cumulative_index++;
				}
				w.flush();
				w.close();
			}
			catch (Exception e) {}
			IJ.log("About to start processing this directory: "+save_tmp_directory+" and this file: out"+(j+1)+".txt");
			IJ.run("Grid/Collection stitching", "type=[Positions from file] order=[Defined by TileConfiguration] directory=["+save_tmp_directory+"] layout_file=out"+(j+1)+".txt fusion_method=[Max. Intensity] regression_threshold=0.30 max/avg_displacement_threshold=2.50 absolute_displacement_threshold=3.50 compute_overlap computation_parameters=[Save computation time (but use more RAM)] image_output=[Fuse and display]");
			WindowManager.getCurrentImage().setTitle("Worm"+(j+1));
		}
		number_worms=(double) block_list.myblocks.size();
		
	}
	
	public static String getResult()
	{
		return number_worms.toString();
	}
	
	public ArrayList<worm_wrapper> find_location_entries(String info)
	{
		int idx=0;
		ArrayList<worm_wrapper> rtn=new ArrayList<worm_wrapper>();
		
		//Get the names of the individual tile acquisitions
		
		/*OLD
		while (info.indexOf("Name =",idx)>-1)
		{
			int tidx=info.indexOf("Name =",idx)+7;
			int eidx=info.indexOf("(raw", tidx);
			IJ.log("idx:  "+idx+","+tidx+","+eidx);
			String tstring=info.substring(tidx,eidx);
			idx=eidx;
			
			boolean breaker=false;
			for (int i=0; i<rtn.size(); i++)
			{
				String mstring=rtn.get(i).name;
				if (tstring.equals(mstring)) 
				{
					breaker=true;
					break;
				}
			}
			if (breaker) continue;
			
			worm_wrapper tmp=new worm_wrapper();
			tmp.name=tstring;
			rtn.add(tmp);
		}*/
		int tt=0;
		while (idx>-1)
		{
			String screen_string="Series "+idx+" Name =";
			int tidx=info.indexOf(screen_string)+screen_string.length();
			int newline_pos=info.indexOf("\n", tidx);
			String tmps=info.substring(tidx, newline_pos-1);
			
			IJ.log(tmps);
			if (!tmps.contains("raw tile"))
			{
				IJ.log("Skipping");
				idx++;
				continue;
			}
			IJ.log("NEW ONE");
			IJ.log(screen_string+","+tidx);
			if (info.indexOf(screen_string)<0)
			{
				idx=-1;
				break;
			}
			int eidx=info.indexOf("(raw", tidx);
			IJ.log("idx:  "+idx+","+tidx+","+eidx);
			String tstring=info.substring(tidx+1,eidx);
			
			IJ.log(tstring);
			worm_wrapper tmp=new worm_wrapper();
			tmp.name=tstring;
			rtn.add(tmp);
			idx++;
			
			boolean stop_me=false;
			while (!stop_me)
			{
				screen_string="Series "+idx+" Name =";
				tidx=info.indexOf(screen_string)+screen_string.length();
				if (info.indexOf(screen_string)<0) 
				{
					idx=-1;
					break;
				}
				eidx=info.indexOf("(raw", tidx);
				IJ.log("idx:  "+idx+","+tidx+","+eidx);
				String bstring=info.substring(tidx+1,eidx);
				if (!bstring.equals(tstring)) stop_me=true;
				idx++;
			}
			
		}
		
		for (ListIterator<worm_wrapper> myI=rtn.listIterator(); myI.hasNext();)
		{
			worm_wrapper cur=myI.next();
			cur.x_loc=new ArrayList<Double>();
			cur.y_loc=new ArrayList<Double>();
			idx=0;
			int num_tiles=1;
			String screenX_string=cur.name+"(raw tile "+num_tiles+") X Location = ";
			String screenY_string=cur.name+"(raw tile "+num_tiles+") Y Location = ";
			
			//IJ.log(screenX_string);
			//IJ.log("idx, num_tiles"+idx+","+num_tiles);
			
			while (info.indexOf(screenX_string)>-1)
			{				
				int tidx=info.indexOf(screenX_string)+screenX_string.length();
				int eidx=info.indexOf(cur.name, tidx)-1;
				String X=info.substring(tidx, eidx);
				
				tidx=info.indexOf(screenY_string)+screenY_string.length();
				eidx=info.indexOf(cur.name, tidx)-1;
				String Y=info.substring(tidx, eidx);
				
				//IJ.log("Name,tile,X,Y: "+cur.name+","+num_tiles+","+X+","+Y);
				cur.x_loc.add(Double.parseDouble(X));
				cur.y_loc.add(Double.parseDouble(Y));
				
				num_tiles++;
				screenX_string=cur.name+"(raw tile "+num_tiles+") X Location = ";
				screenY_string=cur.name+"(raw tile "+num_tiles+") Y Location = ";
				//IJ.log("idx, num_tiles"+idx+","+num_tiles);
			}
		}
		
		for (ListIterator<worm_wrapper> myIter=rtn.listIterator(); myIter.hasNext();)
		{
			worm_wrapper tmp=myIter.next();
			for (int i=0; i<tmp.x_loc.size(); i++)
			{
				IJ.log("Worm,tile,X,Y: "+tmp.name+","+(i+1)+","+tmp.x_loc.get(i)+","+tmp.y_loc.get(i));
			}
		}
		
		return rtn;
	}
	int get_series(String val)
	{
		IJ.log(val);
		int fidx=8;
		int eidx=val.indexOf("Name")-1;
		IJ.log(val.substring(fidx,eidx));
		return Integer.parseInt(val.substring(fidx, eidx));
		
	}
	int get_point(String val)
	{
		int fidx=val.indexOf("point")+6;
		int eidx=val.indexOf("(")-1;
		IJ.log(val.substring(fidx,eidx));
		return Integer.parseInt(val.substring(fidx, eidx));
		
	}
	int get_tile(String val)
	{
		int fidx=val.indexOf("tile")+5;
		int eidx=val.indexOf(")")-0;
		IJ.log(val.substring(fidx,eidx));
		return Integer.parseInt(val.substring(fidx, eidx));
	}
	double get_position(String info, String search_string)
	{
		int fidx=info.indexOf(search_string)+search_string.length();
		int eidx=info.indexOf("X",fidx);
		IJ.log(info.substring(fidx,eidx));
		return Double.parseDouble(info.substring(fidx, eidx-1));
	}
	
	
	public blocks_wrapper find_location_entries_new(String info)
	{
		
		
		String lines[]=info.split("\\r?\\n");
		
		// Series 12 Name = XY point 2 (raw tile 4)
		//		 Series 13 Name = XY point 2 (raw tile 5)
		//XY point 2 (raw tile 5) X Location = 52906.23828125
		blocks_wrapper block_list=new blocks_wrapper();
		for (int i=0; i<lines.length; i++)
		{
			if (lines[i].contains("Series"))
			{
				int slice_index=i;
				int series_number=get_series(lines[i]);
				int XY_point=get_point(lines[i]);
				int tile=get_tile(lines[i]);
				String search_loc="XY point "+XY_point+" (raw tile "+tile+") X Location = ";
				double x_pos=get_position(info, search_loc);
				search_loc="XY point "+XY_point+" (raw tile "+tile+") Y Location = ";
				double y_pos=get_position(info, search_loc);
				block cur=block_list.find_block(XY_point);
				if (cur==null)
				{
					block tmp=new block();
					tmp.block_id=XY_point;
					position my_pos=new position(slice_index, x_pos, y_pos);
					tmp.dat.add(my_pos);
					block_list.myblocks.add(tmp);
				}
				else
				{
					position my_pos=new position(slice_index, x_pos, y_pos);
					cur.dat.add(my_pos);
				}
			}
		}
		

		
		return block_list;
	}
	
}
