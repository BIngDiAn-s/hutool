package cn.hutool.json;

import cn.hutool.core.lang.Filter;
import cn.hutool.core.lang.mutable.Mutable;
import cn.hutool.core.lang.mutable.MutablePair;

/**
 * 
 * @since 5.8.0
 */
public class JSONParser {

	/**
	 * 
	 *
	 * @param tokener {@link JSONTokener}
	 * @return JSONParser
	 */
	public static JSONParser of(JSONTokener tokener) {
		return new JSONParser(tokener);
	}

	private final JSONTokener tokener;

	/**
	 * 
	 *
	 * @param tokener {@link JSONTokener}
	 */
	public JSONParser(JSONTokener tokener) {
		this.tokener = tokener;
	}

	// region parseTo

	/**
	 * 
	 *
	 * @param jsonObject {@link JSONObject}
	 * @param filter    
	 */
	public void parseTo(JSONObject jsonObject, Filter<MutablePair<String, Object>> filter) {
		final JSONTokener tokener = this.tokener;

		if (tokener.nextClean() != '{') {
			throw tokener.syntaxError("A JSONObject text must begin with '{'");
		}

		char prev;
		char c;
		String key;
		while (true) {
			prev = tokener.getPrevious();
			c = tokener.nextClean();
			switch (c) {
				case 0:
					throw tokener.syntaxError("A JSONObject text must end with '}'");
				case '}':
					return;
				case '{':
				case '[':
					if (prev == '{') {
						throw tokener.syntaxError("A JSONObject can not directly nest another JSONObject or JSONArray.");
					}
				default:
					tokener.back();
					key = tokener.nextStringValue();
			}

			// The key is followed by ':'.

			c = tokener.nextClean();
			if (c != ':') {
				throw tokener.syntaxError("Expected a ':' after a key");
			}

			jsonObject.set(key, tokener.nextValue(), filter, jsonObject.getConfig().isCheckDuplicate());

			// Pairs are separated by ','.

			switch (tokener.nextClean()) {
				case ';':
				case ',':
					if (tokener.nextClean() == '}') {
						// 
						return;
					}
					tokener.back();
					break;
				case '}':
					return;
				default:
					throw tokener.syntaxError("Expected a ',' or '}'");
			}
		}
	}

	/**
	 * 
	 */
	public void parseTo(JSONArray jsonArray, Filter<Mutable<Object>> filter) {
		final JSONTokener x = this.tokener;

		if (x.nextClean() != '[') {
			throw x.syntaxError("A JSONArray text must start with '['");
		}
		if (x.nextClean() != ']') {
			x.back();
			for (; ; ) {
				if (x.nextClean() == ',') {
					x.back();
					jsonArray.addRaw(JSONNull.NULL, filter);
				} else {
					x.back();
					jsonArray.addRaw(x.nextValue(), filter);
				}
				switch (x.nextClean()) {
					case ',':
						if (x.nextClean() == ']') {
							return;
						}
						x.back();
						break;
					case ']':
						return;
					default:
						throw x.syntaxError("Expected a ',' or ']'");
				}
			}
		}
	}
	// endregion
}
