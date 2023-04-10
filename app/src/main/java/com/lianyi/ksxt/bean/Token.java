package com.lianyi.ksxt.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class Token implements Parcelable {

    /**
     * access_token : 86206333-0c2f-40e7-9d15-553567f83f56
     * token_type : bearer
     * expires_in : 3599
     * scope : read write
     */

    public String access_token;
    public String token_type;
    public int expires_in;
    public String scope;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.access_token);
        dest.writeString(this.token_type);
        dest.writeInt(this.expires_in);
        dest.writeString(this.scope);
    }

    public Token() {
    }

    protected Token(Parcel in) {
        this.access_token = in.readString();
        this.token_type = in.readString();
        this.expires_in = in.readInt();
        this.scope = in.readString();
    }

    public static final Creator<Token> CREATOR = new Creator<Token>() {
        @Override
        public Token createFromParcel(Parcel source) {
            return new Token(source);
        }

        @Override
        public Token[] newArray(int size) {
            return new Token[size];
        }
    };
}
