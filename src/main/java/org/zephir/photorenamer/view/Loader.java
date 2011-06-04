package org.zephir.photorenamer.view;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

public class Loader {
	private final static String SWT_VERSION = "3.6.2";
	
	public static void main(String[] args) {
		loadSwtJar();
		
		PhotoRenamerForm.main(new String[] {});
	}
	
	private static void loadSwtJar() {
		try {
	        String osName = System.getProperty("os.name").toLowerCase();
	        String osArch = System.getProperty("os.arch").toLowerCase();
	        URLClassLoader classLoader = (URLClassLoader) Loader.class.getClassLoader();
	        Method addUrlMethod = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
	        addUrlMethod.setAccessible(true);

	        String swtFileNameOsPart = 
	            osName.contains("win") ? "win32" :
	            osName.contains("mac") ? "macosx" :
	            osName.contains("linux") || osName.contains("nix") ? "linux_gtk" :
	            ""; // throw new RuntimeException("Unknown OS name: "+osName)

	        String swtFileNameArchPart = osArch.contains("64") ? "x64" : "x86";
	        String swtFileName = "swt_"+swtFileNameOsPart+"_"+swtFileNameArchPart+"-"+SWT_VERSION+".jar";
	        
	        URL swtFileUrl = getResourceURL(swtFileName);
	        if (swtFileUrl == null) {
	        	System.out.println("SWT jar not found ("+swtFileName+"), program exiting !");
	        	System.exit(0);
	        } else {
	        	System.out.println("Loading SWT jar: '"+swtFileUrl+"'");
	        	addUrlMethod.invoke(classLoader, swtFileUrl);
	        }
	    } catch (Exception e) {
	        System.err.println("loadSwtJar() KO: " + e);
	        e.printStackTrace();
	    }
	}
	
	/**
	 * @param path
	 * @return the absolute canonical path of the ressource
	 */
	public static URL getResourceURL(String path) {
		try {
			URL url = null;

			File file = new File(path);
			if (file.exists() && file.isDirectory()) {
				url = file.toURI().toURL();
			} else {
				url = Thread.currentThread().getContextClassLoader().getResource(path);
				if (url == null) {
					url = Loader.class.getResource(path);
				}
			}
			return url;
		} catch (Exception e) {
			return null;
		}
	}
}
