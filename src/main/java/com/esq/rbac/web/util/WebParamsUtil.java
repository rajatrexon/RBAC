/*
 * Copyright (c)2018 ESQ Management Solutions Pvt Ltd. All Rights Reserved.
 *
 * Permission to use, copy, modify, and distribute this software requires
 * a signed licensing agreement.
 *
 * IN NO EVENT SHALL ESQ BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL,
 * INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS, ARISING OUT OF
 * THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF ESQ HAS BEEN ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE. ESQ SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE.
 */
package com.esq.rbac.web.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Arrays;
import java.util.Enumeration;

public class WebParamsUtil {

	public static final MultiValueMap<String, String> paramsToMap(HttpServletRequest hsr) {
		MultiValueMap map = new LinkedMultiValueMap();
		Enumeration<String> e = hsr!=null?hsr.getParameterNames():null;
		if(e!=null) {
			while (e.hasMoreElements()) {
				String name = (String) e.nextElement();
				String[] values = hsr.getParameterValues(name);
				if(values!=null) {
					map.put(name, Arrays.asList(values));
				}
			}
		}
		return map;
	}
}
