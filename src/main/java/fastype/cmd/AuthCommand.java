package fastype.cmd;

import fastype.Auth;
import fastype.Config;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine.*;

import java.util.concurrent.Callable;

@Slf4j
@Command(
        name = "auth"
)
public class AuthCommand implements Callable<Integer> {
    @Option(names = {"-i", "--interactive" }, description = "Whether to open a browser and enter credential")
    boolean interactive;

    @Option(names = {"-t", "--token"}, description = "Postype auth token, cookie with key `PSE1`")
    String token;

    @Override
    public Integer call() throws Exception {
        if (interactive) {
            log.debug("chrome browser will be open in a moment...");
            Auth auth = Auth.getInstance();
            String token = auth.getToken();
            Config.set("token", token);
        } else {
            if (token.isEmpty()) {
                return 2;
            }
            Config.set("token", token);
            log.debug("Auth token successfully set");
        }
        Config.write();
        return 0;
    }
}
