### Intrusion-Tolerant Decentralized Ledger Platform based on a Permissionless Blockchain Solution

## Deploy

No diretório shell, correr o comando

sh deploy.sh <n_proxies(max_proxies==10)> <n_clients(max_clients=10)> <blockmess> <artillery> <n_faults(max_faults=3)>

$1 : número de proxies 
$2 : numero de clientes
$3 : 0 - correr o bft smart, 1 - correr o blockmess
$4 : 0, correr a classe Test do cliente, 1- correr os testes do artillery
$5 : numero de falhas a tolerar, apenas necessário para correr o bft smart

EX: correr o bft-smart para tolerância 1 falha bizantina, com os testes do artillery

sh deploy.sh 4 4 0 1 1 

EX: correr o blockmess com os testes do client

sh deploy.sh 4 4 1 0 

Nota: o blockmess está integrado com o proxy, ao contrário do bft smart que está dividido entre proxy e replica. 

A solução está preparada para correr em docker

# Client
Faz os pedidos ao proxy, responsável pelo POW. Pode correr testes locais para verificar a correção das operações ou receber pedidos do artillery. 

# Proxy
No caso do bft smart, atua como Service Proxy
No caso do blockmess, está todo integrado no próprio proxy. As operações de leitura não são ordenadas, as operações de escrita são async com exepção da get_block_to_mine que precisa de devolver o bloco a minar e por isso necessita de ser sync. Essa operação poderá gerar um novo bloco, caso não haja nenhum bloco por minar e hajam transações suficientes, e como tal é de escrita pois escreve o novo bloco no ledger.

# Replica
Replicas do bft smart, responsáveis pela ordenação e processamento de operações

# Artillery
start_x.yml - ficheiros de arranque do cliente, devem ser corridos antes do workload
workload_x.yml - workload para teste de bftsmart vs blockmess, os pesos de operações de leitura e escrita poderao ser alterados nos ficheiros
mine.yml . workload para testar a mineração de blocos. os primeiros blocos têm dificuldade 0, a partir do 2o bloco a dificuldade aumenta e este workload permite testar o tempo que demora a minar um bloco. Os resultados terão de ser observados nos logs dos containers uma vez que ainda não descobrimos como transferir o ficheiro report do container para o exterior.