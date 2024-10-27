import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

public class Main {
  public static void main(String[] args) {
      System.out.println("Logs from your program will appear here!");

      // Parse directory argument
      String directory = "/tmp/"; // default directory
      for (int i = 0; i < args.length; i++) {
          if (args[i].equals("--directory") && i + 1 < args.length) {
              directory = args[i + 1];
              break;
          }
      }

      try (ServerSocket serverSocket = new ServerSocket(4221)) {
          serverSocket.setReuseAddress(true);

          while (true) {
              Socket clientSocket = serverSocket.accept();
              System.out.println("Accepted new connection");

              new Thread(new ConnectionHandler(clientSocket, directory)).start();
          }
      } catch (IOException e) {
          System.out.println("IOException: " + e.getMessage());
      }
  }
}

class ConnectionHandler implements Runnable {
    private final Socket clientSocket;
    private final String directory;

    public ConnectionHandler(Socket clientSocket, String directory) {
        this.clientSocket = clientSocket;
        this.directory = directory;
    }

    @Override
    public void run() {
        String response = "";

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             OutputStream out = clientSocket.getOutputStream()) {

            String requestLine = reader.readLine();
            System.out.println("Request Line: " + requestLine);

            if (requestLine == null) {
                return;
            }

            String[] requestParts = requestLine.split(" ");
            String method = requestParts[0];
            String path = requestParts.length > 1 ? requestParts[1] : "/";
            String[] pathParts = path.split("/");

            HashMap<String, String> headers = new HashMap<>();
            String line;
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                String[] arr = line.split(": ");
                if (arr.length == 2) {
                    headers.put(arr[0], arr[1]);
                }
            }

            String acceptEncoding = headers.getOrDefault("Accept-Encoding", "");
            boolean supportsGzip = acceptEncoding.contains("gzip");

            if ("GET".equals(method) && pathParts.length > 1 && "echo".equals(pathParts[1])) {
                String content = pathParts.length > 2 ? pathParts[2] : "";

                response = "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: text/plain\r\n" +
                        "Content-Length: " + content.length() + "\r\n";
                
                if (supportsGzip) {
                    response += "Content-Encoding: gzip\r\n";
                }
                
                response += "\r\n" + content;

            } else if ("POST".equals(method) && pathParts.length == 3 && "files".equals(pathParts[1])) {
                String filename = pathParts[2];
                int contentLength = Integer.parseInt(headers.getOrDefault("Content-Length", "0"));

                char[] body = new char[contentLength];
                reader.read(body, 0, contentLength);
                String bodyContent = new String(body);

                Path filePath = Paths.get(directory, filename);
                try (FileWriter fileWriter = new FileWriter(filePath.toFile())) {
                    fileWriter.write(bodyContent);
                }

                response = "HTTP/1.1 201 Created\r\n\r\n";

            } else if ("GET".equals(method) && pathParts.length == 3 && "files".equals(pathParts[1])) {
                String filename = pathParts[2];
                Path filePath = Paths.get(directory, filename);

                if (Files.exists(filePath)) {
                    String fileContent = new String(Files.readAllBytes(filePath));

                    response = "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: application/octet-stream\r\n" +
                            "Content-Length: " + fileContent.length() + "\r\n\r\n" +
                            fileContent;
                } else {
                    response = "HTTP/1.1 404 Not Found\r\n\r\n";
                }

            } else if ("GET".equals(method) && path.equals("/")) {
                response = "HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: 0\r\n\r\n";
            } else {
                response = "HTTP/1.1 404 Not Found\r\n\r\n";
            }

            out.write(response.getBytes());
            out.flush();

        } catch (IOException e) {
            System.out.println("IOException in handler: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.out.println("IOException on closing client socket: " + e.getMessage());
            }
        }
    }
}