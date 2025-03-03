package cn.hutool.json;

import cn.hutool.core.bean.BeanPath;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Filter;
import cn.hutool.core.lang.mutable.MutablePair;
import cn.hutool.core.map.CaseInsensitiveMap;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.map.MapWrapper;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.json.serialize.JSONWriter;

import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;

/**
 * 
 */
public class JSONObject extends MapWrapper<String, Object> implements JSON, JSONGetter<String> {
	private static final long serialVersionUID = -330220388580734346L;

	/**
	 * 
	 */
	public static final int DEFAULT_CAPACITY = MapUtil.DEFAULT_INITIAL_CAPACITY;

	/**
	 * 
	 */
	private JSONConfig config;

	// -------------------------------------------------------------------------------------------------------------------- Constructor start

	/**
	 *
	 */
	public JSONObject() {
		this(DEFAULT_CAPACITY, false);
	}

	/**
	 * 
	 */
	public JSONObject(boolean isOrder) {
		this(DEFAULT_CAPACITY, isOrder);
	}

	/**
	 * 
	 */
	public JSONObject(int capacity, boolean isOrder) {
		this(capacity, false, isOrder);
	}

	/**
	 
	 */
	@SuppressWarnings("unused")
	@Deprecated
	public JSONObject(int capacity, boolean isIgnoreCase, boolean isOrder) {
		this(capacity, JSONConfig.create().setIgnoreCase(isIgnoreCase));
	}

	/**
	 * 
	 */
	public JSONObject(JSONConfig config) {
		this(DEFAULT_CAPACITY, config);
	}

	/**
	 *
	 */
	public JSONObject(int capacity, JSONConfig config) {
		super(InternalJSONUtil.createRawMap(capacity, ObjectUtil.defaultIfNull(config, JSONConfig.create())));
		this.config = ObjectUtil.defaultIfNull(config, JSONConfig.create());
	}

	/**
	
	 */
	public JSONObject(Object source) {
		this(source, InternalJSONUtil.defaultIgnoreNullValue(source));
	}

	/**
	 *
	 */
	public JSONObject(Object source, boolean ignoreNullValue) {
		this(source, JSONConfig.create().setIgnoreNullValue(ignoreNullValue));
	}

	/**
	 * 
	 */
	@SuppressWarnings("unused")
	@Deprecated
	public JSONObject(Object source, boolean ignoreNullValue, boolean isOrder) {
		this(source, JSONConfig.create()//
				.setIgnoreCase((source instanceof CaseInsensitiveMap))//
				.setIgnoreNullValue(ignoreNullValue)
		);
	}

	/**
	 *
	 */
	public JSONObject(Object source, JSONConfig config) {
		this(source, config, null);
	}

	/**
	 *
	 */
	public JSONObject(Object source, JSONConfig config, Filter<MutablePair<String, Object>> filter) {
		this(DEFAULT_CAPACITY, config);
		ObjectMapper.of(source).map(this, filter);
	}

	/**
	 * 
	 */
	public JSONObject(Object source, String... names) {
		this();
		if (ArrayUtil.isEmpty(names)) {
			ObjectMapper.of(source).map(this, null);
			return;
		}

		if (source instanceof Map) {
			Object value;
			for (String name : names) {
				value = ((Map<?, ?>) source).get(name);
				this.set(name, value, null, getConfig().isCheckDuplicate());
			}
		} else {
			for (String name : names) {
				try {
					this.putOpt(name, ReflectUtil.getFieldValue(source, name));
				} catch (Exception ignore) {
					// ignore
				}
			}
		}
	}

	/**
	 * 
	 */
	@SuppressWarnings("unused")
	@Deprecated
	public JSONObject(CharSequence source, boolean isOrder) throws JSONException {
		this(source, JSONConfig.create());
	}

	// -------------------------------------------------------------------------------------------------------------------- Constructor end

	@Override
	public JSONConfig getConfig() {
		return this.config;
	}

	/**
	 
	 * @since 4.1.19
	 */
	public JSONObject setDateFormat(String format) {
		this.config.setDateFormat(format);
		return this;
	}

	/**
	 
	 */
	public JSONArray toJSONArray(Collection<String> names) throws JSONException {
		if (CollectionUtil.isEmpty(names)) {
			return null;
		}
		final JSONArray ja = new JSONArray(this.config);
		Object value;
		for (String name : names) {
			value = this.get(name);
			if (null != value) {
				ja.set(value);
			}
		}
		return ja;
	}

	@Override
	public Object getObj(String key, Object defaultValue) {
		return this.getOrDefault(key, defaultValue);
	}

	@Override
	public Object getByPath(String expression) {
		return BeanPath.create(expression).get(this);
	}

	@Override
	public <T> T getByPath(String expression, Class<T> resultType) {
		return JSONConverter.jsonConvert(resultType, getByPath(expression), getConfig());
	}

	@Override
	public void putByPath(String expression, Object value) {
		BeanPath.create(expression).set(this, value);
	}

