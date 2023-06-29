package com.ufanet.meetingsbot.dto;

import com.ufanet.meetingsbot.model.AccountTime;
import com.ufanet.meetingsbot.model.MeetingDate;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

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
public class MeetingTimeDto {
    private Long id;
    private ZonedDateTime dateTime;
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
}
