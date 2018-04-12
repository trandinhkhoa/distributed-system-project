# distributed-system-project

NOTE: Run the command from the root directory of the project

To use the Hash Machine, you need to launch the servers in different terminals, like so:

./start.sh server

You should launch several (we recommend 3, not too much, enough to see the benefit) servers at once, but you need to tell the system how many servers are here.

Then launch the load balancer :

./start.sh load_balancer <hash to find> <dictionary file> <number of server running>

Example :
./start.sh load_balancer e5bc72b6601283cfff857c7770b257ab ../d.txt 3

(Remember to tell her the hash and the original string of the hash)

You may need to wait a few moments for the partition to arrive to the servers (When it is done the message "[Server] [x] Saved my partition " will show on the server's terminal)
Then, several clients (we recommend 3, not too much, enough to see the benefit) can be launched in separate terminals.

./start.sh client

Compiling:
    ant compile

Cleaning the build directory:
    ant clean
