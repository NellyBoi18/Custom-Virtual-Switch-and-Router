package edu.wisc.cs.sdn.vnet.rt;

import edu.wisc.cs.sdn.vnet.Device;
import edu.wisc.cs.sdn.vnet.DumpFile;
import edu.wisc.cs.sdn.vnet.Iface;
import net.floodlightcontroller.packet.IPv4;

import net.floodlightcontroller.packet.Ethernet;

/**
 * @author Aaron Gember-Jacobson and Anubhavnidhi Abhashkumar
 */
public class Router extends Device
{	
	/** Routing table for the router */
	private RouteTable routeTable;
	
	/** ARP cache for the router */
	private ArpCache arpCache;
	
	/**
	 * Creates a router for a specific host.
	 * @param host hostname for the router
	 */
	public Router(String host, DumpFile logfile)
	{
		super(host,logfile);
		this.routeTable = new RouteTable();
		this.arpCache = new ArpCache();
	}
	
	/**
	 * @return routing table for the router
	 */
	public RouteTable getRouteTable()
	{ return this.routeTable; }
	
	/**
	 * Load a new routing table from a file.
	 * @param routeTableFile the name of the file containing the routing table
	 */
	public void loadRouteTable(String routeTableFile)
	{
		if (!routeTable.load(routeTableFile, this))
		{
			System.err.println("Error setting up routing table from file "
					+ routeTableFile);
			System.exit(1);
		}
		
		System.out.println("Loaded static route table");
		System.out.println("-------------------------------------------------");
		System.out.print(this.routeTable.toString());
		System.out.println("-------------------------------------------------");
	}
	
	/**
	 * Load a new ARP cache from a file.
	 * @param arpCacheFile the name of the file containing the ARP cache
	 */
	public void loadArpCache(String arpCacheFile)
	{
		if (!arpCache.load(arpCacheFile))
		{
			System.err.println("Error setting up ARP cache from file "
					+ arpCacheFile);
			System.exit(1);
		}
		
		System.out.println("Loaded static ARP cache");
		System.out.println("----------------------------------");
		System.out.print(this.arpCache.toString());
		System.out.println("----------------------------------");
	}

	/**
	 * Handle an Ethernet packet received on a specific interface.
	 * @param etherPacket the Ethernet packet that was received
	 * @param inIface the interface on which the packet was received
	 */
        @Override
        public void handlePacket(Ethernet etherPacket, Iface inIface) {
            if (etherPacket.getEtherType() != Ethernet.TYPE_IPv4) {
                return; 
            }

            IPv4 ipPacket = (IPv4) etherPacket.getPayload();

            short originalChecksum = ipPacket.getChecksum();
            ipPacket.resetChecksum();
            byte[] serializedIP = ipPacket.serialize();
            ipPacket.deserialize(serializedIP, 0, serializedIP.length);
            if (originalChecksum != ipPacket.getChecksum()) {
                return; // Drop the packet if checksum verification fails
            }

            if (0 == (ipPacket.getTtl() - 1)) {
                return; // Drop the packet if TTL is 0
            }
            ipPacket.setTtl((byte) (ipPacket.getTtl() - 1));

            ipPacket.resetChecksum();
            serializedIP = ipPacket.serialize();
            ipPacket.deserialize(serializedIP, 0, serializedIP.length);

            int destIpAddress = ipPacket.getDestinationAddress();
            RouteEntry bestMatch = this.routeTable.lookup(destIpAddress);
            if (bestMatch == null) {
                return; 
            }

            int nextHopIp = bestMatch.getGatewayAddress();
            if (0 == nextHopIp) {
                nextHopIp = destIpAddress;
            }

            ArpEntry arpEntry = this.arpCache.lookup(nextHopIp);
            if (arpEntry == null) {
                return; 
            }

            etherPacket.setSourceMACAddress(bestMatch.getInterface().getMacAddress().toBytes());
            etherPacket.setDestinationMACAddress(arpEntry.getMac().toBytes());

            this.sendPacket(etherPacket, bestMatch.getInterface());
        }


}
