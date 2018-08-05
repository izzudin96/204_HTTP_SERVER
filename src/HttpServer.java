import java.net.*;
import java.io.*;
import java.util.*;

class HttpServer
{
    private ArrayList<HttpServerSession> sessions;

    public static void main(String args[]) {
        HttpServer server = new HttpServer();
        System.out.println("Server is starting...");
        System.out.println("Server files path:  = " + System.getProperty("user.dir"));
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
                HttpServerSession thread = new HttpServerSession(client);
                synchronized (sessions) {
                    sessions.add(thread);
                }
                thread.start();
            }
        } catch (Exception e) {
            System.err.println("Exception: " + e);
        }
    }
}

class HttpServerSession extends Thread
{
    private Socket client;
    private String filename;
    private String requestType;
    static final File WEB_ROOT = new File(".");
    static final String FILE_NOT_FOUND = "404.html";

    public HttpServerSession(Socket client) {
        this.client = client;
    }

    public void run() {
        try {
            //The reader is used to read client's GET request.
            BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
            //The writer is used to write HTTP headers
            PrintWriter writer = new PrintWriter(client.getOutputStream());
            //The data is binary output to client
            BufferedOutputStream data = new BufferedOutputStream(client.getOutputStream());

            if(client.isConnected()) {
                String requestedFileName = parseRequestFileName(reader.readLine());

                System.out.println("File Name:" + requestedFileName);

                if(requestedFileName.equals("")) {
                    requestedFileName = "index.html";
                }

                try {
                    File file = new File(WEB_ROOT, requestedFileName);
                    System.out.println("File:" + file);
                    int fileLength = (int) file.length();
                    String contentType = parseContentType(requestedFileName);

                    byte[] fileData = readFileData(file, fileLength);

                    writer.println("HTTP/1.1 200 OK");
                    writer.println("Server: Java HTTP Server from SSaurel : 1.0");
                    writer.println("Date: " + new Date());
                    writer.println("Content-type: " + contentType);
                    writer.println("Content-length: " + fileLength);
                    writer.println();
                    writer.flush();

                    data.write(fileData, 0, fileLength);
                    data.flush();
                } catch (FileNotFoundException e) {
                    File file = new File(WEB_ROOT, FILE_NOT_FOUND);
                    int fileLength = (int) file.length();
                    String content = "text/html";
                    byte[] fileData = readFileData(file, fileLength);

                    writer.println("HTTP/1.1 404 File Not Found");
                    writer.println("Server: Java HTTP Server from SSaurel : 1.0");
                    writer.println("Date: " + new Date());
                    writer.println("Content-type: " + content);
                    writer.println("Content-length: " + fileLength);
                    writer.println(); // blank line between headers and content, very important !
                    writer.flush(); // flush character output stream buffer

                    data.write(fileData, 0, fileLength);
                    data.flush();
                }
            }


            if(1>2) {
                System.out.println("\nClose connection with client... \n\n");
                client.close();
            }
        } catch (Exception e) {
            System.err.println("Exception: " + e);
        }
    }

    public String parseRequestFileName(String request) {
        String parts[] = request.split(" ");
        if(parts.length != 3) {
            throw new RuntimeException("Does not understand request");
        } else {
            filename = parts[1].substring(1);
            System.out.println("\nRequested file is: " + filename);
            return filename;
        }
    }

    public String parseRequestType(String request) {
        String parts[] = request.split(" ");
        if(parts.length != 3) {
            throw new RuntimeException("Does not understand request");
        } else {
            requestType = parts[0];
            System.out.println("\nRequest Type is: " + requestType);
            return requestType;
        }
    }

    public String parseContentType(String requestedFile) {
        if(requestedFile.endsWith(".htm") || requestedFile.endsWith(".html")) {
            return "text/html";
        } else {
            return "text/plain";
        }
    }

    private byte[] readFileData(File file, int fileLength) throws IOException {
        FileInputStream fileIn = null;
        byte[] fileData = new byte[fileLength];

        try {
            fileIn = new FileInputStream(file);
            fileIn.read(fileData);
        } finally {
            if (fileIn != null)
                fileIn.close();
        }

        return fileData;
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