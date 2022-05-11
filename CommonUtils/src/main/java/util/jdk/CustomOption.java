package util.jdk;

import org.apache.commons.cli.Option;

public class CustomOption extends Option {

    public CustomOption(String opt, String description) throws IllegalArgumentException {
        super(opt, description);

    }

    public CustomOption(String opt, boolean hasArg, String description) throws IllegalArgumentException {
        super(opt, hasArg, description);
    }

    public CustomOption(String opt, String longOpt, boolean hasArg, String description) throws IllegalArgumentException {
        super(opt, longOpt, hasArg, description);
    }
    public CustomOption(String opt, String longOpt, boolean hasArg, String description,boolean isRequired) throws IllegalArgumentException {
        super(opt, longOpt, hasArg, description);
        setRequired(isRequired);
    }
}
