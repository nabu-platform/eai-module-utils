/*
* Copyright (C) 2014 Alexander Verbruggen
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program. If not, see <https://www.gnu.org/licenses/>.
*/

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
import be.nabu.utils.codec.impl.DeflateTranscoder.DeflaterLevel;
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
	public Base64Encoder base64Encoder(@WebParam(name = "type") Base64Type type, @WebParam(name = "bytesPerLine") Integer bytesPerLine) {
		Base64Encoder base64Encoder = new Base64Encoder();
		if (Base64Type.URL.equals(type)) {
			base64Encoder.setUseBase64Url(true);
			// no line skipping
			base64Encoder.setBytesPerLine(0);
		}
		if (bytesPerLine != null) {
			base64Encoder.setBytesPerLine(bytesPerLine);
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
	public DeflateTranscoder deflateEncoder(@WebParam(name = "nowrap") Boolean nowrap) {
		return new DeflateTranscoder(DeflaterLevel.BEST_COMPRESSION, nowrap != null && nowrap);
	}
	@WebResult(name = "transcoder")
	public InflateTranscoder deflateDecoder(@WebParam(name = "nowrap") Boolean nowrap) {
		return new InflateTranscoder(nowrap != null && nowrap);
	}
}
