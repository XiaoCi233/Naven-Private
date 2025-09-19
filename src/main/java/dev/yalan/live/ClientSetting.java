package dev.yalan.live;

import com.google.gson.annotations.SerializedName;

public class ClientSetting {
    @SerializedName("version")
    private final String version;

    public ClientSetting(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }
}
