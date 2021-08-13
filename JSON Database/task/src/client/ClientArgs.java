package client;

import com.beust.jcommander.Parameter;

public class ClientArgs {
    @Parameter(
            names = "-t",
            description = "Type of the message"
    )
    private String type;

    @Parameter(
            names = "-k",
            description = "Key of the message"
    )
    private String key;

    @Parameter(
            names = "-v",
            description = "The value of the message"
    )
    private String value;

    public String getType() {
        return type;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
