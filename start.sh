usage(){
    if [ $# -eq 0 ] ; then
        echo "Usage: start.sh <arg>";
        echo -e "\tserver"
        echo -e "\tclient"
        echo -e "\tload_balancer <MD5 hash string> <path/to/dict> <number of servers>"
        exit 0;
    fi
}

if [ "$1" == "server" ] ; then
    ant compile
    cd build/
    java -cp ../lib/amqp-client-5.1.2.jar:../lib/slf4j-api-1.7.21.jar:../lib/slf4j-simple-1.7.22.jar:. HashServer
elif [ "$1" == "client" ] ; then
    ant compile
    cd build/
    java -cp ../lib/amqp-client-5.1.2.jar:../lib/slf4j-api-1.7.21.jar:../lib/slf4j-simple-1.7.22.jar:. Client
elif [ "$1" == "load_balancer" ] ; then
    if [ $# -ne 4 ]; then
        usage;
    fi
    ant compile
    cd build/
    java -cp ../lib/amqp-client-5.1.2.jar:../lib/slf4j-api-1.7.21.jar:../lib/slf4j-simple-1.7.22.jar:. LoadBalancer $2 $3 $4
else
    echo "Wrong argument.";
fi
