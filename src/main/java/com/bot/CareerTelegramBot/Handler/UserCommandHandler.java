package com.bot.CareerTelegramBot.Handler;

import com.bot.CareerTelegramBot.bot.CareersSearchBot;
import com.bot.CareerTelegramBot.service.SearchCareersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.HashMap;
import java.util.Map;

@Component
public class UserCommandHandler {

    private static final String START = "/start";
    private static final String HELP = "/help";
    private static final String SEARCH = "/search";
    private static final String SUBSCRIBE = "/subscribe";
    private static final String UNSUBSCRIBE = "/unsubscribe";

    private final SearchCareersService searchCareersService;

    private enum BotState {
        WAITING_FOR_SEARCH,
        WAITING_FOR_SUBSCRIPTION
    }

    private final Map<Long, BotState> userStates = new HashMap<>();

    @Autowired
    public UserCommandHandler(SearchCareersService searchCareersService) {
        this.searchCareersService = searchCareersService;
    }

    public void handleCommand(Update update, CareersSearchBot careersSearchBot) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }

        var message = update.getMessage().getText();
        var chatId = update.getMessage().getChatId();

        BotState userState = userStates.get(chatId);
        if (userState != null) {
            switch (userState) {
                case WAITING_FOR_SEARCH -> {
                    searchCommand(chatId, message, careersSearchBot);
                    userStates.remove(chatId);
                }
                case WAITING_FOR_SUBSCRIPTION -> {
                    subscribeCommand(chatId, message, careersSearchBot);
                    userStates.remove(chatId);
                }
            }
        } else switch (message) {
            case START -> {
                String userName = update.getMessage().getChat().getUserName();
                startCommand(chatId, userName, careersSearchBot);
            }
            case SEARCH -> {
                userStates.put(chatId, BotState.WAITING_FOR_SEARCH);
                careersSearchBot.sendMessage(chatId, "Enter your request:");
            }
            case SUBSCRIBE -> {
                userStates.put(chatId, BotState.WAITING_FOR_SUBSCRIPTION);
                careersSearchBot.sendMessage(chatId, "Enter your request:");
            }
            case UNSUBSCRIBE -> unsubscribeCommand(chatId, careersSearchBot);
            case HELP -> helpCommand(chatId, careersSearchBot);
            default -> unknownCommand(chatId, careersSearchBot);
        }
    }

    private void startCommand(Long chatId, String userName, CareersSearchBot careersSearchBot) {
        var text = "Welcome, %s!";
        var formattedText = String.format(text, userName);
        careersSearchBot.sendMessage(chatId, formattedText);
        helpCommand(chatId, careersSearchBot);
    }

    public boolean searchCommand(Long chatId, String req, CareersSearchBot careersSearchBot) {
        try {
            var careersFromDjinni = searchCareersService.getCareersFromDjinni(req);
            var careersFromDou = searchCareersService.getCareersFromDou(req);

            careersSearchBot.sendCareersIntoSeparateMessages(chatId, careersFromDjinni);
            careersSearchBot.sendCareersIntoSeparateMessages(chatId, careersFromDou);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            unknownCommand(chatId, careersSearchBot);
        }
        return false;
    }

    public void subscribeCommand(Long chatId, String req, CareersSearchBot careersSearchBot) {
        careersSearchBot.sendMessage(chatId, careersSearchBot.subscribeOnCareers(chatId, req) ?
                "Successfully subscribed!"
                :
                "Failed to subscribe. Please try again!");
    }

    private void unsubscribeCommand(Long chatId, CareersSearchBot careersSearchBot) {
        careersSearchBot.unsubscribeFromCareers(chatId);
    }

    private void helpCommand(Long chatId, CareersSearchBot careersSearchBot) {
        var text = """
                    Bot commands:
                    /start - start the bot
                    /search request - search for a specified request on job platforms
                    /subscribe request - subscribe on a specified request on job platforms (every 30 min)
                    /unsubscribe - cancel subscription
                    /help - get bot commands
                    """;
        careersSearchBot.sendMessage(chatId, text);
    }

    private void unknownCommand(Long chatId, CareersSearchBot careersSearchBot) {
        var text = "Bad command!";
        careersSearchBot.sendMessage(chatId, text);
    }
}
