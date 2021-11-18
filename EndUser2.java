import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Scanner;

public class EndUser2 extends Node{

    EndUser2(){
        try {
            socket = new DatagramSocket(Constants.ENDUSER_PORT);
            listener.go();
        } catch (
                Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            (new EndUser2()).start();
            System.out.println("Program terminated");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void start() throws Exception {
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("\n***CHOOSE ACTION***\n"
                    + "1. Send message\n2. Wait for message\n");
            System.out.print("Choose Action:");
            String command = sc.nextLine();

            if (command.contains("1"))
                sendMessage();
            else if (command.contains("2")) {
                System.out.println("Waiting....");
                this.wait();
            }
        }
    }

    public void sendMessage() {
        Scanner sc = new Scanner(System.in);

        // Get message from user
        System.out.print("Enter message: ");
        String message = sc.nextLine();

        // Make packetOut packet
        PacketManager messagePacket = new PacketManager();
        messagePacket.packetOut(Constants.E2, 1, message); // send to user2

        // send Packet
        sendPacket(messagePacket);
    }

    @Override
    public synchronized void onReceipt(DatagramPacket packet) throws IOException {
        this.notify();
        byte[] packetContent = packet.getData();

        if(packetContent[0] == Constants.PACKET_OUT)
            // Print message in packet
            System.out.println("EndUser1: " + new String(packetContent, Constants.HEADER_SIZE, packetContent.length-Constants.HEADER_SIZE).trim() + "\n");

    }
}
