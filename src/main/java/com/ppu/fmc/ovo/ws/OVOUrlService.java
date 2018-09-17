package com.ppu.fmc.ovo.ws;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ppu.fmc.ovo.model.RequestClient;
import com.ppu.fmc.ovo.model.ResponseMsg;

@Service
public class OVOUrlService {
    private static final Logger logger = LoggerFactory.getLogger(OVOUrlService.class);

	@Value("${ovo.ws.auth.basic}")
	private String basicAuth;

	@Value("${ovo.ws.url}")
	private String url;

//	@Value("${ovo.ws.isssl:false}")
	private boolean isSSL = false;
	
//	@Autowired
//	private RestTemplateBuilder restTemplateBuilder;
	
	private RestTemplate getRestTemplate() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
		/*if (isSSL) {
			 TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;

			    SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom()
			                    .loadTrustMaterial(null, acceptingTrustStrategy)
			                    .build();

			    SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);

			    CloseableHttpClient httpClient = HttpClients.custom()
			                    .setSSLSocketFactory(csf)
			                    .build();

			    HttpComponentsClientHttpRequestFactory requestFactory =
			                    new HttpComponentsClientHttpRequestFactory();

			    requestFactory.setHttpClient(httpClient);
			    RestTemplate restTemplate = new RestTemplate(requestFactory);
			    return restTemplate;
		} else {*/
			return new RestTemplate();
//		}
	}
	
    @Async
    public CompletableFuture<ResponseMsg> sendDataOld(RequestClient req) throws InterruptedException, JsonProcessingException, RestClientException, KeyManagementException, NoSuchAlgorithmException, KeyStoreException {

		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("Authorization", basicAuth);
//		headers.add("Accept", MediaType.APPLICATION_JSON.toString());
//		headers.add("Authorization", "Basic Y2lzY286cGFzc3dvcmQxMjM=");
		
		String jsonInString = new ObjectMapper().writeValueAsString(req);
		logger.info("Sending " + jsonInString);
		
		HttpEntity<String> request = new HttpEntity<String>(jsonInString, headers);
		
		RestTemplate rt = getRestTemplate();
		
//		ResponseMsg postForObject = getRestTemplate().postForObject(url, request, ResponseMsg.class);	// not working iwth ssl
//		return CompletableFuture.completedFuture(postForObject);
		
//		String url = "http://10.10.10.71:8000/apis/cisco/url";
		ResponseEntity<ResponseMsg> exchange = rt.exchange(url, HttpMethod.POST, request, ResponseMsg.class);

//		System.out.println(exchange.getBody().toString());
        
        
        return CompletableFuture.completedFuture(exchange.getBody());
    }
    
    @Async
    public CompletableFuture<ResponseMsg> sendData(RequestClient req) throws InterruptedException, RestClientException, KeyManagementException, NoSuchAlgorithmException, KeyStoreException, IOException {

		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("Authorization", basicAuth);
//		headers.add("Accept", MediaType.APPLICATION_JSON.toString());
//		headers.add("Authorization", "Basic Y2lzY286cGFzc3dvcmQxMjM=");
		
		String jsonInString = new ObjectMapper().writeValueAsString(req);
		logger.info("Sending " + jsonInString);
		
		HttpEntity<String> request = new HttpEntity<String>(jsonInString, headers);
		
		RestTemplate rt = getRestTemplate();
		
//		String url = "http://10.10.10.71:8000/apis/cisco/url";
		ResponseEntity<String> exchange = rt.exchange(url, HttpMethod.POST, request, String.class);

		/* harus direpair karena balikannya seperti ini (bisa dicek via postman)
		"{\"message\":\"OK\",\"status\":200}"
		*/
				
		String json = exchange.getBody().toString();
		
		String rst = json.replace("\\","");
		if (rst.startsWith("\"")) {
			rst = rst.replaceFirst("\"", "").substring(0,  rst.length() -2);
		}
		
		ObjectMapper om = new ObjectMapper();
//		System.err.println(om.readValue(rst, ResponseMsg.class).toString());

		ResponseMsg rm = om.readValue(rst, ResponseMsg.class);
        
        return CompletableFuture.completedFuture(rm);
    }
    
    @Async
    public CompletableFuture<ResponseMsg> sendData3(RequestClient req) throws InterruptedException, JsonProcessingException, RestClientException, KeyManagementException, NoSuchAlgorithmException, KeyStoreException {

		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("Authorization", basicAuth);
//		headers.add("Accept", MediaType.APPLICATION_JSON.toString());
//		headers.add("Authorization", "Basic Y2lzY286cGFzc3dvcmQxMjM=");
		
		String jsonInString = new ObjectMapper().writeValueAsString(req);
		logger.info("Sending " + jsonInString);
		
		HttpEntity<String> request = new HttpEntity<String>(jsonInString, headers);
		
		RestTemplate rt = getRestTemplate();
	    List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
	    MappingJackson2HttpMessageConverter jsonMessageConverter = new MappingJackson2HttpMessageConverter();
ObjectMapper objectMapper = new ObjectMapper();
objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		//	    
//	    List<MediaType> mediaTypes = new ArrayList<MediaType>();
//	    mediaTypes.add(MediaType.APPLICATION_JSON);
//	    jsonMessageConverter.setSupportedMediaTypes(mediaTypes);
	    jsonMessageConverter.setObjectMapper(objectMapper );
//	    
	    messageConverters.add(jsonMessageConverter);
//	    messageConverters.add(jsonMessageConverter);
		rt.setMessageConverters(messageConverters);
		
//		ResponseMsg postForObject = getRestTemplate().postForObject(url, request, ResponseMsg.class);	// not working iwth ssl
//		return CompletableFuture.completedFuture(postForObject);
		
//		String url = "http://10.10.10.71:8000/apis/cisco/url";
		ResponseEntity<ResponseMsg> exchange = rt.exchange(url, HttpMethod.POST, request, ResponseMsg.class);

//		System.out.println(exchange.getBody().toString());
        
        
        return CompletableFuture.completedFuture(exchange.getBody());
    }

}
