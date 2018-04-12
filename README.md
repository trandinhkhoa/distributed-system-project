# distributed-system-project

To use the Hash Machine, you need to launch the servers in different terminals, like so:

./start.sh server

Then launch the load balancer :

./start.sh load_balancer e5bc72b6601283cfff857c7770b257ab ../d.txt

After those are launched, several clients can be launched in separate terminals.

./start.sh client

Compiling:
    ant compile

Cleaning the build directory:
    ant clean
