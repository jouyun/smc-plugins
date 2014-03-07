package rplugins;
import ij.*;
import ij.plugin.*;
import ij.plugin.filter.PlugInFilter;
import ij.process.*;
import ij.gui.*;
import java.awt.Frame;
import java.awt.Component;
import java.awt.Container;
import java.awt.Canvas;
import java.awt.Panel;
import java.awt.event.*;
import java.util.*;
import java.lang.reflect.*;
import java.util.Arrays;
import ij.text.*;
import java.util.*;

public class RLAinterfaceTEST extends RLAMouseListenerObject{
	MouseListener mouselisten;
	List<List<String>> llTableArray;
	TextPanel tpTable;
	ImagePlus imp;
	String imageName;
	ImageStack origStack;
	int width;
	int height;
	int stackSize;
	int numChan;
	int numZ;

	int x;
	int y;
	int z;

	int[] X;
	int[] Y;
	int[] Z;
	int[] R;
	boolean change = true;

	public void run(ImageProcessor ip) {
		imp=WindowManager.getCurrentImage();
		int bitD = imp.getBitDepth();
		IJ.log("bitD = "+bitD);
		if(bitD<32){
			WaitForUserDialog wfud2 =new WaitForUserDialog("ERROR", "Image must be 32bit");
			wfud2.show();
		}else{
			IJ.run("32-bit");
			//ImageWindow win2 = img.getWindow();
			//doSetupFakeObjects();
			doGetTableData();
			getImageInfo();
	
			IJ.run("Make Composite", "display=Composite");
			doListen();	//method inherited from RLAMouseListenerObject.java
			doUnhighlight();
		}
		ClosePlugin();
	}

	public void doReportMouse(int x, int y){
		this.x = x;
		this.y = y;
		z = imp.getSlice();
		IJ.log("NEW  " + x + "   NEW  "+y + "current z = "+z);
		doFindNearestObject();
//drawTest();
	}
	
	public void doCloseTasks(){
		tpTable.removeMouseListener(this);
		
	}
	
	void doFindNearestObject(){
		int length = llTableArray.size();
		
		double r = -1;
		double distance=-1;
		int index=-1;
		for(int i = 0; i<length; i++){
			List<String> row = llTableArray.get(i);
			
			if(Integer.parseInt(row.get(2))==z){
				//IJ.log("Found at index:  " +i);
				int xdiff= x - Integer.parseInt(row.get(0));
				int ydiff= y - Integer.parseInt(row.get(1));
				r=Math.sqrt(ydiff*ydiff+xdiff*xdiff);	//---distance between the two points
				if(r<distance || distance==-1){	//---minimize distance to find nearest object to click
					distance = r;
					IJ.log(""+distance);
					index = i;
				}
			}
		}
		if(distance >=0){
			List<String> row = llTableArray.get(index);
			int rr = Integer.parseInt(row.get(3));
			IJ.log("rr = "+rr+"    dist = "+distance);
			if(rr>=distance){
				int xx = Integer.parseInt(row.get(0));
				int yy = Integer.parseInt(row.get(1));
				//int zz = Integer.parseInt(row.get(2));
				doHighlight(xx,yy,rr);
				doSelectRow(index);
			}else{
				doUnhighlight();
				doUnselectRow();
			}
		}else{
			doUnhighlight();
			doUnselectRow();
		}
	}
	
