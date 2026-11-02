package dad.dataprovider.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_alternativas_quiz")
public class AlternativaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_alternativa")
  public Integer id;

  @Column(name = "pergunta_id")
  public Integer perguntaId;

  @Column(name = "texto_alternativa")
  public String texto;

  @Column(name = "correta")
  public boolean correta;
}
