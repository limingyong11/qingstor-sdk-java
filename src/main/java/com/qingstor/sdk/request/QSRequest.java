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

package com.qingstor.sdk.request;

import com.qingstor.sdk.config.EvnContext;
import com.qingstor.sdk.constants.QSConstant;
import com.qingstor.sdk.exception.QSException;
import com.qingstor.sdk.model.OutputModel;
import com.qingstor.sdk.model.RequestInputModel;
import com.qingstor.sdk.utils.Base64;
import com.qingstor.sdk.utils.QSLoggerUtil;
import com.qingstor.sdk.utils.QSParamInvokeUtil;
import com.qingstor.sdk.utils.QSSignatureUtil;
import com.qingstor.sdk.utils.QSStringUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import okhttp3.Request;

@SuppressWarnings({"rawtypes", "unchecked"})
public class QSRequest implements ResourceRequest {

    private static Logger logger = QSLoggerUtil.setLoggerHanlder(QSRequest.class.getName());

    private static final String REQUEST_PREFIX = "/";

    @Override
    public void sendApiRequestAsync(
            Map context, RequestInputModel paramBean, ResponseCallBack callback)
            throws QSException {
        String validate = paramBean != null ? paramBean.validateParam() : "";
        EvnContext evnContext = (EvnContext) context.get(QSConstant.EVN_CONTEXT_KEY);
        context.put(QSConstant.PARAM_KEY_USER_AGENT, evnContext.getAdditionalUserAgent());
        String evnValidate = evnContext.validateParam();
        if (!QSStringUtil.isEmpty(validate) || !QSStringUtil.isEmpty(evnValidate)) {
            if (QSStringUtil.isEmpty(validate)) {
                validate = evnValidate;
            }
            OutputModel out = QSParamInvokeUtil.getOutputModel(callback);
            QSOkHttpRequestClient.fillResponseCallbackModel(
                    QSConstant.REQUEST_ERROR_CODE, validate, out);
            callback.onAPIResponse(out);
        } else {
            Request request = buildRequest(context, paramBean);
            QSOkHttpRequestClient.getInstance()
                    .requestActionAsync(request, evnContext.isSafeOkHttp(), callback);
        }
    }

    @Override
    public void sendApiRequestAsync(String requestUrl, Map context, ResponseCallBack callback)
            throws QSException {
        EvnContext evnContext = (EvnContext) context.get(QSConstant.EVN_CONTEXT_KEY);
        Request request = QSOkHttpRequestClient.getInstance().buildUrlRequest(requestUrl);
        QSOkHttpRequestClient.getInstance()
                .requestActionAsync(request, evnContext.isSafeOkHttp(), callback);
    }

    @Override
    public OutputModel sendApiRequest(
            String requestUrl, Map context, Class<? extends OutputModel> outputClass)
            throws QSException {
        EvnContext evnContext = (EvnContext) context.get(QSConstant.EVN_CONTEXT_KEY);
        Request request = QSOkHttpRequestClient.getInstance().buildUrlRequest(requestUrl);
        return QSOkHttpRequestClient.getInstance()
                .requestAction(request, evnContext.isSafeOkHttp(), outputClass);
    }

