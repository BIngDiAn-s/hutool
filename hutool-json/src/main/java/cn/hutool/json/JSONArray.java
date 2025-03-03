package cn.hutool.json;

import cn.hutool.core.bean.BeanPath;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Filter;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.lang.mutable.Mutable;
import cn.hutool.core.lang.mutable.MutableObj;
import cn.hutool.core.lang.mutable.MutablePair;
import cn.hutool.core.text.StrJoiner;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.serialize.JSONWriter;

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;

/**

 * @author looly
 */
public class JSONArray implements JSON, JSONGetter<Integer>, List<Object>, RandomAccess {
	private static final long serialVersionUID = 2664900568717612292L;

	/**
	
	 */
	public static final int DEFAULT_CAPACITY = 10;

	/**
	
	 */
	private List<Object> rawList;
	/**
	
	 */
	private final JSONConfig config;

	// region Constructors

	/**

	 */
	public JSONArray() {
		this(DEFAULT_CAPACITY);
	}

	/**
	 *
	 * @since 3.2.2
	 */
	public JSONArray(int initialCapacity) {
		this(initialCapacity, JSONConfig.create());
	}

	/**
	 * 
	 * @since 4.6.5
	 */
	public JSONArray(JSONConfig config) {
		this(DEFAULT_CAPACITY, config);
	}

	/**
	 * 
	 * @since 4.1.19
	 */
	public JSONArray(int initialCapacity, JSONConfig config) {
		this.rawList = new ArrayList<>(initialCapacity);
		this.config = ObjectUtil.defaultIfNull(config, JSONConfig::create);
	}

	/**
	 * 
	 */
	public JSONArray(Object object) throws JSONException {
		this(object, true);
	}

	/**
	 *
	 */
	public JSONArray(Object object, boolean ignoreNullValue) throws JSONException {
		this(object, JSONConfig.create().setIgnoreNullValue(ignoreNullValue));
	}

	/**
	 *
	 */
	public JSONArray(Object object, JSONConfig jsonConfig) throws JSONException {
		this(object, jsonConfig, null);
	}

	/**
	 *
	 */
	public JSONArray(Object object, JSONConfig jsonConfig, Filter<Mutable<Object>> filter) throws JSONException {
		this(DEFAULT_CAPACITY, jsonConfig);
		ObjectMapper.of(object).map(this, filter);
	}
	// endregion

	@Override
	public JSONConfig getConfig() {
		return this.config;
	}

	/**
	 * 
	 */
	public JSONArray setDateFormat(String format) {
		this.config.setDateFormat(format);
		return this;
	}

	/**
	 */
	public String join(String separator) throws JSONException {
		return StrJoiner.of(separator)
				.append(this, InternalJSONUtil::valueToString).toString();
	}

	@Override
	public Object get(int index) {
		return this.rawList.get(index);
	}

	@Override
	public Object getObj(Integer index, Object defaultValue) {
		return (index < 0 || index >= this.size()) ? defaultValue : this.rawList.get(index);
	}

	@Override
	public Object getByPath(String expression) {
		return BeanPath.create(expression).get(this);
	}

	@Override
	public <T> T getByPath(String expression, Class<T> resultType) {
		return JSONConverter.jsonConvert(resultType, getByPath(expression), getConfig());
	}

	@Override
	public void putByPath(String expression, Object value) {
		BeanPath.create(expression).set(this, value);
	}

	/**
	 * Append an object value. This increases the array's length by one. <br>
	 * 
	 *
	 * @param value 
	 * @return this.
	 * @see #set(Object)
	 */
	public JSONArray put(Object value) {
		return set(value);
	}

	/**
	 * Append an object value. This increases the array's length by one. <br>
	
	 *
	 * @param valu
	 * @return this.
	 * @since 5.2.5
	 */
	public JSONArray set(Object value) {
		this.add(value);
		return this;
	}

	/**
	 * 
	 * @see #set(int, Object)
	 */
	public JSONArray put(int index, Object value) throws JSONException {
		this.set(index, value);
		return this;
	}

