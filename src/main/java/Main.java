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
      String userAgent = null;
      String response = "";

      if (parts != null && parts.length > 0) {
          int userAgentIndex = -1;
          for (int i = 0; i < parts.length; i++) {
              if (parts[i].equals("\"User-Agent:")) {
                  userAgentIndex = i;
                  break;
              }
          }
          if (userAgentIndex != -1 && userAgentIndex + 1 < parts.length) {
              userAgent = parts[userAgentIndex + 1];
          }
      }

      if (userAgent != null) {
          System.out.println("User-Agent: " + userAgent);
          response = "HTTP/1.1 200 OK\r\n" +
            "Content-Type: text/plain\r\n" +
            "Content-Length: " + userAgent.length() + "\r\n\r\n" +
            userAgent + "\r\n";
      } else {
          System.out.println("User-Agent header not found.");
          clientSocket.getOutputStream().write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
      }

        

        System.out.println("path: " + path);
        // System.out.println(pathsize);

        String[] pathArr = path.split("/");


        if(pathArr.length > 1){
          if(pathArr[1].equals("echo")){
            
            String cont = pathArr[2];
            System.out.println("cont: " + cont);
            String pathsize = Integer.toString(cont.length());
            
            response = "HTTP/1.1 200 OK\r\n" +
            "Content-Type: text/plain\r\n" +
            "Content-Length: " + pathsize + "\r\n\r\n" +
            cont + "\r\n";

            System.out.println(response);

            clientSocket.getOutputStream().write(response.getBytes());
          }else{
            clientSocket.getOutputStream().write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
          }
        }else{
          clientSocket.getOutputStream().write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
        }

    } catch (IOException e) {
      System.out.println("IOException: " + e.getMessage());
    }
  }
}
