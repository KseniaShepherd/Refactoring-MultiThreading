import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

public class ConnectionHandler implements Runnable {
    private Socket socket;

    public ConnectionHandler(Socket socket) throws IOException {
        this.socket = socket;
    }

    public void run() {
        List<String> validPaths = List.of("/index.html", "/spring.svg", "?spring.png");
        String requestLine = null;
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))
        ) {
            requestLine = in.readLine();

            String[] parts = requestLine.split(" ");
            if (parts.length != 3) {
                try {
                    out.write(
                            "HTTP/1.1 400 Bad Request\r\n" +
                                    "Content-Lenght: 0\r\n" +
                                    "Connection: close\r\n" +
                                    "\r\n");
                    out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;

            }
            String path = parts[1];
            if (!validPaths.contains(path)) {
                try {
                    out.write(
                            "HTTP/1.1 404 Not Found\r\n" +
                                    "Content-Lenght: 0\r\n" +
                                    "Connection: close\r\n" +
                                    "\r\n");
                    out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }
            Path filePath = Path.of(".", "public", path);
            String mimeType = Files.probeContentType(filePath);

            // special case for classic
            if (path.equals("/classic.html")) {
                String content = Files.readString(filePath).replace(
                        "{time}",
                        LocalDateTime.now().toString());
                out.write(
                        "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: " + mimeType + "\r\n" +
                                "Content-Length: " + content.getBytes().length + "\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                );
                out.write(content);
                out.flush();
                return;
            }

            long length = Files.size(filePath);
            out.write(
                    "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: " + mimeType + "\r\n" +
                            "Content-Length: " + length + "\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            );
            Files.copy(filePath, (Path) out);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

