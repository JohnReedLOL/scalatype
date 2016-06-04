package info.collaboration_station.utilities;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author johnmichaelreed2
 */
public class Network {

    /**
     * Returns MAC address of the given interface name.
     *
     * @param interfaceName eth0, wlan0 or NULL=use first interface
     * @return mac address or empty string
     */
    public static String getMACAddress(String interfaceName) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                if (interfaceName != null) {
                    if (!intf.getName().equalsIgnoreCase(interfaceName)) {
                        continue;
                    }
                }
                byte[] mac = intf.getHardwareAddress();
                if (mac == null) {
                    Tester.killApplication("Didn't work");
                    return "";
                }
                StringBuilder buf = new StringBuilder();
                for (int idx = 0; idx < mac.length; idx++) {
                    buf.append(String.format("%02X:", mac[idx]));
                }
                if (buf.length() > 0) {
                    buf.deleteCharAt(buf.length() - 1);
                }
                return buf.toString();
            }
        } catch (Exception ex) {
            Tester.killApplication("That didn't work");
        } // for now eat exceptions
        return "";
        /*try {
         // this is so Linux hack
         return loadFileAsString("/sys/class/net/" +interfaceName + "/address").toUpperCase().trim();
         } catch (IOException ex) {
         return null;
         }*/
    }

    public static byte[] getMACAddressBytes(String interfaceName) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                if (interfaceName != null) {
                    if (!intf.getName().equalsIgnoreCase(interfaceName)) {
                        continue;
                    }
                }
                byte[] mac = intf.getHardwareAddress();
                if (mac == null) {
                    Tester.killApplication("Didn't work");
                    return null;
                }
                return mac;
            }
        } catch (Exception ex) {
            Tester.killApplication("That didn't work");
        } // for now eat exceptions
        return null;
    }
    
    /**
     * Gets all viable, reachable, non-loopback inet addresses on this machine.
     * Useful for cases in which multiple inet addresses must be tried or backups
     * are needed.
     * @return
     * @throws Exception if something goes wrong.
     */
    public static InetAddress[] getAllReachableViableNonLoopBackInetAddresses() throws Exception {
        
        // start with an empty list of InetAddresses.
        final ArrayList<InetAddress> ipAddresses = new ArrayList<>();

        // Go through the network interfaces.
        List<NetworkInterface> interfaces = null;
        try {
            interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
        } catch (SocketException se) {
            Printer.printException("Failed to get network interfaces.", se);
            Tester.check(ipAddresses.isEmpty());
            ipAddresses.trimToSize();
            InetAddress[] inetAddressArr = new InetAddress[ipAddresses.size()];
            inetAddressArr = ipAddresses.toArray(inetAddressArr);
            return inetAddressArr;
        }
        for (final NetworkInterface intf : interfaces) {
            try {
                if (intf.isLoopback() || !intf.isUp()) {
                    // we are not interested in loopback addresses or non-functional interfaces.
                    continue; // keep going.
                }
            } catch (SocketException se) {
                Printer.printException("Failed to access network interface " + intf.getDisplayName(), se);
                continue; // keep going.
            }
            final List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
            // First get all the site local addresses
            for (InetAddress addr : addrs) {
                if (addr == null || addr.isLoopbackAddress()) {
                    continue; // no null addresses or loopback addresses, keep going.
                } else {
                    Tester.check(addr.getHostAddress().toString().contains(":")
                        || addr.getHostAddress().toString().contains("."), 
                            "ipv6 addresses contain colons, ipv4 addresses contain periods.");
                            boolean isReachable = false;
                            try {
                                isReachable = addr.isReachable(50);
                            } catch (IOException ioe) {
                                // I couldn't reach this address.
                                continue; // keep going.
                            }
                            if (isReachable) {
                                ipAddresses.add(addr);
                            }
                }
            }
        }
        ipAddresses.trimToSize();
        InetAddress[] toReturn = new InetAddress[ipAddresses.size()];
        toReturn = ipAddresses.toArray(toReturn);
        return toReturn;
    }

    /**
     * Gets the default, viable ipv4 or ipv6 address from the OS.
     *
     * If the default local ip address is invalid or unavailable, gets all the
     * viable site local ipv4 addresses as well as all link local ipv4 addresses
     * and ipv6 addresses. Written under the assumption that only one link local
     * ipv4 or link local ipv6 address can exist for a machine.
     *
     * @return the viable potential local ip addresses. May be one or more than
     * one depending on the OS and the environment.
     * @deprecated 
     */
    private static InetAddress[] getPotentiallyViableNonLoopbackIpAddresses() throws IOException {

        final ArrayList<InetAddress> ipAddresses = new ArrayList<>();
        // First see if the default getLocalHost works.
        /*{
            InetAddress defaultAddressOrNull = null;
            try {
                defaultAddressOrNull = InetAddress.getLocalHost();
                if (defaultAddressOrNull != null) {
                    if (defaultAddressOrNull.isSiteLocalAddress()) {
                        boolean isReachable = false;
                        try {
                            isReachable = defaultAddressOrNull.isReachable(50);
                        } catch (IOException ioe) {
                            // I couldn't reach this address. Keep going.
                        }
                        if (isReachable) {
                            ipAddresses.add(defaultAddressOrNull);
                            InetAddress[] inetAddressArr = new InetAddress[ipAddresses.size()];
                            inetAddressArr = ipAddresses.toArray(inetAddressArr);
                            Tester.check(inetAddressArr.length == ipAddresses.size(), "Failed to transfer addresses");
                            Tester.check(inetAddressArr.length == 1, "I only inserted one element.");
                            return inetAddressArr;
                        }
                    }
                }
            } catch (UnknownHostException uhe) {
                Printer.printException(uhe, "Failed to get local host.");
            }
        }*/

        // If it doesn't work, go through the interfaces.
        List<NetworkInterface> interfaces = null;
        try {
            interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
        } catch (SocketException se) {
            Printer.printException("Failed to get network interfaces.", se);
            Tester.check(ipAddresses.isEmpty());
            InetAddress[] inetAddressArr = new InetAddress[ipAddresses.size()];
            inetAddressArr = ipAddresses.toArray(inetAddressArr);
            return inetAddressArr;
        }
        for (final NetworkInterface intf : interfaces) {
            try {
                if (intf.isLoopback() || !intf.isUp()) {
                    // we are not interested in loopback addresses or non-functional interfaces.
                    continue; // keep going.
                }
            } catch (SocketException se) {
                Printer.printException("Failed to access network interface " + intf.getDisplayName(), se);
                continue; // keep going.
            }
            final List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
            // First get all the site local addresses
            for (InetAddress addr : addrs) {
                if (addr == null || addr.isLoopbackAddress()) {
                    continue; // no null addresses or loopback addresses, keep going.
                } else {
                    Tester.check(addr.getHostAddress().toString().contains(":")
                        || addr.getHostAddress().toString().contains("."), 
                            "ipv6 addresses contain colons, ipv4 addresses contain periods.");
                    // if address is ipv4. Fuck it take the non-ipv4 addresses too.
                    // if (!addr.getHostAddress().toString().contains(":")) { 
                        //if (addr.isSiteLocalAddress()) {
                            // Fuck it. Take the non-site locals too just in case local ip has a non-standard prefix
                            boolean isReachable = false;
                            try {
                                isReachable = addr.isReachable(50);
                            } catch (IOException ioe) {
                                // I couldn't reach this address.
                                continue; // keep going.
                            }
                            if (isReachable) {
                                ipAddresses.add(addr);
                            }
                        //}
                    // }
                }
            }
            // Add link local addresses as a backup [last resort].
            /*
            for (InetAddress addr : addrs) {
                if (addr == null) {
                    continue; // no null addresses, keep going.
                }
                // We don't care if the link local address is ipv4 or ipv6
                if (addr.isLinkLocalAddress()) {
                    // link local addresses may work if site local addresses are not availiable.
                    boolean isReachableLinkLocal = false;
                    try {
                        isReachableLinkLocal = addr.isReachable(50);
                    } catch (IOException ioe) {
                        // I couldn't reach this address. Keep going.
                        continue;
                    }
                    if (isReachableLinkLocal) {
                        ipAddresses.add(addr);
                        // As many link local addresses as we want. 
                        // Note that link local ipv4 addresses will only be used if non-link local ones are not availiable and no link local ipv6 are availiable either.
                        // Link local ipv6 addresses should be used if no site local ipv4 addresses are availiable.
                        //break; // only 1 link-local address maximum.
                    }
                }
            }*/
        }
        InetAddress[] toReturn = new InetAddress[ipAddresses.size()];
        toReturn = ipAddresses.toArray(toReturn);
        return toReturn;
    }

    private static void checkIfNumLinkLocalAddressesIsReasonable(final InetAddress[] inetAddresses) {
        int numSiteLocalIpv6Addresses = 0;
        int numSiteLocalIpv4Addresses = 0;
        for (InetAddress addr : inetAddresses) {
            Tester.check(!(addr == null), "No null addresses");
            if (addr.getHostAddress().toString().contains(":")) {
                if (addr.isLinkLocalAddress()) {
                    ++numSiteLocalIpv6Addresses;
                }
            } else if (addr.getHostAddress().toString().contains(".")) {
                if (addr.isLinkLocalAddress()) {
                    ++numSiteLocalIpv4Addresses;
                }
            } else {
                Tester.killApplication("All ip addresses must contain either a colon or a dot.");
            }
        }
        Tester.check(numSiteLocalIpv4Addresses == 0 || numSiteLocalIpv4Addresses == 1, "Too many link local ipv4 addresses");
        Tester.check(numSiteLocalIpv6Addresses == 0 || numSiteLocalIpv6Addresses == 1, "Too many link local ipv6 addresses");
    }
}
