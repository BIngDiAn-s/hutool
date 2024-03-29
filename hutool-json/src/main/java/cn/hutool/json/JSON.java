package cn.hutool.json;

import cn.hutool.core.bean.BeanPath;
import cn.hutool.core.bean.copier.IJSONTypeConverter;
import cn.hutool.core.lang.TypeReference;

import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Type;

/**
 * 
 *
 * @author Looly
 */
public interface JSON extends Cloneable, Serializable, IJSONTypeConverter {

	/**
	
	
	 */
	JSONConfig getConfig();

	/**
	 *
	 * @since 4.0.6
	 */
	Object getByPath(String expression);

	/**
	
	 *
	 * <pre>
	 * persion
	 * persion.name
	 * persons[3]
	 * person.friends[5].name
	 */
	void putByPath(String expression, Object value);

	/**
	 
	 */
	<T> T getByPath(String expression, Class<T> resultType);

	/**
	
	 */
	default String toStringPretty() throws JSONException {
		return this.toJSONString(4);
	}

	/**
	
	 */
	default String toJSONString(int indentFactor) throws JSONException {
		final StringWriter sw = new StringWriter();
		return this.write(sw, indentFactor, 0).toString();
	}

	/**
	
	 */
	default Writer write(Writer writer) throws JSONException {
		return this.write(writer, 0, 0);
	}

	/**

	 */
	Writer write(Writer writer, int indentFactor, int indent) throws JSONException;

	/**
	
	 */
	default <T> T toBean(Class<T> clazz) {
		return toBean((Type) clazz);
	}

	/**
	
	 */
	default <T> T toBean(TypeReference<T> reference) {
		return toBean(reference.getType());
	}

	/**
	 
	 */
	default <T> T toBean(Type type) {
		return JSONConverter.jsonConvert(type, this, getConfig());
	}

	/**
	 *
	 */
	@Deprecated
	default <T> T toBean(Type type, boolean ignoreError) {
		return JSONConverter.jsonConvert(type, this, JSONConfig.create().setIgnoreError(ignoreError));
	}
}
