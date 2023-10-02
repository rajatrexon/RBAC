/*
 * Copyright ©2012 ESQ Management Solutions Pvt Ltd. All Rights Reserved.
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
package com.esq.rbac.service.contact.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MapUtil {

    public static Map<String, String> clone(Map<String, String> map) {
        Map<String, String> result = new HashMap<String, String>();
        for (String key : map.keySet()) {
            String keyCopy = new String(key);
            String valueCopy = new String(map.get(key));
            result.put(keyCopy, valueCopy);
        }
        return result;
    }

    public static Boolean equals(Map<String, String> first, Map<String, String> second) {
        Set<String> secondKeySet = new HashSet<String>(second.keySet());
        for (String key : first.keySet()) {
            if (secondKeySet.contains(key) == false) {
                return false;
            }
            secondKeySet.remove(key);
            if (second.containsKey(key) == false) {
                return false;
            }
            if (first.get(key).equals(second.get(key)) == false) {
                return false;
            }
        }
        if (secondKeySet.isEmpty() == false) {
            return false;
        }
        return true;
    }
}
