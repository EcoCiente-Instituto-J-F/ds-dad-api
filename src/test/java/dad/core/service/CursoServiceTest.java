package dad.core.service;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dad.core.domain.Aula;
import dad.core.domain.Curso;
import dad.core.exception.RecursoNaoEncontradoException;
import dad.core.exception.RegraNegocioException;
import dad.core.gateway.CursoGateway;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CursoServiceTest {

    @Mock
    private CursoGateway gateway;

    @InjectMocks
    private CursoService service;

    @Test
    void deveBloquearTituloVazio() {
        var curso = new Curso(
            null,
            " ",
            "Descrição",
            true,
            List.of()
        );

        assertThrows(
            RegraNegocioException.class,
            () -> service.criar(curso)
        );

        verifyNoInteractions(gateway);
    }

    @Test
    void deveBloquearDescricaoVazia() {
        var curso = new Curso(
            null,
            "Curso",
            "",
            true,
            List.of()
        );

        assertThrows(
            RegraNegocioException.class,
            () -> service.criar(curso)
        );

        verifyNoInteractions(gateway);
    }

    @Test
    void deveBloquearAulaSemConteudo() {
        var curso = new Curso(
            null,
            "Curso",
            "Descrição",
            true,
            List.of(
                new Aula(
                    null,
                    null,
                    "Aula",
                    " ",
                    1
                )
            )
        );

        assertThrows(
            RegraNegocioException.class,
            () -> service.criar(curso)
        );

        verifyNoInteractions(gateway);
    }

    @Test
    void deveBloquearListaDeAulasNula() {
        var curso = new Curso(
            null,
            "Curso",
            "Descrição",
            true,
            null
        );

        assertThrows(
            RegraNegocioException.class,
            () -> service.atualizar(curso)
        );

        verifyNoInteractions(gateway);
    }

    @Test
    void devePermitirCursoSemAulas() {
        var curso = new Curso(
            null,
            "Curso",
            "Descrição",
            true,
            List.of()
        );

        when(gateway.criar(curso)).thenReturn(curso);

        assertEquals(
            curso,
            service.criar(curso)
        );

        verify(gateway).criar(curso);
    }

    @Test
    void deveInformarCursoInexistente() {
        when(gateway.buscarPorId(99))
            .thenReturn(Optional.empty());

        assertThrows(
            RecursoNaoEncontradoException.class,
            () -> service.buscar(99)
        );
    }

    @ParameterizedTest
    @CsvSource({
        "-1, 20",
        "0, 0",
        "0, 101"
    })
    void deveBloquearPaginacaoInvalida(
        int pagina,
        int tamanho
    ) {
        assertThrows(
            RegraNegocioException.class,
            () -> service.listar("", pagina, tamanho)
        );

        verifyNoInteractions(gateway);
    }
}