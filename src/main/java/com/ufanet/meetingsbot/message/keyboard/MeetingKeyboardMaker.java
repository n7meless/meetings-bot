package com.ufanet.meetingsbot.message.keyboard;

import com.ufanet.meetingsbot.constants.Status;
import com.ufanet.meetingsbot.constants.ToggleButton;
import com.ufanet.meetingsbot.constants.state.EditState;
import com.ufanet.meetingsbot.constants.state.MeetingState;
import com.ufanet.meetingsbot.constants.state.UpcomingState;
import com.ufanet.meetingsbot.dto.AccountDto;
import com.ufanet.meetingsbot.dto.AccountTimeDto;
import com.ufanet.meetingsbot.dto.MeetingDto;
import com.ufanet.meetingsbot.dto.SubjectDto;
import com.ufanet.meetingsbot.entity.Group;
import com.ufanet.meetingsbot.exceptions.AccountNotFoundException;
import com.ufanet.meetingsbot.utils.CustomFormatter;
import com.ufanet.meetingsbot.utils.Emojis;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.ufanet.meetingsbot.utils.CustomFormatter.DATE_WEEK_FORMATTER;


@Component
@RequiredArgsConstructor
public class MeetingKeyboardMaker extends KeyboardMaker {
    private final Emojis[] emojiCircles = {Emojis.RED_CIRCLE, Emojis.ORANGE_CIRCLE,
            Emojis.YELLOW_CIRCLE, Emojis.GREEN_CIRCLE, Emojis.BLUE_CIRCLE,
            Emojis.PURPLE_CIRCLE};

    public List<List<InlineKeyboardButton>> getSubjectDurationInlineMarkup(MeetingDto meetingDto) {
        Integer duration = meetingDto.getSubjectDto().getDuration();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        for (int i = 15, j = 0; i <= 90; i += 15, j++) {
            InlineKeyboardButton button;
            if (duration != null && duration == i) {
                button = defaultInlineButton(emojiCircles[j].getEmojiSpace() + i, " ");
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
        InlineKeyboardButton cancelBtn = getCancelInlineButton(ToggleButton.CANCEL.name());
        InlineKeyboardButton nextBtn = getNextInlineButton(ToggleButton.NEXT.name());
        buttons.add(cancelBtn);
        if (next) {
            buttons.add(nextBtn);
        }
        return buttons;
    }


    public List<List<InlineKeyboardButton>> getParticipantsInlineButtons(Set<AccountDto> groupMembers,
                                                                         Set<AccountDto> participantsIds) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        for (AccountDto member : groupMembers) {
            InlineKeyboardButton participantBtn = defaultInlineButton(member.getFirstname(), Long.toString(member.getId()));

            if (participantsIds.contains(member)) {
                participantBtn.setText(Emojis.GREEN_SELECTED.getEmojiSpace() + member.getFirstname());
            }

            keyboard.add(List.of(participantBtn));
        }
        return keyboard;
    }

    public InlineKeyboardMarkup getGroupsInlineMarkup(List<Group> groups) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        for (Group group : groups) {
            InlineKeyboardButton groupBtn = defaultInlineButton(group.getTitle(),
                    String.valueOf(group.getId()));
            keyboard.add(List.of(groupBtn));
        }
        return buildInlineMarkup(keyboard);
    }

