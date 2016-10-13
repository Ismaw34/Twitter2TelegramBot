package com.meteoritestudios.t2t;

import com.pengrad.telegrambot.model.Update;

public abstract class QueryBase extends ComsBase {

	public QueryBase(String key) {
		super(key);
	}

	public boolean isThis(Update update) {
		return update.callbackQuery().data().startsWith(key);
	}

}
