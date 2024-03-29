package cn.hutool.json;

import java.util.Iterator;

/**
 * 
 * 
 * @author looly
 * @since 4.0.12
 */
public class JSONObjectIter implements Iterable<JSONObject> {

	Iterator<Object> iterator;
	
	public JSONObjectIter(Iterator<Object> iterator) {
		this.iterator = iterator;
	}

	@Override
	public Iterator<JSONObject> iterator() {
		return new Iterator<JSONObject>() {

			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}

			@Override
			public JSONObject next() {
				return (JSONObject) iterator.next();
			}

			@Override
			public void remove() {
				iterator.remove();
			}
		};
	}

}
