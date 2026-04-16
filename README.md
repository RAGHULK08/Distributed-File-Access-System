University Distributed File Access System

A custom distributed file system built with Java Sockets, designed to manage and share files across different university departments. The system uses a centralized indexing architecture where clients locate files via an Index Server and download them directly from the respective Department Servers.

🏗️ System Architecture
The system communicates over the campus network or internet using a Custom Java Socket API protocol.

Index Server (IndexServer.java): Acts as the central registry, running by default on port 9090. It maintains a concurrent map of registered department servers and their available files.

Department Servers (DepartmentServer.java): Independent file hosting nodes representing university departments (e.g., CS, Physics, Math). Upon startup, these servers register their network details and file lists with the Index Server. They handle GET, LIST, and DOWNLOAD requests from clients.

Student/Faculty Client (FileClient.java): A command-line interface that allows users to query the Index Server and download files. File data streams flow directly from the Department Server to the Client.

Configuration (Config.java): Manages system properties like index.host, index.port, download.dir (default: "downloads"), and buffer.size (default: 4096 bytes).

🚀 Quick Start Guide
Prerequisites
Java Development Kit (JDK) 14 or higher (supports switch expressions used in the code).

Bash environment (for the startup script).

1. Compile the Code
Open your terminal in the directory containing the source files and compile them:

Bash
javac *.java
2. Start the Servers
Use the provided Bash script to initialize the system. This script automatically creates mock department directories with sample files (e.g., algorithms.txt, quantum.txt) and launches the Index Server alongside three Department Servers (ports 9091, 9092, 9093).

Bash
chmod +x StartSystem.sh
./StartSystem.sh
To shut down all background server processes, simply press Ctrl+C in the terminal running the script.

3. Run the Client
In a new terminal window, start the client application by passing the Index Server's host and port as arguments:

Bash
java FileClient localhost 9090
💻 Usage
Once the client is running, you will be presented with a menu:

1. Search for a file: Query the Index Server for a specific file by name.

2. List all available files: View an alphabetical list of every file registered across all department servers.

3. Download a file: Prompts you for a filename, searches for its server location, and downloads it to the local downloads directory. If a file exists on multiple servers, you will be prompted to choose a source.

4. Exit: Closes the client application.

📡 Protocol Commands (Under the Hood)
The Custom Java Socket API utilizes standard string-based commands:

REGISTER <server_info>: Sent by Department Servers to the Index Server.

SEARCH <filename>: Sent by Clients to the Index Server.

LIST_ALL: Sent by Clients to the Index Server to view all system files.

DOWNLOAD <filename>: Sent by Clients to Department Servers to initiate file data streaming. Server responds with SIZE <bytes> followed by the byte stream.
