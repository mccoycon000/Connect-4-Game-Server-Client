import java.io.*;
import java.net.*;
import java.util.Date;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
/**
 * Class for handling client side connect 4
 * @author Connor McCoy
 *
 */
public class Connect4Client extends Application implements Connect4Constants
{
	 private boolean myTurn = false;
	  private char myToken = ' ';
	  private char otherToken = ' ';
	  private Label lblTitle = new Label();
	  private Label lblStatus = new Label();
	  private int xSelected;
	  private int ySelected;
	  private DataInputStream fromServer;
	  private DataOutputStream toServer;
	  private boolean continuePlaying = true;
	  private boolean waiting = true;
	  private String host = "localhost";
    private Cell[][] cell = new Cell[6][7];
    private static int directions[][] = {
            {1,0}, {1,-1}, {1,1}, {0,1}
    };

    /**
     * Sets up and begins the GUI connect4 game
     * @param the main stage for setting up GUI
     */
    public void start(Stage primaryStage)
    {

        GridPane pane = new GridPane();
        for (int i = 0; i < 6; i++)
            for (int j = 0; j < 7; j++)
                pane.add(cell[i][j] = new Cell(i, j), j, i);
        
        for (int i = 0; i < 5; i++)
            for (int j = 0; j < 7; j++)
                cell[i][j].next = cell[i+1][j];
        for (int i = 5; i > 4; i--)
            for (int j = 0; j < 7; j++)
                cell[i][j].next = cell[i][j];

        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(pane);
        borderPane.setBottom(lblStatus);
        Scene scene = new Scene(borderPane, 450, 450);
        primaryStage.setTitle("ConnectFour"); 
        primaryStage.setScene(scene); 
        primaryStage.show(); 
        
        connectToServer();
    }
    
    private void connectToServer() {
    	try {
    		Socket socket = new Socket (host,8000);
    		
    		fromServer = new DataInputStream(socket.getInputStream());
    		
    		toServer = new DataOutputStream(socket.getOutputStream());
    	}catch (Exception e) {
    		e.printStackTrace();
    	}
    
    
    new Thread(() -> {
    	try {
    		int player = fromServer.readInt();
    		
    		if (player == PLAYER1) {
    			myToken = 'X';
    			otherToken = 'O';
    			Platform.runLater(() -> {
    				lblTitle.setText("Player 1 with token 'X'");
    				lblStatus.setText("Waiting for player 2 to join");
    			});
    			
    			fromServer.readInt();
    			
    			Platform.runLater (() -> lblStatus.setText("Player 2 has Joined. I start first"));
    			
    			myTurn = true;
    		}
    		else if (player == PLAYER2) {
    			myToken = 'O';
    			otherToken = 'X';
    			Platform.runLater(() -> {
    				lblTitle.setText("Player 2 with token 'O'");
    				lblStatus.setText("Waiting for player 1 to move");
    			});
    		}
    		
    		while (continuePlaying) {
    			if (player == PLAYER1) {
    				waitForPlayerAction();
    				sendMove();
    				receiveInfoFromServer();
    			} else if (player == PLAYER2) {
    				receiveInfoFromServer();
    				waitForPlayerAction();
    				sendMove();
    			}
    		}
    	}catch(Exception e) {
    		e.printStackTrace();
    	}
    }).start();
    }
    
    private void waitForPlayerAction() throws InterruptedException {
    	while (waiting) {
    		Thread.sleep(100);
    	}
    	waiting = true;
    }
    
    private void sendMove() throws IOException {
    	toServer.writeInt(xSelected);
    	toServer.writeInt(ySelected);
    }
    
    private void receiveInfoFromServer() throws IOException {
        
        int status = fromServer.readInt();

        if (status == PLAYER1_WON) {
        	continuePlaying = false;
          if (myToken == 'X') {
            Platform.runLater(() -> lblStatus.setText("You are the winner!"));
          }
          else if (myToken == 'O') {
            Platform.runLater(() -> 
              lblStatus.setText("Player 1 (X) has won!"));
            receiveMove();
          }
        }
        else if (status == PLAYER2_WON) {
         
        	continuePlaying = false;
          if (myToken == 'O') {
            Platform.runLater(() -> lblStatus.setText("You are the winner"));
          }
          else if (myToken == 'X') {
            Platform.runLater(() -> 
              lblStatus.setText("Player 2 (O) has won!"));
            receiveMove();
          }
        }
        else if (status == DRAW) {
       
          continuePlaying = false;
          Platform.runLater(() -> 
            lblStatus.setText("Draw! Game is over."));

          if (myToken == 'O') {
            receiveMove();
          }
        }
        else {
          receiveMove();
          Platform.runLater(() -> lblStatus.setText("Your turn"));
          myTurn = true; 
        }
      }
    
	private void receiveMove() throws IOException {
		int x = fromServer.readInt();
		int y = fromServer.readInt();
		Platform.runLater(() -> cell[x][y].setToken(otherToken));
	}

