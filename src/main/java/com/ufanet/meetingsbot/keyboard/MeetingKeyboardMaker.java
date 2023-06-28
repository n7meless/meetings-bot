package com.ufanet.meetingsbot.keyboard;

import com.ufanet.meetingsbot.constants.Status;
import com.ufanet.meetingsbot.constants.state.EditState;
import com.ufanet.meetingsbot.constants.state.UpcomingState;
import com.ufanet.meetingsbot.dto.AccountDto;
import com.ufanet.meetingsbot.dto.MeetingDto;
import com.ufanet.meetingsbot.model.AccountTime;
import com.ufanet.meetingsbot.model.Group;
import com.ufanet.meetingsbot.model.Meeting;
import com.ufanet.meetingsbot.utils.Emojis;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.ufanet.meetingsbot.utils.CustomFormatter.DATE_WEEK_FORMATTER;


@Component
@RequiredArgsConstructor
public class MeetingKeyboardMaker extends KeyboardMaker {

    public List<List<InlineKeyboardButton>> getSubjectDurationInlineMarkup(MeetingDto meetingDto) {
//        Subject subject = meeting.getSubject();
        int duration = meetingDto.getSubjectDuration();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        int circleOrdinal = Emojis.RED_CIRCLE.ordinal();
        Emojis[] values = Emojis.values();
        for (int i = 15; i <= 90; i += 15, circleOrdinal++) {
            InlineKeyboardButton button;
            if (duration != 0 && duration == i) {
                button = defaultInlineButton(values[circleOrdinal].getEmojiSpace() + i, " ");
            } else {
                button = defaultInlineButton(Integer.toString(i), Integer.toString(i));
            }
            row.add(button);
        }
        keyboard.add(row);
        return keyboard;
    }

    public List<InlineKeyboardButton> defaultRowHelperInlineButtons(boolean next) {
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        buttons.add(cancel);
        if (next) {
            buttons.add(stepNext);
        }
        return buttons;
    }


    public List<List<InlineKeyboardButton>> getParticipantsInlineButtons(Set<AccountDto> groupMembers,
                                                                         Set<AccountDto> participantsIds) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        for (AccountDto member : groupMembers) {
            List<InlineKeyboardButton> button;
            if (participantsIds.contains(member)) {
                button = List.of(defaultInlineButton(
                        Emojis.GREY_SELECTED.getEmojiSpace() + " " + member.getFirstname(),
                        String.valueOf(member.getId())));
            } else {
                button = List.of(defaultInlineButton(member.getFirstname(),
                        Long.toString(member.getId())));
            }

            keyboard.add(button);
        }
        return keyboard;
    }

    public InlineKeyboardMarkup getGroupsInlineMarkup(MeetingDto meeting, List<Group> groups) {
        long groupId = meeting.getGroupId();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        for (Group group : groups) {
            List<InlineKeyboardButton> button;
            if (groupId == group.getId()) {
                button = List.of(defaultInlineButton(Emojis.GREY_SELECTED.getEmojiSpace() +
                        group.getTitle(), " "));
            } else {
                button = List.of(defaultInlineButton(group.getTitle(),
                        String.valueOf(group.getId())));
            }
            keyboard.add(button);

        }
//        keyboard.add(defaultRowHelperInlineButtons(hasGroup));
        return InlineKeyboardMarkup.builder().keyboard(keyboard).build();
    }

    public List<List<InlineKeyboardButton>> getQuestionsInlineMarkup(Set<String> questions) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        for (String question : questions) {
            InlineKeyboardButton questionButton = InlineKeyboardButton.builder()
                    .text(Emojis.GREY_SELECTED.getEmojiSpace()
                            + question).callbackData(question).build();
            keyboard.add(List.of(questionButton));
        }
        return keyboard;
    }

    public List<List<InlineKeyboardButton>> getEditingInlineButtons() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        InlineKeyboardButton subject = InlineKeyboardButton.builder()
                .text(Emojis.CLIPBOARD.getEmojiSpace() + "Ред. темы")
                .callbackData(EditState.EDIT_SUBJECT.name()).build();

        InlineKeyboardButton participants = InlineKeyboardButton.builder()
                .text(Emojis.PARTICIPANTS.getEmojiSpace() + "Ред. участников")
                .callbackData(EditState.EDIT_PARTICIPANT.name()).build();

        keyboard.add(List.of(subject, participants));

        InlineKeyboardButton time = InlineKeyboardButton.builder()
                .text(Emojis.DURATION.getEmojiSpace() + "Ред. время")
                .callbackData(EditState.EDIT_TIME.name()).build();
        InlineKeyboardButton address = InlineKeyboardButton.builder()
                .text(Emojis.OFFICE.getEmojiSpace() + "Ред. адрес")
                .callbackData(EditState.EDIT_ADDRESS.name()).build();

        keyboard.add(List.of(time, address));
        keyboard.add(List.of(cancel));
        return keyboard;
    }

    public InlineKeyboardMarkup getMeetingConfirmKeyboard(long meetingId) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        InlineKeyboardButton change = InlineKeyboardButton.builder()
                .text(Emojis.CHANGE.getEmojiSpace() + "Изменить указанные интервалы")
                .callbackData(UpcomingState.UPCOMING_EDIT_MEETING_TIME.name() + " " + meetingId).build();
        InlineKeyboardButton confirm = InlineKeyboardButton.builder()
                .text(Emojis.GREEN_SELECTED.getEmojiSpace() + "Подтверждаю")
                .callbackData(UpcomingState.UPCOMING_CONFIRM_MEETING_TIME.name() + " " + meetingId).build();
        InlineKeyboardButton cancel = InlineKeyboardButton.builder()
                .text(Emojis.CANCEL_CIRCLE.getEmojiSpace() + "Не смогу прийти")
                .callbackData(UpcomingState.UPCOMING_CANCEL_MEETING_TIME.name() + " " + meetingId).build();
        keyboard.add(List.of(change));
        keyboard.add(List.of(cancel, confirm));
        return InlineKeyboardMarkup.builder().keyboard(keyboard).build();
    }

    //TODO поменять подход
    public InlineKeyboardMarkup getChangeMeetingTimeKeyboard(long meetingId, List<AccountTime> accountTimes, String zoneId) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
