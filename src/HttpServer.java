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
    private String requestedFileName;

    static final String DEFAULT_FILE = "index.html";
    static final String FILE_NOT_FOUND = "404.html";
    static final File WEB_ROOT = new File(".");

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
                requestedFileName = parseRequestFileName(reader.readLine());
                handleUserRequestedFile(requestedFileName, writer, data);
            }

            System.out.println("\nClose connection with client...");
            client.close();
        } catch (Exception e) {
            System.err.println("Exception: " + e);
        }
    }

    public void handleUserRequestedFile(String requestedFileName, PrintWriter writer, BufferedOutputStream data) throws IOException {
        if(requestedFileName.equals("")) {
            requestedFileName = DEFAULT_FILE;
        }

        try {
            File file = new File(WEB_ROOT, requestedFileName);
            int fileLength = (int) file.length();
            String contentType = parseContentType(requestedFileName);

            byte[] fileData = readFileData(file, fileLength);

            writer.println("HTTP/1.1 200 OK");
            writer.println("Server: COMPX202 HTTP Server by Izzudin Anuar");
            writer.println("Date: " + new Date());
            writer.println("Content-type: " + contentType);
            writer.println("Content-length: " + fileLength);
            writer.println();
            writer.flush();

            while(true) {
                try {
                    sleep(6000);
                    print("Sleeping");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                data.write(fileData, 0, fileLength);
                break;
            }
            data.flush();
        }

        catch (FileNotFoundException e) {
            File file = new File(WEB_ROOT, FILE_NOT_FOUND);
            int fileLength = (int) file.length();
            String content = "text/html";
            byte[] fileData = readFileData(file, fileLength);

            writer.println("HTTP/1.1 404 File Not Found");
            writer.println("Server: COMPX202 HTTP Server by Izzudin Anuar");
            writer.println("Date: " + new Date());
            writer.println("Content-type: " + content);
            writer.println("Content-length: " + fileLength);
            writer.println();
            writer.flush();

            data.write(fileData, 0, fileLength);
            data.flush();
        }
    }

    public String parseRequestFileName(String request) {
        String parts[] = request.split(" ");
        if(parts.length != 3) {
            throw new RuntimeException("Does not understand request");
        } else {
            filename = parts[1].substring(1);
            print("\nRequested file is: " + filename);
            return filename;
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

    private void print(String text) {
        System.out.println(text);
    }
}