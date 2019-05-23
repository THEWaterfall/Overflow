package waterfall.protocol.command;

import com.google.inject.Inject;
import waterfall.communication.server.ClientHandler;
import waterfall.communication.server.SocketClientHandler;
import waterfall.game.AIPlayer;
import waterfall.game.GameFactoryImpl;
import waterfall.game.Player;
import waterfall.model.Account;
import waterfall.model.Lobby;
import waterfall.protocol.Command;
import waterfall.protocol.CommandConstants;
import waterfall.protocol.CommandUtil;
import waterfall.service.GameTypeService;
import waterfall.service.LobbyService;

public class PlayCommand implements CommandAction {

    @Inject
    private CommandUtil commandUtil;

    @Inject
    private LobbyService lobbyService;

    @Inject
    private GameTypeService gameTypeService;

    private GameFactoryImpl gameFactory;

    public PlayCommand() {
        this.gameFactory = new GameFactoryImpl();
    }

    @Override
    public Command execute(ClientHandler clientHandler, Command command) {
        Account account = clientHandler.getAccount();
        Command response = commandUtil.constructCommand(
                CommandConstants.COMMAND_PLAY,
                CommandConstants.COMMAND_TYPE_RESPONSE,
                CommandConstants.COMMAND_TYPE_HANDLER,
                CommandConstants.COMMAND_STATUS_SUCCESS);

        if(!account.isInLobby()) {
            response.setStatus(CommandConstants.COMMAND_STATUS_SUCCESS);
            Lobby currentLobby = new Lobby();
            account.setLobby(currentLobby);

            currentLobby.addUser(account.getUser());

            currentLobby.setGame(gameFactory.getGame(command.getAttributesCommand().get(0)));
            currentLobby.setGameType(gameTypeService.findByName(command.getAttributesCommand().get(0)));
            account.setPlayer(gameFactory.getPlayer(command.getAttributesCommand().get(0)));
            currentLobby.getGame().registerPlayer(account.getPlayer());

            lobbyService.save(currentLobby);

            if (command.getAttributesCommand().size() > 1 && command.getAttributesCommand().get(1).equals("bot")) {
                constructAIPlayer(clientHandler, command);
                response.setMessage("The game has been started.");
            } else {
                response.setMessage("Lobby has been created with id: " + currentLobby.getId());
            }
        } else {
            response.setStatus(CommandConstants.COMMAND_STATUS_FAILURE);
            response.setMessage("You are already in lobby.");
        }

        return response;
    }

    private void constructAIPlayer(ClientHandler clientHandler, Command command) {
        Player aiPlayer = gameFactory.getPlayer("ai" + command.getAttributesCommand().get(0));

        ClientHandler aiHandler = new SocketClientHandler();
        aiHandler.getAccount().setPlayer(aiPlayer);
        aiHandler.getAccount().setLobby(clientHandler.getAccount().getLobby());
        aiHandler.getAccount().setOpponentHandler(clientHandler);

        clientHandler.getAccount().setOpponentHandler(aiHandler);

        clientHandler.getAccount().getLobby().getGame().registerPlayer(aiPlayer);

        new Thread(aiHandler).start();
    }
}
