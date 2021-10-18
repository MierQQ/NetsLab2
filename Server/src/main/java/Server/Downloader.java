package Server;

import java.io.*;
import java.net.Socket;
import java.security.DigestInputStream;
import java.security.MessageDigest;

public class Downloader implements Runnable{
    private Socket _socket;
    private long _start;
    private volatile long _readBytes;
    private volatile boolean _isFinished;
    private volatile boolean _isGood;
    public Downloader(Socket socket) {
        _socket = socket;
        _start = System.currentTimeMillis();
        _readBytes = 0;
        _isFinished = false;
        _isGood = false;
    }

    public String getClientAddress() {
        return _socket.toString();
    }

    private static String checksumForDigest(File filename, MessageDigest md) throws IOException {
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

    public long getReadBytes(){
        return _readBytes;
    }

    public boolean isFinished() {
        return _isFinished;
    }

    public boolean isGood() {
        return _isGood;
    }

    public long getStart() {
        return _start;
    }

    @Override
    public void run() {
        try(Socket socket = _socket) {
            DataInputStream in = new DataInputStream(socket.getInputStream());
            String fileName = in.readUTF();
            String checkSum = in.readUTF();
            String name = fileName;
            File dir = new File("./uploads");
            File outputFile;
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            synchronized (Downloader.class) {
                if (!dir.exists()) {
                    if (!dir.mkdir()) {
                        outputStream.writeInt(1);
                        in.close();
                        outputStream.close();
                        throw new Exception("failed to create folder \"uploads\"");
                    }
                }
                int number = 1;

                while (true) {
                    outputFile = new File(dir, name);
                    if (outputFile.exists()) {
                        name = number + fileName;
                        number++;
                    } else {
                        break;
                    }
                }
                System.out.println(getClientAddress() + ": " + name);
                if(!outputFile.createNewFile()) {
                    outputStream.writeInt(1);
                    in.close();
                    outputStream.close();
                    throw new Exception("Failed to create file");
                }
            }
            FileOutputStream output = new FileOutputStream(outputFile);
            outputStream.writeInt(0);

            long size = in.readLong();

            byte[] bytes = new byte[4096];
            int count;
            while ((count = in.read(bytes)) >= 0) {
                _readBytes += count;
                output.write(bytes, 0, count);
            }

            output.close();
            synchronized (this) {
                _isFinished = true;
                MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
                String finalCheckSum = checksumForDigest(outputFile, messageDigest);
                if (_readBytes == size && finalCheckSum.equals(checkSum)) {
                    _isGood = true;
                }
            }
            in.close();
            outputStream.close();
        } catch (Exception ex) {
            _isFinished = true;
            ex.printStackTrace();
        }
    }
}
