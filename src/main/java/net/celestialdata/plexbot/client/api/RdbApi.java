/*
 * Plexbot
 * Provides all the API functions necessary for the Plexbot to function.
 *
 * OpenAPI spec version: 1.0.0
 * Contact: brandan.schmitz@celestialdata.net
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */

package net.celestialdata.plexbot.client.api;

import com.google.gson.reflect.TypeToken;
import net.celestialdata.plexbot.client.*;
import net.celestialdata.plexbot.client.model.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class RdbApi {
    private ApiClient apiClient;

    public RdbApi() {
        this(Configuration.getDefaultApiClient());
    }

    public RdbApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Build call for addMagnet
     *
     * @param magnet                  (optional)
     * @param progressListener        Progress listener
     * @param progressRequestListener Progress request listener
     * @return Call to execute
     * @throws ApiException If fail to serialize the request body object
     */
    @SuppressWarnings("DuplicatedCode")
    public com.squareup.okhttp.Call addMagnetCall(String magnet, final ProgressResponseBody.ProgressListener progressListener, final ProgressRequestBody.ProgressRequestListener progressRequestListener) throws ApiException {
        Object localVarPostBody = null;

        // create path and map variables
        String localVarPath = "/torrents/addMagnet";

        List<Pair> localVarQueryParams = new ArrayList<>();
        List<Pair> localVarCollectionQueryParams = new ArrayList<>();

        Map<String, String> localVarHeaderParams = new HashMap<>();

        Map<String, Object> localVarFormParams = new HashMap<>();
        if (magnet != null)
            localVarFormParams.put("magnet", magnet);

        final String[] localVarAccepts = {
                "application/json"
        };
        final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        if (localVarAccept != null) localVarHeaderParams.put("Accept", localVarAccept);

        final String[] localVarContentTypes = {
                "multipart/form-data"
        };
        final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);
        localVarHeaderParams.put("Content-Type", localVarContentType);

        if (progressListener != null) {
            apiClient.getHttpClient().networkInterceptors().add(chain -> {
                com.squareup.okhttp.Response originalResponse = chain.proceed(chain.request());
                return originalResponse.newBuilder()
                        .body(new ProgressResponseBody(originalResponse.body(), progressListener))
                        .build();
            });
        }

        String[] localVarAuthNames = new String[]{"rdbApiKey"};
        //noinspection ConstantConditions
        return apiClient.buildCall(localVarPath, "POST", localVarQueryParams, localVarCollectionQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAuthNames, progressRequestListener);
    }

    private com.squareup.okhttp.Call addMagnetValidateBeforeCall(String magnet, final ProgressResponseBody.ProgressListener progressListener, final ProgressRequestBody.ProgressRequestListener progressRequestListener) throws ApiException {

        return addMagnetCall(magnet, progressListener, progressRequestListener);


    }

    /**
     * RDB - Add Magnet
     * Add a magnet to Real-Debrid
     *
     * @param magnet (optional)
     * @return RdbMagnetLink
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     */
    public RdbMagnetLink addMagnet(String magnet) throws ApiException {
        ApiResponse<RdbMagnetLink> resp = addMagnetWithHttpInfo(magnet);
        return resp.getData();
    }

    /**
     * RDB - Add Magnet
     * Add a magnet to Real-Debrid
     *
     * @param magnet (optional)
     * @return ApiResponse&lt;RdbMagnetLink&gt;
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     */
    public ApiResponse<RdbMagnetLink> addMagnetWithHttpInfo(String magnet) throws ApiException {
        com.squareup.okhttp.Call call = addMagnetValidateBeforeCall(magnet, null, null);
        Type localVarReturnType = new TypeToken<RdbMagnetLink>() {
        }.getType();
        return apiClient.execute(call, localVarReturnType);
    }

    /**
     * RDB - Add Magnet (asynchronously)
     * Add a magnet to Real-Debrid
     *
     * @param magnet   (optional)
     * @param callback The callback to be executed when the API call finishes
     * @return The request call
     * @throws ApiException If fail to process the API call, e.g. serializing the request body object
     */
    public com.squareup.okhttp.Call addMagnetAsync(String magnet, final ApiCallback<RdbMagnetLink> callback) throws ApiException {

        ProgressResponseBody.ProgressListener progressListener = null;
        ProgressRequestBody.ProgressRequestListener progressRequestListener = null;

        if (callback != null) {
            progressListener = callback::onDownloadProgress;

            progressRequestListener = callback::onUploadProgress;
        }

        com.squareup.okhttp.Call call = addMagnetValidateBeforeCall(magnet, progressListener, progressRequestListener);
        Type localVarReturnType = new TypeToken<RdbMagnetLink>() {
        }.getType();
        apiClient.executeAsync(call, localVarReturnType, callback);
        return call;
    }

    /**
     * Build call for deleteTorrent
     *
     * @param id                      Torrent ID (required)
     * @param progressListener        Progress listener
     * @param progressRequestListener Progress request listener
     * @return Call to execute
     * @throws ApiException If fail to serialize the request body object
     */
    @SuppressWarnings("DuplicatedCode")
    public com.squareup.okhttp.Call deleteTorrentCall(String id, final ProgressResponseBody.ProgressListener progressListener, final ProgressRequestBody.ProgressRequestListener progressRequestListener) throws ApiException {
        Object localVarPostBody = null;

        // create path and map variables
        String localVarPath = "/torrents/delete/{id}"
                .replaceAll("\\{" + "id" + "}", apiClient.escapeString(id));

        List<Pair> localVarQueryParams = new ArrayList<>();
        List<Pair> localVarCollectionQueryParams = new ArrayList<>();

        Map<String, String> localVarHeaderParams = new HashMap<>();

        Map<String, Object> localVarFormParams = new HashMap<>();

        final String[] localVarAccepts = {
                "application/json"
        };
        final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        if (localVarAccept != null) localVarHeaderParams.put("Accept", localVarAccept);

        final String[] localVarContentTypes = {

        };
        final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);
        localVarHeaderParams.put("Content-Type", localVarContentType);

        if (progressListener != null) {
            apiClient.getHttpClient().networkInterceptors().add(chain -> {
                com.squareup.okhttp.Response originalResponse = chain.proceed(chain.request());
                return originalResponse.newBuilder()
                        .body(new ProgressResponseBody(originalResponse.body(), progressListener))
                        .build();
            });
        }

        String[] localVarAuthNames = new String[]{"rdbApiKey"};
        //noinspection ConstantConditions
        return apiClient.buildCall(localVarPath, "DELETE", localVarQueryParams, localVarCollectionQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAuthNames, progressRequestListener);
    }

    private com.squareup.okhttp.Call deleteTorrentValidateBeforeCall(String id, final ProgressResponseBody.ProgressListener progressListener, final ProgressRequestBody.ProgressRequestListener progressRequestListener) throws ApiException {
        // verify the required parameter 'id' is set
        if (id == null) {
            throw new ApiException("Missing the required parameter 'id' when calling deleteTorrent(Async)");
        }

        return deleteTorrentCall(id, progressListener, progressRequestListener);


    }

    /**
     * Delete a torrent from real-debrid
     *
     * @param id Torrent ID (required)
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     */
    public void deleteTorrent(String id) throws ApiException {
        deleteTorrentWithHttpInfo(id);
    }

    /**
     * Delete a torrent from real-debrid
     *
     * @param id Torrent ID (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     */
    public ApiResponse<Void> deleteTorrentWithHttpInfo(String id) throws ApiException {
        com.squareup.okhttp.Call call = deleteTorrentValidateBeforeCall(id, null, null);
        return apiClient.execute(call);
    }

    /**
     * (asynchronously)
     * Delete a torrent from real-debrid
     *
     * @param id       Torrent ID (required)
     * @param callback The callback to be executed when the API call finishes
     * @return The request call
     * @throws ApiException If fail to process the API call, e.g. serializing the request body object
     */
    public com.squareup.okhttp.Call deleteTorrentAsync(String id, final ApiCallback<Void> callback) throws ApiException {

        ProgressResponseBody.ProgressListener progressListener = null;
        ProgressRequestBody.ProgressRequestListener progressRequestListener = null;

        if (callback != null) {
            progressListener = callback::onDownloadProgress;

            progressRequestListener = callback::onUploadProgress;
        }

        com.squareup.okhttp.Call call = deleteTorrentValidateBeforeCall(id, progressListener, progressRequestListener);
        apiClient.executeAsync(call, callback);
        return call;
    }

    /**
     * Build call for getTorrentInfo
     *
     * @param id                      Torrent ID (required)
     * @param progressListener        Progress listener
     * @param progressRequestListener Progress request listener
     * @return Call to execute
     * @throws ApiException If fail to serialize the request body object
     */
    @SuppressWarnings("DuplicatedCode")
    public com.squareup.okhttp.Call getTorrentInfoCall(String id, final ProgressResponseBody.ProgressListener progressListener, final ProgressRequestBody.ProgressRequestListener progressRequestListener) throws ApiException {
        Object localVarPostBody = null;

        // create path and map variables
        String localVarPath = "/torrents/info/{id}"
                .replaceAll("\\{" + "id" + "}", apiClient.escapeString(id));

        List<Pair> localVarQueryParams = new ArrayList<>();
        List<Pair> localVarCollectionQueryParams = new ArrayList<>();

        Map<String, String> localVarHeaderParams = new HashMap<>();

        Map<String, Object> localVarFormParams = new HashMap<>();

        final String[] localVarAccepts = {
                "application/json"
        };
        final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        if (localVarAccept != null) localVarHeaderParams.put("Accept", localVarAccept);

        final String[] localVarContentTypes = {

        };
        final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);
        localVarHeaderParams.put("Content-Type", localVarContentType);

        if (progressListener != null) {
            apiClient.getHttpClient().networkInterceptors().add(chain -> {
                com.squareup.okhttp.Response originalResponse = chain.proceed(chain.request());
                return originalResponse.newBuilder()
                        .body(new ProgressResponseBody(originalResponse.body(), progressListener))
                        .build();
            });
        }

        String[] localVarAuthNames = new String[]{"rdbApiKey"};
        //noinspection ConstantConditions
        return apiClient.buildCall(localVarPath, "GET", localVarQueryParams, localVarCollectionQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAuthNames, progressRequestListener);
    }

    private com.squareup.okhttp.Call getTorrentInfoValidateBeforeCall(String id, final ProgressResponseBody.ProgressListener progressListener, final ProgressRequestBody.ProgressRequestListener progressRequestListener) throws ApiException {
        // verify the required parameter 'id' is set
        if (id == null) {
            throw new ApiException("Missing the required parameter 'id' when calling getTorrentInfo(Async)");
        }

        return getTorrentInfoCall(id, progressListener, progressRequestListener);


    }

    /**
     * RDB - Get a torrent&#x27;s info
     * Get all information about the specified torrent on real-debrid
     *
     * @param id Torrent ID (required)
     * @return RdbTorrentInfo
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     */
    public RdbTorrentInfo getTorrentInfo(String id) throws ApiException {
        ApiResponse<RdbTorrentInfo> resp = getTorrentInfoWithHttpInfo(id);
        return resp.getData();
    }

    /**
     * RDB - Get a torrent&#x27;s info
     * Get all information about the specified torrent on real-debrid
     *
     * @param id Torrent ID (required)
     * @return ApiResponse&lt;RdbTorrentInfo&gt;
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     */
    public ApiResponse<RdbTorrentInfo> getTorrentInfoWithHttpInfo(String id) throws ApiException {
        com.squareup.okhttp.Call call = getTorrentInfoValidateBeforeCall(id, null, null);
        Type localVarReturnType = new TypeToken<RdbTorrentInfo>() {
        }.getType();
        return apiClient.execute(call, localVarReturnType);
    }

    /**
     * RDB - Get a torrent&#x27;s info (asynchronously)
     * Get all information about the specified torrent on real-debrid
     *
     * @param id       Torrent ID (required)
     * @param callback The callback to be executed when the API call finishes
     * @return The request call
     * @throws ApiException If fail to process the API call, e.g. serializing the request body object
     */
    public com.squareup.okhttp.Call getTorrentInfoAsync(String id, final ApiCallback<RdbTorrentInfo> callback) throws ApiException {

        ProgressResponseBody.ProgressListener progressListener = null;
        ProgressRequestBody.ProgressRequestListener progressRequestListener = null;

        if (callback != null) {
            progressListener = callback::onDownloadProgress;

            progressRequestListener = callback::onUploadProgress;
        }

        com.squareup.okhttp.Call call = getTorrentInfoValidateBeforeCall(id, progressListener, progressRequestListener);
        Type localVarReturnType = new TypeToken<RdbTorrentInfo>() {
        }.getType();
        apiClient.executeAsync(call, localVarReturnType, callback);
        return call;
    }

    /**
     * Build call for getUser
     *
     * @param progressListener        Progress listener
     * @param progressRequestListener Progress request listener
     * @return Call to execute
     * @throws ApiException If fail to serialize the request body object
     */
    @SuppressWarnings("DuplicatedCode")
    public com.squareup.okhttp.Call getUserCall(final ProgressResponseBody.ProgressListener progressListener, final ProgressRequestBody.ProgressRequestListener progressRequestListener) throws ApiException {
        Object localVarPostBody = null;

        // create path and map variables
        String localVarPath = "/user";

        List<Pair> localVarQueryParams = new ArrayList<>();
        List<Pair> localVarCollectionQueryParams = new ArrayList<>();

        Map<String, String> localVarHeaderParams = new HashMap<>();

        Map<String, Object> localVarFormParams = new HashMap<>();

        final String[] localVarAccepts = {
                "application/json"
        };
        final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        if (localVarAccept != null) localVarHeaderParams.put("Accept", localVarAccept);

        final String[] localVarContentTypes = {

        };
        final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);
        localVarHeaderParams.put("Content-Type", localVarContentType);

        if (progressListener != null) {
            apiClient.getHttpClient().networkInterceptors().add(chain -> {
                com.squareup.okhttp.Response originalResponse = chain.proceed(chain.request());
                return originalResponse.newBuilder()
                        .body(new ProgressResponseBody(originalResponse.body(), progressListener))
                        .build();
            });
        }

        String[] localVarAuthNames = new String[]{"rdbApiKey"};
        //noinspection ConstantConditions
        return apiClient.buildCall(localVarPath, "GET", localVarQueryParams, localVarCollectionQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAuthNames, progressRequestListener);
    }

    private com.squareup.okhttp.Call getUserValidateBeforeCall(final ProgressResponseBody.ProgressListener progressListener, final ProgressRequestBody.ProgressRequestListener progressRequestListener) throws ApiException {

        return getUserCall(progressListener, progressRequestListener);


    }

    /**
     * RDB - Get Current User
     * Get the current real-debrid user info
     *
     * @return RdbUser
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     */
    public RdbUser getUser() throws ApiException {
        ApiResponse<RdbUser> resp = getUserWithHttpInfo();
        return resp.getData();
    }

    /**
     * RDB - Get Current User
     * Get the current real-debrid user info
     *
     * @return ApiResponse&lt;RdbUser&gt;
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     */
    public ApiResponse<RdbUser> getUserWithHttpInfo() throws ApiException {
        com.squareup.okhttp.Call call = getUserValidateBeforeCall(null, null);
        Type localVarReturnType = new TypeToken<RdbUser>() {
        }.getType();
        return apiClient.execute(call, localVarReturnType);
    }

    /**
     * RDB - Get Current User (asynchronously)
     * Get the current real-debrid user info
     *
     * @param callback The callback to be executed when the API call finishes
     * @return The request call
     * @throws ApiException If fail to process the API call, e.g. serializing the request body object
     */
    public com.squareup.okhttp.Call getUserAsync(final ApiCallback<RdbUser> callback) throws ApiException {

        ProgressResponseBody.ProgressListener progressListener = null;
        ProgressRequestBody.ProgressRequestListener progressRequestListener = null;

        if (callback != null) {
            progressListener = callback::onDownloadProgress;

            progressRequestListener = callback::onUploadProgress;
        }

        com.squareup.okhttp.Call call = getUserValidateBeforeCall(progressListener, progressRequestListener);
        Type localVarReturnType = new TypeToken<RdbUser>() {
        }.getType();
        apiClient.executeAsync(call, localVarReturnType, callback);
        return call;
    }

    /**
     * Build call for selectTorrentFiles
     *
     * @param id                      Torrent ID (required)
     * @param files                   (optional)
     * @param progressListener        Progress listener
     * @param progressRequestListener Progress request listener
     * @return Call to execute
     * @throws ApiException If fail to serialize the request body object
     */
    @SuppressWarnings("DuplicatedCode")
    public com.squareup.okhttp.Call selectTorrentFilesCall(String id, String files, final ProgressResponseBody.ProgressListener progressListener, final ProgressRequestBody.ProgressRequestListener progressRequestListener) throws ApiException {
        Object localVarPostBody = null;

        // create path and map variables
        String localVarPath = "/torrents/selectFiles/{id}"
                .replaceAll("\\{" + "id" + "}", apiClient.escapeString(id));

        List<Pair> localVarQueryParams = new ArrayList<>();
        List<Pair> localVarCollectionQueryParams = new ArrayList<>();

        Map<String, String> localVarHeaderParams = new HashMap<>();

        Map<String, Object> localVarFormParams = new HashMap<>();
        if (files != null)
            localVarFormParams.put("files", files);

        final String[] localVarAccepts = {
                "application/json"
        };
        final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        if (localVarAccept != null) localVarHeaderParams.put("Accept", localVarAccept);

        final String[] localVarContentTypes = {
                "multipart/form-data"
        };
        final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);
        localVarHeaderParams.put("Content-Type", localVarContentType);

        if (progressListener != null) {
            apiClient.getHttpClient().networkInterceptors().add(chain -> {
                com.squareup.okhttp.Response originalResponse = chain.proceed(chain.request());
                return originalResponse.newBuilder()
                        .body(new ProgressResponseBody(originalResponse.body(), progressListener))
                        .build();
            });
        }

        String[] localVarAuthNames = new String[]{"rdbApiKey"};
        //noinspection ConstantConditions
        return apiClient.buildCall(localVarPath, "POST", localVarQueryParams, localVarCollectionQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAuthNames, progressRequestListener);
    }

    private com.squareup.okhttp.Call selectTorrentFilesValidateBeforeCall(String id, String files, final ProgressResponseBody.ProgressListener progressListener, final ProgressRequestBody.ProgressRequestListener progressRequestListener) throws ApiException {
        // verify the required parameter 'id' is set
        if (id == null) {
            throw new ApiException("Missing the required parameter 'id' when calling selectTorrentFiles(Async)");
        }

        return selectTorrentFilesCall(id, files, progressListener, progressRequestListener);


    }

    /**
     * RDB - Select Torrent Files
     * Select which files included in a torrent should be downloaded by Real-Debrid
     *
     * @param id    Torrent ID (required)
     * @param files (optional)
     * @return RdbError
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     */
    public RdbError selectTorrentFiles(String id, String files) throws ApiException {
        ApiResponse<RdbError> resp = selectTorrentFilesWithHttpInfo(id, files);
        return resp.getData();
    }

    /**
     * RDB - Select Torrent Files
     * Select which files included in a torrent should be downloaded by Real-Debrid
     *
     * @param id    Torrent ID (required)
     * @param files (optional)
     * @return ApiResponse&lt;RdbError&gt;
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     */
    public ApiResponse<RdbError> selectTorrentFilesWithHttpInfo(String id, String files) throws ApiException {
        com.squareup.okhttp.Call call = selectTorrentFilesValidateBeforeCall(id, files, null, null);
        Type localVarReturnType = new TypeToken<RdbError>() {
        }.getType();
        return apiClient.execute(call, localVarReturnType);
    }

    /**
     * RDB - Select Torrent Files (asynchronously)
     * Select which files included in a torrent should be downloaded by Real-Debrid
     *
     * @param id       Torrent ID (required)
     * @param files    (optional)
     * @param callback The callback to be executed when the API call finishes
     * @return The request call
     * @throws ApiException If fail to process the API call, e.g. serializing the request body object
     */
    public com.squareup.okhttp.Call selectTorrentFilesAsync(String id, String files, final ApiCallback<RdbError> callback) throws ApiException {

        ProgressResponseBody.ProgressListener progressListener = null;
        ProgressRequestBody.ProgressRequestListener progressRequestListener = null;

        if (callback != null) {
            progressListener = callback::onDownloadProgress;

            progressRequestListener = callback::onUploadProgress;
        }

        com.squareup.okhttp.Call call = selectTorrentFilesValidateBeforeCall(id, files, progressListener, progressRequestListener);
        Type localVarReturnType = new TypeToken<RdbError>() {
        }.getType();
        apiClient.executeAsync(call, localVarReturnType, callback);
        return call;
    }

    /**
     * Build call for unrestrictLink
     *
     * @param link                    (optional)
     * @param progressListener        Progress listener
     * @param progressRequestListener Progress request listener
     * @return Call to execute
     * @throws ApiException If fail to serialize the request body object
     */
    @SuppressWarnings("DuplicatedCode")
    public com.squareup.okhttp.Call unrestrictLinkCall(String link, final ProgressResponseBody.ProgressListener progressListener, final ProgressRequestBody.ProgressRequestListener progressRequestListener) throws ApiException {
        Object localVarPostBody = null;

        // create path and map variables
        String localVarPath = "/unrestrict/link";

        List<Pair> localVarQueryParams = new ArrayList<>();
        List<Pair> localVarCollectionQueryParams = new ArrayList<>();

        Map<String, String> localVarHeaderParams = new HashMap<>();

        Map<String, Object> localVarFormParams = new HashMap<>();
        if (link != null)
            localVarFormParams.put("link", link);

        final String[] localVarAccepts = {
                "application/json"
        };
        final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        if (localVarAccept != null) localVarHeaderParams.put("Accept", localVarAccept);

        final String[] localVarContentTypes = {
                "multipart/form-data"
        };
        final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);
        localVarHeaderParams.put("Content-Type", localVarContentType);

        if (progressListener != null) {
            apiClient.getHttpClient().networkInterceptors().add(chain -> {
                com.squareup.okhttp.Response originalResponse = chain.proceed(chain.request());
                return originalResponse.newBuilder()
                        .body(new ProgressResponseBody(originalResponse.body(), progressListener))
                        .build();
            });
        }

        String[] localVarAuthNames = new String[]{"rdbApiKey"};
        //noinspection ConstantConditions
        return apiClient.buildCall(localVarPath, "POST", localVarQueryParams, localVarCollectionQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAuthNames, progressRequestListener);
    }

    private com.squareup.okhttp.Call unrestrictLinkValidateBeforeCall(String link, final ProgressResponseBody.ProgressListener progressListener, final ProgressRequestBody.ProgressRequestListener progressRequestListener) throws ApiException {

        return unrestrictLinkCall(link, progressListener, progressRequestListener);


    }

    /**
     * RDB - Unrestrict a link
     * Unrestrict a real-debrid download link so it can be downloaded
     *
     * @param link (optional)
     * @return RdbUnrestrictedLink
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     */
    public RdbUnrestrictedLink unrestrictLink(String link) throws ApiException {
        ApiResponse<RdbUnrestrictedLink> resp = unrestrictLinkWithHttpInfo(link);
        return resp.getData();
    }

    /**
     * RDB - Unrestrict a link
     * Unrestrict a real-debrid download link so it can be downloaded
     *
     * @param link (optional)
     * @return ApiResponse&lt;RdbUnrestrictedLink&gt;
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     */
    public ApiResponse<RdbUnrestrictedLink> unrestrictLinkWithHttpInfo(String link) throws ApiException {
        com.squareup.okhttp.Call call = unrestrictLinkValidateBeforeCall(link, null, null);
        Type localVarReturnType = new TypeToken<RdbUnrestrictedLink>() {
        }.getType();
        return apiClient.execute(call, localVarReturnType);
    }

    /**
     * RDB - Unrestrict a link (asynchronously)
     * Unrestrict a real-debrid download link so it can be downloaded
     *
     * @param link     (optional)
     * @param callback The callback to be executed when the API call finishes
     * @return The request call
     * @throws ApiException If fail to process the API call, e.g. serializing the request body object
     */
    public com.squareup.okhttp.Call unrestrictLinkAsync(String link, final ApiCallback<RdbUnrestrictedLink> callback) throws ApiException {

        ProgressResponseBody.ProgressListener progressListener = null;
        ProgressRequestBody.ProgressRequestListener progressRequestListener = null;

        if (callback != null) {
            progressListener = callback::onDownloadProgress;

            progressRequestListener = callback::onUploadProgress;
        }

        com.squareup.okhttp.Call call = unrestrictLinkValidateBeforeCall(link, progressListener, progressRequestListener);
        Type localVarReturnType = new TypeToken<RdbUnrestrictedLink>() {
        }.getType();
        apiClient.executeAsync(call, localVarReturnType, callback);
        return call;
    }
}
