/***
 * Copyright (c) 2009 Caelum - www.caelum.com.br/opensource All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package br.com.caelum.vraptor.serialization.xstream;

import static br.com.caelum.vraptor.serialization.xstream.VRaptorClassMapper.isPrimitive;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import br.com.caelum.vraptor.interceptor.TypeNameExtractor;
import br.com.caelum.vraptor.serialization.ProxyInitializer;
import br.com.caelum.vraptor.serialization.Serializer;
import br.com.caelum.vraptor.serialization.SerializerBuilder;

import com.google.gson.Gson;

/**
 * A SerializerBuilder based on XStream
 * 
 * @author Lucas Cavalcanti
 * @since 3.0.2
 */
public class GsonSerializer implements SerializerBuilder {

	private final Gson gson;

	private final Writer writer;

	private final TypeNameExtractor extractor;

	private final ProxyInitializer initializer;

	private final Serializee serializee = new Serializee();

	private String alias;

	public GsonSerializer(Gson gson, Writer writer, TypeNameExtractor extractor, ProxyInitializer initializer) {
		this.gson = gson;
		this.writer = writer;
		this.extractor = extractor;
		this.initializer = initializer;
	}

	public Serializer exclude(String... names) {
		serializee.excludeAll(names);
		return this;
	}

	private void preConfigure(Object obj, String alias) {
		checkNotNull(obj, "You can't serialize null objects");

		serializee.setRootClass(initializer.getActualClass(obj));
		if (alias == null) {
			alias = extractor.nameFor(serializee.getRootClass());
		}

		setRoot(obj);

		setAlias(obj, alias);
	}

	private void setRoot(Object obj) {
		if (Collection.class.isInstance(obj)) {
			this.serializee.setRoot(normalizeList(obj));
		} else {
			this.serializee.setRoot(obj);
		}
	}

	@SuppressWarnings("unchecked")
	private Collection<Object> normalizeList(Object obj) {
		Collection<Object> list;
		list = (Collection<Object>) obj;
		serializee.setElementTypes(findElementTypes(list));

		return list;
	}

	private void setAlias(Object obj, String alias) {
		// TODO verificar comportamento original. Collections e etc
		this.alias = alias;
	}

	public <T> Serializer from(T object, String alias) {
		preConfigure(object, alias);
		return this;
	}

	public <T> Serializer from(T object) {
		preConfigure(object, null);
		return this;
	}

	private Set<Class<?>> findElementTypes(Collection<Object> list) {
		Set<Class<?>> set = new HashSet<Class<?>>();
		for (Object element : list) {
			if (element != null && !isPrimitive(element.getClass())) {
				set.add(initializer.getActualClass(element));
			}
		}
		return set;
	}

	public Serializer include(String... fields) {
		serializee.includeAll(fields);
		return this;
	}

	public void serialize() {
		try {
			Object root = serializee.getRoot();

			if (alias != null) {

				// TODO FAZER O GSON PROCESSAR O OBJETO E DEPOIS JOGAR A STRING
				// NO
				// VALUE DO MAP. ASSIM AS ANOTAÇÕES DO GSON FUNCIONAM

				Map<String, Object> tree = new HashMap<>();
				tree.put(alias, root);
				writer.write(gson.toJson(tree));
			} else {
				writer.write(gson.toJson(root));
			}

			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Serializer recursive() {
		this.serializee.setRecursive(true);
		return this;
	}
}