/*******************************************************************************
 * Copyright (c) 2013 Jay Unruh, Stowers Institute for Medical Research.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/

package jalgs;

import java.io.*;
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class jdataio{
	// here are my utility methods for io including gui interfaces

	public String readstringfile(BufferedReader b){
		try{
			String s;
			StringBuffer sb=new StringBuffer();
			s=b.readLine();
			while(s!=null){
				sb.append(s);
				sb.append("\n");
				s=b.readLine();
			}
			return sb.toString();
		}catch(IOException e){
			return null;
		}
	}

	public String[] readstringfilelines(BufferedReader b){
		try{
			List<String> s2=new ArrayList<String>();
			String s=b.readLine();
			while(s!=null){
				s2.add(s);
				s=b.readLine();
			}
			String[] s3=new String[s2.size()];
			for(int i=0;i<s2.size();i++){
				s3[i]=s2.get(i);
			}
			return s3;
		}catch(IOException e){
			return null;
		}
	}

	public boolean writestringfile(BufferedWriter b,String s){
		try{
			b.write(s);
			return true;
		}catch(IOException e){
			return false;
		}
	}

	public boolean writestringfile(BufferedWriter b,String[] lines){
		try{
			for(int i=0;i<lines.length;i++){
				b.write(lines[i]+"\n");
			}
			return true;
		}catch(IOException e){
			return false;
		}
	}

	public boolean dir_contains(File dir,String fname){
		String[] list=dir.list();
		for(int i=0;i<list.length;i++){
			if(fname.equals(list[i]))
				return true;
		}
		return false;
	}

	public String[] get_sorted_string_list(String directory,String mask){
		String[] names=(new File(directory)).list();
		ArrayList<String> namelist=new ArrayList<String>();
		int nmask=0;
		for(int i=0;i<names.length;i++){
			if(mask!=null){
				if(names[i].contains(mask)){
					nmask++;
					namelist.add(names[i]);
				}
			}else{
				nmask++;
				namelist.add(names[i]);
			}
		}
		Collections.sort(namelist);
		String[] temp=new String[nmask];
		for(int i=0;i<nmask;i++){
			temp[i]=namelist.get(i);
		}
		return temp;
	}

	public String[] get_datemod_sorted_string_list(String directory,String mask){
		File[] files=(new File(directory)).listFiles();
		boolean[] good=new boolean[files.length];
		int nmask=0;
		for(int i=0;i<files.length;i++){
			if(mask!=null){
				if(files[i].getName().contains(mask)){
					good[i]=true;
					nmask++;
				}
			}else{
				good[i]=true;
				nmask++;
			}
		}
		String[] names=new String[nmask];
		long[] created=new long[nmask];
		int counter=0;
		for(int i=0;i<nmask;i++){
			if(good[i]){
				names[counter]=files[i].getName();
				created[counter]=files[i].lastModified();
				counter++;
			}
		}
		int[] order=jsort.get_javasort_order(created);
		String[] temp=new String[nmask];
		for(int i=0;i<nmask;i++){
			temp[i]=names[order[i]];
		}
		return temp;
	}

	public String[] get_datemod_sorted_string_list(String directory,String[] masks,String[] notmasks){
		File[] files=(new File(directory)).listFiles();
		ArrayList<File> filelist=new ArrayList<File>();
		int nmask=0;
		for(int i=0;i<files.length;i++){
			boolean invalidated=false;
			if(notmasks!=null){
				for(int j=0;j<notmasks.length;j++){
					if(files[i].getName().contains(notmasks[j])){
						invalidated=true;
						break;
					}
				}
			}
			if(!invalidated){
				if(masks!=null){
					boolean found=true;
					for(int j=0;j<masks.length;j++){
						if(!files[i].getName().contains(masks[j])){
							found=false;
							break;
						}
					}
					if(found){
						nmask++;
						filelist.add(files[i]);
					}
				}else{
					nmask++;
					filelist.add(files[i]);
				}
			}
		}
		long[] created=new long[nmask];
		for(int i=0;i<nmask;i++)
			created[i]=filelist.get(i).lastModified();
		int[] order=jsort.get_javasort_order(created);
		String[] temp=new String[nmask];
		for(int i=0;i<nmask;i++){
			temp[i]=filelist.get(order[i]).getName();
		}
		return temp;
	}

	public String[] get_sorted_string_list(String directory,String[] masks,String[] notmasks){
		String[] names=(new File(directory)).list();
		ArrayList<String> namelist=new ArrayList<String>();
		int nmask=0;
		for(int i=0;i<names.length;i++){
			boolean invalidated=false;
			if(notmasks!=null){
				for(int j=0;j<notmasks.length;j++){
					if(names[i].contains(notmasks[j])){
						invalidated=true;
						break;
					}
				}
			}
			if(!invalidated){
				if(masks!=null){
					for(int j=0;j<masks.length;j++){
						if(names[i].contains(masks[j])){
							nmask++;
							namelist.add(names[i]);
							break;
						}
					}
				}else{
					nmask++;
					namelist.add(names[i]);
				}
			}
		}
		Collections.sort(namelist);
		String[] temp=new String[nmask];
		for(int i=0;i<nmask;i++){
			temp[i]=namelist.get(i);
		}
		return temp;
	}

	public String[] get_numeric_sorted_string_list(String directory,String mask){
		String[] names=(new File(directory)).list();
		boolean[] masked=new boolean[names.length];
		int nmask=0;
		if(mask==null){
			nmask=names.length;
		}else{
			for(int i=0;i<names.length;i++){
				if(names[i].contains(mask)){
					nmask++;
				}else{
					masked[i]=true;
				}
			}
		}
		String[] temp=new String[nmask];
		int counter=0;
		for(int i=0;i<names.length;i++){
			if(!masked[i]){
				temp[counter]=names[i].substring(0);
				counter++;
			}
		}
		return sort_strings_numeric(temp);
	}

	public String[] get_numeric_sorted_string_list(String directory,String[] masks,String[] notmasks){
		String[] names=(new File(directory)).list();
		boolean[] masked=new boolean[names.length];
		int nmask=0;
		for(int i=0;i<names.length;i++){
			boolean invalidated=false;
			if(notmasks!=null){
				for(int j=0;j<notmasks.length;j++){
					if(names[i].contains(notmasks[j])){
						invalidated=true;
						masked[i]=true;
						break;
					}
				}
			}
			if(!invalidated){
				if(masks!=null){
					boolean validated=false;
					for(int j=0;j<masks.length;j++){
						if(names[i].contains(masks[j])){
							nmask++;
							validated=true;
							break;
						}
						if(!validated){
							masked[i]=true;
						}
					}
				}else{
					nmask++;
				}
			}
		}
		String[] temp=new String[nmask];
		int counter=0;
		for(int i=0;i<names.length;i++){
			if(!masked[i]){
				temp[counter]=names[i].substring(0);
				counter++;
			}
		}
		return sort_strings_numeric(temp);
	}

	public String[] sort_strings_numeric(String[] data){
		List<List<String>> list=new ArrayList<List<String>>();
		for(int i=0;i<data.length;i++){
			List<String> templist=new ArrayList<String>();
			templist.add(data[i]);
			String num="";
			for(int j=0;j<data[i].length();j++){
				char ch=data[i].charAt(j);
				if(ch>=48&&ch<=57)
					num+=ch;
			}
			num="000000000000"+num;
			templist.add(num.substring(num.length()-12));
			list.add(templist);
		}
		Collections.sort(list,new Comparator<List<String>>(){
			public int compare(List<String> o1,List<String> o2){
				return o1.get(1).compareTo(o2.get(1));
			}
		});
		String[] newlist=new String[data.length];
		for(int i=0;i<data.length;i++){
			newlist[i]=(String)list.get(i).get(0);
		}
		return newlist;
	}

	public String[] openfiles_sort_string(String defaultdir,Component parent){
		File[] files=openfiles(defaultdir,parent);
		ArrayList<String> namelist=new ArrayList<String>();
		for(int i=0;i<files.length;i++){
			namelist.add(files[i].getPath());
		}
		Collections.sort(namelist);
		String[] temp=new String[files.length];
		for(int i=0;i<files.length;i++){
			temp[i]=namelist.get(i);
		}
		return temp;
	}

	public File[] openfiles_sort(String defaultdir,Component parent){
		File[] files=openfiles(defaultdir,parent);
		ArrayList<String> namelist=new ArrayList<String>();
		for(int i=0;i<files.length;i++){
			namelist.add(files[i].getPath());
		}
		Collections.sort(namelist);
		File[] temp=new File[files.length];
		for(int i=0;i<files.length;i++){
			temp[i]=new File(namelist.get(i));
		}
		return temp;
	}

	public File[] openfiles(String defaultdir,Component parent){
		JFileChooser fc=new JFileChooser();
		fc.setDialogTitle("Open Files");
		if(defaultdir!=null)
			fc.setCurrentDirectory(new File(defaultdir));
		fc.setMultiSelectionEnabled(true);
		fc.showOpenDialog(parent);
		return fc.getSelectedFiles();
	}

	public boolean writeintelint(OutputStream outstream,int data){
		try{
			byte[] dumbyte=new byte[4];
			dumbyte[0]=(byte)data;
			dumbyte[1]=(byte)(data>>8);
			dumbyte[2]=(byte)(data>>16);
			dumbyte[3]=(byte)(data>>24);
			outstream.write(dumbyte[0]);
			outstream.write(dumbyte[1]);
			outstream.write(dumbyte[2]);
			outstream.write(dumbyte[3]);
		}catch(IOException e){
			return false;
		}
		return true;
	}

	public boolean writeintelshort(OutputStream outstream,short data){
		try{
			byte[] dumbyte=new byte[2];
			dumbyte[0]=(byte)data;
			dumbyte[1]=(byte)(data>>8);
			outstream.write(dumbyte[0]);
			outstream.write(dumbyte[1]);
		}catch(IOException e){
			return false;
		}
		return true;
	}

	public boolean writeintelfloat(OutputStream outstream,float data){
		return writeintelint(outstream,Float.floatToIntBits(data));
	}

	public boolean writeintelintarray(OutputStream outstream,int[] data){
		for(int i=0;i<data.length;i++){
			if(!writeintelint(outstream,data[i])){
				return false;
			}
		}
		return true;
	}

	public boolean writeintelintarray(OutputStream outstream,float[] data){
		for(int i=0;i<data.length;i++){
			if(!writeintelint(outstream,(int)data[i])){
				return false;
			}
		}
		return true;
	}

	public boolean writeintelintarray(OutputStream outstream,float[] data,int length){
		for(int i=0;i<length;i++){
			if(!writeintelint(outstream,(int)data[i])){
				return false;
			}
		}
		return true;
	}

	public boolean writeintelintarray(OutputStream outstream,int[][] data){
		for(int i=0;i<data.length;i++){
			if(!writeintelintarray(outstream,data[i])){
				return false;
			}
		}
		return true;
	}

	public boolean writeintelintarray(OutputStream outstream,int[] data,int length){
		for(int i=0;i<length;i++){
			if(!writeintelint(outstream,data[i])){
				return false;
			}
		}
		return true;
	}

	public boolean writebytearray(OutputStream outstream,byte[] data){
		try{
			outstream.write(data);
		}catch(IOException e){
			return false;
		}
		return true;
	}

	public boolean writebytearray(OutputStream outstream,byte[] data,int length){
		byte[] data2;
		if(data.length==length){
			data2=data;
		}else{
			data2=new byte[length];
			if(data.length>length){
				System.arraycopy(data,0,data2,0,length);
			}else{
				System.arraycopy(data,0,data2,0,data.length);
			}
		}
		try{
			outstream.write(data2);
		}catch(IOException e){
			return false;
		}
		return true;
	}

	public boolean writeintelshortarray(OutputStream outstream,short[] data){
		for(int i=0;i<data.length;i++){
			if(!writeintelshort(outstream,data[i])){
				return false;
			}
		}
		return true;
	}

	public boolean writeintelshortarray(OutputStream outstream,short[] data,int length){
		for(int i=0;i<length;i++){
			if(!writeintelshort(outstream,data[i])){
				return false;
			}
		}
		return true;
	}

	public boolean writeintelfloatarray(OutputStream outstream,float[] data){
		for(int i=0;i<data.length;i++){
			if(!writeintelfloat(outstream,data[i])){
				return false;
			}
		}
		return true;
	}

	public boolean writeintelfloatarray(OutputStream outstream,float[][] data){
		for(int i=0;i<data.length;i++){
			if(!writeintelfloatarray(outstream,data[i])){
				return false;
			}
		}
		return true;
	}

	public boolean writeintelfloatarray(OutputStream outstream,float[][][] data){
		for(int i=0;i<data.length;i++){
			if(!writeintelfloatarray(outstream,data[i])){
				return false;
			}
		}
		return true;
	}

	public boolean writeintelfloatarray(OutputStream outstream,double[] data){
		float[] fdata=new float[data.length];
		for(int i=0;i<data.length;i++){
			fdata[i]=(float)data[i];
		}
		return writeintelfloatarray(outstream,fdata);
	}

	public boolean writeintelfloatarray(OutputStream outstream,float[] data,int length){
		for(int i=0;i<length;i++){
			if(!writeintelfloat(outstream,data[i])){
				return false;
			}
		}
		return true;
	}

	public boolean writeintelfloatarray(OutputStream outstream,double[] data,int length){
		float[] fdata=new float[data.length];
		for(int i=0;i<data.length;i++){
			fdata[i]=(float)data[i];
		}
		return writeintelfloatarray(outstream,fdata,length);
	}

	public boolean writestring(OutputStream outstream,String data){
		byte[] data2=data.getBytes();
		if(!writeintelint(outstream,data2.length)){
			return false;
		}
		try{
			outstream.write(data2);
		}catch(IOException e){
			return false;
		}
		return true;
	}

	public boolean writeboolean(OutputStream outstream,boolean data){
		byte temp=(byte)0;
		if(data){
			temp=(byte)1;
		}
		try{
			outstream.write(temp);
		}catch(IOException e){
			return false;
		}
		return true;
	}

	public int readintelint(InputStream instream){
		byte[] temp=new byte[4];
		int dumint;
		try{
			dumint=instream.read(temp);
		}catch(IOException e){
			return -1;
		}
		if(dumint<0){
			return -1;
		}else{
			return (int)(((temp[3]&0xff)<<24)|((temp[2]&0xff)<<16)|((temp[1]&0xff)<<8)|(temp[0]&0xff));
		}
	}

	public boolean readintelintfile(InputStream instream,int length,int[] data){
		int dumint;
		byte[] framebytes=new byte[length*4];
		try{
			dumint=instream.read(framebytes);
		}catch(IOException e){
			return false;
		}
		if(dumint<0){
			return false;
		}
		for(int i=0;i<length;i++){
			data[i]=(int)(((framebytes[4*i+3]&0xff)<<24)|((framebytes[4*i+2]&0xff)<<16)|((framebytes[4*i+1]&0xff)<<8)|(framebytes[4*i]&0xff));
		}
		return true;
	}

	public boolean readintelintfile(InputStream instream,int[] data){
		return readintelintfile(instream,data.length,data);
	}

	public boolean readintelintfile(InputStream instream,int[][] data){
		for(int i=0;i<data.length;i++){
			if(!readintelintfile(instream,data[i])){
				return false;
			}
		}
		return true;
	}

	public boolean readintelintfile(InputStream instream,int length,float[] data){
		int dumint;
		byte[] framebytes=new byte[length*4];
		try{
			dumint=instream.read(framebytes);
		}catch(IOException e){
			return false;
		}
		if(dumint<0){
			return false;
		}
		for(int i=0;i<length;i++){
			int tempint=(int)(((framebytes[4*i+3]&0xff)<<24)|((framebytes[4*i+2]&0xff)<<16)|((framebytes[4*i+1]&0xff)<<8)|(framebytes[4*i]&0xff));
			data[i]=(float)tempint;
		}
		return true;
	}

	public int readintelshort(InputStream instream){
		byte[] temp=new byte[2];
		int dumint;
		try{
			dumint=instream.read(temp);
		}catch(IOException e){
			return -1;
		}
		if(dumint<0){
			return -1;
		}else{
			return (int)(((temp[1]&0xff)<<8)|(temp[0]&0xff));
		}
	}

	public int readmotorolashort(InputStream instream){
		byte[] temp=new byte[2];
		int dumint;
		try{
			dumint=instream.read(temp);
		}catch(IOException e){
			return -1;
		}
		if(dumint<0){
			return -1;
		}else{
			return (int)(((temp[0]&0xff)<<8)|(temp[1]&0xff));
		}
	}
	
	public int readmotorolaint(InputStream instream){
		byte[] temp=new byte[4];
		int dumint;
		try{
			dumint=instream.read(temp);
		}catch(IOException e){
			return -1;
		}
		if(dumint<0){
			return -1;
		}else{
			return (int)(((temp[0]&0xff)<<24)|((temp[1]&0xff)<<16)|((temp[2]&0xff)<<8)|(temp[3]&0xff));
		}
	}
	
	public float readmotorolafloat(InputStream instream){
		int temp=readmotorolaint(instream);
		if(temp==-1) return Float.NaN;
		return Float.intBitsToFloat(temp);
	}

	public int readintelbyte(InputStream instream){
		byte[] data=new byte[1];
		try{
			instream.read(data);
		}catch(IOException e){
			return -1;
		}
		return (int)(data[0]&0xff);
	}

	public boolean readintelbytefile(InputStream instream,int length,byte[] data){
		int dumint;
		byte[] framebytes=new byte[length];
		try{
			dumint=instream.read(framebytes);
		}catch(IOException e){
			return false;
		}
		if(dumint<0){
			return false;
		}
		for(int i=0;i<length;i++){
			data[i]=framebytes[i];
		}
		return true;
	}

	public boolean readintelbytefile(InputStream instream,int length,float[] data){
		int dumint;
		byte[] framebytes=new byte[length];
		try{
			dumint=instream.read(framebytes);
		}catch(IOException e){
			return false;
		}
		if(dumint<0){
			return false;
		}
		for(int i=0;i<length;i++){
			data[i]=(float)(framebytes[i]&0xff);
		}
		return true;
	}

	public boolean readintelshortfile(InputStream instream,int length,short[] data){
		int dumint;
		byte[] framebytes=new byte[length*2];
		try{
			dumint=instream.read(framebytes);
		}catch(IOException e){
			return false;
		}
		if(dumint<0){
			return false;
		}
		for(int i=0;i<length;i++){
			data[i]=(short)(((framebytes[2*i+1]&0xff)<<8)|(framebytes[2*i]&0xff));
		}
		return true;
	}

	public boolean readintelshortfile(InputStream instream,int length,float[] data){
		int dumint;
		byte[] framebytes=new byte[length*2];
		try{
			dumint=instream.read(framebytes);
		}catch(IOException e){
			return false;
		}
		if(dumint<0){
			return false;
		}
		for(int i=0;i<length;i++){
			short temp=(short)(((framebytes[2*i+1]&0xff)<<8)|(framebytes[2*i]&0xff));
			data[i]=(float)(temp&0xffff);
		}
		return true;
	}

	public boolean readintelshortfile(InputStream instream,int length,int[] data){
		int dumint;
		byte[] framebytes=new byte[length*2];
		try{
			dumint=instream.read(framebytes);
		}catch(IOException e){
			return false;
		}
		if(dumint<0){
			return false;
		}
		for(int i=0;i<length;i++){
			short temp=(short)(((framebytes[2*i+1]&0xff)<<8)|(framebytes[2*i]&0xff));
			data[i]=temp&0xffff;
		}
		return true;
	}

	public float readintelfloat(InputStream instream){
		byte[] dumbyte=new byte[4];
		try{
			instream.read(dumbyte);
		}catch(IOException e){
			return -1.0f;
		}
		int tmp;
		tmp=(int)(((dumbyte[3]&0xff)<<24)|((dumbyte[2]&0xff)<<16)|((dumbyte[1]&0xff)<<8)|(dumbyte[0]&0xff));
		return Float.intBitsToFloat(tmp);
	}

	public boolean readintelfloatfile(InputStream instream,int length,float[] data){
		int dumint;
		byte[] framebytes=new byte[length*4];
		try{
			dumint=instream.read(framebytes);
		}catch(IOException e){
			return false;
		}
		if(dumint<0){
			return false;
		}
		for(int i=0;i<length;i++){
			int tempint=(int)(((framebytes[4*i+3]&0xff)<<24)|((framebytes[4*i+2]&0xff)<<16)|((framebytes[4*i+1]&0xff)<<8)|(framebytes[4*i]&0xff));
			data[i]=Float.intBitsToFloat(tempint);
		}
		return true;
	}

	public boolean readintelfloatfile(InputStream instream,float[] data){
		return readintelfloatfile(instream,data.length,data);
	}

	public boolean readintelfloatfile(InputStream instream,float[][] data){
		for(int i=0;i<data.length;i++){
			if(!readintelfloatfile(instream,data[i])){
				return false;
			}
		}
		return true;
	}

	public boolean readintelfloatfile(InputStream instream,float[][][] data){
		for(int i=0;i<data.length;i++){
			if(!readintelfloatfile(instream,data[i])){
				return false;
			}
		}
		return true;
	}

	public boolean readintelfloatfile(InputStream instream,int length,double[] data){
		int dumint;
		byte[] framebytes=new byte[length*4];
		try{
			dumint=instream.read(framebytes);
		}catch(IOException e){
			return false;
		}
		if(dumint<0){
			return false;
		}
		for(int i=0;i<length;i++){
			int tempint=(int)(((framebytes[4*i+3]&0xff)<<24)|((framebytes[4*i+2]&0xff)<<16)|((framebytes[4*i+1]&0xff)<<8)|(framebytes[4*i]&0xff));
			data[i]=(double)Float.intBitsToFloat(tempint);
		}
		return true;
	}

	public double readinteldouble(InputStream instream){
		byte[] b1=new byte[8];
		try{
			instream.read(b1);
		}catch(IOException e){
			return -1.0f;
		}
		long[] b=new long[8];
		for(int i=0;i<8;i++){
			b[i]=b1[i]&0xff;
		}
		long tempint=(long)((b[7]<<56)|(b[6]<<48)|(b[5]<<40)|(b[4]<<32)|(b[3]<<24)|(b[2]<<16)|(b[1]<<8)|b[0]);
		return Double.longBitsToDouble(tempint);
	}

	public boolean readinteldoublefile(InputStream instream,int length,float[] data){
		int dumint;
		byte[] framebytes=new byte[length*8];
		try{
			dumint=instream.read(framebytes);
		}catch(IOException e){
			return false;
		}
		if(dumint<0){
			return false;
		}
		long[] b=new long[8];
		for(int i=0;i<length;i++){
			for(int j=0;j<8;j++){
				b[j]=(long)(framebytes[8*i+j]&0xff);
			}
			long tempint=(long)((b[7]>>56)|(b[6]>>48)|(b[5]>>40)|(b[4]>>32)|(b[3]>>24)|(b[2]>>16)|(b[1]>>8)|b[0]);
			data[i]=(float)Double.longBitsToDouble(tempint);
		}
		return true;
	}

	public boolean readinteldoublefile(InputStream instream,int length,double[] data){
		int dumint;
		byte[] framebytes=new byte[length*8];
		try{
			dumint=instream.read(framebytes);
		}catch(IOException e){
			return false;
		}
		if(dumint<0){
			return false;
		}
		long[] b=new long[8];
		for(int i=0;i<length;i++){
			for(int j=0;j<8;j++){
				b[j]=(long)(framebytes[8*i+j]&0xff);
			}
			long tempint=(long)((b[7]>>56)|(b[6]>>48)|(b[5]>>40)|(b[4]>>32)|(b[3]>>24)|(b[2]>>16)|(b[1]>>8)|b[0]);
			data[i]=Double.longBitsToDouble(tempint);
		}
		return true;
	}

	public String readstring(InputStream instream){
		int length=readintelint(instream);
		byte[] data=null;
		if(length>=0){
			data=new byte[length];
			try{
				int temp=instream.read(data);
				if(temp<length){
					return null;
				}
				return new String(data);
			}catch(IOException e){
				return null;
			}
		}else{
			return null;
		}
	}

	public String readstring(InputStream instream,int length){
		byte[] data=null;
		if(length>=0){
			data=new byte[length];
			try{
				int temp=instream.read(data);
				if(temp<length){
					return null;
				}
				return new String(data);
			}catch(IOException e){
				return null;
			}
		}else{
			return null;
		}
	}

	public boolean readboolean(InputStream instream){
		byte[] data=new byte[1];
		try{
			instream.read(data);
			return(data[0]==(byte)1);
		}catch(IOException e){
			return false;
		}
	}

	public boolean skipstreambytes(InputStream instream,int skip){
		int left;
		try{
			left=skip;
			do{
				int temp=(int)instream.skip(left);
				left-=temp;
			}while(left>0);
		}catch(IOException e){
			return false;
		}
		return true;
	}

	public boolean skipstreambytes(InputStream instream,long skip){
		long left;
		try{
			left=skip;
			do{
				long temp=(long)instream.skip(left);
				left-=temp;
			}while(left>0);
		}catch(IOException e){
			return false;
		}
		return true;
	}

}
