package model.ploy;

import java.io.Serializable;

import model.Game;
import model.Player;

/**
 * Class Cannon extends the abstract class Game as a concrete game instance that
 * allows to play Cannon.
 *
 */
public class PloyGame extends Game implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5424778147226994452L;

	// Define the board
    private String[][] board;

	/************************
	 * member
	 ***********************/

	// just for better comprehensibility of the code: assign white and black player
	private Player blackPlayer;
	private Player whitePlayer;

	// internal representation of the game state
	// : insert additional game data here

	/************************
	 * constructors
	 ***********************/

	// Constructor
    public PloyGame() {
        super(); // Call the constructor of the superclass Game

        // Initialize the board as a 9x9 grid
        this.board = new String[9][9];

        // Initialize the board with the starting setup
        initializeBoard();
    }

	// Method to set up the initial state of the board
    private void initializeBoard() {
        // Fill the board with initial positions of the pieces
        // Using the string representation as described in the task
        // Example: board[0][0] = "w84"; // an example piece
        // Note: You'll need to fill in the correct initial positions as per the game's rules

        // Clear the board initially
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                board[i][j] = ""; // Empty square
            }
        }

        // Set up the initial positions of the pieces
        // Example: board[0][0] = "w84"; // This is just an example, replace with actual initial positions
        // You need to follow the game's rules to place each piece correctly
    }

	public String getType() {
		return "ploy";
	}

	/*******************************************
	 * Game class functions already implemented
	 ******************************************/

	@Override
	public boolean addPlayer(Player player) {
		if (!started) {
			players.add(player);

			// game starts with two players
			if (players.size() == 2) {
				started = true;
				this.blackPlayer= players.get(0);
				this.whitePlayer = players.get(1);
				nextPlayer = blackPlayer;
			}
			return true;
		}

		return false;
	}

	@Override
	public String getStatus() {
		if (error)
			return "Error";
		if (!started)
			return "Wait";
		if (!finished)
			return "Started";
		if (surrendered)
			return "Surrendered";
		if (draw)
			return "Draw";

		return "Finished";
	}

	@Override
	public String gameInfo() {
		String gameInfo = "";

		if (started) {
			if (blackGaveUp())
				gameInfo = "black gave up";
			else if (whiteGaveUp())
				gameInfo = "white gave up";
			else if (didWhiteDraw() && !didBlackDraw())
				gameInfo = "white called draw";
			else if (!didWhiteDraw() && didBlackDraw())
				gameInfo = "black called draw";
			else if (draw)
				gameInfo = "draw game";
			else if (finished)
				gameInfo = blackPlayer.isWinner() ? "black won" : "white won";
		}

		return gameInfo;
	}

	@Override
	public String nextPlayerString() {
		return isWhiteNext() ? "w" : "b";
	}

	@Override
	public int getMinPlayers() {
		return 2;
	}

	@Override
	public int getMaxPlayers() {
		return 2;
	}

	@Override
	public boolean callDraw(Player player) {

		// save to status: player wants to call draw
		if (this.started && !this.finished) {
			player.requestDraw();
		} else {
			return false;
		}

		// if both agreed on draw:
		// game is over
		if (players.stream().allMatch(p -> p.requestedDraw())) {
			this.draw = true;
			finish();
		}
		return true;
	}

	@Override
	public boolean giveUp(Player player) {
		if (started && !finished) {
			if (this.whitePlayer == player) {
				whitePlayer.surrender();
				blackPlayer.setWinner();
			}
			if (this.blackPlayer == player) {
				blackPlayer.surrender();
				whitePlayer.setWinner();
			}
			surrendered = true;
			finish();

			return true;
		}

		return false;
	}

	/*******************************************
	 * Helpful stuff
	 ******************************************/

	/**
	 * 
	 * @return True if it's white player's turn
	 */
	public boolean isWhiteNext() {
		return nextPlayer == whitePlayer;
	}

	/**
	 * Ends game after regular move (save winner, finish up game state,
	 * histories...)
	 * 
	 * @param player
	 * @return
	 */
	public boolean regularGameEnd(Player winner) {
		// public for tests
		if (finish()) {
			winner.setWinner();
			return true;
		}
		return false;
	}

	public boolean didWhiteDraw() {
		return whitePlayer.requestedDraw();
	}

	public boolean didBlackDraw() {
		return blackPlayer.requestedDraw();
	}

	public boolean whiteGaveUp() {
		return whitePlayer.surrendered();
	}

	public boolean blackGaveUp() {
		return blackPlayer.surrendered();
	}

	/*******************************************
	 * !!!!!!!!! To be implemented !!!!!!!!!!!!
	 ******************************************/

	@Override
	public void setBoard(String state) {
    	// Split the input string into rows
    	String[] rows = state.split("/");

    	// Reset the board before setting new state
    	this.board = new String[9][9];

    	// Iterate over each row and update the board
    	for (int i = 0; i < rows.length; i++) {
        	String[] squares = rows[i].split(",");
        	for (int j = 0; j < squares.length; j++) {
            	this.board[i][j] = squares[j].isEmpty() ? null : squares[j];
        	}
    	}
	}

	@Override
	public String getBoard() {
    	StringBuilder sb = new StringBuilder();

    	for (int i = 0; i < board.length; i++) {
        	for (int j = 0; j < board[i].length; j++) {
            	sb.append(board[i][j]);
            	if (j < board[i].length - 1) {
                	sb.append(",");
            	}
        	}
        	if (i < board.length - 1) {
            	sb.append("/");
        	}
    	}

    	return sb.toString();
	}

	@Override
	public boolean tryMove(String moveString, Player player) {
    	// Validate the move format
    	if (!isValidMoveFormat(moveString)) {
        	return false;
    	}

    	// Parse the move string
    	String[] parts = moveString.split("-");
    	String start = parts[0];
    	String destination = parts[1];
    	int rotation = Integer.parseInt(parts[2]);

    	// Check if it's the correct player's turn
    	if (!isPlayerTurn(player)) {
        	return false;
    	}

    	// Validate and execute the move
    	if (isValidMove(start, destination, rotation, player)) {
        	executeMove(start, destination, rotation);
        	updateGameState(player);
        	return true;
    	}

    	return false;
	}

	private boolean isValidMoveFormat(String moveString) {
		// Regex pattern to match the move format
		String pattern = "^[a-i][1-9]-[a-i][1-9]-[0-7]$";
	
		// Check if the moveString matches the pattern
		return moveString.matches(pattern);
	}

	private boolean isPlayerTurn(Player player) {
    	// Check if it's the right player's turn
    	return player.equals(nextPlayer);
	}

	private boolean isValidMove(String start, String destination, int rotation, Player player) {
		// Convert board positions (e.g., 'a1', 'b3') to array indices
		int[] startPos = convertPositionToArrayIndices(start);
		int[] destPos = convertPositionToArrayIndices(destination);
	
		// Check if the start position is valid
		if (!isPositionValid(startPos)) {
			return false;
		}
	
		// Check if there is a piece at the start position and if it belongs to the current player
		String piece = board[startPos[0]][startPos[1]];
		if (!isPieceOfCurrentPlayer(piece, player)) {
			return false;
		}
	
		// Check if the destination position is valid
		if (!isPositionValid(destPos)) {
			return false;
		}
	
		// Check if the move is valid for the piece type
		if (!isMoveValidForPiece(piece, startPos, destPos, rotation)) {
			return false;
		}
	
		// Check if the path to the destination is clear
		if (!isPathClear(startPos, destPos)) {
			return false;
		}
	
		return true;
	}
	
	private int[] convertPositionToArrayIndices(String position) {
		// The board is 9x9, with positions labeled from 'a9' to 'i1'
	
		// Extract the column (letter) and row (number) from the position
		char columnLetter = position.charAt(0);
		int rowNumber = Character.getNumericValue(position.charAt(1));
	
		// Convert the column letter to an index (0-8)
		// 'a' corresponds to index 0, 'b' to 1, ..., 'i' to 8
		int columnIndex = columnLetter - 'a';
	
		// Convert the row number to an index (0-8)
		// Note: '9' corresponds to index 0, '8' to 1, ..., '1' to 8
		int rowIndex = 9 - rowNumber;
	
		// Return the indices as an array
		return new int[]{rowIndex, columnIndex};
	}
	
	private boolean isPositionValid(int[] position) {
		// Check if the position array is not null and has exactly 2 elements (row and column)
		if (position == null || position.length != 2) {
			return false;
		}
	
		// Extract row and column indices
		int row = position[0];
		int col = position[1];
	
		// Check if the row and column indices are within the bounds of the board
		return row >= 0 && row < 9 && col >= 0 && col < 9;
	}
	
	private boolean isPieceOfCurrentPlayer(String piece, Player player) {
		// Check if the piece string is valid
		if (piece == null || piece.isEmpty()) {
			return false;
		}
	
		// Get the color of the piece (assuming the first character represents the color)
		char pieceColor = piece.charAt(0);
	
		// Determine the color of the current player based on the turn order
		char currentPlayerColor = getCurrentPlayerColor(player);
	
		// Check if the piece color matches the current player's color
		return pieceColor == currentPlayerColor;
	}
	
	private char getCurrentPlayerColor(Player player) {
		// Check if it's white player's turn
		if (!isWhiteNext()) {
			// If isWhiteNext() is false, it's white player's turn
			return 'w';
		} else {
			// Otherwise, it's black player's turn
			return 'b';
		}
	}
	
	private boolean isMoveValidForPiece(String piece, int[] startPos, int[] destPos, int rotation) {
		// First, check if the piece string is valid
		if (piece == null || piece.length() < 2) {
			return false;
		}
	
		// Extract the type of the piece from the string
		// Assuming that the characters after the first one represent the piece type and movement capabilities
		String pieceType = piece.substring(1);
	
		// Convert pieceType to a numerical value to interpret the movement capabilities
		int pieceValue;
		try {
			pieceValue = Integer.parseInt(pieceType);
		} catch (NumberFormatException e) {
			return false; // pieceType is not a valid number
		}
	
		// Check movement based on piece type
		// This is a simplified example. You will need to expand this logic based on the rules for each piece
		switch (piece.charAt(0)) {
			case 'S': // Shield
				return isValidShieldMove(startPos, destPos, rotation, pieceValue);
			case 'P': // Probe
				return isValidProbeMove(startPos, destPos, rotation, pieceValue);
			case 'L': // Lance
				return isValidLanceMove(startPos, destPos, rotation, pieceValue);
			case 'C': // Commander
				return isValidCommanderMove(startPos, destPos, rotation, pieceValue);
			default:
				return false; // Unknown piece type
		}
	}
	
	private boolean isValidShieldMove(int[] startPos, int[] destPos, int rotation, int pieceValue) {
		// Check if the movement is within one square
		if (!isMovementWithinRange(startPos, destPos, 1)) {
			return false;
		}
	
		// Check if the rotation is valid (0 or 1)
		if (rotation < 0 || rotation > 1) {
			return false;
		}
	
		// Check if the move is in a valid direction
		return isDirectionValid(startPos, destPos, pieceValue);
	}

	private boolean isValidProbeMove(int[] startPos, int[] destPos, int rotation, int pieceValue) {
		if (!isMovementWithinRange(startPos, destPos, 2)) {
			return false;
		}
	
		if (rotation < 0 || rotation > 1) {
			return false;
		}
	
		return isDirectionValid(startPos, destPos, pieceValue);
	}

	private boolean isValidLanceMove(int[] startPos, int[] destPos, int rotation, int pieceValue) {
		if (!isMovementWithinRange(startPos, destPos, 3)) {
			return false;
		}
	
		if (rotation < 0 || rotation > 1) {
			return false;
		}
	
		return isDirectionValid(startPos, destPos, pieceValue);
	}

	private boolean isValidCommanderMove(int[] startPos, int[] destPos, int rotation, int pieceValue) {
		if (!isMovementWithinRange(startPos, destPos, 1)) {
			return false;
		}
	
		if (rotation < 0 || rotation > 1) {
			return false;
		}
	
		return isDirectionValid(startPos, destPos, pieceValue);
	}

	private boolean isMovementWithinRange(int[] startPos, int[] destPos, int maxDistance) {
		return Math.abs(destPos[0] - startPos[0]) <= maxDistance && Math.abs(destPos[1] - startPos[1]) <= maxDistance;
	}
	
	private boolean isDirectionValid(int[] startPos, int[] destPos, int pieceValue) {
		int deltaX = destPos[0] - startPos[0];
		int deltaY = destPos[1] - startPos[1];
	
		// Calculate direction based on deltaX and deltaY
		int direction = getDirection(deltaX, deltaY);
	
		// Check if the piece can move in the calculated direction
		return (pieceValue & (1 << direction)) != 0;
	}
	
	private int getDirection(int deltaX, int deltaY) {
		// Normalize deltaX and deltaY to -1, 0, or 1 to represent direction
		int normDeltaX = (deltaX == 0) ? 0 : (deltaX > 0 ? 1 : -1);
		int normDeltaY = (deltaY == 0) ? 0 : (deltaY > 0 ? 1 : -1);
	
		// Determine the direction based on normalized deltas
		if (normDeltaX == 0 && normDeltaY < 0) return 0; // North
		if (normDeltaX > 0 && normDeltaY < 0) return 1; // Northeast
		if (normDeltaX > 0 && normDeltaY == 0) return 2; // East
		if (normDeltaX > 0 && normDeltaY > 0) return 3; // Southeast
		if (normDeltaX == 0 && normDeltaY > 0) return 4; // South
		if (normDeltaX < 0 && normDeltaY > 0) return 5; // Southwest
		if (normDeltaX < 0 && normDeltaY == 0) return 6; // West
		if (normDeltaX < 0 && normDeltaY < 0) return 7; // Northwest
	
		// If the direction doesn't match any of the above, return an invalid index
		return -1;
	}
	
	private boolean isPathClear(int[] startPos, int[] destPos) {
		int startX = startPos[0];
		int startY = startPos[1];
		int destX = destPos[0];
		int destY = destPos[1];
	
		int deltaX = destX - startX;
		int deltaY = destY - startY;
	
		// Check for horizontal movement
		if (deltaX == 0) {
			for (int y = Math.min(startY, destY) + 1; y < Math.max(startY, destY); y++) {
				if (!board[startX][y].equals("")) {
					return false; // Found a piece blocking the path
				}
			}
		}
		// Check for vertical movement
		else if (deltaY == 0) {
			for (int x = Math.min(startX, destX) + 1; x < Math.max(startX, destX); x++) {
				if (!board[x][startY].equals("")) {
					return false; // Found a piece blocking the path
				}
			}
		}
		// Check for diagonal movement
		else if (Math.abs(deltaX) == Math.abs(deltaY)) {
			int xStep = deltaX > 0 ? 1 : -1;
			int yStep = deltaY > 0 ? 1 : -1;
			for (int x = startX + xStep, y = startY + yStep; x != destX; x += xStep, y += yStep) {
				if (!board[x][y].equals("")) {
					return false; // Found a piece blocking the path
				}
			}
		}
		// If the move is not horizontal, vertical, or diagonal, it's invalid
		else {
			return false;
		}
	
		return true; // The path is clear
	}

	private void executeMove(String start, String destination, int rotation) {
		// Convert board positions to array indices
		int[] startPos = convertPositionToArrayIndices(start);
		int[] destPos = convertPositionToArrayIndices(destination);
	
		// Retrieve the piece from the start position
		String piece = board[startPos[0]][startPos[1]];
	
		// Apply rotation to the piece if needed
		if (rotation > 0) {
			piece = rotatePiece(piece, rotation);
		}
	
		// Move the piece to the destination position
		board[destPos[0]][destPos[1]] = piece;
	
		// Clear the start position
		board[startPos[0]][startPos[1]] = "";
	}
	
	private String rotatePiece(String piece, int rotation) {
		char color = piece.charAt(0);
		int pieceValue;
	
		try {
			pieceValue = Integer.parseInt(piece.substring(1));
		} catch (NumberFormatException e) {
			// Handle the exception if the piece value is not a number
			return piece; // Return the original piece string
		}
	
		// Ensure the rotation is within the range of 0-7
		rotation = rotation % 8;
	
		// Rotate the piece value
		// For clockwise rotation, shift bits to the left
		// For counterclockwise rotation, shift bits to the right
		if (rotation > 0) {
			pieceValue = (pieceValue << rotation) | (pieceValue >> (8 - rotation));
		} else if (rotation < 0) {
			rotation = -rotation; // Make the rotation positive for counterclockwise
			pieceValue = (pieceValue >> rotation) | (pieceValue << (8 - rotation));
		}
	
		// Ensure the piece value remains within 8 bits
		pieceValue = pieceValue & 0xFF;
	
		return color + Integer.toString(pieceValue);
	}

	private void updateGameState(Player player) {
		// Switch to the next player
		switchPlayer();
	
		// Check for game-end conditions
		if (isGameEnd()) {
			finishGame();
		}
	}
	
	private void switchPlayer() {
		// Assuming 'nextPlayer' is the Player variable representing who is next to play
		// And 'blackPlayer' and 'whitePlayer' are the two players of the game
		nextPlayer = (nextPlayer.equals(blackPlayer)) ? whitePlayer : blackPlayer;
	}
	
	private boolean isGameEnd() {
		// Check if the game has ended based on the commander's presence and other pieces
		return !isAnyPieceLeft(blackPlayer) || !isAnyPieceLeft(whitePlayer)
				|| isOnlyCommanderLeft(blackPlayer) || isOnlyCommanderLeft(whitePlayer);
	}
	
	private boolean isAnyPieceLeft(Player player) {
		char playerColor = getCurrentPlayerColor(player);
	
		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board[i].length; j++) {
				String piece = board[i][j];
				if (piece.startsWith(String.valueOf(playerColor))) {
					return true; // Found a piece belonging to the player
				}
			}
		}
		return false; // No pieces left for this player
	}
	
	private boolean isOnlyCommanderLeft(Player player) {
		char playerColor = getCurrentPlayerColor(player);
		boolean commanderFound = false;
	
		// Define the possible representations of the Commander piece
		String commanderValue1 = "170"; // 4 bits set in an 8-bit representation
		String commanderValue2 = "85";  // Alternative representation
	
		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board[i].length; j++) {
				String piece = board[i][j];
				if (piece.startsWith(String.valueOf(playerColor))) {
					String pieceValue = piece.substring(1);
					if (pieceValue.equals(commanderValue1) || pieceValue.equals(commanderValue2)) {
						// Found a Commander piece
						commanderFound = true;
					} else {
						// Found a non-commander piece
						return false;
					}
				}
			}
		}
	
		return commanderFound; // Return true if only the commander is left, false otherwise
	}
	
	private void finishGame() {
		// Mark the game as finished
		finished = true;
	
		// Determine the winner
		boolean blackHasCommander = isCommanderPresent(blackPlayer);
		boolean whiteHasCommander = isCommanderPresent(whitePlayer);
	
		if (blackHasCommander && !whiteHasCommander) {
			// Black player wins
			blackPlayer.setWinner();
		} else if (!blackHasCommander && whiteHasCommander) {
			// White player wins
			whitePlayer.setWinner();
		} else {
			// Additional conditions to determine the winner or draw
			// This could depend on the number of pieces left or other game-specific rules
		}
	
		// Perform any other finalizations needed for the game
		// For example, update player statistics, record the game result, etc.
	}
	
	private boolean isCommanderPresent(Player player) {
		char playerColor = getCurrentPlayerColor(player);

		// Define the possible representations of the Commander piece
		String commanderValue1 = "170"; // 4 bits set in an 8-bit representation
		String commanderValue2 = "85";  // Alternative representation
	
		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board[i].length; j++) {
				String piece = board[i][j];
				if (piece.startsWith(String.valueOf(playerColor)) && (piece.substring(1).equals(commanderValue1) || piece.substring(1).equals(commanderValue2))) {
					return true; // Commander found
				}
			}
		}
		return false; // Commander not found
	}
	
}