    /** Determine if the game has resulted in a tie
     * 
     * */
    public boolean isFull()
    {
        for (int i = 0; i < 6; i++)
            for (int j = 0; j < 7; j++)
                if (cell[i][j].getToken() == ' ')
                    return false;

        return true;
    }
    /** 
     * Finds if a winner has been decided. 
     * @param token player who's moves are being checked for a win
     * @return Returns whether or not there is a winner
     */

    public boolean isWon(char token)
    {
        boolean isWinner = false;

        for (int[] d : directions) {
            int xdirection = d[0];
            int ydirection = d[1];
            for (int x = 0; x < 6; x++) {
                for (int y = 0; y < 7; y++) {
                    int finalx = x + 3*xdirection;
                    int finaly = y + 3*ydirection;
                    if (0 <= finalx && finalx < 6 && 0 <= finaly && finaly < 7) {
                        char w = cell[x][y].getToken();
                        if (w != ' ' && w == cell[x+xdirection][y+ydirection].getToken()
                                && w == cell[x+2*xdirection][y+2*ydirection].getToken()
                                && w == cell[finalx][finaly].getToken()) {
                            isWinner = true;
                            break;
                        }
                    }
                }
            }
        }
            return isWinner;
        
    }

    /**
     * Inner class for composing GUI connect4 board.
     *
     *
     */
    public class Cell extends Pane
    {
       
    	private int xPosition;
    	private int yPosition;
        private char token = ' ';
        public Cell next;
        /*
         * Basic constructor
         */
        public Cell()
        {
            setStyle("-fx-border-color: black");
            this.setPrefSize(2000, 2000);
            this.setOnMouseClicked(e -> handleMouseClick());
        }
        /**
         * Constructor that also passes board position information
         * @param x x coordinate on game board
         * @param y y coordinate on game board
         */
        public Cell(int x, int y)
        {
        	this.xPosition = x;
        	this.yPosition = y;
            setStyle("-fx-border-color: black");
            this.setPrefSize(2000, 2000);
            this.setOnMouseClicked(e -> handleMouseClick());               
        }
        /**
         * Constructor that passes coordinate info and links cell to next cell
         * @param x x coordinate
         * @param y y coordinate
         * @param next linked to the cell below on game board
         */
        public Cell(int x, int y, Cell next) 
        {
        	this.xPosition = x;
        	this.yPosition = y;
        	this.next = next;
            setStyle("-fx-border-color: black");
            this.setPrefSize(2000, 2000);
            this.setOnMouseClicked(e -> handleMouseClick());
        }
        /**
         * Gets player token
         * @return current player's token
         */
        public char getToken() {
            return token;
        }
        /**
         * Sets player token onto board
         * @param c the players token that is being placed
         */
        public void setToken(char c)
        {
            token = c;
            if (token == 'X')
            {
                Line line1 = new Line(10, 10, this.getWidth() - 10, this.getHeight() -
                        10);
                line1.endXProperty().bind(this.widthProperty().subtract(10));
                line1.endYProperty().bind(this.heightProperty().subtract(10));
                Line line2 = new Line(10, this.getHeight() - 10, this.getWidth() - 10,
                        10);
                line2.startYProperty().bind(this.heightProperty().subtract(10));
                line2.endXProperty().bind(this.widthProperty().subtract(10));
                // Add the lines to the pane
                this.getChildren().addAll(line1, line2);
            }
            else if (token == 'O') {
                Ellipse ellipse = new Ellipse(this.getWidth() / 2,
                        this.getHeight() / 2, this.getWidth() / 2 - 10,
                        this.getHeight() / 2 - 10);
                ellipse.centerXProperty().bind(this.widthProperty().divide(2));
                ellipse.centerYProperty().bind(this.heightProperty().divide(2));

                ellipse.radiusXProperty().bind(this.widthProperty().divide(2).subtract(10));

                ellipse.radiusYProperty().bind(this.heightProperty().divide(2).subtract(10));
                ellipse.setStroke(Color.BLACK);
                ellipse.setFill(Color.WHITE);
                getChildren().add(ellipse); // Add the ellipse to the pane
            }
        }
        
        /**
         * Handles player mouse input for placing a piece only in legal spots
         */
        private void handleMouseClick()
        {
            if (token == ' ' && myTurn)
            {
            	int x = 0;
            	if(next.getToken() == ' ' && xPosition == 5) {
            		setToken(myToken);
            		myTurn = false;
                    xSelected = xPosition;
                    ySelected = yPosition;
                    lblStatus.setText("Waiting for the other player to move");
                    waiting = false; 
            		x =1;
            	}
            	else if (next.getToken() != ' ' && xPosition < 5) {
                    setToken(myToken); 
                    myTurn = false;
                    xSelected = xPosition;
                    ySelected = yPosition;
                    lblStatus.setText("Waiting for the other player to move");
                    waiting = false; 
                    x=1;
            	}
            	else if (next.getToken() == ' ' && xPosition <5 && !isWon(myToken) && myTurn) {
            		lblStatus.setText("Player " + myToken + " please select legal position");
            	}

            } else if(!isWon(myToken) && myTurn) {
            	lblStatus.setText("Player " + myToken + " please select legal position");
            }
        }
    }
    public static void main(String[] args) {
        launch(args);
      }
}
