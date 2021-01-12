import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final int PORT = 9999;
    private static final int N_THREADS = 64;


    public static void main(String[] args) throws IOException {

        ServerSocket serverSocket = new ServerSocket(PORT);
        ExecutorService service = Executors.newFixedThreadPool(N_THREADS);

        while (true) {
            Socket socket = serverSocket.accept();
            service.execute(new ConnectionHandler(socket));
        }
    }
}