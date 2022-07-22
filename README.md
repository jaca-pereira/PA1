# Intrusion-Tolerant Decentralized Ledger Platform based on a Permissionless Blockchain Solution

Our solution has allows for a decentralized ledger that uses a permissionless blockchain solution for maintaining the correct order of operations. This solution allows for bizantine fault tolerance, and prevents intrusion attacks from all of its components. 
We divide the Platform in several components, with the goal of making it more modular, and also to prepare it for deployment in a Dockerized solution.

## Client
A local REST Client, which is used in two modes:
- Mine Block testing - the client can be used to test how much time it takes to run a mine block operation. It will start the blockchain and will print the time it took to mine a block. 
- Benchmark Testing - the client can also be used for testing the response time of the operations, taking REST requests from an Artillery component. In this case, the results will be visible on Artillery's html files.

## Artillery
Artillery allows for testing latency of operations. It performs 2 different workloads that have different purposes.
- start_0.yml - This workload is intended to get the blockchain started, by creating some accounts, mining the genesis block and performing a simple transaction in order to give money to most of the accounts, so that not to many requests "fail" to execute in the actual workload. The first blocks mined have a challenge of zero because they are not intended to be hard to mine, they serve only to give money to different users. 
- workload_0.yml - This workload serves the purpose of testing the fastness of each different solution used regarding the operations that don't require mining. It only performs two types of operations, a write and a read, and the weight of each operation can be defined in the correspondent yml file. We recommend to put the weights in fraction because the examples seen online all used this.

## Proxy
This component takes requests from the clients also via REST interface, and has different sets of functionalities depending on which bft solution is running. The common trace is that it communicates with the client via TLSv1.3, with server side authentication. The client's truststore and the server's keystore and certificates have been previously generated, but generating new ones is simple and only requires running 
```sh security.sh``` 
on shell directory.
Furthermore, all communications between all components are signed (except for communications between artillery and client). This will allow for prevention of byzantine faults as explained during the next sections.

### BFT Smart 
If running BFT Smart, the proxy behaves simply as the name indicates, serving has the proxy in BFT Smart's logic. If the proxy is byzantine, the replicas can detect it by checking if the signature of the request is correct, as the request is signed by the client. Because the reply is also signed by the replicas and the proxy, the client can detect if either one of them is byzantine. This is what makes this solution intrusion tolerant. All invocations to replicas are done asynchronously. 

> Note: GetBlockToMine is issued as ORDERED_REQUEST because it may require a write to the block to mine. 

### Blockmess
When running Blockmess, due to its nature, the proxy also aggregates the functionalities performed by the replicas in BFT Smart. Therefore, it is a conjugation of what is described above and what is described above and bellow. Reads are not submitted to blockmess and therefore they don't provide any ordering guarantees. Only writes are submitted, asynchronously, except for Get Block To Mine as it requires an answer, and therefore is done synchronously. 

## Replica
Replicas are only required when running BFT Smart as their functionality is performed by blockmess in the server proxy in that case. BFT smart is used to order the operations, and the replica has the logic behind these operations implemented.

## Redis
Redis is used to guarantee persistency of the ledger.


## Blockchain

Our blockchain starts as a single genesis block without any operations, simply as a starting point. This is generated at the start of the program. Initially, the number of transactions required for a block and the difficulty of mining it is insignificant, in order to get the flow of operations going and give a starting balance to the users and make testing faster. The number of transactions required will augment by two times until it reaches a limit of 8 transactions per block, and the difficulty will go up to it's normal value after a small number of blocks are mined. 

The strategy behind could be subject to improvements in order to take a more realistic approach, by emitting an initial number of coins that can be awarded to users as they mine blocks, and augmenting the difficulty of mining a block progressively as more of these coins are awarded, in an approach that is more bitcoinish. Right now, there's no initial coins generated, and it is as if a new one is emitted when a block is mined, and is only actually attributed to the user that mined when the next block is mined, as it is equivalent to a normal transaction, but from the ledger to the user. 

The blocks are generated by the ledger, by adding X transactions to it (and verifying its validity while doing so), and the POW is performed by the clients, which try to find a nonce that when hashed with the block's header, has X number of 0 bytes in a row, with X being the difficulty.
The same block is given to every client, and the one who finishes it first is awarded the coins. Soft forks may happen, but are not taken into consideration. 

## Deploy

In order to deploy the solution, different steps are required. The proxies and the replica's are deployed together in the same machines, while the client and artillery are run locally. The solution used to be more flexible but due to some issues with running it on separate nodes, we lost some of the flexibility. The deploy.sh command has the following structure:

```deploy.sh <client> <address> <id> <nodes_blockmess> <sgx> <test_mine>```

- **client** should be 0 if we are not running a client or 1 if we are.  
- **address** should be the IP address of the replica/proxy we are deploying or, when running a client, the IP address of the proxy we are sending requests to.
- **id** is the ID of the proxy/replica/client we are deploying.
- **nodes_blockmess** should be 0 if we are not running blockmess or 4 if we are
- **sgx should** be 0 if we are not running in sgx mode or 1 if we are. Since SGX is not working properly, no need to change the flag.
- **test_mine** should be 0 if we are running a replica/proxy or running the client in Benchmark testing; or 1 if running the mine block test

For running a replica/proxy pair with BFT SMART, simply run the following commands on their correspondent machine (changing Blockmess flag to 4 if running Blockmess):  
 
```sudo sh deploy.sh 0 141.95.173.56 0 0 0 0```
 
 
```sudo sh deploy.sh 0 54.38.65.236 1 0 0 0```
 
 
```sudo sh deploy.sh 0 192.99.168.235 2 0 0 0```
 
 
```sudo sh deploy.sh 0 54.36.163.65 3 0 0 0```
 
> Note: IP addresses need to be exactly as in the commands because the configurations file matches this ID's to this addresses.
 

For running the client locally


```sh deploy.sh 1 141.95.173.56 0 0 0 0``` 
 

For running artillery:

```sh artillery.sh```
 

Due to some problems with artillery, the operation mine block and the way our client is set up, only one client can be run at a time, and it has to be the one exemplified above. This is a problem that was not present before but wasn't fixed in time.  

## Improvements since last checkpoint
 
- Added support for SGX technology, but not working properly. Container is sconified but when running has errors.
- Recovery of downed replicas in BFT Smart.

