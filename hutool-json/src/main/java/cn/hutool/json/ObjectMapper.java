package cn.hutool.json;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.ArrayIter;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.lang.Filter;
import cn.hutool.core.lang.mutable.Mutable;
import cn.hutool.core.lang.mutable.MutablePair;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.TypeUtil;
import cn.hutool.json.serialize.GlobalSerializeMapping;
import cn.hutool.json.serialize.JSONObjectSerializer;
import cn.hutool.json.serialize.JSONSerializer;

import java.io.InputStream;
import java.io.Reader;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * 
 */
public class ObjectMapper {

	/**
	 * 
	 * @return ObjectMapper
	 */
	public static ObjectMapper of(Object source) {
		return new ObjectMapper(source);
	}

	private final Object source;

	/**
	 * 
	 */
	public ObjectMapper(Object source) {
		this.source = source;
	}

	/**
	 *
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	public void map(JSONObject jsonObject, Filter<MutablePair<String, Object>> filter) {
		final Object source = this.source;
		if (null == source) {
			return;
		}

		// 
		final JSONSerializer serializer = GlobalSerializeMapping.getSerializer(source.getClass());
		if (serializer instanceof JSONObjectSerializer) {
			serializer.serialize(jsonObject, source);
			return;
		}

		if (source instanceof JSONArray) {
			// 
			throw new JSONException("Unsupported type [{}] to JSONObject!", source.getClass());
		}

		if (source instanceof Map) {
			// Map
			for (final Map.Entry<?, ?> e : ((Map<?, ?>) source).entrySet()) {
				jsonObject.set(Convert.toStr(e.getKey()), e.getValue(), filter, jsonObject.getConfig().isCheckDuplicate());
			}
		} else if (source instanceof Map.Entry) {
			final Map.Entry entry = (Map.Entry) source;
			jsonObject.set(Convert.toStr(entry.getKey()), entry.getValue(), filter, jsonObject.getConfig().isCheckDuplicate());
		} else if (source instanceof CharSequence) {
			// 
			mapFromStr((CharSequence) source, jsonObject, filter);
		} else if (source instanceof Reader) {
			mapFromTokener(new JSONTokener((Reader) source, jsonObject.getConfig()), jsonObject, filter);
		} else if (source instanceof InputStream) {
			mapFromTokener(new JSONTokener((InputStream) source, jsonObject.getConfig()), jsonObject, filter);
		} else if (source instanceof byte[]) {
			mapFromTokener(new JSONTokener(IoUtil.toStream((byte[]) source), jsonObject.getConfig()), jsonObject, filter);
		} else if (source instanceof JSONTokener) {
			// JSONTokener
			mapFromTokener((JSONTokener) source, jsonObject, filter);
		} else if (source instanceof ResourceBundle) {
			// JSONTokener
			mapFromResourceBundle((ResourceBundle) source, jsonObject, filter);
		} else if (BeanUtil.isReadableBean(source.getClass())) {
			// 
			mapFromBean(source, jsonObject);
		}

		
	}

	/**
	 * 
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	public void map(JSONArray jsonArray, Filter<Mutable<Object>> filter) throws JSONException {
		final Object source = this.source;
		if (null == source) {
			return;
		}

		final JSONSerializer serializer = GlobalSerializeMapping.getSerializer(source.getClass());
		if (null != serializer && JSONArray.class.equals(TypeUtil.getTypeArgument(serializer.getClass()))) {
			// 
			serializer.serialize(jsonArray, source);
		} else if (source instanceof CharSequence) {
			// 
			mapFromStr((CharSequence) source, jsonArray, filter);
		}else if (source instanceof Reader) {
			mapFromTokener(new JSONTokener((Reader) source, jsonArray.getConfig()), jsonArray, filter);
		} else if (source instanceof InputStream) {
			mapFromTokener(new JSONTokener((InputStream) source, jsonArray.getConfig()), jsonArray, filter);
		} else if (source instanceof byte[]) {
			final byte[] bytesSource = (byte[]) source;
			// 
			if (bytesSource.length > 1 && '[' == bytesSource[0] && ']' == bytesSource[bytesSource.length - 1]) {
				mapFromTokener(new JSONTokener(IoUtil.toStream(bytesSource), jsonArray.getConfig()), jsonArray, filter);
			}else{
				// 
				for(final byte b : bytesSource){
					jsonArray.add(b);
				}
			}
		} else if (source instanceof JSONTokener) {
			mapFromTokener((JSONTokener) source, jsonArray, filter);
		} else {
			final Iterator<?> iter;
			if (ArrayUtil.isArray(source)) {// 
				iter = new ArrayIter<>(source);
			} else if (source instanceof Iterator<?>) {// Iterator
				iter = ((Iterator<?>) source);
			} else if (source instanceof Iterable<?>) {// Iterable
				iter = ((Iterable<?>) source).iterator();
			} else {
				if(false == jsonArray.getConfig().isIgnoreError()){
					throw new JSONException("JSONArray initial value should be a string or collection or array.");
				}
				// 
				return;
			}

			final JSONConfig config = jsonArray.getConfig();
			Object next;
			while (iter.hasNext()) {
				next = iter.next();
				// 
				if (next != source) {
					jsonArray.addRaw(JSONUtil.wrap(next, config), filter);
				}
			}
		}
	}

	/**
	 *   
	 * @since 5.3.1
	 */
	private static void mapFromResourceBundle(ResourceBundle bundle, JSONObject jsonObject, Filter<MutablePair<String, Object>> filter) {
		Enumeration<String> keys = bundle.getKeys();
		while (keys.hasMoreElements()) {
			String key = keys.nextElement();
			if (key != null) {
				InternalJSONUtil.propertyPut(jsonObject, key, bundle.getString(key), filter);
			}
		}
	}

	/**
	 * 
	 */
	private static void mapFromStr(CharSequence source, JSONObject jsonObject, Filter<MutablePair<String, Object>> filter) {
		final String jsonStr = StrUtil.trim(source);
		if (StrUtil.startWith(jsonStr, '<')) {
			// 
			XML.toJSONObject(jsonObject, jsonStr, false);
			return;
		}
		mapFromTokener(new JSONTokener(StrUtil.trim(source), jsonObject.getConfig()), jsonObject, filter);
	}

	/**
	 * 
	 */
	private void mapFromStr(CharSequence source, JSONArray jsonArray, Filter<Mutable<Object>> filter) {
		if (null != source) {
			mapFromTokener(new JSONTokener(StrUtil.trim(source), jsonArray.getConfig()), jsonArray, filter);
		}
	}

	/**
	 * 
	 */
	private static void mapFromTokener(JSONTokener x, JSONObject jsonObject, Filter<MutablePair<String, Object>> filter) {
		JSONParser.of(x).parseTo(jsonObject, filter);
	}

	/**
	 * 
	 */
	private static void mapFromTokener(JSONTokener x, JSONArray jsonArray, Filter<Mutable<Object>> filter) {
		JSONParser.of(x).parseTo(jsonArray, filter);
	}

	/**
	 * 
	 */
	private static void mapFromBean(Object bean, JSONObject jsonObject) {
		BeanUtil.beanToMap(bean, jsonObject, InternalJSONUtil.toCopyOptions(jsonObject.getConfig()));
	}
}
