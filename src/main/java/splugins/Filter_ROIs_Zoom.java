package splugins;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.gui.NewImage;
import ij.gui.Roi;
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;

import java.awt.Point;
import java.awt.Polygon;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

public class Filter_ROIs_Zoom implements KeyListener, PlugIn {
	
	ImageWindow win;
    ImageCanvas canvas;
    ImagePlus myimg;
    int counts;
    int Nframes;
    int Nchannels;
    int Nslices;
    int height;
    int width;
    boolean [] hit_list;
    boolean s_down;
    boolean just_went;

	@Override
	public void run(String arg0) {
		myimg = IJ.getImage();
        win = myimg.getWindow();
        canvas = win.getCanvas();
        win.removeKeyListener(IJ.getInstance());
        canvas.removeKeyListener(IJ.getInstance());
        win.addKeyListener(this);
        canvas.addKeyListener(this);
        //ImagePlus.addImageListener(this);
        counts=0;
        hit_list=new boolean[myimg.getNFrames()];
        
        width=myimg.getWidth();
        height=myimg.getHeight();
        Nslices=myimg.getNSlices();
        Nframes=myimg.getNFrames();
        Nchannels=myimg.getNChannels();
        
        RoiManager manager=RoiManager.getInstance();
        hit_list=new boolean[manager.getCount()];
        for (int i=0; i<hit_list.length; i++) hit_list[i]=false;

	}

