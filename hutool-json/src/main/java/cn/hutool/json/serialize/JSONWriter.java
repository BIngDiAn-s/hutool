package cn.hutool.json.serialize;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TemporalAccessorUtil;
import cn.hutool.core.date.format.GlobalCustomFormat;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.lang.Filter;
import cn.hutool.core.lang.mutable.MutablePair;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.CharUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONConfig;
import cn.hutool.json.JSONException;
import cn.hutool.json.JSONNull;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONString;
import cn.hutool.json.JSONUtil;

import java.io.IOException;
import java.io.Writer;
import java.time.MonthDay;
import java.time.temporal.TemporalAccessor;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

/**
 * 
 */
public class JSONWriter extends Writer {

	/**
	 * 
	 */
	private final int indentFactor;
	/**
	 * 
	 */
	private final int indent;
	/**
	 * Writer
	 */
	private final Writer writer;
	/**
	 * 
	 */
	private final JSONConfig config;

	/**
	 * 
	 */
	private boolean needSeparator;
	/**
	 * 
	 */
	private boolean arrayMode;

	/**
	 * 
	 */
	public static JSONWriter of(Writer writer, int indentFactor, int indent, JSONConfig config) {
		return new JSONWriter(writer, indentFactor, indent, config);
	}

	/**
	 *
	 */
	public JSONWriter(Writer writer, int indentFactor, int indent, JSONConfig config) {
		this.writer = writer;
		this.indentFactor = indentFactor;
		this.indent = indent;
		this.config = config;
	}

	/**
	 * 
	 */
	public JSONWriter beginObj() {
		writeRaw(CharUtil.DELIM_START);
		return this;
	}

	/**
	 * 
	 */
	public JSONWriter beginArray() {
		writeRaw(CharUtil.BRACKET_START);
		arrayMode = true;
		return this;
	}

	/**
	 * 
	 */
	public JSONWriter end() {
		// 
		writeLF().writeSpace(indent);
		writeRaw(arrayMode ? CharUtil.BRACKET_END : CharUtil.DELIM_END);
		flush();
		arrayMode = false;
		// 
		needSeparator = true;
		return this;
	}

	/**
	 * 
	 *
	 */
	public JSONWriter writeKey(String key) {
		if (needSeparator) {
			writeRaw(CharUtil.COMMA);
		}
		// 
		writeLF().writeSpace(indentFactor + indent);
		return writeRaw(JSONUtil.quote(key));
	}

	/**
	 * 
	 */
	public JSONWriter writeValue(Object value) {
		if(JSONUtil.isNull(value) && config.isIgnoreNullValue()){
			return this;
		}
		return writeValueDirect(value, null);
	}

	/**
	 * 
	 */
	@Deprecated
	public JSONWriter writeField(String key, Object value){
		if(JSONUtil.isNull(value) && config.isIgnoreNullValue()){
			return this;
		}
		return writeKey(key).writeValueDirect(value, null);
	}

	/**
	 * 
	 */
	public JSONWriter writeField(MutablePair<Object, Object> pair, Filter<MutablePair<Object, Object>> filter){
		if(JSONUtil.isNull(pair.getValue()) && config.isIgnoreNullValue()){
			return this;
		}

		if (null == filter || filter.accept(pair)) {
			if(false == arrayMode){
				// 
				writeKey(StrUtil.toString(pair.getKey()));
			}
			return writeValueDirect(pair.getValue(), filter);
		}
		return this;
	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		this.writer.write(cbuf, off, len);
	}

	@Override
	public void flush() {
		try {
			this.writer.flush();
		} catch (IOException e) {
			throw new IORuntimeException(e);
		}
	}

	@Override
	public void close() throws IOException {
		this.writer.close();
	}

	// ------------------------------------------------------------------------------ Private methods
	/**
	 * 
	 */
	private JSONWriter writeValueDirect(Object value, Filter<MutablePair<Object, Object>> filter) {
		if (arrayMode) {
			if (needSeparator) {
				writeRaw(CharUtil.COMMA);
			}
			// 
			writeLF().writeSpace(indentFactor + indent);
		} else {
			writeRaw(CharUtil.COLON).writeSpace(1);
		}
		needSeparator = true;
		return writeObjValue(value, filter);
	}

