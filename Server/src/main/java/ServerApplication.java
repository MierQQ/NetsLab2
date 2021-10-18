import Server.Server;
import Server.ServerExceptions.ServerException;

public class ServerApplication {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: " + args[0] + " port");
            return;
        }
        Server server = new Server(Integer.parseInt(args[1]));
        try {
            server.run();
        } catch (ServerException e) {
            e.printStackTrace();
        }
    }
}
