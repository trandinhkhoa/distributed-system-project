Topology ?

Server cluster:

Dictionnary file on the bootstrapping machine
Cluster amount fixed : N at boot
Dictionnary shared in N blocks then sent to the other servers
Blocks split into smaller chunks (of size S)

wait_for_client()
send_chunk_to_client()
When iddle, servers can compute tasks themselves



Client:
Connect_to_the_node()
while (taskAreLeft && resultNotFound)
	Get a chunk
	result = Compute -> should be interruptable if result is found by someone else
	if (result == FOUND)
		send_found_to_server();
	else
		ask_server_more_work();
	fi
end



===================================

One load balancer
    Model: Least Connection
    We can see each server number of client
    We add to the one(s) with the least clients

Servers
    Bootstrapping : fixed numbers of servers, we divide the dictionnary at the beginning

Bootstrapping :
    Using Ant scripts

TODO:
- Figure out a way to bootstrap with Ant
    - Figure out how does Ant work
- Program the client
    - Connection to the server/load balancer
    - Requesting more work
    - Handling interruption (the server announce us that the hash inverse was found)
    - Communication with the server (ask for more work, or found the hash inverse)
- Program the load balancer
    - Bootstrap
    - Connect a client to a server, Least Connection
- Program the servers
    - Bootstrap
    - Connect to the load balancer
    - Connect to the servers
    - Wait for clients
    - Send work to do
    - Propagate success to other servers
    - Propagate success to other clients
- Test the program
    - Add unit testing
    - Add a way to launch several headless VMs and start testing

Questions
    How to bootstrap ? Could the load balancer do it for the beginnig ?
        Shell scripts, load balancer can indeed do it
    How do everyone communicate between each others
        RabbitMQ
    What about the testing ?

Liens:
https://www.rabbitmq.com/getstarted.html
https://en.wikipedia.org/wiki/Peer-to-peer_file_sharing
https://www.startpage.com/do/dsearch?query=how+does+load+balancing+work&cat=web&pl=opensearch&language=english
https://en.wikipedia.org/wiki/Load_balancing_(computing)
https://www.startpage.com/do/dsearch?query=chord+p2p&cat=web&pl=opensearch&language=english


