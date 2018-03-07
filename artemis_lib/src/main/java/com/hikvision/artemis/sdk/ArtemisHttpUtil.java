package com.hikvision.artemis.sdk;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hikvision.artemis.sdk.config.ArtemisConfig;
import com.hikvision.artemis.sdk.constant.*;
import com.hikvision.artemis.sdk.enums.Method;
import com.hikvision.artemis.sdk.util.MessageDigestUtil;

import c_ga_org.apache.commons.lang3.StringUtils;

public class ArtemisHttpUtil {

    //自定义参与签名Header前缀（可选,默认只有"X-Ca-"开头的参与到Header签名）
    private final static List<String> CUSTOM_HEADERS_TO_SIGN_PREFIX = new ArrayList<String>();

    //调用网关成功的标志,标志位
    private final static String SUCC_PRE = "2";

    /**
     * get请求
     *
     * @param path        artemis配置的get请求的路径 是一个数组长度为1的Hashmap集合，只存一组数据，key为http的请求方式，value为host后面的path路径。
     * @param querys      map类型  get请求的url查询参数（url中的query参数,没有就是为空）形如 "?aa=1&&bb=2"形式参数变成map键值对 query.put("aa","1");query.put("bb","2")
     * @param accept      指定客户端能够接收的内容类型，该参数传空时的默认全部类型接受
     * @param contentType 请求的与实体对应的MIME信息，该参数传空时的取默认值
     * @return 返回get请求的结果
     */
    public static String doGetArtemis(Map<String, String> path, Map<String, String> querys, String accept, String contentType) {

        /**
         * 根据传入的path获取是请求是http还是https
         */
        String httpSchema = (String) path.keySet().toArray()[0];

        if (httpSchema == null || StringUtils.isEmpty(httpSchema))
            throw new RuntimeException(MsgConstants.HTTP_SCHEMA_ERROR + "httpSchema: " + httpSchema);

        String responseStr = null;
        try {
            Map<String, String> headers = new HashMap<String, String>();
            //（必填）根据期望的Response内容类型设置
            if (StringUtils.isNotBlank(accept)) {
                headers.put(HttpHeader.HTTP_HEADER_ACCEPT, accept);
            } else {
                headers.put(HttpHeader.HTTP_HEADER_ACCEPT, "*/*");
            }
            //请求的与实体对应的MIME信息
            if (StringUtils.isNotBlank(contentType)) {
                headers.put(HttpHeader.HTTP_HEADER_CONTENT_TYPE, contentType);
            } else {
                headers.put(HttpHeader.HTTP_HEADER_CONTENT_TYPE, ContentType.CONTENT_TYPE_TEXT);
            }

            CUSTOM_HEADERS_TO_SIGN_PREFIX.clear();
            Request request = new Request(Method.GET, httpSchema + ArtemisConfig.host, path.get(httpSchema), ArtemisConfig.appKey, ArtemisConfig.appSecret, Constants.DEFAULT_TIMEOUT);
            request.setHeaders(headers);
            request.setSignHeaderPrefixList(CUSTOM_HEADERS_TO_SIGN_PREFIX);

            request.setQuerys(querys);
            //调用服务端
            Response response = Client.execute(request);

            responseStr = getResponseResult(response);

        } catch (Exception e) {
            Log.e("ArtemisHttpUtil", "ArtemisHttpUtil.doGetArtemis", e);
        }
        return responseStr;
    }

