import java.net.*;
import java.io.*;
import java.util.*;


class HttpServer
{
    public static void main(String args[]) throws IOException, ClassNotFoundException
    {
        int portNumber = 8080;

        System.out.println("web server is starting...");

        ServerSocket server = new ServerSocket(portNumber);
        print("Server is running on port: " + server.getLocalPort());

        while(true) {
            try {
                Socket clientSocket = server.accept();
                HttpServerSession session = new HttpServerSession(clientSocket);
            } finally {
                server.close();
            }
        }
    }

    public static void print(String message) {
        System.out.println(message);
    }
}

class HttpServerSession extends Thread
{
    private Socket client;
    private HttpServer server;
    private PrintWriter writer;

    public HttpServerSession(Socket socket) {
        this.client = socket;
        run();
    }

    public void run() {
        try {
            writer = new PrintWriter(client.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
            while(true) {
                /* when the client disconnects, readLine returns null */
                String line = reader.readLine();
                if(line != null) {
                    System.out.println(line);
                    System.out.println("Connection from: " + client.getLocalAddress());
                }
                else {
                    break;
                }
            }
        }
        catch(Exception e) {
            System.err.println("Exception: " + e);
        }

        try {
            client.close();
        } catch (IOException e) {
            System.err.println("Exception: " + e);
        }
    }

    public void send(String message)
    {
        if(writer != null)
            writer.println(message);
    }
}