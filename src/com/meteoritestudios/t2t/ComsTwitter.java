package com.meteoritestudios.t2t;

import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.TwitterException;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ChatAction;
import com.pengrad.telegrambot.request.SendChatAction;
import com.pengrad.telegrambot.request.SendMessage;

public class ComsTwitter extends ComsBase {

	public ComsTwitter() {
		super("/twitter");
	}

	@Override
	public void run(Update update) {
		TelegramBot _bot = TelegramInstance.getBot();
		if (update.message().text().startsWith(key + " ")) {
			String toSend = update.message().text().substring(key.length() + 1);
			try {
				_bot.execute(new SendChatAction(update.message().chat().id(),
						ChatAction.typing.name()));
				StatusUpdate su = new StatusUpdate(toSend);
				if (update.message().replyToMessage() != null) {
					String replyText = update.message().replyToMessage().text();
					if (replyText.contains("/status/")) {
						try {
							long rsid = Long.parseLong(replyText
									.substring(replyText.lastIndexOf('/') + 1));
							Status rstatus = TwitterInstance
									.getTwitterInstance().showStatus(rsid);
							String[] rstext = rstatus.getText().split(" ");
							for (String word : rstext) {
								if (word.startsWith("@")
										&& !word.equalsIgnoreCase("@"
												+ AppConfig.TWITTER_NICK)) {
									toSend = word + " " + toSend;
								}
							}
							if (!toSend.contains("@"
									+ rstatus.getUser().getScreenName())) {
								toSend = "@"
										+ rstatus.getUser().getScreenName()
										+ " " + toSend;
							}
							su = new StatusUpdate(toSend);
							su.setInReplyToStatusId(rsid);
						} catch (NumberFormatException e) {
							e.printStackTrace();
						}
					}
				}
				Status status = TwitterInstance.getTwitterInstance()
						.updateStatus(su);
				_bot.execute(new SendMessage(
						update.message().chat().id(),
						"Se ha publicado el mensaje:\n"
								+ status.getText()
								+ (su.getInReplyToStatusId() > -1 ? "\nEn respuesta a otro Tweet"
										: "")).replyToMessageId(update
						.message().messageId()));
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
					"/twitter <Tu mensaje de hasta 140 caracteres>")
					.replyToMessageId(update.message().messageId()));
		}
	}

	@Override
	public String getHelp() {
		return "/twitter <Texto de 140 caracteres como max> | Se usa para enviar un tweet via @Ismaw34";
	}

	@Override
	public String getToFather() {
		return "twitter - Se usa para enviar un tweet via @Ismaw34";
	}

}
