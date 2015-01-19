package project;

import java.util.Map;

import ij.*;



public class Util {
	
	public static final int BLACK = 0;
	public static final int WHITE = 255;
	
	
	
	private Util() {}
	
	
	
	public static String stripExt(String path) {
		int lastPoint = path.lastIndexOf(".");
		return (lastPoint == -1) ? path : path.substring(0, lastPoint);
	}
	
	
	
	public static String withTrailingSlash(String path) {
		if (!path.endsWith("/")) path += "/";
		return path;
	}
	
	
	
	public static <TKey, TVal> TVal getOrDefault(Map<TKey, TVal> map, TKey key, TVal defaultValue) {
		TVal value = map.get(key);
		return (value != null || map.containsKey(key)) ? value : defaultValue;
	}
	
	
	
	public static void log(String message) {
		IJ.log(message);
	}
	
	public static void log(Object message) {
		log(message.toString());
	}
	
}