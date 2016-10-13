package com.meteoritestudios.t2t;

import twitter4j.TwitterException;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.AnswerCallbackQuery;

public class QueryLike extends QueryBase {

	public QueryLike() {
		super("like");
	}

	@Override
	public void run(Update update) {
		TelegramBot _bot = TelegramInstance.getBot();
		try {
			TwitterInstance.getTwitterInstance().createFavorite(
					Long.parseLong(update.callbackQuery().data()
							.substring(key.length())));
			_bot.execute(new AnswerCallbackQuery(update.callbackQuery().id())
					.text(AppConfig.TWITTER_ICONS[AppConfig.TWITTER_LIKE]
							+ " OK"));
		} catch (NumberFormatException e) {
		} catch (TwitterException e) {
			e.printStackTrace();
			_bot.execute(new AnswerCallbackQuery(update.callbackQuery().id())
					.text(AppConfig.TWITTER_ICONS[AppConfig.TWITTER_LIKE]
							+ " ERROR\n" + e.getMessage()));
		}
	}

	@Override
	public String getHelp() {
		return null;
	}

	@Override
	public String getToFather() {
		return null;
	}

}
