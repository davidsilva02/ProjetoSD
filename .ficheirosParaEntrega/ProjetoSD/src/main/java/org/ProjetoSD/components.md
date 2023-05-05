# Search Module
- Componente RMI
- Tem fila de urls, utiliza-se LinkingBlockingDeque, quando o url é introduzido pelo cliente é colocado na cabeça da fila, quando o link é "descoberto" pelo Downloader é adicionado na cauda
- Tem HashMap com registo dos usuários
- Tem HashMap com o registo do system
- Tem CopyOnWriteArrayList para guardar as pesquisas feitas
- Tem CopyOnWriteArrayList para guardar os barrels (o barrel envia a sua interface e o SearchModule guarda-a) que se ligam, de forma a puder fazer pesquisas num barrel aleatorio.
# Downloader
    TROCAR ENTAO PARA TCP
  - Contem um ConcurrentHashSet com a lista de urls visitados, assim, caso o URL já tenha sido visitado não se faz o CRAWL.
  - Cria um número definido de threads que fazem o CRAWL, guarda na classe JSOUPData, o titulo, o url, uma citacao, os termos que encontrou e os urls que encontrou e adiciona na LinkedBlockingQueue (que está na thread main Downloader) que funciona como uma "fila de mensagens", também adiciona na lista de URLs, os urls que encontrou de forma a fazer o crawl a esses URLs 
  - No inicio, também cria a thread MultiscastSender que vai buscar à "fila de mensagens" a classe JSOUP Data a enviar, e envia por multicast para os barrels.


# Protocolo MULTICAST
- Multicast usado para transferir informação do Downloader para os diferentes barrels ativos
- A thread MulticastSender presente nos Downloaders, vai buscar então a classe a enviar à fila de mensagens
- Existem 2 funcionalidades principais para enviar a info para o barrel, essencialmente, o envio do tamanho da classe a enviar (aqui, de forma a distinguir o tipo de dados que estamos a enviar, marcamos o primeiro byte do array a 0 e depois o barrel só lê do 1 para a frente) e a classe que enviamos propriamente dita ((aqui, de forma a distinguir o tipo de dados que estamos a enviar, marcamos o primeiro byte do array a 0 e depois o barrel só lê do 1 para a frente, e faz a respetiva desliriazação)
- De forma, ao Downloader saber que todos os Barrels cada barrel envia um ACK para o SearchModule, este ACK contém o hash do barrel, guardamos num HashSet de inteiros, quando o tamanho do HashSet for igual ao tamanho de barrels ativos, já sabemos que recebemos todas as confirmações, e portanto, podemos continuar o nosso processo.
- Caso, o tamanho do HashSet não coincida com o numero de confirmações esperadas, fazemos uma nova tentativa, caso não responda novamente, consideramos que o barrel ja esta desligado, e removemos do array de barrels que está no SearchModule
# Barrels
- Ao iniciar, enviam a sua interface para o SearchModule e assim o SearchModule consegue fazer pedidos ao barrel via RMI
- Contém dois ConcurrentHashMap que guardam a informação recolhida pelos Downloaders,um tem como chave um termo e valor um HashSet de classes infoURL (contém toda a informação do URL: titulo, citacao, url,e lista de urls que fazem referencia ao url), e outro que tem como chave o URL em string e a classe infoURL correspondente a esse URL.
- Recebe informações do Downloader, utilizando o protocolo Multicast definido anteriormente e trata essa info de forma a adiciona-la aos HashMaps referidos anteriormente
- Contém também métodos que são chamados pelo SearchModule via RMI de forma a fazer pesquisas por termos ...
# Client
- Conecta-se ao barrel e consegue fazer todas as funcionalidades fazendo pedidos pelo SearchModule 