    /**
     * postForm请求，postForm请求包含query参数和form表单参数
     *
     * @param path        artemis配置的get请求的路径 是一个数组长度为1的Hashmap集合，只存一组数据，key为http的请求方式，value为host后面的path路径。
     * @param paramMap    Form表单请求的参数，键值对形式的map
     * @param querys      map类型  post请求的url查询参数（url中的query参数,没有就是为空）形如 "?aa=1&&bb=2"形式参数变成map键值对 query.put("aa","1");query.put("bb","2")
     * @param accept      指定客户端能够接收的内容类型，该参数传空时的默认全部类型接受
     * @param contentType 请求的与实体对应的MIME信息，该参数传空时的取默认值("application/x-www-form-urlencoded; charset=UTF-8")
     * @return 返回postForm请求的结果
     */
    public static String doPostFormArtemis(Map<String, String> path, Map<String, String> paramMap, Map<String, String> querys, String accept, String contentType) {
        /**
         * 根据传入的path获取是请求是http还是https
         */
        String httpSchema = (String) path.keySet().toArray()[0];

        if (httpSchema == null || StringUtils.isEmpty(httpSchema))
            throw new RuntimeException(MsgConstants.HTTP_SCHEMA_ERROR + "httpSchema: " + httpSchema);
        String responseStr = null;
        try {
            Map<String, String> headers = new HashMap<String, String>();
            //（必填）根据期望的Response内容类型设置
            if (StringUtils.isNotBlank(accept)) {
                headers.put(HttpHeader.HTTP_HEADER_ACCEPT, accept);
            } else {
                headers.put(HttpHeader.HTTP_HEADER_ACCEPT, "*/*");
            }
            //请求的与实体对应的MIME信息
            if (StringUtils.isNotBlank(contentType)) {
                headers.put(HttpHeader.HTTP_HEADER_CONTENT_TYPE, contentType);
            } else {
                headers.put(HttpHeader.HTTP_HEADER_CONTENT_TYPE, ContentType.CONTENT_TYPE_FORM);
            }
            CUSTOM_HEADERS_TO_SIGN_PREFIX.clear();
            Request request = new Request(Method.POST_FORM, httpSchema + ArtemisConfig.host,
                    path.get(httpSchema), ArtemisConfig.appKey, ArtemisConfig.appSecret, Constants.DEFAULT_TIMEOUT);
            request.setHeaders(headers);
            request.setSignHeaderPrefixList(CUSTOM_HEADERS_TO_SIGN_PREFIX);
            //postForm请求的query参数
            request.setQuerys(querys);
            //postForm请求的表单参数
            request.setBodys(paramMap);

            //调用服务端
            Response response = Client.execute(request);

            responseStr = getResponseResult(response);

        } catch (Exception e) {
            Log.e("ArtemisHttpUtil", "ArtemisHttpUtil.doPostFormArtemis", e);
        }
        return responseStr;
    }

    /**
     * postString请求
     *
     * @param path        artemis配置的get请求的路径 是一个数组长度为1的Hashmap集合，只存一组数据，key为http的请求方式，value为host后面的path路径。
     * @param body        postString String请求体
     * @param querys      map类型  post请求的url查询参数（url中的query参数,没有就是为空）形如 "?aa=1&&bb=2"形式参数变成map键值对 query.put("aa","1");query.put("bb","2")
     * @param accept      指定客户端能够接收的内容类型，该参数传空时的默认全部类型接受
     * @param contentType 请求的与实体对应的MIME信息，该参数传空时的取默认值("application/text; charset=UTF-8")
     * @return postString请求返回值
     */
    public static String doPostStringArtemis(Map<String, String> path, String body,
                                             Map<String, String> querys, String accept, String contentType) {
        /**
         * 根据传入的path获取是请求是http还是https
         */
        String httpSchema = (String) path.keySet().toArray()[0];

        if (httpSchema == null || StringUtils.isEmpty(httpSchema))
            throw new RuntimeException(MsgConstants.HTTP_SCHEMA_ERROR + "httpSchema: " + httpSchema);
        String responseStr = null;
        try {

            Map<String, String> headers = new HashMap<String, String>();
            //（必填）根据期望的Response内容类型设置
            if (StringUtils.isNotBlank(accept)) {
                headers.put(HttpHeader.HTTP_HEADER_ACCEPT, accept);
            } else {
                headers.put(HttpHeader.HTTP_HEADER_ACCEPT, "*/*");
            }
            //（可选）Body MD5,服务端会校验Body内容是否被篡改,建议Body非Form表单时添加此Header
//	        if(StringUtils.isNotBlank(body)){
//	        	headers.put(HttpHeader.HTTP_HEADER_CONTENT_MD5, MessageDigestUtil.base64AndMD5(body));
//	        }
            //（POST/PUT请求必选）请求Body内容格式请求的与实体对应的MIME信息
            if (StringUtils.isNotBlank(contentType)) {
                headers.put(HttpHeader.HTTP_HEADER_CONTENT_TYPE, contentType);
            } else {
                headers.put(HttpHeader.HTTP_HEADER_CONTENT_TYPE, ContentType.CONTENT_TYPE_TEXT);
            }

            CUSTOM_HEADERS_TO_SIGN_PREFIX.clear();
            Request request = new Request(Method.POST_STRING, httpSchema + ArtemisConfig.host,
                    path.get(httpSchema), ArtemisConfig.appKey, ArtemisConfig.appSecret, Constants.DEFAULT_TIMEOUT);
            request.setHeaders(headers);
            request.setSignHeaderPrefixList(CUSTOM_HEADERS_TO_SIGN_PREFIX);
            //请求的query
            request.setQuerys(querys);
            //请求的bodyString
            request.setStringBody(body);
            //调用服务端
            Response response = Client.execute(request);

            responseStr = getResponseResult(response);

        } catch (Exception e) {
            Log.e("ArtemisHttpUtil", "ArtemisHttpUtil.doPostStringArtemis", e);
        }
        return responseStr;
    }

