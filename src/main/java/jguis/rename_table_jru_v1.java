package jguis;

import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.Frame;
import ij.plugin.*;
import ij.util.*;
import ij.text.*;
 
public class rename_table_jru_v1 implements PlugIn {
 
                public void run(String arg) {
                                //first get the table window
                                Frame[] niframes=WindowManager.getNonImageWindows();
                                String[] titles=new String[niframes.length];
                                for(int i=0;i<niframes.length;i++){
                                                titles[i]=niframes[i].getTitle();
                                }
                                GenericDialog gd=new GenericDialog("Windows");
                                gd.addChoice("Windows",titles,titles[0]);
                                gd.addStringField("New Name","");
                                gd.showDialog();
                                if(gd.wasCanceled()){return;}
                                int index=gd.getNextChoiceIndex();
                                String newname=gd.getNextString();
                                if(niframes[index] instanceof TextWindow){
                                                niframes[index].setTitle(newname);
                                } else {
                                                IJ.showMessage("wrong window type");
                                }
                }
}