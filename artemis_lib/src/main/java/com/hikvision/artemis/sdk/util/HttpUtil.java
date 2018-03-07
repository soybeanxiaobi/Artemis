/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.hikvision.artemis.sdk.util;

import android.util.Log;

import com.hikvision.artemis.sdk.Response;
import com.hikvision.artemis.sdk.constant.Constants;
import com.hikvision.artemis.sdk.constant.ContentType;
import com.hikvision.artemis.sdk.constant.HttpHeader;
import com.hikvision.artemis.sdk.constant.HttpMethod;
import com.hikvision.artemis.sdk.constant.SystemHeader;

import c_ga_org.apache.commons.lang3.StringUtils;
import c_ga_org.apache.http.NameValuePair;
import c_ga_org.apache.http.message.BasicNameValuePair;
import c_ga_org.apache.http.params.CoreConnectionPNames;
import c_ga_org.apache.http.client.entity.UrlEncodedFormEntity;


import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import c_ga_okhttp3.Headers;
import c_ga_okhttp3.MediaType;
import c_ga_okhttp3.OkHttpClient;
import c_ga_okhttp3.Request;
import c_ga_okhttp3.RequestBody;


public class HttpUtil {
    public static final MediaType JSON = MediaType.parse("application/json;charset=UTF-8");

    public static OkHttpClient build() {
        return new OkHttpClient.Builder().sslSocketFactory(createSSLSocketFactory()).hostnameVerifier(new TrustAllHostnameVerifier()).build();
    }

    private static SSLSocketFactory createSSLSocketFactory() {
        SSLSocketFactory socketFactory = null;
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{new TrustAllManager()}, new SecureRandom());
            socketFactory = sc.getSocketFactory();
        } catch (Exception e) {
            Log.w("Exception:{}", e);
        }
        return socketFactory;
    }

    /**
     * HTTP GET
     *
     * @param host
     * @param path
     * @param connectTimeout
     * @param headers
     * @param querys
     * @param signHeaderPrefixList
     * @param appKey
     * @param appSecret
     * @return
     * @throws Exception
     */
    public static Response httpGet(String host, String path, int connectTimeout, Map<String, String> headers, Map<String, String> querys, List<String> signHeaderPrefixList, String appKey, String appSecret)
            throws Exception {
        headers = initialBasicHeader(HttpMethod.GET, path, headers, querys, null, signHeaderPrefixList, appKey, appSecret);
        OkHttpClient httpClient = wrapClient(host);
        Request.Builder builder = new Request.Builder().url(initUrl(host, path, querys));
        Request request = buildRequest(connectTimeout, headers, builder);
        c_ga_okhttp3.Response execute = httpClient.newCall(request).execute();
        return convert(execute);
    }

    /**
     * 构建Request请求
     *
     * @return
     */
    private static Request buildRequest(int connectTimeout, Map<String, String> headers, Request.Builder builder) {
        builder.addHeader(CoreConnectionPNames.CONNECTION_TIMEOUT, String.valueOf(getTimeout(connectTimeout)));
        for (Map.Entry<String, String> e : headers.entrySet()) {
            builder.addHeader(e.getKey(), e.getValue().trim());
        }
        return builder.build();
    }


    /**
     * HTTP POST
     *
     * @param host
     * @param path
     * @param connectTimeout
     * @param headers
     * @param querys
     * @param bodys
     * @param signHeaderPrefixList
     * @param appKey
     * @param appSecret
     * @return
     * @throws Exception
     */
    public static Response httpPost(String host, String path, int connectTimeout, Map<String, String> headers, Map<String, String> querys, Map<String, String> bodys, List<String> signHeaderPrefixList, String appKey, String appSecret)
            throws Exception {
        if (headers == null) {
            headers = new HashMap<String, String>();
        }
        headers.put(HttpHeader.HTTP_HEADER_CONTENT_TYPE, ContentType.CONTENT_TYPE_FORM);
        headers = initialBasicHeader(HttpMethod.POST, path, headers, querys, bodys, signHeaderPrefixList, appKey, appSecret);
        OkHttpClient httpClient = wrapClient(host);
        Request.Builder builder = new Request.Builder().build().newBuilder().url(initUrl(host, path, querys));
        buildRequest(connectTimeout, headers, builder);

        //附加参数
        UrlEncodedFormEntity formEntity = buildFormEntity(bodys);
        if (formEntity != null) {
            RequestBody requestBody = getRequestBody(formEntity);
            Request request = builder.post(requestBody).build();
            return convert(httpClient.newCall(request).execute());
        }
        return convert(null);
    }

    private static RequestBody getRequestBody(UrlEncodedFormEntity formEntity) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        BufferedInputStream bufferedInputStream = new BufferedInputStream(formEntity.getContent());
        byte[] buffer = new byte[2048];
        int length = 0;
        while ((length = bufferedInputStream.read(buffer)) != -1) {
            bos.write(buffer, 0, length);//写入输出流
        }
        bufferedInputStream.close();//读取完毕，关闭输入流
        return RequestBody.create(JSON, new String(bos.toByteArray()));
    }


    /**
     * Http POST
     *
     * @param host
     * @param path
     * @param connectTimeout
     * @param headers
     * @param querys
     * @param body
     * @param signHeaderPrefixList
     * @param appKey
     * @param appSecret
     * @return
     * @throws Exception
     */
