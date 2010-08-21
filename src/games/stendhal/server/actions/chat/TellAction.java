package games.stendhal.server.actions.chat;

import static games.stendhal.common.constants.Actions.TARGET;
import static games.stendhal.common.constants.Actions.TEXT;
import games.stendhal.common.Grammar;
import games.stendhal.server.actions.ActionListener;
import games.stendhal.server.actions.admin.AdministrationAction;
import games.stendhal.server.core.engine.GameEvent;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.player.GagManager;
import games.stendhal.server.entity.player.Jail;
import games.stendhal.server.entity.player.Player;
import marauroa.common.game.RPAction;

/**
 * handles /tell-action (/msg-action). 
 */
public class TellAction implements ActionListener {
	private String text;
	private String senderName;
	private String receiverName;
	private Player sender;
	private Player receiver;

	private void init(final Player player, final RPAction action) {
		text = action.get(TEXT).trim();
		senderName = player.getName();
		receiverName = action.get(TARGET);
		sender = player;
		receiver = SingletonRepository.getRuleProcessor().getPlayer(receiverName);
	}

	private boolean validateAction(final RPAction action) {
		return action.has(TARGET) && action.has(TEXT);
	}

	private boolean checkOnline() {
		if ((receiver == null)
				|| (receiver.isGhost() && (sender.getAdminLevel() < AdministrationAction.getLevelForCommand("ghostmode")))) {
			sender.sendPrivateText("No player named \"" + receiverName + "\" is currently active.");
			return false;
		}
		return true;
	}

	private boolean checkIgnoreList(final Player player) {
		// check ignore list
		final String reply = receiver.getIgnore(senderName);
		if (reply != null) {
			// sender is on ignore list
			if (reply.length() == 0) {
				tellIgnorePostman(player, Grammar.suffix_s(receiverName)
					+ " mind is not attuned to yours, so you cannot reach them.");
			} else {
				tellIgnorePostman(player, receiverName + " is ignoring you: " + reply);
			}
			return false;
		}
		return true;
	}

	private String createFullMessageText() {
		if (senderName.equals(receiverName)) {
			return "You mutter to yourself: " + text;
		} else {
			return senderName + " tells you: " + text;
		}
	}

	private boolean checkAway() {
		// Handle /away messages
		final String away = receiver.getAwayMessage();
		if (away != null) {
			// Send away response
			tellIgnorePostman(sender, "Please use postman to send a message to " + receiverName + ", who is away: " + away);
			return false;
		} 
		return true;
	}

	private void tellIgnorePostman(final Player receiver, final String message) {
		if (!receiver.getName().equals("postman")) {
			receiver.sendPrivateText(message);
		}
	}

	public void onAction(final Player player, final RPAction action) {
		if (!player.getChatBucket().checkAndAdd()) {
			return;
		}

		if (GagManager.checkIsGaggedAndInformPlayer(player)) {
			return;
		}

		if (Jail.isInJail(player)) {
			player.sendPrivateText("The strong anti telepathy aura prevents you from getting through. Use /support <text> to contact an admin!");
			return;
		}

		if (!validateAction(action)) {
			return;
		}

		init(player, action);

		/* If the receiver is not logged in or if it is a ghost 
		 * and you don't have the level to see ghosts... */
		if (!checkOnline()) {
			return;
		}

		final String message = createFullMessageText();

		if (!checkIgnoreList(player)) {
			return;
		}

		// check grumpiness
		if (!checkGrumpy()) {
			return;
		}

		// check away
		if (!checkAway()) {
			return;
		}

		// transmit the message
		receiver.sendPrivateText(message);

		if (!senderName.equals(receiverName)) {
			player.sendPrivateText("You tell " + receiverName + ": " + text);
		}

		receiver.setLastPrivateChatter(senderName);
		new GameEvent(player.getName(), "chat", receiverName, Integer.toString(text.length()), text.substring(0, Math.min(text.length(), 1000))).raise();
	}

	private boolean checkGrumpy() {
		final String grumpy = receiver.getGrumpyMessage();
		if (grumpy != null) {
			boolean senderFound = false;
			// new way: check in buddies map if sender is buddy
			if(receiver.containsKey("buddies", senderName)) {
				senderFound = true;
			}
			if (!senderFound) {
				// sender is not a buddy
				if (grumpy.length() == 0) {
					tellIgnorePostman(sender, 
						receiverName + " has a closed mind, and is seeking solitude from all but close friends");
				} else {
					tellIgnorePostman(sender, 
						receiverName + " is seeking solitude from all but close friends: " + grumpy);
				}
				return false;
			}
		}

		return true;
	}

}