    public List<List<InlineKeyboardButton>> getQuestionsInlineMarkup(Set<String> questions) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        for (String question : questions) {
            InlineKeyboardButton questionBtn =
                    defaultInlineButton(Emojis.GREEN_SELECTED.getEmojiSpace() + question,
                            question);

            keyboard.add(List.of(questionBtn));
        }
        return keyboard;
    }

    public List<List<InlineKeyboardButton>> getEditingInlineButtons() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        InlineKeyboardButton subjectBtn = defaultInlineButton(Emojis.CLIPBOARD.getEmojiSpace() + "Ред. темы",
                EditState.EDIT_SUBJECT.name());

        InlineKeyboardButton participantBtn = defaultInlineButton(Emojis.PARTICIPANTS.getEmojiSpace() + "Ред. участников",
                EditState.EDIT_PARTICIPANT.name());

        keyboard.add(List.of(subjectBtn, participantBtn));

        InlineKeyboardButton timeBtn = defaultInlineButton(Emojis.DURATION.getEmojiSpace() + "Ред. время",
                EditState.EDIT_TIME.name());

        InlineKeyboardButton addressBtn = defaultInlineButton(Emojis.OFFICE.getEmojiSpace() + "Ред. адрес",
                EditState.EDIT_ADDRESS.name());

        InlineKeyboardButton cancelBtn = getCancelInlineButton(ToggleButton.CANCEL.name());
        keyboard.add(List.of(timeBtn, addressBtn));
        keyboard.add(List.of(cancelBtn));
        return keyboard;
    }

    //TODO move to builder class
    public InlineKeyboardButton getGoogleCalendarButton(long userId, MeetingDto meetingDto) {
        SubjectDto subjectDto = meetingDto.getSubjectDto();
        Integer duration = subjectDto.getDuration();
        String title = subjectDto.getTitle();
        Set<String> questions = subjectDto.getQuestions();
        String address = meetingDto.getAddress();
        String uriString = "https://calendar.google.com/calendar/render?action=TEMPLATE";
        StringBuilder sb = new StringBuilder(uriString);
        sb.append("&text=").append(title);
        sb.append("&location=").append(address);
        AccountDto accountDto = meetingDto.getParticipants().stream().filter(ac -> ac.getId() == userId).findFirst()
                .orElseThrow(() -> new AccountNotFoundException(userId));
        String timeZone = accountDto.getTimeZone();

        ZonedDateTime dateWithZoneId = meetingDto.getDateWithZoneId(timeZone);
        String time = dateWithZoneId.format(CustomFormatter.GOOGLE_DATE_TIME_ZONE_FORMATTER);
        String timePlusDuration = dateWithZoneId.plusMinutes(duration)
                .format(CustomFormatter.GOOGLE_DATE_TIME_ZONE_FORMATTER);

        sb.append("&dates=").append(time).append("/").append(timePlusDuration);
        sb.append("&details=");
        sb.append("Обсуждаемые вопросы встречи:").append("%0A");
        int i = 1;
        for (String question : questions) {
            sb.append(i).append(". ").append(question).append("%0A");
            i++;
        }
        InlineKeyboardButton button = defaultInlineButton(Emojis.CALENDAR.getEmojiSpace() + "Добавить в календарь", "Google Calendar");
        String replaceAll = sb.toString().replaceAll("\\s", "%20");
        button.setUrl(replaceAll);
        return button;
    }

    public InlineKeyboardMarkup getMeetingConfirmKeyboard(long meetingId) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        InlineKeyboardButton changeBtn = defaultInlineButton(Emojis.CHANGE.getEmojiSpace() + "Изменить указанные интервалы",
                UpcomingState.UPCOMING_EDIT_MEETING_TIME.name() + " " + meetingId);

        InlineKeyboardButton confirmBtn = defaultInlineButton(Emojis.GREEN_SELECTED.getEmojiSpace() + "Подтверждаю",
                UpcomingState.UPCOMING_CONFIRM_MEETING_TIME.name() + " " + meetingId);

        InlineKeyboardButton cancelBtn = defaultInlineButton(Emojis.CANCEL_CIRCLE.getEmojiSpace() + "Не смогу прийти",
                UpcomingState.UPCOMING_CANCEL_MEETING_TIME.name() + " " + meetingId);

        keyboard.add(List.of(changeBtn));
        keyboard.add(List.of(cancelBtn, confirmBtn));
        return buildInlineMarkup(keyboard);
    }

    public InlineKeyboardMarkup getChangeMeetingTimeKeyboard(long meetingId, List<AccountTimeDto> accountTimes, String zoneId) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        TreeMap<LocalDate, List<AccountTimeDto>> collected =
                accountTimes.stream().sorted()
                        .collect(Collectors.groupingBy(t -> t.getMeetingTime().getTimeWithZoneOffset(zoneId).toLocalDate(),
                                TreeMap::new, Collectors.toList()));

        for (Map.Entry<LocalDate, List<AccountTimeDto>> entry : collected.entrySet()) {
            LocalDate dateTime = entry.getKey();
            InlineKeyboardButton questionBtn =
                    defaultInlineButton(Emojis.CALENDAR.getEmojiSpace() + dateTime.format(DATE_WEEK_FORMATTER),
                            " ");

            keyboard.add(List.of(questionBtn));

            List<AccountTimeDto> times = entry.getValue();

            int i = 0;
            while (i < times.size()) {
                List<InlineKeyboardButton> buttons = new ArrayList<>();
                for (int j = 0; j < 4 && i < times.size(); j++, i++) {
                    AccountTimeDto accountTime = times.get(i);
                    ZonedDateTime zonedDateTime = accountTime.getMeetingTime().getTimeWithZoneOffset(zoneId);
                    InlineKeyboardButton timeBtn =
                            defaultInlineButton(Emojis.GREEN_SELECTED.getEmojiSpace() + zonedDateTime.toLocalTime(),
                                    UpcomingState.UPCOMING_EDIT_MEETING_TIME.name() + " " + meetingId + " " + accountTime.getId());

                    if (accountTime.getStatus().equals(Status.CANCELED)) {
                        timeBtn.setText(zonedDateTime.toLocalTime().toString());
                    }
                    buttons.add(timeBtn);
                }
                keyboard.add(buttons);
            }
        }

        InlineKeyboardButton confirmBtn = defaultInlineButton(Emojis.GREEN_SELECTED.getEmojiSpace() + "Подтверждаю",
                UpcomingState.UPCOMING_CONFIRM_MEETING_TIME.name() + " " + meetingId);

        InlineKeyboardButton cancelBtn = defaultInlineButton(Emojis.CANCEL_CIRCLE.getEmojiSpace() + "Не смогу прийти",
                UpcomingState.UPCOMING_CANCEL_MEETING_TIME.name() + " " + meetingId);

        keyboard.add(List.of(cancelBtn, confirmBtn));
        return buildInlineMarkup(keyboard);
    }


    public InlineKeyboardMarkup getMeetingUpcomingMarkup(long userId, MeetingDto meetingDto,
                                                         Optional<AccountTimeDto> accountTime) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        Long meetingId = meetingDto.getId();
        InlineKeyboardButton prevBtn = defaultInlineButton(Emojis.LEFT.getEmojiSpace() + "Назад", UpcomingState.UPCOMING_MEETINGS.name());

        InlineKeyboardButton googleCalendarBtn = getGoogleCalendarButton(userId, meetingDto);
        if (meetingDto.getOwner().getId() == userId) {
            InlineKeyboardButton pingBtn = defaultInlineButton(Emojis.PIN.getEmojiSpace() + "Пингануть участника",
                    UpcomingState.UPCOMING_SELECT_PARTICIPANT.name() + " " + meetingDto.getId());
            InlineKeyboardButton cancelBtn = defaultInlineButton(Emojis.CANCEL_CIRCLE.getEmojiSpace() + "Отменить встречу",
                    UpcomingState.UPCOMING_CANCEL_BY_OWNER.name() + " " + meetingDto.getId());
            keyboard.add(List.of(googleCalendarBtn));
            keyboard.add(List.of(pingBtn));
            keyboard.add(List.of(cancelBtn, prevBtn));

        } else if (accountTime.isPresent()) {
            InlineKeyboardButton awaitingBtn = defaultInlineButton("Я опаздываю", UpcomingState.UPCOMING_IAMLATE + " " + meetingId);
            InlineKeyboardButton readyBtn = defaultInlineButton("Я готов", UpcomingState.UPCOMING_IAMREADY + " " + meetingId);
            InlineKeyboardButton cancelBtn = defaultInlineButton("Я не приду", UpcomingState.UPCOMING_IWILLNOTCOME + " " + meetingId);
            Status status = accountTime.get().getStatus();
            switch (status) {
                case AWAITING -> {
                    awaitingBtn.setText(Emojis.GREEN_SELECTED.getEmojiSpace() + "Я опаздываю");
                    awaitingBtn.setCallbackData(UpcomingState.UPCOMING_IAMCONFIRM + " " + meetingId);
                }
                case CANCELED -> {
                    cancelBtn.setText(Emojis.GREEN_SELECTED.getEmojiSpace() + "Я не приду");
                    cancelBtn.setCallbackData(UpcomingState.UPCOMING_IAMCONFIRM + " " + meetingId);
                }
                case READY -> {
                    readyBtn.setText(Emojis.GREEN_SELECTED.getEmojiSpace() + "Я готов");
                    readyBtn.setCallbackData(UpcomingState.UPCOMING_IAMCONFIRM + " " + meetingId);
                }
            }
            keyboard.add(List.of(googleCalendarBtn));
            keyboard.add(List.of(awaitingBtn, readyBtn));
            keyboard.add(List.of(prevBtn, cancelBtn));
        }
        return InlineKeyboardMarkup.builder().keyboard(keyboard).build();
    }

    public InlineKeyboardMarkup getUpcomingMeetingsListMarkup(String zoneId, List<MeetingDto> meetings) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        Map<MeetingState, List<MeetingDto>> meetingMap =
                meetings.stream().sorted(MeetingDto::compareTo)
                        .collect(Collectors.groupingBy(MeetingDto::getState));

        for (Map.Entry<MeetingState, List<MeetingDto>> entry : meetingMap.entrySet()) {
            MeetingState entryState = entry.getKey();
            List<MeetingDto> entryValue = entry.getValue();

            if (entryValue.size() > 0) {
                if (entryState.equals(MeetingState.AWAITING)) {
                    InlineKeyboardButton button = defaultInlineButton(
                            Emojis.ORANGE_CIRCLE.getEmojiSpace() + "В ожидании", " ");
                    keyboard.add(List.of(button));
                } else {
                    InlineKeyboardButton button = defaultInlineButton(
                            Emojis.GREEN_CIRCLE.getEmojiSpace() + "Подтвержденные", " ");
                    keyboard.add(List.of(button));
                }

                int i = 0;
                while (i < entryValue.size()) {
                    List<InlineKeyboardButton> rowButtons = new ArrayList<>();
                    for (int j = 0; j < 2 && i < entryValue.size(); j++, i++) {
                        MeetingDto meeting = entryValue.get(i);
                        ZonedDateTime zonedDateTime = meeting.getDate().withZoneSameInstant(ZoneId.of(zoneId));

                        String upcomingMeeting = Emojis.CALENDAR.getEmojiSpace() +
                                zonedDateTime.format(CustomFormatter.DATE_TIME_FORMATTER);

                        String callback = UpcomingState.UPCOMING_SELECTED_MEETING.name() + " " + meeting.getId();

                        InlineKeyboardButton button =
                                defaultInlineButton(upcomingMeeting, callback);

                        rowButtons.add(button);
                    }
                    keyboard.add(rowButtons);
                }
            }
        }
        return buildInlineMarkup(keyboard);
    }
}
