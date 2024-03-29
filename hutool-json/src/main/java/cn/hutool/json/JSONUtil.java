package cn.hutool.json;

import cn.hutool.core.convert.NumberWithFormat;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.map.MapWrapper;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.serialize.GlobalSerializeMapping;
import cn.hutool.json.serialize.JSONArraySerializer;
import cn.hutool.json.serialize.JSONDeserializer;
import cn.hutool.json.serialize.JSONObjectSerializer;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.time.temporal.TemporalAccessor;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**

 *
 * @author Looly
 */
public class JSONUtil {

	// -------------------------------------------------------------------- Pause start

	/**
	 * 
	 */
	public static JSONObject createObj() {
		return new JSONObject();
	}

	/**
	 * 
	 */
	public static JSONObject createObj(JSONConfig config) {
		return new JSONObject(config);
	}

	/**
	 * 
	 */
	public static JSONArray createArray() {
		return new JSONArray();
	}

	/**
	 * 
	 */
	public static JSONArray createArray(JSONConfig config) {
		return new JSONArray(config);
	}

	/**
	 * 
	 */
	public static JSONObject parseObj(String jsonStr) {
		return new JSONObject(jsonStr);
	}

	/**
	 * J
	 */
	public static JSONObject parseObj(Object obj) {
		return parseObj(obj, null);
	}

	/**
	 * 
	 * @return JSONObject
	 * @since 5.3.1
	 */
	public static JSONObject parseObj(Object obj, JSONConfig config) {
		return new JSONObject(obj, ObjectUtil.defaultIfNull(config, JSONConfig::create));
	}

	/**
	 * J
	 */
	public static JSONObject parseObj(Object obj, boolean ignoreNullValue) {
		return new JSONObject(obj, ignoreNullValue);
	}

	/**
	 * 
	 */
	@SuppressWarnings("unused")
	@Deprecated
	public static JSONObject parseObj(Object obj, boolean ignoreNullValue, boolean isOrder) {
		return new JSONObject(obj, ignoreNullValue);
	}

	/**
	 * 
	 */
	public static JSONArray parseArray(String jsonStr) {
		return new JSONArray(jsonStr);
	}

	/**
	 * 
	 * @since 3.0.8
	 */
	public static JSONArray parseArray(Object arrayOrCollection) {
		return parseArray(arrayOrCollection, null);
	}

	/**
	 * 
	 */
	public static JSONArray parseArray(Object arrayOrCollection, JSONConfig config) {
		return new JSONArray(arrayOrCollection, config);
	}

	/**
	 * 
	 */
	public static JSONArray parseArray(Object arrayOrCollection, boolean ignoreNullValue) {
		return new JSONArray(arrayOrCollection, ignoreNullValue);
	}

	/**
	 * 
	 */
	public static JSON parse(Object obj) {
		return parse(obj, null);
	}

	/**
	 * 
	 */
	public static JSON parse(Object obj, JSONConfig config) {
		if (null == obj) {
			return null;
		}
		JSON json;
		if (obj instanceof JSON) {
			json = (JSON) obj;
		} else if (obj instanceof CharSequence) {
			final String jsonStr = StrUtil.trim((CharSequence) obj);
			json = isTypeJSONArray(jsonStr) ? parseArray(jsonStr, config) : parseObj(jsonStr, config);
		} else if (obj instanceof MapWrapper) {
			// 
			json = parseObj(obj, config);
		} else if (obj instanceof Iterable || obj instanceof Iterator || ArrayUtil.isArray(obj)) {// 
			json = parseArray(obj, config);
		} else {// 
			json = parseObj(obj, config);
		}

		return json;
	}

	/**
	 * 
	 * @return JSONObject
	 */
	public static JSONObject parseFromXml(String xmlStr) {
		return XML.toJSONObject(xmlStr);
	}

	// -------------------------------------------------------------------- Parse end

	// -------------------------------------------------------------------- Read start

	/**
	 * 
	 */
	public static JSON readJSON(File file, Charset charset) throws IORuntimeException {
		return parse(FileReader.create(file, charset).readString());
	}

	/**
	 * 
	 */
	public static JSONObject readJSONObject(File file, Charset charset) throws IORuntimeException {
		return parseObj(FileReader.create(file, charset).readString());
	}

	/**
	 * 
	 */
	public static JSONArray readJSONArray(File file, Charset charset) throws IORuntimeException {
		return parseArray(FileReader.create(file, charset).readString());
	}
	// -------------------------------------------------------------------- Read end

	// -------------------------------------------------------------------- toString start

	/**
	 * 
	 */
	public static String toJsonStr(JSON json, int indentFactor) {
		if (null == json) {
			return null;
		}
		return json.toJSONString(indentFactor);
	}

	/**
	 * 
	 */
	public static String toJsonStr(JSON json) {
		if (null == json) {
			return null;
		}
		return json.toJSONString(0);
	}

