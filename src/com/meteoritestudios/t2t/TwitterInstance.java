package com.meteoritestudios.t2t;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

import twitter4j.DirectMessage;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.User;
import twitter4j.UserList;
import twitter4j.UserStreamListener;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ChatAction;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.SendChatAction;
import com.pengrad.telegrambot.request.SendMessage;

public class TwitterInstance extends ComsBase {

	private static Twitter twitter;
	private static TwitterStream twitter_s;
	private long lastID = 1l;

	TelegramBot ti;

	File twitter_last_id = new File("twitter_last_id.dat");

	public TwitterInstance(TelegramBot ti) {
		super("/fetch");
		this.ti = ti;
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true)
				.setOAuthConsumerKey(AppConfig.TWITTER_CONSUMER_KEY)
				.setOAuthConsumerSecret(AppConfig.TWITTER_CONSUMER_PRIVATE)
				.setOAuthAccessToken(AppConfig.TWITTER_ACCESS_TOKEN)
				.setOAuthAccessTokenSecret(
						AppConfig.TWITTER_ACCESS_TOKEN_PRIVATE);
		Configuration conf = cb.build();

		if (twitter_last_id.exists()) {
			Scanner sc = null;
			try {
				sc = new Scanner(twitter_last_id);
				if (sc.hasNextLine()) {
					try {
						lastID = Long.parseLong(sc.nextLine());
					} catch (NumberFormatException e) {
					}
				}
			} catch (FileNotFoundException e) {
			}
			try {
				sc.close();
			} catch (Exception e) {
			}
		}
		TwitterFactory tf = new TwitterFactory(conf);
		twitter = tf.getInstance();
		this.getLastestsTwits();
		TwitterStreamFactory tsf = new TwitterStreamFactory(conf);
		twitter_s = tsf.getInstance();
		twitter_s.addListener(new EventedTwitter());
		twitter_s.user();
	}

	public static Twitter getTwitterInstance() {
		return twitter;
	}

	public void postStatusToTelegram(Status sta) {
		ti.execute(new SendChatAction(AppConfig.TELEGRAM_SELF_ID,
				ChatAction.typing.name()));
		ArrayList<InlineKeyboardButton> al_ikb = new ArrayList<InlineKeyboardButton>();
		if (sta.isRetweetedByMe()) {
			al_ikb.add(new InlineKeyboardButton(
					AppConfig.TWITTER_ICONS[AppConfig.TWITTER_UNRETWEET])
					.callbackData("unretweet" + sta.getId()));
		} else {
			al_ikb.add(new InlineKeyboardButton(
					AppConfig.TWITTER_ICONS[AppConfig.TWITTER_RETWEET])
					.callbackData("retweet" + sta.getId()));
		}
		if (sta.isFavorited()) {
			al_ikb.add(new InlineKeyboardButton(
					AppConfig.TWITTER_ICONS[AppConfig.TWITTER_UNLIKE])
					.callbackData("unlike" + sta.getId()));
		} else {
			al_ikb.add(new InlineKeyboardButton(
					AppConfig.TWITTER_ICONS[AppConfig.TWITTER_LIKE])
					.callbackData("like" + sta.getId()));
		}
		InlineKeyboardMarkup markup = new InlineKeyboardMarkup(
				al_ikb.toArray(new InlineKeyboardButton[al_ikb.size()]));
		ti.execute(new SendMessage(AppConfig.TELEGRAM_SELF_ID,
				"https://twitter.com/" + sta.getUser().getScreenName()
						+ "/status/" + sta.getId()).replyMarkup(markup));
	}

	public boolean getLastestsTwits() {
		boolean ok = false;
		try {
			ResponseList<Status> home = twitter.getHomeTimeline(new Paging(
					lastID));
			if (home.size() > 0) {
				ok = true;
			}
			for (Status sta : Reversed.reversed(home)) {
				if (sta.getId() > lastID) {
					lastID = sta.getId();
				}
				if (sta.getUser().getScreenName()
						.equalsIgnoreCase(AppConfig.TWITTER_NICK)
						|| this.isIgnored(sta.getUser().getScreenName())) {
					continue;
				}
				if (sta.getText().startsWith("RT")) {
					continue;
				}
				this.postStatusToTelegram(sta);
			}
			PrintWriter pw = null;
			try {
				pw = new PrintWriter(twitter_last_id);
				pw.print(lastID);
			} catch (FileNotFoundException e) {
			}
			try {
				pw.close();
			} catch (Exception e) {
			}
		} catch (TwitterException e) {
			e.printStackTrace();
			e.getRateLimitStatus().getSecondsUntilReset();
			ti.execute(new SendChatAction(AppConfig.TELEGRAM_SELF_ID,
					ChatAction.typing.name()));
			ti.execute(new SendMessage(AppConfig.TELEGRAM_SELF_ID, "Error:\n"
					+ e.getMessage()));
			ok = true;
		}
		return ok;
	}

	class EventedTwitter implements UserStreamListener {

		@Override
		public void onDeletionNotice(
				StatusDeletionNotice paramStatusDeletionNotice) {
			/*
			 * On tweet deletion, self and others. Evented arg ti.execute(new
			 * SendChatAction(AppConfig.TELEGRAM_SELF_ID, ChatAction.typing
			 * .name())); ti.execute(new SendMessage(AppConfig.TELEGRAM_SELF_ID,
			 * "onDeletionNotice\n" + paramStatusDeletionNotice.toString()));
			 */
		}

		@Override
		public void onScrubGeo(long paramLong1, long paramLong2) {
			if (AppConfig.APP_DEBUG) {
				ti.execute(new SendChatAction(AppConfig.TELEGRAM_SELF_ID,
						ChatAction.typing.name()));
				ti.execute(new SendMessage(AppConfig.TELEGRAM_SELF_ID,
						"onScrubGeo\n\n" + paramLong1 + "\n" + paramLong2));
			}
		}

		@Override
		public void onStallWarning(StallWarning paramStallWarning) {
			if (AppConfig.APP_DEBUG) {
				ti.execute(new SendChatAction(AppConfig.TELEGRAM_SELF_ID,
						ChatAction.typing.name()));
				ti.execute(new SendMessage(AppConfig.TELEGRAM_SELF_ID,
						"onStallWarning\n\n" + paramStallWarning.toString()));
			}
		}

		@Override
		public void onStatus(Status sta) {
			if (sta.getId() > lastID) {
				lastID = sta.getId();
			}
			if (sta.getUser().getScreenName()
					.equalsIgnoreCase(AppConfig.TWITTER_NICK)
					|| isIgnored(sta.getUser().getScreenName())) {
				return;
			}
			if (sta.getText().startsWith("RT")) {
				return;
			}
			if (sta.isRetweet()) {
				this.onRetweet(sta);
			} else {
				postStatusToTelegram(sta);
			}
			PrintWriter pw = null;
			try {
				pw = new PrintWriter(twitter_last_id);
				pw.print(lastID);
			} catch (FileNotFoundException e) {
			}
			try {
				pw.close();
			} catch (Exception e) {
			}
		}

		public void onRetweet(Status sta) {
			Status orig = sta.getRetweetedStatus();
			if (orig.getUser().getScreenName().equals(AppConfig.TWITTER_NICK)) {
				// Someone retweeted your tweet
				ti.execute(new SendChatAction(AppConfig.TELEGRAM_SELF_ID,
						ChatAction.typing.name()));
				ti.execute(new SendMessage(AppConfig.TELEGRAM_SELF_ID,
						"The user: @" + sta.getUser().getScreenName()
								+ " has retweeted your tweet:\n"
								+ "https://twitter.com/"
								+ orig.getUser().getScreenName() + "/status/"
								+ orig.getId()));
			} else if (sta.getUser().getScreenName()
					.equals(orig.getUser().getScreenName())) {
				// Someone retweeted itself
				// Ignore?
			} else {
				ti.execute(new SendChatAction(AppConfig.TELEGRAM_SELF_ID,
						ChatAction.typing.name()));
				ArrayList<InlineKeyboardButton> al_ikb = new ArrayList<InlineKeyboardButton>();
				al_ikb.add(new InlineKeyboardButton(
						AppConfig.TWITTER_ICONS[AppConfig.TWITTER_RETWEET])
						.callbackData("retweet" + orig.getId()));
				al_ikb.add(new InlineKeyboardButton(
						AppConfig.TWITTER_ICONS[AppConfig.TWITTER_LIKE])
						.callbackData("like" + orig.getId()));
				InlineKeyboardMarkup markup = new InlineKeyboardMarkup(
						al_ikb.toArray(new InlineKeyboardButton[al_ikb.size()]));
				ti.execute(new SendMessage(AppConfig.TELEGRAM_SELF_ID,
						"The user: @" + sta.getUser().getScreenName()
								+ " has retweeted the tweet:\n"
								+ "https://twitter.com/"
								+ orig.getUser().getScreenName() + "/status/"
								+ orig.getId()).replyMarkup(markup));
			}
		}

		@Override
		public void onTrackLimitationNotice(int paramInt) {
			if (AppConfig.APP_DEBUG) {
				ti.execute(new SendChatAction(AppConfig.TELEGRAM_SELF_ID,
						ChatAction.typing.name()));
				ti.execute(new SendMessage(AppConfig.TELEGRAM_SELF_ID,
						"onTrackLimitationNotice\n\n" + paramInt));
			}
		}

		@Override
		public void onException(Exception e) {
			if (e instanceof TwitterException) {
				if (AppConfig.APP_DEBUG) {
					TwitterException twe = (TwitterException) e;
					if (twe.getMessage() != null) {
						ti.execute(new SendChatAction(
								AppConfig.TELEGRAM_SELF_ID, ChatAction.typing
										.name()));
						ti.execute(new SendMessage(AppConfig.TELEGRAM_SELF_ID,
								"onTwitterException\n\n" + e.getMessage()));
					}
				}
			} else {
				if (AppConfig.APP_DEBUG) {
					ti.execute(new SendChatAction(AppConfig.TELEGRAM_SELF_ID,
							ChatAction.typing.name()));
					ti.execute(new SendMessage(AppConfig.TELEGRAM_SELF_ID,
							"onException\n\n" + e.toString()));
				}
			}
		}

		@Override
		public void onDeletionNotice(long paramLong1, long paramLong2) {
			/*
			 * On tweet deletion, self and others. This says l1=twit_id
			 * l2=twit_user ti.execute(new
			 * SendChatAction(AppConfig.TELEGRAM_SELF_ID, ChatAction.typing
			 * .name())); ti.execute(new SendMessage(AppConfig.TELEGRAM_SELF_ID,
			 * "onDeletionNotice\n" + paramLong1 + "\n" + paramLong2));
			 */
		}

		@Override
		public void onFriendList(long[] paramArrayOfLong) {
			/*
			 * On Startup fetches the current follow list ti.execute(new
			 * SendChatAction(AppConfig.TELEGRAM_SELF_ID, ChatAction.typing
			 * .name())); String list = ""; for (int i = 0; i <
			 * paramArrayOfLong.length; i++) { list += paramArrayOfLong[i] + "";
			 * if (i < paramArrayOfLong.length - 1) { list += "\n"; } }
			 * ti.execute(new SendMessage(AppConfig.TELEGRAM_SELF_ID,
			 * "onFriendList\n" + list));
			 */
		}

		@Override
		public void onFavorite(User paramUser1, User paramUser2, Status sta) {
			/*
			 * ???
			 * 
			 * ti.execute(new SendChatAction(AppConfig.TELEGRAM_SELF_ID,
			 * ChatAction.typing.name())); ti.execute(new
			 * SendMessage(AppConfig.TELEGRAM_SELF_ID, "onFavorite\n\n" +
			 * paramUser1.toString() + "\n\n" + paramUser2.toString() + "\n\n" +
			 * paramStatus.toString())); if (paramUser1.getScreenName()
			 * .equalsIgnoreCase(AppConfig.TWITTER_NICK)) { return; }
			 */
			if (paramUser1.getScreenName().equalsIgnoreCase(
					AppConfig.TWITTER_NICK)
					|| isIgnored(sta.getUser().getScreenName())) {
				return;
			}
			ti.execute(new SendChatAction(AppConfig.TELEGRAM_SELF_ID,
					ChatAction.typing.name()));
			ti.execute(new SendMessage(AppConfig.TELEGRAM_SELF_ID,
					"El usuario: @" + paramUser1.getScreenName()
							+ " le ha dado Like a tu tweet:\n"
							+ "https://twitter.com/"
							+ sta.getUser().getScreenName() + "/status/"
							+ sta.getId()));
		}

		@Override
		public void onUnfavorite(User paramUser1, User paramUser2, Status sta) {
			if (paramUser1.getScreenName().equalsIgnoreCase(
					AppConfig.TWITTER_NICK)
					|| isIgnored(sta.getUser().getScreenName())) {
				return;
			}
			ti.execute(new SendChatAction(AppConfig.TELEGRAM_SELF_ID,
					ChatAction.typing.name()));
			ti.execute(new SendMessage(AppConfig.TELEGRAM_SELF_ID,
					"onUnfavorite\n\n" + paramUser1.toString() + "\n\n"
							+ paramUser2.toString() + "\n\n" + sta.toString()));
		}

		@Override
		public void onFollow(User paramUser1, User paramUser2) {
			/*
			 * Triggered when you follow or others follow you ti.execute(new
			 * SendChatAction(AppConfig.TELEGRAM_SELF_ID,
			 * ChatAction.typing.name())); ti.execute(new
			 * SendMessage(AppConfig.TELEGRAM_SELF_ID, "onFollow\n" +
			 * paramUser1.toString() + "\n" + paramUser2.toString()));
			 */
			if (paramUser1.getScreenName().equalsIgnoreCase(
					AppConfig.TWITTER_NICK)
					|| isIgnored(paramUser1.getScreenName())) {
				return;
			}
			ti.execute(new SendChatAction(AppConfig.TELEGRAM_SELF_ID,
					ChatAction.typing.name()));
			ti.execute(new SendMessage(AppConfig.TELEGRAM_SELF_ID,
					"El usuario: @" + paramUser1.getScreenName()
							+ " te ha seguido."));
		}

		@Override
		public void onUnfollow(User paramUser1, User paramUser2) {
			if (paramUser1.getScreenName().equalsIgnoreCase(
					AppConfig.TWITTER_NICK)) {
				return;
			}
			if (AppConfig.APP_DEBUG) {
				ti.execute(new SendChatAction(AppConfig.TELEGRAM_SELF_ID,
						ChatAction.typing.name()));
				ti.execute(new SendMessage(AppConfig.TELEGRAM_SELF_ID,
						"onUnfollow\n\n" + paramUser1.toString() + "\n\n"
								+ paramUser2.toString()));
			}
		}

		@Override
		public void onDirectMessage(DirectMessage paramDirectMessage) {
			if (paramDirectMessage.getSenderScreenName().equals(
					AppConfig.TWITTER_NICK)) {
				return;
			}
			System.out.println(paramDirectMessage);
			ti.execute(new SendChatAction(AppConfig.TELEGRAM_SELF_ID,
					ChatAction.typing.name()));
			ti.execute(new SendMessage(AppConfig.TELEGRAM_SELF_ID, "DM from: "
					+ paramDirectMessage.getSenderScreenName() + "\n"
					+ paramDirectMessage.getText()));
		}

		@Override
		public void onUserListMemberAddition(User paramUser1, User paramUser2,
				UserList paramUserList) {
			if (AppConfig.APP_DEBUG) {
				ti.execute(new SendChatAction(AppConfig.TELEGRAM_SELF_ID,
						ChatAction.typing.name()));
				ti.execute(new SendMessage(AppConfig.TELEGRAM_SELF_ID,
						"onUserListMemberAddition\n\n" + paramUser1.toString()
								+ "\n\n" + paramUser2.toString() + "\n\n"
								+ paramUserList.toString()));
			}
		}

		@Override
		public void onUserListMemberDeletion(User paramUser1, User paramUser2,
				UserList paramUserList) {
			if (AppConfig.APP_DEBUG) {
				ti.execute(new SendChatAction(AppConfig.TELEGRAM_SELF_ID,
						ChatAction.typing.name()));
				ti.execute(new SendMessage(AppConfig.TELEGRAM_SELF_ID,
						"onUserListMemberDeletion\n\n" + paramUser1.toString()
								+ "\n\n" + paramUser2.toString() + "\n\n"
								+ paramUserList.toString()));
			}
		}

		@Override
		public void onUserListSubscription(User paramUser1, User paramUser2,
				UserList paramUserList) {
			if (AppConfig.APP_DEBUG) {
				ti.execute(new SendChatAction(AppConfig.TELEGRAM_SELF_ID,
						ChatAction.typing.name()));
				ti.execute(new SendMessage(AppConfig.TELEGRAM_SELF_ID,
						"onUserListSubscription\n\n" + paramUser1.toString()
								+ "\n\n" + paramUser2.toString() + "\n\n"
								+ paramUserList.toString()));
			}
		}

		@Override
		public void onUserListUnsubscription(User paramUser1, User paramUser2,
				UserList paramUserList) {
			if (AppConfig.APP_DEBUG) {
				ti.execute(new SendChatAction(AppConfig.TELEGRAM_SELF_ID,
						ChatAction.typing.name()));
				ti.execute(new SendMessage(AppConfig.TELEGRAM_SELF_ID,
						"onUserListUnsubscription\n\n" + paramUser1.toString()
								+ "\n\n" + paramUser2.toString() + "\n\n"
								+ paramUserList.toString()));
			}
		}

		@Override
		public void onUserListCreation(User paramUser, UserList paramUserList) {
			if (AppConfig.APP_DEBUG) {
				ti.execute(new SendChatAction(AppConfig.TELEGRAM_SELF_ID,
						ChatAction.typing.name()));
				ti.execute(new SendMessage(AppConfig.TELEGRAM_SELF_ID,
						"onUserListCreation\n\n" + paramUser.toString()
								+ "\n\n" + paramUserList.toString()));
			}
		}

		@Override
		public void onUserListUpdate(User paramUser, UserList paramUserList) {
			if (AppConfig.APP_DEBUG) {
				ti.execute(new SendChatAction(AppConfig.TELEGRAM_SELF_ID,
						ChatAction.typing.name()));
				ti.execute(new SendMessage(AppConfig.TELEGRAM_SELF_ID,
						"onUserListUpdate\n\n" + paramUser.toString() + "\n\n"
								+ paramUserList.toString()));
			}
		}

		@Override
		public void onUserListDeletion(User paramUser, UserList paramUserList) {
			if (AppConfig.APP_DEBUG) {
				ti.execute(new SendChatAction(AppConfig.TELEGRAM_SELF_ID,
						ChatAction.typing.name()));
				ti.execute(new SendMessage(AppConfig.TELEGRAM_SELF_ID,
						"onUserListDeletion\n\n" + paramUser.toString()
								+ "\n\n" + paramUserList.toString()));
			}
		}

		@Override
		public void onUserProfileUpdate(User paramUser) {
			if (AppConfig.APP_DEBUG) {
				ti.execute(new SendChatAction(AppConfig.TELEGRAM_SELF_ID,
						ChatAction.typing.name()));
				ti.execute(new SendMessage(AppConfig.TELEGRAM_SELF_ID,
						"onUserProfileUpdate\n\n" + paramUser.toString()));
			}
		}

		@Override
		public void onUserSuspension(long paramLong) {
			if (AppConfig.APP_DEBUG) {
				ti.execute(new SendChatAction(AppConfig.TELEGRAM_SELF_ID,
						ChatAction.typing.name()));
				ti.execute(new SendMessage(AppConfig.TELEGRAM_SELF_ID,
						"onUserSuspension\n\n" + paramLong));
			}
		}

		@Override
		public void onUserDeletion(long paramLong) {
			if (AppConfig.APP_DEBUG) {
				ti.execute(new SendChatAction(AppConfig.TELEGRAM_SELF_ID,
						ChatAction.typing.name()));
				ti.execute(new SendMessage(AppConfig.TELEGRAM_SELF_ID,
						"onUserDeletion\n\n" + paramLong));
			}
		}

		@Override
		public void onBlock(User paramUser1, User paramUser2) {
			if (AppConfig.APP_DEBUG) {
				ti.execute(new SendChatAction(AppConfig.TELEGRAM_SELF_ID,
						ChatAction.typing.name()));
				ti.execute(new SendMessage(AppConfig.TELEGRAM_SELF_ID,
						"onBlock\n\n" + paramUser1.toString() + "\n\n"
								+ paramUser2.toString()));
			}
		}

		@Override
		public void onUnblock(User paramUser1, User paramUser2) {
			if (AppConfig.APP_DEBUG) {
				ti.execute(new SendChatAction(AppConfig.TELEGRAM_SELF_ID,
						ChatAction.typing.name()));
				ti.execute(new SendMessage(AppConfig.TELEGRAM_SELF_ID,
						"onUnblock\n\n" + paramUser1.toString() + "\n\n"
								+ paramUser2.toString()));
			}
		}

		@Override
		public void onRetweetedRetweet(User paramUser1, User paramUser2,
				Status paramStatus) {
			if (AppConfig.APP_DEBUG) {
				ti.execute(new SendChatAction(AppConfig.TELEGRAM_SELF_ID,
						ChatAction.typing.name()));
				ti.execute(new SendMessage(AppConfig.TELEGRAM_SELF_ID,
						"onRetweetedRetweet\n\n" + paramUser1.toString()
								+ "\n\n" + paramUser2.toString() + "\n\n"
								+ paramStatus.toString()));
			}
		}

		@Override
		public void onFavoritedRetweet(User paramUser1, User paramUser2,
				Status paramStatus) {
			if (AppConfig.APP_DEBUG) {
				ti.execute(new SendChatAction(AppConfig.TELEGRAM_SELF_ID,
						ChatAction.typing.name()));
				ti.execute(new SendMessage(AppConfig.TELEGRAM_SELF_ID,
						"onFavoritedRetweet\n\n" + paramUser1.toString()
								+ "\n\n" + paramUser2.toString() + "\n\n"
								+ paramStatus.toString()));
			}
		}

		@Override
		public void onQuotedTweet(User paramUser1, User paramUser2,
				Status paramStatus) {
			if (AppConfig.APP_DEBUG) {
				ti.execute(new SendChatAction(AppConfig.TELEGRAM_SELF_ID,
						ChatAction.typing.name()));
				ti.execute(new SendMessage(AppConfig.TELEGRAM_SELF_ID,
						"onQuotedTweet\n" + paramUser1.toString() + "\n\n"
								+ paramUser2.toString() + "\n\n"
								+ paramStatus.toString()));
			}
		}

	}

	@Override
	public void run(Update update) {
		if (update.message().text().startsWith(key)) {
			if (!this.getLastestsTwits()) {
				ti.execute(new SendChatAction(AppConfig.TELEGRAM_SELF_ID,
						ChatAction.typing.name()));
				ti.execute(new SendMessage(AppConfig.TELEGRAM_SELF_ID,
						"Nothing to fetch"));
			}
		}
	}

	public boolean isIgnored(String user) {
		for (String ign : AppConfig.TWITTER_IGNORE) {
			if (ign.equals(user)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String getHelp() {
		return "/fetch | Fuerza una actualización al servidor de Twitter";
	}

	@Override
	public String getToFather() {
		return "fetch - Fuerza una actualización al servidor de Twitter";
	}
}
