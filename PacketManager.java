import java.net.DatagramPacket;
import java.net.InetSocketAddress;

public class PacketManager {
    private DatagramPacket packet;

    public void addHeader(byte[] packetContent, int packetType, int destination, int length) {
        packetContent[0] = (byte) packetType;
        packetContent[1] = (byte) destination;
        packetContent[2] = (byte) length;
    }

    public void addMessageContent(byte[] packetContent, String message) {
        byte[] messageContent = message.getBytes();
        for(int i = 0; i < message.length(); i++)
            packetContent[Constants.HEADER_SIZE + i] = messageContent[i];
    }

    // Create packetOut Packet
    public void packetOut(String destAddress, int nodeNumber, String message) {
        DatagramPacket packet;
        byte[] packetContent = new byte[message.length()+3];

        // add header
        addHeader(packetContent, Constants.PACKET_OUT, nodeNumber, packetContent.length);

        // add message
        addMessageContent(packetContent, message);

        // Set destination address
        InetSocketAddress dstAddress = new InetSocketAddress(destAddress, Constants.SERVICE_PORT);

        // Create the packet
        packet = new DatagramPacket(packetContent, packetContent.length, dstAddress);
        this.packet = packet;
    }

    // Create packetIn Packet
    public void packetIn(int userNo) {
        DatagramPacket packet;
        byte[] packetContent = new byte[3];

        // Add header
        addHeader(packetContent, Constants.PACKET_IN, userNo, packetContent.length);

        // Set destination address // Constants.C -> localhost
        InetSocketAddress dstAddress = new InetSocketAddress(Constants.C, Constants.CONTROLLER_PORT);

        //create the packet
        packet = new DatagramPacket(packetContent, packetContent.length, dstAddress);
        this.packet = packet;
    }

    // Create Ack packet
    public void ackPacket(InetSocketAddress address) {
        DatagramPacket packet;
        byte[] packetContent = new byte[3];

        // Add header
        addHeader(packetContent, Constants.ACK, 0, packetContent.length);

        // Set destination address
        InetSocketAddress dstAddress = address;

        // Create packet
        packet = new DatagramPacket(packetContent, packetContent.length, dstAddress);
        this.packet = packet;
    }

    // Create FlowMod packet
    public void flowMod(InetSocketAddress address, int userNo, String configTable[][]) {
        DatagramPacket packet;

        //processing config table so it can be sent to the switch
        String content = "";
        for(int i = 0; i < configTable.length; i++)
            if(configTable[i][2].equals(address.getAddress().getHostAddress()))
                content += configTable[i][0] + "," + configTable[i][3] + "," + configTable[i][4] + ",";

        //setup the array of bytes to be put in the packet
        byte[] packetContent = new byte[content.length() + 3];

        // Add header
        System.out.println(userNo);
        addHeader(packetContent, Constants.FLOW_MOD, userNo, packetContent.length);

        // Add processed table
        addMessageContent(packetContent, content);

        //get the address of the packet
        InetSocketAddress dstAddress = address;
        packet = new DatagramPacket(packetContent, packetContent.length, dstAddress);
        this.packet = packet;
    }

    // Create Hello packet
    public void helloPacket() {
        DatagramPacket packet;
        byte[] packetContent = new byte[3];

        // Add header
        addHeader(packetContent, Constants.HELLO, 0, packetContent.length);
        InetSocketAddress dstAddress = new InetSocketAddress(Constants.C, Constants.CONTROLLER_PORT);

        // Create packet
        packet = new DatagramPacket(packetContent, packetContent.length, dstAddress);
        this.packet = packet;
    }

    public DatagramPacket getPacket() {
        return this.packet;
    }
}
