# distributed-system-project
This small project aims to reverse an unsalted hash. By coordinating the computation between multiple computers, we reduced the computation time of the dictionary attack. The server store the dictionary. The clients/worker (clients = workers, sorry for the confusing terminology here) ask the server for work (part of the dictionary) to work on. For example, the big dictionary is partitioned to 4 parts corresponding to 4 servers available. All clients in parallel ask for different part and bruteforcing them to improve the speed of the hash reversal process.

# NOTE: 
- Run the commands from the root directory of the project

# 0. Compile and clean
Compiling:

```console
    ant compile
```

Cleaning the build directory:

```console
    ant clean
```

# 1. Phase 1.

## 1.a.
To use the Hash Machine, you need to launch the servers in different terminals, like so:

```console
./start.sh server
```


You should launch several (we recommend 3, not too much, enough to see the benefit) servers at once, but you need to tell the system how many servers are here.

## 1.b
Then launch the load balancer :

```console
./start.sh load_balancer <hash to find> <dictionary file> <number of server running>
```

Examples :

This should inverse the md5 hash into the string "TOO-PERFECT" (without quotes)    (should takes at most 45 seconds)

```console
./start.sh load_balancer 2f222bc8380d40245a91a079a3ae70d0 ../d.txt 3
```

This should inverse the md5 hash into the string "TOPPING" (without quotes) (should takes at most 45 seconds)

```console
./start.sh load_balancer 594f2ba0aaeacf6c9510f445a36217cc ../d.txt 3
```

# 2. Phase 2.
You may need to wait a few moments for the partition to arrive to the servers (When it is done the message "[Server] [x] Saved my partition " will show on the server's terminal)
Then, several clients (we recommend 3, not too much, enough to see the benefit) can be launched in separate terminals.

```console
./start.sh client
```


# 3. Example execution sequence (All command on different terminals)

**start 3 servers**

```console
./start.sh server

./start.sh server

./start.sh server
```

**choose the hash to inverse and split dictionary into 3 servers. This example Hash String should be inverted into "TOPPING"**

```console
./start.sh load_balancer 594f2ba0aaeacf6c9510f445a36217cc ../d.txt 3
```

**connect the client/worker to do the computation**

```console
./start.sh client

./start.sh client

./start.sh client
```
