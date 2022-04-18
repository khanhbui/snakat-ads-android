package com.snakat.ads;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class AdsEvent {
    private final Type mType;
    private final Object mData;

    public AdsEvent(@NonNull Type type) {
        this(type, null);
    }

    public AdsEvent(@NonNull Type type, @Nullable Object data) {
        mType = type;
        mData = data;
    }

    @NonNull
    public Type getType() {
        return mType;
    }

    @Nullable
    public Object getData() {
        return mData;
    }

    public enum Type {
        FAILED_TO_LOAD,
        LOADED,
        CLICKED,
        DISMISSED
    }
}
