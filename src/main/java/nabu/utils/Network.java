package nabu.utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;

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
	
	@WebResult(name = "addresses")
	public java.util.List<java.lang.String> getLocalAddresses() throws SocketException {
		java.util.List<java.lang.String> result = new ArrayList<java.lang.String>();
		Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
		while (networkInterfaces.hasMoreElements()) {
			NetworkInterface network = networkInterfaces.nextElement();
			Enumeration<InetAddress> addresses = network.getInetAddresses();
			while (addresses.hasMoreElements()) {
				InetAddress address = addresses.nextElement();
				if (address.isAnyLocalAddress() || address.isLoopbackAddress() || address.isLinkLocalAddress() || address.isSiteLocalAddress()) {
					result.add(address.getHostAddress());
				}
			}
		}
		return result;
	}
	
	@WebResult(name = "addresses")
	public java.util.List<java.lang.String> getPublicAddresses() throws SocketException {
		java.util.List<java.lang.String> result = new ArrayList<java.lang.String>();
		Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
		while (networkInterfaces.hasMoreElements()) {
			NetworkInterface network = networkInterfaces.nextElement();
			Enumeration<InetAddress> addresses = network.getInetAddresses();
			while (addresses.hasMoreElements()) {
				InetAddress address = addresses.nextElement();
				if (!address.isAnyLocalAddress() && !address.isLoopbackAddress() && !address.isLinkLocalAddress() && !address.isSiteLocalAddress()) {
					result.add(address.getHostAddress());
				}
			}
		}
		return result;
	}
}