	@Override
	public void keyPressed(KeyEvent e) {
		int keyCode = e.getKeyCode();
        char keyChar = e.getKeyChar();
        int flags = e.getModifiers();
        //IJ.log("keyPressed: keyCode=" + keyCode + " (" + KeyEvent.getKeyText(keyCode) + ")");
        //IJ.log("char: "+keyChar);
        if (keyChar=='f')
        {
        	RoiManager manager=RoiManager.getInstance();
        	int selected=manager.getSelectedIndex();
        	int nextidx=selected+1;
        	if (nextidx>=hit_list.length) nextidx=hit_list.length-1;
        	if (nextidx<0) nextidx=0;
        	manager.selectAndMakeVisible(myimg, nextidx);
        	hit_list[selected]=true;
        	IJ.run("To Selection");
        	for (int i=0; i<6; i++) IJ.run("Out [-]");
        	
        	/*Remove this if too slow*/
        	ArrayList <Point> point_list=new ArrayList <Point>();
        	Polygon p=manager.getRoi(nextidx).getPolygon();
			for (int x=0; x<width; x++)
			{
				for (int y=0; y<height; y++)
				{
					Point P=new Point(x,y);
					if (p.contains(P)) 
					{
						point_list.add(P);
					}
				}
			}
			
			
			int cur_c=myimg.getChannel()-1, cur_f=myimg.getFrame()-1, cur_s=myimg.getSlice()-1;
			int current_best=-1;
			float current_best_avg=0;
			for (int i=0; i<Nslices; i++)
			{
				float current=0;
				short [] pix=(short [])myimg.getStack().getProcessor(cur_c+i*Nchannels+Nslices*Nchannels*cur_f+1).getPixels();
				for (int j=0; j<point_list.size(); j++)
				{
					current=current+pix[point_list.get(j).x+point_list.get(j).y*width];
				}
				if (current>current_best_avg)
				{
					current_best_avg=current;
					current_best=i;
				}
			}
			myimg.setZ(current_best+1);
			myimg.updateAndDraw();
			
        }
        if (keyChar=='d')
        {
        	RoiManager manager=RoiManager.getInstance();
        	int selected=manager.getSelectedIndex();
        	int nextidx=selected+1;
        	if (nextidx>=hit_list.length) nextidx=hit_list.length-1;
        	if (nextidx<0) nextidx=0;
        	manager.selectAndMakeVisible(myimg, nextidx);
        	hit_list[selected]=false;
        	IJ.run("To Selection");
        	for (int i=0; i<6; i++) IJ.run("Out [-]");


        	/*Remove this if too slow*/
        	ArrayList <Point> point_list=new ArrayList <Point>();
        	Polygon p=manager.getRoi(nextidx).getPolygon();
			for (int x=0; x<width; x++)
			{
				for (int y=0; y<height; y++)
				{
					Point P=new Point(x,y);
					if (p.contains(P)) 
					{
						point_list.add(P);
					}
				}
			}
			
			
			int cur_c=myimg.getChannel()-1, cur_f=myimg.getFrame()-1, cur_s=myimg.getSlice()-1;
			int current_best=-1;
			float current_best_avg=0;
			for (int i=0; i<Nslices; i++)
			{
				float current=0;
				short [] pix=(short [])myimg.getStack().getProcessor(cur_c+i*Nchannels+Nslices*Nchannels*cur_f+1).getPixels();
				for (int j=0; j<point_list.size(); j++)
				{
					current=current+pix[point_list.get(j).x+point_list.get(j).y*width];
				}
				if (current>current_best_avg)
				{
					current_best_avg=current;
					current_best=i;
				}
			}
			myimg.setZ(current_best+1);
			myimg.updateAndDraw();
        	
        }
        if (keyChar=='o')
        {
        	RoiManager manager=RoiManager.getInstance();
        	int selected=manager.getSelectedIndex();
        	int nextidx=selected-1;
        	if (nextidx>=hit_list.length) nextidx=hit_list.length-1;
        	if (nextidx<0) nextidx=0;
        	manager.selectAndMakeVisible(myimg, nextidx);
        	IJ.run("To Selection");
        	for (int i=0; i<6; i++) IJ.run("Out [-]");


        	/*Remove this if too slow*/
        	ArrayList <Point> point_list=new ArrayList <Point>();
        	Polygon p=manager.getRoi(nextidx).getPolygon();
			for (int x=0; x<width; x++)
			{
				for (int y=0; y<height; y++)
				{
					Point P=new Point(x,y);
					if (p.contains(P)) 
					{
						point_list.add(P);
					}
				}
			}
			int cur_c=myimg.getChannel()-1, cur_f=myimg.getFrame()-1, cur_s=myimg.getSlice()-1;
			int current_best=-1;
			float current_best_avg=0;
			for (int i=0; i<Nslices; i++)
			{
				float current=0;
				short [] pix=(short [])myimg.getStack().getProcessor(cur_c+i*Nchannels+Nslices*Nchannels*cur_f+1).getPixels();
				for (int j=0; j<point_list.size(); j++)
				{
					current=current+pix[point_list.get(j).x+point_list.get(j).y*width];
				}
				if (current>current_best_avg)
				{
					current_best_avg=current;
					current_best=i;
				}
			}
			myimg.setZ(current_best+1);
			myimg.updateAndDraw();

        }
        if (keyChar=='l')
        {
        	RoiManager manager=RoiManager.getInstance();
        	int selected=manager.getSelectedIndex();
        	int nextidx=selected+1;
        	if (nextidx>=hit_list.length) nextidx=hit_list.length-1;
        	if (nextidx<0) nextidx=0;
        	manager.selectAndMakeVisible(myimg, nextidx);
        	IJ.run("To Selection");
        	for (int i=0; i<6; i++) IJ.run("Out [-]");

        	
        	/*Remove this if too slow*/
        	ArrayList <Point> point_list=new ArrayList <Point>();
        	Polygon p=manager.getRoi(nextidx).getPolygon();
			for (int x=0; x<width; x++)
			{
				for (int y=0; y<height; y++)
				{
					Point P=new Point(x,y);
					if (p.contains(P)) 
					{
						point_list.add(P);
					}
				}
			}
			
			
			int cur_c=myimg.getChannel()-1, cur_f=myimg.getFrame()-1, cur_s=myimg.getSlice()-1;
			int current_best=-1;
			float current_best_avg=0;
			for (int i=0; i<Nslices; i++)
			{
				float current=0;
				short [] pix=(short [])myimg.getStack().getProcessor(cur_c+i*Nchannels+Nslices*Nchannels*cur_f+1).getPixels();
				for (int j=0; j<point_list.size(); j++)
				{
					current=current+pix[point_list.get(j).x+point_list.get(j).y*width];
				}
				if (current>current_best_avg)
				{
					current_best_avg=current;
					current_best=i;
				}
			}
			myimg.setZ(current_best+1);
			myimg.updateAndDraw();

        }
        if (keyChar=='q')
        {
        	cleanup();
        	RoiManager manager=RoiManager.getInstance();
        	for (int i=hit_list.length-1; i>-1; i--)
        	{
        		if (hit_list[i]==false)
        		{
        			manager.select(i);
        			manager.runCommand("Delete");
        		}
        	}
        	/*
        	ImagePlus new_img=NewImage.createByteImage("Filtered", width, height, Nslices, NewImage.FILL_BLACK);
        	for (int i=0; i<Nslices; i++)
        	{
        		
        	}
        		
    		new_img.setOpenAsHyperStack(true);
    		new_img.setDimensions(img.getNChannels(), img.getNSlices(), img.getNFrames());
    		new_img.show();
    		new_img.updateAndDraw();*/
    		
        }

	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}
	
	public void cleanup()
	{
		if (win!=null)
		{
			win.removeKeyListener(this);
		}
		
        if (canvas!=null)
        {
        	canvas.removeKeyListener(this);
        }
        //ImagePlus.removeImageListener(this);
	}
	

}
