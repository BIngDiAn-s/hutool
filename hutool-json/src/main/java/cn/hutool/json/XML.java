package cn.hutool.json;

import cn.hutool.core.util.CharUtil;
import cn.hutool.json.xml.JSONXMLParser;
import cn.hutool.json.xml.JSONXMLSerializer;
import cn.hutool.json.xml.ParseConfig;

/**
 * 
 */
public class XML {

	/**
	 * The Character '&amp;'.
	 */
	public static final Character AMP = CharUtil.AMP;

	/**
	 * The Character '''.
	 */
	public static final Character APOS = CharUtil.SINGLE_QUOTE;

	/**
	 * The Character '!'.
	 */
	public static final Character BANG = '!';

	/**
	 * The Character '='.
	 */
	public static final Character EQ = '=';

	/**
	 * The Character '&gt;'.
	 */
	public static final Character GT = '>';

	/**
	 * The Character '&lt;'.
	 */
	public static final Character LT = '<';

	/**
	 * The Character '?'.
	 */
	public static final Character QUEST = '?';

	/**
	 * The Character '"'.
	 */
	public static final Character QUOT = CharUtil.DOUBLE_QUOTES;

	/**
	 * The Character '/'.
	 */
	public static final Character SLASH = CharUtil.SLASH;

	/**
	 *
	 */
	public static JSONObject toJSONObject(String string) throws JSONException {
		return toJSONObject(string, false);
	}

	/**
	 * 
	 */
	public static JSONObject toJSONObject(String string, boolean keepStrings) throws JSONException {
		return toJSONObject(new JSONObject(), string, keepStrings);
	}

	/**
	 * 
	 */
	public static JSONObject toJSONObject(final String string, final ParseConfig parseConfig) throws JSONException {
		return toJSONObject(new JSONObject(), string, parseConfig);
	}

	/**
	 * 
	 */
	public static JSONObject toJSONObject(JSONObject jo, String xmlStr, boolean keepStrings) throws JSONException {
		JSONXMLParser.parseJSONObject(jo, xmlStr, keepStrings);
		return jo;
	}

	/**
	 
	 */
	public static JSONObject toJSONObject(final JSONObject jo, final String xmlStr, final ParseConfig parseConfig) throws JSONException {
		JSONXMLParser.parseJSONObject(jo, xmlStr, parseConfig);
		return jo;
	}

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
		return JSONXMLSerializer.toXml(object, tagName, contentKeys);
	}
}
