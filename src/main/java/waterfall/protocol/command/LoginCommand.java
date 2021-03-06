package waterfall.protocol.command;

import com.google.inject.Guice;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import waterfall.communication.server.ClientHandler;
import waterfall.injection.Module;
import waterfall.model.Account;
import waterfall.model.User;
import waterfall.protocol.Command;
import waterfall.protocol.CommandConstants;
import waterfall.protocol.CommandUtil;
import waterfall.protocol.validation.LoginValidation;
import waterfall.protocol.validation.Validation;
import waterfall.protocol.validation.Validator;
import waterfall.security.Security;

public class LoginCommand implements CommandAction {

    @Inject
    private Security security;

    @Inject
    private CommandUtil commandUtil;

    public LoginCommand() {

    }

    @Override
    public Command execute(ClientHandler clientHandler, Command command) {
        Account account = clientHandler.getAccount();
        Command response = commandUtil.constructCommand(
                CommandConstants.COMMAND_LOGIN,
                CommandConstants.COMMAND_TYPE_RESPONSE,
                CommandConstants.COMMAND_TYPE_HANDLER,
                CommandConstants.COMMAND_STATUS_SUCCESS
        );


        if(!account.isLoggedIn()) {
            User user = login(command.getAttributesCommand().get(0), command.getAttributesCommand().get(1));
            if(user != null) {
                account.setUser(user);
                response.setMessage("You have successfully logged in.");
            } else {
                response.setMessage("User is not found.");
            }
        } else {
            response.setMessage("You are already logged in.");
            response.setStatus(CommandConstants.COMMAND_STATUS_FAILURE);
        }

        return response;
    }

    private User login(String username, String password) {
        return security.authorize(username, password);
    }
}
