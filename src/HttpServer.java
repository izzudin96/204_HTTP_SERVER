import java.net.*;
import java.io.*;
import java.util.*;

class HttpServerSession extends Thread
{
    private HttpServer server;
    private Socket client;
    private PrintWriter writer;

    public HttpServerSession(HttpServer a, Socket b) {
        server = a;
        client = b;
        writer = null;
    }

    public void run() {
        try {
            writer = new PrintWriter(client.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));

            System.out.println("Connection received from: " + client.getRemoteSocketAddress());
            while(client.isConnected()) {
                String line = reader.readLine();
//                System.out.println(line);
                String parts[] = line.split(" ");
                if(parts.length == 3) {
                    System.out.println("First Line: " + parts[1].substring(1));
                }
            }
        } catch (Exception e) {
            System.err.println("Exception: " + e);
        }

        server.logout(this);
    }
}

class HttpServer
{
    private ArrayList<HttpServerSession> sessions;

    public static void main(String args[]) {
        HttpServer server = new HttpServer();
        server.start_server();
    }

    public void start_server() {
        sessions = new ArrayList<HttpServerSession>();

        try {
            ServerSocket server = new ServerSocket(8080);
            System.out.println("Starting server on port: " + server.getLocalPort());
            while(true) {
                Socket client = server.accept();
                HttpServerSession thread = new HttpServerSession(this, client);
                synchronized (sessions) {
                    sessions.add(thread);
                }
                thread.start();
            }
        } catch (Exception e) {
            System.err.println("Exception: " + e);
        }
    }

    public void logout(HttpServerSession session)
    {
        int i, len;
        synchronized(sessions) {
            len = sessions.size();
            for(i=0; i<len; i++) {
                HttpServerSession s = sessions.get(i);
                if(s == session) {
                    sessions.remove(i);
                    break;
                }
            }
        }
    }
}