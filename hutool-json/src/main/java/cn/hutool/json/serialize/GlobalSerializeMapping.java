package cn.hutool.json.serialize;

import cn.hutool.core.map.SafeConcurrentHashMap;
import cn.hutool.json.JSON;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Looly
 *
 */
public class GlobalSerializeMapping {

	private static Map<Type, JSONSerializer<? extends JSON, ?>> serializerMap;
	private static Map<Type, JSONDeserializer<?>> deserializerMap;

	static {
		serializerMap = new SafeConcurrentHashMap<>();
		deserializerMap = new SafeConcurrentHashMap<>();

		final TemporalAccessorSerializer localDateSerializer = new TemporalAccessorSerializer(LocalDate.class);
		serializerMap.put(LocalDate.class, localDateSerializer);
		deserializerMap.put(LocalDate.class, localDateSerializer);

		final TemporalAccessorSerializer localDateTimeSerializer = new TemporalAccessorSerializer(LocalDateTime.class);
		serializerMap.put(LocalDateTime.class, localDateTimeSerializer);
		deserializerMap.put(LocalDateTime.class, localDateTimeSerializer);

		final TemporalAccessorSerializer localTimeSerializer = new TemporalAccessorSerializer(LocalTime.class);
		serializerMap.put(LocalTime.class, localTimeSerializer);
		deserializerMap.put(LocalTime.class, localTimeSerializer);
	}

	/**
	 * 
	 */
	public static void put(Type type, JSONArraySerializer<?> serializer) {
		putInternal(type, serializer);
	}

	/**
	 * 
	 */
	public static void put(Type type, JSONObjectSerializer<?> serializer) {
		putInternal(type, serializer);
	}

	/**
	
	 */
	synchronized private static void putInternal(Type type, JSONSerializer<? extends JSON, ?> serializer) {
		if(null == serializerMap) {
			serializerMap = new SafeConcurrentHashMap<>();
		}
		serializerMap.put(type, serializer);
	}

	/**
	 * 
	 */
	synchronized public static void put(Type type, JSONDeserializer<?> deserializer) {
		if(null == deserializerMap) {
			deserializerMap = new ConcurrentHashMap<>();
		}
		deserializerMap.put(type, deserializer);
	}

	/**
	 */
	public static JSONSerializer<? extends JSON, ?> getSerializer(Type type){
		if(null == serializerMap) {
			return null;
		}
		return serializerMap.get(type);
	}

	/**
	 * 
	 */
	public static JSONDeserializer<?> getDeserializer(Type type){
		if(null == deserializerMap) {
			return null;
		}
		return deserializerMap.get(type);
	}
}
