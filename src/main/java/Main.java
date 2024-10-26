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
        

        System.out.println("path: " + path);
        // System.out.println(pathsize);

        String[] pathArr = path.split("/");

        String response = "";

        if(pathArr.length > 1){
          if(pathArr[1].equals("echo")){
            
            String cont = pathArr[2];
            System.out.println("cont: " + cont);
            String pathsize = Integer.toString(cont.length());
            
            response = "HTTP/1.1 200 OK\r\n" +
            "Content-Type: text/plain\r\n" +
            "Content-Length: " + pathsize + "\r\n\r\n" +
            cont + "\r\n";
          }else{
            clientSocket.getOutputStream().write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
          }
        }else{
          clientSocket.getOutputStream().write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
        }
    
      // serverSocket.accept(); // Wait for connection from client.

      // if(path.equals("/") || path == null){
      //   clientSocket.getOutputStream().write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
      // }else{
      //   clientSocket.getOutputStream().write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
      // }

      

      System.out.println(response);

      clientSocket.getOutputStream().write(response.getBytes());

    } catch (IOException e) {
      System.out.println("IOException: " + e.getMessage());
    }
  }
}
