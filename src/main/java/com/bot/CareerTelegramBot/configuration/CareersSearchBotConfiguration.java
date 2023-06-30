package com.bot.CareerTelegramBot.configuration;

import com.bot.CareerTelegramBot.bot.CareersSearchBot;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class CareersSearchBotConfiguration {

    @Bean
    public TelegramBotsApi telegramBotsApi(CareersSearchBot careersSearchBot) throws TelegramApiException {
        var api = new TelegramBotsApi(DefaultBotSession.class);
        api.registerBot(careersSearchBot);
        return api;
    }

}
