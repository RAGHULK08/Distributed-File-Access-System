// IndexServer.java
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class IndexServer {
    private static final int PORT = 9090;
    private static final Map<String, List<FileLocation>> fileIndex = new ConcurrentHashMap<>();
    private static final Map<String, ServerInfo> departmentServers = new ConcurrentHashMap<>();
    
    public static void main(String[] args) {
        System.out.println("Index Server starting on port " + PORT);
        
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            ExecutorService pool = Executors.newFixedThreadPool(10);
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                pool.execute(new IndexHandler(clientSocket));
            }
        } catch (IOException e) {
            System.err.println("Index Server error: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }
    
    static class IndexHandler implements Runnable {
        private final Socket socket;
        
        public IndexHandler(Socket socket) {
            this.socket = socket;
        }
        
        @Override
        public void run() {
            try (
                BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
            ) {
                String request = in.readLine();
                if (request == null) return;
                
                String[] parts = request.split(" ", 2);
                String command = parts[0];
                
                switch (command) {
                    case "REGISTER" -> handleRegister(parts[1], out);
                    case "SEARCH" -> handleSearch(parts[1], out);
                    case "LIST_ALL" -> handleListAll(out);
                    case "GET_SERVER" -> handleGetServer(parts[1], out);
                }
            } catch (IOException e) {
                System.err.println("Handler error: " + e.getMessage());
                e.printStackTrace(System.err);
            } finally {
                try { socket.close(); } catch (IOException e) {}
            }
        }
        
        private void handleRegister(String data, PrintWriter out) {
            // Format: SERVER_NAME|IP|PORT|FILE1,FILE2,FILE3
            String[] serverData = data.split("\\|");
            String serverName = serverData[0];
            String ip = serverData[1];
            int port = Integer.parseInt(serverData[2]);
            
            departmentServers.put(serverName, new ServerInfo(ip, port));
            
            String[] files = new String[0];
            if (serverData.length > 3) {
                files = serverData[3].split(",");
                for (String file : files) {
                    fileIndex.computeIfAbsent(file.trim().toLowerCase(), 
                        k -> new ArrayList<>()).add(new FileLocation(serverName, file.trim()));
                }
            }
            
            out.println("REGISTERED");
            System.out.println("Registered: " + serverName + " with " + files.length + " files");
        }
        
        private void handleSearch(String filename, PrintWriter out) {
            filename = filename.toLowerCase();
            List<FileLocation> locations = fileIndex.get(filename);
            
            if (locations != null && !locations.isEmpty()) {
                StringBuilder response = new StringBuilder("FOUND ");
                for (FileLocation loc : locations) {
                    ServerInfo info = departmentServers.get(loc.serverName);
                    response.append(String.format("%s|%s|%d|%s,",
                        loc.serverName, info.ip, info.port, loc.filename));
                }
                out.println(response.toString().substring(0, response.length() - 1));
            } else {
                out.println("NOT_FOUND");
            }
        }
        
        private void handleListAll(PrintWriter out) {
            StringBuilder response = new StringBuilder("FILES ");
            for (Map.Entry<String, List<FileLocation>> entry : fileIndex.entrySet()) {
                response.append(entry.getKey()).append(",");
            }
            if (response.length() > 6) {
                out.println(response.toString().substring(0, response.length() - 1));
            } else {
                out.println("NO_FILES");
            }
        }
        
        private void handleGetServer(String serverName, PrintWriter out) {
            ServerInfo info = departmentServers.get(serverName);
            if (info != null) {
                out.println("SERVER " + info.ip + " " + info.port);
            } else {
                out.println("SERVER_NOT_FOUND");
            }
        }
    }
    
    static class FileLocation {
        String serverName;
        String filename;
        
        FileLocation(String serverName, String filename) {
            this.serverName = serverName;
            this.filename = filename;
        }
    }
    
    static class ServerInfo {
        String ip;
        int port;
        
        ServerInfo(String ip, int port) {
            this.ip = ip;
            this.port = port;
        }
    }
}