	void setTableListeners(TextPanel tp){
		mouselisten = new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				//get selection index
				Component comp = e.getComponent();
				String name = comp.getName();
				Container cont = comp.getParent();
				String parent = cont.getName();
				IJ.log(name + "...   parent = "+parent);
				if(comp instanceof Panel){
					IJ.log("FOUND TEXTPANEL!!!!  YAY!!!!!!!!!!!!!!!!!!!!!!");
				}
				int index = tpTable.getSelectionStart();
				if(index<0){
					doUnhighlight();
				}else{
					IJ.log("clicked   "+index);
					List<String> row = llTableArray.get(index);
					int xx = Integer.parseInt(row.get(0));
					int yy = Integer.parseInt(row.get(1));
					int zz = Integer.parseInt(row.get(2));
					int rr = Integer.parseInt(row.get(3));
					int currentChan = imp.getChannel();
					int currentFrame = imp.getFrame();
					imp.setPosition(currentChan,zz,currentFrame);
					doHighlight(xx,yy,rr);
				}
			}
		};
		tpTable.addMouseListener(mouselisten);

	}
	
	void doGetTableData(){
		//first get the table window
		Frame[] niframes=WindowManager.getNonImageWindows();
		String[] titles=new String[niframes.length];
		for(int i=0;i<niframes.length;i++){
			titles[i]=niframes[i].getTitle();
		}
		GenericDialog gd=new GenericDialog("Select Data Table");
		//gd.addTextAreas("",null,10,20);
		gd.addChoice("Table Window",titles,titles[0]);
		gd.showDialog(); if(gd.wasCanceled()){return;}
		//String input=gd.getNextText();
		int index=gd.getNextChoiceIndex();
		if(niframes[index] instanceof TextWindow){
			TextWindow tw=(TextWindow)niframes[index];
			tpTable=tw.getTextPanel();
			llTableArray = table2listtable(tpTable);
			setTableListeners(tpTable);
		}else {
			IJ.showMessage("wrong window type");
		}
	}
	
	public static List<List<String>> table2listtable(TextPanel tp){	//from Jay's table_tools
        int nlines=tp.getLineCount();
        List<List<String>> retvals=new ArrayList<List<String>>();
        int longest=0;
        for(int i=0;i<nlines;i++){
            String line=tp.getLine(i);
            String[] temp2=split_string_tab(line);
            List<String> temp=stringarray2list(temp2);
            if(i==0){longest=temp.size();}
            for(int j=temp.size();j<longest;j++){
                temp.add("");
            }
            retvals.add(temp);
        }
        return retvals;
    }
	
	public static String[] split(String line,String delim){	//from Jay's table_tools
        return line.split(delim);
    }
	
	public static String[] split_string_tab(String line){	//from Jay's table_tools
        String temp;
        if(line.endsWith("\t")){
            temp=line.substring(0,line.length()-1);
        } else {
            temp=line.substring(0);
        }
        return split(temp,"\t");
    }
	
	public static List<String> stringarray2list(String[] arr){	//from Jay's table_tools
        List<String> temp=new ArrayList<String>();
        for(int i=0;i<arr.length;i++){
            temp.add(arr[i]);
        }
        return temp;
    }

	void doSetupFakeObjects(){
		int[] X1 = {111,294,64,263};
		int[] Y1 = {104,59,248,248};
		int[] Z1 = {1,1,1,1};
		int[] R1 = {5,5,12,12};
		X = X1;
		Y=Y1;
		Z = Z1;
		R=R1;

	}

	void getImageInfo(){
		imp=WindowManager.getCurrentImage();
		imageName = imp.getTitle();
		width=imp.getWidth(); height=imp.getHeight();
		origStack=imp.getStack();
		stackSize = origStack.getSize();
		numChan = imp.getNChannels();
		numZ = imp.getNSlices();
		imp.setActivated();
		IJ.run("32-bit");
		IJ.log(imageName+"    size: "+stackSize+"    chan: "+numChan+"    z: "+numZ);
	}
	
	void doSelectRow(int index){
		tpTable.setSelection(index,index);
	}
	
	void doUnselectRow(){
		tpTable.resetSelection();
	}

	void doHighlight(int x, int y, int r){
//IJ.log("highlight...");
/*
		int indexHighlight = imp.getStackIndex(2,1,1);   //-----channel, slice, frame
		float[] highlightPixels =(float[])origStack.getPixels(indexHighlight);
		Arrays.fill(highlightPixels, 0.0f);	//initialize pixel array to zero before highlighting
			int maxradius=r;
			int startx=x-maxradius; if(startx<0){startx=0;}
			int endx=x+maxradius; if(endx>=width){endx=width-1;}
			int starty=y-maxradius; if(starty<0){starty=0;}
			int endy=y+maxradius; if(endy>=height){endy=height-1;}
			for(int i=starty;i<=endy;i++){
				int ydiff=i-y;
				for(int j=startx;j<=endx;j++){
					int xdiff=j-x;
					double rad=Math.sqrt(ydiff*ydiff+xdiff*xdiff);
					if(rad<=(double)maxradius){
						highlightPixels [j+i*width]=500;
					}
				}
			}
		origStack.setPixels(highlightPixels, indexHighlight);
		imp.updateAndRepaintWindow();
*/
		OvalRoi circle = new OvalRoi((double)x-r,(double)y-r,(double)r*2, (double)r*2);
		imp.setOverlay(circle ,java.awt.Color.yellow,3,java.awt.Color.yellow);
	}

	void doUnhighlight(){
		OvalRoi circle = new OvalRoi(0,0,0,0);
		imp.setHideOverlay(true) ;
	}

	void drawTest(){
		IJ.log("drawTest...");
		int indexHighlight = imp.getStackIndex(2,1,1);   //-----channel, slice, frame
		float[] highlightPixels =(float[])origStack.getPixels(indexHighlight);
		if(change){
			for(int i=(int)width*1/4;i<(int)width*3/4;i++){
				for(int j = (int)height*1/4; j < (int)height*3/4;j++){
					highlightPixels[j+i*width] = 100;
				}
			}
		}else{
			for(int i=0; i<highlightPixels.length; i++){
				highlightPixels[i] = 0;
			}
		}
		origStack.setPixels(highlightPixels, indexHighlight);
		imp.updateAndRepaintWindow();
		change = !change ;
	}
	
	
	void doFindNearestObjectOLD(){
		//-----find nearest object and ask whether the point clicked is within the object's radius
		double distance=999999999;
		int index=0;
		
		for(int i = 0; i<X.length;i++){
			int xdiff= x - X[i];
			int ydiff= y - Y[i];
			double r=Math.sqrt(ydiff*ydiff+xdiff*xdiff);	//---distance between the two points
			//IJ.log("r = "+r);
			if(r<distance || i==0){	//---minimize distance to find nearest object to click
				distance = r;
				index = i;
			}
		}
		IJ.log("dist: "+distance);
		if(R[index]>=distance){
			doHighlight(X[index],Y[index],R[index]);
		}else{
			doUnhighlight();
		}
	}

}