	/**
	 * 
	 */
	public static void toJsonStr(JSON json, Writer writer) {
		if (null != json) {
			json.write(writer);
		}
	}

	/**
	 
	 */
	public static String toJsonPrettyStr(JSON json) {
		if (null == json) {
			return null;
		}
		return json.toJSONString(4);
	}

	/**
	 * 
	 */
	public static String toJsonStr(Object obj) {
		return toJsonStr(obj, (JSONConfig) null);
	}

	/**
	 * 
	 */
	public static String toJsonStr(Object obj, JSONConfig jsonConfig) {
		if (null == obj) {
			return null;
		}
		if (obj instanceof CharSequence) {
			return StrUtil.str((CharSequence) obj);
		}
		return toJsonStr(parse(obj, jsonConfig));
	}

	/**
	 *
	 */
	public static void toJsonStr(Object obj, Writer writer) {
		if (null != obj) {
			toJsonStr(parse(obj), writer);
		}
	}

	/**
	
	 */
	public static String toJsonPrettyStr(Object obj) {
		return toJsonPrettyStr(parse(obj));
	}

	/**
	 * 
	 */
	public static String toXmlStr(JSON json) {
		return XML.toXml(json);
	}
	// -------------------------------------------------------------------- toString end

	// -------------------------------------------------------------------- toBean start

	/**
	 * J
	 */
	public static <T> T toBean(String jsonString, Class<T> beanClass) {
		return toBean(parseObj(jsonString), beanClass);
	}

	/**
	 * 
	 */
	public static <T> T toBean(String jsonString, JSONConfig config, Class<T> beanClass) {
		return toBean(parseObj(jsonString, config), beanClass);
	}

	/**
	 *
	 */
	public static <T> T toBean(JSONObject json, Class<T> beanClass) {
		return null == json ? null : json.toBean(beanClass);
	}

	/**
	 * 
	 */
	public static <T> T toBean(String jsonString, TypeReference<T> typeReference, boolean ignoreError) {
		return toBean(jsonString, typeReference.getType(), ignoreError);
	}

	/**
	 */
	public static <T> T toBean(String jsonString, Type beanType, boolean ignoreError) {
		final JSON json = parse(jsonString, JSONConfig.create().setIgnoreError(ignoreError));
		if(null == json){
			return null;
		}
		return json.toBean(beanType);
	}

	/**
	 * 
	 */
	public static <T> T toBean(JSON json, TypeReference<T> typeReference, boolean ignoreError) {
		return toBean(json, typeReference.getType(), ignoreError);
	}

	/**
	 *
	 */
	@SuppressWarnings("deprecation")
	public static <T> T toBean(JSON json, Type beanType, boolean ignoreError) {
		if (null == json) {
			return null;
		}
		return json.toBean(beanType, ignoreError);
	}
	// -------------------------------------------------------------------- toBean end

	/**
	 *
	 */
	public static <T> List<T> toList(String jsonArray, Class<T> elementType) {
		return toList(parseArray(jsonArray), elementType);
	}

	/**
	 * 
	 */
	public static <T> List<T> toList(JSONArray jsonArray, Class<T> elementType) {
		return null == jsonArray ? null : jsonArray.toList(elementType);
	}

	/**
	 * 
	 * 
	 */
	public static Object getByPath(JSON json, String expression) {
		return getByPath(json, expression, null);
	}

	/**
	 * 
	 * 
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getByPath(JSON json, String expression, T defaultValue) {
		if((null == json || StrUtil.isBlank(expression))){
			return defaultValue;
		}

		if(null != defaultValue){
			final Class<T> type = (Class<T>) defaultValue.getClass();
			return ObjectUtil.defaultIfNull(json.getByPath(expression, type), defaultValue);
		}
		return (T) json.getByPath(expression);
	}

	/**
	 * 
	 * 
	 */
	public static void putByPath(JSON json, String expression, Object value) {
		json.putByPath(expression, value);
	}

	/**
	 * 
	 */
	public static String quote(String string) {
		return quote(string, true);
	}

	/**
	 * 
	 */
	public static String quote(String string, boolean isWrap) {
		StringWriter sw = new StringWriter();
		try {
			return quote(string, sw, isWrap).toString();
		} catch (IOException ignored) {
			// will never happen - we are writing to a string writer
			return StrUtil.EMPTY;
		}
	}

	/**
	 *
	 */
	public static Writer quote(String str, Writer writer) throws IOException {
		return quote(str, writer, true);
	}

	/**
	 * 
	 */
	public static Writer quote(String str, Writer writer, boolean isWrap) throws IOException {
		if (StrUtil.isEmpty(str)) {
			if (isWrap) {
				writer.write("\"\"");
			}
			return writer;
		}

		char c; // 
		int len = str.length();
		if (isWrap) {
			writer.write('"');
		}
		for (int i = 0; i < len; i++) {
			c = str.charAt(i);
			switch (c) {
				case '\\':
				case '"':
					writer.write("\\");
					writer.write(c);
					break;
				default:
					writer.write(escape(c));
			}
		}
		if (isWrap) {
			writer.write('"');
		}
		return writer;
	}

