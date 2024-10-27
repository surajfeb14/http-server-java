import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.io.File; 
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.Scanner;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        System.out.println("Logs from your program will appear here!");

        try (ServerSocket serverSocket = new ServerSocket(4221)) {
            serverSocket.setReuseAddress(true);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Accepted new connection");

                // Create a new thread for each connection
                new Thread(new ConnectionHandler(clientSocket, args)).start();
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }
}

class ConnectionHandler implements Runnable {
    private final Socket clientSocket;
    String[] args;

    public ConnectionHandler(Socket clientSocket, String[] args) {
        this.clientSocket = clientSocket;
        this.args = args;
    }

    @Override
    public void run() {
        String response = "";
        String body = "";

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             OutputStream out = clientSocket.getOutputStream()) {

            // Read the request line
            String requestLine = reader.readLine();
            System.out.println("Request Line: " + requestLine);

            HashMap<String, String> headers = new HashMap<>();
            String read;
            while ((read = reader.readLine()) != null && !read.isEmpty()) {
                String[] arr = read.split(": ");
                if (arr.length == 2) {
                    headers.put(arr[0], arr[1]);
                }
            }
            while ((read = reader.readLine()) != null){
              body += reader.readLine() + " ";
            }
            reader.close();
            


            // Parse request line parts
            String[] parts = requestLine.split(" ");
            System.out.println("parts: "+Arrays.toString(parts));
            String path = parts.length > 1 ? parts[1] : "/";
            System.out.println("Path: " + path);
            
            // Handle the response based on path
            String[] pathArr = path.split("/");

            //For POST
            if(parts[0].equals("POST")){
              if (pathArr.length > 1 && (pathArr[1].startsWith("/files/"))) {
                try{
                  File file = new File("/tmp/data/codecrafters.io/http-server-tester/" + path.substring(7));
                  FileWriter myWriter = new FileWriter(file);
                  System.out.println("Body: " + body);
                  myWriter.write(body);
                  myWriter.close();
                  file.createNewFile();
                  System.out.println("File: " + file.getAbsolutePath());
                  response = "HTTP/1.1 201 Created\r\n\r\n";
                  }catch(Exception e){
                    System.out.println("File not created");
                    response = "HTTP/1.1 404 Not Found\r\n\r\n";
                  }
              }
            }else{

              
              // Parse User-Agent if present
              String userAgent = headers.get("User-Agent");
              
              if (path.equals("/")) {
                response = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: text/plain\r\n" +
                "Content-Length: 0\r\n\r\n";
              } else if (pathArr.length > 1 && "echo".equals(pathArr[1])) {
                String content = pathArr.length > 2 ? pathArr[2] : "";
                response = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: text/plain\r\n" +
                "Content-Length: " + content.length() + "\r\n\r\n" +
                content;
              } else if (userAgent != null) {
                response = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: text/plain\r\n" +
                "Content-Length: " + userAgent.length() + "\r\n\r\n" +
                userAgent;
              } else if (pathArr.length > 1 && "files".equals(pathArr[1])) {
                try{
                  File file = new File("/tmp/data/codecrafters.io/http-server-tester/" + path.substring(7));
                  String data = "";
                  String dataSize = "";
                  dataSize = String.valueOf(file.length());
                  Scanner myReader = new Scanner(file);
                  while (myReader.hasNextLine()) {
                    data += myReader.nextLine() + " ";
                    System.out.println(data);
                  }
                  myReader.close();
                  response = "HTTP/1.1 200 OK\r\n" +
                  "Content-Type: application/octet-stream\r\n" +
                  "Content-Length: " + dataSize + "\r\n\r\n" +
                  data;
                }catch(Exception e){
                  System.out.println("File not found");
                  response = "HTTP/1.1 404 Not Found\r\n\r\n";
                  e.printStackTrace();
                }
                
              }else {
                response = "HTTP/1.1 404 Not Found\r\n\r\n";
              }
            }

            System.out.println("Response: " + response);

            // Send response
            out.write(response.getBytes());
            out.flush();

        } catch (IOException e) {
            System.out.println("IOException in handler: " + e.getMessage());
        } finally {
            // Close client connection
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.out.println("IOException on closing client socket: " + e.getMessage());
            }
        }
    }
}