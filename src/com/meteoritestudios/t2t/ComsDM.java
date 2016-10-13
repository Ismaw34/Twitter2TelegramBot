package com.meteoritestudios.t2t;

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
		_bot.execute(new SendChatAction(update.message().chat().id(),
				ChatAction.typing.name()));
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
		_bot.execute(new SendMessage(update.message().chat().id(),
				"El DM está NYI"));
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
