package com.meteoritestudios.t2t;

import com.pengrad.telegrambot.model.Update;

public abstract class ComsBase {

	protected final String key;

	public ComsBase(String key) {
		this.key = key;
	}

	public boolean isThis(Update update) {
		return update.message().text().startsWith(key);
	}

	public boolean storeWho() {
		return true;
	}

	public abstract void run(Update update);

	public abstract String getHelp();
	
	public abstract String getToFather();

}
