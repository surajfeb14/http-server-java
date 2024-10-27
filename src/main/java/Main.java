import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Main {
    public static void main(String[] args) {
        System.out.println("Logs from your program will appear here!");

        ServerSocket serverSocket = null;
        Socket clientSocket = null;
        String response = "";

        try {
            serverSocket = new ServerSocket(4221);
            serverSocket.setReuseAddress(true);

            clientSocket = serverSocket.accept(); // Wait for connection from client
            System.out.println("Accepted new connection");

            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

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

            // Parse request line parts
            String[] parts = requestLine.split(" ");
            String path = parts.length > 1 ? parts[1] : "/";
            System.out.println("Path: " + path);

            // Parse User-Agent if present
            String userAgent = headers.get("User-Agent");

            // Handle the response based on path
            String[] pathArr = path.split("/");
            if (pathArr.length > 1 && "echo".equals(pathArr[1])) {
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
            } else {
                response = "HTTP/1.1 404 Not Found\r\n\r\n";
            }

            System.out.println("Response: " + response);

            // Send response
            OutputStream out = clientSocket.getOutputStream();
            out.write(response.getBytes());
            out.flush();

        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        } finally {
            // Close resources
            try {
                if (clientSocket != null) clientSocket.close();
                if (serverSocket != null) serverSocket.close();
            } catch (IOException e) {
                System.out.println("IOException on closing: " + e.getMessage());
            }
        }
    }
}