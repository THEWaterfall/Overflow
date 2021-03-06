package waterfall.communication.server;

import com.google.inject.Inject;
import waterfall.model.Account;
import waterfall.protocol.Command;
import waterfall.protocol.CommandConstants;
import waterfall.protocol.CommandUtil;
import waterfall.protocol.command.CommandAction;
import waterfall.protocol.command.CommandHandler;
import waterfall.protocol.validation.ValidationResult;
import waterfall.protocol.validation.Validator;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class SocketClientHandler implements ClientHandler {

    private Socket socket;
    private BufferedReader input;
    private BufferedWriter output;

    @Inject
    private CommandUtil commandUtil;

    private Validator validator;

    private Account account;

    private boolean isStopped = true;

    public SocketClientHandler() {
        this.account = new Account(this);
        this.validator = new Validator();
    }

    @Override
    public void run() {
        start();

        onConnect();
        Command command = new Command();
        try {
            while (!isStopped()) {
                command = receiveRequest();
                Command response = processCommand(command);
                sendResponse(response);
            }
        } finally {
            CommandHandler.getCommand(CommandConstants.COMMAND_EXIT).execute(this, command);
            stop();
        }
    }

    @Override
    public void stopConnection() {
        isStopped = true;
    }

    private void start() {
        try {
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        isStopped = false;
    }

    private void stop() {
        try {
            account.getClientHandlers().remove(this);
            output.close();
            input.close();
            socket.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Command receiveRequest() {
        String request = "";
        try {
            request = input.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return commandUtil.convertToCommand(request);
    }

    @Override
    public void sendResponse(Command response) {
        try {
            output.write(commandUtil.covertToString(response));
            output.newLine();
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Command processCommand(Command command) {
        command = verifyAuth(command);

        CommandAction commandAction = CommandHandler.getCommand(command.getTypeCommand());

        // Command validation
        ValidationResult result = validator.validate(command, CommandHandler.getValidations(command.getTypeCommand()));

        if(!result.isValid()) {
            return result.getErrorCommand();
        }

        // Command execution
        Command response = commandAction.execute(this, command);

        return response;
    }

    @Override
    public Account getAccount() {
        return account;
    }

    public boolean isStopped() {
        return isStopped;
    }

    private Command verifyAuth(Command command) {
        if (!account.isLoggedIn() &&
                !command.getTypeCommand().equals(CommandConstants.COMMAND_LOGIN) &&
                !command.getTypeCommand().equals(CommandConstants.COMMAND_EXIT)) {
            command = constructLogin();
        }

        return command;
    }

    private Command constructLogin() {
        Command command = commandUtil.constructCommand(
                CommandConstants.COMMAND_MESSAGE,
                CommandConstants.COMMAND_TYPE_RESPONSE,
                CommandConstants.COMMAND_TYPE_HANDLER,
                CommandConstants.COMMAND_STATUS_SUCCESS
        );
        command.setMessage("Type /login [username] [password] to log in.");

        return command;
    }

    private void onConnect() {
        Command loginCommand = constructLogin();
        sendResponse(loginCommand);
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public List<ClientHandler> getClientHandlerList() {
        return this.account.getClientHandlers();
    }

    public void setClientHandlerList(List<ClientHandler> clientHandlerList) {
        this.account.setClientHandlers(clientHandlerList);
    }

}
