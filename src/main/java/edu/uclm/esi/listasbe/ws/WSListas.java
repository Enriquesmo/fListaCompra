package edu.uclm.esi.listasbe.ws;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import edu.uclm.esi.listasbe.dao.ListaDao;
import edu.uclm.esi.listasbe.model.Lista;
import edu.uclm.esi.listasbe.model.Producto;

@Component
public class WSListas extends TextWebSocketHandler {
	
	@Autowired
	private static ListaDao listaDao;

	private Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
	
	
	@Autowired
	public static void setListaDao(ListaDao listaDao) {
		WSListas.listaDao = listaDao;
	}
	
	private Map<String, List<WebSocketSession>> sessionsByIdLista = new ConcurrentHashMap<>();
	
	public void notificar(String idLista, Producto producto) {		
		JSONObject jso = new JSONObject();
		jso.put("tipo","actualizacionDeLista");
		jso.put("idLista", "idLista");
		jso.put("unidadesCompradas", producto.getUnidadesCompradas());
		jso.put("unidadesPedidas", producto.getUnidadesPedidas());
		TextMessage message = new TextMessage(jso.toString());
		
		
		List<WebSocketSession> interesados=this.sessionsByIdLista.get(idLista);
//		for (WebSocketSession target : interesados) {
//			new Thread (new Runable() {
//				@Override
//				public void run() {
//					try {
//						
//					} catch (IOException e)
//				}
//			})
//		}
	}
	
	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		System.out.println(session.getId());
		String email = this.getParameter(session,"email");
		List<String> listas = this.listaDao.getListasDe(email);
		for (String idLista : listas) {
			List<WebSocketSession> auxi = this.sessionsByIdLista.get(idLista);
			if (auxi==null) {
				auxi= new ArrayList<>();
				auxi.add(session);
			} else {
				auxi.add(session);
			}
			this.sessionsByIdLista.put(idLista, auxi);
		}
	}
	
	
	
	private String getParameter(WebSocketSession session, String parName) {
		URI uri =session.getUri();
		String query = uri.getQuery();
		for (String param : query.split("&")) {
			String[] pair = param.split("=");
			if (pair.length > 1 && parName.equals(pair[0])) {
				return pair[1];
			}
		}
		return null;
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		this.sessions.remove(session.getId());
	}

	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
	}


}