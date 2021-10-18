package Client;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.Arrays;

public class Client {
    private String _fileName;
    private int _port;
    private String _address;
    public Client(String file, String address, int port) {
        _fileName = file;
        _port = port;
        _address = address;
    }

    private static String checksumForDigest(String filename, MessageDigest md) throws IOException {
        try (
                var fis = new FileInputStream(filename);
                var bis = new BufferedInputStream(fis);
                var dis = new DigestInputStream(bis, md)
        ) {
            while (dis.read() != -1) ;
            md = dis.getMessageDigest();
        }
        var result = new StringBuilder();
        for (byte b : md.digest()) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    public void run() throws Exception{
        File file = new File(_fileName);
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        String checkSum = checksumForDigest(_fileName, messageDigest);
        try (Socket socket = new Socket(_address, _port)) {
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            String fileName = _fileName.split("/")[_fileName.split("/").length - 1];
            out.writeUTF(fileName);
            out.writeUTF(checkSum);
            DataInputStream in = new DataInputStream(socket.getInputStream());
            int code = in.readInt();
            if (code != 0) {
                out.close();
                in.close();
                throw new Exception("Server error");
            }

            System.out.println("Sending file");

            long length = file.length();
            out.writeLong(length);
            InputStream inFile = new FileInputStream(file);
            byte[] bytes = new byte[4096];
            int count;
            while ((count = inFile.read(bytes)) >= 0) {
                out.write(bytes, 0, count);
            }
            out.close();
            in.close();
            inFile.close();
        } catch (Exception exception) {
            throw exception;
        }
    }
}
