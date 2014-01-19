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

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by eto on 10/01/14.
 */
public class RequestToken implements Parcelable, Comparable<RequestToken> {
    final int requestId;


    RequestToken(int requestId) {
        this.requestId = requestId;
    }

    public void suspend() {
        BAASBox.getDefaultChecked().suspend(this);
    }

    public <T> boolean resume(T tag, BAASBox.BAASHandler<?, T> handler) {
        RequestToken t = BAASBox.getDefaultChecked().resume(this, tag, handler);
        return this.equals(t);
    }

    public boolean cancel() {
        return BAASBox.getDefaultChecked().cancel(this);
    }

    @Override
    public int hashCode() {
        return requestId;
    }

    @Override
    public int compareTo(RequestToken another) {
        return this.requestId - another.requestId;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o instanceof RequestToken) {
            return ((RequestToken) o).requestId == requestId;
        }
        return false;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(requestId);
    }


    public static Creator<RequestToken> CREATOR =
            new Creator<RequestToken>() {
                @Override
                public RequestToken createFromParcel(Parcel source) {
                    return new RequestToken(source.readInt());
                }

                @Override
                public RequestToken[] newArray(int size) {
                    return new RequestToken[size];
                }
            };
}
