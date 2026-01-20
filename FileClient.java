// FileClient.java
import java.io.*;
import java.net.*;
import java.util.*;

public class FileClient {
    private final String indexServerHost;
    private final int indexServerPort;
    private final Scanner scanner;
    
    public FileClient(String indexServerHost, int indexServerPort) {
        this.indexServerHost = indexServerHost;
        this.indexServerPort = indexServerPort;
        this.scanner = new Scanner(System.in);
    }
    
    public void start() {
        while (true) {
            System.out.println("\n=== Distributed File System Client ===");
            System.out.println("1. Search for a file");
            System.out.println("2. List all available files");
            System.out.println("3. Download a file");
            System.out.println("4. Exit");
            System.out.print("Choose option: ");
            
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline
            
            switch (choice) {
                case 1 -> searchFile();
                case 2 -> listAllFiles();
                case 3 -> downloadFile();
                case 4 -> {
                    System.out.println("Exiting...");
                    return;
                }
                default -> System.out.println("Invalid option");
            }
        }
    }
    
    private void searchFile() {
        System.out.print("Enter filename to search: ");
        String filename = scanner.nextLine();
        
        try (Socket socket = new Socket(indexServerHost, indexServerPort);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            
            out.println("SEARCH " + filename);
            String response = in.readLine();
            
            if (response.startsWith("FOUND")) {
                System.out.println("File found at:");
                String[] locations = response.substring(6).split(",");
                for (int i = 0; i < locations.length; i++) {
                    String[] info = locations[i].split("\\|");
                    System.out.println((i+1) + ". Server: " + info[0] + 
                                     ", File: " + info[3]);
                }
            } else {
                System.out.println("File not found in the system");
            }
            
        } catch (IOException e) {
            System.err.println("Error connecting to index server: " + e.getMessage());
        }
    }
    
    private void listAllFiles() {
        try (Socket socket = new Socket(indexServerHost, indexServerPort);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            
            out.println("LIST_ALL");
            String response = in.readLine();
            
            if (response.startsWith("FILES")) {
                System.out.println("Available files:");
                String[] files = response.substring(6).split(",");
                Arrays.sort(files);
                for (String file : files) {
                    System.out.println("- " + file);
                }
            } else {
                System.out.println("No files available");
            }
            
        } catch (IOException e) {
            System.err.println("Error connecting to index server: " + e.getMessage());
        }
    }
    
    private void downloadFile() {
        System.out.print("Enter filename to download: ");
        String filename = scanner.nextLine();
        
        // First, search for the file
        String serverInfo;
        try (Socket socket = new Socket(indexServerHost, indexServerPort);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            
            out.println("SEARCH " + filename);
            String response = in.readLine();
            
            if (response.startsWith("FOUND")) {
                String[] locations = response.substring(6).split(",");
                if (locations.length > 1) {
                    System.out.println("File found on multiple servers. Choose one:");
                    for (int i = 0; i < locations.length; i++) {
                        String[] info = locations[i].split("\\|");
                        System.out.println((i+1) + ". Server: " + info[0]);
                    }
                    System.out.print("Choose server (1-" + locations.length + "): ");
                    int choice = scanner.nextInt();
                    scanner.nextLine();
                    serverInfo = locations[choice - 1];
                } else {
                    serverInfo = locations[0];
                }
            } else {
                System.out.println("File not found");
                return;
            }
            
        } catch (IOException e) {
            System.err.println("Error connecting to index server: " + e.getMessage());
            return;
        }
        
        if (serverInfo != null) {
            String[] info = serverInfo.split("\\|");
            downloadFromServer(info[1], Integer.parseInt(info[2]), info[3]);
        }
    }
    
    private void downloadFromServer(String serverHost, int serverPort, String filename) {
        System.out.print("Enter local filename to save as (or press Enter for same name): ");
        String localFilename = scanner.nextLine();
        if (localFilename.isEmpty()) {
            localFilename = filename;
        }
        
        try (Socket socket = new Socket(serverHost, serverPort);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            
            // Request file download
            out.println("DOWNLOAD " + filename);
            
            // Read response
            String response = in.readLine();
            if (response.startsWith("SIZE")) {
                long fileSize = Long.parseLong(response.split(" ")[1]);
                System.out.println("Downloading " + filename + " (" + fileSize + " bytes)...");
                
                // Create downloads directory
                new File("downloads").mkdirs();
                
                // Download file
                try (FileOutputStream fileOut = new FileOutputStream("downloads/" + localFilename);
                     BufferedOutputStream bout = new BufferedOutputStream(fileOut)) {
                    
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    long totalRead = 0;
                    
                    InputStream socketIn = socket.getInputStream();
                    while (totalRead < fileSize && 
                           (bytesRead = socketIn.read(buffer)) != -1) {
                        bout.write(buffer, 0, bytesRead);
                        totalRead += bytesRead;
                        
                        // Show progress
                        if (fileSize > 0) {
                            int percent = (int)((totalRead * 100) / fileSize);
                            System.out.print("\rProgress: " + percent + "%");
                        }
                    }
                    bout.flush();
                    System.out.println("\nDownload completed: downloads/" + localFilename);
                    
                } catch (IOException e) {
                    System.err.println("Error saving file: " + e.getMessage());
                }
                
            } else if (response.startsWith("ERROR")) {
                System.out.println("Error: " + response.substring(6));
            }
            
        } catch (IOException e) {
            System.err.println("Error connecting to file server: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java FileClient <indexServerHost> <indexServerPort>");
            return;
        }
        
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        
        FileClient client = new FileClient(host, port);
        client.start();
    }
}