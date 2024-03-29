package cn.hutool.json;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.convert.ConvertException;
import cn.hutool.core.convert.NumberWithFormat;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.getter.OptNullBasicTypeFromObjectGetter;
import cn.hutool.core.util.StrUtil;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**

 */
public interface JSONGetter<K> extends OptNullBasicTypeFromObjectGetter<K> {

	/**
	 *
	 */
	JSONConfig getConfig();

	/**
	
	 */
	default boolean isNull(K key) {
		return JSONUtil.isNull(this.getObj(key));
	}

	/**
	 * 
	 */
	default String getStrEscaped(K key) {
		return getStrEscaped(key, null);
	}

	/**
	 * 
	 */
	default String getStrEscaped(K key, String defaultValue) {
		return JSONUtil.escape(getStr(key, defaultValue));
	}

	/**
	 * 
	 */
	default JSONArray getJSONArray(K key) {
		final Object object = this.getObj(key);
		if (JSONUtil.isNull(object)) {
			return null;
		}

		if (object instanceof JSON) {
			return (JSONArray) object;
		}
		return new JSONArray(object, getConfig());
	}

	/**
	 *
	 */
	default JSONObject getJSONObject(K key) {
		final Object object = this.getObj(key);
		if (JSONUtil.isNull(object)) {
			return null;
		}

		if (object instanceof JSON) {
			return (JSONObject) object;
		}
		return new JSONObject(object, getConfig());
	}

	/**
	 *
	 */
	default <T> T getBean(K key, Class<T> beanType) {
		final JSONObject obj = getJSONObject(key);
		return (null == obj) ? null : obj.toBean(beanType);
	}

	/**
	 *
	 */
	default <T> List<T> getBeanList(K key, Class<T> beanType) {
		final JSONArray jsonArray = getJSONArray(key);
		return (null == jsonArray) ? null : jsonArray.toList(beanType);
	}

	@Override
	default Date getDate(K key, Date defaultValue) {
		// 
		final Object obj = getObj(key);
		if (JSONUtil.isNull(obj)) {
			return defaultValue;
		}
		if (obj instanceof Date) {
			return (Date) obj;
		} else if(obj instanceof NumberWithFormat){
			return (Date) ((NumberWithFormat) obj).convert(Date.class, obj);
		}

		final Optional<String> formatOps = Optional.ofNullable(getConfig()).map(JSONConfig::getDateFormat);
		if (formatOps.isPresent()) {
			final String format = formatOps.get();
			if (StrUtil.isNotBlank(format)) {
				// 
				final String str = Convert.toStr(obj);
				if (null == str) {
					return defaultValue;
				}
				return DateUtil.parse(str, format);
			}
		}

		return Convert.toDate(obj, defaultValue);
	}

	/**
	 * 
	 */
	default LocalDateTime getLocalDateTime(K key, LocalDateTime defaultValue) {
		//
		final Object obj = getObj(key);
		if (JSONUtil.isNull(obj)) {
			return defaultValue;
		}
		if (obj instanceof LocalDateTime) {
			return (LocalDateTime) obj;
		}

		final Optional<String> formatOps = Optional.ofNullable(getConfig()).map(JSONConfig::getDateFormat);
		if (formatOps.isPresent()) {
			final String format = formatOps.get();
			if (StrUtil.isNotBlank(format)) {
				// 
				final String str = Convert.toStr(obj);
				if (null == str) {
					return defaultValue;
				}
				return LocalDateTimeUtil.parse(str, format);
			}
		}

		return Convert.toLocalDateTime(obj, defaultValue);
	}

	/**
	 
	 */
	default byte[] getBytes(K key) {
		return get(key, byte[].class);
	}

	/**
	 *
	 */
	default <T> T get(K key, Class<T> type) throws ConvertException {
		return get(key, type, false);
	}

	/**
	 * 
	 */
	default <T> T get(K key, Class<T> type, boolean ignoreError) throws ConvertException {
		final Object value = this.getObj(key);
		if (JSONUtil.isNull(value)) {
			return null;
		}
		return JSONConverter.jsonConvert(type, value, JSONConfig.create().setIgnoreError(ignoreError));
	}
}
