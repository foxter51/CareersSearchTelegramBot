package com.bot.CareerTelegramBot.service;

import java.util.List;

public interface SearchCareersService {

    List<String> getCareersFromDjinni (String req);
    List<String> getCareersFromDou (String req);

}
