package edu.uclm.esi.listasbe.dao;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import edu.uclm.esi.listasbe.model.Lista;
import jakarta.transaction.Transactional;

public interface ListaDao extends CrudRepository<Lista, String>{
	
	//Sirve para realizar peticiones personalizadas o no a la BBDD
	
	@Query(value="select lista_id from lista_emails_usuarios where emails_usuarios=:email", nativeQuery=true)
	List<String> getListasDe(String email);

    @Modifying
    @Transactional
    @Query(value = "UPDATE lista_emails_usuarios SET confirmado = TRUE WHERE lista_id = :idLista AND emails_usuarios = :email", nativeQuery = true)
    void confirmar(String idLista, String email);
    //vip
    @Query(value = """
            SELECT u.vip
            FROM usuario u
            WHERE u.email = :emailUsuario
        """, nativeQuery = true)
    Boolean esUsuarioVip(String emailUsuario);
    //vipDate
    @Query(value = """
            SELECT u.vip_fecha
            FROM usuario u
            WHERE u.email = :emailUsuario
        """, nativeQuery = true)
    LocalDateTime VipDate(String emailUsuario);  
    //quitar vip
    @Modifying
    @Query(value = """
            UPDATE usuario u
            SET u.vip = false
            WHERE u.email = :emailUsuario
        """, nativeQuery = true)
    void desactivarVip(String emailUsuario);
    
 // Devuelve el número de listas asociadas a un usuario
    @Query(value = "SELECT COUNT(*) " +
                   "FROM lista_emails_usuarios leu " +
                   "WHERE leu.emails_usuarios = :emailUsuario",
           nativeQuery = true)
    int contarListasDeUsuario(String emailUsuario);

    // Verificar si un token existe en la tabla 'usuario'
    @Query(value = "SELECT COUNT(*) FROM usuario WHERE cookie = :token", nativeQuery = true)
    long contarToken(String token);

    default boolean existeToken(String token) {
        return contarToken(token) > 0;
    }
    
//borra user de listas
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM lista_emails_usuarios WHERE lista_id = :idLista AND emails_usuarios = :email", nativeQuery = true)
    void eliminarUsuarioDeLista(String idLista, String email);

}