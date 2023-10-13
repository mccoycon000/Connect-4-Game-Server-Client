/**
 * constants shared between server and client programs
 * @author Connor McCoy
 *
 */
public interface Connect4Constants {
  public static int PLAYER1 = 1; 
  public static int PLAYER2 = 2; 
  public static int PLAYER1_WON = 1; 
  public static int PLAYER2_WON = 2; 
  public static int DRAW = 3; 
  public static int CONTINUE = 4; 
  public static int DIRECTIONS[][] = {
          {1,0}, {1,-1}, {1,1}, {0,1}
};
}