package dad.entrypoint.controller;

import dad.core.service.ApoioService;
import dad.entrypoint.dto.response.CatalogosResponse;
import dad.entrypoint.dto.response.DashboardResponse;
import dad.entrypoint.mapper.ApoioMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/v1")
public class ApoioController {

  private final ApoioService service;
  private final ApoioMapper mapper;

  public ApoioController(ApoioService service, ApoioMapper mapper) {
    this.service = service;
    this.mapper = mapper;
  }

  @GetMapping("/dashboard")
  public DashboardResponse dashboard(@RequestParam(defaultValue = "6") int meses) {
    return mapper.toResponse(service.buscarDashboard(meses));
  }

  @GetMapping("/catalogos")
  public CatalogosResponse catalogos() {
    return mapper.toResponse(service.buscarCatalogos());
  }
}