    /**
     * postBytes请求
     *
     * @param path        artemis配置的get请求的路径 是一个数组长度为1的Hashmap集合，只存一组数据，key为http的请求方式，value为host后面的path路径。
     * @param bytesBody   请求体，byte字节
     * @param querys      map类型  post请求的url查询参数（url中的query参数,没有就是为空）形如 "?aa=1&&bb=2"形式参数变成map键值对 query.put("aa","1");query.put("bb","2")
     * @param accept      指定客户端能够接收的内容类型，该参数传空时的默认全部类型接受
     * @param contentType 请求的与实体对应的MIME信息，该参数传空时的取默认值("application/text; charset=UTF-8")
     * @return 返回postBytes请求的结果
     */
    public static String doPostBytesArtemis(Map<String, String> path, byte[] bytesBody, Map<String, String> querys, String accept, String contentType) {
        /**
         * 根据传入的path获取是请求是http还是https
         */
        String httpSchema = (String) path.keySet().toArray()[0];

        if (httpSchema == null || StringUtils.isEmpty(httpSchema))
            throw new RuntimeException(MsgConstants.HTTP_SCHEMA_ERROR + "httpSchema: " + httpSchema);

        String responseStr = null;
        try {
            Map<String, String> headers = new HashMap<String, String>();
            //（必填）根据期望的Response内容类型设置
            if (StringUtils.isNotBlank(accept)) {
                headers.put(HttpHeader.HTTP_HEADER_ACCEPT, accept);
            } else {
                headers.put(HttpHeader.HTTP_HEADER_ACCEPT, "*/*");
            }
            //（可选）Body MD5,服务端会校验Body内容是否被篡改,建议Body非Form表单时添加此Header
            if (bytesBody != null) {
                headers.put(HttpHeader.HTTP_HEADER_CONTENT_MD5, MessageDigestUtil.base64AndMD5(bytesBody));
            }
            //（POST/PUT请求必选）请求Body内容格式
            if (StringUtils.isNotBlank(contentType)) {
                headers.put(HttpHeader.HTTP_HEADER_CONTENT_TYPE, contentType);
            } else {
                headers.put(HttpHeader.HTTP_HEADER_CONTENT_TYPE, ContentType.CONTENT_TYPE_TEXT);
            }
            CUSTOM_HEADERS_TO_SIGN_PREFIX.clear();
            Request request = new Request(Method.POST_BYTES, httpSchema + ArtemisConfig.host,
                    path.get(httpSchema), ArtemisConfig.appKey, ArtemisConfig.appSecret, Constants.DEFAULT_TIMEOUT);
            request.setHeaders(headers);
            request.setSignHeaderPrefixList(CUSTOM_HEADERS_TO_SIGN_PREFIX);
            //请求的query
            request.setQuerys(querys);
            request.setBytesBody(bytesBody);
            //调用服务端
            Response response = Client.execute(request);

            responseStr = getResponseResult(response);
        } catch (Exception e) {
            Log.e("ArtemisHttpUtil", "ArtemisHttpUtil.doPostBytesArtemis", e);
        }
        return responseStr;
    }

