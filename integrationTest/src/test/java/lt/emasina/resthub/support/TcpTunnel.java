package lt.emasina.resthub.support;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

class RelayClient {

    private Socket sc;
    private Socket st;

    public RelayClient(Socket sc, Socket st) throws IOException {

        // relay the stuff thru
        new Relay(sc.getInputStream(), st.getOutputStream()).start();
        new Relay(st.getInputStream(), sc.getOutputStream()).start();

    }

    public void close() throws IOException {
        sc.close();
        st.close();
    }

}

@RequiredArgsConstructor
class Relay extends Thread {

    private final static int BUFSIZ = 1000;
    private final byte buf[] = new byte[BUFSIZ];

    private final InputStream in;
    private final OutputStream out;

    @Override
    public void run() {
        int n;
        try {
            while ((n = in.read(buf)) > 0) {
                out.write(buf, 0, n);
                out.flush();
            }
        } catch (IOException e) {
        } finally {
            try {
                in.close();
                out.close();
            } catch (IOException e) {
            }
        }
    }

}

@Log
@RequiredArgsConstructor
public class TcpTunnel {

    private final int listenport;
    private final String tunnelhost;
    private final int tunnelport;

    private ServerSocket ss;
    private final Set<RelayClient> clients = new HashSet<>();

    public void start() throws IOException {
        new Thread() {
            @Override
            public void run() {
                try {

                    System.out.println("TcpTunnel: ready to rock and roll on port " + listenport);
                    ss = new ServerSocket(listenport);
                    while (true) {

                        clients.add(new RelayClient(ss.accept(), new Socket(tunnelhost, tunnelport)));
                        System.out.println("TcpTunnel: tunnelling port " + listenport + " to port " + tunnelport + " on host " + tunnelhost);

                    }
                    
                } catch (IOException ex) {
                    log.log(Level.WARNING, "Error while opening Server Socket", ex);
                }
            }
        }.start();
    }

    public void stop() {

        try {
            ss.close();
        } catch (IOException | NullPointerException ex) { }
        ss = null;

        for (RelayClient c : clients) {
            try {
                c.close();
            } catch (IOException | NullPointerException ex) { }
        }

        clients.clear();

    }

    public static void main(String[] args) throws IOException {
        final TcpTunnel t = new TcpTunnel(8888, "localhost", 80);

        t.start();

        System.in.read();
        System.out.println("Stopping");

        t.stop();

        System.in.read();
        System.out.println("Starting");

        t.start();

        System.in.read();
        System.out.println("Stopping");

        t.stop();
        
    }

}
