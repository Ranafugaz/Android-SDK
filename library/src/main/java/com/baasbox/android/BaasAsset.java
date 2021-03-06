/*
 * Copyright (C) 2014. BaasBox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.baasbox.android;

import com.baasbox.android.json.JsonObject;
import com.baasbox.android.net.HttpRequest;
import com.baasbox.android.net.HttpResponse;

/**
 * Collection of functions that work with assets.
 *
 * Created by Andrea Tortorella on 13/03/14.
 */
public final class BaasAsset {

    private BaasAsset(){}

    /**
     * Asynchronously retrieves named assets data,
     * If the named asset is a document, the document is retrieved
     * otherwise attached data to the file are returned.
     * This version of the method uses default priority.
     *
     * @param id the name of the asset
     * @param handler an handler that will be handed the response
     * @return a request token
     */
    public static RequestToken fetchData(String id, BaasHandler<JsonObject> handler){
        return fetchData(id, RequestOptions.DEFAULT, handler);
    }

    /**
     * Asynchronously retrieves named assets data,
     * If the named asset is a document, the document is retrieved
     * otherwise attached data to the file are returned.
     *
     * @param id the name of the asset
     * @param flags used for the request a bit or of {@link RequestOptions} constants
     * @param handler an handler that will be handed the response
     * @return a request token
     */
    public static RequestToken fetchData(String id, int flags, BaasHandler<JsonObject> handler){
        if(id==null) throw new IllegalArgumentException("asset id cannot be null");
        BaasBox box = BaasBox.getDefaultChecked();
        AssetDataRequest req = new AssetDataRequest(box,id,flags,handler);
        return box.submitAsync(req);
    }

    /**
     * Synchronously retrieves named assets data,
     * If the named asset is a document, the document is retrieved
     * otherwise attached data to the file are returned.
     *
     * @param id the name of the asset
     * @return a baas result wrapping the response.
     *
     */
    public static BaasResult<JsonObject> fetchDataSync(String id){
        if (id == null) throw new IllegalArgumentException("asset id cannot be null");
        BaasBox box = BaasBox.getDefaultChecked();
        AssetDataRequest req = new AssetDataRequest(box,id, RequestOptions.DEFAULT,null);
        return box.submitSync(req);
    }

    public static RequestToken streamAsset(String id,BaasHandler<byte[]> handler){
        return BaasAsset.streamAsset(id,new AssetStreamer(),handler);
    }


    private static class AssetStreamer extends StreamBody<byte[]> {
        @Override
        protected byte[] convert(byte[] body, String id, String contentType, long contentLength) {
            return body;
        }
    }

    /**
     * Streams the file using the provided data stream handler.
     *
     * @param id      the name of the asset to download
     * @param data    the data stream handler {@link com.baasbox.android.DataStreamHandler}
     * @param handler the completion handler
     * @param <R>     the type to transform the bytes to.
     * @return a request token to handle the request
     */
    public static <R> RequestToken streamAsset(String id, DataStreamHandler<R> data, BaasHandler<R> handler) {
        return BaasAsset.doStreamAsset(id, null, -1, RequestOptions.DEFAULT, data, handler);
    }

    /**
     * Streams the file using the provided data stream handler.
     *
     * @param id      the name of the asset to download
     * @param size    a size spec to specify the resize of an image asset
     * @param data    the data stream handler {@link com.baasbox.android.DataStreamHandler}
     * @param handler the completion handler
     * @param <R>     the type to transform the bytes to.
     * @return a request token to handle the request
     */
    public static <R> RequestToken streamImageAsset(String id, int size, DataStreamHandler<R> data, BaasHandler<R> handler) {
        return BaasAsset.doStreamAsset(id, null, size, RequestOptions.DEFAULT, data, handler);
    }

    /**
     * Streams the file using the provided data stream handler.
     *
     * @param id       the name of the asset to download
     * @param flags
     * @param handler  the completion handler
     * @param <R>      the type to transform the bytes to.
     * @return a request token to handle the request
     */
    public static <R> RequestToken streamAsset(String id, int flags, DataStreamHandler<R> contentHandler, BaasHandler<R> handler) {
        return BaasAsset.doStreamAsset(id, null, -1, flags, contentHandler, handler);
    }

