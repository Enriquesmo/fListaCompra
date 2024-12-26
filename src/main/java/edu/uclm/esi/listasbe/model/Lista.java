package edu.uclm.esi.listasbe.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;

@Entity
public class Lista {
	@Id @Column(length = 36)
	private String id;
	@Column(length = 80)
	private String nombre;
	@Column (length = 80)
	private String creador;
	
	@OneToMany (mappedBy = "lista")
	private List<Producto> productos;
	
	
	@ElementCollection
	private List<String> emailsUsuarios;
	
	
	public Lista() {
		this.id = UUID.randomUUID().toString();
		this.productos = new ArrayList<>();
		this.emailsUsuarios = new ArrayList<>();
	}
	

	public String getCreador() {
		return creador;
	}


	public void setCreador(String creador) {
		this.creador = creador;
	}


	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public void add(Producto producto) {
		this.productos.add(producto);
	}
	
	public List<Producto> getProductos() {
		return productos;
	}
	public void setProductos(List<Producto> productos) {
		this.productos = productos;
	}

	public List<String> getEmailsUsuarios() {
		return emailsUsuarios;
	}

	public void setEmailsUsuarios(List<String> emailsUsuarios) {
		this.emailsUsuarios = emailsUsuarios;
	}


	public void addEmailUsuario(String email) {
		this.emailsUsuarios.add(email);
		
	}
	
	
}
