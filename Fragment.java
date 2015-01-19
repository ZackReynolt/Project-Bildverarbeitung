package project;

import java.awt.Rectangle;

import ij.*;
import ij.process.ImageProcessor;


public class Fragment extends ImagePlus {
	
	public final Rectangle location;
	
	public Fragment(String title, ImageProcessor imageProcessor, Rectangle location) {
		super(title, imageProcessor);
		this.location = location;
	}
	
}