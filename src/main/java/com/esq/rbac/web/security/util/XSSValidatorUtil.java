/*
 * Copyright (c)2016 ESQ Management Solutions Pvt Ltd. All Rights Reserved.
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

package com.esq.rbac.web.security.util;

import com.esq.rbac.web.exception.ErrorInfo;
import com.esq.rbac.web.exception.ErrorInfoException;
import com.esq.rbac.web.util.EncryptionUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class XSSValidatorUtil {

	private static boolean raiseErrorOnXSS = true;
	private static boolean isRegexBased = false;
	private static boolean isDisabled = false;

	private static final Logger log = LoggerFactory
			.getLogger(XSSValidatorUtil.class);

	private static Map<String, Integer> hexvalueMap = new LinkedHashMap<String, Integer>();
	static {
		hexvalueMap.put("UNIX_LINES", Pattern.UNIX_LINES);
		hexvalueMap.put("CASE_INSENSITIVE", Pattern.CASE_INSENSITIVE);
		hexvalueMap.put("COMMENTS", Pattern.COMMENTS);
		hexvalueMap.put("MULTILINE", Pattern.MULTILINE);
		hexvalueMap.put("DOTALL", Pattern.DOTALL);
		hexvalueMap.put("UNICODE_CASE", Pattern.UNICODE_CASE);
		hexvalueMap.put("CANON_EQ", Pattern.CANON_EQ);
	}

	private static Pattern[] patterns = new Pattern[] {
			// Script fragments
			Pattern.compile("<script>(.*?)</script>", Pattern.CASE_INSENSITIVE),
			//other html tags
			Pattern.compile("<(.*?)", Pattern.CASE_INSENSITIVE),
						// src='...'
			Pattern.compile("src[\r\n]*=[\r\n]*\\\'(.*?)\\\'",
					Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
							| Pattern.DOTALL),
			Pattern.compile("src[\r\n]*=[\r\n]*\\\"(.*?)\\\"",
					Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
							| Pattern.DOTALL),

			// lonely script tags
			Pattern.compile("</script>", Pattern.CASE_INSENSITIVE),
			Pattern.compile("<script(.*?)>", Pattern.CASE_INSENSITIVE
					| Pattern.MULTILINE | Pattern.DOTALL),
			// eval(...)
			Pattern.compile("eval\\((.*?)\\)", Pattern.CASE_INSENSITIVE
					| Pattern.MULTILINE | Pattern.DOTALL),
			// expression(...)
			Pattern.compile("expression\\((.*?)\\)", Pattern.CASE_INSENSITIVE
					| Pattern.MULTILINE | Pattern.DOTALL),
			// javascript:...
			Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE),
			// vbscript:...
			Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE),
			// onload(...)=...
			Pattern.compile("onload(.*?)=", Pattern.CASE_INSENSITIVE
					| Pattern.MULTILINE | Pattern.DOTALL)};

	public static String handleForXSS(String value)
			throws UnsupportedEncodingException {
		if (value != null && !isDisabled) {
			// NOTE: It's highly recommended to use the ESAPI library and
			// uncomment the following line to
			// avoid encoded attacks.
			// value = ESAPI.encoder().canonicalize(value);

			if (raiseErrorOnXSS) {
				String checkString = value;
				try{
					checkString = URLDecoder.decode(value, "UTF-8");
				}
				catch(IllegalArgumentException e){
				}
				for (Pattern scriptPattern : patterns) {
					if (scriptPattern.matcher(checkString).find()) {
						try {
							log.error(
									"handleForXSS; patternFailed={}; checkString(encrypted)={}; ",
									scriptPattern.pattern(), EncryptionUtils
											.encryptPassword(checkString));
						} catch (Exception e) {
							log.error(
									"handleForXSS; patternFailed={}; checkString={}; ",
									scriptPattern.pattern(), checkString);
						}
						ErrorInfoException errorInfoException = new ErrorInfoException(ErrorInfo.XSS_ERROR_CODE, ErrorInfo.XSS_ERROR_MESSAGE);
						errorInfoException.getParameters().put("value", checkString);
						throw errorInfoException;
					}
				}
			} else {
				// Avoid null characters
				value = value.replaceAll("\0", "");

				// Remove all sections that match a pattern
				for (Pattern scriptPattern : patterns) {
					value = scriptPattern.matcher(value).replaceAll("");
				}
			}
		}
		return value;
	}

	public static void setPatternsRegex(String xssRegex) {
		try {
			ArrayNode jsonArray = (ArrayNode) new ObjectMapper().readTree(xssRegex);
			List<String> patternList = new LinkedList<String>();
			List<Pattern> patternListForSetter = new LinkedList<Pattern>();
			for (int i = 0; i < jsonArray.size(); i++) {

				ArrayNode regexPatternJsonArray = (ArrayNode) jsonArray.get(i).get("type");
				int finalValue = 0;
				for (int j = 0; j < regexPatternJsonArray.size(); j++) {
					String patterntype = regexPatternJsonArray.path(j).asText();
					finalValue = finalValue | hexvalueMap.get(patterntype);
				}
				if (finalValue != 0) {

					patternList.add(Pattern.compile(jsonArray.get(i).path("regexData").asText(),finalValue).pattern());
					patternListForSetter.add(Pattern.compile(jsonArray.get(i).path("regexData").asText(),finalValue));

				} else {
					patternList.add(Pattern.compile(jsonArray.get(i).path("regexData").asText()).pattern());
					patternListForSetter.add(Pattern.compile(jsonArray.get(i).path("regexData").asText(),finalValue));
				}

			}
			Pattern[] array = new Pattern[patternListForSetter.size()];
			patternListForSetter.toArray(array);
			patterns = array;
		} catch (Exception e) {
			log.error("setPatternsRegex; JSONException={};", e);
		}
	}

	public static boolean isRaiseErrorOnXSS() {
		return raiseErrorOnXSS;
	}

	public static void setRaiseErrorOnXSS(boolean raiseErrorOnXSSInput) {
		raiseErrorOnXSS = raiseErrorOnXSSInput;
	}

	public static boolean isRegexBased() {
		return isRegexBased;
	}

	public static void setRegexBased(boolean isRegexBasedInput) {
		isRegexBased = isRegexBasedInput;
	}

	public static boolean isDisabled() {
		return isDisabled;
	}

	public static void setDisabled(boolean isDisabledInput) {
		isDisabled = isDisabledInput;
	}

	public static Pattern[] getPatterns() {
		return patterns;
	}

}
