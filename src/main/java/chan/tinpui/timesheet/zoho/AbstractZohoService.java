package chan.tinpui.timesheet.zoho;

import chan.tinpui.timesheet.exception.InvalidAuthTokenZohoException;
import chan.tinpui.timesheet.exception.ZohoException;
import com.zoho.api.authenticator.OAuthToken;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

public abstract class AbstractZohoService implements ZohoService {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractZohoService.class);
    private static final int INVALID_AUTH_ERROR_CODE = 7202;
    protected final RestTemplate restTemplate;

    public AbstractZohoService() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
        SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
        SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);
        CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(csf).build();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);
        this.restTemplate = new RestTemplate(requestFactory);
    }

    protected String getRequest(String url, OAuthToken authToken) throws ZohoException {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Zoho-oauthtoken " + authToken.getAccessToken());
        HttpEntity<Void> request = new HttpEntity<>(headers);
        LOG.info("GET: " + url);
        ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
        if (responseEntity.hasBody()) {
            String response = responseEntity.getBody();
            LOG.info(response);
            return response;
        } else {
            throw new ZohoException("Zoho response did not have a body");
        }
    }

    protected JSONArray extractResultFrom(String response) throws ZohoException {
        JSONObject responseBody = new JSONObject(response).getJSONObject("response");
        if (responseBody.has("errors")) {
            JSONArray optArrayErrors = responseBody.optJSONArray("errors");
            if (optArrayErrors != null) {
                for (Object object : optArrayErrors) {
                    if (object instanceof JSONObject) {
                        JSONObject error = (JSONObject) object;
                        if (error.optInt("code") == INVALID_AUTH_ERROR_CODE) {
                            throw new InvalidAuthTokenZohoException(error.getString("message"));
                        }
                    }
                }
            } else {
                JSONObject optObjectErrors = responseBody.optJSONObject("errors");
                if (optObjectErrors != null) {
                    if (optObjectErrors.optInt("code") == INVALID_AUTH_ERROR_CODE) {
                        throw new InvalidAuthTokenZohoException(optObjectErrors.getString("message"));
                    }
                    throw new ZohoException(optObjectErrors.getString("message"));
                }
            }
            throw new ZohoException(responseBody.getString("message"));
        } else {
            return responseBody.getJSONArray("result");
        }
    }
}
