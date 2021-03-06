package waterfall.communication.server;

import com.google.inject.Guice;
import com.google.inject.Injector;
import waterfall.game.GameFactory;
import waterfall.game.GameFactoryImpl;
import waterfall.game.UserPlayer;
import waterfall.game.chess.ChessGame;
import waterfall.injection.Module;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SocketServer implements Server {
    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private List<ClientHandler> clientHandlerList;
    private Injector injector;
    private GameFactory gameFactory;

    private int port;

    private boolean isStopped = true;

    public SocketServer(int port, int clients) {
        this.port = port;
        this.threadPool = Executors.newFixedThreadPool(clients);
        this.clientHandlerList = new ArrayList<>(clients);
        this.injector = Guice.createInjector(new Module());

        this.gameFactory = new GameFactoryImpl();
        this.gameFactory.register("chess", ChessGame.class);
        this.gameFactory.register("chess", UserPlayer.class);
    }

    @Override
    public void start() {
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }

        isStopped = false;
        while (!isStopped()) {
            Socket socket = null;
            try {
                socket = serverSocket.accept();
            } catch (IOException e) {
                e.printStackTrace();
            }

            SocketClientHandler clientHandler = injector.getInstance(SocketClientHandler.class);
            clientHandler.setSocket(socket);
            clientHandler.setClientHandlerList(clientHandlerList);

            clientHandlerList.add(clientHandler);

            threadPool.execute(clientHandler);
        }
    }

    @Override
    public void stop() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        isStopped = true;
    }

    public boolean isStopped() {
        return isStopped;
    }

    public static void main(String[] args) {
        new SocketServer(8088, 10).start();
    }
}
