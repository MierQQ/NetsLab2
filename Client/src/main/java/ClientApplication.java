import Client.Client;

import java.nio.channels.CancelledKeyException;

public class ClientApplication {
    public static void main(String[] args) {
        if (args.length != 4) {
            System.out.println("Usage: " + args[0] + " filepath address port");
            return;
        }
        Client client = new Client(args[1], args[2], Integer.parseInt(args[3]));
        try {
            client.run();
        } catch (Exception exception) {
            System.err.println(exception.getMessage());
        }
    }
}
