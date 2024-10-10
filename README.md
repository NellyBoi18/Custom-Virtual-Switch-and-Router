# Custom-Virtual-Switch-and-Router
 
## Overview

Implements the forwarding behavior of a switch and a router.

## Environment Setup

A virtual machine (VM) with Mininet and POX installed is required for this project.


## Getting Started

You will be using Mininet, POX, and skeleton code for a simple router to complete the assignment. Mininet
and POX are already installed in the virtual machine (VM) you used for Assignment 1. You should continue
to use this VM for this project. You can always refer back to Part 2 of Assignment 1 if you have questions
about using your VM.

### Preparing Your Environment

Symlink POX and configure the POX modules:

```bash
cd ~/POX_ENV
ln -s ../pox
./config.sh
```

## Sample Configuration

The first sample configuration consists of a single switch (s1) and three emulated hosts (h1, h2, h3). The hosts
are each running an HTTP server. One host should be able
to fetch a web page from any other host (using wget or curl). Additionally, the hosts should be able to ping
each other. The topology the below image shows is defined in the configuration filetopos/singlesw.topo.

![single_sw](/Images/single_sw.png)

## Running the Virtual Switch

You’ll need to start 3 terminals as described below.

#### Step 1

Open a terminal and start Mininet emulation of a Ethernet network with a single switch by running the
following commands:

```bash
$cd ~/POX_ENV/
$sudo ./run_mininet.py topos/single_sw.topo -a
```

You should see output like the following:

```bash
*** Loading topology file topos/single_sw.topo
*** Writing IP file ./ip_config
*** Creating network
*** Adding controller
Unable to contact the remote controller at 127.0.0.1:
*** Adding hosts:
h1 h2 h
*** Adding switches:
s
*** Adding links:
(h1, s1) (h2, s1) (h3, s1)
*** Configuring hosts
h1 h2 h
*** Starting controller
*** Starting 1 switches
s
*** Configuring routing for h
*** Starting SimpleHTTPServer on host h
*** Configuring routing for h
*** Starting SimpleHTTPServer on host h
*** Configuring routing for h
*** Starting SimpleHTTPServer on host h
*** Writing ARP cache file ./arp_cache
*** Configuring ARP for h
*** Configuring ARP for h
*** Configuring ARP for h
*** Starting CLI:
mininet>
```

Exit the mininet emulation. We will restart this later in step 3. This is required to create some initial files
that are required in the next step.

#### Step 2

Open a terminal. Start the controller, by running the following commands:

```bash
cd ~/POX_ENV/
./run_pox.sh
```

You should see output like the following:

```bash
POX 0.5.0 (eel) / Copyright 2011-2018 James McCauley, et al.
INFO:.home.mininet.assign2.pox_module.cs640.ofhandler:Successfully loaded VNet config file
{’h3-eth0’: [’10.0.1.103’, ’255.255.255.0’], ’h1-eth0’: [’10.0.1.101’, ’255.255.255.0’], ’h2-eth
,→’: [’10.0.1.102’, ’255.255.255.0’]}

INFO:.home.mininet.assign2.pox_module.cs640.vnethandler:VNet server listening on 127.0.0.1:
INFO:core:POX 0.5.0 (eel) is up.
```

You must wait for Mininet to connect to the POX controller before you continue to the next step (Run step
3 now). Once Mininet has connected, you will see output like the following:

```bash
INFO:openflow.of_01:[00-00-00-00-00-01 2] connected
INFO:cs640.vnethandler:VNetHandler catch VNetDevInfo(ifaces={’eth3’: (None, None, None, 3), ’eth2’:
,→ (None, None, None, 2), ’eth1’: (None, None, None, 1)},swid=s1,dpid=1)

Keep POX running. (Don’t press ctrl-z.) If you don’t see this line being printed out, you may shutdown (i.e.
press ctrl-c) pox and mininet and start over. Note that POX is used “under the hood” in this assignment
to direct packets between Mininet and your virtual switch and virtual router instances (i.e., Java processes).
You do not need to understand, modify, or interact with POX in any way, besides executing therunpox.sh
script.
```

#### Step 3

