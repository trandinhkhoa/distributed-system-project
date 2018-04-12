if [ $# -eq 0 ] ; then
    echo "Usage: start.sh <arg>";
    echo -e "\tserver"
    echo -e "\tclient"
    echo -e "\tload_balancer <hashString> <path/to/dict>"
    exit 0;
fi

if [ "$1" == "server" ] ; then
    cd build/
    java -cp ../lib/amqp-client-5.1.2.jar:../lib/slf4j-api-1.7.21.jar:../lib/slf4j-simple-1.7.22.jar:. HashServer
elif [ "$1" == "client" ] ; then
    cd build/
    java -cp ../lib/amqp-client-5.1.2.jar:../lib/slf4j-api-1.7.21.jar:../lib/slf4j-simple-1.7.22.jar:. Client
elif [ "$1" == "load_balancer" ] ; then
    if [ $# -ne 3 ]; then
        echo "Load balancer require a hash string and a path to a dictionary.";
        exit 1;
    fi
    cd build/
    java -cp ../lib/amqp-client-5.1.2.jar:../lib/slf4j-api-1.7.21.jar:../lib/slf4j-simple-1.7.22.jar:. LoadBalancer $2 $3
else
    echo "Wrong argument.";
fi
