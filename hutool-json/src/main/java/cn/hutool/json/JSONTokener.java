package cn.hutool.json;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

/**
 * 
 */
public class JSONTokener {

	private long character;
	/**
	 * 
	 */
	private boolean eof;
	/**
	 * 
	 */
	private long index;
	/**
	 * 
	 */
	private long line;
	/**
	 *
	 */
	private char previous;
	/**
	 * 
	 */
	private boolean usePrevious;
	/**
	 * 
	 */
	private final Reader reader;

	/**
	 * 
	 */
	private final JSONConfig config;

	// ------------------------------------------------------------------------------------ Constructor start

	/**
	 * 
	 */
	public JSONTokener(Reader reader, JSONConfig config) {
		this.reader = reader.markSupported() ? reader : new BufferedReader(reader);
		this.eof = false;
		this.usePrevious = false;
		this.previous = 0;
		this.index = 0;
		this.character = 1;
		this.line = 1;
		this.config = config;
	}

	/**
	 * 
	 */
	public JSONTokener(InputStream inputStream, JSONConfig config) throws JSONException {
		this(IoUtil.getUtf8Reader(inputStream), config);
	}

	/**
	 *
	 */
	public JSONTokener(CharSequence s, JSONConfig config) {
		this(new StringReader(StrUtil.str(s)), config);
	}
	// ------------------------------------------------------------------------------------ Constructor end

	/**
	 * 
	 */
	public void back() throws JSONException {
		if (this.usePrevious || this.index <= 0) {
			throw new JSONException("Stepping back two steps is not supported");
		}
		this.index -= 1;
		this.character -= 1;
		this.usePrevious = true;
		this.eof = false;
	}

	/**
	 * 
	 */
	public boolean end() {
		return this.eof && false == this.usePrevious;
	}

	/**
	 
	 */
	public boolean more() throws JSONException {
		this.next();
		if (this.end()) {
			return false;
		}
		this.back();
		return true;
	}

	/**
	 *
	 */
	public char next() throws JSONException {
		int c;
		if (this.usePrevious) {
			this.usePrevious = false;
			c = this.previous;
		} else {
			try {
				c = this.reader.read();
			} catch (IOException exception) {
				throw new JSONException(exception);
			}

			if (c <= 0) { // End of stream
				this.eof = true;
				c = 0;
			}
		}
		this.index += 1;
		if (this.previous == '\r') {
			this.line += 1;
			this.character = c == '\n' ? 0 : 1;
		} else if (c == '\n') {
			this.line += 1;
			this.character = 0;
		} else {
			this.character += 1;
		}
		this.previous = (char) c;
		return this.previous;
	}

	/**
	 * Get the last character read from the input or '\0' if nothing has been read yet.
	 *
	 * @return the last character read from the input.
	 */
	protected char getPrevious() {
		return this.previous;
	}

	/**
	 * 
	 */
	public char next(char c) throws JSONException {
		char n = this.next();
		if (n != c) {
			throw this.syntaxError("Expected '" + c + "' and instead saw '" + n + "'");
		}
		return n;
	}

	/**
	 *
	 */
	public String next(int n) throws JSONException {
		if (n == 0) {
			return "";
		}

		char[] chars = new char[n];
		int pos = 0;
		while (pos < n) {
			chars[pos] = this.next();
			if (this.end()) {
				throw this.syntaxError("Substring bounds error");
			}
			pos += 1;
		}
		return new String(chars);
	}

	/**
	 * 
	 * 
	 */
	public char nextClean() throws JSONException {
		char c;
		while (true) {
			c = this.next();
			if (c == 0 || c > ' ') {
				return c;
			}
		}
	}

	/**
	 * 
	 */
	public String nextString(char quote) throws JSONException {
		char c;
		StringBuilder sb = new StringBuilder();
		while (true) {
			c = this.next();
			switch (c) {
				case 0:
				case '\n':
				case '\r':
					throw this.syntaxError("Unterminated string");
				case '\\':// 转义符
					c = this.next();
					switch (c) {
						case 'b':
							sb.append('\b');
							break;
						case 't':
							sb.append('\t');
							break;
						case 'n':
							sb.append('\n');
							break;
						case 'f':
							sb.append('\f');
							break;
						case 'r':
							sb.append('\r');
							break;
						case 'u':// Unicode符
							sb.append((char) Integer.parseInt(this.next(4), 16));
							break;
						case '"':
						case '\'':
						case '\\':
						case '/':
							sb.append(c);
							break;
						default:
							throw this.syntaxError("Illegal escape.");
					}
					break;
				default:
					if (c == quote) {
						return sb.toString();
					}
					sb.append(c);
			}
		}
	}

