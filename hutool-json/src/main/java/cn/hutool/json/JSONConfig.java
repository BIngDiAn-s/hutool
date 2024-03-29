package cn.hutool.json;

import cn.hutool.core.comparator.CompareUtil;

import java.io.Serializable;
import java.util.Comparator;

/**
 * 
 */
public class JSONConfig implements Serializable {
	private static final long serialVersionUID = 119730355204738278L;

	
	private Comparator<String> keyComparator;
	
	private boolean ignoreError;
	
	private boolean ignoreCase;
	/**
	 * 
	 */
	private String dateFormat;
	/**
	 * 
	 */
	private boolean ignoreNullValue = true;
	/**
	 * 
	 */
	private boolean transientSupport = true;

	/**
	 *
	 */
	private boolean stripTrailingZeros = true;

	/**
	 * 
	 */
	private boolean checkDuplicate;

	/**
	 * 
	 *
	 * @return JSONConfig
	 */
	public static JSONConfig create() {
		return new JSONConfig();
	}

	/**
	 * 
	
	 */
	@Deprecated
	public boolean isOrder() {
		return true;
	}

	/**
	
	 */
	@SuppressWarnings("unused")
	@Deprecated
	public JSONConfig setOrder(boolean order) {
		return this;
	}

	/**
	 *
	 */
	public Comparator<String> getKeyComparator() {
		return this.keyComparator;
	}

	/**
	 *
	 */
	public JSONConfig setNatureKeyComparator() {
		return setKeyComparator(CompareUtil.naturalComparator());
	}

	/**
	
	 * @since 5.7.21
	 */
	public JSONConfig setKeyComparator(Comparator<String> keyComparator) {
		this.keyComparator = keyComparator;
		return this;
	}

	/**
	 
	 */
	public boolean isIgnoreError() {
		return ignoreError;
	}

	/**
	 
	 */
	public JSONConfig setIgnoreError(boolean ignoreError) {
		this.ignoreError = ignoreError;
		return this;
	}

	/**
	
	 */
	public boolean isIgnoreCase() {
		return ignoreCase;
	}

	/**
	
	 */
	public JSONConfig setIgnoreCase(boolean ignoreCase) {
		this.ignoreCase = ignoreCase;
		return this;
	}

	/**
	
	 */
	public String getDateFormat() {
		return dateFormat;
	}

	/**
	 *
	 */
	public JSONConfig setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
		return this;
	}

	/**
	
	 */
	public boolean isIgnoreNullValue() {
		return this.ignoreNullValue;
	}

	/**
	
	 */
	public JSONConfig setIgnoreNullValue(boolean ignoreNullValue) {
		this.ignoreNullValue = ignoreNullValue;
		return this;
	}

	/**
	 
	 */
	public boolean isTransientSupport() {
		return this.transientSupport;
	}

	/**
	 * 
	 */
	public JSONConfig setTransientSupport(boolean transientSupport) {
		this.transientSupport = transientSupport;
		return this;
	}

	/**
	 * 
	 */
	public boolean isStripTrailingZeros() {
		return stripTrailingZeros;
	}

	/**
	 
	 */
	public JSONConfig setStripTrailingZeros(boolean stripTrailingZeros) {
		this.stripTrailingZeros = stripTrailingZeros;
		return this;
	}

	/**
	
	 */
	public boolean isCheckDuplicate() {
		return checkDuplicate;
	}

	/**
	
	 */
	public JSONConfig setCheckDuplicate(boolean checkDuplicate) {
		this.checkDuplicate = checkDuplicate;
		return this;
	}
}
