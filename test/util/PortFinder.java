package util;

import java.io.IOException;
import java.net.ServerSocket;

public class PortFinder {

    public static int findFreePort() {
        try (ServerSocket socket =  new ServerSocket(0)) {
            socket.setReuseAddress(true);
            return socket.getLocalPort();
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new IllegalStateException("Could not find a free TCP/IP port");
    }
}
