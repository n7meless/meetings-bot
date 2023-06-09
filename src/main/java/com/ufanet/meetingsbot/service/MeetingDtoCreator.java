package com.ufanet.meetingsbot.service;

import com.ufanet.meetingsbot.dto.MeetingDto;
import com.ufanet.meetingsbot.model.Meeting;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class MeetingDtoCreator {

    public void updateGroup(MeetingDto meetingDto, Long groupId) {
        meetingDto.setChatId(groupId);
    }

    public void updateParticipants(MeetingDto meetingDto, Long participantId) {
        List<Long> participants = meetingDto.getUserIds();
        if (participants.contains(participantId)) {
            participants.remove(participantId);
        } else participants.add(participantId);
    }

    public void updateSubject(MeetingDto meetingDto, String subject) {
        meetingDto.setSubjectName(subject);
    }

    public void updateQuestion(MeetingDto meetingDto, String question) {
        List<String> questions = meetingDto.getQuestions();
        if (questions.contains(question)) {
            questions.remove(question);
        } else questions.add(question);
    }

    public void updateDate(MeetingDto meetingDto, String date) {
        Map<String, List<String>> dateTime = meetingDto.getDateTime();
        if (dateTime.containsKey(date)) {
            dateTime.remove(date);
        } else dateTime.put(date, new ArrayList<>());
    }

    public void updateTime(MeetingDto meetingDto, String time) {
        Map<String, List<String>> dateTime = meetingDto.getDateTime();
        String date = meetingDto.getCurrentDate();
        List<String> times = dateTime.get(date);
        if (times.contains(time)){
            times.remove(time);
        }else times.add(time);
    }

    public void updateAddress(MeetingDto meetingDto, String address) {
        meetingDto.setAddress(address);
    }

    public Meeting mapToEntity(MeetingDto meetingDto) {
        return new Meeting();
    }
}
