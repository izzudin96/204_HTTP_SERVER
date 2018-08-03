import java.net.*;
import java.io.*;
import java.util.*;

class ChatServerSession extends Thread
{
    private ChatServer server;
    private Socket client;
    private PrintWriter writer;

    public void run()
    {
        try {
            writer = new PrintWriter(client.getOutputStream(), true);
            BufferedReader reader =
                    new BufferedReader(
                            new InputStreamReader(client.getInputStream()));
            while(true) {
                /* when the client disconnects, readLine returns null */
                String line = reader.readLine();
                if(line != null)
                    server.sayToAll(this, line);
                else
                    break;
            }
        }
        catch(Exception e) {
            System.err.println("Exception: " + e);
        }

        server.logout(this);
    }

    public void send(String message)
    {
        if(writer != null)
            writer.println(message);
    }

    public ChatServerSession(ChatServer a, Socket b)
    {
        server = a;
        client = b;
        writer = null;
    }
}

class ChatServer
{
    private ArrayList<ChatServerSession> sessions;

    public void sayToAll(ChatServerSession from, String message)
    {
        int i, len;
        synchronized(sessions) {
            len = sessions.size();
            for(i=0; i<len; i++) {
                ChatServerSession to = sessions.get(i);
                if(from != to) {
                    to.send(message);
                }
            }
        }
    }

    public void logout(ChatServerSession session)
    {
        int i, len;
        synchronized(sessions) {
            len = sessions.size();
            for(i=0; i<len; i++) {
                ChatServerSession s = sessions.get(i);
                if(s == session) {
                    sessions.remove(i);
                    break;
                }
            }
        }
    }

    public void start_server()
    {
        sessions = new ArrayList<ChatServerSession>();

        try {
            ServerSocket ss = new ServerSocket(1234);
            while(true) {
                Socket client = ss.accept();
                ChatServerSession thread =
                        new ChatServerSession(this, client);
                synchronized(sessions) {
                    sessions.add(thread);
                }
                thread.start();
            }
        }
        catch(Exception e) {
            System.err.println("Exception: " + e);
        }
    }

    public static void main(String args[])
    {
        ChatServer server = new ChatServer();
        server.start_server();
    }
}