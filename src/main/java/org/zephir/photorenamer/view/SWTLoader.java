package org.zephir.photorenamer.view;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

public class SWTLoader {
	private final static String SWT_VERSION = "3.6.2";
	public static Object display = null;
	
	public static final String MAIN_CLASS = "org.zephir.photorenamer.view.PhotoRenamerForm";

	public static void main(String[] args) {
		loadSWTLibrary();
		
		try {
	        // initialize the Display object in the main thread
	        Class<?> mainClass = SWTLoader.class.getClassLoader().loadClass("org.eclipse.swt.widgets.Display");
			display = mainClass.newInstance();
			
			// launch the application
			URLClassLoader classLoader = (URLClassLoader) SWTLoader.class.getClassLoader();
			mainClass = classLoader.loadClass(MAIN_CLASS);
			Method main = mainClass.getMethod("main", new Class[] { String[].class });
			main.invoke(null, new Object[] { args });
		} catch (Exception e) {
	        System.err.println("Loader KO: " + e);
	        e.printStackTrace();
	    }
	}

	private static void loadSWTLibrary() {
		URLClassLoader classLoader = (URLClassLoader) SWTLoader.class.getClassLoader();
        
		try {
			classLoader.loadClass("org.eclipse.swt.graphics.Point");
			System.err.println("loadSwtJar() WARN: SWT library already loaded !");
		} catch (ClassNotFoundException e) { }	
			
		try {
	        String osName = System.getProperty("os.name").toLowerCase();
	        String osArch = System.getProperty("os.arch").toLowerCase();
	        Method addUrlMethod = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
	        addUrlMethod.setAccessible(true);

	        String swtFileNameOsPart = 
	            osName.contains("win") ? "win32" :
	            osName.contains("mac") ? "macosx" :
	            osName.contains("linux") || osName.contains("nix") ? "linux_gtk" :
	            null; 
	        
	        if (swtFileNameOsPart == null) {
	        	throw new RuntimeException("Unknown OS name: "+osName);
	        }

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
					url = SWTLoader.class.getResource(path);
				}
			}
			return url;
		} catch (Exception e) {
			return null;
		}
	}

	public static Object getDisplay() {
		return display;
	}
}