//        Map<LocalDate, List<AccountTime>> collected = new TreeMap<>();
//
//        accountTimes.forEach(at -> {
//            MeetingTime meetingTime = at.getMeetingTime();
//            MeetingDate meetingDate = meetingTime.getMeetingDate();
//            LocalDate localDate = meetingDate.getDate();
//            List<AccountTime> times = collected.getOrDefault(localDate, new ArrayList<>());
//            times.add(at);
//            collected.put(localDate, times);
//        });

        TreeMap<LocalDate, List<AccountTime>> collected =
                accountTimes.stream().collect(Collectors.groupingBy(t -> t.getMeetingTime().getMeetingDate().getDate(), TreeMap::new, Collectors.toList()));

        for (Map.Entry<LocalDate, List<AccountTime>> entry : collected.entrySet()) {
            LocalDate dateTime = entry.getKey();
            InlineKeyboardButton questionButton = InlineKeyboardButton.builder()
                    .text(Emojis.CALENDAR.getEmojiSpace() + dateTime.format(DATE_WEEK_FORMATTER))
                    .callbackData(" ").build();
            keyboard.add(List.of(questionButton));

            List<AccountTime> times = entry.getValue();

            int i = 0;
            while (i < times.size()) {
                List<InlineKeyboardButton> buttons = new ArrayList<>();
                for (int j = 0; j < 4 && i < times.size(); j++, i++) {
                    AccountTime accountTime = times.get(i);
                    ZonedDateTime zonedDateTime = accountTime.getMeetingTime().getTimeWithZoneOffset(zoneId);
                    InlineKeyboardButton time = InlineKeyboardButton.builder()
                            .text(Emojis.GREY_SELECTED.getEmojiSpace() + zonedDateTime.toLocalTime().toString())
                            .callbackData(UpcomingState.UPCOMING_EDIT_MEETING_TIME.name() + " " + meetingId + " " + accountTime.getId()).build();

                    if (accountTime.getStatus().equals(Status.CANCELED)) {
                        time.setText(zonedDateTime.toLocalTime().toString());
                    }
                    buttons.add(time);
                }
                keyboard.add(buttons);
            }
        }

        InlineKeyboardButton confirm = InlineKeyboardButton.builder()
                .text(Emojis.GREEN_SELECTED.getEmojiSpace() + "Подтверждаю")
                .callbackData(UpcomingState.UPCOMING_CONFIRM_MEETING_TIME.name() + " " + meetingId).build();
        InlineKeyboardButton cancel = InlineKeyboardButton.builder()
                .text(Emojis.CANCEL_CIRCLE.getEmojiSpace() + "Не смогу прийти")
                .callbackData(UpcomingState.UPCOMING_CANCEL_MEETING_TIME.name() + " " + meetingId).build();
        keyboard.add(List.of(cancel, confirm));
        return InlineKeyboardMarkup.builder().keyboard(keyboard).build();
    }


    public InlineKeyboardMarkup getMeetingUpcomingMarkup(long userId, MeetingDto meetingDto,
                                                         Optional<AccountTime> accountTime) {
        Long meetingId = meetingDto.getId();
        InlineKeyboardButton btn1 = defaultInlineButton("Я опаздываю", UpcomingState.UPCOMING_IAMLATE + " " + meetingId);
        InlineKeyboardButton btn2 = defaultInlineButton("Я готов", UpcomingState.UPCOMING_IAMREADY + " " + meetingId);
        InlineKeyboardButton btn3 = defaultInlineButton("Я не приду", UpcomingState.UPCOMING_IWILLNOTCOME + " " + meetingId);
        InlineKeyboardButton btn4 = defaultInlineButton("Пингануть участника", "Пингануть участника");
        InlineKeyboardButton btn5 = defaultInlineButton("Назад", UpcomingState.UPCOMING_MEETINGS.name());
        InlineKeyboardButton btn6 = defaultInlineButton("Отменить встречу",
                UpcomingState.UPCOMING_CANCEL_BY_OWNER.name() + " " + meetingDto.getId());

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        boolean isOwner = meetingDto.getOwner().getId() == userId;
        if (isOwner) {
            keyboard.add(List.of(btn4));
            keyboard.add(List.of(btn6, btn5));
        } else if (accountTime.isPresent()) {
            Status status = accountTime.get().getStatus();
            switch (status) {
                case AWAITING -> {
                    btn1.setText(Emojis.GREEN_SELECTED.getEmojiSpace() + "Я опаздываю");
                    btn1.setCallbackData(" ");
                }
                case CONFIRMED -> {
                    btn2.setText(Emojis.GREEN_SELECTED.getEmojiSpace() + "Я готов");
                    btn2.setCallbackData(" ");
                }
                case CANCELED -> {
                    btn3.setText(Emojis.GREEN_SELECTED.getEmojiSpace() + "Я не приду");
                    btn3.setCallbackData(" ");
                }
            }
            keyboard.add(List.of(btn1, btn2));
            keyboard.add(List.of(btn5, btn3));
        }
        return InlineKeyboardMarkup.builder().keyboard(keyboard).build();
    }
}
