package dad.dataprovider.entity;

import java.time.OffsetDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "tb_agendamentos_coletas")
public class ColetaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_agendamento_coleta")
  public Integer id;

  @Column(name = "condominio_id")
  public Integer condominioId;

  @Column(name = "cooperativa_id")
  public Integer cooperativaId;

  @Column(name = "status_agendamento_id")
  public Integer statusId;

  @Column(name = "data_inicio")
  public OffsetDateTime dataInicio;

  @Column(name = "data_fim")
  public OffsetDateTime dataFim;

  @Column(name = "possui_recorrencia")
  public boolean recorrente;
}