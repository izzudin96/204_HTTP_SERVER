import java.net.*;
import java.io.*;
import java.nio.Buffer;
import java.util.*;

class HttpServer
{
    private ArrayList<HttpServerSession> sessions;

    public static void main(String args[]) {
        HttpServer server = new HttpServer();
        System.out.println("Server is starting...");
        server.start_server();
    }

    public void start_server() {
        sessions = new ArrayList<HttpServerSession>();

        try {
            ServerSocket server = new ServerSocket(8080);
            System.out.println("Starting server on port: " + server.getLocalPort());
            while(true) {
                Socket client = server.accept();
                System.out.println("\nConnection received from: " + client.getLocalAddress());
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

//    public void logout(HttpServerSession session)
//    {
//        int i, len;
//        synchronized(sessions) {
//            len = sessions.size();
//            for(i=0; i<len; i++) {
//                HttpServerSession s = sessions.get(i);
//                if(s == session) {
//                    sessions.remove(i);
//                    break;
//                }
//            }
//        }
//    }
}

class HttpServerSession extends Thread
{
    private HttpServer server;
    private Socket client;
    private PrintWriter writer;
    private String filename;

    public HttpServerSession(HttpServer a, Socket b) {
        server = a;
        client = b;
        writer = null;
    }

    public void run() {
        try {
            writer = new PrintWriter(client.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
            if(client.isConnected()) {
                String request = reader.readLine();
                System.out.println("\n" + request);
                String parts[] = request.split(" ");
                if(parts.length != 3) {
                    throw new Exception("\nDoes not understand request");
                } else {
                    filename = parts[1].substring(1);
                    System.out.println("\nRequested file is: " + filename);
                }
            }

            BufferedOutputStream bos = new BufferedOutputStream(client.getOutputStream());
            writer.println("Hello worldss");

//            println(bos, "Hello world");

            System.out.println("\nClose connection with client... \n\n");
            client.close();

            System.out.println("Ignore following");




            BufferedOutputStream output = new BufferedOutputStream(client.getOutputStream());
            DataOutputStream dataOutput = new DataOutputStream(client.getOutputStream());


            while(client.isConnected()) {
                String line = reader.readLine();
                String parts[] = line.split(" ");
                if(parts.length == 3) {
                    System.out.println("First Line: " + parts[1].substring(1));

                    println(output, "HTTP/1.1 200 OK");
                    println(output, "Content-Type: text/html");
                    println(output, "\r\n");
                    println(output, "<p> Hello world </p>");

//                    writer.println("HTTP/1.1 200 OK");
//                    writer.println("Content-Type: text/html");
//                    writer.println("\r\n");
//                    writer.println("<p> Hello world </p>");
//                    writer.flush();

//                    dataOutput.writeBytes("HTTP/1.1 200 OK");
//                    dataOutput.writeBytes("Content-Type: text/html");
//                    dataOutput.writeBytes("\r\n");
//                    dataOutput.writeBytes("<p> Hello world mello tello sellpo</p>");
//
//                    dataOutput.close();

//                    out.close();
                }
            }
        } catch (Exception e) {
            System.err.println("Exception: " + e);
        }

        server.logout(this);
    }

    private void println(BufferedOutputStream bos, String s) throws IOException {
        String news = s + "\r\n";
        byte[] array = news.getBytes();
        for(int i = 0; i <array.length; i++) {
            bos.write(array[i]);
        }
        return;
    }
}