Open another terminal, Start Mininet emulation of a Ethernet network with a single switch by running the
following commands:

```bash
$cd ~/POX_ENV/
$sudo ./run_mininet.py topos/single_sw.topo -a
```

Keep this terminal open, as you will need the mininet command line for debugging. (Don’t press ctrl-z.)

#### Step 4

Open a third terminal. Compile yourVirtualNetworkimplementation with ant, and startVirtualNetwork.jar
on the only switch in this topologys1, by running the following commands:

```bash
cd ~/POX_ENV/
ant
java -jar VirtualNetwork.jar -v s
```

You should see output like the following:

```bash
Connecting to server localhost:
Device interfaces:
eth
eth
eth
<-- Ready to process packets -->
```

Go back to the terminal where Mininet is running. To issue a command on an emulated host, type the
hostname followed by the command in the Mininet console. Only the host on which to run the command
should be specified by name; any arguments for the command should use IP addresses. For example, the
following command sends 2 ping packets from h1 to h2:

```bash
mininet> h1 ping -c 2 10.0.1.
```

The pings will fail because the virtual switch is not fully implemented. However, in the terminal where your
virtual switch is running, you should see the following output:

```bash
*** -> Received packet:
ip
dl_vlan: untagged
dl_vlan_pcp: 0
dl_src: 00:00:00:00:00:
dl_dst: 00:00:00:00:00:
nw_src: 10.0.1.
nw_dst: 10.0.1.
nw_tos: 0
nw_proto: 1
icmp_type: 8


icmp_code: 0
*** -> Received packet:
ip
dl_vlan: untagged
dl_vlan_pcp: 0
dl_src: 00:00:00:00:00:
dl_dst: 00:00:00:00:00:
nw_src: 10.0.1.
nw_dst: 10.0.1.
nw_tos: 0
nw_proto: 1
icmp_type: 8
icmp_code: 0

```
You can stop your virtual switch by pressing ctrl-c in the terminal where it’s running. You can restart
the simple router without restarting POX and mininet, but it’s often useful to restart POX and mininet to
ensure the emulated network starts in a clean state.

## Code Overview

The virtual network code consists of the following important packages and classes:

- edu.wisc.cs.sdn.vnet
    - The main method (Main)
    - Classes representing a network device and interfaces (Device,Iface)
    - Code for creating a PCAP file containing all packets sent/received by a network device (DumpFile)
- edu.wisc.cs.sdn.vnet.sw
    - Virtual switch (Switch)
- edu.wisc.cs.sdn.vnet.rt
    - Virtual router (Router)
    - ARP cache (ArpCache,ArpEntry)
    - Route table (RouteTable,RouteEntry)
- net.floodlightcontroller.packet

- edu.wisc.cs.sdn.vnet.vns- code to communicate with POX
- org.openflow.util- code for manipulating special types

When the virtual switch or router receives a packet, thehandlePacket()function in theSwitchorRouter
class is called. When it wants to send a packet, call thesendPacket()function in theDeviceclass (which
is a superclass of theSwitchandRouterclasses).

## 2. Virtual Switch

A learning switch forwards packets at the link layer
based on destination MAC addresses.


### Forwarding Packets

The thehandlePacket(...) method in the edu.wisc.cs.sdn.vnet.sw.Switch class
sends a received packet out the appropriate interface(s)of the switch.

The sendPacket(...) function inherited from the edu.wisc.cs.sdn.vnet.Device class
sends a packet out a specific interface. To broadcast/flood a packet, this method is called multiple times
with a different interface specified each time. The interfaces variable inherited from the Deviceclass
contains all interfaces on the switch. The interfaces on a switch only have names; they do not have MAC
addresses, IP addresses, or subnet masks.

## Testing

You can test the learning switch by following the directions from Part 1. You can use any of the following
topologies (in the /POX_ENV/toposdirectory):

- singlesw.topo
- linear5sw.topo
- inclasssw.topo

