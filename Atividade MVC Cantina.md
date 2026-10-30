# ATIVIDADE: API EM MVC, A CANTINA DA ESCOLA

# PARTE 1: RODE O ROTEIRO

Com o banco vazio, faça os pedidos na ordem e anote o status que a API realmente devolveu. O primeiro produto cadastrado recebe o id 1. Preencha "antes" agora e "depois" quando terminar a Parte 3.

 1. Cadastrar o produto P-01, Suco, preço 5.50, estoque 1
    - Esperado: 201
    - Deu antes: 200    
    - Deu depois: 201 

 2. Cadastrar outro produto com o mesmo código P-01
    - Esperado: 409
    - Deu antes: 500    
    - Deu depois:  409

 3. Cadastrar um produto sem nome (código P-02, preço 3.00, estoque 4)
    - Esperado: 400
    - Deu antes: 200    
    - Deu depois: 400

 4. Cadastrar um produto com preço 0 (código P-03, nome Bala, estoque 4)
    - Esperado: 400
    - Deu antes: 200    
    - Deu depois: 400

 5. Excluir o produto 1 (que tem estoque 1)
    - Esperado: 409
    - Deu antes: 409    
    - Deu depois: 409

 6. Vender o produto 1
    - Esperado: 200
    - Deu antes: 200
    - Deu depois: 200

 7. Vender o produto 1 de novo (agora com estoque 0)
    - Esperado: 409
    - Deu antes: 409    
    - Deu depois: 409

 8. Excluir o produto 1
    - Esperado: 204
    - Deu antes: 200    
    - Deu depois: 204

 9. Buscar o produto 1
    - Esperado: 404
    - Deu antes: 200    
    - Deu depois: 404

 10.  Vender o produto 99, que não existe
    - Esperado: 404
    - Deu antes: 500
    - Deu depois: 404

- Quantos passos NÃO bateram com o esperado? 
  - 7


# PARTE 2: ACHE O PORQUÊ 

Para cada passo que não bateu, descubra em qual camada está o problema e o que está errado. Leia os arquivos model, service e controller com calma. Uma linha por passo.

## Passo 1
- Camada: Controller
- Arquivo e trecho: ProdutoController.java  
```java
return ResponseEntity.ok(service.cadastrar(produto));
```
- O que está errado: deve retornar .created ao invés de .ok

## Passo 2
- Camada: Service
- Arquivo e trecho: ProdutoService.java
```java
return repository.save(produto);
```
- O que está errado: Não possui validação e ignora caso tenha um produto já existente

## Passo 3
- Camada: Model
- Arquivo e trecho: Produto.java
```java
@Column(nullable = false)
```
- O que está errado: Nullable = false ainda permite valor vazio

## Passo 4
- Camada: Model
- Arquivo e trecho: Produto.java
```java
private double preco;
```
- O que está errado: Aceita valor nulo, negativo e zero

## Passo 8
- Camada: Controller
- Arquivo e trecho: ProdutoController.java
```java
return ResponseEntity.ok().build();
```
- O que está errado: retorna 200 ao invés de 204

## Passo 9
- Camada: Service
- Arquivo e trecho: ProdutoService.java
```java
repository.findById(id).orElse(null);
```
- O que está errado: não retorna a exceção ProdutoNaoEncontrado, e sim null

## Passo 10
- Camada: Controller
- Arquivo e trecho: ProdutoController.java
```java
repository.findById(id).get();
```
- O que está errado: lança um erro indevido quando não há produto (500)


# PARTE 3: CONSERTE

Conserte o código até os 10 passos devolverem o status esperado. Depois rode o roteiro de novo (com o banco vazio) e preencha o "Deu depois" da Parte 1.

[X] Os 10 passos bateram com o esperado
[X] Cada problema foi corrigido na camada onde ele mora, não escondido em outra
[X] O projeto continua subindo sem erro

O que você mudou em cada arquivo:

- Arquivo: Produto.java 
  - Mudou: adição de @NotBlank em alguns atributos, @Positive no preço e @Min(0) em estoque
- Arquivo: ProdutoSerivce.java      
  - Mudou: validações para produto inexistente, checagem de estoque
- Arquivo: ProdutoController.java
  - Mudou: agora vai ao service ao invés do repo diretamente, correção de códigos http errados


# PARTE 4: O ROTEIRO NÃO PEGA TUDO

Um roteiro só mostra o que ele testa. Faça mais um pedido: com o banco vazio, excluir o produto 99, que não existe.

1. Que status a API devolve? 500
2. Qual deveria ser? 404

3. Mesmo com os 10 passos corretos, o código pode ter problemas de arquitetura. Olhe o ProdutoController: ele fala com alguma camada com a qual não deveria? Qual, e por quê?
Resposta: Acessava repository diretamente ao invés do service, tornando o service essencialmente inútil.

1. Onde está a regra "produto sem estoque não pode ser vendido"? Está na camada certa? Se não, mova e rode o roteiro de novo.
Resposta: A regra estava em controller, ao invés de service.


# PARTE 5: PERGUNTAS

Responda com suas palavras.

1. O Controller devolveu 200 quando o produto não existia. Quem deveria avisar que o produto não existe, e quem deveria transformar esse aviso em 404?
Resposta: O service que deveria avisar que o produto não existe, depois o controller retorna 404

2. Se a cantina ganhar um aplicativo de celular que também vende produtos, que parte do código você reaproveita sem mudar nada? Por quê?
Resposta: O model e o service. Eles não precisam ser modificados para outras plataformas, enquanto o controller geralmente precisa.

3. Qual é a diferença entre 400 e 409? Dê um exemplo de cada na cantina.
Resposta: Um erro 400 é um erro geral que significa que o dado que o usuário enviou está errado, enquanto o 409 significa que os dados estão certos mas as condições estão erradas, dando conflito com algo. Um exemplo disso é tentar excluir um produto que ainda tem estoque na API.

4. A API subiu sem erro, mas vários passos do roteiro estavam errados. O que prova que uma API está certa?
Resposta: Os códigos HTTP certos em cada ocasião são os que provam que uma API está configurada corretamente.


O QUE ENTREGAR

- Este arquivo, com as respostas preenchidas.
- A pasta do projeto com o código consertado, sem a pasta target.
