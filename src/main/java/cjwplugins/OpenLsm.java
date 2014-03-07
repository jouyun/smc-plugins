package cjwplugins;

import ij.IJ;
import ij.ImageJ;
import ij.plugin.PlugIn;


public class OpenLsm implements PlugIn {

	public void run(String arg) {
		IJ.log(arg);
	}
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String lsmfile = "/Users/cjw/TechDevelopment/SomeImages/Y007_000_zoomf_May_07_2013_7_0_0.lsm"; 
		Class<?> clazz = OpenLsm.class;
		String url = clazz.getResource("/" + clazz.getName().replace('.', '/') + ".class").toString();
		String tmp1 = url.substring(5, url.length() - clazz.getName().length() - 6);
		tmp1 = url.substring(5, url.length());
		int last = tmp1.lastIndexOf("/");
		String pluginsDir = tmp1.substring(0,last);
		System.getProperties().setProperty("plugins.dir", pluginsDir);
		System.out.println(url);
		System.out.println(clazz.getName());
		System.out.println(System.getProperty("plugins.dir"));
		System.out.println(pluginsDir);
		System.out.println(last);
		
		new ImageJ(null);
		IJ.runPlugIn(clazz.getName(), lsmfile);
	}

}
