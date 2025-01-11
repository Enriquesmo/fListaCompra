package edu.uclm.esi.listasbe.dao;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import edu.uclm.esi.listasbe.model.Lista;
import edu.uclm.esi.listasbe.model.Producto;
import jakarta.transaction.Transactional;

public interface ProductoDao extends CrudRepository<Producto,String>{
	@Transactional
	@Modifying
	@Query("DELETE FROM Producto p WHERE p.lista.id = :listaId")
	void deleteByListaId(@Param("listaId") String listaId);
}
