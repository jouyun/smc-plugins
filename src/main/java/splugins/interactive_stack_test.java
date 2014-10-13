package splugins;

import ij.IJ;
import ij.ImageListener;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.plugin.PlugIn;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class interactive_stack_test implements PlugIn, KeyListener, ImageListener {

	ImageWindow win;
    ImageCanvas canvas;
    ImagePlus myimg;
    int counts;
    int Nslices;
    int height;
    int width;
    boolean [] hit_list;
    boolean s_down;
    boolean just_went;
    float z_correction;
    float x_correction;
    float y_correction;
    float pixel_size;
    float go_x;
    float go_y;
    float go_z;
    float backup_x;
    float backup_y;
    float backup_z;
	@Override
	public void keyPressed(KeyEvent e) {
		int keyCode = e.getKeyCode();
        char keyChar = e.getKeyChar();
        int flags = e.getModifiers();
        //IJ.log("keyPressed: keyCode=" + keyCode + " (" + KeyEvent.getKeyText(keyCode) + ")");
        //IJ.log("char: "+keyChar);
        if (keyChar=='q') 
        {
        	cleanup();
        }
        if (keyChar=='g')
        {
        	int myslice=myimg.getCurrentSlice();
        	String mylabel=myimg.getStack().getSliceLabel(myslice);
        	int a=mylabel.indexOf(",",0);
        	
        	int b=mylabel.indexOf(",",a+1);
        	int c=mylabel.indexOf(',',b+1);
        	int d=mylabel.indexOf(',',c+1);
        	int E=mylabel.indexOf(',',d+1);
        	
        	float xm=Float.parseFloat(mylabel.substring(a+1, b));
        	float ym=Float.parseFloat(mylabel.substring(b+1, c));
        	float xstage=Float.parseFloat(mylabel.substring(c+1, d));
        	float ystage=Float.parseFloat(mylabel.substring(d+1, E));
        	//float zstage=Float.parseFloat(mylabel.substring(E+1, mylabel.length()));
        	IJ.log("X: "+xm+" "+ym+" "+xstage+" "+ystage+" ");
        	
        	go_x=xstage-(xm-1280/2)*pixel_size-x_correction;
        	go_y=ystage-(ym-1280/2)*pixel_size-y_correction;
        	//float go_z=zstage-z_correction;
        }
        if (keyChar=='c')
        {
        	float current_x=0;
        	float current_y=0;
        	float current_z=0;
        	
        	x_correction=x_correction-(current_x-go_x);
        	y_correction=y_correction-(current_y-go_y);
        	z_correction=z_correction-(current_z-go_z);
        	IJ.log("Xcorrection: "+x_correction+" Ycorrection: "+y_correction+"Zcorrection"+z_correction);
        }
        if (keyChar=='r')
        {
        	x_correction=backup_x;
        	y_correction=backup_y;
        	z_correction=backup_z;
        }

	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void run(String arg0) {
		myimg = IJ.getImage();
        win = myimg.getWindow();
        canvas = win.getCanvas();
        win.removeKeyListener(IJ.getInstance());
        canvas.removeKeyListener(IJ.getInstance());
        win.addKeyListener(this);
        canvas.addKeyListener(this);
        ImagePlus.addImageListener(this);
        counts=0;
        hit_list=new boolean[myimg.getNFrames()];
        
        width=myimg.getWidth();
        height=myimg.getHeight();
        Nslices=myimg.getNSlices();
        
        GenericDialog gd=new GenericDialog("Set correction terms");
		gd.addNumericField("X correction:  ", 0, 1);
		gd.addNumericField("Y correction:  ", 0, 1);
		gd.addNumericField("Z correction:  ", 0, 1);
		gd.showDialog();
		
		x_correction=(float)gd.getNextNumber();
		y_correction=(float)gd.getNextNumber();
		z_correction=(float)gd.getNextNumber();
		pixel_size=0.5463f;
		
		backup_x=x_correction;
		backup_y=y_correction;
		backup_z=z_correction;

	}

	@Override
	public void imageClosed(ImagePlus arg0) {
		cleanup();
		
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
        
        
        ImagePlus.removeImageListener(this);
	}
	@Override
	public void imageOpened(ImagePlus arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void imageUpdated(ImagePlus arg0) {
		// TODO Auto-generated method stub
		
	}

}
