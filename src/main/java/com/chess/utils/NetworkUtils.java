package com.chess.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Utility class for network-related functions.
 */
public class NetworkUtils {
    
    public static final int PORT = 9999;

    /**
     * Attempts to find the best local IP address for LAN connections.
     * It prioritizes non-loopback, non-virtual, and site-local addresses.
     * @return The best-guess local LAN IP address, or "127.0.0.1" as a fallback.
     */
    public static String getLocalIpAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();

                // Ignore loopback, virtual, and disabled interfaces
                if (iface.isLoopback() || !iface.isUp() || iface.isVirtual()) {
                    continue;
                }

                String displayName = iface.getDisplayName().toLowerCase();
                // More filtering for common virtual/irrelevant interfaces
                if (displayName.contains("virtual") || displayName.contains("vmnet") || displayName.contains("docker")) {
                    continue;
                }

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();

                    // We are only interested in IPv4 addresses that are site-local
                    if (addr instanceof java.net.Inet4Address && addr.isSiteLocalAddress()) {
                        // Found a good candidate, return it.
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            Logger.getLogger(NetworkUtils.class).warn("Could not determine local IP address: " + e.getMessage());
        }

        return "127.0.0.1"; // Ultimate fallback
    }
} 