	/**
	 * 
	 */
	public JSONObject toJSONObject(JSONArray names) throws JSONException {
		if (names == null || names.size() == 0 || this.size() == 0) {
			return null;
		}
		final JSONObject jo = new JSONObject(this.config);
		for (int i = 0; i < names.size(); i += 1) {
			jo.set(names.getStr(i), this.getObj(i));
		}
		return jo;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((rawList == null) ? 0 : rawList.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final JSONArray other = (JSONArray) obj;
		if (rawList == null) {
			return other.rawList == null;
		} else {
			return rawList.equals(other.rawList);
		}
	}

	@Override
	public Iterator<Object> iterator() {
		return rawList.iterator();
	}

	/**
	 * 
	 */
	public Iterable<JSONObject> jsonIter() {
		return new JSONObjectIter(iterator());
	}

	@Override
	public int size() {
		return rawList.size();
	}

	@Override
	public boolean isEmpty() {
		return rawList.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return rawList.contains(o);
	}

	@Override
	public Object[] toArray() {
		return rawList.toArray();
	}

	@Override
	@SuppressWarnings({"unchecked"})
	public <T> T[] toArray(T[] a) {
		return (T[]) JSONConverter.toArray(this, a.getClass().getComponentType());
	}

	@Override
	public boolean add(Object e) {
		return addRaw(JSONUtil.wrap(e, this.config), null);
	}

	@Override
	public Object remove(int index) {
		return index >= 0 && index < this.size() ? this.rawList.remove(index) : null;
	}

	@Override
	public boolean remove(Object o) {
		return rawList.remove(o);
	}

	@SuppressWarnings("NullableProblems")
	@Override
	public boolean containsAll(Collection<?> c) {
		return rawList.containsAll(c);
	}

	@SuppressWarnings("NullableProblems")
	@Override
	public boolean addAll(Collection<?> c) {
		if (CollUtil.isEmpty(c)) {
			return false;
		}
		for (Object obj : c) {
			this.add(obj);
		}
		return true;
	}

	@SuppressWarnings("NullableProblems")
	@Override
	public boolean addAll(int index, Collection<?> c) {
		if (CollUtil.isEmpty(c)) {
			return false;
		}
		final ArrayList<Object> list = new ArrayList<>(c.size());
		for (Object object : c) {
			list.add(JSONUtil.wrap(object, this.config));
		}
		return rawList.addAll(index, list);
	}

	@SuppressWarnings("NullableProblems")
	@Override
	public boolean removeAll(Collection<?> c) {
		return this.rawList.removeAll(c);
	}

	@SuppressWarnings("NullableProblems")
	@Override
	public boolean retainAll(Collection<?> c) {
		return this.rawList.retainAll(c);
	}

	@Override
	public void clear() {
		this.rawList.clear();

	}

	/**
	 *
	 */
	@Override
	public Object set(int index, Object element) {
		return set(index, element, null);
	}

	/**
	 * 
	 */
	public Object set(int index, Object element, Filter<MutablePair<Integer, Object>> filter) {
		// 
		if (null != filter) {
			final MutablePair<Integer, Object> pair = new MutablePair<>(index, element);
			if (filter.accept(pair)) {
				// 
				element = pair.getValue();
			}
		}

		if (index >= size()) {
			add(index, element);
		}
		return this.rawList.set(index, JSONUtil.wrap(element, this.config));
	}

	@Override
	public void add(int index, Object element) {
		if (index < 0) {
			throw new JSONException("JSONArray[{}] not found.", index);
		}
		if (index < this.size()) {
			InternalJSONUtil.testValidity(element);
			this.rawList.add(index, JSONUtil.wrap(element, this.config));
		} else {
			// 
			Validator.checkIndexLimit(index, this.size());
			while (index != this.size()) {
				this.add(JSONNull.NULL);
			}
			this.set(element);
		}

	}

	@Override
	public int indexOf(Object o) {
		return this.rawList.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		return this.rawList.lastIndexOf(o);
	}

	@Override
	public ListIterator<Object> listIterator() {
		return this.rawList.listIterator();
	}

	@Override
	public ListIterator<Object> listIterator(int index) {
		return this.rawList.listIterator(index);
	}

	@Override
	public List<Object> subList(int fromIndex, int toIndex) {
		return this.rawList.subList(fromIndex, toIndex);
	}

	/**
	 * 
	 */
	public Object toArray(Class<?> arrayClass) {
		return JSONConverter.toArray(this, arrayClass);
	}

	/**
	 * 
	 */
	public <T> List<T> toList(Class<T> elementType) {
		return JSONConverter.toList(this, elementType);
	}

	/**
	 * 
	 */
	@Override
	public String toString() {
		return this.toJSONString(0);
	}

	/**
	 * 
	 */
	public String toJSONString(int indentFactor, Filter<MutablePair<Object, Object>> filter) {
		final StringWriter sw = new StringWriter();
		synchronized (sw.getBuffer()) {
			return this.write(sw, indentFactor, 0, filter).toString();
		}
	}

	@Override
	public Writer write(Writer writer, int indentFactor, int indent) throws JSONException {
		return write(writer, indentFactor, indent, null);
	}

	/**
	 * 
	 */
	public Writer write(Writer writer, int indentFactor, int indent, Filter<MutablePair<Object, Object>> filter) throws JSONException {
		final JSONWriter jsonWriter = JSONWriter.of(writer, indentFactor, indent, config).beginArray();

		CollUtil.forEach(this, (value, index) -> jsonWriter.writeField(new MutablePair<>(index, value), filter));
		jsonWriter.end();
		// 
		return writer;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		final JSONArray clone = (JSONArray) super.clone();
		clone.rawList = ObjectUtil.clone(this.rawList);
		return clone;
	}

	/**
	 * 
	 */
	protected boolean addRaw(Object obj, Filter<Mutable<Object>> filter) {
		// 
		if (null != filter) {
			final Mutable<Object> mutable = new MutableObj<>(obj);
			if (filter.accept(mutable)) {
				// 
				obj = mutable.get();
			}else{
				// 
				return false;
			}
		}
		return this.rawList.add(obj);
	}
}
