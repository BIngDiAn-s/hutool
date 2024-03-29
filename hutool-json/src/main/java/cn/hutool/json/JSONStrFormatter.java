package cn.hutool.json;

import cn.hutool.core.util.CharUtil;
import cn.hutool.core.util.StrUtil;

/**
 * 
 * @author looly
 * @since 3.1.2
 */
public class JSONStrFormatter {

	/**
	 * 
	 */
	private static final String SPACE = "    ";
	/**
	 * 
	 */
	private static final char NEW_LINE = StrUtil.C_LF;

	/**
	 * 
	 */
	public static String format(String json) {
		final StringBuilder result = new StringBuilder();

		Character wrapChar = null;
		boolean isEscapeMode = false;
		int length = json.length();
		int number = 0;
		char key;
		for (int i = 0; i < length; i++) {
			key = json.charAt(i);

			if (CharUtil.DOUBLE_QUOTES == key || CharUtil.SINGLE_QUOTE == key) {
				if (null == wrapChar) {
					//
					wrapChar = key;
				} else if (wrapChar.equals(key)) {
					if (isEscapeMode) {
						//
						isEscapeMode = false;
					}

					//
					wrapChar = null;
				}

				if ((i > 1) && (json.charAt(i - 1) == CharUtil.COLON)) {
					result.append(CharUtil.SPACE);
				}

				result.append(key);
				continue;
			}

			if (CharUtil.BACKSLASH == key) {
				if (null != wrapChar) {
					//
					isEscapeMode = !isEscapeMode;
					result.append(key);
					continue;
				} else {
					result.append(key);
				}
			}

			if (null != wrapChar) {
				//
				result.append(key);
				continue;
			}

			//
			if ((key == CharUtil.BRACKET_START) || (key == CharUtil.DELIM_START)) {
				//
				if ((i > 1) && (json.charAt(i - 1) == CharUtil.COLON)) {
					result.append(NEW_LINE);
					result.append(indent(number));
				}
				result.append(key);
				//
				result.append(NEW_LINE);
				//
				number++;
				result.append(indent(number));

				continue;
			}

			// 
			if ((key == CharUtil.BRACKET_END) || (key == CharUtil.DELIM_END)) {
				// 
				result.append(NEW_LINE);
				// 
				number--;
				result.append(indent(number));
				// 
				result.append(key);
				// 
//				if (((i + 1) < length) && (json.charAt(i + 1) != ',')) {
//					result.append(NEW_LINE);
//				}
				// 
				continue;
			}

			// 
			if ((key == ',')) {
				result.append(key);
				result.append(NEW_LINE);
				result.append(indent(number));
				continue;
			}

			if ((i > 1) && (json.charAt(i - 1) == CharUtil.COLON)) {
				result.append(CharUtil.SPACE);
			}

			//
			result.append(key);
		}

		return result.toString();
	}

	/**
	 * 
	 */
	private static String indent(int number) {
		return StrUtil.repeat(SPACE, number);
	}
}
