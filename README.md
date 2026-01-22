<img width="1408" height="768" alt="Gemini_Generated_Image_jbj1lwjbj1lwjbj1" src="https://github.com/user-attachments/assets/7cc3322c-193a-4644-96d8-07421b5d19d0" /># 📁 Distributed File Access System
🚀 Overview
A Java-based distributed file system that enables seamless access to student notes stored across multiple departmental servers in a university environment. This system implements a client-server architecture with socket programming, allowing remote file discovery, retrieval, and management.

✨ Features
🔍 Centralized File Discovery - Single index server for file location tracking

🏗️ Distributed Storage - Files stored across multiple departmental servers

⚡ Real-time Access - Instant file search and download capabilities

🛡️ Concurrent Operations - Multiple clients can access files simultaneously

📊 Dynamic Registration - Servers can join/leave the network dynamically

🔧 Cross-Platform - Works on Windows, Linux, and macOS

📦 Components
1. Index Server (IndexServer.java)
Central registry maintaining file-to-server mappings

Handles client queries for file locations

Manages dynamic server registration

2. Department Servers (DepartmentServer.java)
Store and serve actual files

Register with index server on startup

Handle file retrieval requests

3. Client Application (FileClient.java)
User interface for file operations

Communicates with index server for discovery

Downloads files from appropriate department servers

🛠️ Technology Stack
Java 8+ - Core programming language

Socket Programming - Network communication

Multithreading - Concurrent client handling

TCP/IP Protocol - Reliable data transfer

File I/O - File management operations

📋 Prerequisites
Java Development Kit (JDK) 8 or higher

Basic understanding of network programming

Two or more machines for distributed testing (optional)

⚡ Quick Start

Step 1: Clone the Repository
bash
git clone https://github.com/yourusername/distributed-file-system.git
cd distributed-file-system

Step 2: Create Directory Structure
bash
# Create department directories
mkdir -p cs_department physics_department math_department downloads

# Add sample files
echo "Computer Science Notes" > cs_department/algorithms.txt
echo "Physics Notes" > physics_department/quantum.txt
echo "Mathematics Notes" > math_department/calculus.txt

Step 3: Compile the Source Code
bash
javac IndexServer.java DepartmentServer.java FileClient.java
🚦 Running the System
Single Machine Setup
Terminal 1 - Start Index Server:

bash
java IndexServer
Terminal 2 - Start Department Servers:

bash
java DepartmentServer "CS_Server" 9091 "cs_department" "localhost" 9090
java DepartmentServer "Physics_Server" 9092 "physics_department" "localhost" 9090
java DepartmentServer "Math_Server" 9093 "math_department" "localhost" 9090
Terminal 3 - Run Client:

bash
java FileClient localhost 9090
Multi-Machine Setup (Cross-Platform)
On Server Machine (Ubuntu/Linux):

bash
# Get your IP address
hostname -I

# Start servers with actual IP
java IndexServer &
java DepartmentServer "CS_Server" 9091 "cs_department" "192.168.1.100" 9090 &
On Client Machine (Windows):

powershell
# Connect using server's IP
java FileClient 192.168.1.100 9090
🎮 Usage Guide
Client Application Menu:
text
=== Distributed File System Client ===
1. Search for a file
2. List all available files
3. Download a file
4. Exit
Example Session:
text
Choose option: 1
Enter filename to search: algorithms.txt
File found at:
1. Server: CS_Server, File: algorithms.txt

Choose option: 3
Enter filename to download: algorithms.txt
Downloading algorithms.txt (1024 bytes)...
Progress: 100%
Download completed: downloads/algorithms.txt
🔧 Configuration
Port Configuration
Index Server: Default port 9090

Department Servers: Configurable ports (9091, 9092, etc.)

All ports are configurable in the source code

Network Binding
The system binds to 0.0.0.0 by default, allowing connections from any network interface.

🐛 Troubleshooting
Common Issues & Solutions:
Issue	Solution
Connection refused	Ensure Index Server is running first
Port already in use	Change port numbers or kill existing processes
File not found	Check file exists in department directory
Network timeout	Disable firewall or allow ports 9090-9093
Registration failed	Verify server IP addresses are correct

Firewall Configuration:
bash
# Ubuntu/Linux
sudo ufw allow 9090:9093/tcp

# Windows PowerShell (Admin)
New-NetFirewallRule -DisplayName "DFSPorts" -Direction Inbound -LocalPort 9090-9093 -Protocol TCP -Action Allow

📊 Performance Features
Connection Pooling - Efficient thread management

Chunked Transfers - Large file handling capability

Non-blocking I/O - Responsive during file transfers

Memory Efficient - Stream-based file operations

🔮 Future Enhancements
Planned Features:
Authentication & Authorization - User-based access control

File Encryption - Secure data transmission

Load Balancing - Intelligent request distribution

Replication - File redundancy across servers

Web Interface - Browser-based access

Database Backend - Persistent metadata storage

File Versioning - Track changes and revisions

Search Indexing - Full-text search capability

Potential Improvements:
Implement REST API for modern integration

Add Docker containerization for easy deployment

Develop mobile client applications

Integrate with cloud storage providers

🧪 Testing
Unit Tests
bash
# Test individual components
java TestConnection  # Test network connectivity
java TestFileTransfer  # Test file download functionality
Integration Tests
Start all servers

Run multiple clients simultaneously

Test file search, listing, and download operations

Verify concurrent access handling

📚 Learning Outcomes
This project demonstrates:

Socket Programming - Low-level network communication

Client-Server Architecture - Distributed system design

Multithreading - Concurrent request handling

File Management - Remote file operations

Network Protocols - Custom application-layer protocol design

Error Handling - Robust network application development

👥 Contributing
Contributions are welcome! Please follow these steps:

Fork the repository

Create a feature branch (git checkout -b feature/AmazingFeature)

Commit your changes (git commit -m 'Add some AmazingFeature')

Push to the branch (git push origin feature/AmazingFeature)

Open a Pull Request

Development Guidelines:
Follow Java coding conventions

Add comments for complex logic

Include error handling for all network operations

Test thoroughly before submitting PR

📄 License
This project is licensed under the MIT License - see the LICENSE file for details.

🙏 Acknowledgments
Inspired by distributed systems concepts

Built for educational purposes in network programming

Special thanks to Java Socket API documentation

📞 Support
For questions, issues, or contributions:

Contact: raghul1826@gmail.com
