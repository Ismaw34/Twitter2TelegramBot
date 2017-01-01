package com.meteoritestudios.t2t;

import java.util.ArrayList;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.AnswerCallbackQuery;
import com.pengrad.telegrambot.request.EditMessageText;

public class QueryUnLike extends QueryBase {

	public QueryUnLike() {
		super("unlike");
	}

	@Override
	public void run(Update update) {
		TelegramBot _bot = TelegramInstance.getBot();
		Twitter _ti = TwitterInstance.getTwitterInstance();
		try {
			long twitter_status_id = Long.parseLong(update.callbackQuery()
					.data().substring(key.length()));
			_ti.destroyFavorite(twitter_status_id);
			_bot.execute(new AnswerCallbackQuery(update.callbackQuery().id())
					.text(AppConfig.TWITTER_ICONS[AppConfig.TWITTER_UNLIKE]
							+ " OK"));
			Status status = _ti.showStatus(twitter_status_id);
			ArrayList<InlineKeyboardButton> al_ikb = new ArrayList<InlineKeyboardButton>();
			if (status.isRetweetedByMe()) {
				al_ikb.add(new InlineKeyboardButton(
						AppConfig.TWITTER_ICONS[AppConfig.TWITTER_UNRETWEET])
						.callbackData("unretweet" + twitter_status_id));
			} else {
				al_ikb.add(new InlineKeyboardButton(
						AppConfig.TWITTER_ICONS[AppConfig.TWITTER_RETWEET])
						.callbackData("retweet" + twitter_status_id));
			}
			if (status.isFavorited()) {
				al_ikb.add(new InlineKeyboardButton(
						AppConfig.TWITTER_ICONS[AppConfig.TWITTER_UNLIKE])
						.callbackData("unlike" + twitter_status_id));
			} else {
				al_ikb.add(new InlineKeyboardButton(
						AppConfig.TWITTER_ICONS[AppConfig.TWITTER_LIKE])
						.callbackData("like" + twitter_status_id));
			}
			InlineKeyboardMarkup markup = new InlineKeyboardMarkup(
					al_ikb.toArray(new InlineKeyboardButton[al_ikb.size()]));
			EditMessageText editMessageText = new EditMessageText(
					AppConfig.TELEGRAM_SELF_ID, update.callbackQuery()
							.message().messageId(), update.callbackQuery()
							.message().text()).replyMarkup(markup);
			_bot.execute(editMessageText);
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
