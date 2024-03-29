package cn.hutool.json.xml;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.CharUtil;
import cn.hutool.core.util.EscapeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONException;
import cn.hutool.json.JSONObject;

/**
 * 
 */
public class JSONXMLSerializer {
	/**
	 *
	 */
	public static String toXml(Object object) throws JSONException {
		return toXml(object, null);
	}

	/**
	 * 
	 */
	public static String toXml(Object object, String tagName) throws JSONException {
		return toXml(object, tagName, "content");
	}

	/**
	 * 
	 */
	public static String toXml(Object object, String tagName, String... contentKeys) throws JSONException {
		if (null == object) {
			return null;
		}

		final StringBuilder sb = new StringBuilder();
		if (object instanceof JSONObject) {

			// Emit <tagName>
			appendTag(sb, tagName, false);

			// Loop thru the keys.
			((JSONObject) object).forEach((key, value) -> {
				if (ArrayUtil.isArray(value)) {
					value = new JSONArray(value);
				}

				// Emit content in body
				if (ArrayUtil.contains(contentKeys, key)) {
					if (value instanceof JSONArray) {
						int i = 0;
						for (Object val : (JSONArray) value) {
							if (i > 0) {
								sb.append(CharUtil.LF);
							}
							sb.append(EscapeUtil.escapeXml(val.toString()));
							i++;
						}
					} else {
						sb.append(EscapeUtil.escapeXml(value.toString()));
					}

					// Emit an array of similar keys

				} else if (StrUtil.isEmptyIfStr(value)) {
					sb.append(wrapWithTag(null, key));
				} else if (value instanceof JSONArray) {
					for (Object val : (JSONArray) value) {
						if (val instanceof JSONArray) {
							sb.append(wrapWithTag(toXml(val, null, contentKeys), key));
						} else {
							sb.append(toXml(val, key, contentKeys));
						}
					}
				} else {
					sb.append(toXml(value, key, contentKeys));
				}
			});

			// Emit the </tagname> close tag
			appendTag(sb, tagName, true);
			return sb.toString();
		}

		if (ArrayUtil.isArray(object)) {
			object = new JSONArray(object);
		}

		if (object instanceof JSONArray) {
			for (Object val : (JSONArray) object) {
				// XML does not have good support for arrays. If an array
				// appears in a place where XML is lacking, synthesize an
				// <array> element.
				sb.append(toXml(val, tagName == null ? "array" : tagName, contentKeys));
			}
			return sb.toString();
		}

		return wrapWithTag(EscapeUtil.escapeXml(object.toString()), tagName);
	}

	/**
	 * 
	 */
	private static void appendTag(StringBuilder sb, String tagName, boolean isEndTag) {
		if (StrUtil.isNotBlank(tagName)) {
			sb.append('<');
			if (isEndTag) {
				sb.append('/');
			}
			sb.append(tagName).append('>');
		}
	}

	/**
	 * 
	 */
	private static String wrapWithTag(String content, String tagName) {
		if (StrUtil.isBlank(tagName)) {
			return StrUtil.wrap(content, "\"");
		}

		if (StrUtil.isEmpty(content)) {
			return "<" + tagName + "/>";
		} else {
			return "<" + tagName + ">" + content + "</" + tagName + ">";
		}
	}
}
