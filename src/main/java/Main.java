import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
  public static void main(String[] args) {
    // You can use print statements as follows for debugging, they'll be visible when running tests.
    System.out.println("Logs from your program will appear here!");

    // Uncomment this block to pass the first stage
    //
    ServerSocket serverSocket = null;
    Socket clientSocket = null;

    try {
      serverSocket = new ServerSocket(4221);
    
      // Since the tester restarts your program quite often, setting SO_REUSEADDR
      // ensures that we don't run into 'Address already in use' errors
      serverSocket.setReuseAddress(true);
      
      clientSocket = serverSocket.accept(); // Wait for connection from client.
      
      System.out.println("accepted new connection");

      BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

      String requestLine = reader.readLine();

      String[] parts = requestLine.split(" ");
        String path = parts[1];
        String pathsize = Integer.toString(path.length());
    
      // serverSocket.accept(); // Wait for connection from client.

      // if(path.equals("/") || path == null){
      //   clientSocket.getOutputStream().write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
      // }else{
      //   clientSocket.getOutputStream().write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
      // }
      
        clientSocket.getOutputStream().write("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: " + pathsize + "\r\n\r\n" + path + "\r\n".getBytes());

    } catch (IOException e) {
      System.out.println("IOException: " + e.getMessage());
    }
  }
}
