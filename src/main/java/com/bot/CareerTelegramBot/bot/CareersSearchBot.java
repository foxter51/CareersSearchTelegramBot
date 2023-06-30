package com.bot.CareerTelegramBot.bot;

import com.bot.CareerTelegramBot.Handler.MessageSender;
import com.bot.CareerTelegramBot.Handler.UserCommandHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Component
public class CareersSearchBot extends TelegramLongPollingBot {

    private final UserCommandHandler userCommandHandler;
    private final MessageSender messageSender;

    private ScheduledFuture<?> scheduledFuture;

    @Autowired
    public CareersSearchBot(@Value("${bot.token}") String botToken, UserCommandHandler userCommandHandler, MessageSender messageSender) {
        super(botToken);
        this.userCommandHandler = userCommandHandler;
        this.messageSender = messageSender;
    }

    @Override
    public void onUpdateReceived(Update update) {
        userCommandHandler.handleCommand(update, this);
    }

    @Override
    public String getBotUsername() {
        return "CareersSearch";
    }

    public void sendMessage(Long chatId, String text) {
        messageSender.sendMessage(chatId, text, this);
    }

    public void sendCareersIntoSeparateMessages(Long chatId, List<String> careers) {
        messageSender.sendCareersIntoSeparateMessages(chatId, careers, this);
    }

    public boolean subscribeOnCareers(Long chatId, String req) {
        var scheduler = Executors.newScheduledThreadPool(1);
        var searchResult = userCommandHandler.searchCommand(chatId, req, this);

        if (searchResult) {
            scheduledFuture = scheduler.scheduleAtFixedRate(() -> userCommandHandler.searchCommand(chatId, req, this), 30, 30, TimeUnit.MINUTES);
            return true;
        }
        return false;
    }

    public void unsubscribeFromCareers(Long chatId) {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
            sendMessage(chatId, "The subscription has been stopped!");
        } else sendMessage(chatId, "Nothing to unsubscribe!");
    }
}
