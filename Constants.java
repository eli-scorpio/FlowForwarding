import java.net.InetSocketAddress;

/**
 * @Author: Eligijus Skersonas
 *
 * @Description: A static class of constants for project members to access
 */
public class Constants {

    // Switch Constants
    static final int SERVICE_PORT = 51510;
    static final String E1 = "e1";
    static final String R1 = "r1";
    static final String R2 = "r2";
    static final String E2 = "e2";
    static final String e1Address = "172.20.11.4";
    static final String r1Net1Address = "172.20.11.3";
    static final String r1Net2Address = "172.20.33.4";
    static final String r2Net2Address = "172.20.33.3";
    static final String r2Net3Address = "172.20.66.3";
    static final String e2Address = "172.20.66.4";

    // Controller Constants
    static final String C = "con";
    static final int CONTROLLER_PORT = 49999;

    // EndUser Constants
    static final int ENDUSER_PORT = 50000;

    // Types of Packets Constants
    static final int HELLO = 0;
    static final int PACKET_IN = 1;
    static final int PACKET_OUT = 2;
    static final int FLOW_MOD = 3;
    static final int ACK = 4;

    // Header Constants
    static final int HEADER_SIZE = 3;
}