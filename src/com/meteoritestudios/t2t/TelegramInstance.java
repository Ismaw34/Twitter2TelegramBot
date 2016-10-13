package com.meteoritestudios.t2t;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import retrofit.RetrofitError;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.TelegramBotAdapter;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.GetUpdates;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.GetUpdatesResponse;

public class TelegramInstance extends TimerTask {

	private Timer timer;

	private static TelegramBot _bot;
	private int last_msg = 0;

	TwitterInstance twitter_instance;

	ComsBase[] coms = new ComsBase[5];
	QueryBase[] querys = new QueryBase[2];

	public TelegramInstance() throws Throwable {
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

			@Override
			public void run() {
				System.out.println("Exited");
			}
		}));
		_bot = TelegramBotAdapter.build(AppConfig.TELEGRAM_TOKEN);
		twitter_instance = new TwitterInstance(_bot);
		coms[0] = new ComsTwitter();
		coms[1] = twitter_instance;
		coms[2] = new ComsHelp();
		coms[3] = new ComsFather();
		coms[4] = new ComsDM();
		((ComsHelp) coms[2]).setCommands(coms);
		((ComsFather) coms[3]).setCommands(coms);
		querys[0] = new QueryRetweet();
		querys[1] = new QueryLike();
		timer = new Timer("TimerEvent", true);
		timer.schedule(this, 0, 1000);
		while (true) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
			}
		}
	}

	public static TelegramBot getBot() {
		return _bot;
	}

	public static void main(String[] args) {
		@SuppressWarnings("unused")
		TelegramInstance bip = null;
		try {
			bip = new TelegramInstance();
		} catch (Throwable t) {
			try {
				File file = new File("bipbot_crash.log");
				FileWriter fw = new FileWriter(file, true);
				fw.write(t.toString() + "\n");
				StackTraceElement[] st = t.getStackTrace();
				for (StackTraceElement ste : st) {
					fw.write(ste.toString() + "\n");
				}
				fw.flush();
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			t.printStackTrace();
		}
	}

	@Override
	public void run() {
		TelegramBot _bot = TelegramInstance.getBot();
		try {
			GetUpdatesResponse update_response = _bot.execute(new GetUpdates()
					.limit(100).offset(last_msg).timeout(0));
			if (update_response.isOk()) {
				List<Update> updates = update_response.updates();
				for (Update u : updates) {
					System.out.println(u);
					try {
						if (isValid(u)) {
							if (u.message() != null
									&& u.message().text() != null) {
								for (ComsBase cb : coms) {
									if (cb.isThis(u)) {
										cb.run(u);
										break;
									}
								}
							} else if (u.callbackQuery() != null) {
								for (QueryBase cb : querys) {
									if (cb.isThis(u)) {
										cb.run(u);
										break;
									}
								}
							} else {
							}
						} else {
							_bot.execute(new SendMessage(u.message().chat()
									.id(), "Private bot. Sorry."));
						}
					} catch (RetrofitError e) {
						e.printStackTrace();
					} catch (Throwable t) {
						String exception = t.toString() + "\n";
						StackTraceElement[] st = t.getStackTrace();
						for (StackTraceElement ste : st) {
							exception += "\t" + ste.toString() + "\n";
						}
						if (AppConfig.APP_DEBUG) {
							TelegramInstance.getBot().execute(
									new SendMessage(AppConfig.TELEGRAM_SELF_ID,
											exception));
						}
						try {
							File file = new File("bipbot_fail.log");
							BufferedWriter bw = new BufferedWriter(
									new FileWriter(file, true));
							bw.append("=== === === New Exception === === ===\n");
							bw.append(exception);
							bw.flush();
							bw.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
						t.printStackTrace();
					}
					last_msg = u.updateId() + 1;
				}
			}
		} catch (RetrofitError e) {
			e.printStackTrace();
		} catch (RuntimeException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	protected final boolean isValid(Update update) {
		if (update.message() != null) {
			if (update.message().chat().type() == Chat.Type.Private) {
				if (update.message().chat().id() == AppConfig.TELEGRAM_SELF_ID) {
					return true;
				}
			}
		} else if (update.callbackQuery() != null) {
			if (update.callbackQuery().message().chat().type() == Chat.Type.Private) {
				if (update.callbackQuery().message().chat().id() == AppConfig.TELEGRAM_SELF_ID) {
					return true;
				}
			}
		}
		return false;
	}

}
