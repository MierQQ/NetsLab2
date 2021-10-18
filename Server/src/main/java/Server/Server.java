package Server;

import Server.ServerExceptions.ServerException;

import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private final int _port;
    public Server(int port) {
        _port = port;
    }
    public void run() throws ServerException{
        try (ServerSocket servSocket = new ServerSocket(_port)) {
            while (true) {
                Socket socket = servSocket.accept();
                Downloader downloader = new Downloader(socket);
                Thread newDownloadThread = new Thread(downloader);
                newDownloadThread.start();
                Thread speedPrinterThread = new Thread(new SpeedPrinter(downloader));
                speedPrinterThread.start();
            }
        } catch (Exception ex) {
            throw new ServerException("Exception while running server", ex);
        }
    }
}
