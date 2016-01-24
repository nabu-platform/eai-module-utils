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
	
	@WebResult(name = "stream")
	public InputStream transcode(@WebParam(name = "stream") InputStream input, @NotNull @WebParam(name = "transcoder") be.nabu.utils.codec.api.Transcoder<ByteBuffer> transcoder) throws IOException {
		return IOUtils.toInputStream(TranscoderUtils.wrapReadable(IOUtils.wrap(input), transcoder), true);
	}
	
	@WebResult(name = "transcoder")
	public Base64Encoder base64Encoder() {
		return new Base64Encoder();
	}
	
	@WebResult(name = "transcoder")
	public Base64Decoder base64Decoder() {
		return new Base64Decoder();
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
