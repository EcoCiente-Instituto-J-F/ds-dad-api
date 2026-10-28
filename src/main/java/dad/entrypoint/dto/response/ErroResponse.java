package dad.entrypoint.dto.response;

import java.time.Instant;
import java.util.Map;

public record ErroResponse(
    Instant instante,
    int status,
    String mensagem,
    Map<String, String> campos
) {
}