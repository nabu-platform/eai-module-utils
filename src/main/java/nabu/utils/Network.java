package nabu.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

@WebService
public class Network {
	@WebResult(name = "local")
	public boolean isLocal(@WebParam(name = "ip") java.lang.String ip) throws UnknownHostException {
		InetAddress byName = InetAddress.getByName(ip);
		return byName != null && (byName.isAnyLocalAddress() || byName.isLoopbackAddress());
	}
	@WebResult(name = "siteLocal")
	public boolean isSiteLocal(@WebParam(name = "ip") java.lang.String ip) throws UnknownHostException {
		InetAddress byName = InetAddress.getByName(ip);
		return byName != null && (byName.isAnyLocalAddress() || byName.isLoopbackAddress() || byName.isLinkLocalAddress() || byName.isSiteLocalAddress());
	}
}
