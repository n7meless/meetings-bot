package com.ufanet.meetingsbot.dto;

import com.ufanet.meetingsbot.entity.MeetingDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MeetingDateDto implements Comparable<MeetingDateDto> {
    private Long id;
    private LocalDate date;
    private MeetingDto meeting;
    @Builder.Default
    private Set<MeetingTimeDto> meetingTimes = new HashSet<>();

    public void removeTimeIf(Predicate<? super MeetingTimeDto> predicate) {
        this.meetingTimes.removeIf(predicate);
    }

    public Set<MeetingTimeDto> getMeetingTimes() {
        return meetingTimes == null ? new HashSet<>() : meetingTimes;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MeetingDateDto date = (MeetingDateDto) o;
        return this.getDate().isEqual(date.getDate());
    }

    @Override
    public int hashCode() {
        return Objects.hash(date);
    }

    @Override
    public int compareTo(MeetingDateDto meetingDate) {
        return this.getDate().compareTo(meetingDate.getDate());
    }
}