If you are testing on a topology with multiple switches, you should launch your VirtualNetwork.jar on
ALL of the switches (java -jar VirtualNetwork.jar -v s X wheres X is a switch in your topology). You
may test your implementation with any software that initiates a network transfer (e.g. your Iperfer from
assignment 1). For your convenience, each virtual host has a simple web server running on them. To
initiate a connection to the provided web servers, use hX curl hYto connect to hY from hX where hX and
hY are different hosts on your mininet network. You should see something like this if the switch
is forwarding packets correctly:

```html
<html>
<head><title>Test Page</title></head>
<body>
Congratuations! <br/>
Your router successfully route your packets. <br/>
</body>
</html>
```

## 3. Virtual Router

For simplicity, the router will use a statically provided route table and a statically provided ARP cache.
Furthermore, when the router encounters an error (e.g., no matching route entry), it will silently drop a
packet, rather than sending an ICMP packet with the appropriate error message.

### Route Lookups

Given an IP address, thelookup(...) function should return the RouteEntry object that has the longest prefix
match with the given IP address. If no entry matches, then the function should return null.

### Checking Packets

The handlePacket(...) method in theedu.wisc.cs.sdn.vnet.rt.Router
class should update and send a received packet out the appropriate interface of the router.

When an Ethernet frame is received, it should first check if it contains an IPv4 packet. The
getEtherType() method in the net.floodlightcontroller.packet.Ethernet class determines the type
of packet contained in the payload of the Ethernet frame. If the packet is not IPv4, you do not need to do
any further processing - i.e., your router should drop the packet.

If the frame contains an IPv4 packet, then it verifies the checksum and TTL of the IPv4 packet. It
uses the getPayload()method of the Ethernet class to get the IPv4 header and cast the result
to net.floodlightcontroller.packet.IPv4.

The IP checksum should only be computed over the IP header. The length of the IP header can be determined
from the header length field in the IP header, which specifies the length of the IP header in 4-byte words (i.e.,
multiple the header length field by 4 to get the length of the IP header in bytes). The checksum field in the
IP header should be zeroed before calculating the IP checksum. If the checksum is incorrect, then the router drops the packet.

After verifying the checksum, it decrements the IPv4 packet’s TTL by 1. If the resulting TTL is 0,
then the router drops the packet.

### Forwarding Packet

IPv4 packets with a correct checksum, TTL>1 (pre decrement), and a destination other than one of
the router’s interfaces should be forwarded. You should use thelookup(...)method in theRouteTable
class, which was implemented earlier, to obtain the RouteEntrythat has the longest prefix match with the
destination IP address. If no entry matches, then the
router drops the packet.

If an entry matches, then it determines the next-hop IP address and lookup the MAC address corre-
sponding to that IP address. It calls thelookup(...) method in the edu.wisc.cs.sdn.vnet.rt.ArpCache
class to obtain the MAC address from the statically populated ARP cache. This address should be the new
destination MAC address for the Ethernet frame. The MAC address of the outgoing interface should be the
new source MAC address for the Ethernet frame.

After it has correctly updated the Ethernet header, it calls the sendPacket(...) function
inherited from the edu.wisc.cs.sdn.vnet.Device class to send the frame out the correct interface.

## Testing

You can test the learning switch by following the directions from Part 1. However, when starting your
virtual router, you must include the appropriate static route table and static ARP cache as arguments. For
example:

```bash
java -jar VirtualNetwork.jar -v r1 -r rtable.r1 -a arp_cache
```

Make sure you already started mininet before hand so that it could generate the routing tablesrtable.rX
and ARP cachearpcachefor you.

You can use any of the following topologies (in the /POX_ENV/toposdirectory) to test your router:

- singlert.topo
- pairrt.topo
- trianglert.topo
- linear5rt.topo

To test the switch and router implementations together, use any of the following topologies:

- singleeach.topo
- trianglewithsw.topo

You can also create your own topologies based on these examples. As is in Part 2, you’ll want to launch
VirtualNetwork.jar on ALL of the routers AND switches in the topology you are testing. Be sure
you use the correct routing table (rtable.rX) on the correct virtual routerrX.

#### Acknowledgements

This programming assignment borrows from the Simple Router assignment from Stanford CS144: An In-
troduction to Computer Networks and Rodrigo Fonseca’s IP Project from Brown University CSCI-1680:
Computer Networks.