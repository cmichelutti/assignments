package control;

import model.Game;
import model.HaskellBot;
import model.User;
import model.ploy.PloyGame;

public class GameFactory {
	
	//TODO: change path to bot executable if desired
	public static final String PLOY_BOT_PATH = "";
	public static final String PLOY_BOT_COMMAND = "Bot";

	
	public static Game createGame(String gameType) {
		try {
			switch(gameType) {
				case "ploy": return new PloyGame();
			}
		} catch (Exception e)  {
			e.printStackTrace();
		}
		
		return null;
	}

	public static User createBot(String type, Game game) {
		switch(type) {
			case "haskell": 
				switch(game.getClass().getName().substring(game.getClass().getName().lastIndexOf(".")+1)) {
					case "PloyGame": return new HaskellBot(game, PLOY_BOT_PATH, PLOY_BOT_COMMAND);
					default: return null;
				}
			default: return null;
		}
	}
	
}
