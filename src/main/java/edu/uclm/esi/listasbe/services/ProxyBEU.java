package edu.uclm.esi.listasbe.services;

import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.protocol.BasicHttpContext;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PutMapping;


import edu.uclm.esi.listasbe.dao.ListaDao;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class ProxyBEU {
	@Autowired
	private ListaDao listaDao;
	public boolean validar3(String token) {
		String url = "http://localhost:9000/tokens/validar";
		
		try (CloseableHttpClient httpClient = HttpClients.createDefault()){
			HttpPut httpPut = new HttpPut(url);
			httpPut.setEntity(new StringEntity(token));
			httpPut.setHeader("Content-Type", "text/plain");
			HttpContext context = new BasicHttpContext();
			try (CloseableHttpResponse response = httpClient.execute(httpPut,context)) {
				System.out.println("Response Status:" + response.getCode());
				return response.getCode()==200;
			}
		} catch (Exception e) {
			return false;
		}
	}
	
	
	public boolean validar(String Token) {
		boolean exist = this.listaDao.existeToken(Token);
		if (exist) {
            return true; // Sesión válida
        }else {
        	return false;
        }
	}

	
	public String findCookie(HttpServletRequest request, String cookieName) {
		Cookie[] cookies = request.getCookies();
		if (cookies== null) {
			return null;
		}
		for (int i=0; i<cookies.length; i++) {
			if(cookies[i].getName().equals(cookieName))
				return cookies[i].getValue();
		}
		return null;
	}

}