    /**
     * putString请求
     *
     * @param path        artemis配置的get请求的路径 是一个数组长度为1的Hashmap集合，只存一组数据，key为http的请求方式，value为host后面的path路径。
     * @param body        putString String请求体
     * @param accept      指定客户端能够接收的内容类型，该参数传空时的默认全部类型接受
     * @param contentType 请求的与实体对应的MIME信息，该参数传空时的取默认值("application/text; charset=UTF-8")
     * @return postString请求返回值
     */
    public static String doPutStringArtemis(Map<String, String> path, String body, String accept, String contentType) {
        /**
         * 根据传入的path获取是请求是http还是https
         */
        String httpSchema = (String) path.keySet().toArray()[0];

        if (httpSchema == null || StringUtils.isEmpty(httpSchema))
            throw new RuntimeException(MsgConstants.HTTP_SCHEMA_ERROR + "httpSchema: " + httpSchema);

        String responseStr = null;
        try {
            Map<String, String> headers = new HashMap<String, String>();
            //（必填）根据期望的Response内容类型设置
            if (StringUtils.isNotBlank(accept)) {
                headers.put(HttpHeader.HTTP_HEADER_ACCEPT, accept);
            } else {
                headers.put(HttpHeader.HTTP_HEADER_ACCEPT, "*/*");
            }
            //（可选）Body MD5,服务端会校验Body内容是否被篡改,建议Body非Form表单时添加此Header
            if (StringUtils.isNotBlank(body)) {
                headers.put(HttpHeader.HTTP_HEADER_CONTENT_MD5, MessageDigestUtil.base64AndMD5(body));
            }
            //（POST/PUT请求必选）请求Body内容格式
            if (StringUtils.isNotBlank(contentType)) {
                headers.put(HttpHeader.HTTP_HEADER_CONTENT_TYPE, contentType);
            } else {
                headers.put(HttpHeader.HTTP_HEADER_CONTENT_TYPE, ContentType.CONTENT_TYPE_TEXT);
            }
            CUSTOM_HEADERS_TO_SIGN_PREFIX.clear();
            Request request = new Request(Method.PUT_STRING, httpSchema + ArtemisConfig.host,
                    path.get(httpSchema), ArtemisConfig.appKey, ArtemisConfig.appSecret, Constants.DEFAULT_TIMEOUT);
            request.setHeaders(headers);
            request.setSignHeaderPrefixList(CUSTOM_HEADERS_TO_SIGN_PREFIX);
            request.setStringBody(body);

            //调用服务端
            Response response = Client.execute(request);

            responseStr = getResponseResult(response);

        } catch (Exception e) {
            Log.e("ArtemisHttpUtil", "ArtemisHttpUtil.doPutStringArtemis", e);
        }
        return responseStr;
    }

    /**
     * putBytes请求
     *
     * @param path        artemis配置的putBytes请求的路径
     * @param bytesBody   请求体，byte字节
     * @param accept      指定客户端能够接收的内容类型，该参数传空时的默认全部类型接受
     * @param contentType 请求的与实体对应的MIME信息，该参数传空时的取默认值("application/text; charset=UTF-8")
     * @return 返回putBytes请求的结果
     */
    public static String doPutBytesArtemis(Map<String, String> path, byte[] bytesBody, String accept, String contentType) {

        /**
         * 根据传入的path获取是请求是http还是https
         */
        String httpSchema = (String) path.keySet().toArray()[0];

        if (httpSchema == null || StringUtils.isEmpty(httpSchema))
            throw new RuntimeException(MsgConstants.HTTP_SCHEMA_ERROR + "httpSchema: " + httpSchema);

        String responseStr = null;
        try {
            Map<String, String> headers = new HashMap<String, String>();
            //（必填）根据期望的Response内容类型设置
            if (StringUtils.isNotBlank(accept)) {
                headers.put(HttpHeader.HTTP_HEADER_ACCEPT, accept);
            } else {
                headers.put(HttpHeader.HTTP_HEADER_ACCEPT, "*/*");
            }
            //（可选）Body MD5,服务端会校验Body内容是否被篡改,建议Body非Form表单时添加此Header
            if (bytesBody != null) {
                headers.put(HttpHeader.HTTP_HEADER_CONTENT_MD5, MessageDigestUtil.base64AndMD5(bytesBody));
            }
            //（POST/PUT请求必选）请求Body内容格式
            if (StringUtils.isNotBlank(contentType)) {
                headers.put(HttpHeader.HTTP_HEADER_CONTENT_TYPE, contentType);
            } else {
                headers.put(HttpHeader.HTTP_HEADER_CONTENT_TYPE, ContentType.CONTENT_TYPE_TEXT);
            }
            CUSTOM_HEADERS_TO_SIGN_PREFIX.clear();
            Request request = new Request(Method.PUT_BYTES, httpSchema + ArtemisConfig.host,
                    path.get(httpSchema), ArtemisConfig.appKey, ArtemisConfig.appSecret, Constants.DEFAULT_TIMEOUT);
            request.setHeaders(headers);
            request.setSignHeaderPrefixList(CUSTOM_HEADERS_TO_SIGN_PREFIX);
            request.setBytesBody(bytesBody);
            //调用服务端
            Response response = Client.execute(request);

            responseStr = getResponseResult(response);
        } catch (Exception e) {
            Log.e("ArtemisHttpUtil", "ArtemisHttpUtil.doPutBytesArtemis", e);
        }
        return responseStr;
    }

