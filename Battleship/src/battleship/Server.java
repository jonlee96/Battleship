package battleship;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class Server {
	
	// Grid board
	private Player[][] p1board = new Player[10][10];
	private Player[][] p2board = new Player[10][10];
	
	Player currentPlayer;
	public int p1Life = 1700;
	public int p2Life = 1700;
	
	public boolean winnerChickenDinner() {
		return (p1Life <= 0 || p2Life <= 0);
	}
	
	public boolean shipHit(int xloc, int yloc, Player player) {
		boolean hit = false;
		
		if (player.playerNum == '2') {
			for (int i = 0; i < p1board.length; i++) {
				for (int j = 0; j < p1board.length; j++) {
					if (p1board[xloc][yloc] != null) {
						hit = true;
						p1Life--;
					} else { hit = false; }
				}
			}
		}
		else if (player.playerNum == '1') {
			for (int i = 0; i < p2board.length; i++) {
				for (int j = 0; j < p2board.length; j++) {
					if (p2board[xloc][yloc] != null) {
						hit = true;
						p2Life--;
					} else { hit = false; }
				}
			}
		}
		return(hit);
	}
	
	
	public synchronized void move(int xloc, int yloc, Player player) {
        if (player != currentPlayer) {
        	System.out.println("Not Player " + player.playerNum + "'s turn");
            throw new IllegalStateException("Not your turn");
        } 
//        else if (p1board[xloc][yloc] != currentPlayer) {
//        	System.out.println("Player " + player.playerNum + " has already destroyed this square");
//            throw new IllegalStateException("Already attacked this square");
//        } 
//        else if (board[xloc][yloc] != null) {
//        	System.out.println("Cell already occupied");
//            throw new IllegalStateException("Cell already occupied");
//        }
        //board[xloc][yloc] = currentPlayer;
        currentPlayer = currentPlayer.opponent;
    }
	
	public synchronized void add(int xloc, int yloc, int shipNum, Player player) {
		if (player.opponent == null) {
        	System.out.println("Player " + player.playerNum + " doesn't have an opponent yet");
            throw new IllegalStateException("You don't have an opponent yet");
        } 
		else if (player.playerNum == '1') {
				if (shipNum == 0) {
					for(int i = 0; i <= 4; i++) {
						if ((p1board[xloc][yloc+i] != null) || (yloc > 5))
							throw new IllegalStateException("Ship already exist in here or out of bounds");
						p1board[xloc][yloc+ i] = currentPlayer;
						
					}
				}
				else if (shipNum == 1) {
					for(int i = 0; i <= 3; i++) {
						if (p1board[xloc][yloc+i] != null || (yloc > 6))
							throw new IllegalStateException("Ship already exist in here or out of bounds");
						p1board[xloc][yloc+ i] = currentPlayer;
					}
				}
				else if (shipNum == 2) {
					for(int i = 0; i <= 2; i++) {
						if (p1board[xloc][yloc+i] != null || (yloc > 7))
							throw new IllegalStateException("Ship already exist in here or out of bounds");
						p1board[xloc][yloc+ i] = currentPlayer;
					}
				}
				else if (shipNum == 3) {
					for(int i = 0; i <= 2; i++) {
						if (p1board[xloc][yloc+i] != null || (yloc > 7))
							throw new IllegalStateException("Ship already exist in here or out of bounds");
						p1board[xloc][yloc+ i] = currentPlayer;
					}
				}
				else if (shipNum == 4) {
					for(int i = 0; i <= 1; i++) {
						if (p1board[xloc][yloc+i] != null || (yloc > 8))
							throw new IllegalStateException("Ship already exist in here or out of bounds");
						p1board[xloc][yloc+ i] = currentPlayer;
					}
					player.output.println("MESSAGE Waiting for other player to finish placement");
				}
		}
		else if (player.playerNum == '2') {
			if (shipNum == 0) {
				for(int i = 0; i <= 4; i++) {
					if (p2board[xloc][yloc+i] != null || (yloc > 5))
						throw new IllegalStateException("Ship already exist in here or out of bounds");
					p2board[xloc][yloc+ i] = currentPlayer;
					
				}
			}
			else if (shipNum == 1) {
				for(int i = 0; i <= 3; i++) {
					if (p2board[xloc][yloc+i] != null|| (yloc > 6))
						throw new IllegalStateException("Ship already exist in here or out of bounds");
					p2board[xloc][yloc+ i] = currentPlayer;
				}
			}
			else if (shipNum == 2) {
				for(int i = 0; i <= 2; i++) {
					if (p2board[xloc][yloc+i] != null || (yloc > 7))
						throw new IllegalStateException("Ship already exist in here or out of bounds");
					p2board[xloc][yloc+ i] = currentPlayer;
				}
			}
			else if (shipNum == 3) {
				for(int i = 0; i <= 2; i++) {
					if (p2board[xloc][yloc+i] != null || (yloc > 7))
						throw new IllegalStateException("Ship already exist in here or out of bounds");
					p2board[xloc][yloc+ i] = currentPlayer;
				}
			}
			else if (shipNum == 4) {
				for(int i = 0; i <= 1; i++) {
					if (p2board[xloc][yloc+i] != null || (yloc > 8))
						throw new IllegalStateException("Ship already exist in here or out of bounds");
					p2board[xloc][yloc+ i] = currentPlayer;
				}
				player.output.println("MESSAGE Waiting for other player to finish placement");
				player.opponent.output.println("MESSAGE Opponent finished placement, attack when ready!");
			}
		}
        
    }
	
	class Player implements Runnable {
		char playerNum;
		Player opponent;
		Socket socket;
		Scanner input;
		PrintWriter output;
		
		public Player(Socket socket, char playerNum) {
			this.socket = socket;
			this.playerNum = playerNum;
		}
		
		@Override
		public void run() {
			try {
				setup();
				processCommands();
			} catch (Exception e) {
				System.out.println("Threading Error: " + e);
			} finally {
				if (opponent != null && opponent.output != null) {
					System.out.println("Player " + opponent.playerNum + " left the game");
					opponent.output.println("OTHER_PLAYER_LEFT");
				}
				try {
					socket.close();
				} catch (IOException e) {
					
				}
			}
		}
		
		private void setup() throws IOException {
			input = new Scanner(socket.getInputStream());
			output = new PrintWriter(socket.getOutputStream(), true);
			output.println("WELCOME " + playerNum);
			
			if (playerNum == '1') {
				currentPlayer = this;
				output.println("MESSAGE Waiting for oppnent to connect");
			}
			else {
				opponent = currentPlayer;
				opponent.opponent = this;
				opponent.output.println("MESSAGE Add your carrier");
			}
			
		}
		
		private void processCommands() {
            while (input.hasNextLine()) {
                String command = input.nextLine();
                if (command.startsWith("QUIT")) {
                    return;
                } 
                else if (command.startsWith("ADD")) {
                	processAddCommand(Integer.parseInt(command.substring(4,5)), Integer.parseInt(command.substring(5,6)), Integer.parseInt(command.substring(6,7)));
                }
                else if (command.startsWith("MOVE")) {
                    processMoveCommand(Integer.parseInt(command.substring(5,6)), Integer.parseInt(command.substring(6,7)));
                }
            }
        }
		
		private void processMoveCommand(int xloc, int yloc) {
            try {
                move(xloc, yloc, this);
                System.out.println("P1 Health: " + p1Life + " P2 Health: " + p2Life);
                output.println("VALID_MOVE");
                System.out.println("Valid move at coordinates (" + xloc + "," + yloc + ")");
                opponent.output.println("OPPONENT_MOVED " + xloc + yloc);
                
                if (shipHit(xloc, yloc, this)) {
                	System.out.println(playerNum + " scored a hit!");
                	output.println("HIT " + xloc + yloc);
                	opponent.output.println("OPPONENT_HIT " + xloc + yloc);
                }
                if (winnerChickenDinner()) {
                    output.println("VICTORY");
                    opponent.output.println("DEFEAT");
                }
            } catch (IllegalStateException e) {
                output.println("MESSAGE " + e.getMessage());
            }
        }
		
		private void processAddCommand(int xloc, int yloc, int shipNum) {
			try {
				add(xloc, yloc, shipNum, this);
				output.println("SHIP_ADDED " + xloc + yloc + shipNum);
				
			} catch (IllegalStateException e) {
				output.println("MESSAGE " + e.getMessage());
			} 
		}
		
	}
}