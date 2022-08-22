package com.wyc.util;

import com.wyc.model.SeqNextRequest;
import com.wyc.model.SeqNextResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

public class UtilRestTemplate {
    public static final RestTemplate restTemplate = new RestTemplate();

    static {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setReadTimeout(3000);
        factory.setConnectTimeout(3000);
    }

    public static <T> T get(String url, Class<T> responseType) {
        return get(url, new HashMap<>(), new HashMap<>(), responseType);
    }

    public static <T> T get(String url, Map<String, Object> urlParam, Class<T> responseType) {
        return get(url, urlParam, new HashMap<>(), responseType);
    }

    public static <T> T get(String url, Map<String, Object> urlParam, Map<String, String> headerParam, Class<T> responseType) {
        HttpHeaders headers = new HttpHeaders();
        for (Map.Entry<String, String> entry : headerParam.entrySet()) {
            headers.set(entry.getKey(), entry.getValue());
        }
        ResponseEntity<T> exchange = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), responseType, urlParam);
        return exchange.getBody();
    }


    public static <T> T post(String url, Map<String, Object> urlParam, Class<T> responseType) {
        return post(url, urlParam, new HashMap<>(), null, responseType);
    }

    public static <T> T post(String url, Map<String, Object> urlParam, Map<String, String> headerParam, Class<T> responseType) {
        return post(url, urlParam, headerParam, null, responseType);
    }

    public static <T> T post(String url, Map<String, Object> urlParam, Map<String, String> headerParam, Object body, Class<T> responseType) {
        HttpHeaders headers = new HttpHeaders();
        for (Map.Entry<String, String> entry : headerParam.entrySet()) {
            headers.set(entry.getKey(), entry.getValue());
        }
        HttpEntity<Object> entity = null;
        if (body == null) {
            entity = new HttpEntity<>(headers);
        } else {
            entity = new HttpEntity<>(body, headers);
        }
        ResponseEntity<T> exchange = restTemplate.exchange(url, HttpMethod.POST, entity, responseType, urlParam);
        return exchange.getBody();
    }

    public static void main(String[] args) {
        //http://localhost:8080/seq/next?retryCount=1
        SeqNextRequest nextRequest = new SeqNextRequest();
        nextRequest.setName("seq");
        nextRequest.setCount(1);

        SeqNextResponse post = UtilRestTemplate.post("http://localhost:8080/seq/next", new HashMap<>(), new HashMap<>(), nextRequest, SeqNextResponse.class);
        System.out.println(post);
    }

}
