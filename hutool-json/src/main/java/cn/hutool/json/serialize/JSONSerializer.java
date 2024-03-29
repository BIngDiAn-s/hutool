package cn.hutool.json.serialize;

import cn.hutool.json.JSON;

/**
 * 
 */
@FunctionalInterface
public interface JSONSerializer<T extends JSON, V> {

	/**
	 * 
	 */
	void serialize(T json, V bean);
}
