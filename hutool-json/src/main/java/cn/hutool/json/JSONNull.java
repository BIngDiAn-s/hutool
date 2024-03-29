package cn.hutool.json;

import cn.hutool.core.util.StrUtil;

import java.io.Serializable;

/**
 * 
 */
public class JSONNull implements Serializable {
	private static final long serialVersionUID = 2633815155870764938L;

	/**
	 
	 */
	public static final JSONNull NULL = new JSONNull();

	/**
	 * A Null object is equal to the null value and to itself.
	
	 */
	@SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
	@Override
	public boolean equals(Object object) {
		return object == null || (object == this);
	}

	/**
	 * Get the "null" string value.
	 
	 *
	 * @return The string "null".
	 */
	@Override
	public String toString() {
		return StrUtil.NULL;
	}
}
