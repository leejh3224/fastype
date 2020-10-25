package fastype.cmd;

import fastype.Config;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine.*;

import java.util.concurrent.Callable;

@Slf4j
@Command(
        name = "config"
)
public class ConfigCommand implements Callable<Integer> {
    @Option(names = { "-k", "--key" }, description = "config key", defaultValue = "")
    String key;

    @Option(names = { "-v", "--value" }, description = "config value", defaultValue = "")
    String value;

    @Override
    public Integer call() {
        if (key.isEmpty() || value.isEmpty()) {
            log.debug("`key` and `value` must not be empty");
            return 2;
        }
        Config.set(key, value);
        Config.write();
        log.debug("key of {} successfully set", key);
        return 0;
    }
}
