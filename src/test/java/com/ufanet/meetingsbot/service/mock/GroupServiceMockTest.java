package com.ufanet.meetingsbot.service.mock;

import com.ufanet.meetingsbot.entity.Group;
import com.ufanet.meetingsbot.repository.GroupRepository;
import com.ufanet.meetingsbot.service.GroupService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class GroupServiceMockTest {
    @InjectMocks
    private GroupService groupService;
    @Mock
    private GroupRepository groupRepository;

    private Group dummyGroup(long id) {
        return Group.builder().createdDt(LocalDateTime.now())
                .id(id).description("description").title("title")
                .build();
    }

    @Test
    public void shouldReturnGroupWhenGetByGroupId() {
        //given
        Group dummyGroup = dummyGroup(1L);

        //when + then
        Mockito.when(groupRepository.findById(Mockito.anyLong())).thenReturn(Optional.ofNullable(dummyGroup));
        Optional<Group> group = groupService.getByGroupId(Mockito.anyShort());

        Assertions.assertTrue(group.isPresent());
        Assertions.assertEquals(group.get().getId(), dummyGroup.getId());
        Assertions.assertEquals(group.get().getDescription(), dummyGroup.getDescription());
        Assertions.assertEquals(group.get().getTitle(), dummyGroup.getTitle());
        Assertions.assertEquals(group.get().getCreatedDt(), dummyGroup.getCreatedDt());
    }

    @Test
    public void shouldReturnGroupWhenSave() {
        //given
        Group dummyGroup = dummyGroup(1L);

        //when + then
        Mockito.when(groupRepository.save(Mockito.any(Group.class))).thenReturn(dummyGroup);
        Group group = groupService.save(dummyGroup);

        Assertions.assertNotNull(group);
        Assertions.assertEquals(group.getId(), dummyGroup.getId());
        Assertions.assertEquals(group.getDescription(), dummyGroup.getDescription());
        Assertions.assertEquals(group.getTitle(), dummyGroup.getTitle());
        Assertions.assertEquals(group.getCreatedDt(), dummyGroup.getCreatedDt());
    }

    @Test
    public void shouldReturnGroupsWhenGetByMemberId() {
        //given
        List<Group> dummyGroups = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Group dummy = dummyGroup(i);
            dummyGroups.add(dummy);
        }
        //when + then
        Mockito.when(groupRepository.findGroupsByMemberId(Mockito.anyLong())).thenReturn(dummyGroups);
        List<Group> groups = groupService.getGroupsByMemberId(Mockito.anyLong());

        Assertions.assertNotNull(groups);
        Assertions.assertFalse(groups.isEmpty());
        Assertions.assertSame(groups, dummyGroups);
        Assertions.assertEquals(groups.size(), dummyGroups.size());
    }
}
