package Server.ServerExceptions;

public class ServerException extends Exception {
    public ServerException(String str, Exception ex) {
        super(str, ex);
    }
}
