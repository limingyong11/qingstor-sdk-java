// +-------------------------------------------------------------------------
// | Copyright (C) 2016 Yunify, Inc.
// +-------------------------------------------------------------------------
// | Licensed under the Apache License, Version 2.0 (the "License");
// | you may not use this work except in compliance with the License.
// | You may obtain a copy of the License in the LICENSE file, or at:
// |
// | http://www.apache.org/licenses/LICENSE-2.0
// |
// | Unless required by applicable law or agreed to in writing, software
// | distributed under the License is distributed on an "AS IS" BASIS,
// | WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// | See the License for the specific language governing permissions and
// | limitations under the License.
// +-------------------------------------------------------------------------

package com.qingstor.sdk.utils;

import com.qingstor.sdk.constants.QSConstant;
import com.qingstor.sdk.exception.QSException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/** Created by on 11/4/15. */
public class QSStringUtil {
	
	static CharsetEncoder asciiEncoder =
            	Charset.forName("US-ASCII").newEncoder(); // or "ISO-8859-1" for ISO Latin 1

    public static boolean isPureAscii(String v) {
        return asciiEncoder.canEncode(v);
    }

	public static String objectToJson(String key, Object o) {
        StringBuffer buffer = new StringBuffer("{ \"" + key + "\":");
        buffer.append(objectJSONKeyValue(key,o));
        buffer.append("}");
        return buffer.toString();
    }

    private static String objectJSONKeyValue(String key, Object o) {
        StringBuffer buffer = new StringBuffer(" \"" + key + "\":");
        buffer.append(objectJSONValue(o));
        return buffer.toString();
    }

    private static String objectJSONValue(Object o) {
        StringBuffer buffer = new StringBuffer();
        if (o instanceof List) {
            List lst = (List) o;
            buffer.append("[");
            for (int i = 0; i < lst.size(); i++) {
                buffer.append(objectJSONValue(lst.get(i)));
                if (i + 1 < lst.size()) {
                    buffer.append(",");
                }
            }
            buffer.append("]");
        } else if (o instanceof Map) {
            Map m = (Map) o;
            buffer.append(getMapToJson(m));
        } else if (o instanceof  Integer
                || o instanceof  Double
                || o instanceof  Boolean
                || o instanceof  Long
                || o instanceof  Float) {
            buffer.append(o);
        }else if (o instanceof  String) {
            buffer.append("\"").append(o).append("\"");
        }else {
            Map objMap = QSParamInvokeUtil.getRequestParams(o,"");
            buffer.append(getMapToJson(objMap));
        }
        return buffer.toString();
    }

    public static String getMapToJson(Map params) {
        Iterator iterator = params.entrySet().iterator();
        StringBuffer buffer = new StringBuffer("{");
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String key = (String) entry.getKey();
            Object bodyObj = params.get(key);
            buffer.append(objectJSONKeyValue(key,bodyObj));
            buffer.append(",");
        }
        if (buffer.length() > 1) {
            buffer.setLength(buffer.length() - 1);
        }
        buffer.append("}");
        return buffer.toString();
    }

    public static String percentEncode(String value, String encoding)
            throws UnsupportedEncodingException {
        return value != null
                ? URLEncoder.encode(value, encoding)
                        .replace("+", "%20")
                        .replace("*", "%2A")
                        .replace("%7E", "~")
                : null;
    }

    public static boolean isEmpty(String str) {
        if (str == null || "".equals(str.trim()) || "null".equalsIgnoreCase(str)) {
            return true;
        }
        return false;
    }

    public static String getUserAgent() {
    	String osName = System.getProperty("os.name"); //操作系统名称
        String langVersion = System.getProperty("java.version"); //java.version系统版本
        String userAgent =
                QSConstant.SDK_NAME
                        + "/"
                        + QSConstant.SDK_VERSION
                        + " ( java v"
                        + langVersion
                        + ";"
                        + osName
                        + ")";
        return userAgent;
    }

    public static String getParameterRequired(String paraName, String value) {
        return String.format("%s is required in %s ", paraName, value);
    }

    public static String getParameterValueNotAllowedError(
            String paraName, String value, String[] values) {

        StringBuffer buf = new StringBuffer();
        for (Object o : values) {
            buf.append(o.toString()).append(",");
        }
        if (buf.length() > 0) {
            buf.setLength(buf.length() - 1);
        }

        return String.format(
                "%s value %s is not allowed, should be one of %s ",
                paraName, value, buf.toString());
    }
    
    /**
     * Chinese characters transform
     *
     * @param str
     * @return
     */
    public static String asciiCharactersEncoding(String str) throws QSException {
        if (QSStringUtil.isEmpty(str)) {
            return "";
        }
        StringBuffer buffer = new StringBuffer();
        try {
            for (int i = 0; i < str.length(); i++) {
                String temp = str.substring(i, i + 1);
                if (!isPureAscii(temp)) {
                    buffer.append(URLEncoder.encode(temp, QSConstant.ENCODING_UTF8));
                } else {
                    buffer.append(temp);
                }
            }
            return buffer.toString();
        } catch (UnsupportedEncodingException e) {
            throw new QSException("UnsupportedEncodingException:", e);
        }
    }
    
    
    /**
     * urlpath characters transform
     *
     * @param str
     * @return
     */
    public static String urlCharactersEncoding(String str) throws QSException {
        if (QSStringUtil.isEmpty(str)) {
            return "";
        }
        StringBuffer buffer = new StringBuffer();
        try {
            for (int i = 0; i < str.length(); i++) {
                String temp = str.substring(i, i + 1);
                if (" ".equals(temp)) {
                    buffer.append("%20");
                } else if ("/".equals(temp) || "&".equals(temp)|| "=".equals(temp)|| ":".equals(temp)) {
                    buffer.append(temp);
                } else {
                    buffer.append(URLEncoder.encode(temp, QSConstant.ENCODING_UTF8));
                }
            }
            return buffer.toString();
        } catch (UnsupportedEncodingException e) {
            throw new QSException("UnsupportedEncodingException:", e);
        }
    }
}
