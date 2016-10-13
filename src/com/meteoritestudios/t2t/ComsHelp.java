package com.meteoritestudios.t2t;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ChatAction;
import com.pengrad.telegrambot.request.SendChatAction;
import com.pengrad.telegrambot.request.SendMessage;

public class ComsHelp extends ComsBase {

	ComsBase[] comandos;

	public ComsHelp() {
		super("/help");
	}

	public void setCommands(ComsBase[] comandos) {
		this.comandos = comandos;
	}

	@Override
	public void run(Update update) {
		TelegramBot _bot = TelegramInstance.getBot();
		_bot.execute(new SendChatAction(update.message().chat().id(),
				ChatAction.typing.name()));
		String help = "";
		for (ComsBase base : comandos) {
			if (base.getHelp() != null) {
				help += base.getHelp() + "\n";
			}
		}
		_bot.execute(new SendMessage(update.message().chat().id(), help
				.substring(0, help.length() - 1)).replyToMessageId(update
				.message().messageId()));
	}

	@Override
	public String getHelp() {
		return "/help | Envia este mensaje";
	}

	@Override
	public String getToFather() {
		return "help - Envia una lista de comandos";
	}

}
