/*
 * Copyright (c) 2024. looly(loolly@aliyun.com)
 * Hutool is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          https://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package cn.hutool.json.xml;

import java.io.Serializable;

/**
 * 
 */
public class ParseConfig implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	public static final int DEFAULT_MAXIMUM_NESTING_DEPTH = 512;

	/**
	 * 
	 *
	 * @return ParseConfig
	 */
	public static ParseConfig of() {
		return new ParseConfig();
	}

	/**
	 * 
	 */
	private boolean keepStrings;
	/**
	 * 
	 */
	private int maxNestingDepth = -1;

	/**
	 * 
	 * 
	 */
	public boolean isKeepStrings() {
		return keepStrings;
	}

	/**
	 * 
	 */
	public ParseConfig setKeepStrings(final boolean keepStrings) {
		this.keepStrings = keepStrings;
		return this;
	}

	/**
	 * 
	 */
	public int getMaxNestingDepth() {
		return maxNestingDepth;
	}

	/**
	 * 
	 */
	public ParseConfig setMaxNestingDepth(final int maxNestingDepth) {
		this.maxNestingDepth = maxNestingDepth;
		return this;
	}
}
