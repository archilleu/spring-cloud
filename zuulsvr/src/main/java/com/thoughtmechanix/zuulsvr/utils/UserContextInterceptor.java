/**
 * 需要 使用 一个 Spring 拦截 器， 它将 被 注入 RestTemplate 类 中。
 *
 * 约翰·卡内尔. Spring微服务实战（异步图书） (Kindle 位置 3849-3850). 人民邮电出版社. Kindle 版本.
 */
package com.thoughtmechanix.zuulsvr.utils;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

public class UserContextInterceptor implements ClientHttpRequestInterceptor {

    /**
     * 　 intercept() 方法 在 RestTemplate 发生 实际 的 HTTP 服务 调用 之前 被 调用
     *
     * 约翰·卡内尔. Spring微服务实战（异步图书） (Kindle位置3856). 人民邮电出版社. Kindle 版本.
     * @param request
     * @param body
     * @param execution
     * @return
     * @throws IOException
     */
    @Override
    public ClientHttpResponse intercept(
            HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {

        HttpHeaders headers = request.getHeaders();
        headers.add(UserContext.CORRELATION_ID, UserContextHolder.getContext().getCorrelationId());
        headers.add(UserContext.AUTH_TOKEN, UserContextHolder.getContext().getAuthToken());

        return execution.execute(request, body);
    }
}