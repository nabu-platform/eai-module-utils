package nabu.utils;

import java.io.IOException;
import java.io.InputStream;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.validation.constraints.NotNull;

import be.nabu.utils.codec.TranscoderUtils;
import be.nabu.utils.codec.impl.Base64Decoder;
import be.nabu.utils.codec.impl.Base64Encoder;
import be.nabu.utils.codec.impl.DeflateTranscoder;
import be.nabu.utils.codec.impl.GZIPDecoder;
import be.nabu.utils.codec.impl.GZIPEncoder;
import be.nabu.utils.codec.impl.InflateTranscoder;
import be.nabu.utils.io.IOUtils;
import be.nabu.utils.io.api.ByteBuffer;

@WebService
public class Transcoder {
	
	public enum Base64Type {
		STANDARD,
		URL
	}
	
	@WebResult(name = "stream")
	public InputStream transcode(@WebParam(name = "stream") InputStream input, @NotNull @WebParam(name = "transcoder") be.nabu.utils.codec.api.Transcoder<ByteBuffer> transcoder) throws IOException {
		return IOUtils.toInputStream(TranscoderUtils.wrapReadable(IOUtils.wrap(input), transcoder), true);
	}
	
	@WebResult(name = "transcoder")
	public Base64Encoder base64Encoder(@WebParam(name = "type") Base64Type type) {
		Base64Encoder base64Encoder = new Base64Encoder();
		if (Base64Type.URL.equals(type)) {
			base64Encoder.setUseBase64Url(true);
			// no line skipping
			base64Encoder.setBytesPerLine(0);
		}
		return base64Encoder;
	}
	
	@WebResult(name = "transcoder")
	public Base64Decoder base64Decoder(@WebParam(name = "type") Base64Type type) {
		Base64Decoder base64Decoder = new Base64Decoder();
		if (Base64Type.URL.equals(type)) {
			base64Decoder.setUseBase64Url(true);
		}
		return base64Decoder;
	}
	
	@WebResult(name = "transcoder")
	public GZIPEncoder gzipEncoder() {
		return new GZIPEncoder();
	}
	@WebResult(name = "transcoder")
	public GZIPDecoder gzipDecoder() {
		return new GZIPDecoder();
	}
	@WebResult(name = "transcoder")
	public DeflateTranscoder deflateEncoder() {
		return new DeflateTranscoder();
	}
	@WebResult(name = "transcoder")
	public InflateTranscoder deflateDecoder() {
		return new InflateTranscoder();
	}
}
