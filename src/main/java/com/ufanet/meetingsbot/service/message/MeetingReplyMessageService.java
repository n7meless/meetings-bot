package com.ufanet.meetingsbot.service.message;

import com.ufanet.meetingsbot.constants.ToggleButton;
import com.ufanet.meetingsbot.dto.MeetingMessage;
import com.ufanet.meetingsbot.keyboard.MeetingInlineKeyboardMaker;
import com.ufanet.meetingsbot.model.*;
import com.ufanet.meetingsbot.repository.GroupRepository;
import com.ufanet.meetingsbot.service.AccountService;
import com.ufanet.meetingsbot.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MeetingReplyMessageService extends ReplyMessageService {
    private final MeetingInlineKeyboardMaker meetingKeyboard;
    private final AccountService accountService;
    private final GroupRepository groupRepository;
    private final GroupService groupService;

    public void sendGroupMessage(long userId, Meeting meeting) {
        List<Group> groups = groupService.getGroupsByMemberId(userId);

        SendMessage message =
                messageUtils.generateSendMessageHtml(userId, localeMessageService.getMessage("reply.meeting.group"),
                        meetingKeyboard.getGroupsInlineMarkup(meeting, groups));

        executeSendMessage(message);
    }

    public void sendParticipantsMessage(long userId, Meeting meeting) {
        Set<Account> members = accountService.getAccountByGroupsIdAndIdNot(meeting.getGroup().getId(), userId);

        Set<Account> participants = meeting.getParticipants();
        EditMessageText message = messageUtils.generateEditMessageHtml(userId,
                localeMessageService.getMessage("reply.meeting.participants"),
                meetingKeyboard.getParticipantsInlineMarkup(members, participants));

        executeEditMessage(message);
    }

    public void sendSubjectMessage(long userId, Meeting meeting) {
        Subject subject = meeting.getSubject();
        InlineKeyboardMarkup keyboardMarkup;
        String title = subject.getTitle();
        boolean hasTitle = title != null;
        keyboardMarkup = InlineKeyboardMarkup.builder()
                .keyboardRow(meetingKeyboard.defaultRowHelperInlineButtons(hasTitle, false))
                .build();

        if (hasTitle) {
            SendMessage sendMessage = messageUtils.generateSendMessageHtml(userId, "Укажите тему встречи",
                    keyboardMarkup);
            executeSendMessage(sendMessage);
        } else {
            EditMessageText sendMessage =
                    messageUtils.generateEditMessageHtml(userId,
                            localeMessageService.getMessage("reply.meeting.subject"), keyboardMarkup);

            executeEditMessage(sendMessage);
        }
    }

    public void sendQuestionMessage(long userId, Meeting meeting) {
        Subject subject = meeting.getSubject();
        Set<Question> questions = subject.getQuestions();
        SendMessage sendMessage =
                messageUtils.generateSendMessageHtml(userId, localeMessageService.getMessage("reply.meeting.question"),
                        meetingKeyboard.getQuestionsInlineMarkup(meeting));
        executeSendMessage(sendMessage);
    }

    public void sendDateMessage(long userId, Meeting meeting, String callback) {
        EditMessageText message =
                messageUtils.generateEditMessageHtml(userId, localeMessageService.getMessage("reply.meeting.date"),
                        meetingKeyboard.getCalendarInlineMarkup(meeting, callback));

        executeEditMessage(message);

    }

    public void sendTimeMessage(long userId, Meeting meeting) {
        EditMessageText message = messageUtils.generateEditMessageHtml(userId,
                localeMessageService.getMessage("reply.meeting.time"),
                meetingKeyboard.getTimeInlineMarkup(meeting));

        executeEditMessage(message);
    }

    public void sendAddressMessage(long userId) {
        List<InlineKeyboardButton> buttons =
                meetingKeyboard.defaultRowHelperInlineButtons(true, true);

        EditMessageText message = messageUtils.generateEditMessageHtml(userId,
                localeMessageService.getMessage("reply.meeting.address"),
                InlineKeyboardMarkup.builder().keyboardRow(buttons).build());

        executeEditMessage(message);
    }

    public void sendSubjectDurationMessage(long userId, Meeting meeting) {
        Subject subject = meeting.getSubject();
        if (subject.getDuration() == null) {
            SendMessage sendMessage =
                    messageUtils.generateSendMessageHtml(userId,
                            localeMessageService.getMessage("reply.meeting.duration"),
                            meetingKeyboard.getSubjectDurationInlineMarkup(meeting));
            executeSendMessage(sendMessage);
        } else {
            EditMessageText sendMessage = messageUtils.generateEditMessageHtml(userId,
                    localeMessageService.getMessage("reply.meeting.duration"),
                    meetingKeyboard.getSubjectDurationInlineMarkup(meeting));
            executeEditMessage(sendMessage);
        }

    }

    public void sendCanceledMessage(long userId) {
        EditMessageText message = EditMessageText.builder().chatId(userId)
                .text("Встреча была отменена!").build();


        executeEditMessage(message);
    }

    public void sendEditErrorMessage(long userId) {
        SendMessage message =
                messageUtils.generateSendMessage(userId, "У вас нет ни одной созданной встречи", null);
        executeSendMessage(message);
    }

    public void sendSuccessMessageParticipants(long userId) {
        SendMessage sendMessage = messageUtils.generateSendMessage(userId,
                localeMessageService.getMessage("reply.meeting.sent.participants"));
        executeSendMessage(sendMessage);
    }


    public void sendAwaitingMessage(long userId, Meeting meeting) {
        InlineKeyboardButton sendButton = InlineKeyboardButton.builder().callbackData(ToggleButton.SEND.name())
                .text("Отправить запрос участникам").build();

        List<List<InlineKeyboardButton>> keyboard = meetingKeyboard.getEditingInlineButtons();
        keyboard.add(0, List.of(sendButton));

        InlineKeyboardMarkup keyboardMarkup = InlineKeyboardMarkup.builder()
                .keyboard(keyboard).build();

        MeetingMessage meetingMessage = messageUtils.generateMeetingMessage(meeting);

        String textMeeting =
                localeMessageService.getMessage("reply.meeting.awaiting",
                        meetingMessage.participants(), meetingMessage.subject(), meetingMessage.questions(),
                        meetingMessage.duration(), meetingMessage.times(), meetingMessage.address());

        SendMessage sendMessage = messageUtils.generateSendMessageHtml(userId, textMeeting,
                keyboardMarkup);

        executeSendMessage(sendMessage);
    }

    public void sendMeetingToParticipants(Meeting meeting) {
        Set<Account> participants = meeting.getParticipants();
        MeetingMessage meetingMessage = messageUtils.generateMeetingMessage(meeting);

        String textMeeting = localeMessageService.getMessage("reply.meeting.awaiting.participants",
                meetingMessage.owner(), meetingMessage.subject(), meetingMessage.questions(),
                meetingMessage.participants(), meetingMessage.duration(), meetingMessage.times(),
                meetingMessage.address());

        for (Account participant : participants) {
            InlineKeyboardMarkup keyboard =
                    meetingKeyboard.getMeetingConfirmKeyboard(meeting);

            SendMessage sendMessage =
                    messageUtils.generateSendMessageHtml(participant.getId(), textMeeting, keyboard);

            executeSendMessage(sendMessage);
        }
    }
}
