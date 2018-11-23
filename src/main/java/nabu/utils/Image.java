package nabu.utils;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.String;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import be.nabu.utils.io.IOUtils;
import nabu.utils.types.ImageMetaData;

@WebService
public class Image {
	
	@WebResult(name = "metaData")
	public ImageMetaData getMetaData(@WebParam(name = "image") InputStream stream) throws IOException {
		if (stream == null) {
			return null;
		}
		ImageReader reader = toReader(stream);
		BufferedImage image = reader.read(0);
		ImageMetaData metaData = new ImageMetaData();
		metaData.setWidth(image.getWidth());
		metaData.setHeight(image.getHeight());
		metaData.setContentType(reader.getOriginatingProvider().getMIMETypes()[0]);
		return metaData;
	}

	private static ImageReader toReader(InputStream stream) throws IOException {
		ImageInputStream imageInputStream = ImageIO.createImageInputStream(stream);
		Iterator<ImageReader> imageReaders = ImageIO.getImageReaders(imageInputStream);
		if (!imageReaders.hasNext()) {
			throw new IllegalArgumentException("Unknown image type");
		}
		ImageReader reader = imageReaders.next();
		reader.setInput(imageInputStream);
		return reader;
	}
	
	@WebResult(name = "image")
	public byte [] transform(@WebParam(name = "image") InputStream stream, @WebParam(name = "x") Integer x, @WebParam(name = "y") Integer y, @WebParam(name = "width") Integer width, @WebParam(name = "height") Integer height, @WebParam(name = "targetContentType") String targetContentType) throws IOException {
		if (stream == null) {
			return null;
		}
		ImageReader reader = toReader(stream);
		if (targetContentType == null) {
			targetContentType = reader.getOriginatingProvider().getMIMETypes()[0];
		}
		Iterator<ImageWriter> writers = ImageIO.getImageWritersByMIMEType(targetContentType);
		if (!writers.hasNext()) {
			throw new IllegalArgumentException("No handler for the content type: " + targetContentType);
		}
		BufferedImage image = reader.read(0);
		if (x == null || x < 0) {
			x = 0;
		}
		else if (x != null && x > image.getWidth()) {
			x = image.getWidth();
		}
		if (y == null || y < 0) {
			y = 0;
		}
		else if (y != null && y > image.getHeight()) {
			y = image.getHeight();
		}
		if (width == null || width < 0 || width > image.getWidth() - x) {
			width = image.getWidth() - x;
		}
		if (height == null || height < 0 || height > image.getHeight() - y) {
			height = image.getHeight() - y;
		}
		image = image.getSubimage(x, y, width, height);
		ImageWriter writer = writers.next();
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ImageOutputStream imageOutput = ImageIO.createImageOutputStream(output);
		writer.setOutput(imageOutput);
		writer.write(image);
		imageOutput.flush();
		return output.toByteArray();
	}
	
	@WebResult(name = "image")
	public byte [] resize(@WebParam(name = "image") InputStream stream, @WebParam(name = "width") Integer width, @WebParam(name = "height") Integer height, @WebParam(name = "targetContentType") String targetContentType) throws IOException {
		if (stream == null) {
			return null;
		}
		
		ImageReader reader = toReader(stream);
		if (targetContentType == null) {
			targetContentType = reader.getOriginatingProvider().getMIMETypes()[0];
		}
		Iterator<ImageWriter> writers = ImageIO.getImageWritersByMIMEType(targetContentType);
		if (!writers.hasNext()) {
			throw new IllegalArgumentException("No handler for the content type: " + targetContentType);
		}
		BufferedImage image = reader.read(0);
		
		if (width == null && height == null) {
			throw new IllegalArgumentException("Either width or height has to be filled in");
		}
		else if (width == null) {
			double factor = (double) height / (double) image.getHeight();
			width = (int) (factor * image.getWidth());
		}
		else if (height == null) {
			double factor = (double) width / (double) image.getWidth();
			height = (int) (factor * image.getHeight());
		}
		BufferedImage resizedImage = new BufferedImage(width, height, image.getType());
		Graphics2D graphics = resizedImage.createGraphics();
		graphics.setComposite(AlphaComposite.Src);
		graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphics.drawImage(image, 0, 0, width, height, null);
		graphics.dispose();
		ImageWriter writer = writers.next();
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ImageOutputStream imageOutput = ImageIO.createImageOutputStream(output);
		writer.setOutput(imageOutput);
		if (targetContentType.toLowerCase().contains("jpg") || targetContentType.toLowerCase().contains("jpeg")) {
			JPEGImageWriteParam jpegParams = new JPEGImageWriteParam(null);
			jpegParams.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
			jpegParams.setCompressionQuality(1f);
			writer.write(null, new IIOImage(resizedImage, null, null), jpegParams);
		}
		else {
			writer.write(resizedImage);
		}
		imageOutput.flush();
		return output.toByteArray();
	}
	
