package com.ufanet.meetingsbot.dto;

import com.ufanet.meetingsbot.entity.MeetingTime;
import lombok.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingTimeDto implements Comparable<MeetingTimeDto>{
    private Long id;
    private ZonedDateTime dateTime;
    private MeetingDateDto meetingDate;
    @Builder.Default
    private Set<AccountTimeDto> accountTimes = new HashSet<>();

    public ZonedDateTime getTimeWithZoneOffset(String zoneId) {
        return this.dateTime.withZoneSameInstant(ZoneId.of(zoneId));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MeetingTimeDto that = (MeetingTimeDto) o;
        return Objects.equals(dateTime, that.dateTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dateTime);
    }

    @Override
    public int compareTo(MeetingTimeDto meetingTime) {
        return this.getDateTime().compareTo(meetingTime.getDateTime());
    }
}
