# distributed-system-project

#NOTE: 
- Run the command from the root directory of the project
- The client can also be understood as the worker who receive data and do the computation

#0. Compile and clean
Compiling:
    ant compile

Cleaning the build directory:
    ant clean

#1. Phase 1.

##1.a.
To use the Hash Machine, you need to launch the servers in different terminals, like so:

./start.sh server


You should launch several (we recommend 3, not too much, enough to see the benefit) servers at once, but you need to tell the system how many servers are here.

##1.b
Then launch the load balancer :

./start.sh load_balancer <hash to find> <dictionary file> <number of server running>

Examples :

This should inverse the md5 hash into the string "TOO-PERFECT" (without quotes)    (should takes at most 45 seconds)
./start.sh load_balancer 2f222bc8380d40245a91a079a3ae70d0 ../d.txt 3

This should inverse the md5 hash into the string "TOPPING" (without quotes) (should takes at most 45 seconds)
594f2ba0aaeacf6c9510f445a36217cc  - TOPPING
./start.sh load_balancer 594f2ba0aaeacf6c9510f445a36217cc ../d.txt 3

(Remember to tell her the hash and the original string of the hash)

#2. Phase 2.
You may need to wait a few moments for the partition to arrive to the servers (When it is done the message "[Server] [x] Saved my partition " will show on the server's terminal)
Then, several clients (we recommend 3, not too much, enough to see the benefit) can be launched in separate terminals.

./start.sh client


#3. Example execution sequence (All command on different terminals)

**start 3 servers**

./start.sh server

./start.sh server

./start.sh server

**choose the hash to inverse and split dictionary into 3 servers. This example Hash String should be inverted into "TOPPING"**
./start.sh load_balancer 594f2ba0aaeacf6c9510f445a36217cc ../d.txt 3

**connect the client/worker to do the computation**
./start.sh client

./start.sh client

./start.sh client
