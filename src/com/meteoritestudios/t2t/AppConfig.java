package com.meteoritestudios.t2t;

public final class AppConfig {
	
	// Debug Stuff
	public static final boolean APP_DEBUG = false;

	// Telegram Stuff
	public static final String TELEGRAM_TOKEN = "";
	public static final long TELEGRAM_SELF_ID = -1;
	
	// Twitter Stuff
	public static final String TWITTER_CONSUMER_KEY = "";
	public static final String TWITTER_CONSUMER_PRIVATE = "";
	public static final String TWITTER_ACCESS_TOKEN = "";
	public static final String TWITTER_ACCESS_TOKEN_PRIVATE = "";
	public static final String TWITTER_NICK = ""; // EX: Good = "Twitter", Bad = "@Twitter"
	public static final int TWITTER_RETWEET = 0, TWITTER_UNRETWEET = 1, TWITTER_LIKE = 2, TWIITER_UNLIKE = 3;
	public static final String[] TWITTER_ICONS = new String[]{"🔁","🔂","❤️","💔"}; // Ret | UnRet | Like | Unlike
	
}
