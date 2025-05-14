package city.makeour.moc.ngsiv2;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import city.makeour.ngsi.v2.api.EntitiesApi;
import city.makeour.ngsi.v2.invoker.ApiClient;

public class Ngsiv2Client {
    protected ApiClient apiClient;

    protected EntitiesApi entitiesApi;

    public Ngsiv2Client(ApiClient apiClient) {
        this.apiClient = apiClient;

        this.entitiesApi = new EntitiesApi(this.apiClient);
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public EntitiesApi getEntitiesApi() {
        return entitiesApi;
    }

    public RestClient.ResponseSpec createEntity(String contentType, Object body) {
        if (contentType == null) {
            throw new RestClientResponseException(
                    "Missing the required parameter 'contentType' when calling createEntity",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), (HttpHeaders) null,
                    (byte[]) null, (Charset) null);
        } else if (body == null) {
            throw new RestClientResponseException("Missing the required parameter 'body' when calling createEntity",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), (HttpHeaders) null,
                    (byte[]) null, (Charset) null);
        } else {
            Map<String, Object> pathParams = new HashMap();
            MultiValueMap<String, String> queryParams = new LinkedMultiValueMap();
            HttpHeaders headerParams = new HttpHeaders();
            MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap();
            MultiValueMap<String, Object> formParams = new LinkedMultiValueMap();
            queryParams.putAll(
                    this.apiClient.parameterToMultiValueMap((ApiClient.CollectionFormat) null, "options", "keyValues"));
            if (contentType != null) {
                headerParams.add("Content-Type", this.apiClient.parameterToString(contentType));
            }

            String[] localVarAccepts = new String[0];
            List<MediaType> localVarAccept = this.apiClient.selectHeaderAccept(localVarAccepts);
            String[] localVarContentTypes = new String[] { "application/json" };
            MediaType localVarContentType = this.apiClient.selectHeaderContentType(localVarContentTypes);
            String[] localVarAuthNames = new String[0];
            ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<>() {
            };
            return this.apiClient.invokeAPI("/v2/entities", HttpMethod.POST, pathParams, queryParams, body,
                    headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                    localVarReturnType);
        }
    }
}
