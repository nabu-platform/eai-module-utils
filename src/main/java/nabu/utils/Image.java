package nabu.utils;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.String;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.jws.WebService;

import nabu.types.Dimension;

@WebService
public class Image {
	
	public Dimension dimension(InputStream stream) throws IOException {
		BufferedImage image = ImageIO.read(stream);
		if (image == null) {
			throw new IllegalArgumentException("Unknown image type");
		}
		Dimension dimension = new Dimension();
		dimension.setWidth(image.getWidth());
		dimension.setHeight(image.getHeight());
		return dimension;
	}
	
	public String type(InputStream stream) throws IOException {
		Iterator<ImageReader> imageReaders = ImageIO.getImageReaders(ImageIO.createImageInputStream(stream));
		return imageReaders.hasNext()
			? imageReaders.next().getFormatName()
			: null;
	}
	
	public byte [] convert(InputStream stream, String targetContentType) throws IOException {
		BufferedImage image = ImageIO.read(stream);
		if (image == null) {
			throw new IllegalArgumentException("Unknown image type");
		}
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ImageIO.write(image, targetContentType, output);
		return output.toByteArray();
	}
	
	public byte [] subImage(InputStream stream, Integer x, Integer y, Integer width, Integer height, String targetContentType) throws IOException {
		BufferedImage image = ImageIO.read(stream);
		if (image == null) {
			throw new IllegalArgumentException("Unknown image type");
		}
		if (x == null) {
			x = 0;
		}
		if (y == null) {
			y = 0;
		}
		if (width == null) {
			width = image.getWidth() - x;
		}
		if (height == null) {
			height = image.getHeight() - y;
		}
		BufferedImage subimage = image.getSubimage(x, y, width, height);
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ImageIO.write(subimage, targetContentType, output);
		return output.toByteArray();
	}
	
	public byte [] resize(InputStream stream, Integer width, Integer height, String targetContentType) throws IOException {
		BufferedImage image = ImageIO.read(stream);
		if (image == null) {
			throw new IllegalArgumentException("Unknown image type");
		}
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
		BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = resizedImage.createGraphics();
		graphics.drawImage(image, 0, 0, width, height, null);
		graphics.dispose();
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ImageIO.write(resizedImage, targetContentType, output);
		return output.toByteArray();
	}
}
