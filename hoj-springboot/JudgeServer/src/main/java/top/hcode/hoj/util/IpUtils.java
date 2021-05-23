package top.hcode.hoj.util;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import java.net.*;
import java.util.Enumeration;

/**
 * @Author: Himit_ZH
 * @Date: 2020/10/30 11:12
 * @Description:
 */
@Slf4j
public class IpUtils {
    public static String getUserIpAddr(HttpServletRequest request) {
        String ipAddress = null;
        try {
            ipAddress = request.getHeader("x-forwarded-for");
            if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getHeader("Proxy-Client-IP");
            }
            if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getHeader("WL-Proxy-Client-IP");
            }
            if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getRemoteAddr();
                if (ipAddress.equals("127.0.0.1")) {
                    // 根据网卡取本机配置的IP
                    try {
                        ipAddress = InetAddress.getLocalHost().getHostAddress();
                    } catch (UnknownHostException e) {
                        log.error("用户ip获取异常------->{}", e.getMessage());
                    }
                }
            }
            // 通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
            if (ipAddress != null) {
                if (ipAddress.contains(",")) {
                    return ipAddress.split(",")[0];
                } else {
                    return ipAddress;
                }
            } else {
                return "";
            }
        } catch (Exception e) {
            log.error("用户ip获取异常------->{}", e.getMessage());
            return "";
        }
    }

    public static String getServiceIp(){
        InetAddress address = null;
        try {
            address = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            log.error("本地ip获取异常---------->{}", e.getMessage());
        }
        return address.getHostAddress(); //返回IP地址
    }

    public static String getLocalIpv4Address() {
        Enumeration<NetworkInterface> ifaces = null;
        try {
            ifaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            log.error("本地ipv4获取异常---------->{}", e.getMessage());
        }
        String siteLocalAddress = null;
        while (ifaces.hasMoreElements()) {
            NetworkInterface iface = ifaces.nextElement();
            Enumeration<InetAddress> addresses = iface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress addr = addresses.nextElement();
                String hostAddress = addr.getHostAddress();
                if (addr instanceof Inet4Address) {
                    if (addr.isSiteLocalAddress()) {
                        siteLocalAddress = hostAddress;
                    } else {
                        return hostAddress;
                    }
                }
            }
        }
        return siteLocalAddress == null ? "" : siteLocalAddress;
    }
}