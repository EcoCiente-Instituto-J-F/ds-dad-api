package dad.dataprovider.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_perguntas_quiz")
public class PerguntaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_pergunta")
  public Integer id;

  @Column(name = "quiz_id")
  public Integer quizId;

  @Column(name = "enunciado", columnDefinition = "text")
  public String enunciado;

  @Column(name = "ordem")
  public Integer ordem;
}