	/**
	 * 
	 */
	public static String escape(String str) {
		if (StrUtil.isEmpty(str)) {
			return str;
		}

		final int len = str.length();
		final StringBuilder builder = new StringBuilder(len);
		char c;
		for (int i = 0; i < len; i++) {
			c = str.charAt(i);
			builder.append(escape(c));
		}
		return builder.toString();
	}

	/**
	 * 
	 */
	public static Object wrap(Object object, JSONConfig jsonConfig) {
		if (object == null) {
			return jsonConfig.isIgnoreNullValue() ? null : JSONNull.NULL;
		}
		if (object instanceof JSON //
				|| ObjectUtil.isNull(object) //
				|| object instanceof JSONString //
				|| object instanceof CharSequence //
				|| object instanceof Number //
				|| ObjectUtil.isBasicType(object) //
		) {
			if(object instanceof Number && null != jsonConfig.getDateFormat()){
				return new NumberWithFormat((Number) object, jsonConfig.getDateFormat());
			}
			return object;
		}

		try {
			// fix issue#1399@Github
			if(object instanceof SQLException){
				return object.toString();
			}

			// JSONArray
			if (object instanceof Iterable || ArrayUtil.isArray(object)) {
				return new JSONArray(object, jsonConfig);
			}
			// JSONObject
			if (object instanceof Map || object instanceof Map.Entry) {
				return new JSONObject(object, jsonConfig);
			}

			// 
			if (object instanceof Date
					|| object instanceof Calendar
					|| object instanceof TemporalAccessor
			) {
				return object;
			}
			//
			if (object instanceof Enum) {
				return object.toString();
			}

			// 
			// 
			if (object instanceof Class<?>) {
				return ((Class<?>) object).getName();
			}

			// 
			if (ClassUtil.isJdkClass(object.getClass())) {
				return object.toString();
			}

			// 
			return new JSONObject(object, jsonConfig);
		} catch (final Exception exception) {
			return null;
		}
	}

	/**
	 * =
	 */
	public static String formatJsonStr(String jsonStr) {
		return JSONStrFormatter.format(jsonStr);
	}

	/**
	 * 
	 * 
	 */
	@Deprecated
	public static boolean isJson(String str) {
		return isTypeJSON(str);
	}

	/**
	 * 
	 */
	public static boolean isTypeJSON(String str) {
		return isTypeJSONObject(str) || isTypeJSONArray(str);
	}

	/**
	 * 
	 */
	@Deprecated
	public static boolean isJsonObj(String str) {
		return isTypeJSONObject(str);
	}

	/**
	 * 
	 */
	public static boolean isTypeJSONObject(String str) {
		if (StrUtil.isBlank(str)) {
			return false;
		}
		return StrUtil.isWrap(StrUtil.trim(str), '{', '}');
	}

	/**
	 * 
	 */
	@Deprecated
	public static boolean isJsonArray(String str) {
		return isTypeJSONArray(str);
	}

	/**
	 * 
	 */
	public static boolean isTypeJSONArray(String str) {
		if (StrUtil.isBlank(str)) {
			return false;
		}
		return StrUtil.isWrap(StrUtil.trim(str), '[', ']');
	}

	/**
	 * 
	 */
	public static boolean isNull(Object obj) {
		return null == obj || obj instanceof JSONNull;
	}

	/**
	 * 
	 */
	public static JSONObject xmlToJson(String xml) {
		return XML.toJSONObject(xml);
	}

	/**
	 * 
	 */
	public static void putSerializer(Type type, JSONArraySerializer<?> serializer) {
		GlobalSerializeMapping.put(type, serializer);
	}

	/**
	 * 
	 */
	public static void putSerializer(Type type, JSONObjectSerializer<?> serializer) {
		GlobalSerializeMapping.put(type, serializer);
	}

	/**
	 * 
	 */
	public static void putDeserializer(Type type, JSONDeserializer<?> deserializer) {
		GlobalSerializeMapping.put(type, deserializer);
	}

	// --------------------------------------------------------------------------------------------- Private method start

	/**
	 * 
	 */
	private static String escape(char c) {
		switch (c) {
			case '\b':
				return "\\b";
			case '\t':
				return "\\t";
			case '\n':
				return "\\n";
			case '\f':
				return "\\f";
			case '\r':
				return "\\r";
			default:
				if (c < StrUtil.C_SPACE || //
						(c >= '\u0080' && c <= '\u00a0') || //
						(c >= '\u2000' && c <= '\u2010') || //
						(c >= '\u2028' && c <= '\u202F') || //
						(c >= '\u2066' && c <= '\u206F')//
				) {
					return HexUtil.toUnicodeHex(c);
				} else {
					return Character.toString(c);
				}
		}
	}
	// --------------------------------------------------------------------------------------------- Private method end
}