    /**
     * Streams the file using the provided data stream handler.
     *
     * @param id       the name of the asset to download
     * @param size     a size spec to specify the resize of an image asset
     * @param flags    {@link RequestOptions}
     * @param data     the data stream handler {@link com.baasbox.android.DataStreamHandler}
     * @param handler  the completion handler
     * @param <R>      the type to transform the bytes to.
     * @return a request token to handle the request
     */
    public static <R> RequestToken streamImageAsset(String id, int size, int flags, DataStreamHandler<R> data, BaasHandler<R> handler) {
        return BaasAsset.doStreamAsset(id, null, size, flags, data, handler);
    }

    /**
     * Synchronously streams the asset.
     *
     * @param id   the name of the asset to download
     * @param spec a size spec to specify the resize of an image asset
     * @return a {@link com.baasbox.android.BaasStream} wrapped in a result
     */
    public static BaasResult<BaasStream> streamImageAssetSync(String id, String spec) {
        return BaasAsset.doStreamSync(id, spec, -1);
    }

    /**
     * Synchronously streams the asset.
     *
     * @param id     the name of the asset to download
     * @param sizeId the size index if the asset is an image
     * @return a {@link com.baasbox.android.BaasStream} wrapped in a result
     */
    public static BaasResult<BaasStream> streamImageAssetSync(String id, int sizeId) {
        return BaasAsset.doStreamSync(id, null, sizeId);
    }


    static BaasResult<BaasStream> doStreamSync(String id, String spec, int sizeId) {
        BaasBox box = BaasBox.getDefaultChecked();

        if (id == null) throw new IllegalArgumentException("id cannot be null");
        StreamRequest synReq = new StreamRequest(box, "asset", id, spec, sizeId);
        return box.submitSync(synReq);
    }

    static <R> RequestToken doStreamAsset(String name, String sizeSpec, int sizeIdx, int priority, DataStreamHandler<R> dataStreamHandler, BaasHandler<R> handler) {
        BaasBox box = BaasBox.getDefaultChecked();
        if (dataStreamHandler == null) throw new IllegalArgumentException("data handler cannot be null");
        if (name == null) throw new IllegalArgumentException("id cannot be null");
        AsyncStream<R> stream = new AssetStream<R>(box, name, sizeSpec, sizeIdx, priority, dataStreamHandler, handler);
        return box.submitAsync(stream);
    }


    private static class AssetDataRequest extends NetworkTask<JsonObject> {
        private final String name;

        protected AssetDataRequest(BaasBox box,String name, int flags, BaasHandler<JsonObject> handler) {
            super(box, flags, handler, false);
            this.name=name;
        }

        @Override
        protected JsonObject onOk(int status, HttpResponse response, BaasBox box) throws BaasException {
            return parseJson(response,box);
        }

        @Override
        protected HttpRequest request(BaasBox box) {
            RequestFactory f = box.requestFactory;
            String endpoint =f.getEndpoint("asset/{}/data",name);
            return f.get(endpoint);
        }
    }

    private static class AssetStream<R> extends AsyncStream<R> {
        private final String name;
        private HttpRequest request;

        protected AssetStream(BaasBox box, String name, String sizeSpec, int sizeId, int flags, DataStreamHandler<R> dataStream, BaasHandler<R> handler) {
            super(box, flags, dataStream, handler, false);
            this.name = name;
            RequestFactory.Param param = null;
            if (sizeSpec != null) {
                param = new RequestFactory.Param("resize", sizeSpec);
            } else if (sizeId >= 0) {
                param = new RequestFactory.Param("sizeId", Integer.toString(sizeId));
            }
            String endpoint = box.requestFactory.getEndpoint("asset/{}", name);
            if (param != null) {
                request = box.requestFactory.get(endpoint, param);
            } else {
                request = box.requestFactory.get(endpoint);
            }
        }

        @Override
        protected String streamId() {
            return name;
        }

        @Override
        protected HttpRequest request(BaasBox box) {
            return request;
        }
    }
}
