package dad.dataprovider.mapper;

import java.time.OffsetDateTime;

import org.springframework.stereotype.Component;

import dad.core.domain.CriarUsuario;
import dad.core.domain.EditarUsuario;
import dad.core.domain.Endereco;
import dad.core.domain.Usuario;
import dad.dataprovider.entity.EnderecoEntity;
import dad.dataprovider.entity.UsuarioEntity;

@Component
public class UsuarioPersistenceMapper {

  public Usuario toDomain(
      UsuarioEntity entity,
      EnderecoEntity endereco,
      String tipo,
      Integer condominioId) {
    return new Usuario(
        entity.id,
        entity.nome,
        entity.email,
        entity.tipoUsuarioId,
        tipo,
        entity.ativo,
        entity.dataNascimento,
        entity.cpf,
        entity.avatar,
        entity.registroEm,
        toDomain(endereco),
        condominioId);
  }

  public UsuarioEntity toNovaEntity(
      CriarUsuario comando,
      String senhaHash,
      Integer enderecoId) {
    var entity = new UsuarioEntity();

    entity.nome = comando.nome();
    entity.email = comando.email();
    entity.senhaHash = senhaHash;
    entity.tipoUsuarioId = comando.tipoUsuarioId();
    entity.dataNascimento = comando.dataNascimento();
    entity.cpf = comando.cpf();
    entity.avatar = comando.avatar();
    entity.enderecoId = enderecoId;
    entity.ativo = true;
    entity.registroEm = OffsetDateTime.now();

    return entity;
  }

  public void atualizar(
      UsuarioEntity entity,
      EditarUsuario comando) {
    entity.nome = comando.nome();
    entity.email = comando.email();
    entity.dataNascimento = comando.dataNascimento();
    entity.avatar = comando.avatar();
  }

  public Endereco toDomain(EnderecoEntity entity) {
    return new Endereco(
        entity.cep,
        entity.uf,
        entity.cidade,
        entity.logradouro,
        entity.numero,
        entity.complemento);
  }

  public EnderecoEntity toEntity(Endereco endereco) {
    var entity = new EnderecoEntity();

    entity.cep = endereco.cep();
    entity.uf = endereco.uf();
    entity.cidade = endereco.cidade();
    entity.logradouro = endereco.logradouro();
    entity.numero = endereco.numero();
    entity.complemento = endereco.complemento();

    return entity;
  }
}