
import java.awt.Rectangle;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.regex.Pattern;

import ij.*;
import ij.gui.*;
import ij.io.FileSaver;
import ij.io.Opener;
import ij.process.*;
import ij.plugin.PlugIn;
import project.Fragment;
import static java.lang.Math.*;
import static project.Util.*;


// TODO read paths from params

// imagej -eval "run('Project ', 'parameters');"
// imagej -eval "run('Project ', 'parameters');" -batch

// imagej -eval "run('Project ', 'a=b|blu=sth with spaces|ka|output=C:/Users/Felix/Downloads/TH11 Touhou Chireiden ~ Subterranean Animism|Ah ja=Haha|input=C:/Program Files/ImageJ/plugins/Project/BachDigital/SchriftprobenHandschrift');"


public class Project_ implements PlugIn {
	
	Map<String, String> parameters;
	
	String INPUT_PATH;
	String OUTPUT_PATH;
	boolean RECURSIVE;
	
	
	
	@Override
	public void run(String arg) {
		log("");
		
		
		Map<String, String> parameters = parseParameters();
		final String projectPath = "plugins/Project/";
		INPUT_PATH  = withTrailingSlash(getOrDefault(parameters, "input" , projectPath + "input/"));
		OUTPUT_PATH = withTrailingSlash(getOrDefault(parameters, "output", projectPath + "output/"));
		RECURSIVE   = parameters.containsKey("recursive");
		
		
		Opener opener = new Opener();
		for (String filename : createFileQueue(INPUT_PATH)) {
			ImagePlus image = opener.openImage(filename);
			if (image == null) {
				log("Unable to open file: " + filename);
				continue;
			} else {
				log("File opened: " + filename);
				image.setTitle(stripExt(image.getTitle()));
				processImage(image);
			}
		}
		
		
		log("Plugin execution complete");
	}
	
	
	
	
	/*
	private void parseParameters() {
		final String quoteChar     = "\"";
		final String separatorChar = " ";
		
		String parameterString = Macro.getOptions().trim();
		log("parameters: >" + parameterString + "<");
		
		LinkedList<String> parameters = new LinkedList<String>();
		
		int lastCut = 0;
		boolean insideQuotes = false;
		for (int pos = 0; pos < parameterString.length(); pos++) {
			String character = parameterString.substring(pos, pos + 1);
			if (quoteChar.equals(character)) insideQuotes = !insideQuotes;
			if (!insideQuotes) {
				if (separatorChar.equals(character))
					parameters.add(parameterString.substring(lastCut, pos));
			}
		}
		
		
	}*/
	private static Map<String, String> parseParameters() {
		final String parameterSeparator = "|";
		final String keyValueSeparator  = "=";
		
		String parameterString = Macro.getOptions();
		parameterString = (parameterString == null) ? "" : parameterString.trim();
		//log("parameters: >" + parameterString + "<");
		
		Map<String, String> parameters = new HashMap<String, String>(3);
		
		for (String keyAndValue : parameterString.split(Pattern.quote(parameterSeparator))) {
			int sepPos = keyAndValue.indexOf(keyValueSeparator);
			if (sepPos == -1) {
				parameters.put(keyAndValue, null);
			} else {
				parameters.put(keyAndValue.substring(0, sepPos) .trim().toLowerCase(),
			                   keyAndValue.substring(sepPos + 1).trim());
			}
		}
		
		for (Map.Entry<String, String> e : parameters.entrySet()) {
			log("Parsed parameter: key(" + e.getKey() + ") value(" + e.getValue() + ")");
		}
		
		return parameters;
		/*
		LinkedList<String> parameters = new LinkedList<String>();
		
		int lastCut = 0;
		String key = 
		for (int pos = 0; pos < parameterString.length(); pos++) {
			String character = parameterString.substring(pos, pos + 1);
			if (separator.equals(character))
				parameters.add(parameterString.substring(lastCut, pos));
		}
		*/
		
	}
	
	
	
	
	
	private Queue<String> createFileQueue(String path) {
		List<Integer> SUPPORTED_FILETYPES = Arrays.asList(Opener.TIFF, Opener.BMP, Opener.DICOM, Opener.FITS, Opener.PGM, Opener.GIF, Opener.JPEG, Opener.PNG); // according to Opener.openImage docs 
		
		Queue<String> queue = new LinkedList<String>();
		
		// TODO: fix Exception for empty/nonexistant folders
		Opener opener = new Opener();
		for (File file : new File(path).listFiles()) {
			if (file.isFile()) {
				String filename = file.toString();
				
				if (!SUPPORTED_FILETYPES.contains(opener.getFileType(filename))) {
					log("Skipping file: " + filename);
					log(opener.getFileType(filename));
					continue;
				}
				
				log("Adding file to queue: " + filename);
				queue.add(filename);
			} else if (RECURSIVE && file.isDirectory()) {
				queue.addAll(createFileQueue(file.toString()));
			}
		}
		
		return queue;
	}
	
	
	
	
	private void processImage(ImagePlus image) {
		log("Processing Image: " + image.getTitle());
		
		binarizeImage(image);
		LinkedList<Rectangle> regions = findRegions(image);
		LinkedList<Fragment> fragments = splitImage(image, /*regions*/new Rectangle(500, 1000, 1000, 1000));
		
		
	}
	
	
	
	
	
