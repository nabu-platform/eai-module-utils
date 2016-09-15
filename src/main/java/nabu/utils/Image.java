package nabu.utils;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
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
}
