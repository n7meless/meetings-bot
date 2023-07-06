package com.ufanet.meetingsbot.dto;

import lombok.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingTimeDto implements Comparable<MeetingTimeDto> {
    private Long id;
    private ZonedDateTime dateTime;
    private MeetingDateDto meetingDate;
    @Builder.Default
    private List<AccountTimeDto> accountTimes = new ArrayList<>();

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
