package com.ufanet.meetingsbot.service;

import com.ufanet.meetingsbot.cache.BotStateCache;
import com.ufanet.meetingsbot.constants.MainCommands;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

import static com.ufanet.meetingsbot.constants.MainButtonNameEnum.*;

@Service
@RequiredArgsConstructor
public class MainMenuService {
    private final MessageService messageService;
//    private final List<MessageHandler> messageHandlers;
    private final BotStateCache botStateCache;

//    public BotApiMethod<?> processMessage(Update update) {
//        Message message = update.getMessage();
//        String text = message.getText();
////        if (ChatType.PRIVATE.isTypeOf(text));
//        switch (text) {
//            case "/start":
//            case "/help":
//            case "/about":
//            default: {
//                String type = message.getChat().getType();
//                return (BotApiMethod<?>) messageHandlers.stream()
//                            .filter((handler) -> handler.getMessageType().equals(type))
//                            .map(handler -> handler.handleUpdate(update)).findFirst()
//                            .orElse(messageService.getWarningMessage(message.getChatId()));
//            }
//        }
//    }

    public SetMyCommands getMainCommands() {
        List<BotCommand> commands = new ArrayList<>();
        commands.add(new BotCommand(MainCommands.START.getCommand(), ""));
        commands.add(new BotCommand(MainCommands.HELP.getCommand(), ""));
        SetMyCommands myCommands = new SetMyCommands(commands, new BotCommandScopeDefault(), null);
//        ChatType.Private.CREATE.getType();
        return myCommands;
    }

    public SendMessage getMainMessage(Long userId) {
        return SendMessage.builder().chatId(userId).text("")
                .replyMarkup(getMainMenuKeyboard())
                .build();
    }

    private ReplyKeyboardMarkup getMainMenuKeyboard() {
        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton(CREATE_MEETING.getButtonName()));
        row1.add(new KeyboardButton(UPCOMING_MEETINGS.getButtonName()));

        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton(EDIT_MEETING.getButtonName()));
        row2.add(new KeyboardButton(MY_PROFILE.getButtonName()));
        return ReplyKeyboardMarkup.builder()
                .keyboard(List.of(row1, row2))
                .selective(true)
                .resizeKeyboard(true)
                .oneTimeKeyboard(false)
                .build();
    }

    public boolean isMainCommand(String command) {
        MainCommands[] values = MainCommands.values();
        for (MainCommands value : values) {
            if (value.getCommand().equals(command)) {
                return true;
            }
        }
        return false;
    }
}
