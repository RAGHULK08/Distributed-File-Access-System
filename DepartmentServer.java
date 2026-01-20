// DepartmentServer.java - COMPLETE CORRECTED VERSION
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

public class DepartmentServer {
    private final String serverName;
    private final int port;
    private final String fileDirectory;
    private final String indexServerHost;
    private final int indexServerPort;
    
    public DepartmentServer(String serverName, int port, String fileDirectory, 
                           String indexServerHost, int indexServerPort) {
        this.serverName = serverName;
        this.port = port;
        this.fileDirectory = fileDirectory;
        this.indexServerHost = indexServerHost;
        this.indexServerPort = indexServerPort;
        
        // Create directory if it doesn't exist
        File dir = new File(fileDirectory);
        if (!dir.exists()) {
            dir.mkdirs();
            System.out.println("Created directory: " + dir.getAbsolutePath());
        }
    }
    
    public void start() {
        // Register with index server
        registerWithIndexServer();
        
        // Start file server
        System.out.println(serverName + " starting on port " + port + "...");
        
        try {
            // Bind to all interfaces
            InetAddress bindAddr = InetAddress.getByName("0.0.0.0");
            try (ServerSocket serverSocket = new ServerSocket(port, 50, bindAddr)) {
                System.out.println(serverName + " bound to: " + bindAddr + " on port " + port);
                System.out.println(serverName + " ready for connections...");
                
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    String clientAddress = clientSocket.getInetAddress().getHostAddress();
                    System.out.println(serverName + ": New connection from " + clientAddress);
                    new Thread(new FileHandler(clientSocket)).start();
                }
            }
        } catch (IOException e) {
            System.err.println(serverName + " error: " + e.getMessage());
        }
    }
    
    private void registerWithIndexServer() {
        System.out.println(serverName + ": Registering with Index Server at " + 
                          indexServerHost + ":" + indexServerPort);
        
        try {
            // Get local IP address (not localhost)
            String localIP = getLocalIP();
            System.out.println(serverName + ": Detected local IP as: " + localIP);
            
            // Get list of files in directory
            File dir = new File(fileDirectory);
            File[] files = dir.listFiles();
            StringBuilder fileList = new StringBuilder();
            
            if (files != null && files.length > 0) {
                System.out.println(serverName + ": Found " + files.length + " files");
                for (File file : files) {
                    if (file.isFile()) {
                        fileList.append(file.getName()).append(",");
                        System.out.println("  - " + file.getName());
                    }
                }
            } else {
                System.out.println(serverName + ": No files found in directory: " + fileDirectory);
            }
            
            // Connect to index server
            try (Socket socket = new Socket(indexServerHost, indexServerPort);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                
                // Build registration message
                String filesStr = fileList.length() > 0 ? 
                    fileList.substring(0, fileList.length() - 1) : "";
                
                String registration = String.format("REGISTER %s|%s|%d|%s",
                    serverName, 
                    localIP,
                    port,
                    filesStr);
                
                System.out.println(serverName + ": Sending registration: " + registration);
                out.println(registration);
                
                // Wait for response
                String response = in.readLine();
                System.out.println(serverName + ": Registration response: " + response);
                
                if (response != null && response.startsWith("REGISTERED")) {
                    System.out.println(serverName + ": Registration successful!");
                } else {
                    System.err.println(serverName + ": Registration failed!");
                }
            }
        } catch (ConnectException e) {
            System.err.println(serverName + ": Cannot connect to Index Server at " + 
                             indexServerHost + ":" + indexServerPort);
            System.err.println("Make sure IndexServer is running first!");
        } catch (IOException e) {
            System.err.println(serverName + ": Registration error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println(serverName + ": Registration error: " + e.getMessage());
        }
    }
    
    private String getLocalIP() throws SocketException {
        // Try to get a non-loopback, non-link-local IPv4 address
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        
        try {
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                // Skip interfaces that are down or loopback
                if (!iface.isUp() || iface.isLoopback()) continue;
                
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    // Use IPv4 address that's not link-local
                    if (addr instanceof Inet4Address && !addr.isLinkLocalAddress()) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            System.err.println(serverName + ": Error getting network interfaces: " + e.getMessage());
            throw e;
        }
        
        // Fallback: try to get any IPv4 address
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "127.0.0.1";
        }
    }
    
    class FileHandler implements Runnable {
        private final Socket socket;
        
        public FileHandler(Socket socket) {
            this.socket = socket;
        }
        
        @Override
        public void run() {
            String clientAddress = socket.getInetAddress().getHostAddress();
            
            try (
                BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
            ) {
                String request = in.readLine();
                if (request == null) {
                    System.out.println(serverName + ": Empty request from " + clientAddress);
                    return;
                }
                
                System.out.println(serverName + ": Request from " + clientAddress + ": " + request);
                String[] parts = request.split(" ", 2);
                String command = parts[0];
                
                switch (command) {
                    case "GET" -> {
                        if (parts.length > 1) {
                            handleGetFile(parts[1], out);
                        } else {
                            out.println("ERROR Missing filename");
                        }
                    }
                    case "LIST" -> handleListFiles(out);
                    case "DOWNLOAD" -> {
                        if (parts.length > 1) {
                            handleDownloadFile(parts[1], socket.getOutputStream());
                        } else {
                            out.println("ERROR Missing filename");
                        }
                    }
                    case "TEST" -> out.println("ALIVE " + serverName + " is running on port " + port);
                    default -> {
                        out.println("ERROR Unknown command: " + command);
                        System.out.println(serverName + ": Unknown command from " + 
                                          clientAddress + ": " + command);
                    }
                }
            } catch (IOException e) {
                System.err.println(serverName + ": Error with client " + clientAddress + ": " + e.getMessage());
            } finally {
                try { 
                    socket.close(); 
                    System.out.println(serverName + ": Connection closed: " + clientAddress);
                } catch (IOException e) {
                    System.err.println(serverName + ": Error closing socket: " + e.getMessage());
                }
            }
        }
        
        private void handleGetFile(String filename, PrintWriter out) {
            Path filePath = Paths.get(fileDirectory, filename);
            System.out.println(serverName + ": Checking file: " + filePath);
            
            if (Files.exists(filePath)) {
                try {
                    long fileSize = Files.size(filePath);
                    out.println("FILE_EXISTS " + fileSize);
                    System.out.println(serverName + ": File found: " + filename + " (" + fileSize + " bytes)");
                } catch (IOException e) {
                    out.println("ERROR Cannot read file: " + e.getMessage());
                }
            } else {
                out.println("FILE_NOT_FOUND");
                System.out.println(serverName + ": File not found: " + filename);
            }
        }
        
        private void handleListFiles(PrintWriter out) {
            File dir = new File(fileDirectory);
            File[] files = dir.listFiles();
            
            if (files == null || files.length == 0) {
                out.println("NO_FILES");
                System.out.println(serverName + ": No files available");
                return;
            }
            
            StringBuilder response = new StringBuilder("FILES ");
            System.out.println(serverName + ": Listing " + files.length + " files");
            
            for (File file : files) {
                if (file.isFile()) {
                    response.append(file.getName()).append(",");
                }
            }
            
            if (response.length() > 6) {
                out.println(response.substring(0, response.length() - 1));
            } else {
                out.println("NO_FILES");
            }
        }
        
        private void handleDownloadFile(String filename, OutputStream outStream) {
            System.out.println(serverName + ": Download requested for: " + filename);
            
            try {
                Path filePath = Paths.get(fileDirectory, filename);
                
                if (!Files.exists(filePath)) {
                    try (PrintWriter out = new PrintWriter(outStream, true)) {
                        out.println("ERROR_FILE_NOT_FOUND");
                    }
                    System.out.println(serverName + ": File not found for download: " + filename);
                    return;
                }
                
                // Get file size
                long fileSize = Files.size(filePath);
                System.out.println(serverName + ": Sending file: " + filename + 
                                 " (" + fileSize + " bytes)");
                
                // Send file size first
                try (PrintWriter textOut = new PrintWriter(outStream, true)) {
                    textOut.println("SIZE " + fileSize);
                    textOut.flush();
                }
                
                // Send file content in binary
                byte[] buffer = new byte[4096];
                try (InputStream fileIn = Files.newInputStream(filePath);
                     BufferedOutputStream bout = new BufferedOutputStream(outStream)) {
                    
                    int bytesRead;
                    long totalSent = 0;
                    
                    while ((bytesRead = fileIn.read(buffer)) != -1) {
                        bout.write(buffer, 0, bytesRead);
                        totalSent += bytesRead;
                        
                        // Show progress for large files
                        if (fileSize > 100000) { // For files > 100KB
                            int percent = (int) ((totalSent * 100) / fileSize);
                            if (percent % 10 == 0) {
                                System.out.println(serverName + ": Sending " + filename + 
                                                 ": " + percent + "%");
                            }
                        }
                    }
                    bout.flush();
                }
                
                System.out.println(serverName + ": File sent successfully: " + filename);
                
            } catch (IOException e) {
                System.err.println(serverName + ": Error sending file " + filename + ": " + e.getMessage());
                try (PrintWriter out = new PrintWriter(outStream, true)) {
                    out.println("ERROR Download failed: " + e.getMessage());
                } catch (Exception ex) {
                    // Ignore if we can't send error message
                }
            }
        }
    }
    
    public static void main(String[] args) {
        if (args.length != 5) {
            System.out.println("Usage: java DepartmentServer <serverName> <port> <fileDirectory> <indexHost> <indexPort>");
            System.out.println("Example: java DepartmentServer CS_Server 9091 cs_department localhost 9090");
            System.out.println("For network use, replace 'localhost' with server IP");
            return;
        }
        
        String serverName = args[0];
        int port = Integer.parseInt(args[1]);
        String fileDirectory = args[2];
        String indexHost = args[3];
        int indexPort = Integer.parseInt(args[4]);
        
        System.out.println("=== Department Server Configuration ===");
        System.out.println("Server Name: " + serverName);
        System.out.println("Port: " + port);
        System.out.println("File Directory: " + new File(fileDirectory).getAbsolutePath());
        System.out.println("Index Server: " + indexHost + ":" + indexPort);
        System.out.println("=======================================");
        
        DepartmentServer server = new DepartmentServer(serverName, port, fileDirectory, indexHost, indexPort);
        server.start();
    }
}