//    public static Response httpPost(String host, String path, int connectTimeout, Map<String, String> headers, Map<String, String> querys, String body, List<String> signHeaderPrefixList, String appKey, String appSecret)
//            throws Exception {
//
//        String contentType = headers.get(HttpHeader.HTTP_HEADER_CONTENT_TYPE);
//        if (ContentType.CONTENT_TYPE_FORM.equals(contentType)) {
//            Map<String, String> paramMap = strToMap(body);
//            String modelDatas = paramMap.get("modelDatas");
//            if (StringUtils.isNotBlank(modelDatas)) {
//                paramMap.put("modelDatas", URLDecoder.decode(modelDatas));
//            }
//            headers = initialBasicHeader(HttpMethod.POST, path, headers, querys, paramMap, signHeaderPrefixList, appKey, appSecret);
//        } else {
//            headers = initialBasicHeader(HttpMethod.POST, path, headers, querys, null, signHeaderPrefixList, appKey, appSecret);
//        }
//
//        HttpClient httpClient = wrapClient(host);
//        httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, getTimeout(connectTimeout));
//
//        HttpPost post = new HttpPost(initUrl(host, path, querys));
//        for (Map.Entry<String, String> e : headers.entrySet()) {
//            post.addHeader(e.getKey(), MessageDigestUtil.utf8ToIso88591(e.getValue()));
//        }
//
//        if (StringUtils.isNotBlank(body)) {
//            post.setEntity(new StringEntity(body, Constants.ENCODING));
//        }
//
//        return convert(httpClient.execute(post));
//    }

    /**
     * HTTP POST
     *
     * @param host
     * @param path
     * @param connectTimeout
     * @param headers
     * @param querys
     * @param bodys
     * @param signHeaderPrefixList
     * @param appKey
     * @param appSecret
     * @return
     * @throws Exception
     */
    public static Response httpPost(String host, String path, int connectTimeout, Map<String, String> headers, Map<String, String> querys, byte[] bodys, List<String> signHeaderPrefixList, String appKey, String appSecret)
            throws Exception {
        headers = initialBasicHeader(HttpMethod.POST, path, headers, querys, null, signHeaderPrefixList, appKey, appSecret);
        OkHttpClient httpClient = wrapClient(host);
        Request.Builder builder = new Request.Builder().build().newBuilder().url(initUrl(host, path, querys));
        buildRequest(connectTimeout, headers, builder);

        if (bodys != null) {
            Request request = builder.build();
            RequestBody requestBody = RequestBody.create(JSON, bodys);
            builder.post(requestBody).build();
            return convert(httpClient.newCall(request).execute());
        }

        return convert(null);
    }

    /**
     * HTTP PUT
     *
     * @param host
     * @param path
     * @param connectTimeout
     * @param headers
     * @param querys
     * @param body
     * @param signHeaderPrefixList
     * @param appKey
     * @param appSecret
     * @return
     * @throws Exception
     */
    public static Response httpPut(String host, String path, int connectTimeout, Map<String, String> headers, Map<String, String> querys, String body, List<String> signHeaderPrefixList, String appKey, String appSecret)
            throws Exception {
        headers = initialBasicHeader(HttpMethod.PUT, path, headers, querys, null, signHeaderPrefixList, appKey, appSecret);
        OkHttpClient httpClient = wrapClient(host);
        Request.Builder builder = new Request.Builder().build().newBuilder().url(initUrl(host, path, querys));
        buildRequest(connectTimeout, headers, builder);
        if (body != null) {
            Request request = builder.build();
            RequestBody requestBody = RequestBody.create(JSON, body);
            builder.put(requestBody).build();
            return convert(httpClient.newCall(request).execute());
        }
        return convert(null);
    }

    /**
     * HTTP PUT
     *
     * @param host
     * @param path
     * @param connectTimeout
     * @param headers
     * @param querys
     * @param bodys
     * @param signHeaderPrefixList
     * @param appKey
     * @param appSecret
     * @return
     * @throws Exception
     */
    public static Response httpPut(String host, String path, int connectTimeout, Map<String, String> headers, Map<String, String> querys, byte[] bodys, List<String> signHeaderPrefixList, String appKey, String appSecret)
            throws Exception {

        headers = initialBasicHeader(HttpMethod.POST, path, headers, querys, null, signHeaderPrefixList, appKey, appSecret);
        OkHttpClient httpClient = wrapClient(host);
        Request.Builder builder = new Request.Builder().build().newBuilder().url(initUrl(host, path, querys));
        buildRequest(connectTimeout, headers, builder);

        if (bodys != null) {
            Request request = builder.build();
            RequestBody requestBody = RequestBody.create(JSON, bodys);
            builder.put(requestBody).build();
            return convert(httpClient.newCall(request).execute());
        }

        return convert(null);
    }

    /**
     * HTTP DELETE
     *
     * @param host
     * @param path
     * @param connectTimeout
     * @param headers
     * @param querys
     * @param signHeaderPrefixList
     * @param appKey
     * @param appSecret
     * @return
     * @throws Exception
     */
    public static Response httpDelete(String host, String path, int connectTimeout, Map<String, String> headers, Map<String, String> querys, List<String> signHeaderPrefixList, String appKey, String appSecret)
            throws Exception {
        headers = initialBasicHeader(HttpMethod.POST, path, headers, querys, null, signHeaderPrefixList, appKey, appSecret);
        OkHttpClient httpClient = wrapClient(host);
        Request.Builder builder = new Request.Builder().build().newBuilder().url(initUrl(host, path, querys));
        buildRequest(connectTimeout, headers, builder);
        Request request = builder.build();
        builder.delete().build();
        return convert(httpClient.newCall(request).execute());
    }

    /**
     * @param formParam
     * @return
     * @throws UnsupportedEncodingException
     */
    private static UrlEncodedFormEntity buildFormEntity(Map<String, String> formParam) throws UnsupportedEncodingException {
        if (formParam != null) {
            List<NameValuePair> nameValuePairList = new ArrayList<NameValuePair>();
            for (String key : formParam.keySet()) {
                nameValuePairList.add(new BasicNameValuePair(key, formParam.get(key)));
            }
            UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(nameValuePairList, Constants.ENCODING);
            formEntity.setContentType(ContentType.CONTENT_TYPE_FORM);
            return formEntity;
        }

        return null;
    }

    private static String initUrl(String host, String path, Map<String, String> querys) throws UnsupportedEncodingException {
        StringBuilder sbUrl = new StringBuilder();
        sbUrl.append(host);
        if (!StringUtils.isBlank(path)) {
            sbUrl.append(path);
        }
        if (null != querys) {
            StringBuilder sbQuery = new StringBuilder();
            for (Map.Entry<String, String> query : querys.entrySet()) {
                if (0 < sbQuery.length()) {
                    sbQuery.append(Constants.SPE3);
                }
                if (StringUtils.isBlank(query.getKey()) && !StringUtils.isBlank(query.getValue())) {
                    sbQuery.append(query.getValue());
                }
                if (!StringUtils.isBlank(query.getKey())) {
                    sbQuery.append(query.getKey());
                    if (!StringUtils.isBlank(query.getValue())) {
                        sbQuery.append(Constants.SPE4);
                        sbQuery.append(URLEncoder.encode(query.getValue(), Constants.ENCODING));
                    }
                }
            }
            if (0 < sbQuery.length()) {
                sbUrl.append(Constants.SPE5).append(sbQuery);
            }
        }

        return sbUrl.toString();
    }


    /**
     * @param method
     * @param path
     * @param headers
     * @param querys
     * @param bodys
     * @param signHeaderPrefixList
     * @param appKey
     * @param appSecret
     * @return
     * @throws MalformedURLException
     */
    private static Map<String, String> initialBasicHeader(String method, String path,
                                                          Map<String, String> headers,
                                                          Map<String, String> querys,
                                                          Map<String, String> bodys,
                                                          List<String> signHeaderPrefixList,
                                                          String appKey, String appSecret)
            throws MalformedURLException {
        if (headers == null) {
            headers = new HashMap<String, String>();
        }

        headers.put(SystemHeader.X_CA_TIMESTAMP, String.valueOf(new Date().getTime()));
        headers.put(SystemHeader.X_CA_NONCE, UUID.randomUUID().toString());
        headers.put(SystemHeader.X_CA_KEY, appKey);
        headers.put(SystemHeader.X_CA_SIGNATURE,
                SignUtil.sign(appSecret, method, path, headers, querys, bodys, signHeaderPrefixList));

        return headers;
    }

    /**
     * @param timeout
     * @return
     */
    private static int getTimeout(int timeout) {
        if (timeout == 0) {
            return Constants.DEFAULT_TIMEOUT;
        }
        return timeout;
    }