	/**
	 * Get the text up but not including the specified character or the end of line, whichever comes first. <br>
	 * 
	 */
	public String nextTo(char delimiter) throws JSONException {
		StringBuilder sb = new StringBuilder();
		for (; ; ) {
			char c = this.next();
			if (c == delimiter || c == 0 || c == '\n' || c == '\r') {
				if (c != 0) {
					this.back();
				}
				return sb.toString().trim();
			}
			sb.append(c);
		}
	}

	/**
	 * Get the text up but not including one of the specified delimiter characters or the end of line, whichever comes first.
	 *
	 * @param delimiters A set of delimiter characters.
	 * @return A string, trimmed.
	 */
	public String nextTo(String delimiters) throws JSONException {
		char c;
		StringBuilder sb = new StringBuilder();
		for (; ; ) {
			c = this.next();
			if (delimiters.indexOf(c) >= 0 || c == 0 || c == '\n' || c == '\r') {
				if (c != 0) {
					this.back();
				}
				return sb.toString().trim();
			}
			sb.append(c);
		}
	}

	/**
	 * 
	 */
	public String nextStringValue(){
		char c = this.nextClean();

		switch (c) {
			case '"':
			case '\'':
				return this.nextString(c);
			case '{':
			case '[':
				throw this.syntaxError("Sting value must be not begin with a '{' or '['");
		}

		/*
		 * Handle unquoted text. This could be the values true, false, or null, or it can be a number.
		 * An implementation (such as this one) is allowed to also accept non-standard forms. Accumulate
		 * characters until we reach the end of the text or a formatting character.
		 */

		final StringBuilder sb = new StringBuilder();
		while (c >= ' ' && ",:]}/\\\"[{;=#".indexOf(c) < 0) {
			sb.append(c);
			c = this.next();
		}
		this.back();

		final String string = sb.toString().trim();
		if (string.isEmpty()) {
			throw this.syntaxError("Missing value");
		}
		return string;
	}

	/**
	 * 
	 */
	public Object nextValue() throws JSONException {
		char c = this.nextClean();
		String string;

		switch (c) {
			case '"':
			case '\'':
				return this.nextString(c);
			case '{':
				this.back();
				try {
					return new JSONObject(this, this.config);
				} catch (final StackOverflowError e) {
					throw new JSONException("JSONObject depth too large to process.", e);
				}
			case '[':
				this.back();
				try {
					return new JSONArray(this, this.config);
				} catch (final StackOverflowError e) {
					throw new JSONException("JSONArray depth too large to process.", e);
				}
		}

		/*
		 * Handle unquoted text. This could be the values true, false, or null, or it can be a number.
		 * An implementation (such as this one) is allowed to also accept non-standard forms. Accumulate
		 * characters until we reach the end of the text or a formatting character.
		 */

		final StringBuilder sb = new StringBuilder();
		while (c >= ' ' && ",:]}/\\\"[{;=#".indexOf(c) < 0) {
			sb.append(c);
			c = this.next();
		}
		this.back();

		string = sb.toString().trim();
		if (string.isEmpty()) {
			throw this.syntaxError("Missing value");
		}
		return InternalJSONUtil.stringToValue(string);
	}

	/**
	 * Skip characters until the next character is the requested character. If the requested character is not found, no characters are skipped. 在遇到指定字符前，跳过其它字符。如果字符未找到，则不跳过任何字符。
	 *
	 * 
	 */
	public char skipTo(char to) throws JSONException {
		char c;
		try {
			long startIndex = this.index;
			long startCharacter = this.character;
			long startLine = this.line;
			this.reader.mark(1000000);
			do {
				c = this.next();
				if (c == 0) {
					this.reader.reset();
					this.index = startIndex;
					this.character = startCharacter;
					this.line = startLine;
					return c;
				}
			} while (c != to);
		} catch (IOException exception) {
			throw new JSONException(exception);
		}
		this.back();
		return c;
	}

	/**
	 * Ma
	 */
	public JSONException syntaxError(String message) {
		return new JSONException(message + this);
	}

	/*
	 *
	 * @return {@link JSONArray}
	 */
	public JSONArray toJSONArray() {
		JSONArray jsonArray = new JSONArray(this.config);
		if (this.nextClean() != '[') {
			throw this.syntaxError("A JSONArray text must start with '['");
		}
		if (this.nextClean() != ']') {
			this.back();
			while (true) {
				if (this.nextClean() == ',') {
					this.back();
					jsonArray.add(JSONNull.NULL);
				} else {
					this.back();
					jsonArray.add(this.nextValue());
				}
				switch (this.nextClean()) {
					case ',':
						if (this.nextClean() == ']') {
							return jsonArray;
						}
						this.back();
						break;
					case ']':
						return jsonArray;
					default:
						throw this.syntaxError("Expected a ',' or ']'");
				}
			}
		}
		return jsonArray;
	}

	/**
	 * Make a printable string of this JSONTokener.
	 *
	 * @return " at {index} [character {character} line {line}]"
	 */
	@Override
	public String toString() {
		return " at " + this.index + " [character " + this.character + " line " + this.line + "]";
	}
}