    /**
     * delete请求
     *
     * @param path        artemis配置的get请求的路径 是一个数组长度为1的Hashmap集合，只存一组数据，key为http的请求方式，value为host后面的path路径。
     * @param querys      map类型  delete请求的url查询参数（url中的query参数,没有就是为空）形如 "?aa=1&&bb=2"形式参数变成map键值对 query.put("aa","1");query.put("bb","2")
     * @param accept      指定客户端能够接收的内容类型，该参数传空时的默认全部类型接受
     * @param contentType 请求的与实体对应的MIME信息，该参数传空时的取默认值
     * @return 返回delete请求的结果
     */
    public static String doDeleteArtemis(Map<String, String> path, Map<String, String> querys, String accept, String contentType) {
        /**
         * 根据传入的path获取是请求是http还是https
         */
        String httpSchema = (String) path.keySet().toArray()[0];

        if (httpSchema == null || StringUtils.isEmpty(httpSchema))
            throw new RuntimeException(MsgConstants.HTTP_SCHEMA_ERROR + "httpSchema: " + httpSchema);

        String responseStr = null;
        try {
            Map<String, String> headers = new HashMap<String, String>();
            //（必填）根据期望的Response内容类型设置
            if (StringUtils.isNotBlank(accept)) {
                headers.put(HttpHeader.HTTP_HEADER_ACCEPT, accept);
            } else {
                headers.put(HttpHeader.HTTP_HEADER_ACCEPT, "*/*");
            }
            //请求的与实体对应的MIME信息
            if (StringUtils.isNotBlank(contentType)) {
                headers.put(HttpHeader.HTTP_HEADER_CONTENT_TYPE, contentType);
            }
            Request request = new Request(Method.DELETE, httpSchema + ArtemisConfig.host,
                    path.get(httpSchema), ArtemisConfig.appKey, ArtemisConfig.appSecret, Constants.DEFAULT_TIMEOUT);
            request.setHeaders(headers);
            request.setSignHeaderPrefixList(CUSTOM_HEADERS_TO_SIGN_PREFIX);
            request.setQuerys(querys);
            //调用服务端
            Response response = Client.execute(request);

            responseStr = getResponseResult(response);
        } catch (Exception e) {
            Log.e("ArtemisHttpUtil", "ArtemisHttpUtil.doDeleteArtemis", e);
        }
        return responseStr;
    }

    /**
     * response 获取body内容
     *
     * @param response
     * @return
     */
    private static String getResponseResult(Response response) {
        String responseStr = null;
        int statusCode = response.getStatusCode();
        if (String.valueOf(statusCode).startsWith(SUCC_PRE)) {//调用Artemis网关成功
            responseStr = response.getBody();
        } else {
            String msg = response.getErrorMessage();
            responseStr = response.getBody();
        }
        return responseStr;
    }
}
