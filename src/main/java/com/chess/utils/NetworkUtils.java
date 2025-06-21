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

    /**
     * Attempts to find the local, non-loopback IP address of this machine.
     * This method is designed to be robust, especially for WSL environments.
     * @return The local IP address as a String, or "127.0.0.1" as a fallback.
     */
    public static String getLocalIpAddress() {
        // Attempt to get IP from ipconfig (for WSL/Windows)
        try {
            Process process = Runtime.getRuntime().exec("ipconfig.exe");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("IPv4 Address")) {
                    return line.split(":")[1].trim();
                }
            }
        } catch (IOException e) {
            // This will fail on non-Windows systems, which is expected.
            // We'll fall through to the pure Java method.
            Logger.getLogger(NetworkUtils.class).info("Could not run ipconfig.exe, falling back to Java method.");
        }

        // Fallback to pure Java method
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                // filters out 127.0.0.1 and inactive interfaces
                if (iface.isLoopback() || !iface.isUp()) {
                    continue;
                }

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    // Filters out IPv6 addresses, returns IPv4
                    if (addr instanceof java.net.Inet6Address) {
                        continue;
                    }
                    return addr.getHostAddress();
                }
            }
        } catch (SocketException e) {
            Logger.getLogger(NetworkUtils.class).warn("Could not determine local IP address: " + e.getMessage());
        }

        return "127.0.0.1"; // Ultimate fallback
    }
} 