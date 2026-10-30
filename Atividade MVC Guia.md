## SITUAÇÃO

A cantina da escola pediu uma API para controlar os produtos que vende. Uma IA devolveu o código (está mais abaixo, nesta atividade) e ele sobe sem erro. Mas subir sem erro não quer dizer que está certo.

Seu trabalho: descobrir o que a API faz de errado, consertar e explicar o que encontrou.

## REGRAS DA CANTINA

- Todo produto tem código, nome, preço e estoque.
- O código é único: não pode haver dois produtos com o mesmo código.
- Código e nome são obrigatórios. O preço precisa ser maior que zero e o estoque não pode ser negativo.
- Vender tira uma unidade do estoque. Produto com estoque zero não pode ser vendido.
- Só pode ser excluído um produto com estoque zero.
- Pedir, vender ou excluir um produto que não existe é "não encontrado".

## COMO PREPARAR

1. Abra o projeto Cantina (já vem com Spring, H2 e as dependências configurados, mas sem nenhuma classe). Se ainda não rodou, rode uma vez com internet, porque o Maven baixa as dependências na primeira vez.
2. Dentro de src/main/java/escola, crie cinco pastas (pacotes): model, repository, service, controller e exception.
3. Crie os 6 arquivos da seção "O CÓDIGO DA IA", cada um na sua pasta, e copie o código.
4. Rode o projeto:  mvnw.cmd spring-boot:run

A API fica em http://localhost:8080. O banco é H2 em memória e começa vazio toda vez que a aplicação sobe: pare (Ctrl+C) e suba de novo para recomeçar o roteiro do zero.

### Rotas:
  POST   /produtos              cadastra um produto (JSON no corpo)
  GET    /produtos              lista todos
  GET    /produtos/{id}         busca um produto
  POST   /produtos/{id}/vender  vende uma unidade
  DELETE /produtos/{id}         exclui um produto

Exemplo de corpo para cadastrar:
  {"codigo": "P-01", "nome": "Suco", "preco": 5.50, "estoque": 1}

Faça os pedidos no programa que preferir (Postman, Insomnia, curl, arquivo .http). Se o JSON tiver acento e a API recusar com 400 num passo que devia dar certo, teste sem acento.


# O CÓDIGO DA IA

## exception/RegraDeNegocioException.java

```java
package escola.exception;

public class RegraDeNegocioException extends RuntimeException {

    public RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }
}
```

## exception/ProdutoNaoEncontradoException.java

```java
package escola.exception;

public class ProdutoNaoEncontradoException extends RuntimeException {

    public ProdutoNaoEncontradoException(Long id) {
        super("Produto nao encontrado: " + id);
    }
}
```

## model/Produto.java

```java
package escola.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String codigo;

    @Column(nullable = false)
    private String nome;

    private double preco;

    private int estoque;

    public Produto() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public double getPreco() { return preco; }
    public void setPreco(double preco) { this.preco = preco; }

    public int getEstoque() { return estoque; }
    public void setEstoque(int estoque) { this.estoque = estoque; }
}
```

## repository/ProdutoRepository.java ##

```java
package escola.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import escola.model.Produto;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    boolean existsByCodigo(String codigo);
}

## service/ProdutoService.java ##

package escola.service;

import java.util.List;

import org.springframework.stereotype.Service;

import escola.exception.RegraDeNegocioException;
import escola.model.Produto;
import escola.repository.ProdutoRepository;

@Service
public class ProdutoService {

    private final ProdutoRepository repository;

    public ProdutoService(ProdutoRepository repository) {
        this.repository = repository;
    }

    public List<Produto> listar() {
        return repository.findAll();
    }

    public Produto buscar(Long id) {
        return repository.findById(id).orElse(null);
    }

    public Produto cadastrar(Produto produto) {
        return repository.save(produto);
    }

    public void excluir(Long id) {
        Produto produto = buscar(id);
        if (produto.getEstoque() > 0) {
            throw new RegraDeNegocioException("Produto com estoque nao pode ser excluido");
        }
        repository.delete(produto);
    }
}
```

## controller/ProdutoController.java ##

```java
package escola.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import escola.exception.ProdutoNaoEncontradoException;
import escola.exception.RegraDeNegocioException;
import escola.model.Produto;
import escola.repository.ProdutoRepository;
import escola.service.ProdutoService;

@RestController
@RequestMapping("/produtos")
public class ProdutoController {

    private final ProdutoService service;
    private final ProdutoRepository repository;

    public ProdutoController(ProdutoService service, ProdutoRepository repository) {
        this.service = service;
        this.repository = repository;
    }

    @GetMapping
    public List<Produto> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public Produto buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    public ResponseEntity<Produto> cadastrar(@RequestBody Produto produto) {
        return ResponseEntity.ok(service.cadastrar(produto));
    }

    @PostMapping("/{id}/vender")
    public ResponseEntity<?> vender(@PathVariable Long id) {
        Produto produto = repository.findById(id).get();
        if (produto.getEstoque() == 0) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Produto sem estoque");
        }
        produto.setEstoque(produto.getEstoque() - 1);
        repository.save(produto);
        return ResponseEntity.ok(produto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(ProdutoNaoEncontradoException.class)
    public ResponseEntity<Void> naoEncontrado() {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(RegraDeNegocioException.class)
    public ResponseEntity<String> conflito(RegraDeNegocioException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }
}

```