	/**
	 * 
	 */
	private JSONWriter writeObjValue(Object value, Filter<MutablePair<Object, Object>> filter) {
		final int indent = indentFactor + this.indent;
		if (value == null || value instanceof JSONNull) {
			writeRaw(JSONNull.NULL.toString());
		} else if (value instanceof JSON) {
			if(value instanceof JSONObject){
				((JSONObject) value).write(writer, indentFactor, indent, filter);
			}else if(value instanceof JSONArray){
				((JSONArray) value).write(writer, indentFactor, indent, filter);
			}
		} else if (value instanceof Map || value instanceof Map.Entry) {
			new JSONObject(value).write(writer, indentFactor, indent);
		} else if (value instanceof Iterable || value instanceof Iterator || ArrayUtil.isArray(value)) {
			new JSONArray(value).write(writer, indentFactor, indent);
		} else if (value instanceof Number) {
			writeNumberValue((Number) value);
		} else if (value instanceof Date || value instanceof Calendar || value instanceof TemporalAccessor) {
			// issue#2572@Github
			if(value instanceof MonthDay){
				writeStrValue(value.toString());
				return this;
			}

			final String format = (null == config) ? null : config.getDateFormat();
			writeRaw(formatDate(value, format));
		} else if (value instanceof Boolean) {
			writeBooleanValue((Boolean) value);
		} else if (value instanceof JSONString) {
			writeJSONStringValue((JSONString) value);
		} else {
			writeStrValue(value.toString());
		}

		return this;
	}

	/**
	 * 
	 */
	private void writeNumberValue(Number number) {
		// s
		final boolean isStripTrailingZeros = null == config || config.isStripTrailingZeros();
		writeRaw(NumberUtil.toStr(number, isStripTrailingZeros));
	}

	/**
	 * 
	 */
	private void writeBooleanValue(Boolean value) {
		writeRaw(value.toString());
	}

	/**
	 * 
	 */
	private void writeJSONStringValue(JSONString jsonString) {
		String valueStr;
		try {
			valueStr = jsonString.toJSONString();
		} catch (Exception e) {
			throw new JSONException(e);
		}
		if (null != valueStr) {
			writeRaw(valueStr);
		} else {
			writeStrValue(jsonString.toString());
		}
	}

	/**
	 * 
	 * @param csq 
	 */
	private void writeStrValue(String csq) {
		try {
			JSONUtil.quote(csq, writer);
		} catch (IOException e) {
			throw new IORuntimeException(e);
		}
	}

	/**
	 * 
	 */
	private void writeSpace(int count) {
		if (indentFactor > 0) {
			for (int i = 0; i < count; i++) {
				writeRaw(CharUtil.SPACE);
			}
		}
	}

	/**
	 */
	private JSONWriter writeLF() {
		if (indentFactor > 0) {
			writeRaw(CharUtil.LF);
		}
		return this;
	}

	/**
	 * 
	 */
	private JSONWriter writeRaw(String csq) {
		try {
			writer.append(csq);
		} catch (IOException e) {
			throw new IORuntimeException(e);
		}
		return this;
	}

	/**
	 * 
	 */
	private JSONWriter writeRaw(char c) {
		try {
			writer.write(c);
		} catch (IOException e) {
			throw new IORuntimeException(e);
		}
		return this;
	}

	/**
	 * 
	 */
	private static String formatDate(Object dateObj, String format) {
		if (StrUtil.isNotBlank(format)) {
			final String dateStr;
			if (dateObj instanceof TemporalAccessor) {
				dateStr = TemporalAccessorUtil.format((TemporalAccessor) dateObj, format);
			} else {
				dateStr = DateUtil.format(Convert.toDate(dateObj), format);
			}

			if (GlobalCustomFormat.FORMAT_SECONDS.equals(format)
					|| GlobalCustomFormat.FORMAT_MILLISECONDS.equals(format)) {
				// 
				return dateStr;
			}
			//
			return JSONUtil.quote(dateStr);
		}

		//
		long timeMillis;
		if (dateObj instanceof TemporalAccessor) {
			timeMillis = TemporalAccessorUtil.toEpochMilli((TemporalAccessor) dateObj);
		} else if (dateObj instanceof Date) {
			timeMillis = ((Date) dateObj).getTime();
		} else if (dateObj instanceof Calendar) {
			timeMillis = ((Calendar) dateObj).getTimeInMillis();
		} else {
			throw new UnsupportedOperationException("Unsupported Date type: " + dateObj.getClass());
		}
		return String.valueOf(timeMillis);
	}
}
