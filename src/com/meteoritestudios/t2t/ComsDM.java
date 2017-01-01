package com.meteoritestudios.t2t;

import twitter4j.DirectMessage;
import twitter4j.TwitterException;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ChatAction;
import com.pengrad.telegrambot.request.SendChatAction;
import com.pengrad.telegrambot.request.SendMessage;

public class ComsDM extends ComsBase {

	public ComsDM() {
		super("/dm");
	}

	@Override
	public void run(Update update) {
		TelegramBot _bot = TelegramInstance.getBot();
		if (update.message().text().startsWith(key + " ")) {
			String toSend = update.message().text().substring(key.length() + 1);
			try {
				_bot.execute(new SendChatAction(update.message().chat().id(),
						ChatAction.typing.name()));
				if (update.message().replyToMessage() != null) {
					String replyText = update.message().replyToMessage().text();
					if (replyText.startsWith("DM from: ")) {
						String userToResponse = replyText.substring(
								"DM from: ".length(), replyText.indexOf("\n"));
						DirectMessage dm = TwitterInstance.getTwitterInstance()
								.sendDirectMessage(userToResponse, toSend);
						_bot.execute(new SendChatAction(update.message().chat()
								.id(), ChatAction.typing.name()));
						_bot.execute(new SendMessage(update.message().chat()
								.id(), "Sent a DM to user: "
								+ dm.getRecipientScreenName() + "\nWith text: "
								+ toSend).replyToMessageId(update.message()
								.messageId()));

					}
				} else {
					String[] userToSend = toSend.split(" ");
					if (userToSend.length > 1 && userToSend[0].startsWith("@")) {
						String userToDM = userToSend[0].substring(1);
						String textToSend = toSend.substring(toSend
								.indexOf(userToSend[0])
								+ userToSend[0].length() + 1);
						DirectMessage dm = TwitterInstance.getTwitterInstance()
								.sendDirectMessage(userToDM, textToSend);
						_bot.execute(new SendChatAction(update.message().chat()
								.id(), ChatAction.typing.name()));
						_bot.execute(new SendMessage(update.message().chat()
								.id(), "Sent a DM to user: "
								+ dm.getRecipientScreenName() + "\nWith text: "
								+ textToSend).replyToMessageId(update.message()
								.messageId()));
					} else {
						_bot.execute(new SendChatAction(update.message().chat()
								.id(), ChatAction.typing.name()));
						_bot.execute(new SendMessage(update.message().chat()
								.id(), "/dm <@User> <Texto>")
								.replyToMessageId(update.message().messageId()));
					}
				}
			} catch (TwitterException e) {
				_bot.execute(new SendChatAction(update.message().chat().id(),
						ChatAction.typing.name()));
				_bot.execute(new SendMessage(update.message().chat().id(),
						"Se ha producido un error:\n" + e.getMessage())
						.replyToMessageId(update.message().messageId()));
				e.printStackTrace();
			}
		} else {
			_bot.execute(new SendChatAction(update.message().chat().id(),
					ChatAction.typing.name()));
			_bot.execute(new SendMessage(update.message().chat().id(),
					"/dm <@User> <Texto>").replyToMessageId(update.message()
					.messageId()));
		}
	}

	@Override
	public String getHelp() {
		return "/dm <@Twitter> <Texto>";
	}

	@Override
	public String getToFather() {
		return "dm - Envia un mensaje directo a un usuario";
	}

}
