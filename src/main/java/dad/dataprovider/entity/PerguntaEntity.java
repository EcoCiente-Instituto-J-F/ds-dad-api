package dad.dataprovider.entity;

import jakarta.persistence.*;

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