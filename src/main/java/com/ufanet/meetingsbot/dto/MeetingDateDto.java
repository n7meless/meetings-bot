package com.ufanet.meetingsbot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MeetingDateDto {
    private Long id;
    private LocalDate date;
    @Builder.Default
    private Set<MeetingTimeDto> meetingTimes = new HashSet<>();

    public void removeTimeIf(Predicate<? super MeetingTimeDto> predicate) {
        this.meetingTimes.removeIf(predicate);
    }

    public Set<MeetingTimeDto> getMeetingTimes() {
        return meetingTimes == null ? new HashSet<>() : meetingTimes;
    }
}
