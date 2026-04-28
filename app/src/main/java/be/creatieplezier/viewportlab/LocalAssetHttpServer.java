package be.creatieplezier.viewportlab;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LocalAssetHttpServer {
    private static final String TAG = "ViewportLabServer";

    private final Context context;
    private final int port;
    private final String assetRoot;
    private final ExecutorService clientExecutor = Executors.newCachedThreadPool();

    private volatile boolean running = false;
    private ServerSocket serverSocket;
    private Thread serverThread;

    public LocalAssetHttpServer(Context context, int port, String assetRoot) {
        this.context = context.getApplicationContext();
        this.port = port;
        this.assetRoot = assetRoot;
    }

    public void start() throws IOException {
        if (running) return;

        serverSocket = new ServerSocket();
        serverSocket.setReuseAddress(true);
        serverSocket.bind(new InetSocketAddress("127.0.0.1", port));

        running = true;
        serverThread = new Thread(this::acceptLoop, "ViewportLabLocalhost-" + port);
        serverThread.setDaemon(true);
        serverThread.start();

        Log.i(TAG, "Server started on http://127.0.0.1:" + port);
    }

    public void stop() {
        running = false;

        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException ignored) {
        }

        clientExecutor.shutdownNow();
    }

    private void acceptLoop() {
        while (running) {
            try {
                Socket socket = serverSocket.accept();
                clientExecutor.submit(() -> handleClient(socket));
            } catch (IOException exception) {
                if (running) {
                    Log.e(TAG, "Accept failed", exception);
                }
            }
        }
    }

    private void handleClient(Socket socket) {
        try (Socket client = socket) {
            client.setSoTimeout(8000);

            BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
            String requestLine = reader.readLine();

            if (requestLine == null || requestLine.trim().isEmpty()) {
                return;
            }

            String[] requestParts = requestLine.split(" ");
            if (requestParts.length < 2) {
                writeText(client.getOutputStream(), 400, "Bad Request", "text/plain; charset=utf-8", "Bad Request");
                return;
            }

            String method = requestParts[0].toUpperCase(Locale.ROOT);
            String rawPath = requestParts[1];

            // Consume headers.
            String headerLine;
            while ((headerLine = reader.readLine()) != null && !headerLine.isEmpty()) {
                // intentionally ignored
            }

            if (!method.equals("GET") && !method.equals("HEAD")) {
                writeText(client.getOutputStream(), 405, "Method Not Allowed", "text/plain; charset=utf-8", "Method Not Allowed");
                return;
            }

            AssetResponse response = loadAsset(rawPath);
            writeResponse(client.getOutputStream(), response, method.equals("HEAD"));
        } catch (Exception exception) {
            Log.e(TAG, "Client handling failed", exception);
        }
    }

    private AssetResponse loadAsset(String rawPath) throws IOException {
        String path = rawPath.split("\\?")[0];
        path = URLDecoder.decode(path, "UTF-8");

        if (path.equals("/") || path.trim().isEmpty()) {
            path = "/index.html";
        }

        path = sanitizePath(path);
        String assetPath = assetRoot + path;

        try {
            byte[] body = readAsset(assetPath);
            return new AssetResponse(200, "OK", mimeType(assetPath), body);
        } catch (IOException first) {
            byte[] body = readAsset(assetRoot + "/index.html");
            return new AssetResponse(200, "OK", "text/html; charset=utf-8", body);
        }
    }

    private String sanitizePath(String path) {
        path = path.replace("\\", "/");

        while (path.contains("//")) {
            path = path.replace("//", "/");
        }

        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        if (path.contains("..")) {
            return "/index.html";
        }

        return path;
    }

    private byte[] readAsset(String assetPath) throws IOException {
        AssetManager assets = context.getAssets();

        try (InputStream inputStream = assets.open(assetPath)) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[16 * 1024];
            int read;

            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }

            return outputStream.toByteArray();
        }
    }

    private void writeResponse(OutputStream outputStream, AssetResponse response, boolean headOnly) throws IOException {
        String headers =
                "HTTP/1.1 " + response.status + " " + response.reason + "\r\n" +
                "Content-Type: " + response.contentType + "\r\n" +
                "Content-Length: " + response.body.length + "\r\n" +
                "Cache-Control: no-cache\r\n" +
                "Access-Control-Allow-Origin: *\r\n" +
                "Service-Worker-Allowed: /\r\n" +
                "X-Viewport-Lab-Port: " + port + "\r\n" +
                "Connection: close\r\n" +
                "\r\n";

        outputStream.write(headers.getBytes(StandardCharsets.UTF_8));

        if (!headOnly) {
            outputStream.write(response.body);
        }

        outputStream.flush();
    }

    private void writeText(OutputStream outputStream, int status, String reason, String contentType, String body) throws IOException {
        writeResponse(outputStream, new AssetResponse(status, reason, contentType, body.getBytes(StandardCharsets.UTF_8)), false);
    }

    private String mimeType(String path) {
        String lower = path.toLowerCase(Locale.ROOT);

        if (lower.endsWith(".html")) return "text/html; charset=utf-8";
        if (lower.endsWith(".js")) return "text/javascript; charset=utf-8";
        if (lower.endsWith(".css")) return "text/css; charset=utf-8";
        if (lower.endsWith(".json")) return "application/json; charset=utf-8";
        if (lower.endsWith(".webmanifest")) return "application/manifest+json; charset=utf-8";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".svg")) return "image/svg+xml; charset=utf-8";
        if (lower.endsWith(".txt")) return "text/plain; charset=utf-8";
        if (lower.endsWith(".md")) return "text/markdown; charset=utf-8";

        return "application/octet-stream";
    }

    private static class AssetResponse {
        final int status;
        final String reason;
        final String contentType;
        final byte[] body;

        AssetResponse(int status, String reason, String contentType, byte[] body) {
            this.status = status;
            this.reason = reason;
            this.contentType = contentType;
            this.body = body;
        }
    }
}
