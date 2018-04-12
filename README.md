# distributed-system-project

To use the Hash Machine, you need to launch the servers in different terminals, like so:

./start.sh server

You should launch several servers at once, but you need to tell the system how many servers are here.

Then launch the load balancer :

./start.sh load_balancer <hash to find> <dictionary file> <number of server running>

Example :
./start.sh load_balancer e5bc72b6601283cfff857c7770b257ab ../d.txt 3

After those are launched, several clients can be launched in separate terminals.

./start.sh client

Compiling:
    ant compile

Cleaning the build directory:
    ant clean