	private void binarizeImage(ImagePlus image) {
		ImageConverter.setDoScaling(true);
		new ImageConverter(image).convertToGray8();
		ImageProcessor processor = image.getProcessor();
		processor.autoThreshold();
	}
	
	
	
	
	
	private LinkedList<Rectangle> findRegions(ImagePlus image) {
		LinkedList<Rectangle> regions = new LinkedList<Rectangle>();
		
		/*
		do horizontal linescans over the image
		cut left and right of line until large portion (1/10 image?) of white at the borders
		possibly also cut white stretches exceeding a certain length
		line is probably text if certain avg black width and avg distance between (determine experimentally)

		optimization: not scans for every line, but at some distance and only do inbetween if black found
		*/
		
		ImageProcessor processor = image.getProcessor();
		
		for (int y = 0; y < image.getHeight(); y++) {
			/*processor.setRoi(0, y, image.getWidth(), 1);
			ImageProcessor lineProcessor = processor.crop();*/
			int blackPixels = 0;
			for (int x = 0; x < image.getWidth(); x++) {
				if (processor.getPixel(x, y) == BLACK) blackPixels++;
			}
			double blackPortion = blackPixels / (double)image.getWidth();
			boolean lineIsText = .125 <= blackPortion && blackPortion <= .3;
			
			log("Line scan for y = " + y + ": text = " + lineIsText);
		}
		
		
		return regions;
	}
	
	
	
	private LinkedList<Fragment> splitImage(ImagePlus image) {
		 return splitImage(image, new Rectangle(image.getWidth(), image.getHeight()));
	}
	
	private LinkedList<Fragment> splitImage(ImagePlus image, Rectangle... regions) {
		 return splitImage(image, Arrays.asList(regions));
	}
	
	private LinkedList<Fragment> splitImage(ImagePlus image, List<Rectangle> regions) {
		final int fragmentMinSize = max(image.getWidth(), image.getHeight()) / 1000 + 1;	// ~4p
		final int fragmentMaxSize = min(image.getWidth(), image.getHeight()) / 10;			// 10%
		
		log("Splitting image " + image.getTitle() + " with " + 1 + " region(s)");
		
		image.show();
		ImageProcessor processor = image.getProcessor();
		processor.setColor(WHITE);
		
		LinkedList<Fragment> fragments = new LinkedList<Fragment>();
		
		int fragmentNr = 0;
		for (Rectangle region : regions) {	
			log("Splitting region " + region);
			for (int y = region.y; y < region.y + region.height; y++) {
				for (int x = region.x; x < region.x + region.width; x++) {
					if (processor.getPixel(x, y) == BLACK) {
						IJ.doWand(image, x, y, 0, "8-connected");
						// TODO retrieve only the shape or the whole bounding box?
						Roi selection = image.getRoi();
						Rectangle selectionBounds = selection.getBounds();
						
						String logMsgSuffix = "";
						
						boolean skip;
						if (selectionBounds.width < fragmentMinSize || selectionBounds.height < fragmentMinSize) {
							skip = true;
							logMsgSuffix = " -> skipping, too small";
						} else if (selectionBounds.width > fragmentMaxSize || selectionBounds.height > fragmentMaxSize) {
							skip = true;
							logMsgSuffix = " -> skipping, too large";
						} else {
							skip = false;
							image.copy(false);
							fragmentNr++;
							logMsgSuffix = " -> keeping as #" + fragmentNr;
						}
						processor.fill(selection);
						log("Fragment found with bounding box " + selectionBounds.toString() + logMsgSuffix);
						
						if (skip) continue;
						
						ImageProcessor fragmentProcessor = new ByteProcessor(selectionBounds.width, selectionBounds.height);
						Fragment fragment = new Fragment("fragment " + fragmentNr, fragmentProcessor, selectionBounds);
						fragmentProcessor.setColor(WHITE);
						fragmentProcessor.resetRoi();
						fragmentProcessor.fill();
						fragment.paste();
						
						fragments.add(fragment);
					}
				}
			}
		}
		
		for (Fragment fragment : fragments) saveImage(fragment, OUTPUT_PATH);
		
		return fragments;
	}
	
	
	
	
	
	private void saveImage(ImagePlus image, String path) {
		String filename = path + image.getTitle() + ".png";
		if (new FileSaver(image).saveAsPng(filename)) {
			log("Image saved: " + filename);
		} else {
			log("Unable to save image: " + filename);
		}
		// TODO: check if path exists already, cause it needs to
	}
	
	
}

