import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

public class Controller extends Node {

    private static final String[][] preconfigTable = {
            //{     DESTINATION   		           SOURCE 	                    SWITCH  			        IN 				            OUT  }
            //--------------------------------------------------------------------------------------------------------------------------------------
            {   Constants.e2Address,        Constants.e1Address,        Constants.e1Address,        Constants.e1Address,      Constants.r1Net1Address},
            {   Constants.e2Address,        Constants.e1Address,        Constants.r1Net1Address,    Constants.e1Address,      Constants.r2Net2Address},
            {   Constants.e2Address,        Constants.e1Address,        Constants.r2Net2Address,    Constants.r1Net2Address,  Constants.e2Address},
            {   Constants.e2Address,        Constants.e1Address,        Constants.e2Address,        Constants.r2Net3Address,  Constants.e2Address},
            //--------------------------------------------------------------------------------------------------------------------------------------
            {   Constants.e1Address,        Constants.e2Address,        Constants.e2Address,        Constants.e2Address,      Constants.r2Net3Address},
            {   Constants.e1Address,        Constants.e2Address,        Constants.r2Net2Address,    Constants.e2Address,      Constants.r1Net2Address},
            {   Constants.e1Address,        Constants.e2Address,        Constants.r1Net1Address,    Constants.r2Net2Address,  Constants.e1Address},
            {   Constants.e1Address,        Constants.e2Address,        Constants.e1Address,        Constants.r1Net1Address,  Constants.e1Address},
            //--------------------------------------------------------------------------------------------------------------------------------------
    };


    Controller() {
        try {
            socket = new DatagramSocket(Constants.CONTROLLER_PORT);
            listener.go();
        } catch (

                Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            System.out.println("  DEST            SRC           SWITCH           IN             OUT");
            for(int i = 0; i < preconfigTable.length; i++) {
                for (int j = 0; j < preconfigTable[0].length; j++)
                    System.out.print(preconfigTable[i][j] + "    ");
                System.out.println("\n");
            }
            (new Controller()).start();
            System.out.println("Program terminated");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void start() throws Exception {
        while (true) {
            System.out.println("Waiting....");
            this.wait();
        }
    }

    @Override
    public synchronized void onReceipt(DatagramPacket packet) throws IOException {
        byte[] packetContent = packet.getData();
        if(packetContent[0] == Constants.HELLO) {
            System.out.println("Received helloPacket from switch: " + packet.getAddress().getHostName());

            // Send ack
            PacketManager response = new PacketManager();
            response.ackPacket(new InetSocketAddress(packet.getAddress(), packet.getPort()));
            sendPacket(response);
        }
        else if(packetContent[0] == Constants.ACK){
            System.out.println("Received ACK!");
        }
        else if (packetContent[0] == Constants.PACKET_IN) {
            System.out.println("Received packetIn from switch: " + packet.getAddress().getHostName());

            // Create FlowMod Packet
            PacketManager flowMod = new PacketManager();
            flowMod.flowMod(new InetSocketAddress(packet.getAddress(), packet.getPort()), packetContent[1], this.preconfigTable);

            // Send flowMod Packet
            sendPacket(flowMod);
            System.out.println("Sending flowMod to switch: " + packet.getAddress().getHostName());
        }
    }
}

