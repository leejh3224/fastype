package fastype;

import fastype.cmd.AuthCommand;
import fastype.cmd.ConfigCommand;
import fastype.cmd.DraftCommand;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "fastype",
        description = "Fastype is an automation tool to help you convert your markdown files into Postype compatible html and upload it for you",
        subcommands = {
                AuthCommand.class,
                DraftCommand.class,
                ConfigCommand.class
        }
)
public class Fastype implements Callable<Integer> {

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Fastype()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        return 0;
    }
}
