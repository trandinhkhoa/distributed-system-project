# distributed-system-project

To compile and use this program, you need to use ant.

You can start the main system like so :

ant bootstrap -Dhash="hash to find" -Dhostfile="path/to/hostfile" -Ddictfile="path/to/dict"

The servers are launched (for now) like so :
./src/launch.sh host_file hash rabbitMQ_queue_IP

The host file is the list of the servers IP, one IP per line.
The hash is the hash to compute.
The rabbitMQ_queue_IP is the IP of the load balancer.

A client is launched like so :

ant client -Dqueue_ip="IP of rabbitMQ queue"

The following won't do much for now :

Compiling:
    ant compile

Testing:
    ant test

Cleaning the directory:
    ant clean
