#!/bin/bash
# start_system.sh
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

echo "Compiling sources..."
javac *.java

echo "Starting Distributed File System..."

# Start Index Server
echo "Starting Index Server..."
java IndexServer &
INDEX_PID=$!
sleep 2

# Start Department Servers (in separate terminals or background)
echo "Starting Department Servers..."

# Create some sample files
mkdir -p cs_department
echo "Computer Science Notes" > cs_department/algorithms.txt
echo "Database Systems" > cs_department/database.txt

mkdir -p physics_department
echo "Quantum Mechanics" > physics_department/quantum.txt
echo "Thermodynamics" > physics_department/thermo.txt

mkdir -p math_department
echo "Calculus Notes" > math_department/calculus.txt
echo "Linear Algebra" > math_department/algebra.txt

# Start servers in background
java DepartmentServer "CS_Server" 9091 "cs_department" "localhost" 9090 &
java DepartmentServer "Physics_Server" 9092 "physics_department" "localhost" 9090 &
java DepartmentServer "Math_Server" 9093 "math_department" "localhost" 9090 &

sleep 3

echo "System started!"
echo "Start client with: java FileClient localhost 9090"
echo "Press Ctrl+C to stop all servers"

# Wait for Ctrl+C
trap "kill $INDEX_PID; exit" SIGINT
wait