//    private static Response convert(HttpResponse response) throws IOException {
//        Response res = new Response();
//
//        if (null != response) {
//            res.setStatusCode(response.getStatusLine().getStatusCode());
//            for (Header header : response.getAllHeaders()) {
//                res.setHeader(header.getName(), MessageDigestUtil.iso88591ToUtf8(header.getValue()));
//            }
//
//            res.setContentType(res.getHeader("Content-Type"));
//            res.setRequestId(res.getHeader("X-Ca-Request-Id"));
//            res.setErrorMessage(res.getHeader("X-Ca-Error-Message"));
//            if (response.getEntity() == null) {
//                res.setBody(null);
//            } else {
//                res.setBody(readStreamAsStr(response.getEntity().getContent()));
//            }
//        } else {
//            res.setStatusCode(500);
//            res.setErrorMessage("No Response");
//        }
//
//        return res;
//    }


    private static Response convert(c_ga_okhttp3.Response response) throws IOException {
        Response res = new Response();

        if (null != response) {
            res.setStatusCode(response.code());
            Headers headers = response.headers();
            for (int i = 0; i < headers.size(); i++) {
                res.setHeader(headers.name(i), MessageDigestUtil.iso88591ToUtf8(headers.get(headers.name(i))));
            }

            res.setContentType(res.getHeader("Content-Type"));
            res.setRequestId(res.getHeader("X-Ca-Request-Id"));
            res.setErrorMessage(res.getHeader("X-Ca-Error-Message"));
            if (response.body() == null) {
                res.setBody(null);
            } else {
                res.setBody(readStreamAsStr(response.body().byteStream()));
            }
        } else {
            res.setStatusCode(500);
            res.setErrorMessage("No Response");
        }

        return res;
    }

    /**
     * @param is
     * @return
     * @throws IOException
     */
    public static String readStreamAsStr(InputStream is) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        WritableByteChannel dest = Channels.newChannel(bos);
        ReadableByteChannel src = Channels.newChannel(is);
        ByteBuffer bb = ByteBuffer.allocate(4096);

        while (src.read(bb) != -1) {
            bb.flip();
            dest.write(bb);
            bb.clear();
        }
        src.close();
        dest.close();

        return new String(bos.toByteArray(), Constants.ENCODING);
    }

    private static OkHttpClient wrapClient(String host) {
        if (host.startsWith("https://")) {
            return build();
        }
        return new OkHttpClient();

    }

    private static Map<String, String> strToMap(String str) {
        Map<String, String> map = new HashMap<String, String>();
        try {
            String[] params = str.split("&");
            for (String param : params) {
                String[] a = param.split("=");
                map.put(a[0], a[1]);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return map;
    }
}