	/**
	 *
	 */
	@Override
	@Deprecated
	public JSONObject put(String key, Object value) throws JSONException {
		return set(key, value);
	}

	/**
	 * 
	 */
	public JSONObject set(String key, Object value) throws JSONException {
		return set(key, value, null, false);
	}

	/**
	 * 
	 */
	public JSONObject set(String key, Object value, Filter<MutablePair<String, Object>> filter, boolean checkDuplicate) throws JSONException {
		if (null == key) {
			return this;
		}

		// 
		if (null != filter) {
			final MutablePair<String, Object> pair = new MutablePair<>(key, value);
			if (filter.accept(pair)) {
				// 
				key = pair.getKey();
				value = pair.getValue();
			} else {
				// 
				return this;
			}
		}

		final boolean ignoreNullValue = this.config.isIgnoreNullValue();
		if (ObjectUtil.isNull(value) && ignoreNullValue) {
			// 
			this.remove(key);
		} else {
			if (checkDuplicate && containsKey(key)) {
				throw new JSONException("Duplicate key \"{}\"", key);
			}

			super.put(key, JSONUtil.wrap(InternalJSONUtil.testValidity(value), this.config));
		}
		return this;
	}

	/**
	 * 
	 */
	public JSONObject putOnce(String key, Object value) throws JSONException {
		return setOnce(key, value, null);
	}

	/**
	 * 
	 * @since 5.8.0
	 */
	public JSONObject setOnce(String key, Object value, Filter<MutablePair<String, Object>> filter) throws JSONException {
		return set(key, value, filter, true);
	}

	/**
	 *
	 */
	public JSONObject putOpt(String key, Object value) throws JSONException {
		if (key != null && value != null) {
			this.set(key, value);
		}
		return this;
	}

	@Override
	public void putAll(Map<? extends String, ?> m) {
		for (Entry<? extends String, ?> entry : m.entrySet()) {
			this.set(entry.getKey(), entry.getValue());
		}
	}

	/**
	 * 
	 */
	public JSONObject accumulate(String key, Object value) throws JSONException {
		InternalJSONUtil.testValidity(value);
		Object object = this.getObj(key);
		if (object == null) {
			this.set(key, value);
		} else if (object instanceof JSONArray) {
			((JSONArray) object).set(value);
		} else {
			this.set(key, JSONUtil.createArray(this.config).set(object).set(value));
		}
		return this;
	}

	/**
	 * 
	 */
	public JSONObject append(String key, Object value) throws JSONException {
		InternalJSONUtil.testValidity(value);
		Object object = this.getObj(key);
		if (object == null) {
			this.set(key, new JSONArray(this.config).set(value));
		} else if (object instanceof JSONArray) {
			this.set(key, ((JSONArray) object).set(value));
		} else {
			throw new JSONException("JSONObject [" + key + "] is not a JSONArray.");
		}
		return this;
	}

	/**
	 * 
	 */
	public JSONObject increment(String key) throws JSONException {
		Object value = this.getObj(key);
		if (value == null) {
			this.set(key, 1);
		} else if (value instanceof BigInteger) {
			this.set(key, ((BigInteger) value).add(BigInteger.ONE));
		} else if (value instanceof BigDecimal) {
			this.set(key, ((BigDecimal) value).add(BigDecimal.ONE));
		} else if (value instanceof Integer) {
			this.set(key, (Integer) value + 1);
		} else if (value instanceof Long) {
			this.set(key, (Long) value + 1);
		} else if (value instanceof Double) {
			this.set(key, (Double) value + 1);
		} else if (value instanceof Float) {
			this.set(key, (Float) value + 1);
		} else {
			throw new JSONException("Unable to increment [" + JSONUtil.quote(key) + "].");
		}
		return this;
	}

	/**
	 * 
	 */
	@Override
	public String toString() {
		return this.toJSONString(0);
	}

	/**
	 *
	 */
	public String toJSONString(int indentFactor, Filter<MutablePair<Object, Object>> filter) {
		final StringWriter sw = new StringWriter();
		synchronized (sw.getBuffer()) {
			return this.write(sw, indentFactor, 0, filter).toString();
		}
	}

	@Override
	public Writer write(Writer writer, int indentFactor, int indent) throws JSONException {
		return write(writer, indentFactor, indent, null);
	}

	/**
	 * 
	 */
	public Writer write(Writer writer, int indentFactor, int indent, Filter<MutablePair<Object, Object>> filter) throws JSONException {
		final JSONWriter jsonWriter = JSONWriter.of(writer, indentFactor, indent, config)
				.beginObj();
		this.forEach((key, value) -> jsonWriter.writeField(new MutablePair<>(key, value), filter));
		jsonWriter.end();
		// 
		return writer;
	}

	@Override
	public JSONObject clone() throws CloneNotSupportedException {
		final JSONObject clone = (JSONObject) super.clone();
		clone.config = this.config;
		return clone;
	}
}
