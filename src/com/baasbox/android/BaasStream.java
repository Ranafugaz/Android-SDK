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
 * See the License for the specific language governing permissions andlimitations under the License.
 */

package com.baasbox.android;

import org.apache.http.Header;
import org.apache.http.HttpEntity;

import java.io.BufferedInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by eto on 16/01/14.
 */
public final class BaasStream extends FilterInputStream {
    private final HttpEntity entity;
    public final String contentType;
    public final long contentLength;
    public final String id;

    BaasStream(String id, HttpEntity entity) throws IOException {
        super(getInput(entity));
        this.entity = entity;
        this.id = id;
        Header contentTypeHeader = entity.getContentType();
        String contentType = "application/octet-stream";
        if (contentTypeHeader != null) {
            contentType = contentTypeHeader.getValue();
        }
        this.contentType = contentType;
        contentLength = entity.getContentLength();
    }

    @Override
    public void close() throws IOException {
        super.close();
        entity.consumeContent();
    }


    public static BufferedInputStream getInput(HttpEntity entity) throws IOException {
        InputStream in = entity.getContent();
        if (in instanceof BufferedInputStream) {
            return (BufferedInputStream) in;
        }
        return new BufferedInputStream(in);
    }
}
