import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

public class Switch extends Node {
    private String[][] flowTable;
    private DatagramPacket buffer;

    Switch() {
        try {
            socket = new DatagramSocket(Constants.SERVICE_PORT);
            listener.go();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            (new Switch()).start();
            System.out.println("Program terminated");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void start() throws Exception {
        // Send helloPacket to Controller
        PacketManager helloPacket = new PacketManager();
        helloPacket.helloPacket();
        sendPacket(helloPacket);

        // Wait for packets
        while (true) {
            System.out.println("Waiting....");
            this.wait();
        }
    }

    public void processFlowTable(byte[] packetData) {
        byte[] packetContent = new byte[packetData.length-3];

        // Extract packet content into byte Array
       // for(int i = 0; i < packetContent.length; i++)
       //     packetContent[i] = packetData[i+Constants.HEADER_SIZE];

        // Convert Byte Array to String
        String flowTableString = new String(packetData, Constants.HEADER_SIZE, packetData.length-Constants.HEADER_SIZE).trim();

        // Split String into String array (Pattern: ",")
        String[] flowTableElements = flowTableString.split(",");

        // Construct flow table
        flowTable = new String[flowTableElements.length/3][3];
        int row = 0;
        for(int i = 0; i < flowTableElements.length; i++) {
            if(i != 0 && i%3 == 0)
                row++;
            flowTable[row][i%3] = flowTableElements[i];
        }

    }

    public void forwardPacket() throws IOException{
        byte[] packetContent = this.buffer.getData();
        boolean found = false;

        // Compute Destination
        String dest = (packetContent[1] == 1)? Constants.e1Address:
                        (packetContent[1] == 2)? Constants.e2Address: "";

        // Set Socket Address by looking at table
        for(int i = 0; i < this.flowTable.length && !found; i++) {
            if(dest.equals(flowTable[i][0]) && this.buffer.getAddress().getHostAddress().equals(flowTable[i][1])) {
                this.buffer.setSocketAddress(new InetSocketAddress(flowTable[i][2], Constants.SERVICE_PORT));
                found = true;
            }
        }

        // Forward buffer and clear
        socket.send(this.buffer);
        System.out.println("Forwarded Buffer to next hop - " + buffer.getAddress().getHostName());
        this.buffer = null;
    }

    @Override
    public synchronized void onReceipt(DatagramPacket packet) throws IOException {
        byte[] packetContent = packet.getData();

        if(packetContent[0] == Constants.ACK)
            System.out.println("Received ACK!");     // helloPacket has been acknowledged by the controller
        else if (packetContent[0] == Constants.PACKET_OUT) {
            System.out.println("Received PacketOut from - " + packet.getAddress().getHostName());

            // packet to buffer
            if(buffer == null)
                buffer = packet;

            // If buffer is received through localhost then the local endUser is the source
            // then if destination is enduser1 forward to r2 if destination is enduser2 forward to r1
            if(buffer.getAddress().getHostName().equals("localhost")){
                if(packetContent[1] == 1)
                    buffer.setSocketAddress(new InetSocketAddress(Constants.r2Net3Address, Constants.SERVICE_PORT));
                else if(packetContent[1] == 2)
                    buffer.setSocketAddress(new InetSocketAddress(Constants.r1Net1Address, Constants.SERVICE_PORT));

                // Forward buffer and clear
                socket.send(buffer);
                System.out.println("Forwarded Buffer to next hop - " + buffer.getAddress().getHostName());
                this.buffer = null;
            }
            // If buffer is received from e2 and destination is enduser2 then send to enduser on local host
            else if(buffer.getAddress().getHostAddress().equals(Constants.e2Address) && packetContent[1] == 2) {
                // Set destination
                buffer.setSocketAddress(new InetSocketAddress("localhost", Constants.ENDUSER_PORT));

                // Forward Buffer and clear
                socket.send(buffer);
                System.out.println("Packet sent to EndUser2");
                this.buffer = null;
            }
            // If buffer is received from e1 and destination is enduser1 then send to enduser on local host
            else if(buffer.getAddress().getHostAddress().equals(Constants.e1Address) && packetContent[1] == 1) {
                // Set destination
                buffer.setSocketAddress(new InetSocketAddress("localhost", Constants.ENDUSER_PORT));

                // Forward Buffer and clear
                socket.send(buffer);
                System.out.println("Packet sent to EndUser1");
                this.buffer = null;
            }
            else {
                // Convert to packetIn
                PacketManager packetIn = new PacketManager();
                packetIn.packetIn(packetContent[1]);

                // Send packet to Controller
                sendPacket(packetIn);
                System.out.println("Forwarded PacketIn to Controller");
            }
        }
        else if (packetContent[0] == Constants.FLOW_MOD) {
            System.out.println("Received flowMod from Controller");

            // Send ack
            PacketManager response = new PacketManager();
            response.ackPacket(new InetSocketAddress(packet.getAddress(), packet.getPort()));
            sendPacket(response);

            // Process Table
            processFlowTable(packetContent);

            // Forward packet and clear buffer
            forwardPacket();
        }
    }
}