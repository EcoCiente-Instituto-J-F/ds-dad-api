package dad.dataprovider.entity;

import java.time.OffsetDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "tb_visitas_coletas")
public class VisitaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_visita_coleta")
  public Integer id;

  @Column(name = "agendamento_coleta_id")
  public Integer coletaId;

  @Column(name = "data_visita")
  public OffsetDateTime dataVisita;

  @Column(name = "foi_realizada")
  public boolean realizada;

  @Column(name = "houve_confirmacao")
  public boolean confirmada;

  @Column(name = "confirmado_em")
  public OffsetDateTime confirmadoEm;

  @Column(name = "observacao", columnDefinition = "text")
  public String observacao;
}