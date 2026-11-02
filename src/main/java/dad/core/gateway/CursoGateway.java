package dad.core.gateway;

import dad.core.domain.Curso;
import dad.core.domain.Pagina;
import java.util.Optional;

public interface CursoGateway {

  Pagina<Curso> listar(String busca, int pagina, int tamanho);

  Optional<Curso> buscarPorId(Integer id);

  Curso criar(Curso curso);

  Curso atualizar(Curso curso);
}
