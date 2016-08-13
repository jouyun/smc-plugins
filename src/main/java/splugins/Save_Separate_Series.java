package splugins;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.ArrayList;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.gui.Roi;
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;
import ij.process.ImageProcessor;

public class Save_Separate_Series implements PlugIn, KeyListener {

	ImagePlus imp;
	int width;
	int height;
	int slices;
	int frames;
	int channels;
	int cur_slice;
	int cur_frame;
	int cur_channel;
	int [] labels;
	ImageWindow win;
    ImageCanvas canvas;
    ImagePlus myimg;
    String directory;
    ArrayList <String> class_list;
    
	@Override
	public void run(String arg0) {
		imp=WindowManager.getCurrentImage();
		width=imp.getWidth(); 
		height=imp.getHeight();
		slices=imp.getNSlices();
		frames=imp.getNFrames();
		channels=imp.getNChannels();
		cur_slice=imp.getSlice()-1;
		cur_frame=imp.getFrame()-1;
		cur_channel=imp.getChannel()-1;
		
		//Ask for names
		GenericDialog gd=new GenericDialog("Pick names");
		gd.addStringField("Directory: ", "/home/smc/fast/tmp");
		gd.addStringField("First class: ", "");
		gd.addStringField("Second class: ", "");
		gd.addStringField("Third class: ", "");
		gd.addStringField("Fourth class: ", "");
		gd.showDialog();
		
		//Get names
		directory=gd.getNextString();
		String tmp=gd.getNextString();
		class_list=new ArrayList<String>();
		while (tmp.length()>0)
		{
			class_list.add(tmp);
			tmp=gd.getNextString();
		}
		
		//Make directories
		for (int i=0; i<class_list.size(); i++)
		{
			File dir=new File(directory+File.separator+class_list.get(i));
			dir.mkdir();
		}
		
		//Setup listeners
		myimg = IJ.getImage();
        win = myimg.getWindow();
        canvas = win.getCanvas();
        win.removeKeyListener(IJ.getInstance());
        canvas.removeKeyListener(IJ.getInstance());
        win.addKeyListener(this);
        canvas.addKeyListener(this);
        
		//Setup logger, labels will be one more than their actual label to allow for 0s as unsaved
        labels=new int [imp.getStack().getSize()];
        
		

	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		int keyCode = e.getKeyCode();
        char keyChar = e.getKeyChar();
        int flags = e.getModifiers();
        //IJ.log("keyPressed: keyCode=" + keyCode + " (" + KeyEvent.getKeyText(keyCode) + ")");
        //IJ.log("char: "+keyChar);
        if (keyChar=='a')
        {
        	labels[imp.getCurrentSlice()-1]=1;
        	imp.setSlice(imp.getCurrentSlice()+1);
        	imp.updateAndDraw();
        }
        if (keyChar=='f')
        {
        	labels[imp.getCurrentSlice()-1]=2;
        	imp.setSlice(imp.getCurrentSlice()+1);
        	imp.updateAndDraw();
        }
        if (keyChar=='g')
        {
        	if (class_list.size()>2) labels[imp.getCurrentSlice()-1]=3;
        	imp.setSlice(imp.getCurrentSlice()+1);
        	imp.updateAndDraw();
        }
        if (keyChar=='h')
        {
        	if (class_list.size()>3) labels[imp.getCurrentSlice()-1]=4;
        	imp.setSlice(imp.getCurrentSlice()+1);
        	imp.updateAndDraw();
        }
        if (keyChar=='z')
        {
        	imp.setSlice(imp.getCurrentSlice()-1);
        	imp.updateAndDraw();
        }
        if (keyChar=='v')
        {
        	imp.setSlice(imp.getCurrentSlice()+1);
        	imp.updateAndDraw();
        }
        if (keyChar=='x')
        {
        	labels[imp.getCurrentSlice()-1]=0;
        	imp.setSlice(imp.getCurrentSlice()+1);
        	imp.updateAndDraw();
        }
        if (keyChar=='q')
        {
        	for (int i=0; i<imp.getStack().getSize(); i++)
        	{
        		if (labels[i]==0) continue;
        		String folder=directory+File.separator+class_list.get(labels[i]-1)+File.separator;
        		ImagePlus tmp=new ImagePlus("Title", (ImageProcessor)(imp.getStack().getProcessor(i+1).clone()));
        		IJ.saveAs(tmp, "Jpeg", folder+(i+1)+".jpg");
        	}
        	cleanup();
        }
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
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
