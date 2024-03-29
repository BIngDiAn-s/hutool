package cn.hutool.json;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.Filter;
import cn.hutool.core.lang.mutable.MutablePair;
import cn.hutool.core.map.CaseInsensitiveLinkedMap;
import cn.hutool.core.map.CaseInsensitiveTreeMap;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.CharUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * 
 * @author Looly
 */
public final class InternalJSONUtil {

	private InternalJSONUtil() {
	}

	/**
	 * 
	 */
	static Object testValidity(Object obj) throws JSONException {
		if (false == ObjectUtil.isValidIfNumber(obj)) {
			throw new JSONException("JSON does not allow non-finite numbers.");
		}
		return obj;
	}

	/**
	 * 
	 * @throws JSONException If the value is or contains an invalid number.
	 */
	static String valueToString(Object value) throws JSONException {
		if (value == null || value instanceof JSONNull) {
			return JSONNull.NULL.toString();
		}
		if (value instanceof JSONString) {
			try {
				return ((JSONString) value).toJSONString();
			} catch (Exception e) {
				throw new JSONException(e);
			}
		} else if (value instanceof Number) {
			return NumberUtil.toStr((Number) value);
		} else if (value instanceof Boolean || value instanceof JSONObject || value instanceof JSONArray) {
			return value.toString();
		} else if (value instanceof Map) {
			Map<?, ?> map = (Map<?, ?>) value;
			return new JSONObject(map).toString();
		} else if (value instanceof Collection) {
			Collection<?> coll = (Collection<?>) value;
			return new JSONArray(coll).toString();
		} else if (ArrayUtil.isArray(value)) {
			return new JSONArray(value).toString();
		} else {
			return JSONUtil.quote(value.toString());
		}
	}

	/**
	 * 
	 *
	 * @param string A String.
	 * @return A simple JSON value.
	 */
	public static Object stringToValue(String string) {
		// 
		if (StrUtil.isEmpty(string) || StrUtil.NULL.equalsIgnoreCase(string)) {
			return JSONNull.NULL;
		}

		// 
		if ("true".equalsIgnoreCase(string)) {
			return Boolean.TRUE;
		}
		if ("false".equalsIgnoreCase(string)) {
			return Boolean.FALSE;
		}

		// 
		char b = string.charAt(0);
		if ((b >= '0' && b <= '9') || b == '-') {
			try {
				if (StrUtil.containsAnyIgnoreCase(string, ".", "e")) {
					// p
					return new BigDecimal(string);
				} else {
					final long myLong = Long.parseLong(string);
					if (string.equals(Long.toString(myLong))) {
						if (myLong == (int) myLong) {
							return (int) myLong;
						} else {
							return myLong;
						}
					}
				}
			} catch (Exception ignore) {
			}
		}

		// 
		return string;
	}

	/**
	 * 
	 */
	static JSONObject propertyPut(JSONObject jsonObject, Object key, Object value, Filter<MutablePair<String, Object>> filter) {
		final String[] path = StrUtil.splitToArray(Convert.toStr(key), CharUtil.DOT);
		final int last = path.length - 1;
		JSONObject target = jsonObject;
		for (int i = 0; i < last; i += 1) {
			final String segment = path[i];
			JSONObject nextTarget = target.getJSONObject(segment);
			if (nextTarget == null) {
				nextTarget = new JSONObject(target.getConfig());
				target.set(segment, nextTarget, filter, target.getConfig().isCheckDuplicate());
			}
			target = nextTarget;
		}
		target.set(path[last], value, filter, target.getConfig().isCheckDuplicate());
		return jsonObject;
	}

	/**
	 * 
	 */
	static boolean defaultIgnoreNullValue(Object obj) {
		return (false == (obj instanceof CharSequence))//
				&& (false == (obj instanceof JSONTokener))//
				&& (false == (obj instanceof Map));
	}

	/**
	 *
	 */
	static CopyOptions toCopyOptions(JSONConfig config) {
		return CopyOptions.create()
				.setIgnoreCase(config.isIgnoreCase())
				.setIgnoreError(config.isIgnoreError())
				.setIgnoreNullValue(config.isIgnoreNullValue())
				.setTransientSupport(config.isTransientSupport());
	}

	/**
	 * 
	 */
	static Map<String, Object> createRawMap(int capacity, JSONConfig config) {
		final Map<String, Object> rawHashMap;
		if (null == config) {
			config = JSONConfig.create();
		}
		final Comparator<String> keyComparator = config.getKeyComparator();
		if (config.isIgnoreCase()) {
			if (null != keyComparator) {
				rawHashMap = new CaseInsensitiveTreeMap<>(keyComparator);
			} else {
				rawHashMap = new CaseInsensitiveLinkedMap<>(capacity);
			}
		} else {
			if (null != keyComparator) {
				rawHashMap = new TreeMap<>(keyComparator);
			} else {
				rawHashMap = new LinkedHashMap<>(capacity);
			}
		}
		return rawHashMap;
	}
}