	// inspiration from: https://github.com/collicalex/JPEGOptimizer/blob/master/src/utils/ImageUtils.java
	// the maxdiff should be a number between 0 and 1, sensible values seem to be around 0.5
	public InputStream optimize(@WebParam(name = "image") InputStream stream, @WebParam(name = "maxDiff") Double maxDiff) throws IOException {
		// seems sensible value...
		if (maxDiff == null) {
			maxDiff = 0.4;
		}
		byte[] bytes = IOUtils.toBytes(IOUtils.wrap(stream));
		ImageReader reader = toReader(new ByteArrayInputStream(bytes));
		BufferedImage image = reader.read(0);
		
		// transform to RGB
		if (image.getType() != BufferedImage.TYPE_INT_RGB) {
			BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
			Graphics2D graphics = newImage.createGraphics();
			graphics.setComposite(AlphaComposite.Src);
			graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			graphics.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
			graphics.dispose();
			image = toImage(toJPEG(newImage, 100));
		}
		
		byte[] result = bytes;
		for (float i = 95; i >= 10; i -= 5) {
			byte[] imageBytes = toJPEG(image, i);
			BufferedImage optimizedImage = toImage(imageBytes);
			double diff = diffImageFastest(image, optimizedImage);
			if (diff <= maxDiff) {
				result = imageBytes;
			}
			else {
				break;
			}
		}
		return new ByteArrayInputStream(result);
	}
	private BufferedImage toImage(byte [] bytes) throws IOException {
		ImageReader optimizedReader = toReader(new ByteArrayInputStream(bytes));
		return optimizedReader.read(0);
	}
	private byte[] toJPEG(BufferedImage image, float quality) throws IOException {
		JPEGImageWriteParam jpegParams = new JPEGImageWriteParam(null);
		jpegParams.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		jpegParams.setCompressionQuality(quality / 100f);
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ImageOutputStream imageOutput = ImageIO.createImageOutputStream(output);
		Iterator<ImageWriter> writers = ImageIO.getImageWritersByMIMEType("image/jpeg");
		if (!writers.hasNext()) {
			throw new IllegalArgumentException("No handler for the content type: image/jpeg");
		}
		ImageWriter writer = writers.next();
		writer.setOutput(imageOutput);
		writer.write(null, new IIOImage(image, null, null), jpegParams);
		imageOutput.flush();
		return output.toByteArray();
	}
	
	private static double diffImageFastest(BufferedImage img1, BufferedImage img2) throws IOException {
		int width1 = img1.getWidth();
		int width2 = img2.getWidth();
		int height1 = img1.getHeight();
		int height2 = img2.getHeight();

		if (width1 != width2 || height1 != height2) {
			throw new IOException("Images have different sizes");
		}

		DataBuffer db1 = img1.getRaster().getDataBuffer();
		DataBuffer db2 = img2.getRaster().getDataBuffer();

		double diff = 0;
		int size = db1.getSize(); // size = width * height * 3
		double p = 0;

		// TODO: jpeg format v9 can use 12bit per channel, see:
		// http://www.tomshardware.fr/articles/jpeg-lossless-12bit,1-46742.html

		if (size == width1 * height1 * 3) { 
			// RGB 24bit per pixel - 3 bytes
			// per pixel: 1 for R, 1 for G, 1 for B

			for (int i = 0; i < size; i += 3) {
//				double deltaR = (db2.getElem(i) - db1.getElem(i)) / 255.;
//				double deltaG = (db2.getElem(i+1) - db1.getElem(i+1)) / 255.;
//				double deltaB = (db2.getElem(i+2) - db1.getElem(i+2)) / 255.;
//				diff += java.lang.Math.sqrt(java.lang.Math.pow(deltaR, 2) + java.lang.Math.pow(deltaG, 2) + java.lang.Math.pow(deltaB, 2));

				double deltaR = (db2.getElem(i) - db1.getElem(i));
				double deltaG = (db2.getElem(i + 1) - db1.getElem(i + 1));
				double deltaB = (db2.getElem(i + 2) - db1.getElem(i + 2));
				diff += java.lang.Math.sqrt(((deltaR * deltaR) + (deltaG * deltaG) + (deltaB * deltaB)) / 65025.);
			}

			double maxPixDiff = java.lang.Math.sqrt(3); 
			// max diff per color component is 1. So max diff on the 3 RGB component is 1+1+1.
			double n = width1 * height1;
			p = diff / (n * maxPixDiff);
		}
		else if (size == width1 * height1) { 
			// Gray 8bit per pixel - Don't know if it's possible in jpeg, but just in case, code it! :)
			for (int i = 0; i < size; i++) {
				diff += (db2.getElem(i) - db1.getElem(i)) / 255.;
			}
			p = diff / size;
		}

		return p * 100;
	}
}
