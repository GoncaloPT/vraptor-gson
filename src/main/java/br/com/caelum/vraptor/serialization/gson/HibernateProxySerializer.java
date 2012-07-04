package br.com.caelum.vraptor.serialization.gson;

import java.lang.reflect.Type;

import org.hibernate.proxy.HibernateProxy;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class HibernateProxySerializer implements JsonSerializer<HibernateProxy> {

	private Gson gson;

	public HibernateProxySerializer() {
		gson = new Gson();
	}

	@Override
	public JsonElement serialize(HibernateProxy proxyObj, Type arg1, JsonSerializationContext arg2) {
		try {
			Object deProxied = proxyObj.getHibernateLazyInitializer().getImplementation();
			return gson.toJsonTree(deProxied);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}