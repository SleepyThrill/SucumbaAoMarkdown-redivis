package escola.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import escola.exception.ProdutoNaoEncontradoException;
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
        return repository.findById(id)
                .orElseThrow(() -> new ProdutoNaoEncontradoException(id));
    }

    public Produto cadastrar(Produto produto) {
        if (repository.existsByCodigo(produto.getCodigo())) {
            throw new RegraDeNegocioException("Codigo de produto ja cadastrado");
        }
        // POST sempre cria um novo registro, mesmo se vier um id no JSON.
        produto.setId(null);
        return repository.save(produto);
    }

    @Transactional
    public Produto vender(Long id) {
        Produto produto = buscar(id);
        if (produto.getEstoque() <= 0) {
            throw new RegraDeNegocioException("Produto sem estoque");
        }
        produto.setEstoque(produto.getEstoque() - 1);
        return repository.save(produto);
    }

    @Transactional
    public void excluir(Long id) {
        Produto produto = buscar(id);
        if (produto.getEstoque() > 0) {
            throw new RegraDeNegocioException("Produto com estoque nao pode ser excluido");
        }
        repository.delete(produto);
    }
}
