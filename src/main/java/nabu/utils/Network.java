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
