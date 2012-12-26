package de.maikmerten.chainreaction.swing;

import de.maikmerten.chainreaction.Player;

import java.awt.*;

import static de.maikmerten.chainreaction.swing.UIField.CELL_SIZE;

public class UICellBG implements UIDrawable {
	private static final int DELAY = 50;
	private final int animCount = 50, brightness = 75, fadeCount = 20;

	private int animCounter = 0, fadeCounter = 0;
	private long lastAnim = System.currentTimeMillis();
	private boolean raise = true;
	private Player player;
	private Player oldPlayer;

	public UICellBG(final Player player) {
		this.player = player;
	}
	
	public void changeOwner(final Player player) {
		if(this.player == player) {
			return;
		}
		oldPlayer = this.player;
		this.player = player;
		this.fadeCounter = 0;
	}

	@Override
	public void draw(Graphics2D g2d) {
		final long now = System.currentTimeMillis();
		Color color;
		if(oldPlayer != null && fadeCounter == fadeCount) {
			oldPlayer = null;
		}
		if(oldPlayer != null) {
			final Color oldColor = UIPlayer.getPlayer(oldPlayer).getBackground();
			final Color newColor = UIPlayer.getPlayer(player).getBackground();
			color = new Color(
					calcCurrFadeColor(oldColor.getRed(), newColor.getRed()),
					calcCurrFadeColor(oldColor.getGreen(), newColor.getGreen()),
					calcCurrFadeColor(oldColor.getBlue(), newColor.getBlue()));
		}
		else {
			color = UIPlayer.getPlayer(player).getBackground();
		}

		
		if((now - lastAnim) > DELAY) {
			final int anims = (int)(now - lastAnim)/DELAY;
			if(oldPlayer != null) {
				fadeCounter = (fadeCounter + anims);
				if(fadeCounter > fadeCount) {
					fadeCounter = fadeCount;
				}
			}
			animCounter = (animCounter + (raise ? anims : - anims)) % animCount;
			animCounter = animCounter < 0 ? 0 : animCounter;
			if(player.equals(Player.NONE)) {
				raise = false;
			}
			else if(raise ? animCounter == (animCount-1) : animCounter == 0) {
				raise = !raise;
			}
			lastAnim = now;
		}
		final Color oldColor = g2d.getColor();

		float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), new float[3]);
		hsb[2] += (animCounter*((float)brightness/animCount)/255);
		g2d.setColor(new Color(Color.HSBtoRGB(hsb[0], hsb[1], hsb[2])));
		
		g2d.fillRect(0, 0, (CELL_SIZE*2), (CELL_SIZE*2));
		
		g2d.setColor(oldColor);
	}
	
	private int calcCurrFadeColor(final int oldCanal, final int newCanal) {
		return oldCanal + (fadeCounter * (newCanal - oldCanal)) / (fadeCount-1);
		
	}
}
