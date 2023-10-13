import java.io.*;
import java.net.*;
import java.util.Date;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
/**
 * Class for handling server side connect4
 * @author Connor McCoy
 *
 */
public class Connect4Server extends Application 
implements Connect4Constants {
private int sessionNo = 1; 


@Override
/**
 * Start method for connecting new players to the Connect4 Server
 */
public void start(Stage stage) throws Exception {
	TextArea taLog = new TextArea();
	Scene scene = new Scene(new ScrollPane(taLog), 900, 400);
	stage.setTitle("Connect4Server");
	stage.setScene(scene);
	stage.show();
	
	new Thread ( () -> {
		try {
			ServerSocket serverSocket = new ServerSocket(8000);
			Platform.runLater(() -> taLog.appendText(new Date() + ": Server Started at socket 8000\n"));
			
			while (true) {
				Platform.runLater( () -> taLog.appendText(new Date() + ": Wait for players to join session " + sessionNo + '\n'));
				
				Socket player1 = serverSocket.accept();
				
				Platform.runLater(() -> {
					taLog.appendText(new Date() + ": Player 1 joined session " + sessionNo + '\n');
					taLog.appendText ("Player 1's IP address" + player1.getInetAddress().getHostAddress() + '\n');
				});
				
				new DataOutputStream ( player1.getOutputStream()).writeInt(PLAYER1);
				
				Socket player2 = serverSocket.accept();
				
				Platform.runLater(() -> {
					taLog.appendText(new Date() + ": Player 2 joined session " + sessionNo + '\n');
					taLog.appendText("Players 2's IP adress" + player2.getInetAddress().getHostAddress() + '\n');
				});
				
				new DataOutputStream(player2.getOutputStream()).writeInt(PLAYER2);
				Platform.runLater(() -> taLog.appendText(new Date() + ": Start a thread for session " + sessionNo++ + '\n'));
				
				new Thread(new HandleASession (player1, player2)).start();
			}
		}
		catch(IOException ex){
			ex.printStackTrace();
		}
	}).start();
	}

class HandleASession implements Runnable, Connect4Constants {
	
	private Socket player1;
	private Socket player2;
	
	private char[][] gameBoard = new char[6][7];
	
	private DataInputStream fromPlayer1;
	private DataOutputStream toPlayer1;
	private DataInputStream fromPlayer2;
	private DataOutputStream toPlayer2;
	
	private boolean keepPlaying = true;
	
	/**
	 * Starts a session with two players
	 * @param player1 is the first player to connect
	 * @param player2 the second player to connect
	 */
	
	public HandleASession(Socket player1, Socket player2) {
		this.player1 = player1;
		this.player2 = player2;
		
		for (int i = 0; i < 6; i++)
			for (int j = 0; j < 7; j++)
				gameBoard[i][j] = ' ';
	}
	
	/**
	 * run method for starting the game
	 */
	
	public void run() {
		try {
				DataInputStream fromPlayer1 = new DataInputStream(player1.getInputStream());
				DataOutputStream toPlayer1 = new DataOutputStream( player1.getOutputStream());
			    DataInputStream fromPlayer2 = new DataInputStream(player2.getInputStream());
			    DataOutputStream toPlayer2 = new DataOutputStream(player2.getOutputStream());
			    
			    toPlayer1.writeInt(1);
			    
			    while(true) {
			    	int xPos = fromPlayer1.readInt();
			    	int yPos = fromPlayer1.readInt();
			    	gameBoard[xPos][yPos] = 'X';
			    	
			    	if (foundWinner('X')) {
			    		toPlayer1.writeInt(PLAYER1_WON);
			    		toPlayer2.writeInt(PLAYER1_WON);
			    		sendMove(toPlayer2, xPos, yPos);
			    		break;
			    	} else if(isDraw()) {
			    		toPlayer1.writeInt(DRAW);
			    		toPlayer2.writeInt(DRAW);
			    		sendMove(toPlayer2, xPos, yPos);
			    		break;
			    	} else {
			    		toPlayer2.writeInt(CONTINUE);
			    		
			    		sendMove(toPlayer2, xPos, yPos);
			    	}
			    	
			    	xPos = fromPlayer2.readInt();
			    	yPos = fromPlayer2.readInt();
			    	gameBoard[xPos][yPos] = 'O';
			    	
			    	if(foundWinner('O')) {
			    		toPlayer1.writeInt(PLAYER2_WON);
			    		toPlayer2.writeInt(PLAYER2_WON);
			    		sendMove(toPlayer1, xPos, yPos);
			    		break;
			    	}else {
			    		toPlayer1.writeInt(CONTINUE);
			    		sendMove(toPlayer1, xPos, yPos);
			    	}
			    }
		}
		catch(IOException ex) {
			ex.printStackTrace();
		}
	}
	
	private void sendMove(DataOutputStream out, int xPos, int yPos)
	        throws IOException {
	      out.writeInt(xPos);
	      out.writeInt(yPos); 
	    }
	
	private boolean isDraw() {
	      for (int i = 0; i < 6; i++)
	        for (int j = 0; j < 7; j++)
	          if (gameBoard[i][j] == ' ')
	            return false; 
	      return true;
	    }
	
	private boolean foundWinner(char token)
    {
        boolean isWinner = false;

        for (int[] d : DIRECTIONS) {
            int xdirection = d[0];
            int ydirection = d[1];
            for (int x = 0; x < 6; x++) {
                for (int y = 0; y < 7; y++) {
                    int finalx = x + 3*xdirection;
                    int finaly = y + 3*ydirection;
                    if (0 <= finalx && finalx < 6 && 0 <= finaly && finaly < 7) {
                        char w = gameBoard[x][y];
                        if (w != ' ' && w == gameBoard[x+xdirection][y+ydirection]
                                && w == gameBoard[x+2*xdirection][y+2*ydirection]
                                && w == gameBoard[finalx][finaly]) {
                            isWinner = true;
                            break;
                        }
                    }
                }
            }
        }
            return isWinner;     
    }
	
}
/**
 * for running with javaFX
 * @param args
 */
public static void main(String[] args) {
    launch(args);
  }

}
