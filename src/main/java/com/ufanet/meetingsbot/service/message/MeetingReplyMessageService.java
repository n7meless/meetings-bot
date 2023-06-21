package com.ufanet.meetingsbot.service.message;

import com.ufanet.meetingsbot.constants.ToggleButton;
import com.ufanet.meetingsbot.constants.state.MeetingState;
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
    private final MeetingInlineKeyboardMaker meetingInlineKeyboardMaker;
    private final AccountService accountService;
    private final GroupRepository groupRepository;
    private final GroupService groupService;

    public void sendMessage(long userId, Meeting meeting, String callback) {
        MeetingState state = meeting.getState();
        switch (state) {
            case GROUP_SELECT -> sendGroupMessage(userId, meeting);
            case PARTICIPANT_SELECT -> sendParticipantsMessage(userId, meeting);
            case SUBJECT_SELECT -> sendSubjectMessage(userId, meeting);
            case SUBJECT_DURATION_SELECT -> sendSubjectDurationMessage(userId, meeting);
            case QUESTION_SELECT -> sendQuestionMessage(userId, meeting);
            case DATE_SELECT -> sendDateMessage(userId, meeting, callback);
            case TIME_SELECT -> sendTimeMessage(userId, meeting);
            case ADDRESS_SELECT -> sendAddressMessage(userId);
            case READY -> sendAwaitingMessage(userId, meeting);
            case CANCELED -> sendCanceledMessage(userId);
        }
    }

    public void sendGroupMessage(long userId, Meeting meeting) {
        List<Group> groups = groupService.getGroupsByMemberId(userId);

        SendMessage message =
                messageUtils.generateSendMessageHtml(userId, localeMessageService.getMessage("reply.meeting.group"),
                        meetingInlineKeyboardMaker.getGroupsInlineMarkup(meeting, groups));

        executeSendMessage(message);
    }

    public void sendParticipantsMessage(long userId, Meeting meeting) {
        Set<Account> members = accountService.getAccountByGroupsIdAndIdNot(meeting.getGroup().getId(), userId);

        Set<Account> participants = meeting.getParticipants();
        EditMessageText message = messageUtils.generateEditMessageHtml(userId,
                localeMessageService.getMessage("reply.meeting.participants"),
                meetingInlineKeyboardMaker.getParticipantsInlineMarkup(members, participants));

        executeEditMessage(message);
    }

    public void sendSubjectMessage(long userId, Meeting meeting) {
        Subject subject = meeting.getSubject();
        InlineKeyboardMarkup keyboardMarkup;
        String title = subject.getTitle();
        boolean hasTitle = title != null;
        keyboardMarkup = InlineKeyboardMarkup.builder()
                .keyboardRow(meetingInlineKeyboardMaker.defaultRowHelperInlineButtons(hasTitle, false))
                .build();
        //TODO Доделать
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
                        meetingInlineKeyboardMaker.getQuestionsInlineMarkup(meeting));
        executeSendMessage(sendMessage);
    }

    public void sendDateMessage(long userId, Meeting meeting, String callback) {
        EditMessageText message =
                messageUtils.generateEditMessageHtml(userId, localeMessageService.getMessage("reply.meeting.date"),
                        meetingInlineKeyboardMaker.getCalendarInlineMarkup(meeting, callback));

        executeEditMessage(message);

    }

    public void sendTimeMessage(long userId, Meeting meeting) {
        EditMessageText message = messageUtils.generateEditMessageHtml(userId,
                localeMessageService.getMessage("reply.meeting.time"),
                meetingInlineKeyboardMaker.getTimeInlineMarkup(meeting));

        executeEditMessage(message);
    }

    public void sendAddressMessage(long userId) {
        List<InlineKeyboardButton> buttons =
                meetingInlineKeyboardMaker.defaultRowHelperInlineButtons(true, true);

        EditMessageText message =messageUtils.generateEditMessageHtml(userId,
                localeMessageService.getMessage("reply.meeting.address"),
                InlineKeyboardMarkup.builder().keyboardRow(buttons).build());

        executeEditMessage(message);
    }

    public void sendSubjectDurationMessage(long userId, Meeting meeting) {

        EditMessageText sendMessage =
                EditMessageText.builder()
                        .text(localeMessageService.getMessage("reply.meeting.duration"))
                        .chatId(userId)
                        .replyMarkup(meetingInlineKeyboardMaker.getSubjectDurationInlineMarkup(meeting)).build();

        executeEditMessage(sendMessage);
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

        List<List<InlineKeyboardButton>> keyboard = meetingInlineKeyboardMaker.getEditingInlineButtons();
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

//        Set<AccountTime> accountTimes = meeting.getDates().stream()
//                .map(MeetingDate::getMeetingTimes).flatMap(Collection::stream)
//                .map(MeetingTime::getAccountTimes).flatMap(Collection::stream)
//                .collect(Collectors.toSet());

        for (Account participant : participants) {
            InlineKeyboardMarkup keyboard =
                    meetingInlineKeyboardMaker.getMeetingConfirmKeyboard(meeting);

            SendMessage sendMessage =
                    messageUtils.generateSendMessageHtml(participant.getId(), textMeeting, keyboard);

            executeSendMessage(sendMessage);
        }
    }

    public void sendMeetingToParticipant(long userId, Meeting meeting) {

        MeetingMessage meetingMessage = messageUtils.generateMeetingMessage(meeting);
        String textMeeting = localeMessageService.getMessage("reply.meeting.awaiting.participants",
                meetingMessage.owner(), meetingMessage.subject(), meetingMessage.questions(),
                meetingMessage.participants(), meetingMessage.duration(), meetingMessage.times(),
                meetingMessage.address());

        InlineKeyboardMarkup keyboard = meetingInlineKeyboardMaker.getMeetingConfirmKeyboard(meeting);
        EditMessageText sendMessage = messageUtils.generateEditMessageHtml(userId, textMeeting,keyboard);

        executeEditMessage(sendMessage);
    }

}
