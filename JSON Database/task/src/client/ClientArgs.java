package client;

import com.beust.jcommander.Parameter;

public class ClientArgs {
    public static final String SENT = "Sent: ";
    public static final String RECEIVED = "Received: ";
    public static final String CLIENT_STARTED = "Client started!";
    public static final String PATH_TO_CLIENT_FILE = "./src/client/data/";

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

    @Parameter(
            names = "-in",
            description = "File containing the request as JSON string"
    )
    private String file;

    public String getType() {
        return type;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public String getFile() {
        return file;
    }
}
