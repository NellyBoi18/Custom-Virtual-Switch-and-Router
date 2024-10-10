package edu.wisc.cs.sdn.vnet.sw;

import net.floodlightcontroller.packet.Ethernet;
import edu.wisc.cs.sdn.vnet.Device;
import edu.wisc.cs.sdn.vnet.DumpFile;
import edu.wisc.cs.sdn.vnet.Iface;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Switch extends Device {
    private Map<String, IfaceEntry> macTable;

    private class IfaceEntry {
        public Iface iface;
        public long timeStamp;

        public IfaceEntry(Iface iface, long timeStamp) {
            this.iface = iface;
            this.timeStamp = timeStamp;
        }
    }

    public Switch(String host, DumpFile logfile) {
        super(host, logfile);
        this.macTable = new HashMap<>();
    }

    @Override
    public void handlePacket(Ethernet etherPacket, Iface inIface) {
        System.out.println("*** -> Received packet: " +
                etherPacket.toString().replace("\n", "\n\t"));

        String srcMac = etherPacket.getSourceMAC().toString();
        String dstMac = etherPacket.getDestinationMAC().toString();

        // Learn the source MAC address
        macTable.put(srcMac, new IfaceEntry(inIface, System.currentTimeMillis()));

        // Remove stale entries
        long currentTime = System.currentTimeMillis();
        Iterator<Map.Entry<String, IfaceEntry>> it = macTable.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, IfaceEntry> entry = it.next();
            if (currentTime - entry.getValue().timeStamp > 15000) { // 15 seconds
                it.remove();
            }
        }

        // Forward or broadcast the packet
        if (macTable.containsKey(dstMac)) {
            sendPacket(etherPacket, macTable.get(dstMac).iface);
        } else {
            for (Iface iface : interfaces.values()) {
                if (!iface.equals(inIface)) {
                    sendPacket(etherPacket, iface);
                }
            }
        }
    }
}
