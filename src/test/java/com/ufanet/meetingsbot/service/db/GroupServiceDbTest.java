package com.ufanet.meetingsbot.service.db;

import com.ufanet.meetingsbot.annotation.DatabaseTest;
import com.ufanet.meetingsbot.entity.Group;
import com.ufanet.meetingsbot.service.GroupService;
import com.ufanet.meetingsbot.service.ServiceTestConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.util.Optional;


@DatabaseTest
@ContextConfiguration(classes = ServiceTestConfiguration.class)
public class GroupServiceDbTest {

    @Autowired
    private GroupService groupService;

    @Test
    void shouldReturnGroup_whenSaveInDatabase() {
        Group group = Group.builder()
                .id(123L)
                .title("Java")
                .description("Beginners group")
                .build();

        Group saved = groupService.save(group);

        Assertions.assertNotNull(saved);
        Assertions.assertNotNull(saved.getId());
        Assertions.assertEquals(saved.getId(), group.getId());
        Assertions.assertEquals(saved.getTitle(), group.getTitle());
        Assertions.assertEquals(saved.getDescription(), group.getDescription());
    }

    @Test
    void shouldReturnGroup_whenGetByGroupId() {
        Optional<Group> group = groupService.getByGroupId(1);

        Assertions.assertTrue(group.isPresent());
        Assertions.assertNotNull(group.get().getId());
        Assertions.assertNotNull(group.get().getCreatedDt());
        Assertions.assertNotNull(group.get().getId());
        Assertions.assertNotNull(group.get().getTitle());
        Assertions.assertNotNull(group.get().getDescription());
    }

    @Test
    void shouldDeleteFromDatabaseById_whenSaveAndDelete() {
        long groupId = 1L;
        Optional<Group> selected = groupService.getByGroupId(groupId);
        Assertions.assertTrue(selected.isPresent());

        groupService.deleteById(groupId);

        Optional<Group> deleted = groupService.getByGroupId(groupId);
        Assertions.assertTrue(deleted.isEmpty());
    }
}
