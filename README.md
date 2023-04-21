# ProjetoSD

Projeto realizado por David Silva e Eduardo Tanqueiro no âmbito da cadeira de sistemas distribuídos.

## Primeira meta:
Componentes desenvolvidas:
- SearchModule (tem como objetivo servir de “ponte” entre os diversos componentes)
- Downloader (tem como principal objetivo analisar os URLs e enviar a informação recolhida por Multicast para os Barrels)
- Index Storage Barrels (guarda o índice invertido dos urls, isto é, para cada termo guarda os urls em que esse termo aparece)
- Client (serve para um cliente fazer pesquisas e ver estatísticas do sistema, comunica-se apenas com o SearchModule )

As componentes desenvolvidas comunicam-se essencialmente através de RMI, sockets Multicast e UDP.