
order:(from build folder)

1. server
java -cp ../lib/amqp-client-5.1.2.jar:../lib/slf4j-api-1.7.21.jar:../lib/slf4j-simple-1.7.22.jar:. HashServer

2.-> load balancer

java -cp ../lib/amqp-client-5.1.2.jar:../lib/slf4j-api-1.7.21.jar:../lib/slf4j-simple-1.7.22.jar:. LoadBalancer <hashString> <path/to/dict> 

(example)
 java -cp ../lib/amqp-client-5.1.2.jar:../lib/slf4j-api-1.7.21.jar:../lib/slf4j-simple-1.7.22.jar:. LoadBalancer e5bc72b6601283cfff857c7770b257ab ../d.txt


3. client request 
java -cp ../lib/amqp-client-5.1.2.jar:../lib/slf4j-api-1.7.21.jar:../lib/slf4j-simple-1.7.22.jar:. Client


TODO list:
- send result back from client
- if a node connect to the queue and find multiple item, the node take them all(this not very important if the launch order is guaranteed)

