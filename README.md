# Intrusion-Tolerant Decentralized Ledger Platform based on a Permissionless Blockchain Solution

Our solution has allows for a decentralized ledger that uses a permissionless blockchain solution for maintaining the correct order of operations. This solution allows for bizantine fault tolerance, and prevents intrusion attacks from all of its components. 
We divide the Platform in several components, with the goal of making it more modular, and also to prepare it for deployment in a Dockerized solution.

## Client
A REST Client, which is used in two modes:
- Operation Correctness testing - the client can be used to test the correctness of the operations. It performs a series of operations and prints its results in the console of the container if using Docker Desktop or by typing, for example:
```docker logs client_0```
- Benchmark Testing - the client can also be used for testing the response time of the operations, taking REST requests from an Artillery component. In this case, the results will be visible on Artillery's log.
Currently, each client sends requests to a single proxy, and this can only be altered in the shell file by changing the logic.

## Artillery
Artillery allows for testing latency of operations. It performs 3 different workloads that have different purposes. The different files are intended for sending tests to the different containers. It is prepared for up to 4 clients.

- start_x.yml - This workloads are intended to get the blockchain going, by creating some accounts, mining the genesis block and performing a simple transaction in order to give money to most of the accounts, so that not to many requests "fail" to execute in the actual workload. The first blocks mined have a challenge of zero because they are not intended to be hard to mine, they serve only to give money to different users. 
- workload_x.yml - This workload serves the purpose of testing the fastness of each different solution used regarding the operations that don't require mining. It allows for comparison between Blockmess and BFT smart. It only performs two operations, a write and a read, and the weight of each operation can be defined in the correspondent yml file (in the future we plan to have a wights csv). We recommend to put the weights in fraction because the examples seen online all used this.
- mine.yml - This workload will test how much time it takes to mine a block in average. It has to be ran after workload_x are ran, in order to have enough transactions to mine.

Ideally, results files for each workload would be generated, but we haven't found a way to obtain the file from the container to the outside world, and therefore they have to be viewed in the console (for the first container of each type of workload, which runs sequentially) or in the respective container's logs, as exemplified in the previous section (for the other containers, which run on the background). 


## Proxy
This component takes requests from the clients also via REST interface, and has different sets of functionalities depending on which bft solution is running. The common trace is that it communicates with the client via TLSv1.3, with server side authentication. The client's truststore and the server's keystore and certificates have been previously generated, but generating new ones is simple and only requires running 
```sh security.sh``` 
on shell directory.
Furthermore, all communications between components are signed (except for communications between artillery and clients). This will allow for prevention of byzantine faults as explained during the next sections.

### BFT Smart 
If running BFT Smart, the proxy behaves simply as the name indicates, serving has the proxy in BFT Smart's logic. If the proxy is byzantine, the replicas can detect it by checking if the signature of the request is correct, as the request is signed by the client. Because the reply is also signed by the replicas and the proxy, the client can detect if either one of them is byzantine. This is what makes this solution intrusion tolerant. All invocations to replicas are done asynchronously. 

> Note: GetBlockToMine is issued as ORDERED_REQUEST because it may require a write to the block to mine. 

### Blockmess
When running Blockmess, due to its nature, the proxy also aggregates the functionalities performed by the replicas in BFT Smart. Therefore, it is a conjugation of what is described above and what is described above and bellow. Reads are not submitted to blockmess as therefore they don't provide any ordering guarantees. Only writes are submitted, asynchronously, except for Get Block To Mine as it requires an answer, and therefore is done synchronously. This feature may still be subject to some improvements.

## Replica
Replicas are only required when running BFT Smart as their functionality is performed by blockmess in the server proxy in that case. BFT smart is used to order the operations, and the replica has the logic behind these operations implemented.

## Redis
Redis is used to guarantee persistency of the ledger.


## Blockchain

Our blockchain starts as a single genesis block without any operations, simply as a starting point. This is generated at the start of the program. Initially, the number of transactions required for a block and the difficulty of mining it is insignificant, in order to get the flow of operations going and give a starting balance to the users and make testing faster. The number of transactions required will augment by two times until it reaches a limit of 8 transactions per block, and the difficulty will go up to it's normal value after a small number of blocks are mined. 

The strategy behind this may also be subject to changes in the future in order to take a more realistic approach, by emitting an initial number of coins that can be awarded to users as they mine blocks, and augmenting the difficulty of mining a block progressively as more of these coins are awarded, in an approach that is more bitcoinish. Right now, there's no initial coins generated, and it is as if a new one is emitted when a block is mined, and is only actually attributed to the user that mined when the next block is mined, as it is equivalent to a normal transaction, but from the ledger to the user. 

The blocks are generated by the ledger, by adding X transactions to it (and verifying its validity while doing so), and the POW is performed by the clients, which try to find a nonce that when hashed with the block's header, has X number of 0 bytes in a row, with X being the difficulty.
The same block is given to every client, and the one who finishes it first is awarded the coins. Soft forks may happen, but are not taken into consideration. 

## Deploy

In order to deploy the solution, simply type the following command in a terminal, in the directory /shell

```sh deploy.sh <n_proxies> <n_clients> <blockmess> <artillery> <n_faults)>```

- $1 is the number of proxies to deploy.  
- $2 is the number of clients to deploy.
- $3 is an integer that acts as a boolean. When 0, runs BFT Smart, and when 1 runs Blockmess.
- $4 is an integer that acts as a boolean. When 0, runs operation correctness tests and when 1, runs artillery tests.
- $5 is the number of faults to tolerate, which will result in the number of replicas to deploy (N=3F+1). Only required if #3 is set to 0

EX: Running Artillery tests on a solution with BFT Smart tolerating one fault (4 replicas), with 4 clients and 4 proxies:  

 ```sh deploy.sh 4 4 0 1 1``` 

EX: Running operation correctness tests on a solution with Blockmess, using 4 proxies and a single replica:

```sh deploy.sh 4 1 1 0``` 


Docker is required to run this solution. 

## Possible improvements

Some improvements weere already mentioned along this document (specially on Blockchain section). Additionally, we intend to add support to:
- Running parts of Send Transaction using Intel SGX technology.
- Allow for recovery of lost replicas in BFT Smart.
- Add an option to run artillery locally and check the results file generated. (done)
- Deploy and test the solution on a Cloud infrastructure. 

