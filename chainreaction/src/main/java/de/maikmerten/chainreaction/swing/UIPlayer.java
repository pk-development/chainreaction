package de.maikmerten.chainreaction.swing;

import de.maikmerten.chainreaction.Player;

import java.awt.*;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import java.util.Properties;

public class UIPlayer {
	private final static Properties props;
	private final static Map<Player, UIPlayer> players;
	private final static Map<Player, Color> bgColors;
	private final static Map<Player, Color> fgColors;
	
	static {
		props = new Properties();
		try {
			props.load(UIPlayer.class.getResourceAsStream("/player.properties"));
		}
		catch (IOException e) {
			System.err.println("could not load player properties");
		}
		players = new EnumMap<Player, UIPlayer>(Player.class);
		
		bgColors = new EnumMap<Player, Color>(Player.class);
		bgColors.put(Player.FIRST, new Color(21, 39, 99));
		bgColors.put(Player.SECOND, new Color(120, 0, 0));
		bgColors.put(Player.NONE, Color.BLACK);
		
		fgColors = new EnumMap<Player, Color>(Player.class);
		fgColors.put(Player.FIRST, new Color(111, 129, 189));
		fgColors.put(Player.SECOND, new Color(210, 90, 90));
		fgColors.put(Player.NONE, Color.WHITE);
	}
	
	private final Player player;
	
	private UIPlayer(Player player) {
		this.player = player;
	}
	
	public Player getPlayer() {
		return this.player;
	}
	
	public UIAtom createAtom() {
		switch(player) {
			case FIRST:
			case SECOND:
				return new UIAtom(props.getProperty("player." + player.name()) + "atom.properties");
			case NONE:
				throw new UnsupportedOperationException("You cannot create an atom for no player");
			default:
				throw new IllegalStateException("This should never happen");
		}
	}
	
	public Color getBackground() {
		return bgColors.get(player);
	}
	
	public Color getForeground() {
		return fgColors.get(player);
	}
	
	public static UIPlayer getPlayer(Player player) {
		if(!players.containsKey(player)) {
			players.put(player, new UIPlayer(player));
		}
		return players.get(player);
	}
}