    @Override
    public OutputModel sendApiRequest(Map context, RequestInputModel paramBean, Class outputClass)
            throws QSException {
        String validate = paramBean != null ? paramBean.validateParam() : "";
        EvnContext evnContext = (EvnContext) context.get(QSConstant.EVN_CONTEXT_KEY);
        context.put(QSConstant.PARAM_KEY_USER_AGENT, evnContext.getAdditionalUserAgent());
        String evnValidate = evnContext.validateParam();
        if (!QSStringUtil.isEmpty(validate) || !QSStringUtil.isEmpty(evnValidate)) {
            if (QSStringUtil.isEmpty(validate)) {
                validate = evnValidate;
            }
            try {
                OutputModel model = (OutputModel) outputClass.newInstance();
                QSOkHttpRequestClient.fillResponseCallbackModel(
                        QSConstant.REQUEST_ERROR_CODE, validate, model);
                return model;
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage());
                throw new QSException(e.getMessage());
            }
        } else {
            Request request = buildRequest(context, paramBean);
            return QSOkHttpRequestClient.getInstance()
                    .requestAction(request, evnContext.isSafeOkHttp(), outputClass);
        }
    }

    private Request buildRequest(Map context, RequestInputModel params) throws QSException {

        EvnContext evnContext = (EvnContext) context.get(QSConstant.EVN_CONTEXT_KEY);
        String zone = (String) context.get(QSConstant.PARAM_KEY_REQUEST_ZONE);

        Map paramsQuery = QSParamInvokeUtil.getRequestParams(params, QSConstant.PARAM_TYPE_QUERY);
        Map paramsBody = QSParamInvokeUtil.getRequestParams(params, QSConstant.PARAM_TYPE_BODY);
        Map paramsHeaders =
                QSParamInvokeUtil.getRequestParams(params, QSConstant.PARAM_TYPE_HEADER);
        if(context.get(QSConstant.PARAM_KEY_USER_AGENT) != null){
        	paramsHeaders.put(QSConstant.PARAM_KEY_USER_AGENT, context.get(QSConstant.PARAM_KEY_USER_AGENT));
        }
        
        String requestApi = (String) context.get(QSConstant.PARAM_KEY_REQUEST_APINAME);

        this.initHeadContentMd5(requestApi, paramsBody, paramsHeaders);

        String method = (String) context.get(QSConstant.PARAM_KEY_REQUEST_METHOD);
        String bucketName = (String) context.get(QSConstant.PARAM_KEY_BUCKET_NAME);
        String requestPath = (String) context.get(QSConstant.PARAM_KEY_REQUEST_PATH);

        String objectName = (String) context.get(QSConstant.PARAM_KEY_OBJECT_NAME);
        if (context.containsKey(QSConstant.PARAM_KEY_OBJECT_NAME)) {
            requestPath = requestPath.replace(QSConstant.BUCKET_NAME_REPLACE, bucketName);
            requestPath = requestPath.replace(QSConstant.OBJECT_NAME_REPLACE, QSStringUtil.urlCharactersEncoding(objectName));
        } else {
            requestPath = requestPath.replace(QSConstant.BUCKET_NAME_REPLACE, bucketName + "/");
        }

        String authSign =
                QSSignatureUtil.getAuth(
                        evnContext.getAccessKey(),
                        evnContext.getAccessSecret(),
                        method,
                        requestPath,
                        paramsQuery,
                        paramsHeaders);
        String requestSuffixPath = getRequestSuffixPath((String) context.get(QSConstant.PARAM_KEY_REQUEST_PATH), bucketName, objectName);
        paramsHeaders.put("Authorization", authSign);
        logger.log(Level.INFO, authSign);
        String singedUrl =
                getSignedUrl(
                        evnContext.getRequestUrl(),
                        zone,
                        bucketName,
                        paramsQuery,
                        requestSuffixPath);
        logger.log(Level.INFO, singedUrl);
        if (QSConstant.PARAM_KEY_REQUEST_API_MULTIPART.equals(requestApi)) {
            Request request =
                    QSOkHttpRequestClient.getInstance()
                            .buildStorMultiUpload(
                                    method, paramsBody, singedUrl, paramsHeaders, paramsQuery);
            return request;

        } else {
            Request request =
                    QSOkHttpRequestClient.getInstance()
                            .buildStorRequest(method, paramsBody, singedUrl, paramsHeaders);
            return request;
        }
    }

    private String getRequestSuffixPath(String requestPath, String bucketName, String objectName) throws QSException {
        if (QSStringUtil.isEmpty(bucketName)) {
            return REQUEST_PREFIX;
        }
        String suffixPath = requestPath.replace(REQUEST_PREFIX + QSConstant.BUCKET_NAME_REPLACE, "").replace(REQUEST_PREFIX + QSConstant.OBJECT_NAME_REPLACE, "");
        if (QSStringUtil.isEmpty(objectName)) {
            objectName = "";
        } else {
        	objectName = QSStringUtil.urlCharactersEncoding(objectName);
        }

        return String.format("%s%s%s", REQUEST_PREFIX, objectName, suffixPath);
    }

    

    private static String getSignedUrl(
            String serviceUrl,
            String zone,
            String bucketName,
            Map paramsQuery,
            String requestSuffixPath)
            throws QSException {
        if ("".equals(bucketName) || bucketName == null) {
            return QSSignatureUtil.generateQSURL(paramsQuery, serviceUrl + requestSuffixPath);
        } else {
            String storRequestUrl = serviceUrl.replace("://", "://%s." + zone + ".");
            return QSSignatureUtil.generateQSURL(
                    paramsQuery, String.format(storRequestUrl, bucketName) + requestSuffixPath);
        }
    }

    private void initHeadContentMd5(String requestApi, Map paramsBody, Map paramsHead) throws QSException {
        if (QSConstant.PARAM_KEY_REQUEST_API_DELETE_MULTIPART.equals(requestApi)) {
            if (paramsBody.size() > 0) {
                Object bodyContent = QSOkHttpRequestClient.getInstance().getBodyContent(paramsBody);
                MessageDigest instance = null;
                try {
                    instance = MessageDigest.getInstance("MD5");
                } catch (NoSuchAlgorithmException e) {
                    throw new QSException("MessageDigest MD5 error", e);
                }
                String contentMD5 = new String(Base64.encode(instance.digest(bodyContent.toString().getBytes())));
                paramsHead.put(QSConstant.PARAM_KEY_CONTENT_MD5, contentMD5);
            }
        }
    }
}
