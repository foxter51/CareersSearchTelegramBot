package com.bot.CareerTelegramBot.Handler;

import com.bot.CareerTelegramBot.bot.CareersSearchBot;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Component
public class MessageSender {

    public void sendMessage(Long chatId, String text, CareersSearchBot careersSearchBot) {
        var chatIdStr = String.valueOf(chatId);
        var sendMessage = new SendMessage(chatIdStr, text);
        try {
            careersSearchBot.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendCareersIntoSeparateMessages(Long chatId, List<String> careers, CareersSearchBot careersSearchBot) {
        var careersPlain = new StringBuilder();

        for (int i = 0; i < careers.size(); i++) {
            var currentCareer = careers.get(i);

            if (careersPlain.length() + currentCareer.length() > 4096) {
                sendMessage(chatId, careersPlain.toString(), careersSearchBot);
                careersPlain.setLength(0);
            }
            careersPlain.append(currentCareer);

            if (i == careers.size() - 1) {
                sendMessage(chatId, careersPlain.toString(), careersSearchBot);
            }
        }
    }
}
