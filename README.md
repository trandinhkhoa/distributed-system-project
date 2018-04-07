# distributed-system-project

To compile and use this program, you need to use ant.

You can start the load balancer like so:
ant load_balancer -Dhash="hash to find" -Dhostfile="path/to/hostfile" -Ddictfile="path/to/dict"

Compiling:
    ant compile

Testing:
    ant test

Cleaning the directory:
    ant clean
