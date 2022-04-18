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

    @NonNull
    @Override
    public String toString() {
        return String.format("(%s, %s)",
                mType,
                mData
        );
    }

    public enum Type {
        FAILED_TO_LOAD,
        LOADED,
        CLICKED,
        DISMISSED;

        @NonNull
        @Override
        public String toString() {
            switch (this) {
                case FAILED_TO_LOAD:
                    return "FAILED_TO_LOAD";
                case LOADED:
                    return "LOADED";
                case CLICKED:
                    return "CLICKED";
                case DISMISSED:
                    return "DISMISSED";
            }
            return super.toString();
        }
    }
}
