package com.meteoritestudios.t2t;

import twitter4j.TwitterException;
import twitter4j.User;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ChatAction;
import com.pengrad.telegrambot.request.SendChatAction;
import com.pengrad.telegrambot.request.SendMessage;

public class ComsUnfollow extends ComsBase {

	public ComsUnfollow() {
		super("/unfollow");
	}

	@Override
	public void run(Update update) {
		TelegramBot _bot = TelegramInstance.getBot();
		if (update.message().text().startsWith(key + " ")) {
			String toSend = update.message().text().substring(key.length() + 1);
			try {
				_bot.execute(new SendChatAction(update.message().chat().id(),
						ChatAction.typing.name()));
				User followed = TwitterInstance.getTwitterInstance()
						.destroyFriendship(toSend);
				_bot.execute(new SendMessage(update.message().chat().id(),
						"Se ha des-seguido al usuario:\n"
								+ followed.getScreenName())
						.replyToMessageId(update.message().messageId()));
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
					"/unfollow <El usuario a des-seguir>")
					.replyToMessageId(update.message().messageId()));
		}
	}

	@Override
	public String getHelp() {
		return "/unfollow <User name> | It is used to unfollow a user";
	}

	@Override
	public String getToFather() {
		return "unfollow - Unfollow a user";
	}

}
