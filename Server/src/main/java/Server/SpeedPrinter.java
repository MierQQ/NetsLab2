package Server;

import java.net.Socket;

public class SpeedPrinter implements Runnable {
    private final Downloader _downloader;
    public SpeedPrinter(Downloader downloader) {
        _downloader = downloader;
    }

    @Override
    public void run() {
        String sender = _downloader.getClientAddress();
        long now;
        long start = _downloader.getStart();
        long last = start;
        long nowReadBytes;
        long lastReadBytes = 0;
        do {
            try {
                Thread.sleep(3000);
                now = System.currentTimeMillis();
                nowReadBytes = _downloader.getReadBytes();
                System.out.println(sender + ": Average speed: " + (((double)nowReadBytes / (now - start)) * 1000) + "/sec");
                System.out.println(sender + ": Speed: " + (((double)(nowReadBytes - lastReadBytes) / (now - last)) * 1000) + "/sec");
                last = now;
                lastReadBytes = nowReadBytes;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (!_downloader.isFinished());
        synchronized (_downloader) {
            if (_downloader.isGood()) {
                System.out.println(sender + ": Download success");
            } else {
                System.out.println(sender + ": Download failed");
            }
        };
    }
}
